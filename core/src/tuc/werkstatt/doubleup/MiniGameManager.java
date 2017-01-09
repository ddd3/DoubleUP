package tuc.werkstatt.doubleup;

import com.badlogic.gdx.utils.IntIntMap;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import tuc.werkstatt.doubleup.network.ClientFinishedMessage;
import tuc.werkstatt.doubleup.network.ClientProgressMessage;
import tuc.werkstatt.doubleup.network.ExitMessage;
import tuc.werkstatt.doubleup.network.GameFinishedMessage;
import tuc.werkstatt.doubleup.network.GameNextMessage;

public class MiniGameManager {
	private final DoubleUp game;
    private int currMiniGameID = -1;
    private int currRound = 0;
    private final int numRounds = 3;
    private IntIntMap players = new IntIntMap(); // ID and points

    public MiniGameManager(final DoubleUp game) {
		this.game = game;

        System.out.println("Num connected: " + game.server.getConnections().length + " clients");
        for (Connection conn : game.server.getConnections()) {
            players.put(conn.getID(), 0);
        }
        game.server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof ClientFinishedMessage) {
                    ClientFinishedMessage msg = (ClientFinishedMessage) object;
                    if (msg.gameID == currMiniGameID) {
                        System.out.println("Server: ClientFinishedMessage received, ID: " + msg.clientID);
                        clientFinished(msg.gameID, msg.clientID);
                    }
                } else if (object instanceof ClientProgressMessage) {
                    ClientProgressMessage msg = (ClientProgressMessage) object;
                    clientProgressReceived(msg.gameID, msg.clientID, msg.progress);
                }
            }
        });
        startNextMiniGame();
    }

    public void clientFinished(int finGameID, int clientID) {
        // send msg to all clients, saying the current game has been won by a player
        GameFinishedMessage finMsg = new GameFinishedMessage();
        finMsg.gameID = finGameID;
        finMsg.clientWinnerID = clientID;
        game.server.sendToAllTCP(finMsg);
        System.out.println("Server: GameFinishedMessage sent");

        players.getAndIncrement(clientID, 0, 1);

        if (currRound < numRounds) {
            startNextMiniGame();
        } else {
            // all games were played, quitting, submitting overall winner to all players
            // TODO: multiple winners should be possible
            int winnerID = players.keys().next();
            int points = players.get(winnerID, 0);
            for (IntIntMap.Entry entry : players.entries()) {
                if (entry.value >= points) {
                    winnerID = entry.key;
                    points = entry.value;
                }
            }
            ExitMessage exMsg = new ExitMessage();
            exMsg.overallclientWinnerID = winnerID;
            game.server.sendToAllTCP(exMsg);
            System.out.println("Server: ExitMessage sent");
            game.server.stop();
        }
    }

    public void clientProgressReceived(int gameID, int clientID, int progress) {
        // TODO: implement progress on clients and server
    }

    private void startNextMiniGame() {
        final int nextGameID;
        if (game.isTestingEnvironment()) {
            nextGameID = game.minigames.indexOf(game.getTestingMiniGame(), false);
        } else {
            //nextGameID = MathUtils.random(game.minigames.size - 1); // randomly
            nextGameID = currRound % game.minigames.size; // sequential
        }
        ++currRound;
        currMiniGameID = nextGameID;
        GameNextMessage nextMsg = new GameNextMessage();
        nextMsg.gameID = nextGameID;
        game.server.sendToAllTCP(nextMsg);
        System.out.println("Server: GameNextMessage sent: " + game.minigames.get(nextGameID));
    }
}
