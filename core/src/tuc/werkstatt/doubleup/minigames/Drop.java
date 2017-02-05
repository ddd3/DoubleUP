package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class Drop extends MiniGame {
    private Array<Sprite> iceSprites;
    private Array<Sprite> fruitSprites;
    private Sprite crateSprite;
    private Rectangle crateRect;
    private Array<Item> items;
    private final int maxPoints = 12;
    private int currPoints = 0;
    private long lastDropTime;
    private final float itemSize = 192;
    private final float crateSize = 256;
    private Sound plusSound, minusSound;

    private class Item {
        Sprite sp;
        float x, y, vy, rot, rotSpeed;
        boolean alive, isFruit;

        Item() {
            spawn();
        }

        void spawn() {
            isFruit = MathUtils.randomBoolean(0.39f);
            sp = isFruit ? fruitSprites.random() : iceSprites.random();
            x = MathUtils.random(0, game.width - sp.getWidth());
            y = game.height;
            vy = MathUtils.random(-game.height * 0.4f, -game.height * 0.82f);
            rot = MathUtils.random(360f);
            rotSpeed = MathUtils.random(-120f, 120f);
            alive = true;
        }

        void updateAndDraw(float deltaTime) {
            if (!alive) { return; }
            y += vy * deltaTime;
            rot += rotSpeed * deltaTime;
            if (y < -sp.getHeight()) {
                alive = false;
                return;
            }
            sp.setRotation(rot);
            sp.setPosition(x, y);
            if (sp.getBoundingRectangle().overlaps(crateRect)) {
                if (isFruit) {
                    currPoints++;
                    plusSound.play(0.7f);
                } else {
                    currPoints = Math.max(0, currPoints - 2);
                    minusSound.play(0.5f);
                }
                alive = false;
                return;
            }
            sp.draw(game.batch);
        }
    }

    public Drop(DoubleUp game) {
        super(game);
        setTitle("Catch Fruits");
        setDescription("Catch falling fruits to earn points, but avoid the ice pops");
        setBackground("ui/title_background");
        setIcon("minigames/Drop/fruit3");

        final int numItems = 5;
        iceSprites = new Array<Sprite>(true, numItems);
        fruitSprites = new Array<Sprite>(true, numItems);
        for (int i = 1; i <= numItems; ++i) {
            Sprite sp = getSprite("minigames/Drop/ice" + i);
            final float sizeFactor = itemSize / Math.max(sp.getWidth(), sp.getHeight());
            sp.setSize(sp.getWidth() * sizeFactor, sp.getHeight() * sizeFactor);
            sp.setOriginCenter();
            iceSprites.add(sp);
        }
        for (int i = 1; i <= numItems; ++i) {
            Sprite sp = getSprite("minigames/Drop/fruit" + i);
            final float sizeFactor = itemSize / Math.max(sp.getWidth(), sp.getHeight());
            sp.setSize(sp.getWidth() * sizeFactor, sp.getHeight() * sizeFactor);
            sp.setOriginCenter();
            fruitSprites.add(sp);
        }
        crateSprite = getSprite("minigames/Drop/crate");
        crateSprite.setSize(crateSize, crateSize);
        crateSprite.setPosition((game.width - crateSize) / 2f, 20f);
        crateRect = new Rectangle((game.width - crateSize * 0.75f) / 2f, 20f, crateSize * 0.75f, crateSize / 2f);

        items = new Array<Item>(false, 14);
        lastDropTime = TimeUtils.millis();
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
        for (Item i : items) {
            i.updateAndDraw(deltaTime);
        }
        crateSprite.draw(game.batch);
        game.batch.end();
    }

    public void update(float deltaTime) {
        if (Gdx.input.isTouched()) {
            Vector2 pos = getTouchPos();
            pos.x = MathUtils.clamp(pos.x, crateSize / 2f, game.width - crateSize / 2f);
            crateSprite.setX(pos.x - crateSprite.getWidth() / 2f);
            crateRect.setX(pos.x - crateRect.getWidth() / 2f);
        }
        // spawn new items
        if (TimeUtils.timeSinceMillis(lastDropTime) > 300) {
            Item item = null;
            for (Item i : items) {
                if (i.alive) {
                    continue;
                } else {
                    item = i;
                    break;
                }
            }
            if (item == null) {
                item = new Item();
                items.add(item);
            } else {
                item.spawn();
            }
            lastDropTime = TimeUtils.millis();
        }
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void dispose() {}
}
