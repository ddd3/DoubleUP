package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Arrays;

public abstract class MiniGame implements Screen {
    public final DoubleUp game;

    private static boolean isInitialized = false;
    private static ShapeRenderer uiShapeRenderer;
    private static Sprite bottomPanelSprite, topPanelSprite, roundIndicatorSprite,
            currRoundIndicatorSprite, flagSprite, iconBackgroundSprite, introPanelSprite, introGermSprite,
            count1Sprite, count2Sprite, count3Sprite, countGoSprite, holdSprite, activeCountSprite;
    private static Sprite[] animalSprites;
    private Sprite backgroundSprite, iconSprite;

    private static Sound count1Sound, count2Sound, count3Sound, countGoSound, holdSound;

    private static final float indicatorSpacing = 12f;
    private static float indicatorStartPosX;

    private Vector3 projectedTouchPos = new Vector3();
    private Vector2 unprojectedTouchPos = new Vector2();

    private long lastProgressTime;
    private Player[] cachePlayers;

    private enum State { Intro, Count, Game, Hold, Score }
    private State state = State.Intro;
    private long introTimeStamp =  TimeUtils.millis();
    private long countTimeStamp;
    private int countdown = 3;
    private GlyphLayout title;
    private GlyphLayout description;
    private final float descriptionPadding = 32f;
    private boolean isIntroInit = false;

    public MiniGame(DoubleUp game) {
        this.game = game;
        if (!isInitialized) {
            initUserInterface();
            initSounds();
            initKeyHandling();
            isInitialized = true;
        }
        Network.state = Network.State.Minigame;
        game.client.setCurrMinigame(this);
        lastProgressTime = TimeUtils.millis();
        cachePlayers = game.client.getPlayers();
    }

    public static void reinit() {
        isInitialized = false;
    }

    private void initUserInterface() {
        uiShapeRenderer = new ShapeRenderer();
        uiShapeRenderer.setProjectionMatrix(game.uiCamera.combined);
        game.uiBatch.setProjectionMatrix(game.uiCamera.combined);

        topPanelSprite = getSprite("ui/top_panel");
        topPanelSprite.setPosition(0, game.targetResHeight - game.targetTopBarHeight);
        bottomPanelSprite = getSprite("ui/bottom_panel");
        bottomPanelSprite.setPosition(0, 0);
        roundIndicatorSprite = getSprite("ui/bottom_circle");
        final int maxRounds = game.client.getMaxMiniGameRounds();
        final float originalIndicatorSize = roundIndicatorSprite.getWidth();
        final float maxPossibleIndicatorSize = Math.max(10, (game.targetResWidth - (maxRounds + 1) * indicatorSpacing) / maxRounds);
        final float scaledIndicatorSize = Math.min(originalIndicatorSize, maxPossibleIndicatorSize);
        indicatorStartPosX = game.targetResWidth / 2f - (maxRounds / 2f * (scaledIndicatorSize + indicatorSpacing)) + indicatorSpacing / 2f;
        currRoundIndicatorSprite = getSprite("ui/bottom_filled");
        roundIndicatorSprite.setSize(scaledIndicatorSize, scaledIndicatorSize);
        currRoundIndicatorSprite.setSize(scaledIndicatorSize, scaledIndicatorSize);
        flagSprite = getSprite("ui/flag");
        flagSprite.setPosition(1140f - flagSprite.getWidth() / 2, game.targetResHeight - flagSprite.getHeight() - 3f);

        animalSprites = Arrays.copyOf(GameOptions.animalSprites, GameOptions.animalSprites.length);
        for (Sprite sp : animalSprites) {
            sp.setColor(Color.WHITE);
            sp.setSize(96, 96);
            sp.setY(game.targetResHeight - 68f - sp.getHeight() / 2f);
        }

        // intro overlay
        introPanelSprite = getSprite("ui/doodle_select_panel");
        introPanelSprite.setPosition((game.targetResWidth - introPanelSprite.getWidth()) / 2f,
                (game.targetResHeight - introPanelSprite.getHeight()) / 2f + 100f);
        introGermSprite = getSprite("ui/green_germs");
        introGermSprite.setPosition(introPanelSprite.getX() + (introPanelSprite.getWidth() - introGermSprite.getWidth()) / 2f,
                introPanelSprite.getY() + introPanelSprite.getHeight() - introGermSprite.getHeight() / 2f);
        iconBackgroundSprite = getSprite("ui/doodle_box_selected_background");
        iconBackgroundSprite.setSize(iconBackgroundSprite.getWidth() * 2.5f, iconBackgroundSprite.getHeight() * 2.5f);
        iconBackgroundSprite.setPosition(introPanelSprite.getX() + (introPanelSprite.getWidth() - iconBackgroundSprite.getWidth()) / 2f,
                introPanelSprite.getY() + introPanelSprite.getHeight() - iconBackgroundSprite.getHeight() - 120f);

        // count overlay
        final float countPosFromTop = 640f;
        count1Sprite = getSprite("ui/count1");
        count1Sprite.setPosition(game.targetResWidth / 2f - count1Sprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - count1Sprite.getHeight() / 2f);
        count2Sprite = getSprite("ui/count2");
        count2Sprite.setPosition(game.targetResWidth / 2f - count2Sprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - count2Sprite.getHeight() / 2f);
        count3Sprite = getSprite("ui/count3");
        count3Sprite.setPosition(game.targetResWidth / 2f - count3Sprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - count3Sprite.getHeight() / 2f);
        countGoSprite = getSprite("ui/countGo");
        countGoSprite.setPosition(game.targetResWidth / 2f - countGoSprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - countGoSprite.getHeight() / 2f);
        holdSprite = getSprite("ui/hold");
        holdSprite.setPosition(game.targetResWidth / 2f - holdSprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - holdSprite.getHeight() / 2f);
    }

    private void initSounds() {
        count1Sound = getSound("sounds/1.ogg");
        count2Sound = getSound("sounds/2.ogg");
        count3Sound = getSound("sounds/3.ogg");
        countGoSound = getSound("sounds/go.ogg");
        holdSound = getSound("sounds/hold.ogg");
    }

    private void initKeyHandling() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyUp(final int keycode) {
                if (keycode == Input.Keys.BACK) {
                    Gdx.app.log("MiniGame", "Back button pressed, returning to StartScreen");
                    game.setScreen(new Start(game));
                }
                return false;
            }
        });
    }

    public void setTitle(String title) {
        this.title = new GlyphLayout(game.titleFont, title);
    }

    public void setDescription(String description) {
        final float targetWidth = introPanelSprite.getWidth() - descriptionPadding * 2f;
        this.description = new GlyphLayout(game.font, description, MaterialColors.text, targetWidth, Align.center, true);
    }

    public void setIcon(String name) {
        final float padding = 24f;
        final float iconSize = iconBackgroundSprite.getWidth() - padding * 2f;
        iconSprite = getSprite(name);
        final float scaleFactor = iconSize / Math.max(iconSprite.getWidth(), iconSprite.getHeight());
        iconSprite.setSize(iconSprite.getWidth() * scaleFactor, iconSprite.getHeight() * scaleFactor);
        iconSprite.setPosition(iconBackgroundSprite.getX() + (iconBackgroundSprite.getWidth() - iconSprite.getWidth()) / 2f,
                iconBackgroundSprite.getY() + (iconBackgroundSprite.getHeight() - iconSprite.getHeight()) / 2f);
    }

    public void setBackground(String name) {
        backgroundSprite = getSprite(name);
        backgroundSprite.setSize(game.width, game.height);
        backgroundSprite.setPosition(0, game.targetBottomBarHeight);
    }

    // implementation necessary for tracking game state and updating other players/server
    public abstract float getProgress();
    public abstract boolean isFinished();
    public abstract void draw(float deltaTime);
    public abstract void update(float deltaTime);

    @Override
    public final void render(float deltaTime) {
        Gdx.gl.glClearColor(MaterialColors.background.r, MaterialColors.background.g, MaterialColors.background.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.uiView.apply();
        if (state == State.Intro) {
            drawUserInterface();
            drawIntroInterface();
            if (TimeUtils.timeSinceMillis(introTimeStamp) > 4000) {
                state = State.Count;
            }
        } else if (state == State.Count) {
            drawUserInterface();
            updateAndDrawCountOverlay();
        } else if (state == State.Game) {
            drawUserInterface();
            updateTouchPosition();
            updateSubMiniGame(deltaTime);
            updateNetwork();
        } else if (state == State.Hold) {

        } else if (state == State.Score) {

        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) { game.toggleMusicMute(); }
    }

    private void drawUserInterface() {
        if (backgroundSprite != null) {
            game.uiBatch.begin();
            backgroundSprite.draw(game.uiBatch);
            game.uiBatch.end();
        } else {
            setBackground("ui/title_background");
        }

        final int topPanelY = game.targetResHeight - game.targetTopBarHeight;
        // values measured in image editor
        final int progressBarX = 20;
        final int progressBarY = topPanelY + 52;
        final int progressBarWidth = 1162;
        final int progressBarHeight = 36;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(Color.WHITE);
        uiShapeRenderer.rect(progressBarX, progressBarY, progressBarWidth, progressBarHeight);
        uiShapeRenderer.setColor(MaterialColors.uiGreen);
        final int currPlayerProgressInWidth = (int) (progressBarWidth / 100f * getProgress());
        uiShapeRenderer.rect(progressBarX, progressBarY, currPlayerProgressInWidth, progressBarHeight);
        uiShapeRenderer.end();

        game.uiBatch.begin();
        topPanelSprite.draw(game.uiBatch);
        flagSprite.draw(game.uiBatch);
        Sprite currPlayerSprite = null;
        for (Player p : cachePlayers) {
            if (p.ID == game.client.getID()) {
                currPlayerSprite = animalSprites[p.icon];
                continue;
            }
            Sprite sp = animalSprites[p.icon];
            sp.setX(progressBarX - sp.getWidth() / 2f + progressBarWidth / 100f * p.miniGameProgress);
            sp.draw(game.uiBatch);
        }
        currPlayerSprite.setX(progressBarX - currPlayerSprite.getWidth() / 2f + progressBarWidth / 100f * getProgress());
        currPlayerSprite.draw(game.uiBatch);

        bottomPanelSprite.draw(game.uiBatch);
        for (int i = 1; i <= game.client.getMaxMiniGameRounds(); ++i) {
            final float currPosX = indicatorStartPosX + (i - 1) * (roundIndicatorSprite.getWidth() + indicatorSpacing);
            if (i == game.client.getCurrMiniGameRound()) {
                currRoundIndicatorSprite.setPosition(currPosX, (game.targetBottomBarHeight - currRoundIndicatorSprite.getWidth()) / 2);
                currRoundIndicatorSprite.draw(game.uiBatch);
            } else {
                roundIndicatorSprite.setPosition(currPosX, (game.targetBottomBarHeight - roundIndicatorSprite.getWidth()) / 2);
                roundIndicatorSprite.draw(game.uiBatch);
            }
        }
        game.uiBatch.end();
    }

    private void drawIntroInterface() {
        if (!isIntroInit) {
            if (title == null) {
                setTitle("Missing title");
            }
            if (description == null) {
                setDescription("Missing description");
            }
            if (iconSprite == null) {
                setIcon("minigames/PumpBalloon/balloon");
            }
            isIntroInit = true;
        }
        game.uiBatch.begin();
        introPanelSprite.draw(game.uiBatch);
        introGermSprite.draw(game.uiBatch);
        iconBackgroundSprite.draw(game.uiBatch);
        iconSprite.draw(game.uiBatch);
        game.titleFont.draw(game.uiBatch, title, introPanelSprite.getX() + (introPanelSprite.getWidth() - title.width) / 2f,
                introPanelSprite.getY() + introPanelSprite.getHeight() + title.height / 2f);
        final float maxDescHeight = iconBackgroundSprite.getY() - introPanelSprite.getY() - descriptionPadding * 2f;
        game.font.draw(game.uiBatch, description, introPanelSprite.getX() + (introPanelSprite.getWidth() - description.width) / 2f,
                introPanelSprite.getY() + 2f * descriptionPadding + (maxDescHeight + description.height) / 2f);
        game.uiBatch.end();
    }

    private void updateAndDrawCountOverlay() {
        final long stepMillis = 800;
        final float soundVol = 0.8f;
        if (countdown == 3) {
            countTimeStamp = TimeUtils.millis();
            activeCountSprite = count3Sprite;
            count3Sound.play(soundVol);
            --countdown;
        } else if (countdown == 2 && TimeUtils.timeSinceMillis(countTimeStamp) > stepMillis) {
            countTimeStamp = TimeUtils.millis();
            activeCountSprite = count2Sprite;
            count2Sound.play(soundVol);
            --countdown;
        } else if (countdown == 1 && TimeUtils.timeSinceMillis(countTimeStamp) > stepMillis) {
            countTimeStamp = TimeUtils.millis();
            activeCountSprite = count1Sprite;
            count1Sound.play(soundVol);
            --countdown;
        } else if (countdown == 0 && TimeUtils.timeSinceMillis(countTimeStamp) > stepMillis) {
            countTimeStamp = TimeUtils.millis();
            activeCountSprite = countGoSprite;
            countGoSound.play(soundVol);
            --countdown;
        } else if (countdown < 0 && TimeUtils.timeSinceMillis(countTimeStamp) > stepMillis) {
            state = State.Game;
        }
        game.uiBatch.begin();
        final float alphaVal = Math.max(0, 1f - TimeUtils.timeSinceMillis(countTimeStamp) / (float)stepMillis);
        activeCountSprite.setAlpha(alphaVal);
        activeCountSprite.draw(game.uiBatch);
        game.uiBatch.end();
    }

    private void updateTouchPosition() {
        if (Gdx.input.isTouched()) {
            projectedTouchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(projectedTouchPos);
            unprojectedTouchPos.set(projectedTouchPos.x, projectedTouchPos.y);
        }
    }

    private void updateSubMiniGame(final float deltaTime) {
        game.gameView.apply();
        draw(deltaTime);
        update(deltaTime);
    }

    private void updateNetwork() {
        if (Network.isHosting) { game.server.update(); }
        if (isFinished()) { game.client.sendClientFinishedMessage(); }
        if (TimeUtils.timeSinceMillis(lastProgressTime) > 500) {
            game.client.sendClientProgressMessage(getProgress());
            lastProgressTime = TimeUtils.millis();
            cachePlayers = game.client.getPlayers();
        }
    }

    public final Vector2 getTouchPos() { return unprojectedTouchPos; }
    public final Sprite getSprite(String name) { return game.getSprite(name); }
    public final Sound getSound(String name) { return game.getSound(name); }

    @Override
    public void resize(int width, int height) {
        game.resizeViews();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    public final void exit() {
        dispose();
        game.setScreen(null);
    }
}
