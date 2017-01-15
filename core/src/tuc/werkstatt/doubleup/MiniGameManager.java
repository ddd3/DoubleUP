package tuc.werkstatt.doubleup;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.Timer;
import java.util.TimerTask;

import tuc.werkstatt.doubleup.network.ClientFinishedMessage;
import tuc.werkstatt.doubleup.network.ClientProgressMessage;
import tuc.werkstatt.doubleup.network.ExitMessage;
import tuc.werkstatt.doubleup.network.GameFinishedMessage;
import tuc.werkstatt.doubleup.network.GameNextMessage;
import tuc.werkstatt.doubleup.network.GameProgressMessage;

public class MiniGameManager {
	private final DoubleUp game;
    private int currMiniGameID = -1;
    private int currMiniGameRound = 0;
    private int maxMiniGameRounds = 5;
    private Array<Player> players;

    private Timer sendProgressTimer;
    private TimerTask sendProgressTimerTask;
    private GameProgressMessage progressMsg;

    public MiniGameManager(final DoubleUp game) {
		this.game = game;

        sendProgressTimer = new Timer();
        progressMsg = new GameProgressMessage();

        players = new Array<Player>(false, game.server.getConnections().length, Player.class);

        System.out.println("Num connected: " + game.server.getConnections().length + " clients");
        for (Connection conn : game.server.getConnections()) {
            Player player = new Player();
            player.ID = conn.getID();
            player.color8888 = Color.rgba8888(MathUtils.random(1f), MathUtils.random(1f), MathUtils.random(1f), 1f);
            player.miniGameProgress = 0;
            player.points = 0;
            players.add(player);
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
                    if (msg.gameID == currMiniGameID) {
                        clientProgressReceived(msg.gameID, msg.clientID, msg.progress);
                    }
                }
            }
        });
        startNextMiniGame();
    }

    private void clientFinished(int finGameID, int clientID) {
        if (sendProgressTimerTask != null) {
            sendProgressTimerTask.cancel();
        }
        // send msg to all clients, saying the current game has been won by a player
        GameFinishedMessage finMsg = new GameFinishedMessage();
        finMsg.gameID = finGameID;
        finMsg.clientWinnerID = clientID;
        game.server.sendToAllTCP(finMsg);
        System.out.println("Server: GameFinishedMessage sent");

        for (Player p : players) {
            if (p.ID == clientID) {
                p.points++;
                break;
            }
        }

        if (currMiniGameRound < maxMiniGameRounds) {
            startNextMiniGame();
        } else {
            // all games were played, quitting, submitting overall winner to all players
            // TODO: multiple winners should be possible
            int winnerID = -1;
            int points = 0;
            for (Player p : players) {
                if (p.points >= points) {
                    winnerID = p.ID;
                    points = p.points;
                }
            }
            ExitMessage exMsg = new ExitMessage();
            exMsg.overallclientWinnerID = winnerID;
            game.server.sendToAllTCP(exMsg);
            System.out.println("Server: ExitMessage sent");
            game.server.stop();
        }
    }

    private void startNextMiniGame() {
        final int nextGameID;
        if (game.isTestingEnvironment()) {
            nextGameID = game.minigames.indexOf(game.getTestingMiniGame(), false);
        } else {
            //nextGameID = MathUtils.random(game.minigames.size - 1); // randomly
            nextGameID = currMiniGameRound % game.minigames.size; // sequential
        }
        ++currMiniGameRound;
        currMiniGameID = nextGameID;
        GameNextMessage nextMsg = new GameNextMessage();
        nextMsg.gameID = nextGameID;
        nextMsg.maxRounds = maxMiniGameRounds;
        nextMsg.currRound = currMiniGameRound;
        game.server.sendToAllTCP(nextMsg);
        System.out.println("Server: GameNextMessage sent: " + game.minigames.get(nextGameID));

        for (Player p : players) {
            p.miniGameProgress = 0;
        }

        sendProgressTimerTask = new TimerTask() {
            @Override
            public void run() {
                sendProgressToClients();
            }
        };
        final long sendProgressDelay = 500;
        sendProgressTimer.scheduleAtFixedRate(sendProgressTimerTask, sendProgressDelay / 2, sendProgressDelay);
    }

    private void clientProgressReceived(int gameID, int clientID, float progress) {
        for (Player p : players) {
            if (p.ID == clientID) {
                p.miniGameProgress = progress;
                break;
            }
        }
    }

    private void sendProgressToClients() {
        progressMsg.gameID = currMiniGameID;
        progressMsg.players = this.players.items;
        game.server.sendToAllUDP(progressMsg);
    }
}
