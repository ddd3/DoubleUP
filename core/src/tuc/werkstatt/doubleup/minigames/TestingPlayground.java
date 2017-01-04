package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import tuc.werkstatt.doubleup.DoubleUpPrototype;
import tuc.werkstatt.doubleup.MiniGame;

public final class TestingPlayground extends MiniGame {

    public TestingPlayground(DoubleUpPrototype game) {
        super(game);
    }

    @Override
    public void show() {}

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public boolean isFinished() { return false; }

    @Override
    public void draw(float deltaTime) {
//        game.batch.setProjectionMatrix(game.camera.combined);
//        game.batch.begin();
//        game.batch.end();
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            Gdx.app.exit();
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
    public void dispose() {}
}
