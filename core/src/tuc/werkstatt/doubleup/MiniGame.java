package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Arrays;
import java.util.Comparator;

public abstract class MiniGame implements Screen {
    public final DoubleUp game;

    private static boolean isInitialized = false;
    private static ShapeRenderer uiShapeRenderer;
    private static Sprite bottomPanelSprite, topPanelSprite, roundIndicatorSprite,
            currRoundIndicatorSprite, flagSprite;
    private static Sprite iconBackgroundSprite, introPanelSprite, introGermSprite,
            count1Sprite, count2Sprite, count3Sprite, countGoSprite, activeCountSprite, holdSprite;
    private static Sprite scoreBoxSprite, pointBackSprite, point1Sprite, point2Sprite, point3Sprite,
            plusSprite, medalGoldSprite, medalSilverSprite, medalBronzeSprite;
    private static Sprite[] animalSprites;
    private Sprite backgroundSprite, iconSprite;

    private static Sound count1Sound, count2Sound, count3Sound, countGoSound, holdSound,
            scoreUpSound, slideSoftSound;

    private static final float indicatorSpacing = 12f;
    private static float indicatorStartPosX;
    private float scoreScaleFactor;

    private Vector3 projectedTouchPos = new Vector3();
    private Vector2 unprojectedTouchPos = new Vector2();
    private Vector2[] scoreBoxPositions;

    private long lastProgressTime;
    private Player[] cachePlayers;
    private Player[] pointSortedPlayers;
    private boolean cachedPlayersSwapped = false;
    private boolean newPointGlyphsGenerated = false;

    private enum State { Intro, Count, Game, Hold, Score, Empty }
    private State state;
    private ScoreState scoreState;
    private long introTimeStamp, countTimeStamp, holdTimeStamp, scoreTimeStamp;
    private int countdown = 3;
    private int scoreAnimStep;
    private boolean holdFired = false;

    private GlyphLayout title;
    private GlyphLayout description;
    private GlyphLayout[] scoreGlyphs;
    private final float descriptionPadding = 32f;

    private boolean isIntroInit = false;
    private boolean isScoreInit = false;
    private boolean finishMessageAlreadySent = false;

    public MiniGame(DoubleUp game) {
        this.game = game;
        if (!isInitialized) {
            initUserInterface();
            initSounds();
            initKeyHandling();
            isInitialized = true;
        }
        state = State.Intro;
        Network.state = Network.State.Minigame;
        game.client.setCurrMinigame(this);
        lastProgressTime = TimeUtils.millis();
        cachePlayers = game.client.getPlayers();
    }

    public static void reinit() {
        isInitialized = false;
    }

    private void initUserInterface() {
        uiShapeRenderer = new ShapeRenderer();
        uiShapeRenderer.setProjectionMatrix(game.uiCamera.combined);
        game.uiBatch.setProjectionMatrix(game.uiCamera.combined);
        animalSprites = Arrays.copyOf(GameOptions.animalSprites, GameOptions.animalSprites.length);

        topPanelSprite = getSprite("ui/top_panel");
        topPanelSprite.setPosition(0, game.targetResHeight - game.targetTopBarHeight);
        bottomPanelSprite = getSprite("ui/bottom_panel");
        bottomPanelSprite.setPosition(0, 0);
        roundIndicatorSprite = getSprite("ui/bottom_circle");
        final int maxRounds = game.client.getMaxMiniGameRounds();
        final float originalIndicatorSize = roundIndicatorSprite.getWidth();
        final float maxPossibleIndicatorSize = Math.max(10, (game.targetResWidth - (maxRounds + 1) * indicatorSpacing) / maxRounds);
        final float scaledIndicatorSize = Math.min(originalIndicatorSize, maxPossibleIndicatorSize);
        indicatorStartPosX = game.targetResWidth / 2f - (maxRounds / 2f * (scaledIndicatorSize + indicatorSpacing)) + indicatorSpacing / 2f;
        currRoundIndicatorSprite = getSprite("ui/bottom_filled");
        roundIndicatorSprite.setSize(scaledIndicatorSize, scaledIndicatorSize);
        currRoundIndicatorSprite.setSize(scaledIndicatorSize, scaledIndicatorSize);
        flagSprite = getSprite("ui/flag");
        flagSprite.setPosition(1140f - flagSprite.getWidth() / 2, game.targetResHeight - flagSprite.getHeight() - 3f);

        // intro overlay
        introPanelSprite = getSprite("ui/doodle_select_panel");
        introPanelSprite.setPosition((game.targetResWidth - introPanelSprite.getWidth()) / 2f,
                (game.targetResHeight - introPanelSprite.getHeight()) / 2f + 100f);
        introGermSprite = getSprite("ui/green_germs");
        introGermSprite.setPosition(introPanelSprite.getX() + (introPanelSprite.getWidth() - introGermSprite.getWidth()) / 2f,
                introPanelSprite.getY() + introPanelSprite.getHeight() - introGermSprite.getHeight() / 2f);
        iconBackgroundSprite = getSprite("ui/doodle_box_selected_background");
        iconBackgroundSprite.setSize(iconBackgroundSprite.getWidth() * 2.5f, iconBackgroundSprite.getHeight() * 2.5f);
        iconBackgroundSprite.setPosition(introPanelSprite.getX() + (introPanelSprite.getWidth() - iconBackgroundSprite.getWidth()) / 2f,
                introPanelSprite.getY() + introPanelSprite.getHeight() - iconBackgroundSprite.getHeight() - 120f);

        // count overlay
        final float countPosFromTop = 640f;
        count1Sprite = getSprite("ui/count1");
        count1Sprite.setPosition(game.targetResWidth / 2f - count1Sprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - count1Sprite.getHeight() / 2f);
        count2Sprite = getSprite("ui/count2");
        count2Sprite.setPosition(game.targetResWidth / 2f - count2Sprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - count2Sprite.getHeight() / 2f);
        count3Sprite = getSprite("ui/count3");
        count3Sprite.setPosition(game.targetResWidth / 2f - count3Sprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - count3Sprite.getHeight() / 2f);
        countGoSprite = getSprite("ui/countGo");
        countGoSprite.setPosition(game.targetResWidth / 2f - countGoSprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - countGoSprite.getHeight() / 2f);
        holdSprite = getSprite("ui/hold");
        holdSprite.setPosition(game.targetResWidth / 2f - holdSprite.getWidth() / 2f,
                game.targetResHeight - countPosFromTop - holdSprite.getHeight() / 2f);

        // hold overlay
        final float holdPosFromTop = countPosFromTop;
        holdSprite = getSprite("ui/hold");
        holdSprite.setPosition(game.targetResWidth / 2f - holdSprite.getWidth() / 2f,
                game.targetResHeight - holdPosFromTop - holdSprite.getHeight() / 2f);

        // score overlay
        scoreBoxSprite = getSprite("ui/doodle_box");
        pointBackSprite = getSprite("ui/points");
        point1Sprite = getSprite("ui/points1");
        point2Sprite = getSprite("ui/points2");
        point3Sprite = getSprite("ui/points3");
        plusSprite = getSprite("ui/plus");
        medalGoldSprite = getSprite("ui/medal_gold");
        medalSilverSprite = getSprite("ui/medal_silver");
        medalBronzeSprite = getSprite("ui/medal_bronze");
    }

    private void initSounds() {
        count1Sound = getSound("sounds/1.ogg");
        count2Sound = getSound("sounds/2.ogg");
        count3Sound = getSound("sounds/3.ogg");
        countGoSound = getSound("sounds/go.ogg");
        holdSound = getSound("sounds/hold.ogg");
        scoreUpSound = getSound("sounds/scoreup.ogg");
        slideSoftSound = getSound("sounds/slide_soft.ogg");
    }

    private void initKeyHandling() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyUp(final int keycode) {
                if (keycode == Input.Keys.BACK) {
                    Gdx.app.log("MiniGame", "Back button pressed, returning to StartScreen");
                    game.setScreen(new Start(game));
                }
                return false;
            }
        });
    }

    public void setTitle(String title) {
        this.title = new GlyphLayout(game.titleFont, title);
    }

    public void setDescription(String description) {
        final float targetWidth = introPanelSprite.getWidth() - descriptionPadding * 2f;
        this.description = new GlyphLayout(game.font, description, MaterialColors.text, targetWidth, Align.center, true);
    }

    public void setIcon(String name) {
        final float padding = 24f;
        final float iconSize = iconBackgroundSprite.getWidth() - padding * 2f;
        iconSprite = getSprite(name);
        final float scaleFactor = iconSize / Math.max(iconSprite.getWidth(), iconSprite.getHeight());
        iconSprite.setSize(iconSprite.getWidth() * scaleFactor, iconSprite.getHeight() * scaleFactor);
        iconSprite.setPosition(iconBackgroundSprite.getX() + (iconBackgroundSprite.getWidth() - iconSprite.getWidth()) / 2f,
                iconBackgroundSprite.getY() + (iconBackgroundSprite.getHeight() - iconSprite.getHeight()) / 2f);
    }

    public void setBackground(String name) {
        backgroundSprite = getSprite(name);
        backgroundSprite.setSize(game.width, game.height);
        backgroundSprite.setPosition(0, game.targetBottomBarHeight);
    }

    // implementation necessary for tracking game state and updating other players/server
    public abstract float getProgress();
    public abstract boolean isFinished();
    public abstract void draw(float deltaTime);
    public abstract void update(float deltaTime);

    @Override
    public final void render(float deltaTime) {
        Gdx.gl.glClearColor(MaterialColors.background.r, MaterialColors.background.g, MaterialColors.background.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.uiView.apply();

        if (state == State.Intro) {
            drawUserInterface();
            introOverlay();
        } else if (state == State.Count) {
            drawUserInterface();
            countOverlay();
        } else if (state == State.Game) {
            drawUserInterface();
            updateTouchPosition();
            updateSubMiniGame(deltaTime);
            updateNetwork();
        } else if (state == State.Hold) {
            drawUserInterface();
            holdOverlay();
        } else if (state == State.Score) {
            drawUserInterface();
            scoreOverlay();
        } else if (state == State.Empty) {
            drawUserInterface();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) { game.toggleMusicMute(); }
    }

    private void drawUserInterface() {
        if (backgroundSprite != null) {
            game.uiBatch.begin();
            backgroundSprite.draw(game.uiBatch);
            game.uiBatch.end();
        } else {
            setBackground("ui/title_background");
        }

        final int topPanelY = game.targetResHeight - game.targetTopBarHeight;
        // values measured in image editor
        final int progressBarX = 20;
        final int progressBarY = topPanelY + 52;
        final int progressBarWidth = 1162;
        final int progressBarHeight = 36;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(Color.WHITE);
        uiShapeRenderer.rect(progressBarX, progressBarY, progressBarWidth, progressBarHeight);
        uiShapeRenderer.setColor(MaterialColors.uiGreen);
        final int currPlayerProgressInWidth = (int) (progressBarWidth / 100f * getProgress());
        uiShapeRenderer.rect(progressBarX, progressBarY, currPlayerProgressInWidth, progressBarHeight);
        uiShapeRenderer.end();

        game.uiBatch.begin();
        topPanelSprite.draw(game.uiBatch);
        flagSprite.draw(game.uiBatch);
        Sprite currPlayerSprite = null;
        for (Player p : cachePlayers) {
            if (p.ID == game.client.getID()) {
                currPlayerSprite = animalSprites[p.icon];
                currPlayerSprite.setSize(96, 96);
                currPlayerSprite.setPosition(progressBarX - currPlayerSprite.getWidth() / 2f + progressBarWidth / 100f * getProgress(),
                        game.targetResHeight - 68f - currPlayerSprite.getHeight() / 2f);
            } else {
                Sprite sp = animalSprites[p.icon];
                sp.setSize(96, 96);
                sp.setPosition(progressBarX - sp.getWidth() / 2f + progressBarWidth / 100f * p.miniGameProgress,
                        game.targetResHeight - 68f - sp.getHeight() / 2f);
                sp.draw(game.uiBatch);
            }
        }
        currPlayerSprite.draw(game.uiBatch);

        bottomPanelSprite.draw(game.uiBatch);
        for (int i = 1; i <= game.client.getMaxMiniGameRounds(); ++i) {
            final float currPosX = indicatorStartPosX + (i - 1) * (roundIndicatorSprite.getWidth() + indicatorSpacing);
            if (i == game.client.getCurrMiniGameRound()) {
                currRoundIndicatorSprite.setPosition(currPosX, (game.targetBottomBarHeight - currRoundIndicatorSprite.getWidth()) / 2);
                currRoundIndicatorSprite.draw(game.uiBatch);
            } else {
                roundIndicatorSprite.setPosition(currPosX, (game.targetBottomBarHeight - roundIndicatorSprite.getWidth()) / 2);
                roundIndicatorSprite.draw(game.uiBatch);
            }
        }
        game.uiBatch.end();
    }

    private void introOverlay() {
        if (!isIntroInit) {
            if (title == null) {
                setTitle("Missing title");
            }
            if (description == null) {
                setDescription("Missing description");
            }
            if (iconSprite == null) {
                setIcon("minigames/PumpBalloon/balloon");
            }
            introTimeStamp = TimeUtils.millis();
            isIntroInit = true;
        } else if (TimeUtils.timeSinceMillis(introTimeStamp) > 4000) {
            state = State.Count;
        }

        game.uiBatch.begin();
        introPanelSprite.draw(game.uiBatch);
        introGermSprite.draw(game.uiBatch);
        iconBackgroundSprite.draw(game.uiBatch);
        iconSprite.draw(game.uiBatch);
        game.titleFont.draw(game.uiBatch, title, introPanelSprite.getX() + (introPanelSprite.getWidth() - title.width) / 2f,
                introPanelSprite.getY() + introPanelSprite.getHeight() + title.height / 2f);
        final float maxDescHeight = iconBackgroundSprite.getY() - introPanelSprite.getY() - descriptionPadding * 2f;
        game.font.draw(game.uiBatch, description, introPanelSprite.getX() + (introPanelSprite.getWidth() - description.width) / 2f,
                introPanelSprite.getY() + 2f * descriptionPadding + (maxDescHeight + description.height) / 2f);
        game.uiBatch.end();
    }

    private void countOverlay() {
        final long stepMillis = 800;
        final float soundVol = 0.55f;
        if (countdown == 3) {
            countTimeStamp = TimeUtils.millis();
            activeCountSprite = count3Sprite;
            count3Sound.play(soundVol);
            --countdown;
        } else if (countdown == 2 && TimeUtils.timeSinceMillis(countTimeStamp) > stepMillis) {
            countTimeStamp = TimeUtils.millis();
            activeCountSprite = count2Sprite;
            count2Sound.play(soundVol);
            --countdown;
        } else if (countdown == 1 && TimeUtils.timeSinceMillis(countTimeStamp) > stepMillis) {
            countTimeStamp = TimeUtils.millis();
            activeCountSprite = count1Sprite;
            count1Sound.play(soundVol);
            --countdown;
        } else if (countdown == 0 && TimeUtils.timeSinceMillis(countTimeStamp) > stepMillis) {
            countTimeStamp = TimeUtils.millis();
            activeCountSprite = countGoSprite;
            countGoSound.play(soundVol);
            --countdown;
        } else if (countdown < 0 && TimeUtils.timeSinceMillis(countTimeStamp) > stepMillis) {
            state = State.Game;
        }
        game.uiBatch.begin();
        final float alphaVal = Math.max(0, 1f - TimeUtils.timeSinceMillis(countTimeStamp) / (float)stepMillis);
        activeCountSprite.setAlpha(alphaVal);
        activeCountSprite.draw(game.uiBatch);
        game.uiBatch.end();
    }

    private void holdOverlay() {
        final long holdTime = 1200;
        if (!holdFired) {
            holdSound.play(0.7f);
            holdTimeStamp = TimeUtils.millis();
            holdFired = true;
        } else if (TimeUtils.timeSinceMillis(holdTimeStamp) > holdTime) {
                state = State.Score;
        }

        game.uiBatch.begin();
        final float alphaVal = Math.max(0, 1f - TimeUtils.timeSinceMillis(holdTimeStamp) / (float)holdTime);
        holdSprite.setAlpha(alphaVal);
        holdSprite.draw(game.uiBatch);
        game.uiBatch.end();
    }

    private enum ScoreState {
        BoxWait, BoxInAnim, AddInAnim, AddWait, AddMoveAnim,
        ScoreUpAnim, SortAnim, MedalInAnim, End
    }

    private class ScoreAnimation {
        final ScoreState state;
        final int duration;
        ScoreAnimation(ScoreState state, int duration) {
            this.duration = duration;
            this.state = state;
        }
    }
    private static Array<ScoreAnimation> animations = null;
    private void initScoreAnimations() {
        if (animations == null) {
            animations = new Array<ScoreAnimation>(true, 12);
            animations.addAll(
                    new ScoreAnimation(ScoreState.BoxInAnim, 550),
                    new ScoreAnimation(ScoreState.BoxWait, 750),
                    new ScoreAnimation(ScoreState.AddInAnim, 550),
                    new ScoreAnimation(ScoreState.AddWait, 1000),
                    new ScoreAnimation(ScoreState.AddMoveAnim, 400),
                    new ScoreAnimation(ScoreState.ScoreUpAnim, 250),
                    new ScoreAnimation(ScoreState.BoxWait, 1000),
                    new ScoreAnimation(ScoreState.SortAnim, 750),
                    new ScoreAnimation(ScoreState.MedalInAnim, 450),
                    new ScoreAnimation(ScoreState.End, 2500)
            );
        }
    }

    private void scoreOverlay() {
        final float scoreSpacing = 32f;
        if (!isScoreInit) {
            cachePlayers = game.client.getPlayers();
            pointSortedPlayers = Arrays.copyOf(cachePlayers, cachePlayers.length);
            Arrays.sort(pointSortedPlayers, new Comparator<Player>() {
                @Override
                public int compare(Player p1, Player p2) {
                    if (p1.points < p2.points) { return 1; }
                    else if (p1.points > p2.points) { return -1; }
                    else { return 0; }
                }
            });
            scaleScoreElements(scoreSpacing);
            setScoreBoxPosition();
            updateScoreGlyphs(recalculatePreviousScoreValues());
            initScoreAnimations();
            scoreAnimStep = 0;
            scoreState = animations.get(0).state;
            scoreTimeStamp = TimeUtils.millis();
            slideSoftSound.play();
            isScoreInit = true;
        } else if (TimeUtils.timeSinceMillis(scoreTimeStamp) >= animations.get(scoreAnimStep).duration) {
            scoreAnimStep++;
            if (scoreAnimStep == animations.size) {
                state = State.Empty;
                if (Network.isHosting) {
                    game.server.sendGameNextMessage();
                }
                return;
            }
            scoreState = animations.get(scoreAnimStep).state;
            scoreTimeStamp = TimeUtils.millis();
        }

        game.uiBatch.begin();
        switch (scoreState) {
            case BoxInAnim: drawBoxInAnimation(scoreSpacing); break;
            case AddInAnim: drawAddInAnimation(scoreSpacing); break;
            case AddWait: drawAddWaitAnimation(scoreSpacing); break;
            case AddMoveAnim: drawAddMoveAnimation(scoreSpacing); break;
            case ScoreUpAnim:
                if (!newPointGlyphsGenerated) {
                    int[] newPoints = new int[cachePlayers.length];
                    for (int i = 0; i < cachePlayers.length; ++i) {
                        newPoints[i] = cachePlayers[i].points;
                    }
                    updateScoreGlyphs(newPoints);
                    scoreUpSound.play(0.6f);
                    newPointGlyphsGenerated = true;
                }
                drawScoreUpAnimation(scoreSpacing);
                break;
            case SortAnim: drawSortAnimation(); break;
            case MedalInAnim:
                if (!cachedPlayersSwapped) {
                    cachePlayers = pointSortedPlayers;
                    int[] newPoints = new int[cachePlayers.length];
                    for (int i = 0; i < cachePlayers.length; ++i) {
                        newPoints[i] = cachePlayers[i].points;
                    }
                    updateScoreGlyphs(newPoints);
                    cachedPlayersSwapped = true;
                }
                drawMedalInAnimation(scoreSpacing);
                break;
            case BoxWait: drawBoxesStill(scoreSpacing); break;
            case End:
                drawBoxesStill(scoreSpacing);
                drawMedals(scoreSpacing, 1f);
                break;
            default:
        }
        game.uiBatch.end();
    }

    private int[] recalculatePreviousScoreValues() {
        int[] oldPoints = new int[cachePlayers.length];
        for (int i = 0; i < cachePlayers.length; ++i) {
            oldPoints[i] = cachePlayers[i].points;
        }
        if (oldPoints.length == 1) {
            oldPoints[0] -= 1;
        } else if (oldPoints.length == 2) {
            oldPoints[0] -= 2;
            oldPoints[1] -= 1;
        } else {
            oldPoints[0] -= 3;
            oldPoints[1] -= 2;
            oldPoints[2] -= 1;
        }
        return oldPoints;
    }

    private void drawMedals(float scoreSpacing, float alpha) {
        for (int j = 0, rank = 1; j < cachePlayers.length; ++j) {
            if (rank == 1) {
                medalGoldSprite.setAlpha(alpha);
                final float x = scoreBoxPositions[j].x - medalGoldSprite.getWidth() - scoreSpacing;
                final float y = scoreBoxPositions[j].y + (scoreBoxSprite.getHeight() - medalGoldSprite.getHeight()) / 2f;
                medalGoldSprite.setPosition(x, y);
                medalGoldSprite.draw(game.uiBatch);
            } else if (rank == 2) {
                medalSilverSprite.setAlpha(alpha);
                final float x = scoreBoxPositions[j].x - medalSilverSprite.getWidth() - scoreSpacing;
                final float y = scoreBoxPositions[j].y + (scoreBoxSprite.getHeight() - medalSilverSprite.getHeight()) / 2f;
                medalSilverSprite.setPosition(x, y);
                medalSilverSprite.draw(game.uiBatch);
            } else {
                medalBronzeSprite.setAlpha(alpha);
                final float x = scoreBoxPositions[j].x - medalBronzeSprite.getWidth() - scoreSpacing;
                final float y = scoreBoxPositions[j].y + (scoreBoxSprite.getHeight() - medalBronzeSprite.getHeight()) / 2f;
                medalBronzeSprite.setPosition(x, y);
                medalBronzeSprite.draw(game.uiBatch);
            }
            if (j + 1 < cachePlayers.length && cachePlayers[j + 1].points != cachePlayers[j].points) {
                rank++;
            }
            if (rank > 3) { break; }
        }
    }

    private void drawBoxesStill(float scoreSpacing) {
        for (int i = 0; i < cachePlayers.length; ++i) {
            final float x = scoreBoxPositions[i].x;
            final float y = scoreBoxPositions[i].y;
            scoreBoxSprite.setPosition(x, y);
            scoreBoxSprite.draw(game.uiBatch);
            final float iconPadding =  16f;
            final float iconSize = scoreBoxSprite.getWidth() - iconPadding * 2f;
            Sprite iconSprite = animalSprites[cachePlayers[i].icon];
            iconSprite.setSize(iconSize, iconSize);
            iconSprite.setPosition(x + iconPadding, y + iconPadding);
            iconSprite.draw(game.uiBatch);
            pointBackSprite.setPosition(x + scoreBoxSprite.getWidth() + scoreSpacing, y);
            pointBackSprite.draw(game.uiBatch);
            game.scoreFont.draw(game.uiBatch, scoreGlyphs[i], pointBackSprite.getX() + (pointBackSprite.getWidth() - scoreGlyphs[i].width) / 2f,
                    pointBackSprite.getY() + (pointBackSprite.getHeight() + scoreGlyphs[i].height) / 2f);
        }
    }

    private void drawMedalInAnimation(float scoreSpacing) {
        final float duration = (float)animations.get(scoreAnimStep).duration;
        final float alphaFactor = Math.min(1, TimeUtils.timeSinceMillis(scoreTimeStamp) / duration);
        drawBoxesStill(scoreSpacing);
        drawMedals(scoreSpacing, alphaFactor);
    }

    private void drawSortAnimation() {
        final float duration = (float)animations.get(scoreAnimStep).duration;
        final float factor = Math.min(1, TimeUtils.timeSinceMillis(scoreTimeStamp) / duration);
        for (int i = 0; i < pointSortedPlayers.length; ++i) {
            float oldY = 0;
            int oldGlyph = 0;
            for (int j = 0; j < cachePlayers.length; ++j) {
                if (pointSortedPlayers[i].ID == cachePlayers[j].ID) {
                    oldY = scoreBoxPositions[j].y;
                    oldGlyph = j;
                }
            }
            final float y = oldY + (scoreBoxPositions[i].y - oldY) * factor;
            scoreBoxSprite.setY(y);
            scoreBoxSprite.draw(game.uiBatch);
            final float iconPadding = 16f;
            final float iconSize = scoreBoxSprite.getWidth() - iconPadding * 2f;
            Sprite iconSprite = animalSprites[pointSortedPlayers[i].icon];
            iconSprite.setSize(iconSize, iconSize);
            iconSprite.setPosition(scoreBoxSprite.getX() + iconPadding, y + iconPadding);
            iconSprite.draw(game.uiBatch);
            pointBackSprite.setY(y);
            pointBackSprite.draw(game.uiBatch);
            game.scoreFont.draw(game.uiBatch, scoreGlyphs[oldGlyph], pointBackSprite.getX() + (pointBackSprite.getWidth() - scoreGlyphs[oldGlyph].width) / 2f,
                    pointBackSprite.getY() + (pointBackSprite.getHeight() + scoreGlyphs[oldGlyph].height) / 2f);
        }
    }

    private void drawScoreUpAnimation(float scoreSpacing) {
        for (int i = 0; i < cachePlayers.length; ++i) {
            final float x = scoreBoxPositions[i].x;
            final float y = scoreBoxPositions[i].y;
            scoreBoxSprite.setPosition(x, y);
            scoreBoxSprite.draw(game.uiBatch);
            final float iconPadding =  16f;
            final float iconSize = scoreBoxSprite.getWidth() - iconPadding * 2f;
            Sprite iconSprite = animalSprites[cachePlayers[i].icon];
            iconSprite.setSize(iconSize, iconSize);
            iconSprite.setPosition(x + iconPadding, y + iconPadding);
            iconSprite.draw(game.uiBatch);
            pointBackSprite.setPosition(scoreBoxSprite.getX() + scoreBoxSprite.getWidth() + scoreSpacing, y);
            pointBackSprite.draw(game.uiBatch);
            game.scoreFont.draw(game.uiBatch, scoreGlyphs[i], pointBackSprite.getX() + (pointBackSprite.getWidth() - scoreGlyphs[i].width) / 2f,
                    pointBackSprite.getY() + (pointBackSprite.getHeight() + scoreGlyphs[i].height) / 2f);

            if (i >= 3) {
                continue;
            }
            final float duration = (float)animations.get(scoreAnimStep).duration;
            final float factor = Math.min(1, TimeUtils.timeSinceMillis(scoreTimeStamp) / duration);
            pointBackSprite.setAlpha(1f - factor);
            pointBackSprite.setScale(1f + 0.75f * factor);
            pointBackSprite.draw(game.uiBatch);
            pointBackSprite.setAlpha(1f);
            pointBackSprite.setScale(1f);
        }
    }

    private void drawAddMoveAnimation(float scoreSpacing) {
        for (int i = 0; i < cachePlayers.length; ++i) {
            final float x = scoreBoxPositions[i].x;
            final float y = scoreBoxPositions[i].y;
            scoreBoxSprite.setPosition(x, y);
            scoreBoxSprite.draw(game.uiBatch);
            final float iconPadding =  16f;
            final float iconSize = scoreBoxSprite.getWidth() - iconPadding * 2f;
            Sprite iconSprite = animalSprites[cachePlayers[i].icon];
            iconSprite.setSize(iconSize, iconSize);
            iconSprite.setPosition(x + iconPadding, y + iconPadding);
            iconSprite.draw(game.uiBatch);
            pointBackSprite.setPosition(scoreBoxSprite.getX() + scoreBoxSprite.getWidth() + scoreSpacing, y);
            pointBackSprite.draw(game.uiBatch);
            game.scoreFont.draw(game.uiBatch, scoreGlyphs[i], pointBackSprite.getX() + (pointBackSprite.getWidth() - scoreGlyphs[i].width) / 2f,
                    pointBackSprite.getY() + (pointBackSprite.getHeight() + scoreGlyphs[i].height) / 2f);

            if (i >= 3) {
                continue;
            }
            final float duration = (float)animations.get(scoreAnimStep).duration;
            final float factor = Math.max(0, 1f - TimeUtils.timeSinceMillis(scoreTimeStamp) / duration);
            final float starMiddle = pointBackSprite.getX() + pointBackSprite.getWidth() / 2f;
            final float plusX = pointBackSprite.getX() + pointBackSprite.getWidth() + scoreSpacing;
            final float plusMovement = starMiddle + (plusX - starMiddle) * factor;
            plusSprite.setAlpha(factor);
            plusSprite.setRotation(0);
            plusSprite.setPosition(plusMovement, pointBackSprite.getY() + (pointBackSprite.getHeight() - plusSprite.getHeight()) / 2f);
            plusSprite.draw(game.uiBatch);
            Sprite currPoint;
            if (i == 0) {
                currPoint = cachePlayers.length >= 3 ? point3Sprite : cachePlayers.length == 2 ? point2Sprite : point1Sprite;
            } else if (i == 1) {
                currPoint = cachePlayers.length >= 3 ? point2Sprite : point1Sprite;
            } else {
                currPoint = point1Sprite;
            }
            final float pointX = pointBackSprite.getX() + pointBackSprite.getWidth() + plusSprite.getWidth() + 2f * scoreSpacing;
            final float pointMovement = starMiddle + (pointX - starMiddle) * factor;
            currPoint.setAlpha(factor);
            currPoint.setRotation(0);
            currPoint.setPosition(pointMovement, plusSprite.getY() + (plusSprite.getHeight() - currPoint.getHeight()) / 2f);
            currPoint.draw(game.uiBatch);
        }
    }

    private void drawAddWaitAnimation(float scoreSpacing) {
        for (int i = 0; i < cachePlayers.length; ++i) {
            final float x = scoreBoxPositions[i].x;
            final float y = scoreBoxPositions[i].y;
            scoreBoxSprite.setPosition(x, y);
            scoreBoxSprite.draw(game.uiBatch);
            final float iconPadding =  16f;
            final float iconSize = scoreBoxSprite.getWidth() - iconPadding * 2f;
            Sprite iconSprite = animalSprites[cachePlayers[i].icon];
            iconSprite.setSize(iconSize, iconSize);
            iconSprite.setPosition(x + iconPadding, y + iconPadding);
            iconSprite.draw(game.uiBatch);
            pointBackSprite.setPosition(scoreBoxSprite.getX() + scoreBoxSprite.getWidth() + scoreSpacing, y);
            pointBackSprite.draw(game.uiBatch);
            game.scoreFont.draw(game.uiBatch, scoreGlyphs[i], pointBackSprite.getX() + (pointBackSprite.getWidth() - scoreGlyphs[i].width) / 2f,
                    pointBackSprite.getY() + (pointBackSprite.getHeight() + scoreGlyphs[i].height) / 2f);

            if (i >= 3) {
                continue;
            }
            plusSprite.setScale(1f);
            plusSprite.setRotation(0f);
            plusSprite.setAlpha(1f);
            plusSprite.setPosition(pointBackSprite.getX() + pointBackSprite.getWidth() + scoreSpacing,
                    pointBackSprite.getY() + (pointBackSprite.getHeight() - plusSprite.getHeight()) / 2f);
            plusSprite.draw(game.uiBatch);
            Sprite currPoint;
            if (i == 0) {
                currPoint = cachePlayers.length >= 3 ? point3Sprite : cachePlayers.length == 2 ? point2Sprite : point1Sprite;
            } else if (i == 1) {
                currPoint = cachePlayers.length >= 3 ? point2Sprite : point1Sprite;
            } else {
                currPoint = point1Sprite;
            }
            currPoint.setScale(1f);
            currPoint.setRotation(0f);
            currPoint.setAlpha(1f);
            currPoint.setPosition(plusSprite.getX() + plusSprite.getWidth() + scoreSpacing,
                    plusSprite.getY() + (plusSprite.getHeight() - currPoint.getHeight()) / 2f);
            currPoint.draw(game.uiBatch);
        }
    }

    private void drawAddInAnimation(float scoreSpacing) {
        for (int i = 0; i < cachePlayers.length; ++i) {
            final float x = scoreBoxPositions[i].x;
            final float y = scoreBoxPositions[i].y;
            scoreBoxSprite.setPosition(x, y);
            scoreBoxSprite.draw(game.uiBatch);
            final float iconPadding =  16f;
            final float iconSize = scoreBoxSprite.getWidth() - iconPadding * 2f;
            Sprite iconSprite = animalSprites[cachePlayers[i].icon];
            iconSprite.setSize(iconSize, iconSize);
            iconSprite.setPosition(x + iconPadding, y + iconPadding);
            iconSprite.draw(game.uiBatch);
            pointBackSprite.setPosition(scoreBoxSprite.getX() + scoreBoxSprite.getWidth() + scoreSpacing, y);
            pointBackSprite.draw(game.uiBatch);
            game.scoreFont.draw(game.uiBatch, scoreGlyphs[i], pointBackSprite.getX() + (pointBackSprite.getWidth() - scoreGlyphs[i].width) / 2f,
                    pointBackSprite.getY() + (pointBackSprite.getHeight() + scoreGlyphs[i].height) / 2f);

            if (i >= 3) {
                continue;
            }
            final float duration = (float)animations.get(scoreAnimStep).duration;
            final float factor = Math.min(1, TimeUtils.timeSinceMillis(scoreTimeStamp) / duration);
            final float rotation = 0f + 360f * factor;
            plusSprite.setScale(factor);
            plusSprite.setRotation(rotation);
            plusSprite.setAlpha(factor);
            plusSprite.setPosition(pointBackSprite.getX() + pointBackSprite.getWidth() + scoreSpacing,
                    pointBackSprite.getY() + (pointBackSprite.getHeight() - plusSprite.getHeight()) / 2f);
            plusSprite.draw(game.uiBatch);
            Sprite currPoint;
            if (i == 0) {
                currPoint = cachePlayers.length >= 3 ? point3Sprite : cachePlayers.length == 2 ? point2Sprite : point1Sprite;
            } else if (i == 1) {
                currPoint = cachePlayers.length >= 3 ? point2Sprite : point1Sprite;
            } else {
                currPoint = point1Sprite;
            }
            currPoint.setScale(factor);
            currPoint.setRotation(rotation);
            currPoint.setAlpha(factor);
            currPoint.setPosition(plusSprite.getX() + plusSprite.getWidth() + scoreSpacing,
                    plusSprite.getY() + (plusSprite.getHeight() - currPoint.getHeight()) / 2f);
            currPoint.draw(game.uiBatch);
        }
    }

    private void drawBoxInAnimation(float scoreSpacing) {
        final float duration = (float)animations.get(scoreAnimStep).duration;
        final float factor = Math.max(0, 1f - TimeUtils.timeSinceMillis(scoreTimeStamp) / duration);
        final float boxX = scoreBoxPositions[0].x;
        final float boxMovement = boxX - (boxX + scoreBoxSprite.getWidth()) * factor;
        final float starX = boxX + scoreBoxSprite.getWidth() + scoreSpacing;
        final float starMovement = starX + (game.targetResWidth - starX) * factor;
        final float iconPadding =  16f;
        final float iconSize = scoreBoxSprite.getWidth() - iconPadding * 2f;
        for (int i = 0; i < cachePlayers.length; ++i) {
            final float y = scoreBoxPositions[i].y;
            scoreBoxSprite.setPosition(boxMovement, y);
            scoreBoxSprite.draw(game.uiBatch);
            Sprite iconSprite = animalSprites[cachePlayers[i].icon];
            iconSprite.setSize(iconSize, iconSize);
            iconSprite.setPosition(boxMovement + iconPadding, y + iconPadding);
            iconSprite.draw(game.uiBatch);
            pointBackSprite.setPosition(starMovement, y);
            pointBackSprite.draw(game.uiBatch);
            game.scoreFont.draw(game.uiBatch, scoreGlyphs[i], pointBackSprite.getX() + (pointBackSprite.getWidth() - scoreGlyphs[i].width) / 2f,
                    pointBackSprite.getY() + (pointBackSprite.getHeight() + scoreGlyphs[i].height) / 2f);
        }
    }

    private void scaleScoreElements(float scoreSpacing) {
        final float scoreMaxHeight = game.targetResHeight - game.targetTopBarHeight - game.targetBottomBarHeight - scoreSpacing * 2f;
        final float originalSize = pointBackSprite.getHeight();
        final float boxSize = Math.min(originalSize, (scoreMaxHeight - (cachePlayers.length + 1) * scoreSpacing) / cachePlayers.length);
        scoreScaleFactor = boxSize / originalSize;

        scoreBoxSprite.setSize(boxSize, boxSize);
        pointBackSprite.setSize(boxSize, boxSize);
        point1Sprite.setSize(point1Sprite.getWidth() * scoreScaleFactor, point1Sprite.getHeight() * scoreScaleFactor);
        point2Sprite.setSize(point2Sprite.getWidth() * scoreScaleFactor, point2Sprite.getHeight() * scoreScaleFactor);
        point3Sprite.setSize(point3Sprite.getWidth() * scoreScaleFactor, point3Sprite.getHeight() * scoreScaleFactor);
        plusSprite.setSize(plusSprite.getWidth() * scoreScaleFactor, plusSprite.getHeight() * scoreScaleFactor);
        medalBronzeSprite.setSize(medalBronzeSprite.getWidth() * scoreScaleFactor, medalBronzeSprite.getHeight() * scoreScaleFactor);
        medalSilverSprite.setSize(medalSilverSprite.getWidth() * scoreScaleFactor, medalSilverSprite.getHeight() * scoreScaleFactor);
        medalGoldSprite.setSize(medalGoldSprite.getWidth() * scoreScaleFactor, medalGoldSprite.getHeight() * scoreScaleFactor);
    }

    private void setScoreBoxPosition() {
        final float spacing = 32f;
        final float lineWidth = medalGoldSprite.getWidth() + scoreBoxSprite.getWidth() +
                pointBackSprite.getWidth() + plusSprite.getWidth() + point3Sprite.getWidth() + 6 * spacing;
        final float posX = (game.targetResWidth - lineWidth) / 2f + 2 * spacing + medalGoldSprite.getWidth();
        float posY = game.targetResHeight / 2f + cachePlayers.length / 2f * (spacing + scoreBoxSprite.getHeight()) + spacing / 2f;
        scoreBoxPositions = new Vector2[cachePlayers.length];
        for (int i = 0; i < cachePlayers.length; ++i) {
            posY -= spacing + scoreBoxSprite.getHeight();
            scoreBoxPositions[i] = new Vector2(posX, posY);
        }
    }

    private void updateScoreGlyphs(int[] points) {
        scoreGlyphs = new GlyphLayout[points.length];
        for (int i = 0; i < points.length; ++i) {
            scoreGlyphs[i] = new GlyphLayout(game.scoreFont, "" + points[i]);
        }
    }

    private void updateTouchPosition() {
        if (Gdx.input.isTouched()) {
            projectedTouchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(projectedTouchPos);
            unprojectedTouchPos.set(projectedTouchPos.x, projectedTouchPos.y);
        }
    }

    private void updateSubMiniGame(final float deltaTime) {
        game.gameView.apply();
        draw(deltaTime);
        update(deltaTime);
    }

    private void updateNetwork() {
        if (Network.isHosting) { game.server.update(); }
        if (isFinished() && !finishMessageAlreadySent) {
            game.client.sendClientFinishedMessage();
            finishMessageAlreadySent = true;
        }
        if (TimeUtils.timeSinceMillis(lastProgressTime) > 500) {
            game.client.sendClientProgressMessage(getProgress());
            lastProgressTime = TimeUtils.millis();
            cachePlayers = game.client.getPlayers();
        }
    }

    public final Vector2 getTouchPos() { return unprojectedTouchPos; }
    public final Sprite getSprite(String name) { return game.getSprite(name); }
    public final Sound getSound(String name) { return game.getSound(name); }

    @Override
    public void resize(int width, int height) {
        game.resizeViews();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    public final void finished() {
        state = State.Hold;
    }
}
