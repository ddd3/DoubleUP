package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.Arrays;

import tuc.werkstatt.doubleup.Network.ClientFinishedMessage;
import tuc.werkstatt.doubleup.Network.ClientOptionsMessage;
import tuc.werkstatt.doubleup.Network.ClientProgressMessage;
import tuc.werkstatt.doubleup.Network.ExitMessage;
import tuc.werkstatt.doubleup.Network.GameFinishedMessage;
import tuc.werkstatt.doubleup.Network.GameNextMessage;
import tuc.werkstatt.doubleup.Network.GameOptionsMessage;
import tuc.werkstatt.doubleup.Network.GameProgressMessage;

public class Server {
	private final DoubleUp game;
    private final Object lock = new Object();
    private com.esotericsoftware.kryonet.Server netServer;
    private int currMiniGameID = -1;
    private int currMiniGameRound = 0;
    private long lastProgressTime;
    private GameProgressMessage progressMsg;
    private GameOptionsMessage optionsMessage;
    private Player[] players;
    private Array<String> availableIcons;

    public Server(final DoubleUp game) {
		this.game = game;
        progressMsg = new GameProgressMessage();
        optionsMessage = new GameOptionsMessage();
        availableIcons = new Array<String>(GameOptions.animalNames);

        netServer = new com.esotericsoftware.kryonet.Server();
        Network.registerClasses(netServer.getKryo());
        netServer.start();
        try {
            netServer.bind(Network.tcpPort, Network.udpPort);
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }

        netServer.addListener(new Listener() {
            public void connected(Connection connection) {
                onConnected(connection);
            }
            public void disconnected(Connection connection) {
                onDisconnected(connection.getID());
            }
            public void received(Connection connection, Object object) {
                if (object instanceof ClientFinishedMessage) {
                    onClientFinishedMessage((ClientFinishedMessage) object);
                } else if (object instanceof ClientProgressMessage) {
                    onClientProgressMessage((ClientProgressMessage) object);
                } else if (object instanceof ClientOptionsMessage) {
                    onClientOptionsMessage((ClientOptionsMessage) object);
                }
            }
        });
    }

    private void onConnected(Connection connection) {
        final int id = connection.getID();
        if (netServer.getConnections().length > GameOptions.maxPlayers) {
            connection.close();
            Gdx.app.log("Server", "Client " + id + " connected, but is rejected due to max player limit");
            return;
        }
        if (Network.state != Network.State.Lobby && Network.state != Network.State.GameOptions) {
            connection.close();
            Gdx.app.log("Server", "Client " + id + " connected, but is rejected due to ongoing game session");
            return;
        }
        Gdx.app.log("Server", "Client connected: " + id);
        Player p = new Player();
        p.ID = id;
        p.icon = -1;
        p.miniGameProgress = 0;
        p.points = 0;
        synchronized (lock) {
            if (players != null) {
                players = Arrays.copyOf(players, players.length + 1);
            } else {
                players = new Player[1];
            }
            players[players.length - 1] = p;
        }
    }

    private void onDisconnected(int id) {
        Gdx.app.log("Server", "Client disconnected: " + id);
        synchronized (lock) {
            if (players == null) { return; }
            for (int i = 0; i < players.length; ++i) {
                if (players[i].ID == id) {
                    if (players.length > 1) {
                        players[i] = players[players.length - 1];
                        players = Arrays.copyOf(players, players.length - 1);
                    } else {
                        players = null;
                    }
                    return;
                }
            }
        }
        if (netServer.getConnections().length <= 0) {
            Gdx.app.log("Server", "Last client disconnected, shutting down");
            Gdx.app.exit();
        }
    }

    public boolean areAllPlayersReady() {
        synchronized (lock) {
            return GameOptions.animalNames.length == availableIcons.size + players.length;
        }
    }

    private int numOfReadyPlayers() {
        synchronized (lock) {
            return GameOptions.animalNames.length - availableIcons.size;
        }
    }

    public void startMiniGameSession() {
        sendOptionsMessage();
        sendGameNextMessage();
    }

    public void update() {
        if (TimeUtils.timeSinceMillis(lastProgressTime) > 500) {
            switch (Network.state) {
                case GameOptions:
                    sendOptionsMessage();
                    break;
                case Minigame:
                    sendProgressMessage();
                    break;
                default:
                    break;
            }
            lastProgressTime = TimeUtils.millis();
        }
    }

    private void givePointsToPlayer(int id, int points) {
        synchronized (lock) {
            for (Player p : players) {
                if (p.ID == id) {
                    p.points += points;
                    break;
                }
            }
        }
    }

    private void onClientFinishedMessage(ClientFinishedMessage msg) {
        if (msg.gameID != currMiniGameID) { return; }
        Gdx.app.log("Server", "ClientFinishedMessage received from client " + msg.clientID);
        sendGameFinishedMessage(msg.clientID);
        givePointsToPlayer(msg.clientID, 1);

        if (currMiniGameRound < GameOptions.maxMiniGameRounds) {
            sendGameNextMessage();
        } else {
            sendExitMessageAndStop();
        }
    }

    private void onClientProgressMessage(ClientProgressMessage msg) {
        if (msg.gameID != currMiniGameID) { return; }
        synchronized (lock) {
            for (Player p : players) {
                if (p.ID == msg.clientID) {
                    p.miniGameProgress = msg.progress;
                    break;
                }
            }
        }
    }

    private void onClientOptionsMessage(ClientOptionsMessage msg) {
        Gdx.app.log("Server", "ClientOptionsMessage received from client " + msg.clientID);
        String name = GameOptions.animalNames[msg.icon];
        synchronized (lock) {
            if (availableIcons.contains(name, false)) {
                for (Player p : players) {
                    if (p.ID != msg.clientID) { continue; }
                    if (p.icon != -1) {
                        availableIcons.add(GameOptions.animalNames[p.icon]);
                    }
                    availableIcons.removeValue(name, false);
                    p.icon = msg.icon;
                    break;
                }
            }
        }
        if (game.isTestingEnvironment()) {
            startMiniGameSession();
        }
    }

    private void sendGameNextMessage() {
        int nextGameID;
        if (game.isTestingEnvironment()) {
            nextGameID = Arrays.asList(game.minigames).indexOf(game.getTestingMiniGame());
        } else {
            if (GameOptions.sequence == GameOptions.Sequence.Random && game.minigames.length > 1) {
                do {
                    nextGameID = MathUtils.random(game.minigames.length - 1);
                } while(nextGameID == currMiniGameID);
            } else {
                nextGameID = currMiniGameRound % game.minigames.length;
            }
        }
        ++currMiniGameRound;
        currMiniGameID = nextGameID;
        GameNextMessage nextMsg = new GameNextMessage();
        nextMsg.gameID = nextGameID;
        nextMsg.currRound = currMiniGameRound;
        netServer.sendToAllTCP(nextMsg);
        Gdx.app.log("Server", "GameNextMessage (" + game.minigames[nextGameID] + ") sent to all clients");

        synchronized (lock) {
            for (Player p : players) {
                p.miniGameProgress = 0;
            }
        }
        lastProgressTime = TimeUtils.millis();
    }

    private void sendGameFinishedMessage(int winnerID) {
        GameFinishedMessage finMsg = new GameFinishedMessage();
        finMsg.gameID = currMiniGameID;
        finMsg.clientWinnerID = winnerID;
        netServer.sendToAllTCP(finMsg);
        Gdx.app.log("Server", "GameFinishedMessage sent to all clients");
    }

    private void sendProgressMessage() {
        synchronized (lock) {
            progressMsg.gameID = currMiniGameID;
            /* // not necessary with player icons, simply draw the device's player on top
            Arrays.sort(players, new Comparator<Player>() {
                @Override
                public int compare(Player p1, Player p2) {
                    return p1.miniGameProgress < p2.miniGameProgress ? 1 : -1;
                }
            });
            */
            progressMsg.players = players;
            netServer.sendToAllTCP(progressMsg);
        }
    }

    private void sendOptionsMessage() {
        synchronized (lock) {
            optionsMessage.maxMiniGameRounds = GameOptions.maxMiniGameRounds;
            optionsMessage.players = players;
            optionsMessage.numReady = numOfReadyPlayers();
            optionsMessage.sequence = GameOptions.sequence;
            netServer.sendToAllTCP(optionsMessage);
        }
    }

    private void sendExitMessageAndStop() {
        // TODO: multiple winners should be possible
        // TODO: implement endscreen
        ExitMessage exMsg = new ExitMessage();
        exMsg.overallclientWinnerID = -1;
        netServer.sendToAllTCP(exMsg);
        Gdx.app.log("Server", "ExitMessage sent to all clients");
        netServer.stop();
    }

    public void dispose() {
        if (netServer != null) { netServer.stop(); }
    }
}
