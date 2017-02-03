package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class PumpBalloon extends MiniGame {
    private Sprite balloonSprite;
    private final float initialHeight = game.height / 9f;
    private Sound pumpSound;
    //private Sound popSound;
    private long lastSoundPlayed = System.currentTimeMillis();
    private float balloonPulsing = 0f;
    private float balloonRotating = 0f;
    private final float balloonAspectRatio;

    public PumpBalloon(DoubleUp game) {
        super(game);
        setTitle("Pump Balloon");
        setDescription("Touch the screen repeatedly to pump the balloon until it pops");
        setBackground("ui/title_background");
        setIcon("minigames/PumpBalloon/balloon");

        balloonSprite = getSprite("minigames/PumpBalloon/balloon");
        balloonAspectRatio = balloonSprite.getWidth() / balloonSprite.getHeight();
        balloonSprite.setSize(initialHeight * balloonAspectRatio, initialHeight);
        balloonSprite.setPosition((game.width - balloonSprite.getWidth()) / 2,
                (game.height - balloonSprite.getHeight()) / 2);
        balloonSprite.setOriginCenter();
    }

    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
        pumpSound = getSound("sounds/pump.wav");
        //popSound = getSound("sounds/pop.wav");
    }

    @Override
    public float getProgress() {
        return 100f * (balloonSprite.getHeight() - initialHeight) / (game.height - initialHeight);
    }

    @Override
    public boolean isFinished() { return balloonSprite.getHeight() >= game.height; }

    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();

        final float rot = 7f * getProgress() / 100f * MathUtils.sinDeg(balloonRotating);
        balloonSprite.setRotation(rot);
        balloonSprite.setColor(1f, 1f, 1f, 1f);
        balloonSprite.draw(game.batch);

        final float currPulse = (1f + MathUtils.sinDeg(balloonPulsing)) / 2f;
        balloonSprite.setColor(1f, 1f - currPulse, 1f - currPulse, 0.5f * getProgress() / 100f);
        balloonSprite.draw(game.batch);

        game.batch.end();
    }

    @Override
    public void update(float deltaTime) {
        final float shrinkage = (game.height - initialHeight) / 20f * deltaTime;
        float nextWidth = Math.max(initialHeight * balloonAspectRatio, balloonSprite.getWidth() - shrinkage);
        float nextHeight = Math.max(initialHeight, balloonSprite.getHeight() - shrinkage);
        adjustBalloonSize(nextWidth, nextHeight);

        if (Gdx.input.justTouched()) {
            final float additionalHeight = (game.height - initialHeight) / 24f;
            nextWidth = balloonSprite.getWidth() + additionalHeight;
            nextHeight = balloonSprite.getHeight() + additionalHeight;
            adjustBalloonSize(nextWidth, nextHeight);
            final long now = System.currentTimeMillis();
            if (now - lastSoundPlayed > 210) {
                pumpSound.play(0.85f, 1f + 1f * getProgress() / 100f, 0f);
                lastSoundPlayed = now;
            }
        }
        balloonPulsing += 180f * 6f * getProgress() / 100f * deltaTime;
        balloonRotating +=  180f * 9f * getProgress() / 100f * deltaTime;

        /*
        if (isFinished()) {
            popSound.play();
        }
        */
    }

    private void adjustBalloonSize(float width, float height) {
        balloonSprite.setSize(width, height);
        balloonSprite.setPosition((game.width - balloonSprite.getWidth()) / 2,
                (game.height - balloonSprite.getHeight()) / 2);
        balloonSprite.setOriginCenter();
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void dispose() {}
}
