package tuc.werkstatt.doubleup.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

import tuc.werkstatt.doubleup.DoubleUp;
import tuc.werkstatt.doubleup.MiniGame;

public final class PlaneWarGame extends MiniGame {

    private Sprite heroSprite, bulletSprite, enemySprite;
    private TextureAtlas shoot;
    private final int maxPoints = 10;
    private int currPoints = 0;
    private Array<Rectangle> bullets,enemys;
    private Sound bulletSound,explodeSound,gameoverSound;
    private long lastShootTime, lastAppearTime;
    private STATE state = STATE.PLAYING;
    private static final String GAME_OVER_TEXT = "Game Over... Tap any key to restart !";
    private BitmapFont bitmapFont;
    private GlyphLayout layout;

    public PlaneWarGame(DoubleUp game) {
        super(game);
        setTitle("Plane Shooter");
        setDescription("Touch to navigate, shoot and avoid other planes to gain points");
        setBackground("ui/title_background");
        setIcon("minigames/PlaneWarGame/plane_icon");

        bitmapFont = new BitmapFont();
        bitmapFont.getData().setScale(4,4);
        bitmapFont.setColor(Color.BLACK);
        layout = new GlyphLayout();
	
	    shoot = new TextureAtlas(Gdx.files.internal("images/minigames/PlaneWarGame/shoot.pack"));

        heroSprite = shoot.createSprite("hero1");
        bulletSprite = shoot.createSprite("bullet1");
        enemySprite = shoot.createSprite("enemy1");

        heroSprite.setSize(200,200);
        heroSprite.setPosition((game.width - heroSprite.getWidth()) / 2, 20);

        enemySprite.setSize(128,128);
        enemys = new Array<Rectangle>();
        spawnEnemy();

        bulletSprite.setSize(16,16);
        bullets = new Array<Rectangle>();
        spawnBullet();
    }

    	private enum STATE {
		PLAYING, GAME_OVER
	}

    private void spawnEnemy() {
        Rectangle enemy = new Rectangle(MathUtils.random
                (0, game.width - heroSprite.getWidth()), game.height,enemySprite.getHeight(),enemySprite.getWidth());
        enemys.add(enemy);
        lastAppearTime = TimeUtils.nanoTime();
    }

    private void spawnBullet() {
        Rectangle bullet = new Rectangle
                (heroSprite.getX()+100,heroSprite.getY()+128,bulletSprite.getHeight()+150,bulletSprite.getWidth()+150);
        bullets.add(bullet);
        lastShootTime=TimeUtils.nanoTime();
    }

    @Override
    public void show() {
        game.loadMusic("music/game_start_loop.ogg");
        bulletSound = getSound("sounds/bullet.mp3");
        explodeSound= getSound("sounds/enemy3_down.mp3");
        gameoverSound= getSound("sounds/game_over.mp3");
    }

    @Override
    public float getProgress() {
        return 100f * currPoints / maxPoints;
}

    @Override
    public boolean isFinished() {
        return currPoints >= maxPoints;
}

    @Override
    public void draw(float deltaTime) {
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        switch(state) {
        case PLAYING:
            for(Rectangle enemy: enemys) {
                enemySprite.setPosition(enemy.x, enemy.y);
                enemySprite.draw(game.batch);
            }
            for(Rectangle bullet: bullets) {
                bulletSprite.setPosition(bullet.x, bullet.y);
                bulletSprite.draw(game.batch);
            }
            heroSprite.draw(game.batch);
            break;
        case GAME_OVER:
            bullets.clear();
            enemys.clear();
            layout.setText(bitmapFont, GAME_OVER_TEXT);
            bitmapFont.draw(game.batch, GAME_OVER_TEXT, (game.width - layout.width) / 2,
                    (game.height - layout.height) / 2);
            if (Gdx.input.isKeyJustPressed(Keys.ANY_KEY) || Gdx.input.justTouched())
            {
                state = STATE.PLAYING;
                currPoints=0;
                heroSprite.setPosition((game.width - heroSprite.getWidth()) / 2, 20);
            }
            break;
        }
        game.batch.end();
    }

    public void update(float deltaTime) {
        // process user input
        if(Gdx.input.isTouched()) {
            heroSprite.setX(getTouchPos().x - heroSprite.getWidth() / 2);
            heroSprite.setY(getTouchPos().y - heroSprite.getWidth() / 2);
        }

        if(Gdx.input.isKeyPressed(Keys.LEFT)) heroSprite.translateX(-400 * deltaTime);
        if(Gdx.input.isKeyPressed(Keys.RIGHT)) heroSprite.translateX(400 * deltaTime);
        if(Gdx.input.isKeyPressed(Keys.UP)) heroSprite.translateY(400 * deltaTime);
        if(Gdx.input.isKeyPressed(Keys.DOWN)) heroSprite.translateY(-400 * deltaTime);

        // make sure the hero stays within the screen bounds
        if(heroSprite.getX() < 0) heroSprite.setX(0);
        if(heroSprite.getY() < 0) heroSprite.setY(0);

        if(heroSprite.getX() > game.width - heroSprite.getWidth()) {
            heroSprite.setX(game.width - heroSprite.getWidth());
        }
        if(heroSprite.getY() > game.height - heroSprite.getHeight()) {
            heroSprite.setY(game.height - heroSprite.getHeight());
        }


        if(TimeUtils.nanoTime() - lastShootTime > 300000000) spawnBullet();
//        bulletSound.play();

        Iterator<Rectangle> iter = bullets.iterator();
        while(iter.hasNext()) {
            Rectangle bullet = iter.next();
            bullet.y += 1500 * deltaTime;
            if (bullet.y + bulletSprite.getHeight() > game.height) {
                iter.remove();
            }
        }

        if(TimeUtils.nanoTime() - lastAppearTime > 800000000) spawnEnemy();

        Iterator<Rectangle> iter1 = enemys.iterator();
        while(iter1.hasNext()) {
            Rectangle enemy = iter1.next();
            enemy.y -= 1000 * deltaTime;
            if(enemy.y + enemySprite.getHeight() < 0) {
                iter1.remove();
                continue;
            }

            Rectangle.tmp.set(enemy.x, enemy.y, enemySprite.getWidth(), enemySprite.getHeight());
            if (bulletSprite.getBoundingRectangle().overlaps(Rectangle.tmp)) {
                explodeSound.play();
                iter1.remove();
                iter.remove();
                currPoints++;
            }

            Rectangle.tmp.set(enemy.x, enemy.y, enemySprite.getWidth(), enemySprite.getHeight());
            if (heroSprite.getBoundingRectangle().overlaps(Rectangle.tmp)) {
                iter.remove();
                gameoverSound.play();
                currPoints=0;
                state = STATE.GAME_OVER;
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
