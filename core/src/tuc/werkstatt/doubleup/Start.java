package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Start implements Screen {
    private final DoubleUpPrototype game;
    private final Texture hostButtonTex;
    private final Texture joinButtonTex;
    private final Rectangle hostRect;
    private final Rectangle joinRect;

    public Start(final DoubleUpPrototype game) {
        this.game = game;

        // quick an dirty buttons, reimplement via scene2d ui components
        hostButtonTex = new Texture(Gdx.files.internal("devHostButton.png"));
        joinButtonTex = new Texture(Gdx.files.internal("devJoinButton.png"));
        hostRect = new Rectangle(game.width / 2 - hostButtonTex.getWidth() / 2,
                game.height / 2 + 15, hostButtonTex.getWidth(), hostButtonTex.getHeight());
        joinRect = new Rectangle(game.width / 2 - joinButtonTex.getWidth() / 2,
                game.height / 2 - joinButtonTex.getHeight() - 15,
                joinButtonTex.getWidth(), joinButtonTex.getHeight());
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        game.batch.draw(hostButtonTex, hostRect.getX(), hostRect.getY());
        game.batch.draw(joinButtonTex, joinRect.getX(), joinRect.getY());
        game.font.draw(game.batch, "StartScreen - options, game rules", 10, 20);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            Vector3 touchPos = game.camera.unproject(
                    new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (hostRect.contains(touchPos.x, touchPos.y)) {
                game.setScreen(new Lobby(game, true));
                dispose();
            } else if (joinRect.contains(touchPos.x, touchPos.y)) {
                game.setScreen(new Lobby(game, false));
                dispose();
            }
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
        joinButtonTex.dispose();
        hostButtonTex.dispose();
    }
}
