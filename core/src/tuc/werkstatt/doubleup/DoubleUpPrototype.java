package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

public class DoubleUpPrototype extends Game {
    // 16:10 aspect ratio, native nexus 7 (course device) resolution
	public final int width = 1200;
	public final int height = 1920;

	public SpriteBatch batch;
	public BitmapFont font;
	public OrthographicCamera camera;
    Server server;
    Client client;

    // add your individual minigame name (needs to match java file) here
    // index also being used as gameID in messages
    Array<String> minigames = new Array<String>(new String[]{
            "PickColor", "PumpBalloon" });

    MiniGame currMiniGame = null;
    private String testingMiniGame = null;
    private String[] args;

    public DoubleUpPrototype(String[] args) {
        this.args = args;
    }

	@Override
	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, width, height);

        // minigame provided by program argument will be quick started and looped for testing purposes
        if (args != null && args.length > 0 &&
                (minigames.contains(args[0], false) || args[0].equals("TestingPlayground"))) {
            testingMiniGame = args[0];
            setScreen(new Lobby(this, true));
        } else {
            setScreen(new Start(this));
        }
	}

    public boolean isTestingEnvironment() {
        return testingMiniGame != null && !testingMiniGame.isEmpty();
    }

    public String getTestingMiniGame() {
        return testingMiniGame;
    }

    void loadMiniGame(String screenName) {
        try {
            setScreen((MiniGame)Class.forName("tuc.werkstatt.doubleup.minigames." + screenName)
                    .getConstructor(DoubleUpPrototype.class).newInstance(this));
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
            Gdx.app.exit();
        }
    }

	@Override
	public void render() {
		super.render();
	}

    @Override
	public void dispose() {
        if (client != null) { client.stop(); }
        if (server != null) { server.stop(); }
		font.dispose();
		batch.dispose();
	}
}
