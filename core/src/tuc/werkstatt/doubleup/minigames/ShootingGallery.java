
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

// All Sprites / Textures, used for targets und decoartion from http://opengameart.org/content/shooting-gallery
// except for one: Reloading button: http://www.iconarchive.com/show/colorful-long-shadow-icons-by-graphicloads/Arrow-reload-icon.html
    private Sound hitTarget;
    //https://www.freesound.org/people/Neotone/sounds/75352/
    private Sound hitDuck;
    //https://www.freesound.org/people/Reitanna/sounds/242664/
    private Sound shot;
    //http://soundbible.com/1785-Skorpion.html
    private Sound reloading;
    //http://opengameart.org/content/voiceover-pack-40-lines
    private Sound noAmmo;
    //https://www.freesound.org/people/j1987/sounds/107806/


    // definition of maxAmounts, points, spawndelays, Sprites and Arrays.
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
    private Array<ColorTarget> colorTarget;
    private Array<RedTarget> redTarget;
    private Array<DuckBrown> brownDuck;
    private Array<DuckYellow> yellowDuck;


//Class for Sprite ColorTarget / Colored Targets.
    // setting picture, velocity (Vector2), size, spawn, kill, and alive, and x / y.
    private class ColorTarget {
        static final float size = 200;
        Sprite sprite;
        Vector2 vel = new Vector2();
        boolean aliveColor = false;



        private ColorTarget() {

            sprite = getSprite("minigames/ShootingGallery/target_colored");
            sprite.setSize(size, size);
            sprite.setOriginCenter();

        }

        private void spawnColor() {
            float x = 0;
            float y = 1050;

            sprite.setPosition(x, y);

            vel.set(500,0);

            aliveColor = true;
        }

        private void killColor() {
            aliveColor = false;
        }


    }

    //Class for Sprite RedTarget / Red Targets.
    // setting picture, velocity (Vector2), size, spawn, kill, and alive, and x / y.
    private class RedTarget {
        static final float size = 200;
        Vector2 vel = new Vector2();
        boolean aliveRed = false;
        Sprite sprite;


        private RedTarget() {

            sprite = getSprite("minigames/ShootingGallery/target_red2");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawnRed() {
            float x = 0;
            float y = 750;

            sprite.setPosition(x, y);

            vel.set(250,0);

            aliveRed = true;
        }

        private void killRed() {
            aliveRed = false;

        }

       }

    //Class for Sprite DuckBrown / Brown Ducks.
    // setting picture, velocity (Vector2), size, spawn, kill, and alive, and x / y.
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

    //Class for Sprite DuckYellow / Yellow Ducks.
    // setting picture, velocity (Vector2), size, spawn, kill, and alive, and x / y.
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
// Choosing the Pictures and define the size. Introduction.
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

           //Define the Arrays-add.

           colorTarget = new Array<ColorTarget>(maxTargetColor);
           for (int i = 0; i < maxTargetColor; ++i) {
               colorTarget.add(new ColorTarget());
           }
           redTarget = new Array<RedTarget>(maxTargetRed);
           for (int j = 0; j < maxTargetRed; ++j) {
               redTarget.add(new RedTarget());
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

    //Loading the Sound and Music.
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


    // Drawing everything.
    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();

        water.setPosition(0,0);
        water.draw(game.batch);


        for (ColorTarget b : colorTarget) {
            if (b.aliveColor) {
                b.sprite.draw(game.batch);

            }
        }

        for (RedTarget b : redTarget) {
            if (b.aliveRed) {
                b.sprite.draw(game.batch);

            }
        }

        for (DuckBrown b : brownDuck) {
            if (b.aliveBrownDuck) {
                b.sprite.draw(game.batch);

            }
        }

        for (DuckYellow b : yellowDuck) {
            if (b.aliveYellowDuck) {
                b.sprite.draw(game.batch);

            }
        }

        curtainStraight.setPosition(0,game.height-250);
        curtainStraight.draw(game.batch);
        curtainLeft.setPosition(0,0);
        curtainLeft.draw(game.batch);
        curtainRight.setPosition(game.width-100,0);
        curtainRight.draw(game.batch);

//Drawing multiplie Ammunation graphic.
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

    // Logic for Touch on Screen.
    @Override
    public void update(float deltaTime) {
        // only allow a shot (and therefore play shot sound) if Ammo > 0.
        if (Gdx.input.justTouched() && currentAmmo>0) {
            Vector2 pos = getTouchPos();

            for (ColorTarget b : colorTarget) {
                if (!b.aliveColor) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);
                if (distance < b.sprite.getWidth() / 2) {

                    hitTarget.play();
                    currPoints = currPoints + 1.5f;
                    b.killColor();

                }



                          }

            for (RedTarget b : redTarget) {
                if (!b.aliveRed) {
                    continue;
                }

                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);


                if (distance < b.sprite.getWidth() / 2) {

                    hitTarget.play();
                    currPoints = currPoints + 1f;
                    b.killRed();
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
// Shot counts only as a Shot if the reload-button was not hit!
            Vector2 posReload = getTouchPos();
            final float distance = Vector2.dst(posReload.x, posReload.y,
                    reload.getX() + reload.getWidth() / 2, reload.getY() + reload.getHeight() / 2);
            if ((distance < reload.getWidth() / 2) == false) {
                shot.play();
                currentAmmo = currentAmmo - 1;


            }

        }
// logic for reload-touch, only if currentAmmo<maxAmmo to avoid constant reloading sound.
        if (Gdx.input.justTouched()) {
            Vector2 posReload = getTouchPos();

            final float distance = Vector2.dst(posReload.x, posReload.y,
                    reload.getX() + reload.getWidth() / 2, reload.getY() + reload.getHeight() / 2);
            if ((distance < reload.getWidth() / 2) && currentAmmo < maxAmmo) {
                reloading.play();
                currentAmmo = maxAmmo;
            }
        }

// No Ammo = Sound, that there is no Ammo.
        if (Gdx.input.justTouched() && currentAmmo == 0) {
            noAmmo.play();
        }

        //Respawn Targets and Ducks. Also: Spawn and resolving the delay.

        for (ColorTarget b : colorTarget) {
            if (b.aliveColor) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight()|| y > game.height)  {
                    b.killColor();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        for (RedTarget b : redTarget) {
            if (b.aliveRed) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight() || y > game.height) {
                    b.killRed();
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
            for (ColorTarget b : colorTarget) {
                if (!b.aliveColor) {
                    b.spawnColor();
                    break;
                }
            }}
        if((currSpawnDelay2 -= deltaTime) < 0) {
            currSpawnDelay2 = birdieSpawnMinDelay;
            for (RedTarget b : redTarget) {
                if (!b.aliveRed) {
                    b.spawnRed();
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
