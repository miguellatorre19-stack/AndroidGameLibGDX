package svalero.com.screens;


import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import svalero.com.KeyFinder;
import svalero.com.characters.Enemy;
import svalero.com.characters.Player;
import svalero.com.managers.*;

import static svalero.com.utils.Constants.CAMERA_HEIGHT;
import static svalero.com.utils.Constants.CAMERA_WIDTH;

public class GameScreen implements Screen {

    final KeyFinder game;

    private SpriteManager spriteManager;
    private LevelManager levelManager;
    private RenderManager renderManager;
    private CameraManager cameraManager;
    private Viewport viewport;
    private Player player;
    private Enemy enemy;
    private Enemy enemy2;
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
        levelManager = new LevelManager(game);
        cameraManager = new CameraManager();
        Texture playerTexture = new Texture("characters/Character_animation/priests_idle/priest1/v1/priest1_v1_1.png");
        playerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        Texture enemyTexture = new Texture("characters/Character_animation/monsters_idle/skeleton2/v2/skeleton2_v2_1.png");
        Texture enemy2Texture = new Texture("characters/Character_animation/monsters_idle/vampire/v2/vampire_v2_1.png");
        player = new Player(
            playerTexture,
            new Vector2(120, 100),
            spriteManager
        );

        enemy = new Enemy(
            enemyTexture,
            new Vector2(200, 190),
            spriteManager,
            2
        );
        enemy2 = new Enemy(
            enemy2Texture,
            new Vector2(80, 190),
            spriteManager,
            3
        );
        spriteManager.setPlayer(player);
        spriteManager.addEnemy(enemy);
        spriteManager.addEnemy(enemy2);
        spriteManager.setLevelManager(levelManager);
        // Wall trap projectiles (slow constant fire).
        spriteManager.addProjectileSource(new Vector2(145, 225), new Vector2(0f, -1f), 1.5f, false);
        spriteManager.addProjectileSource(new Vector2(175, 225), new Vector2(0f, -1f), 2.5f, false);
        cameraManager.innit();
        viewport = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, cameraManager.camera);
        viewport.apply(true);
    }

    //Invocado como un bucle principal de la Screen para renderizar lo que ocurre en partida o mostrar el menu
    @Override
    public void render(float delta) {
        logic(delta);
        draw();
    }

    private void draw() {
        viewport.apply();
        ScreenUtils.clear(Color.BLACK);
        cameraManager.handleCamera(player, levelManager.getMapWorldWidth(), levelManager.getMapWorldHeight());
        levelManager.loadCurrentLevel(cameraManager.camera);
        renderManager.drawFrame(spriteManager, cameraManager.camera);
    }

    private void logic(float delta) {
        spriteManager.handleInput(delta);
    }


    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
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
