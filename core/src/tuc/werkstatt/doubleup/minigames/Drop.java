package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class Drop extends MiniGame {

    private Sprite backgroundSprite;
    private Sprite dropSprite;
    private Sprite bucketSprite;

    private final int maxPoints = 20;
    private int currPoints = 0;
    private Sound dropSound;
    private Array<Vector2> raindrops;
    private long lastDropTime;

    public Drop(DoubleUp game) {
        super(game);

        backgroundSprite = getSprite("ui/title_background");
        backgroundSprite.setSize(game.width, game.height);
        backgroundSprite.setPosition(0, 0);

        // TODO: are these resources free to use in non-commercial projects and licenced accordingly?
        dropSprite = getSprite("minigames/Drop/droplet");
        dropSprite.setSize(64, 64);
        bucketSprite = getSprite("minigames/Drop/bucket");
        bucketSprite.setSize(100, 100);
        // bottom left corner of the bucket is 20 pixels above the bottom screen edge
        bucketSprite.setPosition((game.width - bucketSprite.getWidth()) / 2, 20);

        // create the raindrops array and spawn the first raindrop
        raindrops = new Array<Vector2>();
        spawnRaindrop();
    }

    private void spawnRaindrop() {
        Vector2 raindrop = new Vector2(MathUtils.random(0, game.width - bucketSprite.getWidth()), game.height);
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {
        // TODO: are these resources free to use in non-commercial projects and licenced accordingly?
        game.loadMusic("music/examples/Stan.mp3");
        dropSound = getSound("sounds/drop.wav");
    }

    @Override
    public float getProgress() { return 100f * currPoints / maxPoints; }

    @Override
    public boolean isFinished() { return currPoints >= maxPoints; }

    @Override
    public void draw(float deltaTime) {
        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        // begin a new batch and draw the bucket and
        // all drops
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        backgroundSprite.draw(game.batch);
        for(Vector2 raindrop: raindrops) {
            dropSprite.setPosition(raindrop.x, raindrop.y);
            dropSprite.draw(game.batch);
        }
        bucketSprite.draw(game.batch);
        game.batch.end();
    }

    public void update(float deltaTime) {
        // process user input
        if(Gdx.input.isTouched()) {
            bucketSprite.setX(getTouchPos().x - bucketSprite.getWidth() / 2);
        }

        if(Gdx.input.isKeyPressed(Keys.LEFT)) bucketSprite.translateX(-200 * deltaTime);
        if(Gdx.input.isKeyPressed(Keys.RIGHT)) bucketSprite.translateX(200 * deltaTime);

        // make sure the bucket stays within the screen bounds
        if(bucketSprite.getX() < 0) bucketSprite.setX(0);
        if(bucketSprite.getX() > game.width - bucketSprite.getWidth()) {
            bucketSprite.setX(game.width - bucketSprite.getWidth());
        }

        // check if we need to create a new raindrop
        if(TimeUtils.nanoTime() - lastDropTime > 200000000) spawnRaindrop();

        // move the raindrops, remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the later case we play back
        // a sound effect as well.
        Iterator<Vector2> iter = raindrops.iterator();
        while(iter.hasNext()) {
            Vector2 raindrop = iter.next();
            raindrop.y -= 1500 * deltaTime;
            if(raindrop.y + dropSprite.getHeight() < 0) {
                iter.remove();
                continue;
            }
            Rectangle.tmp.set(raindrop.x, raindrop.y, dropSprite.getWidth(), dropSprite.getHeight());
            if (bucketSprite.getBoundingRectangle().overlaps(Rectangle.tmp)) {
                dropSound.play();
                iter.remove();
                currPoints++;
            }
        }
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
