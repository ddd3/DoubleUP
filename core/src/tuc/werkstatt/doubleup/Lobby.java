package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.TimeUtils;

public class Lobby implements Screen {
    private final DoubleUp game;
    private Sprite backgroundSprite;
    private Sprite checkSprite;
    private Sprite loadingSprite;
    private Sprite checkTextSprite;
    private Sprite loadingTextSprite;
    private float transitionTimeInSeconds = 2f;
    private boolean isReady = false;

    public Lobby(DoubleUp game) {
        this.game = game;
        Network.state = Network.State.Lobby;

        backgroundSprite = game.getSprite("ui/title_background");
        backgroundSprite.setSize(game.targetResWidth, game.targetResHeight);
        backgroundSprite.setPosition(0, 0);
        checkSprite = game.getSprite("ui/check");
        checkSprite.setPosition((game.targetResWidth - checkSprite.getWidth()) / 2,
                (game.targetResHeight - checkSprite.getHeight()) / 2);
        loadingSprite = game.getSprite("ui/loading");
        loadingSprite.setPosition((game.targetResWidth - loadingSprite.getWidth()) / 2,
                (game.targetResHeight - loadingSprite.getHeight()) / 2);
        loadingSprite.setOriginCenter();

        checkTextSprite = game.getSprite("ui/check_text");
        checkTextSprite.setPosition((game.targetResWidth - checkTextSprite.getWidth()) / 2f,
                checkSprite.getY() - checkTextSprite.getHeight() - checkTextSprite.getHeight() / 4f);
        loadingTextSprite = game.getSprite("ui/loading_text");
        loadingTextSprite.setPosition((game.targetResWidth - loadingTextSprite.getWidth()) / 2f,
                loadingSprite.getY() - loadingTextSprite.getHeight() - loadingTextSprite.getHeight() / 4f);

        if (Network.isHosting) {
            game.server = new Server(game);
        }
        game.client = new Client(game);
    }

    @Override
    public void show() {
        if (!game.isTestingEnvironment()) {
            game.loadMusic("music/best_intro_loop.ogg");
        }
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyUp(final int keycode) {
                if (keycode == Input.Keys.BACK) {
                    Gdx.app.log("Lobby", "Back button pressed, returning to StartScreen");
                    game.setScreen(new Start(game));
                }
                if (keycode == Input.Keys.Q || keycode == Input.Keys.ESCAPE) {
                    Gdx.app.log("Lobby", "Escape button pressed, returning to StartScreen");
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
        updateLogic(deltaTime);
        game.drawTransitionBuffer();
    }

    public void draw(float deltaTime) {
        game.uiView.apply();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.uiBatch.setProjectionMatrix(game.uiCamera.combined);
        game.uiBatch.begin();
        backgroundSprite.draw(game.uiBatch);
        if (game.client.isConnected()) {
            checkSprite.draw(game.uiBatch);
            checkTextSprite.draw(game.uiBatch);
        } else {
            loadingSprite.rotate(270f * deltaTime);
            loadingSprite.draw(game.uiBatch);
            loadingTextSprite.draw(game.uiBatch);
        }
        game.uiBatch.end();
    }

    private void updateLogic(float deltaTime) {
        //TODO: android back button and keyboard escape should return to startscreen
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.toggleMusicMute();
        }

        if(!game.client.isConnected() || isReady) { return; }
        transitionTimeInSeconds -= deltaTime;
        if (game.isTestingEnvironment() || transitionTimeInSeconds < 0) {
            game.setScreen(new GameOptions(game));
            isReady = true;
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
