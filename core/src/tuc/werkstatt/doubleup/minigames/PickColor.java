package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class PickColor extends MiniGame {
    private final int maxPoints = 10;
    private int currPoints = 0;
    private final Color[] colors = { materialRed, materialGreen, materialBlue, materialOrange };
    private final Color colorToCollect = colors[0];
    private Sprite backgroundSprite;
    private Sprite swirlSprite;
    private final float swirlSize = 256f;
    private final int numSwirlsPerColor = 6;
    private Sound plusSound;
    private Sound minusSound;
    private long lastSoundPlayed = System.currentTimeMillis();
    private Array<Swirl> swirls;

    private class Swirl {
        Vector2 pos;
        Vector2 vel;
        float rot;
        float rotSpeed;
        Color col;

        private Swirl(Color col) {
            this.col = col;
            this.pos = new Vector2();
            this.vel = new Vector2();
            spawn();
        }

        private void spawn() {
            final boolean horizontal = MathUtils.randomBoolean();
            if (horizontal) {
                final boolean top = MathUtils.randomBoolean();
                pos.x = MathUtils.random(-swirlSize, game.width);
                pos.y = top ? game.height : -swirlSize;
            } else {
                final boolean left = MathUtils.randomBoolean();
                pos.x = left ? -swirlSize : game.width;
                pos.y = MathUtils.random(-swirlSize, game.height);
            }
            vel.setToRandomDirection().setLength(MathUtils.random(game.height / 4f, game.height / 1.5f));
            rotSpeed = MathUtils.random(-360f, 360f);
        }
    }

    public PickColor(DoubleUp game) {
        super(game);
        backgroundSprite = getSprite("ui/title_background");
        backgroundSprite.setSize(game.width, game.height);
        backgroundSprite.setPosition(0, 0);
        swirlSprite = getSprite("minigames/PickColor/swirl");
        swirlSprite.setSize(swirlSize, swirlSize);
        swirlSprite.setOriginCenter();

        swirls = new Array<Swirl>(numSwirlsPerColor * colors.length);
        for (Color col : colors) {
            for (int i = 0; i < numSwirlsPerColor; ++i) {
                swirls.add(new Swirl(col));
            }
        }
    }

    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
        plusSound = getSound("sounds/swirl_plus.wav");
        minusSound = getSound("sounds/swirl_minus.wav");
    }

    @Override
    public float getProgress() { return 100f * currPoints / maxPoints; }

    @Override
    public boolean isFinished() { return currPoints >= maxPoints; }

    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        backgroundSprite.draw(game.batch);
        for (Swirl sw : swirls) {
            swirlSprite.setPosition(sw.pos.x, sw.pos.y);
            swirlSprite.setRotation(sw.rot);
            swirlSprite.setColor(sw.col);
            swirlSprite.draw(game.batch);
        }
        game.batch.end();
    }

    public void update(float deltaTime) {
        // add or remove points, when swirl was touched
        if (Gdx.input.justTouched()) {
            Vector2 touchPos = getTouchPos();
            for (Swirl sw : swirls) {
                final float distance = touchPos.dst(sw.pos.x + swirlSize / 2, sw.pos.y + swirlSize / 2);
                if (distance < swirlSize / 2) {
                    final long now = System.currentTimeMillis();
                    if (sw.col == colorToCollect) {
                        ++currPoints;
                        if (now - lastSoundPlayed > 250) {
                            plusSound.play(0.8f);
                            lastSoundPlayed = now;
                        }
                    } else {
                        currPoints = Math.max(0, currPoints - 1);
                        if (now - lastSoundPlayed > 250) {
                            minusSound.play(0.5f);
                            lastSoundPlayed = now;
                        }
                    }
                    sw.spawn();
                }
            }
        }

        // update swirls and check bounds
        for (Swirl sw : swirls) {
            sw.pos.add(sw.vel.x * deltaTime, sw.vel.y * deltaTime);
            sw.rot += sw.rotSpeed * deltaTime;
            if (sw.pos.x > game.width) { sw.pos.x = -swirlSize; }
            if (sw.pos.x < -swirlSize) { sw.pos.x = game.width; }
            if (sw.pos.y > game.height) { sw.pos.y = -swirlSize; }
            if (sw.pos.y < -swirlSize) { sw.pos.y = game.height; }
        }
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
