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

import java.util.Timer;
import java.util.TimerTask;

import tuc.werkstatt.doubleup.network.ClientFinishedMessage;
import tuc.werkstatt.doubleup.network.ClientProgressMessage;

public abstract class MiniGame implements Screen {
    public final DoubleUp game;

    public static Color materialRed = Color.valueOf("f44336ff");
    public static Color materialGreen = Color.valueOf("4caf50ff");
    public static Color materialBlue = Color.valueOf("2196f3ff");
    public static Color materialLime = Color.valueOf("cddc39ff");
    public static Color materialOrange = Color.valueOf("ff9800ff");
    public static Color materialPurple = Color.valueOf("9c27b0ff");
    public static Color materialBrown = Color.valueOf("795548ff");
    public static Color materialTeal = Color.valueOf("009688ff");
    public static Color materialBackground = Color.valueOf("212121ff");
    public static Color materialText = Color.valueOf("ffffffff");

    private static boolean isUiInitialized = false;
    private static ShapeRenderer uiShapeRenderer;
    private static Color uiPlayerColor;
    private static Sprite bottomPanelSprite;
    private static Sprite topPanelSprite;
    private static Sprite roundIndicatorSprite;
    private static Sprite currRoundIndicatorSprite;
    private static Sprite flagSprite;

    private static final float indicatorSpacing = 12f;
    private static float indicatorStartPosX;

    private Vector3 projectedTouchPos = new Vector3();
    private Vector2 unprojectedTouchPos = new Vector2();

    private Timer sendProgressTimer;
    private TimerTask sendProgressTimerTask;
    private ClientProgressMessage progressMsg;
    public int currRound;
    public int maxRounds;

    Player[] players;

    public MiniGame(DoubleUp game) {
        this.game = game;
        game.currMiniGame = this;
        sendProgressTimer = new Timer();
        progressMsg = new ClientProgressMessage();
        scheduleProgressTask();
    }

    public void init(int currRound, int maxRounds) {
        this.currRound = currRound;
        this.maxRounds = maxRounds;
        initUserInterface();
    }

    private void initUserInterface() {
        if (isUiInitialized) { return; } else { isUiInitialized = true; }

        uiShapeRenderer = new ShapeRenderer();
        uiShapeRenderer.setProjectionMatrix(game.uiCamera.combined);
        game.uiBatch.setProjectionMatrix(game.uiCamera.combined);

        uiPlayerColor = new Color();
        topPanelSprite = getSprite("ui/top_panel");
        topPanelSprite.setPosition(0, game.targetResHeight - game.targetTopBarHeight);
        bottomPanelSprite = getSprite("ui/bottom_panel");
        bottomPanelSprite.setPosition(0, 0);
        roundIndicatorSprite = getSprite("ui/bottom_circle");
        final float originalIndicatorSize = roundIndicatorSprite.getWidth();
        final float maxPossibleIndicatorSize = Math.max(10, (game.targetResWidth - (maxRounds + 1) * indicatorSpacing) / maxRounds);
        final float scaledIndicatorSize = Math.min(originalIndicatorSize, maxPossibleIndicatorSize);
        final boolean evenNumRounds = maxRounds % 2 == 0;
        indicatorStartPosX = evenNumRounds ?
                game.targetResWidth / 2 - ((maxRounds / 2) * (scaledIndicatorSize + indicatorSpacing)) + indicatorSpacing / 2:
                (game.targetResWidth - scaledIndicatorSize) / 2 - ((maxRounds / 2) * (scaledIndicatorSize + indicatorSpacing));
        currRoundIndicatorSprite = getSprite("ui/bottom_filled");
        roundIndicatorSprite.setSize(scaledIndicatorSize, scaledIndicatorSize);
        currRoundIndicatorSprite.setSize(scaledIndicatorSize, scaledIndicatorSize);
        flagSprite = getSprite("ui/flag");
        flagSprite.setPosition(1140f - flagSprite.getWidth() / 2, game.targetResHeight - flagSprite.getHeight() - 3f);
    }

    private void scheduleProgressTask() {
        cancelProgressTask();
        sendProgressTimerTask = new TimerTask() {
            @Override
            public void run() {
                sendProgressToServer();
            }
        };
        final long sendProgressDelay = 500;
        sendProgressTimer.scheduleAtFixedRate(sendProgressTimerTask, sendProgressDelay, sendProgressDelay);
    }

    private void cancelProgressTask() {
        if (sendProgressTimerTask != null) {
            sendProgressTimerTask.cancel();
        }
    }

    private void sendProgressToServer() {
        progressMsg.gameID = game.minigames.indexOf(this.getClass().getSimpleName(), false);
        progressMsg.clientID = game.client.getID();
        progressMsg.progress = getProgress();
        game.client.sendUDP(progressMsg);
    }

    // implementation necessary for tracking game state and updating other players/server
    public abstract float getProgress();
    public abstract boolean isFinished();
    public abstract void draw(float deltaTime);
    public abstract void update(float deltaTime);

    @Override
    public final void render(float deltaTime) {
        Gdx.gl.glClearColor(materialBackground.r, materialBackground.g, materialBackground.b, 1);
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.toggleMusicMute();
        }

        if (isFinished()) {
            System.out.println("Client: ClientFinishedMessage sent");
            ClientFinishedMessage msg = new ClientFinishedMessage();
            msg.gameID = game.minigames.indexOf(this.getClass().getSimpleName(), false);
            msg.clientID = game.client.getID();
            game.client.sendTCP(msg);
            game.setScreen(null);
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
        if (players != null) {
            for (Player p : players) {
                Color.rgba8888ToColor(uiPlayerColor, p.color8888);
                uiShapeRenderer.setColor(uiPlayerColor);
                final float progress = p.ID == game.client.getID() ? getProgress() : p.miniGameProgress;
                final int currPlayerProgressInWidth = (int) (progressBarWidth / 100f * progress);
                uiShapeRenderer.rect(progressBarX, progressBarY, currPlayerProgressInWidth, progressBarHeight);
            }
        }
        uiShapeRenderer.end();

        game.uiBatch.begin();
        topPanelSprite.draw(game.uiBatch);
        flagSprite.draw(game.uiBatch);
        bottomPanelSprite.draw(game.uiBatch);
        for (int i = 1; i <= maxRounds; ++i) {
            final float currPosX = indicatorStartPosX + (i - 1) * (roundIndicatorSprite.getWidth() + indicatorSpacing);
            if (i == currRound) {
                currRoundIndicatorSprite.setPosition(currPosX, (game.targetBottomBarHeight - currRoundIndicatorSprite.getWidth()) / 2);
                currRoundIndicatorSprite.draw(game.uiBatch);
            } else {
                roundIndicatorSprite.setPosition(currPosX, (game.targetBottomBarHeight - roundIndicatorSprite.getWidth()) / 2);
                roundIndicatorSprite.draw(game.uiBatch);
            }
        }
        game.uiBatch.end();
    }

    public final Vector2 getTouchPos() {
        return unprojectedTouchPos;
    }

    public final Sprite getSprite(String name) {
        return game.atlas.createSprite(name);
    }

    public final Sound getSound(String name) {
        if(!game.assets.isLoaded(name)) {
            game.assets.load(name, Sound.class);
            game.assets.finishLoadingAsset(name);
        }
        return game.assets.get(name);
    }

    @Override
    public void resize(int width, int height) {
        game.resizeViews();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    public final void exit() {
        cancelProgressTask();
        sendProgressTimer.cancel();
        dispose();
        game.setScreen(null);
    }
}
