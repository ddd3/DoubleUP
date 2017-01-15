package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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

    private ShapeRenderer uiRenderer;
    private Color uiBarColor;
    private Color playerColor;

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
        this.uiRenderer = new ShapeRenderer();
        this.uiBarColor = new Color(0.85f, 0.85f, 0.85f, 1f);
        this.playerColor = new Color();
        game.currMiniGame = this;
        sendProgressTimer = new Timer();
        progressMsg = new ClientProgressMessage();
        scheduleProgressTask();
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
        game.uiView.apply();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawUserInterface();

        game.camera.update();
        if (Gdx.input.isTouched()) {
            projectedTouchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(projectedTouchPos);
            unprojectedTouchPos.set(projectedTouchPos.x, projectedTouchPos.y);
        }
        game.gameView.apply();
        draw(deltaTime);
        update(deltaTime);

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
        uiRenderer.setProjectionMatrix(game.uiCamera.combined);
        drawUiTopBar();
        drawUiBottomBar();
    }

    private void drawUiTopBar() {
        final int topBarY = game.targetResHeight - game.targetTopBarHeight;
        uiRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiRenderer.setColor(uiBarColor);
        uiRenderer.rect(0, topBarY, game.targetResWidth, game.targetTopBarHeight);

        final int padding = game.targetTopBarHeight / 4;
        final int indicatorHeight = game.targetTopBarHeight / 2;
        final int maxIndicatorWidth = game.targetResWidth - padding * 2;
        if (players != null) {
            for (Player p : players) {
                Color.rgba8888ToColor(playerColor, p.color8888);
                uiRenderer.setColor(playerColor);
                final int progressInWidth = (int) (maxIndicatorWidth / 100f * p.miniGameProgress);
                uiRenderer.rect(padding, topBarY + padding, progressInWidth, indicatorHeight);
            }
        }
        uiRenderer.end();

        uiRenderer.begin(ShapeRenderer.ShapeType.Line);
        uiRenderer.setColor(Color.GRAY);
        uiRenderer.rect(padding, topBarY + padding, maxIndicatorWidth, indicatorHeight);
        uiRenderer.end();
    }

    private void drawUiBottomBar() {
        final int indicatorSpacing = 12;
        final int padding = game.targetBottomBarHeight / 4;
        final int maxPossibleIndicatorSize = (game.targetResWidth - padding * 2 - (maxRounds - 1) * indicatorSpacing) / maxRounds;
        final int indicatorSize = Math.min(game.targetBottomBarHeight / 2, maxPossibleIndicatorSize);
        final boolean evenNumRounds = maxRounds % 2 == 0;
        final int startPosX = evenNumRounds ?
                game.targetResWidth / 2 - ((maxRounds / 2) * (indicatorSize + indicatorSpacing)) + indicatorSpacing / 2:
                (game.targetResWidth - indicatorSize) / 2 - ((maxRounds / 2) * (indicatorSize + indicatorSpacing));

        uiRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiRenderer.setColor(uiBarColor);
        uiRenderer.rect(0, 0, game.targetResWidth, game.targetBottomBarHeight);

        for (int i = 1; i <= maxRounds; ++i) {
            if (i == currRound) {
                uiRenderer.setColor(Color.SCARLET);
            } else {
                uiRenderer.setColor(Color.GRAY);
            }
            final int currPosX = startPosX + (i - 1) * (indicatorSize + indicatorSpacing);
            uiRenderer.rect(currPosX, (game.targetBottomBarHeight - indicatorSize) / 2, indicatorSize, indicatorSize);
        }
        uiRenderer.end();
    }

    public final Vector2 getTouchPos() {
        return unprojectedTouchPos;
    }

    public final Sprite getSprite(String name) {
        return game.atlas.createSprite(name);
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
