package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;
import tuc.werkstatt.doubleup.Network;

public final class TurnGems extends MiniGame {
    private Sprite redSprite, greenSprite, clockSprite;
    private final int gemRows = MathUtils.random(6, 8);
    private final int gemColumns = gemRows;
    private final int maxGems = gemRows * gemColumns;
    private int[][] gemBoard;
    private Rectangle[][] gemRects;
    private final float spacing = 8f;
    private final float padding = 48f;
    private float timeLeftInSec = 15;

    public TurnGems(DoubleUp game) {
        super(game);
        setTitle("Turn Gems");
        setDescription("Touch to turn as many gems as possible [#80be1f]green[], adjacent ones will change as well");
        setBackground("ui/title_background");
        setIcon("minigames/TurnGems/red");

        gemBoard = new int[gemRows][gemColumns];
        gemRects = new Rectangle[gemRows][gemColumns];

        final float boardSize = game.width - 2f * padding;
        final float gemSize = (boardSize - (gemRows - 1) * spacing) / (float)gemRows;
        final float startX = game.width / 2f - (gemColumns / 2f * (gemSize + spacing)) + spacing / 2f;
        final float startY = game.height / 2f - gemSize - spacing + (gemRows / 2f * (gemSize + spacing)) + spacing / 2f + 45f;
        for (int row = 0; row < gemRows; ++row) {
            for (int col = 0; col < gemColumns; ++col) {
                gemBoard[row][col] = -1;
                final float posX = startX + (col * (gemSize + spacing));
                final float posY = startY - (row * (gemSize + spacing));
                gemRects[row][col] = new Rectangle(posX, posY, gemSize, gemSize);
            }
        }
        redSprite = getSprite("minigames/TurnGems/red");
        redSprite.setSize(gemSize, gemSize);
        greenSprite = getSprite("minigames/TurnGems/green");
        greenSprite.setSize(gemSize, gemSize);
        clockSprite = getSprite("minigames/TurnGems/clock");
        clockSprite.setPosition(startX, game.height - clockSprite.getHeight() * 2f);
    }

    private Array<ChangeAnimation> animations = new Array<ChangeAnimation>();
    private Pool<ChangeAnimation> animationPool = new Pool<ChangeAnimation>() {
        @Override
        protected ChangeAnimation newObject() {
            return new ChangeAnimation();
        }
    };
    private class ChangeAnimation implements Pool.Poolable {
        Sprite sprite;
        float x;
        float y;
        float size;
        float alpha;
        boolean alive;

        ChangeAnimation() {
            alive = false;
        }

        public void update(float deltaTime) {
            size -= 4f * deltaTime;
            alpha = Math.max(0f, alpha - 4f * deltaTime);
            if (alpha <= 0) {
                alive = false;
            }
        }

        public ChangeAnimation init(float x, float y, int sp) {
            sprite = sp == -1 ? redSprite : greenSprite;
            this.x = x;
            this.y = y;
            size = 1.5f;
            alpha = 1f;
            alive = true;
            return this;
        }

        @Override
        public void reset() {
            x = 0;
            y = 0;
            size = 1f;
            alpha = 1f;
            sprite = null;
            alive = false;
        }
    }

    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
    }

    @Override
    public float getProgress() {
        int green = 0;
        for (int row = 0; row < gemRows; ++row) {
            for (int col = 0; col < gemColumns; ++col) {
                if (gemBoard[row][col] == 1) {
                    ++green;
                }
            }
        }
        return 100f * green / (float)maxGems;
    }

    @Override
    public boolean isFinished() {
        int green = 0;
        for (int row = 0; row < gemRows; ++row) {
            for (int col = 0; col < gemColumns; ++col) {
                if (gemBoard[row][col] == 1) {
                    ++green;
                }
            }
        }
        return (green == maxGems) || (Network.isHosting && timeLeftInSec < 0);
    }

    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        for (int row = 0; row < gemRows; ++row) {
            for (int col = 0; col < gemColumns; ++col) {
                Sprite sp = gemBoard[row][col] == -1 ? redSprite : greenSprite;
                Rectangle rect = gemRects[row][col];
                sp.setAlpha(1f);
                sp.setScale(1f);
                sp.setPosition(rect.x, rect.y);
                sp.draw(game.batch);
            }
        }
        for (ChangeAnimation anim : animations) {
            anim.sprite.setAlpha(anim.alpha);
            anim.sprite.setScale(anim.size);
            anim.sprite.setPosition(anim.x, anim.y);
            anim.sprite.draw(game.batch);
        }
        clockSprite.draw(game.batch);
        game.font.draw(game.batch, "Seconds left: " + (int)Math.ceil(timeLeftInSec),
                clockSprite.getX() + clockSprite.getWidth() + 24f, clockSprite.getY() + clockSprite.getHeight());
        game.batch.end();
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.justTouched()) {
            for (int row = 0; row < gemRows; ++row) {
                for (int col = 0; col < gemColumns; ++col) {
                    if (gemRects[row][col].contains(getTouchPos().x, getTouchPos().y)) {
                        gemBoard[row][col] *= -1;
                        animations.add(animationPool.obtain().init(gemRects[row][col].x,gemRects[row][col].y, gemBoard[row][col]));
                        turnAdjecent(row, col);
                    }
                }
            }
        }
        for (int i = animations.size - 1; i >= 0; --i) {
            animations.get(i).update(deltaTime);
            if (!animations.get(i).alive) {
                animationPool.free(animations.removeIndex(i));
            }
        }
        timeLeftInSec -= deltaTime;
    }

    private void turnAdjecent(int row, int col) {
        Rectangle rect;
        // left and right
        if (col > 0) {
            rect = gemRects[row][col -1];
            animations.add(animationPool.obtain().init(rect.x, rect.y, gemBoard[row][col - 1]));
            gemBoard[row][col - 1] *= -1;
        }
        if (col < gemColumns - 1) {
            rect = gemRects[row][col + 1];
            animations.add(animationPool.obtain().init(rect.x, rect.y, gemBoard[row][col + 1]));
            gemBoard[row][col + 1] *= -1;
        }
        // top and bottom
        if (row > 0) {
            rect = gemRects[row - 1][col];
            animations.add(animationPool.obtain().init(rect.x, rect.y, gemBoard[row - 1][col]));
            gemBoard[row - 1][col] *= -1;
        }
        if (row < gemRows - 1) {
            rect = gemRects[row + 1][col];
            animations.add(animationPool.obtain().init(rect.x, rect.y, gemBoard[row + 1][col]));
            gemBoard[row + 1][col] *= -1;
        }
        // diagonal, left
        if (col > 0) {
            if (row > 0) {
                rect = gemRects[row - 1][col - 1];
                animations.add(animationPool.obtain().init(rect.x, rect.y, gemBoard[row - 1][col - 1]));
                gemBoard[row - 1][col - 1] *= -1;
            }
            if (row < gemRows -1) {
                rect = gemRects[row + 1][col - 1];
                animations.add(animationPool.obtain().init(rect.x, rect.y, gemBoard[row + 1][col - 1]));
                gemBoard[row + 1][col - 1] *= -1;
            }
        }
        // diagonal, right
        if (col < gemColumns - 1) {
            if (row > 0) {
                rect = gemRects[row - 1][col + 1];
                animations.add(animationPool.obtain().init(rect.x, rect.y, gemBoard[row - 1][col + 1]));
                gemBoard[row - 1][col + 1] *= -1;
            }
            if (row < gemRows -1) {
                rect = gemRects[row + 1][col + 1];
                animations.add(animationPool.obtain().init(rect.x, rect.y, gemBoard[row + 1][col + 1]));
                gemBoard[row + 1][col + 1] *= -1;
            }
        }
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void dispose() {}
}
