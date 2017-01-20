package tuc.werkstatt.doubleup.minigames;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class Drop extends MiniGame {

    private Sprite backgroundSprite;
    private final int maxPoints = 20;
    private int currPoints = 0;
    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private SpriteBatch batch;
    private Rectangle bucket;
    private Array<Rectangle> raindrops;
    private long lastDropTime;


    public Drop(DoubleUp game) {
        super(game);

        // load the images for the droplet and the bucket, 64x64 pixels each
        dropImage = new Texture(Gdx.files.internal("images/minigames/Drop/droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("images/minigames/Drop/bucket.png"));

        // load the drop sound effect
        dropSound = Gdx.audio.newSound(Gdx.files.internal("sounds/drop.wav"));

        // create the SpriteBatch
        batch = new SpriteBatch();

        // create a Rectangle to logically represent the bucket
        bucket = new Rectangle();
        bucket.x = game.width / 2 - 64 / 2; // center the bucket horizontally
        bucket.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
        bucket.width = 64;
        bucket.height = 64;

        // create the raindrops array and spawn the first raindrop
        raindrops = new Array<Rectangle>();
        spawnRaindrop();
        }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, game.width-64);
        raindrop.y = game.height;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {game.loadMusic("music/examples/Stan.mp3");
    }

    @Override
    public float getProgress() { return 100f * currPoints / maxPoints; }

    @Override
    public boolean isFinished() { return currPoints >= maxPoints; }

    @Override
    public void draw(float deltaTime) {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        batch.setProjectionMatrix(game.camera.combined);

        // begin a new batch and draw the bucket and
        // all drops
        batch.begin();
        batch.draw(bucketImage, bucket.x, bucket.y);
        for(Rectangle raindrop: raindrops) {
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        batch.end();
    }

    public void update(float deltaTime) {
        // process user input
        if(Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }
        if(Gdx.input.isKeyPressed(Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

        // make sure the bucket stays within the screen bounds
        if(bucket.x < 0) bucket.x = 0;
        if(bucket.x > game.width - 64) bucket.x = game.width - 64;

        // check if we need to create a new raindrop
        if(TimeUtils.nanoTime() - lastDropTime > 200000000) spawnRaindrop();

        // move the raindrops, remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the later case we play back
        // a sound effect as well.
        Iterator<Rectangle> iter = raindrops.iterator();
        while(iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 1000 * Gdx.graphics.getDeltaTime();
            if(raindrop.y + 64 < 0) iter.remove();
            if(raindrop.overlaps(bucket)) {
                dropSound.play();
                iter.remove();
                currPoints++;
            }
        }
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        // dispose of all the native resources
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        batch.dispose();
    }

}
