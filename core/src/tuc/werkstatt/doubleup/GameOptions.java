package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.util.Arrays;

public class GameOptions implements Screen {
    private final DoubleUp game;
    public static final int maxPlayers = 16;
    public static final int maxMiniGameRounds = 10;
    public static final String[] animalNames = {"bird", "bull", "cat", "cow", "dog", "duck", "elephant",
            "fish", "horse", "ladybug", "leopard", "lion", "lobster", "rabbit", "snail", "turtle"};
    public static final Sprite[] animalSprites = new Sprite[animalNames.length];
    private Sprite backgroundSprite;
    public enum Sequence {Random, Sequential}
    public static final Sequence sequence = Sequence.Sequential;

    public GameOptions(final DoubleUp game) {
        this.game = game;
        Network.state = Network.State.GameOptions;

        backgroundSprite = game.getSprite("ui/title_background");
        backgroundSprite.setSize(game.targetResWidth, game.targetResHeight);
        backgroundSprite.setPosition(0, 0);

        final float padding = 24f;
        final float numIconColumns = 4f;
        final float numIconRows = MathUtils.ceil(animalNames.length / numIconColumns);
        final float iconPanelHeight = game.targetResHeight / 2f - padding * 2f;
        final float iconPanelWidth = game.targetResWidth - padding * 2f;
        final float horiIconSize = (iconPanelWidth - (numIconColumns + 1) * padding) / numIconColumns;
        final float vertIconSize = (iconPanelHeight - (numIconRows + 1) * padding) / numIconRows;
        final float iconSize = Math.min(horiIconSize, vertIconSize);
        final float iconPanelX = padding;
        final float iconPanelY = padding;
        final float startPosX = iconPanelWidth / 2f - (numIconColumns / 2f * (iconSize + padding)) + padding / 2f + iconPanelX;
        final float startPosY = iconPanelHeight / 2f - (numIconRows / 2f * (iconSize + padding)) + padding / 2f + iconPanelY;

        for (int i = 0; i < animalNames.length; ++i) {
            Sprite sp = game.getSprite("ui/icon_" + animalNames[i] + "_contour");
            sp.setSize(iconSize, iconSize);
            final float posX = startPosX + (i % numIconColumns * (iconSize + padding));
            final float posY = startPosY + (i / (int)numIconColumns * ((iconSize + padding)));
            sp.setPosition(posX, posY);
            animalSprites[i] = sp;
        }
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
        for (Sprite sp : animalSprites) {
            sp.setColor(Color.WHITE);
            sp.draw(game.uiBatch);
        }
        Player[] players = game.client.getPlayers();
        if (players != null) {
            for (Player p : players) {
                if (p.icon < 0) {
                    continue;
                }
                Sprite sp = animalSprites[p.icon];
                sp.setColor(p.ID == game.client.getID() ? MaterialColors.teal : MaterialColors.red);
                sp.draw(game.uiBatch);
            }
        }
        game.uiBatch.end();

        updateLogic(deltaTime);
    }

    private void updateLogic(float deltaTime) {
        if (game.isTestingEnvironment()) {
            game.client.sendClientOptionsMessage(0);
            //game.client.sendClientReadyMessage(true);
        } else if (Gdx.input.justTouched()) {
            Vector3 touchPos = game.uiCamera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            for (Sprite sp : animalSprites) {
                if (sp.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    final int icon = Arrays.asList(animalSprites).indexOf(sp);
                    game.client.sendClientOptionsMessage(icon);
                    //game.client.sendClientReadyMessage(true);
                    break;
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.toggleMusicMute();
        }
        if (Network.isHosting) {
            game.server.update();
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
