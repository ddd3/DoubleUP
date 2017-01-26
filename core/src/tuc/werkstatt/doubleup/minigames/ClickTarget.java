
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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class ClickTarget extends MiniGame {


    private Sound hitRight;
    private Sprite backgroundSprite;

    private final int maxPoints = 50;
    private int currPoints = 0;
    private final int maxBall1 = 1;
    private final int maxBall2 = 49;
    private final float ballSpawnMinDelay = 0.0001f;
    private final float birdieSpawnMinDelay = 0.25f;
    private float currSpawnDelay1 = 0f;
    private float currSpawnDelay2 = 0f;

    private Array<Ball> ball;
    private Array<Balld> balld;

    private class Ball {
        static final float size = 250;
        Sprite sprite;
        Vector2 vel = new Vector2();
        boolean alive = false;



        private Ball() {

            sprite = getSprite("minigames/ClickTarget/target");
            sprite.setSize(size, size);
            sprite.setOriginCenter();

        }

        private void spawn() {
            float x = MathUtils.random(0, game.width - (size));
            float y = game.height - MathUtils.random((1*size),(6*size));



            sprite.setPosition(x, y);

            if (currPoints >= 0 && currPoints <=20)
            {
                vel.set(MathUtils.random(MathUtils.random(0,200)),(MathUtils.random(-200,200)));
            }
            if (currPoints >= 21 && currPoints <=40)
            {
                vel.set(MathUtils.random(MathUtils.random(0,350)),(MathUtils.random(-350,350)));
            }
            if (currPoints >= 41 && currPoints < 50)
            {
                vel.set(MathUtils.random(MathUtils.random(0,500)),(MathUtils.random(-500,500)));
            }


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

            sprite = getSprite("minigames/ClickTarget/bird");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawn2() {
            float x = 0;
            float y = game.height - MathUtils.random((1*size),(6*size));


            sprite.setPosition(x, y);

            if (currPoints >= 0 && currPoints <=20)
            {
                vel.set(MathUtils.random(MathUtils.random(0,400)),(MathUtils.random(-400,400)));
            }
            if (currPoints >= 21 && currPoints <=40)
            {
                vel.set(MathUtils.random(MathUtils.random(0,550)),(MathUtils.random(-550,550)));
            }
            if (currPoints >= 41 && currPoints < 50)
            {
                vel.set(MathUtils.random(MathUtils.random(0,700)),(MathUtils.random(-700,700)));
            }


            alive2 = true;
        }

        private void kill2() {
            alive2 = false;

        }

        //    private void killFail2() {
        //      alive2 = false;
        //    }
    }


    public ClickTarget (DoubleUp game) {
        super(game);

        backgroundSprite = getSprite("ui/title_background");
        backgroundSprite.setSize(game.width, game.height);
        backgroundSprite.setPosition(0, 0);

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
        game.loadMusic("music/game_start_loop.ogg");
        hitRight= getSound("sounds/Bow_Fire.mp3");

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

        game.batch.end();
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.justTouched()) {
            Vector2 pos = getTouchPos();

            for (Ball b : ball) {
                if (!b.alive) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);
                if (distance < b.sprite.getWidth() / 2) {

                    hitRight.play();
                    ++currPoints;
                    b.kill();

                }
                else {


                    //    b.killFail();

                }



            }

            for (Balld b : balld) {
                if (!b.alive2) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);


                if (distance < b.sprite.getWidth() / 2) {

                    if (currPoints > 0) {
                        --currPoints;
                    }
                    b.kill2();
                }
                else {


                    //            b.killFail2();

                }




            }
        }

        for (Ball b : ball) {
            if (b.alive) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight()|| y > game.height)  {
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
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight() || y > game.height) {
                    b.kill2();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        if((currSpawnDelay1 -= deltaTime) < 0) {
            currSpawnDelay1 = ballSpawnMinDelay;
            for (Ball b : ball) {
                if (!b.alive) {
                    b.spawn();
                    break;
                }
            }}
        if((currSpawnDelay2 -= deltaTime) < 0) {
            currSpawnDelay2 = birdieSpawnMinDelay;
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
    public void dispose() {}
}
