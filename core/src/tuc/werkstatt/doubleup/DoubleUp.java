package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

public class DoubleUp extends Game {
    // 16:10 aspect ratio, native nexus 7 (course device) resolution
    public final int targetResWidth = 1200;
    public final int targetResHeight = 1920;
    public final int targetTopBarHeight = 138;
    public final int targetBottomBarHeight = 138;
    public final int width = targetResWidth;
    public final int height = targetResHeight - targetTopBarHeight - targetBottomBarHeight;

    private final String atlasFileName = "textures";
    public TextureAtlas atlas;
    public AssetManager assets;
    public BitmapFont font;
    private Music music;
    private String currMusicFileName;
    private boolean isMusicMuted = false;

    public OrthographicCamera camera;
    public OrthographicCamera uiCamera;
    public Viewport gameView;
    public Viewport uiView;
    public SpriteBatch batch;
    public SpriteBatch uiBatch;
    Server server;
    Client client;

    // add your individual minigame name (needs to match java file) here
    // index also being used as gameID in messages
    Array<String> minigames = new Array<String>(new String[]{
            "ClickTarget", "CrazySmiley","PickColor", "PumpBalloon", "FindTheMatch","Drop","PlaneWarGame"  });

    MiniGame currMiniGame = null;
    private String testingMiniGame = null;
    private String[] args;

    public DoubleUp(String[] args) {
        this.args = args;
    }

    @Override
    public void create() {
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            generateTextureAtlas();
        }
        generateFonts();
        loadAssets();
        atlas = assets.get("images/" + atlasFileName + ".atlas", TextureAtlas.class);
        batch = new SpriteBatch();
        uiBatch = new SpriteBatch();
        // user interface, e.g. start screen, top and bottom bar in minigame screen
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, targetResWidth, targetResHeight);
        uiView = new StretchViewport(targetResWidth, targetResHeight, uiCamera);

        // minigame camera and view
        camera = new OrthographicCamera();
        camera.setToOrtho(false, width, height);
        gameView = new StretchViewport(width, height, camera);
        resizeViews();

        // minigame provided by program argument will be quick started and looped for testing purposes
        if (args != null && args.length > 0 &&
                (minigames.contains(args[0], false) || args[0].equals("TestingPlayground"))) {
            testingMiniGame = args[0];
            setScreen(new Lobby(this, true));
        } else {
            setScreen(new Start(this));
        }
    }

    private void generateTextureAtlas() {
        final long imageDirModTime = Gdx.files.internal("images").lastModified();
        final long atlasCreationTime = Gdx.files.internal("images/" + atlasFileName + ".atlas").lastModified();
        final long modTimeThreshold = 30 * 1000; // 30 seconds
        // regenerate texture atlas if dir or subdirs have changed
        if (!Gdx.files.internal("images/" + atlasFileName + ".atlas").exists() ||
                imageDirModTime > atlasCreationTime + modTimeThreshold) {
            System.out.println("Image changes detected, generating new texture atlas ...");
            TexturePacker.Settings settings = new TexturePacker.Settings();
            settings.maxWidth = 2048;
            settings.maxHeight = 2048;
            settings.rotation = true;
            settings.filterMag = Texture.TextureFilter.Linear;
            settings.filterMin = Texture.TextureFilter.Linear;
            TexturePacker.process(settings, "images/", "images/", atlasFileName);
        } else {
            System.out.println("No image changes detected, keep current texture atlas.");
        }
    }

    private void generateFonts() {
        System.out.println("Generating bitmap fonts ...");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/FiraSans-Medium.otf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 48;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.Linear;
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    private void loadAssets() {
        assets = new AssetManager();
        System.out.println("Loading assets ...");
        assets.load("images/" + atlasFileName + ".atlas", TextureAtlas.class);
        assets.finishLoading();
    }

    public void loadMusic(String name) {
        if(!assets.isLoaded(name)) {
            assets.load(name, Music.class);
            assets.finishLoadingAsset(name);
            if (music != null) {
                music.stop();
                assets.unload(currMusicFileName);
            }
            music = assets.get(name);
            music.setLooping(true);
            currMusicFileName = name;
            if (!isMusicMuted) {
                music.play();
            }
        }
    }

    public void toggleMusicMute() {
        if (music == null) { return; }
        if (isMusicMuted) {
            music.play();
            isMusicMuted = false;
        } else {
            music.pause();
            isMusicMuted = true;
        }
    }

    public void resizeViews() {
        uiView.setScreenBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        final int screenBottomBarHeight = (int)((float)Gdx.graphics.getHeight()
                / targetResHeight * targetBottomBarHeight);
        final int screenTopBarHeight = (int)((float)Gdx.graphics.getHeight()
                / targetResHeight * targetTopBarHeight);
        gameView.setScreenBounds(0, screenBottomBarHeight, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight() - screenBottomBarHeight - screenTopBarHeight);
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
                    .getConstructor(DoubleUp.class).newInstance(this));
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
        if (music != null) { music.stop(); }
        font.dispose();
        batch.dispose();
        uiBatch.dispose();
        assets.dispose();
    }
}