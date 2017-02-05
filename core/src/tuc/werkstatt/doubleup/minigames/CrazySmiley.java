
package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.sun.org.apache.xerces.internal.impl.dv.xs.YearDV;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MaterialColors;
import tuc.werkstatt.doubleup.MiniGame;

public final class CrazySmiley extends MiniGame {

    private Sound hitYellow;
    // https://www.freesound.org/people/Robinhood76/sounds/108251/
    private Sound hitRed;
    // https://www.freesound.org/people/fins/sounds/171497/

    // definition of maxAmounts, points, spawndelays, Arrays.
    private final int maxPoints = 7;
    private int currPoints = 0;
    private final int maxYellow = 1;
    private final int maxRed = 9;
    private final float SpawnMinDelay = 0.001f;
    private float currSpawnDelay = 0f;

    private Array<YellowSmiley> yellow;
    private Array<RedSmiley> red;


    //Class for Sprite YellowSmiley .
    // setting picture, velocity (Vector2), size, spawn, kill, and alive, and x / y.
    private class YellowSmiley {
        static final float size = 256;
        Sprite sprite;
        Vector2 vel = new Vector2();
        boolean aliveYellow = false;



        private YellowSmiley() {

            sprite = getSprite("minigames/CrazySmiley/yellow");
            sprite.setSize(size, size);
            sprite.setOriginCenter();

        }

        private void spawnYellow() {
            float x = MathUtils.random(0 - size / 2, game.width - size / 2);
            float y = game.height + size;


           sprite.setPosition(x, y);

            if (currPoints == 0) {
                vel.set(0, -(y / 5f));
            }

            if (currPoints > 0 && currPoints < maxPoints)
            {
                vel.set(0, -(y / 5f) * (currPoints + 1));}

            aliveYellow = true;
        }

        private void killYellow() {
            aliveYellow = false;
        }

        private void killFailYellow() {
            aliveYellow = false;
        }

    }

    //Class for Sprite RedSmiley .
    // setting picture, velocity (Vector2), size, spawn, kill, and alive, and x / y.
    private class RedSmiley {
        static final float size = 180;
        Vector2 vel = new Vector2();
        boolean aliveRed = false;
        Sprite sprite;


        private RedSmiley() {

            sprite = getSprite("minigames/CrazySmiley/red");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawnRed() {
            float x = MathUtils.random(0 - size / 2, game.width - size / 2);
            float y = game.height + size;


            sprite.setPosition(x, y);

            if (currPoints == 0) {
                vel.set(0, -(y / 5f));
            }

            if (currPoints > 0 && currPoints < maxPoints)
            {
                vel.set(0, -(y / 5f) * (currPoints + 1));}

            aliveRed = true;
        }

        private void killRed() {
            aliveRed = false;

        }

        private void killFailRed() {
            aliveRed = false;
        }
    }

    // Choosing the Pictures and define the size. Introduction.
    public CrazySmiley (DoubleUp game) {
        super(game);
        setTitle("Crazy Smiley");
        final String redCol = MaterialColors.red.toString();
        setDescription("Touch the [#ECC81B]yellow[] smiley, but avoid [#" + redCol + "]red[] ones or you will lose points");
        setBackground("ui/title_background");
        setIcon("minigames/CrazySmiley/yellow");

        //Define the Arrays-add.

        yellow = new Array<YellowSmiley>(maxYellow);
        for (int i = 0; i < maxYellow; ++i) {
            yellow.add(new YellowSmiley());
        }
            red = new Array<RedSmiley>(maxRed);
            for (int j = 0; j < maxRed; ++j) {
                red.add(new RedSmiley());
            }
    }

    //Loading the Sound and Music.
    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
        hitYellow = getSound("sounds/laughter.wav");
        hitRed = getSound("sounds/error.wav");
    }

    @Override
    public float getProgress() { return 100f * currPoints / maxPoints; }

    @Override
    public boolean isFinished() { return currPoints >= maxPoints; }

    // Drawing everything.
    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();


        for (YellowSmiley b : yellow) {
            if (b.aliveYellow) {
                b.sprite.draw(game.batch);

            }
        }

        for (RedSmiley b : red) {
            if (b.aliveRed) {
                b.sprite.draw(game.batch);

            }
        }

        game.batch.end();
    }

    // Logic for Touch on Screen.
    @Override
    public void update(float deltaTime) {
        if (Gdx.input.justTouched()) {
            Vector2 pos = getTouchPos();

            for (YellowSmiley b : yellow) {
                if (!b.aliveYellow) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);
                if (distance < b.sprite.getWidth() / 2) {

                    hitYellow.play(0.65f);
                    ++currPoints;
                    b.killYellow();

                }
                else {


                    b.killFailYellow();

                }



            }

            for (RedSmiley b : red) {
                if (!b.aliveRed) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);


                if (distance < b.sprite.getWidth() / 2) {
                    hitRed.play(0.65f);
                    if (currPoints > 0) {
                        --currPoints;
                    }
                    b.killRed();
                }
                else {


                    b.killFailRed();

                }




            }
        }


        //Respawn Smileys. Also: Spawn and resolving the delay.
        for (YellowSmiley b : yellow) {
            if (b.aliveYellow) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight()) {
                    b.killYellow();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        for (RedSmiley b : red) {
            if (b.aliveRed) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight()) {
                    b.killRed();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        if((currSpawnDelay -= deltaTime) < 0) {
            currSpawnDelay = SpawnMinDelay;
            for (YellowSmiley b : yellow) {
                if (!b.aliveYellow) {
                    b.spawnYellow();
                    break;
                }
            }

            for (RedSmiley b : red) {
                if (!b.aliveRed) {
                    b.spawnRed();
                    break;
                }
            }
        }

    }


    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void dispose() {}
}
