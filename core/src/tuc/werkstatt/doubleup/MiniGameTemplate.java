package tuc.werkstatt.doubleup;

public final class MiniGameTemplate extends MiniGame {

    // Declare resources here, so they're available in all functions below:
    // e.g.: Sprite ball; for graphical elements

    public MiniGameTemplate(DoubleUp game) {
        super(game);

        // Here you should load your sprites (graphical elements) and other resources for the current minigame:
        // e.g.: ball = getSprite("minigames/YOUR_GAME_NAME/IMAGE_NAME_WITHOUT_FILEEXTENSION);
        // getSprite("minigames/Jumping/rabbit") will look for an atlas texture region that is corresponding to
        // an image file located in "DoubleUp/android/assets/images/minigames/Jumping/rabbit.png".
        // You can also acquire ui elements if you need them for your game, getSprite("ui/...").

        // If you are otherwise creating new libdgx objects like a ShapeRenderer or you create your own textures
        // via new Texture(...) and so on, you should clean them up in the dispose method below.
        // Sprites that are loaded via the getSprite method are cleaned up automatically by the texture atlas
        // in the background.
    }

    @Override
    public void show() {
        // Here, you should start music and sound files, since it will be called right after your minigame
        // is the new main screen. You could also position certain graphical elements.
    }

    @Override
    public float getProgress() {
        // This method is important for the network communication with other players.
        // It should return a percentage between 0 and 100.
        // Let's say, you have a minigame in with the player needs to click five images, then
        // you should create variable to keep track of the current number of valid clicks as well as
        // the maximum amount of clicks necessary to win the game:
        // e.g.: int maxClick = 5; int currentClick = 0;
        // This method should then return the percentage value of how much progress the current player has made.
        // return 100f * currentClick / maxClick;
        // 0 Clicks returns 0%, 3 Clicks would return 60% and so on.

        return 0; // modify this
    }

    @Override
    public boolean isFinished() {
        // This method is important for the network communication with other players.
        // It should return whether the current player has finished the game. As long as it returns
        // false, nothings happens. But as soon as this method return true, it will send a notification
        // to the server indicating that the game instance was finished. It is a naive approach since it
        // does not handle any kind of cheating attempts. Blocking such attempts would be nearly impossible
        // for an open source novice program anyway. So, just roll with it and return the finish status.
        // e.g.: return maxClick == currentClick; return true only if the player has successfully clicked a certain number of images.

        return false; // modify this
    }

    @Override
    public void draw(float deltaTime) {
        // Render all your sprites, font text, etc here. It's called once per frame, usually 60 times per second.
        // deltaTime parameter is the time in seconds since the last frame/last call to this function and
        // is important so games run the same on fast and slow machines, with and without vsync.

        // If you use sprites for your minigame, then the rendering should happen within the begin() and end()
        // method calls of the global sprite batch. This is not strictly necessary but does boost performance when
        // dealing with more than a few graphical elements. OpenGL likes being handed a rather large chunk of data of the
        // same texture than to rebind textures over and over again. The call to setProjectionMatrix(...) is telling the
        // batch object that we are only interested in drawing 2d stuff, we ignoring the z axis and thus perspective for now.
        // Should your game rely only on geometric elements like lines, quads, circles, etc and is not using any kind
        // of backing textures, this can be replaced with the corresponding calls in a ShapeRenderer instance.

        // Basic text can be drawn by using game.font.draw(...) inside of the below batch.begin() and .end() calls.

        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();

        // add your sprites here, call your sprite's draw method and give it the global sprite batch for efficient rendering.
        // e.g.: ball.draw(game.batch)

        game.batch.end();

        // Check the example minigames PickColor and PumpBalloon.
    }

    @Override
    public void update(float deltaTime) {
        // You should handle all your game logic here. That includes checking for player controls,
        // updating positions and checking collisions for sprites. It's called once per frame, usually 60 times per second.
        // deltaTime parameter is the time in seconds since the last frame/last call to this function and
        // is important so games run the same on fast and slow machines, with and without vsync.

        // for example like this, move ball sprite to position of finger touch:
        // if (Gdx.input.isTouched()) {
        //     ball.setPosition(getTouchPos().x, getTouchPos().y);
        // }

        // Furthermore: Try using game.width and game.height instead of hardcoded values (such as 1920x1200) for the minigames visible
        // area on the screen. The available space could shrink throughout the development cycle due to the implementation
        // of the user interface items. At the moment the minigame is using the whole screen, but this could change
        // in a couple of days and so you would have to update your game logic accordingly each time a user interface change happens.

        // If you like to support pinching, zooming and moving the camera around, you can use the global game.camera for doing this,
        // but you would have to read the libgdx documentation for how to achieve that.

        // Check the example minigames PickColor and PumpBalloon.
    }

    @Override
    public void hide() {
        // This method will be called when your game is no longer the main screen, either because
        // the game is over, the player is about to return to the start screen.
        // Especially useful to stop playing music.
    }

    @Override
    public void dispose() {
        // This method will be called when your minigame is over and no longer visible to the player, but
        // it is not guaranteed by libgdx/android that this will be called right after the game is finished,
        // it could stay there for an indefinite amount of time, so music and sounds should instead be stopped in the hide method above.
        // Here should happen all the cleanup of object which you have created via new: e.g. ShapeRenderer.dispose();
        // As stated above, sprite that were loaded via getSprite are cleanup up automatically in the background.
    }
}
