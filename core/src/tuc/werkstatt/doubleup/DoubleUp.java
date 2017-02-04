package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.TimeUtils;
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
    public BitmapFont scoreFont;
    private Music music;
    private String currMusicFileName = "";
    private boolean isMusicMuted = false;

    public OrthographicCamera camera;
    public OrthographicCamera uiCamera;
    public Viewport gameView;
    public Viewport uiView;
    public SpriteBatch batch;
    public SpriteBatch uiBatch;

    private enum TransitionState { Active, Inactive }
    private TransitionState transitionState = TransitionState.Inactive;
    public long screenTransitionTimestamp;
    private FrameBuffer transitionBuffer;
    private TextureRegion transitionTextureRegion;

    // add your individual minigame name (needs to match java file) here
    // index also being used as gameID in messages
    final String[] minigames = { "ShootingGallery", "CrazySmiley", "PickColor", "PumpBalloon",
            "FindTheMatch", "Drop", "SpaceShooter"};

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
             generateFonts();
        } else {
            Gdx.app.setLogLevel(Application.LOG_NONE);
        }
        loadBitmapFonts();
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

        transitionBuffer = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

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

    private void generateFont(String fontName, int fontSize, Color fontColor, float borderWidth,
                                    Color borderColor, int shadowOffset, Color shadowColor)
    {
        final int pixelSize = 2048;
        final int padding = 4;
        BitmapFontWriter.FontInfo info = new BitmapFontWriter.FontInfo();
        info.padding = new BitmapFontWriter.Padding(padding, padding, padding, padding);
        info.size = fontSize;

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = fontSize;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.color = fontColor;
        parameter.borderColor = borderColor;
        parameter.borderWidth = borderWidth;
        parameter.shadowColor = shadowColor;
        parameter.shadowOffsetX = shadowOffset;
        parameter.shadowOffsetY = shadowOffset;
        parameter.packer = new PixmapPacker(pixelSize, pixelSize, Pixmap.Format.RGBA8888, padding,
                false, new PixmapPacker.SkylineStrategy());

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.absolute("fonts/" + fontName));
        generator.scaleForPixelHeight(fontSize);
        FreeTypeFontGenerator.FreeTypeBitmapFontData data = generator.generateData(parameter);

        BitmapFontWriter.writeFont(data, new String[] {fontName + "_" + fontSize + ".png"},
                Gdx.files.absolute("fonts/" + fontName + "_" + fontSize + ".fnt"), info, pixelSize, pixelSize);
        BitmapFontWriter.writePixmaps(parameter.packer.getPages(), Gdx.files.absolute("fonts"), fontName + "_" + fontSize);

        generator.dispose();
    }

    private void generateFont(String fontName, int fontSize) {
        generateFont(fontName, fontSize, Color.WHITE);
    }

    private void generateFont(String fontName, int fontSize, Color fontColor) {
        generateFont(fontName, fontSize, fontColor, 0f, Color.BLACK);
    }

    private void generateFont(String fontName, int fontSize, Color fontColor, float borderSize, Color borderColor) {
        generateFont(fontName, fontSize, fontColor, borderSize, borderColor, 0, Color.BLACK);
    }

    private void generateFonts() {
        Gdx.app.log("Assets", "Generating bitmap fonts ...");
        if (!Gdx.files.internal("fonts/CarterOne.ttf_60.fnt").exists()) {
            generateFont("CarterOne.ttf", 60, Color.WHITE, 2f, Color.BLACK, 4, Color.BLACK);
        }
        if (!Gdx.files.internal("fonts/CarterOne.ttf_82.fnt").exists()) {
            generateFont("CarterOne.ttf", 82, Color.WHITE, 6f, Color.valueOf("1c6b65ff"), 5, Color.valueOf("1c6b65ff"));
        }
        if (!Gdx.files.internal("fonts/CarterOne.ttf_75.fnt").exists()) {
            generateFont("CarterOne.ttf", 75, Color.valueOf("fcf2d2ff"), 1f, Color.valueOf("fcf2d2ff"));
        }
    }

    private void loadBitmapFonts() {
        Gdx.app.log("Assets", "Loading bitmap fonts ...");
        font = new BitmapFont(Gdx.files.internal("fonts/CarterOne.ttf_60.fnt"));
        font.getData().markupEnabled = true;
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        titleFont = new BitmapFont(Gdx.files.internal("fonts/CarterOne.ttf_82.fnt"));
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        scoreFont = new BitmapFont(Gdx.files.internal("fonts/CarterOne.ttf_75.fnt"));
        scoreFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        //fixFonts(scoreFont);
    }

    // workaround for buggy (bleeding artifacts) linear filtering in libgdx
    private void fixFonts(BitmapFont font) {
        for (BitmapFont.Glyph[] page : font.getData().glyphs) {
            if (page == null) {
                continue;
            }
            for (BitmapFont.Glyph glyph : page) {
                if (glyph == null) {
                    continue;
                }
                glyph.u2 -= 0.001f;
                glyph.v2 -= 0.001f;
            }
        }
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
            music.setVolume(0.5f);
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

    public void renderToTransitionBuffer(Screen screen) {
        transitionState = DoubleUp.TransitionState.Active;
        transitionBuffer.bind();
        if (screen instanceof Start) {
            ((Start)screen).draw(0f);
        } else if (screen instanceof Lobby) {
            ((Lobby)screen).draw(0f);
        } else if (screen instanceof GameOptions) {
            ((GameOptions)screen).draw(0f);
        } else if (screen instanceof MiniGame) {
            MiniGame m = (MiniGame)screen;
            m.drawUserInterface();
            m.scoreOverlay();
        } else {
            ((Results)screen).draw(0f);
        }
        transitionBuffer.unbind();
        transitionTextureRegion = new TextureRegion(transitionBuffer.getColorBufferTexture());
        transitionTextureRegion.flip(false, true);
    }

    public void drawTransitionBuffer() {
        if (transitionState == TransitionState.Active) {
            final long screenTransitionDuration = 420;
            final float factor = Math.min(1f, TimeUtils.timeSinceMillis(screenTransitionTimestamp) / (float) screenTransitionDuration);
            final float transX = 0 - targetResWidth * factor;
            uiBatch.begin();
            //uiBatch.setColor(1f, 1f, 1f, 1f - factor);
            uiBatch.draw(transitionTextureRegion, transX, 0, targetResWidth, targetResHeight);
            uiBatch.end();
            if (TimeUtils.timeSinceMillis(screenTransitionTimestamp) >= screenTransitionDuration) {
                transitionState = TransitionState.Inactive;
            }
        }
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
        scoreFont.dispose();
        transitionBuffer.dispose();
        batch.dispose();
        uiBatch.dispose();
        assets.dispose();
    }
}