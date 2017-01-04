package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

public class Title implements Screen {
    private final DoubleUpPrototype game;
    private final Texture logoTex;

    public Title(final DoubleUpPrototype game) {
        this.game = game;
        logoTex = new Texture(Gdx.files.internal("devlogo.png"));
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        game.batch.draw(logoTex, game.width / 2 - logoTex.getWidth() / 2,
                game.height / 2 - logoTex.getHeight() / 2);
        game.font.draw(game.batch, "TitleScreen", 10, 20);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.setScreen(new Start(game));
            dispose();
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
        logoTex.dispose();
    }
}
