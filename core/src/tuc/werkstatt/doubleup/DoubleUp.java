package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;

public class DoubleUp extends Game {
    // 16:10 aspect ratio, native nexus 7 (course device) resolution
    public final int targetResWidth = 1200;
    public final int targetResHeight = 1920;
    public final int targetTopBarHeight = 138;
    public final int targetBottomBarHeight = 138;
    public final int width = targetResWidth;
    public final int height = targetResHeight - targetTopBarHeight - targetBottomBarHeight;

    public Client client;
    public Server server;

    private final String atlasFileName = "textures";
    public TextureAtlas atlas;
    public AssetManager assets;
    public BitmapFont font;
    public BitmapFont titleFont;
    private Music music;
    private String currMusicFileName = "";
    private boolean isMusicMuted = false;

    public OrthographicCamera camera;
    public OrthographicCamera uiCamera;
    public Viewport gameView;
    public Viewport uiView;
    public SpriteBatch batch;
    public SpriteBatch uiBatch;

    // add your individual minigame name (needs to match java file) here
    // index also being used as gameID in messages
    final String[] minigames = { "ClickTarget", "CrazySmiley", "PickColor", "PumpBalloon",
            "FindTheMatch", "Drop", "PlaneWarGame"};

    MiniGame currMiniGame = null;
    private String testingMiniGame = null;
    private String[] args;

    public DoubleUp(String[] args) {
        this.args = args;
    }

    @Override
    public void create() {
         if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
             Gdx.app.setLogLevel(Application.LOG_DEBUG);
             generateTextureAtlas();
        } else {
            Gdx.app.setLogLevel(Application.LOG_NONE);
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

        Gdx.input.setCatchBackKey(true);
        // minigame provided by program argument will be quick started and looped for testing purposes
        if (args != null && args.length > 0 &&
                (Arrays.asList(minigames).contains(args[0]) || args[0].equals("TestingPlayground"))) {
            testingMiniGame = args[0];
            Network.isHosting = true;
            isMusicMuted = true;
            setScreen(new Lobby(this));
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
            Gdx.app.log("Assets", "Image changes detected, generating new texture atlas ...");
            TexturePacker.Settings settings = new TexturePacker.Settings();
            settings.maxWidth = 2048;
            settings.maxHeight = 2048;
            settings.rotation = true;
            settings.filterMag = Texture.TextureFilter.Linear;
            settings.filterMin = Texture.TextureFilter.Linear;
            TexturePacker.process(settings, "images/", "images/", atlasFileName);
        } else {
            Gdx.app.log("Assets", "No image changes detected, keep current texture atlas.");
        }
    }

    private BitmapFont generateFont(String fontName, int fontSize, Color borderColor, float borderWidth, int shadowOffset) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/" + fontName));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = fontSize;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.borderColor = borderColor;
        parameter.borderWidth = borderWidth;
        parameter.shadowColor = borderColor;
        parameter.shadowOffsetX = shadowOffset;
        parameter.shadowOffsetY = shadowOffset;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    private void generateFonts() {
        Gdx.app.log("Assets", "Generating bitmap fonts ...");
        font = generateFont("CarterOne.ttf", 60, Color.BLACK, 2f, 4);
        font.getData().markupEnabled = true;
        titleFont = generateFont("CarterOne.ttf", 82, Color.valueOf("1c6b65ff"), 6f, 5);
    }

    private void loadAssets() {
        assets = new AssetManager();
        Gdx.app.log("Assets", "Loading assets ...");
        assets.load("images/" + atlasFileName + ".atlas", TextureAtlas.class);
        assets.finishLoading();
    }

    public void loadMusic(String name) {
        if (name.equals(currMusicFileName)) {
            return;
        }
        if (music != null) {
            music.stop();
            if (assets.isLoaded(currMusicFileName)) {
                assets.unload(currMusicFileName);
            }
        }
        assets.load(name, Music.class);
        assets.finishLoadingAsset(name);
        music = assets.get(name);
        music.setLooping(true);
        currMusicFileName = name;
        if (!isMusicMuted) {
            music.setVolume(0.65f);
            music.play();
        }
    }

    public void stopMusic() {
        if (music != null && music.isPlaying()) {
            music.stop();
            if (assets.isLoaded(currMusicFileName)) {
                assets.unload(currMusicFileName);
                currMusicFileName = "";
            }
        }
    }

    public Sprite getSprite(String name) {
        return atlas.createSprite(name);
    }

    public Sound getSound(String name) {
        if(!assets.isLoaded(name)) {
            assets.load(name, Sound.class);
            assets.finishLoadingAsset(name);
        }
        return assets.get(name);
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

    public void loadMiniGame(int id) {
        try {
            setScreen((MiniGame)Class.forName("tuc.werkstatt.doubleup.minigames." + minigames[id])
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
        if (client != null) { client.dispose(); }
        if (server != null) { server.dispose(); }
        if (music != null) { music.stop(); }
        font.dispose();
        titleFont.dispose();
        batch.dispose();
        uiBatch.dispose();
        assets.dispose();
    }
}