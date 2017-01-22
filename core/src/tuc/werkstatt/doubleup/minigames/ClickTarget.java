/* TODO:
/ Bessere Notationen (Notationen sind teils veraltet vom anderen Minispiel), Komentare, Javadocs.
/ Sounds passabel? Lautstärke?
/ Lizensen bzgl Musik und Images hinzufügen als Kommentar
/ Teils geht Drücken auf Desktop nicht. Eventuell Kill funktion einbauen, die einen zähler +1 macht
/--> nur wenn dieser zähler = 0 ist, wird Bild kleiner.
/Ränder Passabel?
/Andere Spawnsachen hinzufügen, als Blockade (bewegend)? Ausprobieren die nächsten Tage (spätestens nach dem 30.Januar)
/counter 2 und ggf andere sinnlos sachen entfernen
/Sound fürs Treffen muss noch rein
/spielbar android gerät?
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

    private Sound hit;
    private Sprite backgroundSprite;

    private final int maxPoints = 50;
    private int currPoints = 0;
    private final int maxBall1 = 1;
    private int counter = 0;
    private int counter2 = 2;
    private final float ballSpawnMinDelay = 0.001f;
    private float currSpawnDelay = 0f;

    private Array<Ball> ball;

    private class Ball {
     private float size = 250;

        Sprite sprite;
        Vector2 vel = new Vector2();
        boolean alive = false;



        private Ball() {

            sprite = getSprite("minigames/ClickTarget/target");

            sprite.setOriginCenter();

        }

        private void spawn() {
            float x = MathUtils.random((0 - 256 / 2)+50, (game.width - 256 / 2)-50);
            float y = game.height - MathUtils.random((256),(6*256));

          sprite.setPosition(x, y);

            if (counter == 0)
            {
                sprite.setSize(size, size);
            }

            if (counter > 0)

            {

                    size = size -3 ;

                sprite.setSize(size, size);
                x = MathUtils.random(0 - 256 / 2 + (MathUtils.random(0,50)), game.width - 256 / 2+(MathUtils.random(-50,0)));
                y = game.height - MathUtils.random((1*256)+((MathUtils.random(-50,0))),(6*256)+(MathUtils.random(-50,50)));


            }



            alive = true;
        }

        private void kill() {
            alive = false;
        }



    }



    public ClickTarget(DoubleUp game) {
        super(game);

        backgroundSprite = getSprite("ui/title_background");
        backgroundSprite.setSize(game.width, game.height);
        backgroundSprite.setPosition(0, 0);

        ball = new Array<Ball>(maxBall1);
        for (int i = 0; i < maxBall1; ++i) {
            ball.add(new Ball());
        }

    }

    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
        hit = getSound("sounds/Bow_Fire.mp3");
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


        game.batch.end();
    }

    @Override
    public void update(float deltaTime) {
        Vector2 vel = new Vector2();
        if (Gdx.input.justTouched()) {
            Vector2 pos = getTouchPos();

            for (Ball b : ball) {
                if (!b.alive) {
                    continue;

                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);
                if (distance < b.sprite.getWidth() / 2) {

                    hit.play();
                    ++currPoints;

                 ++counter;
                    counter2 = counter2 + 1;
                    b.kill();

                }

          //      else {
            //        b.kill();
           //     }

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



        if((currSpawnDelay -= deltaTime) < 0) {
            currSpawnDelay = ballSpawnMinDelay;
            for (Ball b : ball) {
                if (!b.alive) {
                    b.spawn();
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
