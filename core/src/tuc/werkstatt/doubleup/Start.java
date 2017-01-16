package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;

public class Start implements Screen {
    private final DoubleUp game;
    private ObjectMap<String, Sprite> sprites;
    private boolean isHosting = false;
    private boolean isSlidedOut = false;

    public Start(final DoubleUp game) {
        this.game = game;
        final float padding = 30;
        sprites = new ObjectMap<String, Sprite>();
        Sprite sp;

        //TODO: change this mess into a scene2d.ui
        sp = game.atlas.createSprite("ui/title_background");
        sp.setPosition(0, 0);
        sprites.put("title_background", sp);

        sp = game.atlas.createSprite("ui/help_button");
        sp.setPosition(padding, padding);
        sprites.put("help_button", sp);

        sp = game.atlas.createSprite("ui/settings_button");
        sp.setPosition(game.targetResWidth - sp.getWidth() - padding, padding);
        sprites.put("settings_button", sp);

        sp = game.atlas.createSprite("ui/settings_slideout");
        sp.setPosition(sprites.get("settings_button").getX() + (sprites.get("settings_button").getWidth() - sp.getWidth()) / 2,
                sprites.get("settings_button").getY() + sprites.get("settings_button").getHeight() / 1.25f);
        sprites.put("settings_slideout", sp);

        sp = game.atlas.createSprite("ui/title_logo");
        sp.setPosition((game.targetResWidth - sp.getWidth()) / 2, game.targetResHeight - sp.getHeight() - padding * 3);
        sprites.put("title_logo", sp);

        sp = game.atlas.createSprite("ui/join_button");
        sp.setPosition((game.targetResWidth - sp.getWidth()) / 2, (game.targetResHeight - sp.getHeight()) / 2);
        sprites.put("join_button", sp);

        sp = game.atlas.createSprite("ui/host_panel");
        sp.setPosition((game.targetResWidth - sp.getWidth()) / 2, sprites.get("join_button").getY() - sp.getHeight() + 15);
        sprites.put("host_panel", sp);

        Sprite parent = sprites.get("host_panel");
        sp = game.atlas.createSprite("ui/toggle_green");
        sp.setPosition(parent.getX() + parent.getWidth() - sp.getWidth() - 35,
                parent.getY() + parent.getHeight() / 2 - sp.getHeight() / 2);
        sprites.put("toggle_green", sp);

        sp = game.atlas.createSprite("ui/toggle_red");
        sp.setPosition(sprites.get("toggle_green").getX(), sprites.get("toggle_green").getY());
        sprites.put("toggle_red", sp);

        sp = game.atlas.createSprite("ui/highscores_button");
        sp.setPosition((game.targetResWidth - sp.getWidth()) / 2, sprites.get("host_panel").getY() - sp.getHeight() - padding);
        sprites.put("highscores_button", sp);
    }

    @Override
    public void show() {
        game.loadMusic("music/best_intro_loop.ogg");
    }

    @Override
    public void render(float deltaTime) {
        game.uiView.apply();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.setProjectionMatrix(game.uiCamera.combined);
        game.batch.begin();
        sprites.get("title_background").draw(game.batch);
        sprites.get("help_button").draw(game.batch);
        if (isSlidedOut) {
            sprites.get("settings_slideout").draw(game.batch);
        }
        sprites.get("settings_button").draw(game.batch);
        sprites.get("title_logo").draw(game.batch);
        sprites.get("host_panel").draw(game.batch);
        sprites.get(isHosting ? "toggle_green" : "toggle_red").draw(game.batch);
        sprites.get("highscores_button").draw(game.batch);
        sprites.get("join_button").draw(game.batch);
        game.batch.end();

        updateLogic(deltaTime);
    }

    private void updateLogic(float deltaTime) {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = game.uiCamera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (sprites.get("toggle_green").getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                isHosting = !isHosting;
            } else if (sprites.get("settings_button").getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                isSlidedOut = !isSlidedOut;
            } else if (sprites.get("join_button").getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                game.setScreen(new Lobby(game, isHosting));
            }
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
