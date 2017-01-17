package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.LinkedList;
import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class FindTheMatch extends MiniGame {

    protected Texture card, cardBack, cardBackMark;
    public TextureRegion[] icons;
    public Texture grey, background;
    protected BitmapFont font;
    public Texture gradientTop, gradientBottom;
    public Sound sndFlipCard, sndDing;
    private GameModel model = null;
    private int numRows = 4;
    private int numCols = 4;
    private float h;
    private float cardX, cardY;
    private float cardHeight;
    int currentmatch;
    int maxmatch=8;
    GlyphLayout fontLayout = new GlyphLayout();

    public FindTheMatch(DoubleUp game) {
        super(game);

        card = new Texture(getFileHandle("card-front.png"));
        cardBack = new Texture(getFileHandle("card-back.png"));
        cardBackMark = new Texture(getFileHandle("card-back-mark.png"));
        grey = new Texture(getFileHandle("grey.png"));
        background = new Texture(getFileHandle("background.png"));
        gradientTop = new Texture(getFileHandle("gradient_oben.png"));
        gradientBottom = new Texture(getFileHandle("gradient_unten.png"));

        cardBack.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        cardBackMark.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Texture vehiclesTexture = new Texture(getFileHandle("vehicles.png"));
        vehiclesTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegion[][] veh = TextureRegion.split(vehiclesTexture, 256, 256);

        icons = new TextureRegion[24];
        icons[0] = new TextureRegion(loadTexture("animals/Butterfly_128x128.png"));
        icons[1] = new TextureRegion(loadTexture("animals/Dolphin_128x128.png"));
        icons[2] = new TextureRegion(loadTexture("animals/Elephant_128x128.png"));
        icons[3] = new TextureRegion(loadTexture("animals/Hippopotamus_128x128.png"));
        icons[4] = new TextureRegion(loadTexture("animals/Panda_128x128.png"));
        icons[5] = new TextureRegion(loadTexture("animals/Turtle_128x128.png"));
        icons[6] = new TextureRegion(loadTexture("vacation/surfboard_256x256.png"));
        icons[7] = new TextureRegion(loadTexture("vacation/umbrella_256x256.png"));

        int count = 0;
        for (int i = 0; i < veh.length; i++) {
            TextureRegion[] textureRegions = veh[i];
            for (int j = 0; j < textureRegions.length; j++) {
                icons[8 + count] = textureRegions[j];
                count++;
            }
        }

        sndFlipCard = Gdx.audio.newSound(getFileHandle("flipcard.ogg"));
        sndDing = Gdx.audio.newSound(getFileHandle("ding.ogg"));
    }

    private FileHandle getFileHandle(String file) {
        return Gdx.files.internal("images/minigames/FindTheMatch/"+file);
    }

    private Texture loadTexture(String fileHandle) {
        Texture tmp;
        tmp = new Texture(getFileHandle(fileHandle));
        tmp.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return tmp;
    }

    public void shuffleIcons() {
        TextureRegion tmp;
        int a, b;
        for (int k = 0; k < 25; k++) {
            a = (int) Math.floor(Math.random()*icons.length);
            b = (int) Math.floor(Math.random()*icons.length);
            if (a!=b) {
                tmp = icons[a];
                icons[a] = icons[b];
                icons[b] = tmp;
            }
        }
    }

    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
    }

    public void startGame() {
        model = new GameModel(16);
        numRows = 4;
        numCols = 4;
        shuffleIcons();
    }

    @Override
    public float getProgress() {
        maxmatch = 8;
        return 100f * currentmatch / maxmatch;
    }

    @Override
    public boolean isFinished() {
        return maxmatch == currentmatch;
    }

    @Override
    public void draw(float deltaTime) {
        if (model == null) {
            startGame();
        }
        h = 1920;
        cardHeight = (float) (((h - (numRows + 1) * 16) / numRows)/1.5);
        if (model.introAnimation < 0) {
            compute(16, cardHeight);

            if (model.needsToBeCovered) {
                model.coverCards();
                model.needsToBeCovered = false;
            }
        }
        else model.introAnimation -= Gdx.graphics.getDeltaTime();

        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        game.batch.draw(background, 0, 0, 1200, 1920);

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                cardX = 80 + (c * (cardHeight - cardHeight / 4.9f));
                cardY = 340 + r * (cardHeight-80 + 16);;
                float cardTime = model.introAnimationLength / ((float) model.numCards);
                if (cardTime * (c + numCols * r) < model.introAnimationLength - model.introAnimation) {
                    if (model.getState((c + numCols * r)) == 0) {
                        game.batch.draw(cardBackMark, cardX, cardY, cardHeight, cardHeight);
                    } else if (model.getState((c + numCols * r)) == 1) {
                        game.batch.draw(cardBack, cardX, cardY, cardHeight, cardHeight);
                    } else if (model.getState((c + numCols * r)) >= 2) {
                        game.batch.draw(card, cardX, cardY, cardHeight, cardHeight);

                        if (icons == null) {
                            String str = "" + model.icon[(c + numCols * r)];
                            font.draw(game.batch, str,
                                    cardX + cardHeight / 2 - getStringWidth(font, str) / 2,
                                    cardY + cardHeight / 2 + getStringHeight(font, str) / 2);
                        } else {
                            float iconSize = cardHeight / 2;
                            try {
                                game.batch.draw(icons[model.icon[(c + numCols * r)]],
                                        cardX + cardHeight / 2 - iconSize / 2,
                                        cardY + cardHeight / 2 - iconSize / 2,
                                        iconSize, iconSize);
                            } catch (NullPointerException npe) {
                                npe.printStackTrace();
                            }

                        }
                    }
                }
            }
        }
        game.batch.end();
    }

    @Override
    public void update(float deltaTime) {   }

    @Override
    public void hide() {   }

    @Override
    public void dispose() {   }

    private void compute(float offset, float cardHeight) {
        if (Gdx.input.justTouched()) {
            // (x,y) from top left corner
            float x = Gdx.input.getX()/ ((float) Gdx.graphics.getWidth())* ((float) 1200);
            float y = Gdx.input.getY()/ ((float) Gdx.graphics.getHeight())* ((float) 1920);
            // System.out.println("x" + x);
            // System.out.println("y" + y);
            // System.out.println("-----------");

            // row might be easier:
            int hitRow = -1;
            for (int r = 0; r < numRows; r++) {

                if (y > 1207 && y < 1410 ) {
                    hitRow = 0;
                }
                if (y > 972 && y < 1171 ) {
                    hitRow = 1;
                }
                if (y > 718 && y < 920 ) {
                    hitRow = 2;
                }
                if (y > 475 && y < 678 ) {
                    hitRow = 3;
                }
            }
            // now for the cols:
            int hitCol = -1;
            for (int c = 0; c < numCols; c++) {
                if (x > 115 && x < 345 ) {
                    hitCol = 0;
                }
                if (x > 370 && x < 584 ) {
                    hitCol = 1;
                }
                if (x > 612 && x < 830 ) {
                    hitCol = 2;
                }
                if (x > 853 && x < 1080 ) {
                    hitCol = 3;
                }
            }
            // adapt model ..
            if (hitCol > -1 && hitRow > -1) {
                model.turnCard(hitCol + numCols * hitRow);
            }
        }
    }

    private float getStringWidth(BitmapFont font, String str) {
        fontLayout.setText(font, str);
        return fontLayout.width;
    }

    private float getStringHeight(BitmapFont font, String str) {
        fontLayout.setText(font, str);
        return fontLayout.height;
    }

    class GameModel {
        int[] state;
        int[] icon;
        int numCards;
        float introAnimation;
        float introAnimationLength = 1; // intro animation in seconds ...
        int numberOfMoves;
        float time;
        boolean needsToBeCovered;

        GameModel(int numCards) {
            time = -introAnimationLength;
            this.numCards = numCards;
            state = new int[numCards];
            icon = new int[numCards];
            // checks if flash mode is enabled. If so we need to cover the cards after the initial animation and set "needsToBeCovered" to false
            needsToBeCovered = false;
            for (int i = 0; i < state.length; i++) {
                state[i] = 0;
            }

            // randomize the icons on the cards.
            LinkedList<Integer> icons = new LinkedList<Integer>();
            for (int i = 0; i < numCards / 2; i++) {
                icons.add(i);
                icons.add(i);
            }
            for (int i = 0; i < icon.length; i++) {
                icon[i] = icons.remove((int) Math.floor(Math.random() * icons.size()));
            }
            introAnimation = introAnimationLength;
            numberOfMoves = 0;
        }

        public void coverCards() {
            for (int i = 0; i < state.length; i++) {
                state[i] = 0;
            }
        }

        public int getState(int card) {
            // 0 ... new and face down
            // 1 ... visited but face down
            // 2 ... turned but not found in a pair
            // 3 ... found as part of a pair
            return state[card];
        }

        public void turnCard(int card) {
            // check if there are already two cards turned ...
            int countTurned = 0;
            boolean found = false;
            for (int i = 0; i < state.length; i++) {
                if (state[i] == 2) {
                    countTurned++;
                }
            }
            if (countTurned == 0) {
                if (state[card] < 2) {
                    state[card] = 2;
//                    numberOfMoves++;
                    sndFlipCard.play();
                }
            } else if (countTurned == 1) {
                int iconTurned = icon[card], lastTurned = card;
                for (int i = 0; i < state.length; i++) {
                    if (state[i] == 2 && i != card) {
                        if (iconTurned == icon[i]) {
                            // Player has found a pair of cards ...
                            // ------------------------------------
                            state[i] = 3;
                            state[lastTurned] = 3;
                            found = true;
                            numberOfMoves++;
                            currentmatch = currentmatch+1;
                            sndFlipCard.play();
                            sndDing.play();
                        }
                    }
                }
                if (!found & state[card] < 2) {
                    state[card] = 2;
                    numberOfMoves++;
                    sndFlipCard.play();
                }
            } else {
                for (int i = 0; i < state.length; i++) {
                    if (state[i] == 2) {
                        state[i] = 1;
                    }
                }
            }
        }
    }
}
