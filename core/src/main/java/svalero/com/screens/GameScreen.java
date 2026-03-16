package svalero.com.screens;


import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import svalero.com.KeyFinder;
import svalero.com.characters.Player;
import svalero.com.managers.LevelManager;
import svalero.com.managers.RenderManager;
import svalero.com.managers.ResourceManager;
import svalero.com.managers.SpriteManager;

public class GameScreen implements Screen {

    final KeyFinder game;

    private SpriteManager spriteManager;
    private LevelManager levelManager;
    private RenderManager renderManager;
    private Player player;

    private TextureRegion pjRegion;

    private Sound sound;
    private Music music;

    public GameScreen(final KeyFinder game) {
        this.game = game;
    }

    // Este método se ejecuta en el momento en que se cambia a esta Screen
    //   * Si esta Screen carga un menú es el momento de crearlo
    //   * Si es una pantalla de juego, es el momento de inicializar lo que no
    //   * se ha inicializado en el constructor
    @Override
    public void show() {
        // start the playback of the background music, when the screen is shown
        ResourceManager.loadAllResources();
        // Minimal runtime setup: managers and player must exist before first render().
        renderManager = new RenderManager();
        spriteManager = new SpriteManager(game);
        levelManager = new LevelManager();
        player = new Player(
            new Texture("characters/Character_animation/priests_idle/priest1/v1/priest1_v1_1.png"),
            new Vector2(100, 100),
            spriteManager
        );
        spriteManager.setPlayer(player);
    }

    //Invocado como un bucle principal de la Screen para renderizar lo que ocurre en partida o mostrar el menu
    @Override
    public void render(float delta) {
        draw();
        logic();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        // clears the screen. It’s a good practice to clear the screen every frame.
        // Otherwise, you’ll get weird graphical errors. You can use any color you want, but we’ll just settle on Black this time.
        //shows how the Viewport is applied to the SpriteBatch. This is necessary for the images to be shown in the correct place.
        levelManager.loadCurrentLevel();
        renderManager.drawFrame(spriteManager);
    }

    private void logic() {
        // Store the worldWidth and worldHeight as local variables for brevity
        // Store the pj size for brevity
        // Clamp x to values between 0 and worldWidth
        spriteManager.handleInput(0.2f);
    }


    @Override
    public void resize(int width, int height) {
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

    }
}
