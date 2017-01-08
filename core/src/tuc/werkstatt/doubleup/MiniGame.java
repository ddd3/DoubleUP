package tuc.werkstatt.doubleup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;

import tuc.werkstatt.doubleup.network.ClientFinishedMessage;

public abstract class MiniGame implements Screen {
    public final DoubleUpPrototype game;

    public MiniGame(DoubleUpPrototype game) {
        this.game = game;
        game.currMiniGame = this;
    }
    // implementation necessary for tracking game state and updating other players/server
    public abstract int getProgress();
    public abstract boolean isFinished();
    public abstract void draw(float deltaTime);
    public abstract void update(float deltaTime);

    @Override
    public final void render(float deltaTime) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        draw(deltaTime);
        update(deltaTime);

        // TODO REGULAR PROGRESS TO SERVER

        if (isFinished()) {
            System.out.println("Client: ClientFinishedMessage sent");
            ClientFinishedMessage msg = new ClientFinishedMessage();
            msg.gameID = game.minigames.indexOf(this.getClass().getSimpleName(), false);
            msg.clientID = game.client.getID();
            game.client.sendTCP(msg);
            game.setScreen(null);
        }
    }

    public final Sprite getSprite(String name) {
        return game.atlas.createSprite(name);
    }

    public final void exit() {
        dispose();
        game.setScreen(null);
    }
}
