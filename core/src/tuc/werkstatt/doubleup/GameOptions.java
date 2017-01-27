package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Arrays;

public class GameOptions implements Screen {
    private final DoubleUp game;

    public static final int maxPlayers = 16;
    public static int maxMiniGameRounds;
    public enum Sequence {Random, Sequential}
    public static Sequence sequence = Sequence.Random;

    public static final String[] animalNames = {"bird", "bull", "cat", "cow", "dog", "duck", "elephant",
            "fish", "horse", "ladybug", "leopard", "lion", "lobster", "rabbit", "snail", "turtle"};
    public static final Sprite[] animalSprites = new Sprite[animalNames.length];
    private Sprite background;
    private Sprite checkbox;
    private Sprite emptyCheckbox;
    private Sprite gameRulesText;
    private Sprite gameRulesPanel;
    private Sprite gameRulesGreenGerms;
    private Sprite rulesRoundsText;
    private Sprite rulesRandomText;
    private Sprite rulesReadyText;
    private Sprite doodleSelectText;
    private Sprite doodleSelectPanel;
    private Sprite doodleIconBox;
    private Sprite doodleIconBoxSelBack;
    private Sprite doodleIconBoxSelForg;
    private Sprite doodleGreenGerms;
    private Sprite sliderBar;
    private Sprite sliderButton;
    private Sprite startButton;
    private Sprite startGrayButton;
    private GlyphLayout roundsTextLayout;
    private GlyphLayout readyTextLayout;
    private Vector2[] boxPositions = new Vector2[animalNames.length];
    private final int sliderMin = 3;
    private final int sliderMax = 20;
    private Rectangle sliderRect;
    private long lastMessageTime;
    private int cacheNumReadyPlayers = 0;
    private Player[] cachePlayers;

    public GameOptions(final DoubleUp game) {
        this.game = game;
        Network.state = Network.State.GameOptions;
        maxMiniGameRounds = 8;
        initUserInterface();
        lastMessageTime = TimeUtils.millis();
    }

    private void initUserInterface() {
        background = game.getSprite("ui/title_background");
        background.setSize(game.targetResWidth, game.targetResHeight);
        background.setPosition(0, 0);

        // values measured in graphics application
        // top panel
        doodleSelectPanel = game.getSprite("ui/doodle_select_panel");
        doodleSelectPanel.setPosition((game.targetResWidth - doodleSelectPanel.getWidth()) / 2f,
                game.targetResHeight - 1018f);
        doodleGreenGerms = game.getSprite("ui/green_germs");
        doodleGreenGerms.setPosition((game.targetResWidth - doodleGreenGerms.getWidth()) / 2f,
                doodleSelectPanel.getY() + doodleSelectPanel.getHeight() - doodleGreenGerms.getHeight() / 2f);
        doodleSelectText = game.getSprite("ui/doodle_select_text");
        doodleSelectText.setPosition((game.targetResWidth - doodleSelectText.getWidth()) / 2f,
                doodleSelectPanel.getY() + doodleSelectPanel.getHeight() - doodleSelectText.getHeight() / 2f);
        doodleIconBox = game.getSprite("ui/doodle_box");
        doodleIconBoxSelBack = game.getSprite("ui/doodle_box_selected_background");
        doodleIconBoxSelForg = game.getSprite("ui/doodle_box_selected_foreground");

        // bottom panel
        gameRulesPanel = game.getSprite("ui/game_rules_panel");
        gameRulesPanel.setPosition((game.targetResWidth - gameRulesPanel.getWidth()) / 2f, 178f);
        gameRulesGreenGerms = game.getSprite("ui/green_germs");
        gameRulesGreenGerms.setPosition((game.targetResWidth - gameRulesGreenGerms.getWidth()) / 2f,
                gameRulesPanel.getY() + gameRulesPanel.getHeight() - gameRulesGreenGerms.getHeight() / 2f);
        gameRulesText = game.getSprite("ui/game_rules_text");
        gameRulesText.setPosition((game.targetResWidth - gameRulesText.getWidth()) / 2f,
                gameRulesPanel.getY() + gameRulesPanel.getHeight() - gameRulesText.getHeight() / 2f);
        final float horizAlign = 150f;
        rulesRoundsText = game.getSprite("ui/rules_rounds_text");
        rulesRoundsText.setPosition(gameRulesPanel.getX() + horizAlign, gameRulesPanel.getY() + 387);
        rulesRandomText = game.getSprite("ui/rules_random_text");
        rulesRandomText.setPosition(gameRulesPanel.getX() + horizAlign, gameRulesPanel.getY() + 226);
        rulesReadyText = game.getSprite("ui/rules_ready_text");
        rulesReadyText.setPosition(gameRulesPanel.getX() + horizAlign, gameRulesPanel.getY() + 114);
        sliderBar = game.getSprite("ui/slider_bar");
        sliderButton = game.getSprite("ui/slider_button");
        checkbox = game.getSprite("ui/checkbox");
        sliderBar.setSize(rulesRandomText.getWidth() - sliderButton.getWidth() / 2f + 12f + checkbox.getWidth(), sliderBar.getHeight());
        sliderBar.setPosition(gameRulesPanel.getX() + horizAlign + sliderButton.getWidth() / 2f, gameRulesPanel.getY() + 344);
        sliderRect = new Rectangle(sliderBar.getX() - sliderButton.getWidth() / 2f, sliderBar.getY() - sliderButton.getHeight() / 2f,
                sliderBar.getWidth() + sliderButton.getWidth(), sliderButton.getHeight());
        updateSliderButtonPos();
        checkbox.setPosition(sliderRect.getX() + sliderRect.getWidth() - checkbox.getWidth(),
                rulesRandomText.getY() + rulesRandomText.getHeight() / 2f - checkbox.getHeight() / 2f);
        emptyCheckbox = game.getSprite("ui/checkbox_empty");
        emptyCheckbox.setPosition(checkbox.getX(), checkbox.getY());
        startButton = game.getSprite("ui/start_button");
        startButton.setPosition(gameRulesPanel.getX() + (gameRulesPanel.getWidth() - startButton.getWidth()) / 2f,
                gameRulesPanel.getY() - startButton.getHeight() / 2f + 12f);
        startGrayButton = game.getSprite("ui/start_gray_button");
        startGrayButton.setPosition(startButton.getX(), startButton.getY());

        roundsTextLayout = new GlyphLayout(game.font, "" + maxMiniGameRounds);
        readyTextLayout = new GlyphLayout(game.font, "0/1");

        // player icons
        final float spacing = 16f;
        final float padding = 16f;
        final float numIconColumns = 4f;
        final float numIconRows = MathUtils.ceil(animalNames.length / numIconColumns);
        final float iconPanelHeight = doodleSelectPanel.getHeight() - spacing * 2f - doodleGreenGerms.getHeight() / 2f;
        final float iconPanelWidth = doodleSelectPanel.getWidth() - spacing * 2f;
        final float horiBoxSize = (iconPanelWidth - (numIconColumns + 1) * spacing) / numIconColumns;
        final float vertBoxSize = (iconPanelHeight - (numIconRows + 1) * spacing) / numIconRows;
        final float boxSize = Math.min(horiBoxSize, vertBoxSize);
        final float iconSize = boxSize - padding * 2f;
        final float startPosX = doodleSelectPanel.getX() + spacing + iconPanelWidth / 2f - (numIconColumns / 2f * (boxSize + spacing)) + spacing / 2f;
        float startPosY = doodleSelectPanel.getY() + spacing + iconPanelHeight / 2f - (numIconRows / 2f * (boxSize + spacing)) + spacing / 2f + 12f;

        final float originalSize = doodleIconBox.getWidth();
        doodleIconBox.setSize(boxSize, boxSize);
        doodleIconBoxSelBack.setSize(boxSize, boxSize);
        final float scaledForegroundSize = doodleIconBoxSelForg.getWidth() * doodleIconBox.getWidth() / originalSize;
        doodleIconBoxSelForg.setSize(scaledForegroundSize, scaledForegroundSize);
        for (int i = 0; i < animalNames.length; ++i) {
            final float posX = startPosX + (i % numIconColumns * (boxSize + spacing));
            final float posY = startPosY + (i / (int)numIconColumns * ((boxSize + spacing)));
            boxPositions[i] = new Vector2(posX, posY);
            Sprite sp = game.getSprite("ui/icon_" + animalNames[i] + "_contour");
            sp.setSize(iconSize, iconSize);
            sp.setPosition(posX + padding, posY + padding);
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
        background.draw(game.uiBatch);
        doodleSelectPanel.draw(game.uiBatch);
        doodleGreenGerms.draw(game.uiBatch);
        doodleSelectText.draw(game.uiBatch);
        gameRulesPanel.draw(game.uiBatch);
        gameRulesGreenGerms.draw(game.uiBatch);
        gameRulesText.draw(game.uiBatch);
        rulesRoundsText.draw(game.uiBatch);
        rulesRandomText.draw(game.uiBatch);
        rulesReadyText.draw(game.uiBatch);
        if (sequence == Sequence.Random) {
            checkbox.draw(game.uiBatch);
        } else {
            emptyCheckbox.draw(game.uiBatch);
        }
        if (Network.isHosting) {
            sliderBar.draw(game.uiBatch);
            sliderButton.draw(game.uiBatch);
            if (game.server.areAllPlayersReady()) {
                startButton.draw(game.uiBatch);
            } else {
                startGrayButton.draw(game.uiBatch);
            }
        }
        game.font.draw(game.uiBatch, roundsTextLayout, sliderRect.x + sliderRect.width - roundsTextLayout.width,
                rulesRoundsText.getY() + rulesRoundsText.getHeight());
        game.font.draw(game.uiBatch, readyTextLayout, sliderRect.x + sliderRect.width - readyTextLayout.width,
                rulesReadyText.getY() + rulesReadyText.getHeight());
        for (Vector2 pos : boxPositions) {
            doodleIconBox.setPosition(pos.x, pos.y);
            doodleIconBox.draw(game.uiBatch);
        }

        if (cachePlayers != null) {
            for (Player p : cachePlayers) {
                if (p.icon == -1) { continue; }
                doodleIconBoxSelBack.setColor(p.ID == game.client.getID() ? MaterialColors.green : MaterialColors.red);
                doodleIconBoxSelBack.setPosition(boxPositions[p.icon].x, boxPositions[p.icon].y);
                doodleIconBoxSelBack.draw(game.uiBatch);
                final float diff = (doodleIconBoxSelBack.getWidth() - doodleIconBoxSelForg.getWidth()) / 2f;
                doodleIconBoxSelForg.setPosition(doodleIconBoxSelBack.getX() + diff, doodleIconBoxSelBack.getY() + diff);
                doodleIconBoxSelForg.draw(game.uiBatch);
            }
        }
        for (Sprite sp : animalSprites) {
            sp.draw(game.uiBatch);
        }
        game.uiBatch.end();

        updateLogic(deltaTime);
    }

    private void updateLogic(float deltaTime) {
        if (game.isTestingEnvironment()) {
            game.client.sendClientOptionsMessage(0);
        } else if (Gdx.input.justTouched()) {
            Vector3 touchPos = game.uiCamera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            for (Sprite sp : animalSprites) {
                if (sp.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    final int icon = Arrays.asList(animalSprites).indexOf(sp);
                    game.client.sendClientOptionsMessage(icon);
                    break;
                }
            }
            if (Network.isHosting) {
                if (startButton.getBoundingRectangle().contains(touchPos.x, touchPos.y) && game.server.areAllPlayersReady()) {
                    game.server.startMiniGameSession();
                }
                if (checkbox.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    if (sequence == Sequence.Random) {
                        sequence = Sequence.Sequential;
                    } else {
                        sequence = Sequence.Random;
                    }
                }
            }
        } else if (Network.isHosting && Gdx.input.isTouched()) {
            Vector3 touchPos = game.uiCamera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (sliderRect.contains(touchPos.x, touchPos.y)) {
                final float percentage = (touchPos.x - sliderRect.x) / sliderRect.width;
                maxMiniGameRounds = Math.round(percentage * (sliderMax - sliderMin) + sliderMin);
                updateSliderButtonPos();
                updateGlyphLayouts();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.toggleMusicMute();
        }
        if (Network.isHosting) {
            game.server.update();
        }
        if (TimeUtils.timeSinceMillis(lastMessageTime) > 500) {
            updatePlayerCache();
            updateGlyphLayouts();
            if (!Network.isHosting) {
                maxMiniGameRounds = game.client.getMaxMiniGameRounds();
            }
            lastMessageTime = TimeUtils.millis();
        }
    }

    private void updateSliderButtonPos() {
        sliderButton.setPosition(sliderRect.x + (sliderRect.width - sliderButton.getWidth()) *
                (maxMiniGameRounds - sliderMin) / (sliderMax - sliderMin), sliderRect.y);
    }

    private void updateGlyphLayouts() {
        roundsTextLayout.setText(game.font, "" + maxMiniGameRounds);
        if (cachePlayers != null) {
            readyTextLayout.setText(game.font, cacheNumReadyPlayers + "/" + cachePlayers.length);
        }
    }

    private void updatePlayerCache() {
        cacheNumReadyPlayers = game.client.getNumReadyPlayers();
        cachePlayers = game.client.getPlayers();
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
