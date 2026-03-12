package svalero.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import svalero.com.KeyFinder;
import svalero.com.managers.LevelManager;

public class GameScreen implements Screen {

    final KeyFinder game;

    private LevelManager levelManager;

    private Texture pjTexture;
    private Sprite pjSprite;
    private Texture dungeonSheet;
    private TextureRegion pjRegion;
    public SpriteBatch batch;

    private Sound sound;
    private Music music;

    private Vector2 touchPos;
    private Rectangle pjRectangle;

    public GameScreen(final KeyFinder game) {
        this.game = game;

        //will ensure that no matter what size our window is, the full game view will always be visible.
        // The parameters determine how large our visible game world will be in game units.
        game.viewport = new FitViewport(7 ,5 );

        pjTexture = new Texture("2D_Pixel_Dungeon_Asset_Pack/Character_animation/monsters_idle/vampire/v1/vampire_v1_1.png");
        dungeonSheet = new Texture("2D_Pixel_Dungeon_Asset_Pack/character_and_tileset/Dungeon_Tileset.png");
        dungeonSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // we use split for map tiles, since the source image is 16x16 and thus we can get tilesets of the same size
        //coordinates 0,0 are at the upper-left corner in definition

//        TextureRegion[][] tiles = TextureRegion.split(dungeonSheet, 16, 16);
//        TextureRegion floorTiles = tiles[][];

//        pjRegion = new TextureRegion(dungeonSheet, x, y, width, height);
//        pjSprite = new Sprite(pjRegion);

        pjSprite = new Sprite(pjTexture);
        pjSprite.setSize(0.5f, 0.5f);

        touchPos = new Vector2();

        pjRectangle = new Rectangle();

    }

    // Este método se ejecuta en el momento en que se cambia a esta Screen
    //   * Si esta Screen carga un menú es el momento de crearlo
    //   * Si es una pantalla de juego, es el momento de inicializar lo que no
    //   * se ha inicializado en el constructor
    @Override
    public void show() {
        // start the playback of the background music, when the screen is shown


    }


    //Invocado como un bucle principal de la Screen para renderizar lo que ocurre en partida o mostrar el menu
    @Override
    public void render(float delta) {
        draw();
        logic();
        input();
    }



    private void draw() {
        game.viewport.apply();
        ScreenUtils.clear(Color.BLACK);
        // clears the screen. It’s a good practice to clear the screen every frame.
        // Otherwise, you’ll get weird graphical errors. You can use any color you want, but we’ll just settle on Black this time.
        batch.setProjectionMatrix(game.viewport.getCamera().combined);
        //shows how the Viewport is applied to the SpriteBatch. This is necessary for the images to be shown in the correct place.
        batch.begin();

        float worldWidth = game.viewport.getWorldWidth() * 1.5f;
        float worldHeight = game.viewport.getWorldHeight() * 2;

        //coordinates 0,0 are at the bottom-left
        batch.draw(dungeonSheet, 0 ,0, worldWidth, worldHeight );
        pjSprite.draw(batch);

        batch.end();
    }

    private void logic() {
        // Store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        // Store the pj size for brevity
        float pjSpriteWidth = pjSprite.getWidth();
        float pjSpriteHeight = pjSprite.getHeight();

        // Clamp x to values between 0 and worldWidth
        pjSprite.setX(MathUtils.clamp(pjSprite.getX(), 0, worldWidth-pjSpriteWidth));
        pjSprite.setY(MathUtils.clamp(pjSprite.getY(), 0, worldHeight-pjSpriteHeight));
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();// retrieve the current delta
        //Delta time is the measured time between frames If we multiply our movement by delta time, the movement will be consistent no matter what hardware we run this game on.
        // If we multiply our movement by delta time, the movement will be consistent no matter what hardware we run this game on.
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            pjSprite.translateX(speed * delta); // Move the bucket right
            // todo: Do something when the user presses the right arrow
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            pjSprite.translateX(-speed * delta);// Move the pj left
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)){
            pjSprite.translateY(speed *delta); //Move the pj up
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            pjSprite.translateY(-speed * delta);//Move the pj down
        }

        if (Gdx.input.isTouched()) { // If the user has clicked or tapped the screen
            // todo: React to the player touching the screen
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); // Get where the touch happened on screen
            game.viewport.unproject(touchPos); // Convert the units to the world units of the viewport
            pjSprite.setCenterX(touchPos.x); // Change the horizontally centered position of the bucket
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    //
    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    //Invocado cuando esta Screen ya no es la actual
    @Override
    public void dispose() {
        pjTexture.dispose();
    }
}
