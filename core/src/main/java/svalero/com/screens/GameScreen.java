package svalero.com.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import svalero.com.KeyFinder;
import svalero.com.characters.Enemy;
import svalero.com.characters.Player;
import svalero.com.items.Key;
import svalero.com.managers.CameraManager;
import svalero.com.managers.LevelManager;
import svalero.com.managers.RenderManager;
import svalero.com.managers.ResourceManager;
import svalero.com.managers.SpriteManager;

import static svalero.com.utils.Constants.CAMERA_HEIGHT;
import static svalero.com.utils.Constants.CAMERA_WIDTH;

public class GameScreen implements Screen {

    private final KeyFinder game;

    private SpriteManager spriteManager;
    private LevelManager levelManager;
    private RenderManager renderManager;
    private CameraManager cameraManager;
    private Viewport viewport;
    private Player player;
    private boolean isPaused;
    private PauseOverlay pauseOverlay;
    private PauseInput pauseInput;

    public GameScreen(final KeyFinder game) {
        this.game = game;
    }

    // Este método se ejecuta en el momento en que se cambia a esta Screen
    //   * Si esta Screen carga un menú es el momento de crearlo
    //   * Si es una pantalla de juego, es el momento de inicializar lo que no
    //   * se ha inicializado en el constructor
    @Override
    public void show() {
        ResourceManager.loadAllResources();
        ResourceManager.finishLoadingResources();
        initManagers();
        createEntities();
        setupWorld();
        pauseOverlay = new PauseOverlay();
        pauseInput = new PauseInput();

        spriteManager.addProjectileSource(new Vector2(145, 225), new Vector2(0f, -1f), 1.5f, false);
        spriteManager.addProjectileSource(new Vector2(175, 225), new Vector2(0f, -1f), 2.5f, false);
        cameraManager.innit();
        viewport = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, cameraManager.camera);
        viewport.apply(true);
    }

    //Invocado como un bucle principal de la Screen para renderizar lo que ocurre en partida o mostrar el menu
    @Override
    public void render(float delta) {
        togglePause();

        if (!isPaused) {
            logic(delta);
        }
        draw();
        drawPauseOverlay(delta);
        handlePauseOverlayActions();
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
        if (levelManager.isAtLevelExit(player.getRect())) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }

    }

    private void togglePause(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!isPaused) {
                pause();
            } else {
                resume();
            }
        }
    }

    private void drawPauseOverlay(float delta) {
        if (!isPaused || pauseOverlay == null) return;

        renderManager.batch.setProjectionMatrix(new Matrix4().setToOrtho2D(
            0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
        ));
        renderManager.batch.begin();
        pauseOverlay.render(renderManager.batch, delta);
        renderManager.batch.end();
    }

    private void handlePauseOverlayActions() {
        if (!isPaused || pauseOverlay == null) return;

        PauseAction action = pauseInput.pollAction(pauseOverlay);
        switch (action) {
            case RESUME -> {
                pauseOverlay.pressResume();
                resume();
            }
            case TOGGLE_OPTIONS -> pauseOverlay.pressOptions();
            case QUIT_TO_MENU -> {
                pauseOverlay.pressQuit();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
            case TOGGLE_SOUND -> pauseOverlay.pressSound();
            case NONE -> {
                // No action needed.
            }
        }
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
        isPaused = true;
    }

    @Override
    public void resume() {
        isPaused = false;
        if (pauseOverlay != null) {
            pauseOverlay.onResumeGame();
        }
    }

    @Override
    public void hide() {

    }

    //Invocado cuando esta Screen ya no es la actual
    @Override
    public void dispose() {
        if (pauseOverlay != null) {
            pauseOverlay.dispose();
            pauseOverlay = null;
        }
        pauseInput = null;
        if (renderManager != null && renderManager.batch != null) {
            renderManager.batch.dispose();
            renderManager.batch = null;
        }
    }

    private void initManagers() {
        renderManager = new RenderManager();
        spriteManager = new SpriteManager(game);
        levelManager = new LevelManager(game);
        cameraManager = new CameraManager();
    }

    private void createEntities() {

        Animation<TextureRegion> playerIdle = loadAnimation("priest1_v1", 0.18f, Animation.PlayMode.LOOP);
        Animation<TextureRegion> enemyIdle = loadAnimation("squeleton_idle", 0.18f, Animation.PlayMode.LOOP);
        Animation<TextureRegion> enemyMovement = loadAnimation("squeleton_movement", 0.10f, Animation.PlayMode.LOOP);
        Animation<TextureRegion> enemyAttack = loadAnimation("skeleton_attack", 0.08f, Animation.PlayMode.NORMAL);
        Animation<TextureRegion> enemyDamaged = loadAnimation("squeleton_damaged", 0.10f, Animation.PlayMode.NORMAL);
        Animation<TextureRegion> enemyDeath = loadAnimation("skeleton_death", 0.10f, Animation.PlayMode.NORMAL);

        Animation<TextureRegion> enemySkullIdle = loadAnimation("skull_v2", 0.10f, Animation.PlayMode.LOOP);

        player = new Player(new Vector2(120, 50), spriteManager, playerIdle);
        spriteManager.addEnemy(new Enemy(
            new Vector2(200, 190),
            spriteManager,
            2,
            enemyIdle,
            enemyMovement,
            enemyAttack,
            enemyDamaged,
            enemyDeath
        ));
        spriteManager.addEnemy(new Enemy(
            new Vector2(80, 190),
            spriteManager,
            3,
            enemyIdle,
            enemyMovement,
            enemyAttack,
            enemyDamaged,
            enemyDeath
        ));

        spriteManager.addEnemy(new Enemy(
            new Vector2(50,50),
            spriteManager,
            2,
            enemySkullIdle,
            null,
            null,
            null,
            null
        ));
    }

    private void setupWorld() {
        spriteManager.setPlayer(player);
        spriteManager.addWorldKey(new Key(
            new Vector2(140, 50)
        ));
        spriteManager.setLevelManager(levelManager);
    }

    private Animation<TextureRegion> loadAnimation(String regionName, float duration, Animation.PlayMode playMode) {
        return ResourceManager.buildIndexedAnimation(regionName, duration, playMode);
    }
}
