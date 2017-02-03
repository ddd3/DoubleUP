package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.TimeUtils;

public class Results implements Screen {
    private final DoubleUp game;

    public Results(DoubleUp game) {
        this.game = game;
    }

    @Override
    public void show() {
        if (!game.isTestingEnvironment()) {
            game.loadMusic("music/best_intro_loop.ogg");
        }
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyUp(final int keycode) {
                if (keycode == Input.Keys.BACK) {
                    Gdx.app.log("Results", "Back button pressed, returning to StartScreen");
                    game.setScreen(new Start(game));
                }
                if (keycode == Input.Keys.Q || keycode == Input.Keys.ESCAPE) {
                    Gdx.app.log("Results", "Escape button pressed, returning to StartScreen");
                    game.setScreen(new Start(game));
                }
                return false;
            }
        });
        game.screenTransitionTimestamp = TimeUtils.millis();
    }

    @Override
    public void render(float deltaTime) {
        draw(deltaTime);
        game.drawTransitionBuffer();
        updateLogic(deltaTime);
    }

    public void draw(float deltaTime) {
        game.uiView.apply();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void updateLogic(float deltaTime) {
        if (Gdx.input.justTouched()) {

            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        game.resizeViews();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        game.renderToTransitionBuffer(this);
    }

    @Override
    public void dispose() {}
}
