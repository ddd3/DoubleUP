package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
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
    private final int maxActiveSwirls = 30;
    private final float swirlSpawnMinDelay = 0.15f;
    private float currSpawnDelay = 0f;
    private final Color[] colors = {Color.valueOf("ff6565ff"), Color.valueOf("6565ffff"),
            Color.valueOf("65ff65ff"), Color.valueOf("ffff65ff")};
    private final Color colorToCollect = colors[0];
    private Sprite backgroundSprite;

    private Array<Swirl> swirls;

    private class Swirl {
        static final float size = 256;
        Sprite sprite;
        Vector2 vel = new Vector2();
        float rotationSpeed;
        boolean alive = false;

        private Swirl() {
            sprite = getSprite("minigames/PickColor/swirl");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawn() {
            float x = MathUtils.random(0 - size / 2, game.width - size / 2);
            float y = game.height + size;
            sprite.setColor(colors[MathUtils.random(colors.length - 1)]);
            sprite.setPosition(x, y);
            vel.set(0, -MathUtils.random(game.height / 4f, game.height / 1.5f)).rotate(MathUtils.random(-30f, 30f));
            sprite.setRotation(MathUtils.random(360f));
            rotationSpeed = MathUtils.random(90f, 360f);
            alive = true;
        }

        private void kill() {
            alive = false;
        }
    }

    public PickColor(DoubleUp game) {
        super(game);
        backgroundSprite = getSprite("ui/title_background");
        backgroundSprite.setSize(game.width, game.height);
        backgroundSprite.setPosition(0, 0);

        swirls = new Array<Swirl>(maxActiveSwirls);
        for (int i = 0; i < maxActiveSwirls; ++i) {
            swirls.add(new Swirl());
        }
    }

    @Override
    public void show() {}

    @Override
    public float getProgress() { return 100f * currPoints / maxPoints; }

    @Override
    public boolean isFinished() { return currPoints >= maxPoints; }

    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        backgroundSprite.draw(game.batch);
        int numActive = 0;
        for (Swirl sw : swirls) {
            if (sw.alive) {
                sw.sprite.draw(game.batch);
                numActive++;
            }
        }
        game.batch.end();
    }

    public void update(float deltaTime) {
        // add or remove points, when swirl was touched
        if (Gdx.input.justTouched()) {
            Vector2 pos = getTouchPos();
            for (Swirl sw : swirls) {
                if (!sw.alive) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y, sw.sprite.getX() +
                        sw.sprite.getWidth() / 2, sw.sprite.getY() + sw.sprite.getHeight() / 2);
                if (distance < sw.sprite.getWidth() / 2) {
                    // getColor workaround, bug, e.g. Color.RED will result in ff0000fe instead of ff0000ff
                    if (sw.sprite.getColor().r == colorToCollect.r && sw.sprite.getColor().g == colorToCollect.g
                            && sw.sprite.getColor().b == colorToCollect.b) {
                        ++currPoints;
                    } else {
                        currPoints = Math.max(0, currPoints - 1);
                    }
                    sw.kill();
                }
            }
        }

        // move active swirls, kill out-of-bounds swirls
        for (Swirl sw : swirls) {
            if (sw.alive) {
                final float x = sw.sprite.getX() + sw.vel.x * deltaTime;
                final float y = sw.sprite.getY() + sw.vel.y * deltaTime;
                if (x < 0 - sw.sprite.getWidth() || x > game.width || y < 0 - sw.sprite.getHeight()) {
                    sw.kill();
                } else {
                    sw.sprite.setPosition(x, y);
                    sw.sprite.rotate(sw.rotationSpeed * deltaTime);
                }
            }
        }

        // spawn new swirls
        if((currSpawnDelay -= deltaTime) < 0) {
            currSpawnDelay = swirlSpawnMinDelay;
            for (Swirl sw : swirls) {
                if (!sw.alive) {
                    sw.spawn();
                    break;
                }
            }
        }
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
