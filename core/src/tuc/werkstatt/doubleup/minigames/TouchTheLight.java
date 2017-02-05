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
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MaterialColors;
import tuc.werkstatt.doubleup.MiniGame;

public final class TouchTheLight extends MiniGame {

    private final int maxPoints = 20;
    private int currPoints = 0;
    private final int maxLight = 1;
    private final int maxCurse = 3;
    private final int maxVenom = 3;
    private final float lightSpawnMinDelay = 0.001f;
    private final float curseSpawnMinDelay = MathUtils.random(0f, 1f);
    private final float venomSpawnMinDelay = MathUtils.random(0f, 1f);
    private float lightSpawnDelay = 0f;
    private float curseSpawnDelay = 2f;
    private float venomSpawnDelay = 3f;

    private Array<Light> light;
    private Array<Curse> curse;
    private Array<Venom> venom;

    private class Light {
        static final float size = 256;
        Sprite sprite;
        boolean aliveLight = false;
        Vector2 vel = new Vector2();

        private Light() {

            sprite = getSprite("minigames/TouchTheLight/Light");
            sprite.setSize(size, size);
            sprite.setOriginCenter();

        }

        private void spawnLight() {
            float x = MathUtils.random(50,(game.width-size));
            float y = MathUtils.random(50,(game.height-size));

            sprite.setPosition(x, y);

            aliveLight = true;
        }

        private void killLight() {
            aliveLight = false;
        }

    }

    private class Curse {
        static final float size = 128;
        Vector2 vel = new Vector2();
        boolean aliveCurse = false;
        Sprite sprite;


        private Curse() {

            sprite = getSprite("minigames/TouchTheLight/Curse");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawnCurse() {
            float x = MathUtils.random(50,(game.width-size));
            float y = MathUtils.random(50,(game.height-size));

            sprite.setPosition(x, y);

            if (currPoints <= 5) {
                vel.set(0, -500);
            }
            if (currPoints > 5 && currPoints <= 10) {
                vel.set(0, -600);
            }
            if (currPoints > 10 && currPoints <=15) {
                vel.set(0, -700);
            }
            if (currPoints > 15 && currPoints <= 20) {
                vel.set(0, -800);
            }
            if (currPoints > 20 && currPoints < 25) {
                vel.set(0, -900);
            }


            aliveCurse = true;
        }

        private void killCurse() {
            aliveCurse = false;

        }


    }

    private class Venom {
        static final float size = 128;
        Vector2 vel = new Vector2();
        boolean aliveVenom = false;
        Sprite sprite;


        private Venom() {

            sprite = getSprite("minigames/TouchTheLight/Venom");
            sprite.setSize(size, size);
            sprite.setOriginCenter();
        }

        private void spawnVenom() {
            float x = MathUtils.random(50,(game.width-size));
            float y = MathUtils.random(50,(game.height-size));


            sprite.setPosition(x, y);

            if (currPoints <= 5) {
                vel.set(500, 0);
            }
            if (currPoints > 5 && currPoints <= 10) {
                vel.set(600, 0);
            }
            if (currPoints > 10 && currPoints <=15) {
                vel.set(700, 0);
            }
            if (currPoints > 15 && currPoints <= 20) {
                vel.set(800, 0);
            }
            if (currPoints > 20 && currPoints < 25) {
                vel.set(900, 0);
            }

            aliveVenom = true;
        }

        private void killVenom() {
            aliveVenom = false;

        }


    }


    public TouchTheLight (DoubleUp game) {
        super(game);
        setTitle("Touch The Light");
        final String redCol = MaterialColors.red.toString();
        setDescription("Touch the light but avoid the huge orbs or you will lose all your points");
        setBackground("ui/title_background");
        setIcon("minigames/TouchTheLight/Light");

        light = new Array<Light>(maxLight);
        for (int i = 0; i < maxLight; ++i) {
            light.add(new Light());
        }
        curse = new Array<Curse>(maxCurse);
        for (int j = 0; j < maxCurse; ++j) {
            curse.add(new Curse());
        }
        venom = new Array<Venom>(maxVenom);
        for (int j = 0; j < maxVenom; ++j) {
            venom.add(new Venom());
        }
    }

    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
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
        for (Light b : light) {
            if (b.aliveLight) {
                b.sprite.draw(game.batch);
                numActive++;
            }
        }

        for (Curse b : curse) {
            if (b.aliveCurse) {
                b.sprite.draw(game.batch);
                numActive++;
            }
        }

        for (Venom b : venom) {
            if (b.aliveVenom) {
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

            for (Light b : light) {
                if (!b.aliveLight) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);
                if (distance < b.sprite.getWidth() / 2) {


                    ++currPoints;
                    b.killLight();

                }
             }

            for (Curse b : curse) {
                if (!b.aliveCurse) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);


                if (distance < b.sprite.getWidth() / 2) {

                    if (currPoints > 0) {
                        currPoints = 0;
                    }
                    b.killCurse();
                }
             }

            for (Venom b : venom) {
                if (!b.aliveVenom) {
                    continue;
                }
                final float distance = Vector2.dst(pos.x, pos.y,
                        b.sprite.getX() + b.sprite.getWidth() / 2, b.sprite.getY() + b.sprite.getHeight() / 2);


                if (distance < b.sprite.getWidth() / 2) {

                    if (currPoints > 0) {
                        currPoints = 0;
                    }
                    b.killVenom();
                }
            }
        }

        for (Light b : light) {
            if (b.aliveLight) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight()) {
                    b.killLight();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        for (Curse b : curse) {
            if (b.aliveCurse) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight()) {
                    b.killCurse();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        for (Venom b : venom) {
            if (b.aliveVenom) {
                final float x = b.sprite.getX() + b.vel.x * deltaTime;
                final float y = b.sprite.getY() + b.vel.y * deltaTime;
                if (x < 0 - b.sprite.getWidth() || x > game.width || y < 0 - b.sprite.getHeight()) {
                    b.killVenom();
                } else {
                    b.sprite.setPosition(x, y);

                }
            }
        }

        if((lightSpawnDelay -= deltaTime) < 0) {
            lightSpawnDelay = lightSpawnMinDelay;
            for (Light b : light) {
                if (!b.aliveLight) {
                    b.spawnLight();
                    break;
                }
            }
        }
            if ((curseSpawnDelay -= deltaTime) < 0) {
                curseSpawnDelay = curseSpawnMinDelay;
                for (Curse b : curse) {
                    if (!b.aliveCurse) {
                        b.spawnCurse();
                        break;
                    }
                }
            }

        if ((venomSpawnDelay -= deltaTime) < 0) {
            venomSpawnDelay = venomSpawnMinDelay;
            for (Venom b : venom) {
                if (!b.aliveVenom) {
                    b.spawnVenom();
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
