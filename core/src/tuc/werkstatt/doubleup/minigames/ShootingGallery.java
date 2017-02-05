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

public final class ShootingGallery extends MiniGame {


    private Sound hitTarget;
    private Sound hitDuck;
    private Sound shot;
    private Sound reloading;
    private Sound noAmmo;

    private final float maxPoints = 50;
    private float currPoints = 0;
    private final int maxTargetColor = 3;
    private final int maxTargetRed = 5;
    private final int maxBrownDuck = 2;
    private final int maxYellowDuck = 1;
    private final float ballSpawnMinDelay = MathUtils.random(0.40f , 0.80f);
    private final float birdieSpawnMinDelay = MathUtils.random(0.80f , 1.60f);
    private final float duckBrownSpawnMinDelay = MathUtils.random(2.5f , 5f);
    private final float duckYellowSpawnMinDelay = MathUtils.random(5f , 10f);
    private final int maxAmmo = 6;
    private int currentAmmo = maxAmmo;
    private float currSpawnDelay1 = 0f;
    private float currSpawnDelay2 = 0f;
    private float currSpawnDelay3 = 0f;
    private float currSpawnDelay4 = 0f;
    private Sprite bullet;
    private Sprite bulletEmpty;
    private Sprite curtainRight;
    private Sprite curtainLeft;
    private Sprite water;
    private Sprite reload;
    private Sprite curtainStraight;
    private Array<Ball> ball;
    private Array<Balld> balld;
    private Array<DuckBrown> brownDuck;
    private Array<DuckYellow> yellowDuck;



    private class Ball {
        static final float size = 200;
        Sprite sprite;
        Vector2 vel = new Vector2();
        boolean alive = false;



        private Ball() {

            sprite = getSprite("minigames/ShootingGallery/target_colored");
            sprite.setSize(size, size);
            sprite.setOriginCenter();

        }

        private void spawn() {
            float x = 0;
            float y = 1050;

            sprite.setPosition(x, y);

            vel.set(500,0);

            alive = true;
        }

        private void kill() {
            alive = false;
        }


    }

    private class Balld {
        static final float size = 200;
        Vector2 vel = new Vector2();
        boolean alive2 = false;
        Sprite sprite;


        private Balld() {

            sprite = getSprite("minigames/ShootingGallery/target_red2");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawn2() {
            float x = 0;
            float y = 750;

            sprite.setPosition(x, y);

            vel.set(250,0);

            alive2 = true;
        }

        private void kill2() {
            alive2 = false;

        }

       }

    private class DuckBrown {
        static final float size = 150;
        boolean aliveBrownDuck = false;
        Sprite sprite;
        Vector2 vel = new Vector2();

        private DuckBrown() {

            sprite = getSprite("minigames/ShootingGallery/duck_target_brown");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawnDuckBrown() {
            float x = 0;
            float y = 300;

            sprite.setPosition(x, y);
            vel.set(450,0);
            aliveBrownDuck = true;
        }

        private void killDuckBrown() {
            aliveBrownDuck = false;

        }
    }


    private class DuckYellow {
        static final float size = 150;
        boolean aliveYellowDuck = false;
        Sprite sprite;
        Vector2 vel = new Vector2();

        private DuckYellow() {

            sprite = getSprite("minigames/ShootingGallery/duck_target_yellow");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawnDuckYellow() {
            float x = 0;
            float y = 500;

            sprite.setPosition(x, y);
            vel.set(900,0);
            aliveYellowDuck = true;
        }

        private void killDuckYellow() {
            aliveYellowDuck = false;

        }
    }

       public ShootingGallery(DoubleUp game) {
           super(game);
           setTitle("Shooting Gallery");
           setDescription("Touch Targets and Reload! " +
                   "Scores: Yellow: 5, Brown Duck: 3, Colored Target: 1.5 and Red Target: 1");

           setBackground("ui/title_background");
           setIcon("minigames/ShootingGallery/duck_target_yellow");

           curtainStraight = getSprite("minigames/ShootingGallery/curtain_straight");
           curtainStraight.setSize(game.width, 250);
           curtainStraight.setOriginCenter();
           curtainLeft = getSprite("minigames/ShootingGallery/curtain");
           curtainLeft.setSize(100, game.height);
           curtainLeft.setOriginCenter();
           curtainRight = getSprite("minigames/ShootingGallery/curtain_right");
           curtainRight.setSize(100, game.height);
           curtainRight.setOriginCenter();
           water = getSprite("minigames/ShootingGallery/water2");
           water.setSize(game.width, 700);
           water.setOriginCenter();
           bullet = getSprite("minigames/ShootingGallery/gold_bullet");
           bullet.setSize(41, 75);
           bulletEmpty = getSprite("minigames/ShootingGallery/gold_bullet_empty");
           bulletEmpty.setSize(bullet.getWidth(), bullet.getHeight());
           bullet.setOriginCenter();
           bulletEmpty.setOriginCenter();
           reload = getSprite("minigames/ShootingGallery/reload");
           reload.setSize(200, 200);
           reload.setOriginCenter();

           ball = new Array<Ball>(maxTargetColor);
           for (int i = 0; i < maxTargetColor; ++i) {
               ball.add(new Ball());
           }
           balld = new Array<Balld>(maxTargetRed);
           for (int j = 0; j < maxTargetRed; ++j) {
               balld.add(new Balld());
           }

           brownDuck = new Array<DuckBrown>(maxBrownDuck);
           for (int k = 0; k < maxBrownDuck; ++k) {
               brownDuck.add(new DuckBrown());
           }

           yellowDuck = new Array<DuckYellow>(maxYellowDuck);
           for (int l = 0; l < maxYellowDuck; ++l) {
               yellowDuck.add(new DuckYellow());
           }
       }

    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
        hitTarget= getSound("sounds/metall.wav");
        hitDuck= getSound("sounds/quack.wav");
        reloading= getSound("sounds/war_reloading.ogg");
        noAmmo= getSound("sounds/metallicclick.wav");
        shot= getSound("sounds/Skorpion.mp3");


    }

    @Override
    public float getProgress() { return 100f * currPoints / maxPoints; }

    @Override
    public boolean isFinished() { return currPoints >= maxPoints; }

    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();

        water.setPosition(0,0);
        water.draw(game.batch);

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

        for (DuckBrown b : brownDuck) {
            if (b.aliveBrownDuck) {
                b.sprite.draw(game.batch);
                numActive++;
            }
        }

        for (DuckYellow b : yellowDuck) {
            if (b.aliveYellowDuck) {
                b.sprite.draw(game.batch);
                numActive++;
            }
        }

        curtainStraight.setPosition(0,game.height-250);
        curtainStraight.draw(game.batch);
        curtainLeft.setPosition(0,0);
        curtainLeft.draw(game.batch);
        curtainRight.setPosition(game.width-100,0);
        curtainRight.draw(game.batch);

        final float startX = 100f;
        final float bulletSpacing = 10f;
        for (int i = 1; i <= maxAmmo; ++i) {
            Sprite sp;
            if (i <= currentAmmo) {
                sp = bullet;
            } else {
                sp = bulletEmpty;
            }
            final float x = startX + i * (sp.getWidth() + bulletSpacing);
            sp.setPosition(x, 50);
            sp.draw(game.batch);
        }

        reload.setPosition(900,25);
        reload.draw(game.batch);
        game.batch.end();
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.justTouched() && currentAmmo>0) {
            Vector2 pos = getTouchPos();

            for (Ball b : ball) {
                if (!b.alive) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);
                if (distance < b.sprite.getWidth() / 2) {

                    hitTarget.play();
                    currPoints = currPoints + 1.5f;
                    b.kill();

                }



                          }

            for (Balld b : balld) {
                if (!b.alive2) {
                    continue;
                }

                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);


                if (distance < b.sprite.getWidth() / 2) {

                    hitTarget.play();
                    currPoints = currPoints + 1f;
                    b.kill2();
                }





            }

            for (DuckBrown b : brownDuck) {
                if (!b.aliveBrownDuck) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);


                if (distance < b.sprite.getWidth() / 2) {
                    hitDuck.play();
                    currPoints = currPoints + 3f;
                    b.killDuckBrown();
                }

            }

            for (DuckYellow b : yellowDuck) {
                if (!b.aliveYellowDuck) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);


                if (distance < b.sprite.getWidth() / 2) {
                    hitDuck.play();
                    currPoints = currPoints + 5f;
                    b.killDuckYellow();
                }

            }

            Vector2 posReload = getTouchPos();
            final float distance = Vector2.dst(posReload.x, posReload.y,
                    reload.getX() + reload.getWidth() / 2, reload.getY() + reload.getHeight() / 2);
            if ((distance < reload.getWidth() / 2) == false) {
                shot.play();
                currentAmmo = currentAmmo - 1;


            }

        }

        if (Gdx.input.justTouched()) {
            Vector2 posReload = getTouchPos();

            final float distance = Vector2.dst(posReload.x, posReload.y,
                    reload.getX() + reload.getWidth() / 2, reload.getY() + reload.getHeight() / 2);
            if ((distance < reload.getWidth() / 2) && currentAmmo < maxAmmo) {
                reloading.play();
                currentAmmo = maxAmmo;
            }
        }

        if (Gdx.input.justTouched() && currentAmmo == 0) {
            noAmmo.play();
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

        for (DuckBrown b : brownDuck) {
            if (b.aliveBrownDuck) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width-150 || y < 0 - b.sprite.getHeight() || y > game.height) {
                    b.killDuckBrown();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        for (DuckYellow b : yellowDuck) {
            if (b.aliveYellowDuck) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width-150 || y < 0 - b.sprite.getHeight() || y > game.height) {
                    b.killDuckYellow();
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

        if((currSpawnDelay3 -= deltaTime) < 0) {
            currSpawnDelay3 = duckBrownSpawnMinDelay;
            for (DuckBrown b : brownDuck) {
                if (!b.aliveBrownDuck) {
                    b.spawnDuckBrown();
                    break;
                }
            }
        }

        if((currSpawnDelay4 -= deltaTime) < 0) {
            currSpawnDelay4 = duckYellowSpawnMinDelay;
            for (DuckYellow b : yellowDuck) {
                if (!b.aliveYellowDuck) {
                    b.spawnDuckYellow();
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
