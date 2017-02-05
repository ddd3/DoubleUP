package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Credits implements Screen {
    private final DoubleUp game;

    private Sprite background;
    private float volume;

    private class Credit {
        Sprite sp;
        String text;
        GlyphLayout layout;
        Color col;
        final float segments = 10f;
        float inTime;
        float outTime;
        final float timeInSec;
        float remaining;
        final float spacing = 64f;
        float alpha;

        Credit(String pic, String text, float timeInSec) {
            this.text = text;
            this.sp = game.getSprite(pic);
            alpha = 0f;
            this.col = new Color(1f, 1f, 1f, alpha);
            this.layout = new GlyphLayout(game.font, text, col, game.targetResWidth, Align.center, true);
            this.timeInSec = timeInSec;
            this.remaining = timeInSec;
            this.inTime = timeInSec / segments;
            this.outTime = timeInSec - timeInSec / segments;
            sp.setAlpha(alpha);
            final float maxHeight = game.targetResHeight * 0.22f;
            if (sp.getHeight() > maxHeight) {
                sp.setSize(maxHeight / sp.getHeight() * sp.getWidth(), maxHeight);
            }
            sp.setPosition(game.targetResWidth / 2f - sp.getWidth() / 2f, game.targetResHeight * 0.68f + spacing);
        }

        void update(float deltaTime) {
            inTime -= deltaTime;
            outTime -= deltaTime;
            remaining -= deltaTime;
            if (inTime >= 0) {
                alpha = 1f - inTime / (timeInSec / segments);
            } else if (timeInSec / segments < outTime && remaining >= 0) {
               alpha = 1f;
            } else if (outTime >= 0){
                alpha = outTime / (timeInSec / segments);
            }
            col.set(1f, 1f, 1f, alpha);
            sp.setAlpha(alpha);
            layout.setText(game.font, text, col, game.targetResWidth, Align.center, true);
        }

        void draw() {
            sp.draw(game.uiBatch);
            game.font.draw(game.uiBatch, layout, 0, game.targetResHeight * 0.68f);
        }
    }
    private Array<Credit> credits = new Array<Credit>(true, 12);
    private int currCredit = 0;

    public Credits(final DoubleUp game) {
        this.game = game;

        background = game.getSprite("ui/title_background");
        background.setPosition(0, 0);

        final float stepTime = 10f;
        credits.add(new Credit("ui/title_logo", "\nHAUPTVERANTWORTLICHE\n\nAbdullah T.\nDaniel W." +
                "\nDennis P.\nJialun J.\nSebastian S.", stepTime));
        credits.add(new Credit("ui/tuclogo", "\nINFORMATIKWERKSTATT\n\nInstitut für Informatik\n" +
                "Erstsemesterprojekt, WS 2016/17\n\nInformatik, B.Sc.\nWirtschaftsinformatik, B.Sc.", stepTime));
        credits.add(new Credit("minigames/Drop/fruit1", "\nADDITIONAL  ARTWORK\n\nBalloon Popping Sound\n" +
                "Mike Koenig, soundbible.com\n\nAnimal Mini Icons\nicojam.com\n\nFruit, Ice Pop, " +
                "Crate Images\ngamedeveloperstudio.com", stepTime));
        credits.add(new Credit("minigames/Drop/fruit2", "\nADDITIONAL  ARTWORK\n\nMedals, Space Shooter, " +
                "Shooting Gallery, Puzzle & Voiceover Pack\nkenney.nl\n\nGame Sound Fx " +
                "Pack\nDamaged Panda, opengameart.org\n\nQuack Sound\nReitanna, freesound.org", stepTime));
        credits.add(new Credit("minigames/Drop/fruit3", "\nADDITIONAL  ARTWORK\n\nError Sound\n" +
                "Fins, freesound.org\n\nCartoon Laughter\nRobinhood76, freesound.org\n\nSkorpion Sound\n" +
                "Kibblesbob, soundbible.com", stepTime));
        credits.add(new Credit("minigames/Drop/fruit4", "ADDITIONAL  ARTWORK\n\nMetallic Tonk Sound\n" +
                "Neotone, freesound.org\n\nMetallic Click Sound\nj1987, freesound.org\n\nArrow Reload Icon\n" +
                "GraphicLoads, iconarchive.com\n\nApplause Sound\napricot.blender.org", stepTime));
        credits.add(new Credit("minigames/Drop/fruit5", "\nADDITIONAL  ARTWORK\n\nAnimal Icons\n" +
                "VisualPharm, visualpharm.com\n\nVecation Icon Pack\nVisualPharm, visualpharm.com\n\n" +
                "Vehicle graphics\nCem, cemagraphics.deviantart.com", stepTime));
        credits.add(new Credit("ui/libgdxlogo", "\n\n\n\n\n\nTHANKS FOR PLAYING", stepTime));
    }

    @Override
    public void show() {
        if (!game.isTestingEnvironment()) {
            game.loadMusic("music/examples/intro_2.mp3", false);
        }
        volume = game.getMusicVolume();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyUp(final int keycode) {
                if (keycode == Input.Keys.BACK) {
                    Gdx.app.log("Credits", "Back button pressed, returning to StartScreen");
                    returnToStartScreen();
                }
                if (keycode == Input.Keys.Q || keycode == Input.Keys.ESCAPE) {
                    Gdx.app.log("Credits", "Escape button pressed, returning to StartScreen");
                    returnToStartScreen();
                }
                return false;
            }
        });
        game.screenTransitionTimestamp = TimeUtils.millis();
    }

    private void returnToStartScreen() {
        game.setScreen(new Start(game));
    }

    @Override
    public void render(float deltaTime) {
        draw(deltaTime);
        game.drawTransitionBuffer();
        updateLogic(deltaTime);
    }

    public void draw(float deltaTime) {
        game.uiView.apply();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.uiBatch.setProjectionMatrix(game.uiCamera.combined);
        game.uiBatch.begin();
        background.draw(game.uiBatch);
        if (currCredit < credits.size) {
            credits.get(currCredit).draw();
        }
        game.uiBatch.end();
    }

    private void updateLogic(float deltaTime) {
        if (Gdx.input.justTouched()) {
            returnToStartScreen();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.toggleMusicMute();
        }

        if (currCredit < credits.size) {
            Credit cred = credits.get(currCredit);
            cred.update(deltaTime);
            if (cred.remaining < 0) {
                currCredit++;
            }
        } else {
            fadeOut(deltaTime);
        }
    }

    private void fadeOut(float deltaTime) {
        draw(deltaTime);
        volume -= 0.1f * deltaTime;
        if (volume <= 0) {
            returnToStartScreen();
        } else {
            game.adjustMusicVolume(volume);
        }
    }

    @Override
    public void resize(int width, int height) {
        game.resizeViews();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        game.renderToTransitionBuffer(this);
    }

    @Override
    public void dispose() {}
}
