package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Comparator;

import tuc.werkstatt.doubleup.network.ClientFinishedMessage;
import tuc.werkstatt.doubleup.network.ClientProgressMessage;
import tuc.werkstatt.doubleup.network.ExitMessage;
import tuc.werkstatt.doubleup.network.GameFinishedMessage;
import tuc.werkstatt.doubleup.network.GameNextMessage;
import tuc.werkstatt.doubleup.network.GameProgressMessage;

public class Lobby implements Screen {
    private final DoubleUp game;
    private final boolean isHosting;
    final int tcpPort = 54545;
    final int udpPort = 54544;
    Thread managerThread = null;

    public Lobby(DoubleUp game, boolean isHosting) {
        this.game = game;
        this.isHosting = isHosting;

        if (isHosting) {
            initServer();
        }
        initClient();
    }

    @Override
    public void show() {}

    @Override
    public void render(float deltaTime) {
        game.uiView.apply();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.setProjectionMatrix(game.uiCamera.combined);
        game.batch.begin();
        if (isHosting) {
            game.font.draw(game.batch, "Lobby (Host): " + game.server.getConnections().length +
                    " client(s)", 10, game.font.getLineHeight());
            if (game.server.getConnections().length > 0) {
                game.font.setColor(Color.GREEN);
                game.font.draw(game.batch, "Click/touch to start", 10, game.font.getLineHeight() * 2);
            } else {
                game.font.setColor(Color.RED);
                game.font.draw(game.batch, "At least one client required to start", 10, game.font.getLineHeight() * 2);
            }
            game.font.setColor(Color.WHITE);
        } else {
            game.font.draw(game.batch, "Lobby (Client): " + (game.client.isConnected() ?
                    "connected" : "discovering host"), 10, game.font.getLineHeight());
        }
        game.batch.end();

        updateLogic(deltaTime);
    }

    private void updateLogic(float deltaTime) {
        //TODO: android back button and keyboard escape should return to startscreen
        if (!isHosting || game.server.getConnections().length <= 0) {
            return;
        }

        if (game.isTestingEnvironment()) {
            startMiniGameManger();
        }
        else if (Gdx.input.justTouched()) {
            startMiniGameManger();
        }
    }

    private void startMiniGameManger() {
        if (managerThread == null) {
            managerThread = new Thread(new Runnable() {
                @Override
                public void run() { // TODO: is this (regarding server and client) thread-safe?
                    new MiniGameManager(game);
                }
            });
            managerThread.start();
        }
    }

    private void initServer() {
        game.server = new Server();
        registerClasses(game.server.getKryo());
        game.server.start();
        try {
            game.server.bind(tcpPort, udpPort);
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
    }

    private void initClient() {
        game.client = new Client();
        registerClasses(game.client.getKryo());
        game.client.start();

        game.client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof ExitMessage) {
                    System.out.println("Client: ExitMessage received");
                    //TODO ExitMessage msg = (ExitMessage)object;
                    //TODO msg.overallclientWinnerID
                    Gdx.app.exit();
                }
                else if (object instanceof GameFinishedMessage) {
                    System.out.println("Client: GameFinishedMessage received");
                    GameFinishedMessage msg = (GameFinishedMessage) object;
                    //TODO msg.clientWinnerID
                    if (game.currMiniGame.getClass().getSimpleName().equals(game.minigames.get(msg.gameID))) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                game.currMiniGame.exit();
                            }
                        });
                    }
                }
                else if (object instanceof GameNextMessage) {
                    System.out.println("Client: GameNextMessage received");
                    GameNextMessage msg = (GameNextMessage) object;
                    final int ID = msg.gameID;
                    final int currRound = msg.currRound;
                    final int maxRounds = msg.maxRounds;
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            game.loadMiniGame(game.minigames.get(ID));
                            game.currMiniGame.init(currRound, maxRounds);
                            if (game.currMiniGame.players != null) {
                                for (Player p : game.currMiniGame.players) {
                                    p.miniGameProgress = 0;
                                }
                            }
                        }
                    });
                }
                else if (object instanceof GameProgressMessage) {
                    GameProgressMessage msg = (GameProgressMessage) object;
                    if (game.currMiniGame.getClass().getSimpleName().equals(game.minigames.get(msg.gameID))) {
                        game.currMiniGame.players = msg.players;
                        Arrays.sort(game.currMiniGame.players, new Comparator<Player>() {
                            @Override
                            public int compare(Player p1, Player p2) {
                                return p1.miniGameProgress < p2.miniGameProgress ? 1 : -1;
                            }
                        });
                    }
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                discoverHost();
            }
        }).start();
    }

    private void discoverHost() {
        final int timeout = 5000;
        String hostAddress;
        System.out.println("Client: Trying to discover host");
        while(true) {
            InetAddress host = game.client.discoverHost(udpPort, timeout);
            if (host != null) {
                hostAddress = host.getHostAddress();
                break;
            }
        }
        connectToHost(hostAddress);
    }

    private void connectToHost(String hostAddress) {
        final int timeout = 5000;
        final int retries = 3;
        for (int i = 0; i < retries; ++i) {
            try {
                game.client.connect(timeout, hostAddress, tcpPort, udpPort);
                break;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Client: Connecting to server failed.");
            }
        }
        if (!game.client.isConnected()) {
            Gdx.app.exit();
        }
    }

    private void registerClasses(Kryo kryo) {
        kryo.register(GameNextMessage.class);
        kryo.register(GameFinishedMessage.class);
        kryo.register(GameProgressMessage.class);
        kryo.register(ClientProgressMessage.class);
        kryo.register(ClientFinishedMessage.class);
        kryo.register(ExitMessage.class);
        kryo.register(Player[].class);
        kryo.register(Player.class);
    }

    @Override
    public void resize(int width, int height) {
        game.resizeViews();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
