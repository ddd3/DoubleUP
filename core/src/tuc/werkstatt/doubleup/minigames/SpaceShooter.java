package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class SpaceShooter extends MiniGame {
    private Sprite shipSprite, laserSprite, laserHitSprite, enemy1Sprite, enemy2Sprite, enemy3Sprite,
            smokeASprite, smokeBSprite, smokeCSprite, missileSprite, bombSprite;

    private Sound explosionSound, failSound, laserSound;

    private Array<Enemy> enemies = new Array<Enemy>();
    private Pool<Enemy> enemyPool = new Pool<Enemy>() {
        @Override
        protected Enemy newObject() {
            return new Enemy();
        }
    };

    private Array<Laser> lasers = new Array<Laser>();
    private Pool<Laser> laserPool = new Pool<Laser>() {
        @Override
        protected Laser newObject() {
            return new Laser();
        }
    };

    private Array<Missile> missiles = new Array<Missile>();
    private Pool<Missile> missilePool = new Pool<Missile>() {
        @Override
        protected Missile newObject() {
            return new Missile();
        }
    };

    private Array<Bomb> bombs = new Array<Bomb>();
    private Pool<Bomb> bombPool = new Pool<Bomb>() {
        @Override
        protected Bomb newObject() {
            return new Bomb();
        }
    };

    private Array<Smoke> smokes = new Array<Smoke>();
    private Pool<Smoke> smokePool = new Pool<Smoke>() {
        @Override
        protected Smoke newObject() {
            return new Smoke();
        }
    };

    private Array<Hit> hits = new Array<Hit>();
    private Pool<Hit> hitPool = new Pool<Hit>() {
        @Override
        protected Hit newObject() {
            return new Hit();
        }
    };

    private class Hit implements Pool.Poolable {
        Vector2 pos;
        float size;
        float alpha;
        float rotation;
        boolean alive;

        Hit() {
            pos = new Vector2();
            alive = false;
        }

        public void update(float deltaTime) {
            alpha -= 2f * deltaTime;
            size -= 2f * deltaTime;
            rotation -= 360 * deltaTime;
            if (alpha <= 0) {
                alive = false;
            }
        }

        public Hit init(float x, float y) {
            pos.set(x - laserHitSprite.getWidth() / 2f, y - laserHitSprite.getHeight() / 2f);
            alpha = 1f;
            size = 1f;
            rotation = MathUtils.random(360f);
            alive = true;
            return this;
        }

        @Override
        public void reset() {
            pos.setZero();
            size = 1f;
            alpha = 1f;
            alive = false;
        }
    }

    private class Laser implements Pool.Poolable {
        Vector2 pos;
        Vector2 vel;
        boolean alive;

        Laser() {
            pos = new Vector2();
            vel = new Vector2();
            alive = false;
        }

        public void update(float deltaTime) {
            pos.y += vel.y * deltaTime;
            if (pos.y > game.height) {
                alive = false;
            }
        }

        public Laser init(float x, float y, float vx, float vy) {
            pos.set(x, y);
            vel.set(vx, vy);
            alive = true;
            return this;
        }

        @Override
        public void reset() {
            pos.setZero();
            vel.setZero();
            alive = false;
        }
    }

    private class Bomb implements Pool.Poolable {
        Vector2 pos;
        Vector2 vel;
        float smokeCooldown;
        boolean alive;

        Bomb() {
            pos = new Vector2();
            vel = new Vector2();
            alive = false;
        }

        public void update(float deltaTime) {
            pos.y += vel.y * deltaTime;
            if (pos.y < -bombSprite.getHeight()) {
                alive = false;
            }
            smokeCooldown -= deltaTime;
            if (smokeCooldown < 0) {
                smokeCooldown = 0.05f;
                smoke();
            }
        }

        public Bomb init(float x, float y, float vx, float vy) {
            pos.set(x, y);
            vel.set(vx, vy);
            alive = true;
            smokeCooldown = 0.05f;
            return this;
        }

        private void smoke() {
            smokes.add(smokePool.obtain().init(pos.x + bombSprite.getWidth() / 2f, pos.y + bombSprite.getHeight() * 0.75f));
        }

        @Override
        public void reset() {
            pos.setZero();
            vel.setZero();
            alive = false;
        }
    }

    private Vector2 tempVec = new Vector2();
    private class Missile implements Pool.Poolable {
        Vector2 pos;
        Vector2 vel;
        float smokeCooldown;
        Enemy target;
        boolean alive;

        Missile() {
            pos = new Vector2();
            vel = new Vector2();
            alive = false;
        }

        public void update(float deltaTime) {
            pos.add(-vel.x * deltaTime, -vel.y * deltaTime);
            if (target == null && enemies.size > 0) {
                target = enemies.random();
            } else if (target != null && !target.alive && enemies.size > 0) {
                target = enemies.random();
            } else if (target != null && target.alive) {
                final float targetAngle = tempVec.set(target.pos).sub(pos).nor().angle();
                final float currAngle = vel.angle();
                final float diff = Math.abs(targetAngle - currAngle);
                final float rotationSpeed = 200f * deltaTime;
                vel.rotate(diff < 180 ? rotationSpeed : -rotationSpeed);
            }
            smokeCooldown -= deltaTime;
            if (smokeCooldown < 0) {
                smokeCooldown = 0.05f;
                smoke();
            }
        }

        public Missile init(float x, float y) {
            pos.set(x - missileSprite.getWidth() / 2f, y - missileSprite.getHeight() / 2f);
            vel.set(0, -1).setLength(game.height / 2f);
            alive = true;
            if (enemies.size > 0) {
                target = enemies.random();
            }
            smokeCooldown = 0.05f;
            return this;
        }

        private void smoke() {
            smokes.add(smokePool.obtain().init(pos.x + missileSprite.getWidth() / 2f, pos.y + missileSprite.getHeight() / 2f));
        }

        @Override
        public void reset() {
            pos.setZero();
            vel.setZero();
            target = null;
            alive = false;
        }
    }

    private class Smoke implements Pool.Poolable {
        Sprite sprite;
        Vector2 pos;
        Vector2 vel;
        float col;
        float size;
        float alpha;
        float rot;
        boolean alive;

        Smoke() {
            pos = new Vector2();
            vel = new Vector2();
            alive = false;
        }

        public void update(float deltaTime) {
            size += 1.5f * deltaTime;
            alpha = Math.max(0f, alpha - 2f * deltaTime);
            rot += 45f * deltaTime;
            col = Math.max(0f, col - 1.5f * deltaTime);
            pos.add(vel.x * deltaTime, vel.y * deltaTime);
            if (alpha <= 0) {
                alive = false;
            }
        }

        public Smoke init(float x, float y, float vx, float vy, float col) {
            final int spNum = MathUtils.random(1, 3);
            sprite = spNum == 1 ? smokeASprite : spNum == 2 ? smokeBSprite : smokeCSprite;
            pos.set(x - sprite.getWidth() / 2f, y - sprite.getHeight() / 2f);
            vel.set(vx, vy);
            rot = MathUtils.random(360f);
            size = MathUtils.random(0.4f, 0.75f);
            this.col = col;
            alpha = 0.75f;
            alive = true;
            return this;
        }

        public Smoke init(float x, float y) {
            return init(x, y, 0f, 0f, 0f);
        }

        @Override
        public void reset() {
            pos.setZero();
            vel.setZero();
            rot = 0f;
            size = 1f;
            alpha = 1f;
            sprite = null;
            alive = false;
        }
    }

    private class Enemy implements Pool.Poolable {
        Sprite sprite;
        Vector2 pos;
        Vector2 vel;
        boolean alive;
        final int points = 1;
        float bombCooldown;
        float precisionCooldown;
        int maxHealth;
        int health;

        Enemy() {
            pos = new Vector2();
            vel = new Vector2();
            alive = false;
        }

        public Enemy init(float x, float y, float vx, float vy) {
            final int spNum = MathUtils.random(1, 3);
            sprite = spNum == 1 ? enemy1Sprite : spNum == 2 ? enemy2Sprite : enemy3Sprite;
            pos.set(x, y);
            vel.set(vx, vy);
            alive = true;
            maxHealth = 5;
            health = maxHealth;
            bombCooldown = MathUtils.random(0.5f, 5f);
            precisionCooldown = 1f;
            return this;
        }

        public void update(float deltaTime) {
            pos.x += vel.x * deltaTime;
            if (pos.x < -sprite.getWidth()) {
                pos.x = game.width;
            } else if (pos.x > game.width) {
                pos.x = -sprite.getWidth();
            }
            bombCooldown -= deltaTime;
            if (bombCooldown < 0) {
                fire();
                bombCooldown = MathUtils.random(1.5f, 5f);
                precisionCooldown = 2f;
            }
            precisionCooldown -= deltaTime;
            final float diffToPlayer = Math.abs((pos.x + sprite.getWidth() / 2f) - (shipSprite.getX() + shipSprite.getWidth() / 2f));
            if (precisionCooldown < 0 && diffToPlayer < shipSprite.getWidth() / 4f) {
                if (MathUtils.random(5) == 1) {
                    fire();
                }
                precisionCooldown = 2f;
            }
        }

        private void fire() {
            bombs.add(bombPool.obtain().init(pos.x + sprite.getWidth() / 2f - bombSprite.getWidth() / 2f,
                    pos.y + bombSprite.getHeight(), 0f, -game.height / 2f));
        }

        @Override
        public void reset() {
            sprite = null;
            pos.setZero();
            vel.setZero();
            health = 0;
            alive = false;
        }
    }

    private enum FireType { Laser, Missile }
    private class Player {
        int points = 0;
        int hitMinusPoints = maxPoints / 4;
        float laserCooldown;
        float missileCooldown;
        boolean rightLaser;
        float smokeCooldown;
        float soundTime;

        Player() {
            shipSprite.setPosition((game.width - shipSprite.getWidth()) / 2f, shipSprite.getHeight() * 1.5f);
            rightLaser = MathUtils.randomBoolean();
            laserCooldown = 0.33f;
            missileCooldown = 2f;
            smokeCooldown = 0f;
            soundTime = 0.25f;
        }

        public void update(float deltaTime) {
            if (Gdx.input.isTouched()) {
                final float x = MathUtils.clamp(getTouchPos().x, shipSprite.getWidth() / 2f, game.width - shipSprite.getWidth() / 2f);
                final float y = MathUtils.clamp(getTouchPos().y, 0, shipSprite.getHeight() * 3f);
                shipSprite.setPosition(x - shipSprite.getWidth() / 2f, y);

                laserCooldown -= deltaTime;
                if (laserCooldown < 0) {
                    fire(FireType.Laser);
                    laserCooldown = 0.075f;
                }
                missileCooldown -= deltaTime;
                if (missileCooldown < 0) {
                    missileCooldown = 2f;
                    fire(FireType.Missile);
                }
                soundTime -= deltaTime;
                if (soundTime < 0) {
                    laserSound.play(0.18f, MathUtils.random(1.25f, 1.8f), 0f);
                    soundTime = 0.25f;
                }
            }
            smokeCooldown -= deltaTime;
            if (smokeCooldown < 0) {
                smokeCooldown = 0.1f;
                smoke();
            }
        }

        private void fire(FireType type) {
            if (type == FireType.Laser) {
                final float x = rightLaser ? shipSprite.getX() + shipSprite.getWidth() - laserSprite.getWidth() / 2f :
                        shipSprite.getX() - laserSprite.getWidth() / 2f;
                lasers.add(laserPool.obtain().init(x, shipSprite.getY() + laserSprite.getHeight() / 2f, 0f, game.height * 1.5f));
                rightLaser = !rightLaser;
            } else {
                // if overwhelmed by enemies, spawn more missiles
                for (int i = 0; i <= enemies.size; i += 6) {
                    final float x = shipSprite.getX() + shipSprite.getWidth() / 2f;
                    final float y = shipSprite.getY() + shipSprite.getHeight() / 2f;
                    missiles.add(missilePool.obtain().init(x, y));
                }
            }
        }

        private void smoke() {
            smokes.add(smokePool.obtain().init(shipSprite.getX() + shipSprite.getWidth() / 2f, shipSprite.getY() - 10f));
        }
    }

    private Player player;
    private EnemySpawner spawner;

    public SpaceShooter(DoubleUp game) {
        super(game);
        setTitle("Space Shooter");
        setDescription("Touch to navigate and shoot, avoid falling rockets or you lose points");
        setBackground("ui/title_background");
        setIcon("minigames/SpaceShooter/ship");

        initSprites();
        player = new Player();
        spawner = new EnemySpawner();
    }

    private void initSprites() {
        shipSprite = getSprite("minigames/SpaceShooter/ship");
        shipSprite.setSize(220, 220 * shipSprite.getHeight() / shipSprite.getWidth());
        enemy1Sprite = getSprite("minigames/SpaceShooter/enemy1");
        enemy1Sprite.setSize(128, 128 * enemy1Sprite.getHeight() / enemy1Sprite.getWidth());
        enemy2Sprite = getSprite("minigames/SpaceShooter/enemy2");
        enemy2Sprite.setSize(enemy1Sprite.getWidth(), enemy1Sprite.getHeight());
        enemy3Sprite = getSprite("minigames/SpaceShooter/enemy3");
        enemy3Sprite.setSize(enemy1Sprite.getWidth(), enemy1Sprite.getHeight());
        laserSprite = getSprite("minigames/SpaceShooter/laser");
        laserSprite.setSize(laserSprite.getWidth(), laserSprite.getHeight() * 0.75f);
        laserHitSprite = getSprite("minigames/SpaceShooter/laser_hit");
        bombSprite = getSprite("minigames/SpaceShooter/bomb");
        bombSprite.setRotation(180f);
        smokeASprite = getSprite("minigames/SpaceShooter/smoke1");
        smokeBSprite = getSprite("minigames/SpaceShooter/smoke2");
        smokeCSprite = getSprite("minigames/SpaceShooter/smoke3");
        missileSprite = getSprite("minigames/SpaceShooter/missile");
        missileSprite.setSize(missileSprite.getWidth() * 0.85f, missileSprite.getHeight() * 0.85f);
    }

    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
        laserSound = getSound("sounds/space_laser.ogg");
        explosionSound = getSound("sounds/space_explosion.ogg");
        failSound = getSound("sounds/space_fail.ogg");
    }

    private final int maxPoints = 20;
    @Override
    public float getProgress() {
        return 100f * player.points / maxPoints;
}

    @Override
    public boolean isFinished() {
        return player.points >= maxPoints;
}

    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        for (Smoke smoke : smokes) {
            smoke.sprite.setScale(smoke.size);
            smoke.sprite.setRotation(smoke.rot);
            smoke.sprite.setColor(1f - smoke.col, 1f - smoke.col, 1f - smoke.col, smoke.alpha);
            smoke.sprite.setPosition(smoke.pos.x, smoke.pos.y);
            smoke.sprite.draw(game.batch);
        }
        for (Laser laser : lasers) {
            laserSprite.setPosition(laser.pos.x, laser.pos.y);
            laserSprite.draw(game.batch);
        }
        for (Missile missile : missiles) {
            missileSprite.setRotation(missile.vel.angle() + 90f);
            missileSprite.setPosition(missile.pos.x, missile.pos.y);
            missileSprite.draw(game.batch);
        }
        for (Bomb bomb : bombs) {
            bombSprite.setPosition(bomb.pos.x, bomb.pos.y);
            bombSprite.draw(game.batch);
        }
        for (Enemy enemy : enemies) {
            final float factor = 1f - enemy.health / (float)enemy.maxHealth;
            enemy.sprite.setColor(1f, 1f - 0.5f * factor, 1f - 0.5f * factor, 1f);
            enemy.sprite.setPosition(enemy.pos.x, enemy.pos.y);
            enemy.sprite.draw(game.batch);
        }
        for (Hit hit : hits) {
            laserHitSprite.setPosition(hit.pos.x, hit.pos.y);
            laserHitSprite.setAlpha(hit.alpha);
            laserHitSprite.setRotation(hit.rotation);
            laserHitSprite.setScale(hit.size);
            laserHitSprite.draw(game.batch);
        }
        shipSprite.draw(game.batch);
        game.batch.end();
    }

    public void update(float deltaTime) {
        player.update(deltaTime);
        spawner.update(deltaTime);

        // lasers
        for (Laser laser : lasers) {
            laser.update(deltaTime);
            for (Enemy enemy : enemies) {
                if (!enemy.alive) { continue; }
                laserSprite.setPosition(laser.pos.x, laser.pos.y);
                enemy.sprite.setPosition(enemy.pos.x, enemy.pos.y);
                if (laserSprite.getBoundingRectangle().overlaps(enemy.sprite.getBoundingRectangle())) {
                    enemy.health--;
                    if (enemy.health <= 0) {
                        player.points += enemy.points;
                        enemy.alive = false;
                        spawnExplosion(enemy.pos.x + enemy.sprite.getWidth() / 2f, enemy.pos.y + enemy.sprite.getHeight() / 2f);
                        explosionSound.play(0.3f, 1.5f, 0f);
                    } else {
                        hits.add(hitPool.obtain().init(laser.pos.x - laserSprite.getWidth() / 2f, laser.pos.y + laserSprite.getHeight() / 2f));
                    }
                    laser.alive = false;
                    break;
                }
            }
        }
        for (int i = lasers.size - 1; i >= 0; --i) {
            if (!lasers.get(i).alive) {
                laserPool.free(lasers.removeIndex(i));
            }
        }
        for (int i = hits.size - 1; i >= 0; --i) {
            hits.get(i).update(deltaTime);
            if (!hits.get(i).alive) {
                hitPool.free(hits.removeIndex(i));
            }
        }

        // missiles
        for (Missile missile: missiles) {
            missile.update(deltaTime);
            for (Enemy enemy : enemies) {
                if (!enemy.alive) { continue; }
                missileSprite.setPosition(missile.pos.x, missile.pos.y);
                enemy.sprite.setPosition(enemy.pos.x, enemy.pos.y);
                if (missileSprite.getBoundingRectangle().overlaps(enemy.sprite.getBoundingRectangle())) {
                    enemy.health = 0;
                    player.points += enemy.points;
                    enemy.alive = false;
                    missile.alive = false;
                    spawnExplosion(enemy.pos.x + enemy.sprite.getWidth() / 2f, enemy.pos.y + enemy.sprite.getHeight() / 2f);
                    explosionSound.play(0.3f, 1.5f, 0f);
                    break;
                }
            }
        }
        for (int i = missiles.size - 1; i >= 0; --i) {
            if (!missiles.get(i).alive) {
                missilePool.free(missiles.removeIndex(i));
            }
        }

        // smoke trails
        for (int i = smokes.size - 1; i >= 0; --i) {
            smokes.get(i).update(deltaTime);
            if (!smokes.get(i).alive) {
                smokePool.free(smokes.removeIndex(i));
            }
        }

        // bombs
        for (Bomb bomb : bombs) {
            bomb.update(deltaTime);
            bombSprite.setPosition(bomb.pos.x, bomb.pos.y);
            if (bombSprite.getBoundingRectangle().overlaps(shipSprite.getBoundingRectangle())) {
                bomb.alive = false;
                spawnExplosion(bomb.pos.x + bombSprite.getWidth() / 2f, bomb.pos.y);
                player.points = Math.max(0, player.points - player.hitMinusPoints);
                explosionSound.play(0.35f);
                failSound.play(0.55f);
            }
        }
        for (int i = bombs.size - 1; i >= 0; --i) {
            if (!bombs.get(i).alive) {
                bombPool.free(bombs.removeIndex(i));
            }
        }

        // enemies
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);
        }
        for (int i = enemies.size - 1; i >= 0; --i) {
            if (!enemies.get(i).alive) {
                enemyPool.free(enemies.removeIndex(i));
            }
        }
    }

    private void spawnExplosion(float orgX, float orgY) {
        final float numSmokes = MathUtils.random(7, 12);
        for (int i = 0; i < numSmokes; ++i) {
            final float x = orgX + MathUtils.random(-50, 50);
            final float y = orgY + MathUtils.random(-50, 50);
            final float vx = MathUtils.random(-50, 50);
            final float vy = MathUtils.random(-50, 50);
            final float col = MathUtils.random(0.5f, 1f);
            smokes.add(smokePool.obtain().init(x, y, vx, vy, col));
        }
    }

    private class EnemySpawner {
        int repeat;
        float spawnCooldown, spawnTime, x, y, vx;

        EnemySpawner() {
            init();
        }

        public void update(float deltaTime) {
            spawnCooldown -= deltaTime;
            if (spawnCooldown < 0 && enemies.size <= 30) {
                spawn();
                spawnCooldown = spawnTime;
                --repeat;
                if (repeat == 0) {
                    init();
                }
            }
        }

        private void init() {
            repeat = MathUtils.random(1, (int)(game.width / enemy1Sprite.getWidth()) - 1);
            x = MathUtils.randomBoolean() ? -enemy1Sprite.getWidth() : game.width;
            final int maxRows = (int)(game.height * 0.5f / enemy1Sprite.getHeight());
            final float spacing = 12;
            y = game.height - enemy1Sprite.getHeight() - spacing - MathUtils.random(0, maxRows - 1) * (enemy1Sprite.getHeight() + spacing);
            vx = MathUtils.random(game.width * 0.35f, game.width * 0.85f) * MathUtils.randomSign();
            spawnTime = (enemy1Sprite.getWidth() + spacing) / Math.abs(vx);
            final float lowerBound = enemies.size < 5 ? 0.25f : enemies.size < 10 ? 0.75f : 1.5f;
            spawnCooldown = MathUtils.random(lowerBound, 4f);
        }

        private void spawn() {
            enemies.add(enemyPool.obtain().init(x, y, vx, 0f));
        }
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void dispose() {
    }
}
