package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class PickColor extends MiniGame {
    private final int maxPoints = 10;
    private int currPoints = 0;
    private final int maxActiveSwirls = 30;
    private final float swirlSpawnMinDelay = 0.15f;
    private float currSpawnDelay = 0f;
    private final Color colorToCollect = Color.RED;
    private final Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};

    private Vector3 touchPos = new Vector3();
    private Texture swirlTex;
    private Array<Swirl> swirls;

    private class Swirl extends Sprite {
        static final float size = 256;
        Vector2 vel = new Vector2();
        float rotationSpeed;
        boolean alive = false;

        private Swirl() {
            super(swirlTex);
            setSize(size, size);
            setOriginCenter();
        }

        private void spawn() {
            float x = MathUtils.random(0 - size / 2, game.width - size / 2);
            float y = game.height + size;
            setColor(colors[MathUtils.random(colors.length - 1)]);
            setPosition(x, y);
            vel.set(0, -MathUtils.random(game.height / 4f, game.height / 1.5f)).rotate(MathUtils.random(-30f, 30f));
            setRotation(MathUtils.random(360f));
            rotationSpeed = MathUtils.random(90f, 360f);
            alive = true;
        }

        private void kill() {
            alive = false;
        }
    }

    public PickColor(DoubleUp game) {
        super(game);
    }

    @Override
    public void show() {
        swirlTex = new Texture(Gdx.files.internal("images/minigames/swirl.png"));
        swirls = new Array<Swirl>(maxActiveSwirls);
        for (int i = 0; i < maxActiveSwirls; ++i) {
            swirls.add(new Swirl());
        }
    }

    @Override
    public int getProgress() { return Math.max(0, currPoints); }

    @Override
    public boolean isFinished() { return currPoints >= maxPoints; }

    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();

        int numActive = 0;
        for (Swirl sw : swirls) {
            if (sw.alive) {
                sw.draw(game.batch);
                numActive++;
            }
        }
        game.font.setColor(Color.RED);
        game.font.draw(game.batch, "PickColor - Pick Red: " + getProgress() +
                "/" + maxPoints + ", #active: " + numActive, 10, game.font.getLineHeight());
        game.font.setColor(Color.WHITE);
        game.batch.end();
    }

    public void update(float deltaTime) {
        // add or remove points, when swirl was touched
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(touchPos);
            for (Swirl sw : swirls) {
                if (!sw.alive) {
                    continue;
                }
                final float distance = Vector2.dst(touchPos.x, touchPos.y,
                        sw.getX() + sw.getWidth() / 2, sw.getY() + sw.getHeight() / 2);
                if (distance < sw.getWidth() / 2) {
                    // getColor workaround, bug, e.g. Color.RED will result in ff0000fe instead of ff0000ff
                    if (sw.getColor().r == colorToCollect.r && sw.getColor().g == colorToCollect.g
                            && sw.getColor().b == colorToCollect.b) {
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
                final float x = sw.getX() + sw.vel.x * deltaTime;
                final float y = sw.getY() + sw.vel.y * deltaTime;
                if (x < 0 - sw.getWidth() || x > game.width || y < 0 - sw.getHeight()) {
                    sw.kill();
                } else {
                    sw.setPosition(x, y);
                    sw.rotate(sw.rotationSpeed * deltaTime);
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
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        swirlTex.dispose();
    }
}
