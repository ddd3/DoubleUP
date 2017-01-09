package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class PumpBalloon extends MiniGame {
    private final float initialRadius = 50;
    private float balloonRadius = initialRadius;
    private final int edgeDist = Math.max(game.width, game.height) / 2;
    private final int numOfPumpsUntilFull = 30;
    private Color balloonColor;
    private ShapeRenderer renderer;

    public PumpBalloon(DoubleUp game) {
        super(game);
    }

    @Override
    public void show() {
        renderer = new ShapeRenderer();
        balloonColor = new Color(1f, 1f, 0f, 1f);
    }

    @Override
    public int getProgress() {
        return (int)((balloonRadius - initialRadius) / (edgeDist - initialRadius) * 100);
    }

    @Override
    public boolean isFinished() { return balloonRadius >= edgeDist; }

    @Override
    public void draw(float deltaTime) {
        renderer.setProjectionMatrix(game.camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(balloonColor);
        renderer.circle(game.width / 2, game.height / 2, balloonRadius, 50);
        renderer.end();
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        game.font.draw(game.batch, "PumpBalloon - touch to pump: " + getProgress() + "%",
                10, game.font.getLineHeight());
        game.batch.end();
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.justTouched()) {
            balloonRadius += (edgeDist - initialRadius) / numOfPumpsUntilFull;
            balloonColor.g -= 1f / numOfPumpsUntilFull;
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        renderer.dispose();
    }
}
