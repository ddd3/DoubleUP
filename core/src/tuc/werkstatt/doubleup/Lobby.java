package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Lobby implements Screen {
    private final DoubleUp game;
    private Sprite backgroundSprite;
    private Sprite checkSprite;
    private Sprite loadingSprite;
    private GlyphLayout loadingTextLayout;
    private GlyphLayout checkTextLayout;
    private float transitionTimeInSeconds = 1.75f;
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

        game.font.setColor(MaterialColors.green);
        checkTextLayout = new GlyphLayout(game.font, "Hostserver found");
        game.font.setColor(MaterialColors.orange);
        loadingTextLayout = new GlyphLayout(game.font, "Discovering hostserver");

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
    }

    @Override
    public void render(float deltaTime) {
        game.uiView.apply();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.uiBatch.setProjectionMatrix(game.uiCamera.combined);
        game.uiBatch.begin();
        backgroundSprite.draw(game.uiBatch);
        if (game.client.isConnected()) {
            checkSprite.draw(game.uiBatch);
            game.font.draw(game.uiBatch, checkTextLayout , (game.targetResWidth - checkTextLayout.width) / 2,
                    checkSprite.getY() - 1.5f * checkTextLayout.height);
        } else {
            loadingSprite.rotate(270f * deltaTime);
            loadingSprite.draw(game.uiBatch);
            game.font.draw(game.uiBatch, loadingTextLayout , (game.targetResWidth - loadingTextLayout.width) / 2,
                    loadingSprite.getY() - 1.5f * loadingTextLayout.height);
        }
        game.uiBatch.end();

        updateLogic(deltaTime);
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
    public void hide() {}

    @Override
    public void dispose() {}
}
