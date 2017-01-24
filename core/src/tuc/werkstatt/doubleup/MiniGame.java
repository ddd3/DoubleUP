package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Arrays;

public abstract class MiniGame implements Screen {
    public final DoubleUp game;

    private static boolean isUiInitialized = false;
    private static ShapeRenderer uiShapeRenderer;
    private static Sprite bottomPanelSprite;
    private static Sprite topPanelSprite;
    private static Sprite roundIndicatorSprite;
    private static Sprite currRoundIndicatorSprite;
    private static Sprite flagSprite;
    private static Sprite[] animalSprites;

    private static final float indicatorSpacing = 12f;
    private static float indicatorStartPosX;

    private Vector3 projectedTouchPos = new Vector3();
    private Vector2 unprojectedTouchPos = new Vector2();

    private long lastProgressTime;
    private Player[] cachePlayers;

    public MiniGame(DoubleUp game) {
        this.game = game;
        initUserInterface();
        Network.state = Network.State.Minigame;
        game.client.setCurrMinigame(this);
        lastProgressTime = TimeUtils.millis();
        cachePlayers = game.client.getPlayers();
    }

    private void initUserInterface() {
        if (isUiInitialized) { return; } else { isUiInitialized = true; }

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
        drawUserInterface();

        //game.camera.update();
        if (Gdx.input.isTouched()) {
            projectedTouchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(projectedTouchPos);
            unprojectedTouchPos.set(projectedTouchPos.x, projectedTouchPos.y);
        }
        game.gameView.apply();
        draw(deltaTime);
        update(deltaTime);

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) { game.toggleMusicMute(); }
        if (Network.isHosting) { game.server.update(); }
        if (isFinished()) { game.client.sendClientFinishedMessage(); }
        if (TimeUtils.timeSinceMillis(lastProgressTime) > 500) {
            game.client.sendClientProgressMessage(getProgress());
            lastProgressTime = TimeUtils.millis();
            cachePlayers = game.client.getPlayers();
        }
    }

    private void drawUserInterface() {
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
