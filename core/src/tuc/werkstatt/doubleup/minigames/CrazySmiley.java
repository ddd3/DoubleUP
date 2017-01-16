/* TODO:
/ Bessere Notationen (Notationen sind teils veraltet vom anderen Minispiel), Komentare, Javadocs.
/ Größe der Smileys in Ordnung?
/ Sounds passabel? Lautstärke?
/ Musik einbauen? gleiche Musik über alle Minispiele ohne Neustart?
/ Text Unten zu lang alternativ Beschreibung okay?!? SpawnDelay weiteranpassen?
/ Lizensen bzgl Musik und Images hinzufügen als Kommentar
*/

package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.audio.Sound;
import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class CrazySmiley extends MiniGame {

    private final int maxPoints = 5;
    private int currPoints = 0;
    private final int maxBall1 = 1;
    private final int maxBall2 = 9;
    private final float ballSpawnMinDelay = 0.001f;
    private float currSpawnDelay = 0f;

    private Array<Ball> ball;
    private Array<Balld> balld;

    private class Ball {
        static final float size = 256;
        Sprite sprite;
        Vector2 vel = new Vector2();
        boolean alive = false;



        private Ball() {

            sprite = getSprite("minigames/CrazySmiley/yellow");
            sprite.setSize(size, size);
            sprite.setOriginCenter();

        }

        private void spawn() {
            float x = MathUtils.random(0 - size / 2, game.width - size / 2);
            float y = game.height + size;


           sprite.setPosition(x, y);

            if (currPoints == 0) {
                vel.set(0, -(y / 5f));
            }

            if (currPoints > 0 && currPoints < maxPoints)
            {
                vel.set(0, -(y / 5f) * (currPoints + 1));}

            alive = true;
        }

        private void kill() {
            alive = false;
        }

        private void killFail() {
            alive = false;
        }

    }

    private class Balld {
        static final float size = 180;
        Vector2 vel = new Vector2();
        boolean alive2 = false;
        Sprite sprite;


        private Balld() {

            sprite = getSprite("minigames/CrazySmiley/red");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawn2() {
            float x = MathUtils.random(0 - size / 2, game.width - size / 2);
            float y = game.height + size;


            sprite.setPosition(x, y);

            if (currPoints == 0) {
                vel.set(0, -(y / 5f));
            }

            if (currPoints > 0 && currPoints < maxPoints)
            {
                vel.set(0, -(y / 5f) * (currPoints + 1));}

            alive2 = true;
        }

        private void kill2() {
            alive2 = false;

        }

        private void killFail2() {
            alive2 = false;
        }
    }


    public CrazySmiley (DoubleUp game) {

        super(game);

        ball = new Array<Ball>(maxBall1);
        for (int i = 0; i < maxBall1; ++i) {
            ball.add(new Ball());
        }
            balld = new Array<Balld>(maxBall2);
            for (int j = 0; j < maxBall2; ++j) {
                balld.add(new Balld());
            }

    }

    @Override
    public void show() {

    }

    @Override
    public float getProgress() { return 100f * currPoints / maxPoints; }

    @Override
    public boolean isFinished() { return currPoints >= maxPoints; }



    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();

        int numActive = 0;
        for (Ball b : ball) {
            if (b.alive) {
                b.sprite.draw(game.batch);
                numActive++;
            }
        }

        for (Balld b : balld) {
            if (b.alive2) {
                b.sprite.draw(game.batch);
                numActive++;
            }
        }
        game.font.setColor(Color.GREEN);
        game.font.draw(game.batch, "CrazySmiley - Only Yellow: " + currPoints + "/" + maxPoints +
                " (" + getProgress() + "%) " + ", #active: " + numActive, 10, game.font.getLineHeight());
        game.font.setColor(Color.WHITE);

        game.batch.end();
    }

    @Override
    public void update(float deltaTime) {
        Sound hitYellow = Gdx.audio.newSound(Gdx.files.internal("sounds/laughter.wav"));
        Sound hitRed = Gdx.audio.newSound(Gdx.files.internal("sounds/error.wav"));


        if (Gdx.input.justTouched()) {
            Vector2 pos = getTouchPos();

            for (Ball b : ball) {
                if (!b.alive) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);
                if (distance < b.sprite.getWidth() / 2) {

                    hitYellow.play(0.5f);
                    ++currPoints;
                    b.kill();

                }
                else {


                    b.killFail();

                }



            }

            for (Balld b : balld) {
                if (!b.alive2) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);


                if (distance < b.sprite.getWidth() / 2) {
                    hitRed.play(0.5f);
                    if (currPoints > 0) {
                        --currPoints;
                    }
                    b.kill2();
                }
                else {


                    b.killFail2();

                }




            }
        }

        for (Ball b : ball) {
            if (b.alive) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight()) {
                    b.kill();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        for (Balld b : balld) {
            if (b.alive2) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight()) {
                    b.kill2();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        if((currSpawnDelay -= deltaTime) < 0) {
            currSpawnDelay = ballSpawnMinDelay;
            for (Ball b : ball) {
                if (!b.alive) {
                    b.spawn();
                    break;
                }
            }

            for (Balld b : balld) {
                if (!b.alive2) {
                    b.spawn2();
                    break;
                }
            }
        }

    }


    @Override
    public void hide() {}

    @Override
    public void dispose() {    }
}
