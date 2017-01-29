package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import tuc.werkstatt.doubleup.Network.ClientFinishedMessage;
import tuc.werkstatt.doubleup.Network.ClientOptionsMessage;
import tuc.werkstatt.doubleup.Network.ClientProgressMessage;
import tuc.werkstatt.doubleup.Network.ExitMessage;
import tuc.werkstatt.doubleup.Network.GameFinishedMessage;
import tuc.werkstatt.doubleup.Network.GameNextMessage;
import tuc.werkstatt.doubleup.Network.GameOptionsMessage;
import tuc.werkstatt.doubleup.Network.GameProgressMessage;

public class Client {
    private final DoubleUp game;
    private final Object lock = new Object();
    private com.esotericsoftware.kryonet.Client netClient;
    private MiniGame currMiniGame = null;
    private String currMiniGameName = null;
    private Player[] players;
    private int currMiniGameRound = 0;
    private int maxMiniGameRounds = 0;
    private ClientProgressMessage progressMsg;
    private int numReadyPlayers = 0;

    public Client(DoubleUp game) {
        this.game = game;
        progressMsg = new ClientProgressMessage();

        netClient = new com.esotericsoftware.kryonet.Client();
        Network.registerClasses(netClient.getKryo());
        netClient.start();
        netClient.addListener(new Listener() {
            public void connected (Connection connection) {
                onConnected(connection);
            }
            public void disconnected (Connection connection) {
                onDisconnected(connection);
            }
            public void received (Connection connection, Object object) {
                if (object instanceof Network.ExitMessage) {
                    onExitMessage((ExitMessage) object);
                } else if (object instanceof GameFinishedMessage) {
                    onGameFinishedMessage((GameFinishedMessage) object);
                } else if (object instanceof GameNextMessage) {
                    onGameNextMessage((GameNextMessage) object);
                } else if (object instanceof GameProgressMessage) {
                    onGameProgressMessage((GameProgressMessage) object);
                } else if (object instanceof GameOptionsMessage) {
                    onGameOptionsMessage((GameOptionsMessage) object);
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectToHost(discoverHost());
            }
        }).start();
    }

    private void onConnected(Connection connection) {}

    private void onDisconnected(Connection connection) {
        game.setScreen(new Start(game));
    }

    public boolean isConnected() { return netClient.isConnected(); }

    public int getID() { return netClient.getID(); }

    public Player[] getPlayers() {
        if (players != null) {
            synchronized (lock) {
                return Arrays.copyOf(players, players.length);
            }
        } else {
            return null;
        }
    }
    public int getNumPlayers() {
        if (players != null) {
            synchronized (lock) {
                return players.length;
            }
        }
        return 0;
    }

    public int getNumReadyPlayers() {
        synchronized (lock) {
            return numReadyPlayers;
        }
    }

    private String discoverHost() {
        final int timeout = 5000;
        String hostAddress;
        Gdx.app.log("Client", "Trying to discover host");
        while(true) {
            InetAddress host = netClient.discoverHost(Network.udpPort, timeout);
            if (host != null) {
                hostAddress = host.getHostAddress();
                break;
            }
        }
        return hostAddress;
    }

    private void connectToHost(String hostAddress) {
        if (hostAddress == null || hostAddress.equals("")) {
            Gdx.app.log("Client", "Invalid host address");
            Gdx.app.exit();
            return;
        }
        final int timeout = 5000;
        final int tries = 3;
        for (int i = 0; i < tries; ++i) {
            try {
                netClient.connect(timeout, hostAddress, Network.tcpPort, Network.udpPort);
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!isConnected()) {
            Gdx.app.log("Client", "Connecting to server failed");
            Gdx.app.exit();
        }
    }

    public void setCurrMinigame(MiniGame minigame) {
        synchronized (lock) {
            currMiniGame = minigame;
            currMiniGameName = minigame.getClass().getSimpleName();
        }
    }

    public String getCurrMinigameName() {
        synchronized (lock) {
            return currMiniGameName;
        }
    }

    public int getMaxMiniGameRounds() {
        return maxMiniGameRounds;
    }

    public int getCurrMiniGameRound() {
        return currMiniGameRound;
    }

    private void onExitMessage(ExitMessage msg) {
        Gdx.app.log("Client", "ExitMessage received");
        //TODO ExitMessage msg = (ExitMessage)object;
        //TODO msg.overallclientWinnerID
        Gdx.app.exit();
    }

    private void onGameFinishedMessage(GameFinishedMessage msg) {
        Gdx.app.log("Client", "GameFinishedMessage received");
        //TODO msg.clientWinnerID
        synchronized (lock) {
            if (currMiniGameName.equals(game.minigames[msg.gameID])) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        currMiniGame.exit();
                    }
                });
            }
        }
    }

    private void onGameNextMessage(GameNextMessage msg) {
        Gdx.app.log("Client", "GameNextMessage received");
        final int ID = msg.gameID;
        synchronized (lock) {
            currMiniGameRound = msg.currRound;
            if (players != null) {
                for (Player p : players) {
                    p.miniGameProgress = 0;
                }
            }
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    game.loadMiniGame(ID);
                }
            });
        }
    }

    private void onGameOptionsMessage(GameOptionsMessage msg) {
        synchronized (lock) {
            players = msg.players;
            maxMiniGameRounds = msg.maxMiniGameRounds;
            numReadyPlayers = msg.numReady;
            if (!Network.isHosting) {
                GameOptions.sequence = msg.sequence;
            }
        }
    }

    private void onGameProgressMessage(GameProgressMessage msg) {
        synchronized (lock) {
            if (currMiniGameName.equals(game.minigames[msg.gameID])) {
                players = msg.players;
            }
        }
    }
    public void sendClientFinishedMessage() {
        Gdx.app.log("Client", "ClientFinishedMessage sent");
        ClientFinishedMessage msg = new ClientFinishedMessage();
        synchronized (lock) {
            msg.gameID = Arrays.asList(game.minigames).indexOf(currMiniGameName);
            msg.clientID = netClient.getID();
        }
        netClient.sendTCP(msg);
        game.setScreen(null);
    }

    public void sendClientProgressMessage(final float progress) {
        progressMsg.gameID = -1;
        for (int i = 0; i < game.minigames.length; ++i) {
            if (currMiniGameName.equals(game.minigames[i])) {
                progressMsg.gameID = i;
                break;
            }
        }
        progressMsg.clientID = netClient.getID();
        progressMsg.progress = progress;
        netClient.sendUDP(progressMsg);
    }

    public void sendClientOptionsMessage(int icon) {
        Gdx.app.log("Client", "ClientOptionsMessage sent");
        ClientOptionsMessage msg = new ClientOptionsMessage();
        msg.clientID = netClient.getID();
        msg.icon = icon;
        netClient.sendTCP(msg);
    }

    public void dispose() {
        if (netClient != null) { netClient.stop(); }
    }
}
