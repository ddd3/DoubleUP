package tuc.werkstatt.doubleup;

import com.esotericsoftware.kryo.Kryo;

public class Network {
    private final Object lock = new Object();
    public static final int tcpPort = 54545;
    public static final int udpPort = 54544;
    public static boolean isHosting = false;

    public enum State { None, Lobby, GameOptions, Minigame }
    public static State state = State.None;

    public static void registerClasses(Kryo kryo) {
        kryo.register(GameNextMessage.class);
        kryo.register(GameFinishedMessage.class);
        kryo.register(GameProgressMessage.class);
        kryo.register(GameOptionsMessage.class);
        kryo.register(ClientProgressMessage.class);
        kryo.register(ClientFinishedMessage.class);
        kryo.register(ClientReadyMessage.class);
        kryo.register(ClientOptionsMessage.class);
        kryo.register(ExitMessage.class);
        kryo.register(Player[].class);
        kryo.register(Player.class);
    }

    public static class ClientFinishedMessage {
        public int gameID;
        public int clientID;
    }

    public static class ClientProgressMessage {
        public int gameID;
        public int clientID;
        public float progress;
    }

    public static class ClientOptionsMessage {
        public int clientID;
        public int icon;
    }

    public static class ClientReadyMessage {
        public int clientID;
        public boolean isReady;
    }

    public static class GameFinishedMessage {
        public int gameID;
        public int clientWinnerID;
    }

    public static class GameProgressMessage {
        public int gameID;
        public Player[] players;
    }

    public static class GameNextMessage {
        public int gameID;
        public int currRound;
    }

    public static class GameOptionsMessage {
        public int maxMiniGameRounds;
        public Player[] players;
    }

    public static class ExitMessage {
        public int overallclientWinnerID;
    }
}
