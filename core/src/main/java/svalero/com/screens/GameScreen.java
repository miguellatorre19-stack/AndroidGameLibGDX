package svalero.com.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import svalero.com.KeyFinder;
import svalero.com.characters.Enemy;
import svalero.com.characters.Neutral;
import svalero.com.characters.Player;
import svalero.com.characters.StrongerEnemy;
import svalero.com.items.Coin;
import svalero.com.items.Key;
import svalero.com.managers.AudioManager;
import svalero.com.managers.CameraManager;
import svalero.com.managers.HudManager;
import svalero.com.managers.LevelManager;
import svalero.com.managers.PopupMessageManager;
import svalero.com.managers.RenderManager;
import svalero.com.managers.ResourceManager;
import svalero.com.managers.SpriteManager;

import static svalero.com.utils.Constants.CAMERA_HEIGHT;
import static svalero.com.utils.Constants.CAMERA_WIDTH;

public class GameScreen implements Screen {
    private static final String MESSAGE_1 = "Woah! Creo que te has perdido, amigo. De algun modo has acabado en las antiguas catacumbas. " +
        "No lo vas a tener facil para huir. Para poder moverte hasta la salida, tendras que ir abriendo las puertas del laberinto. " +
        "Para ello necesitaras llaves, como esa de ahi. Son de un solo uso, asi que asegurate de como quieres usarlas.";
    private static final String MESSAGE_2 = "Las calaveras flotantes somos efimeras. Una vez que interactues con nosotros, desapareceremos.";
    private static final String ATLAS_ID = ResourceManager.GENERAL_ATLAS_ID;

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
    private AudioManager audioManager;
    private HudManager hudManager;
    private PopupMessageManager popupMessageManager;

    public GameScreen(final KeyFinder game) {
        this.game = game;
    }

    @Override
    public void show() {
        ResourceManager.loadAllResources();
        ResourceManager.finishLoadingResources();

        initManagers();
        createPlayer();
        setupWorld();

        audioManager = new AudioManager();
        pauseOverlay = new PauseOverlay();
        pauseInput = new PauseInput();
        hudManager = new HudManager(renderManager.batch);
        popupMessageManager = new PopupMessageManager(hudManager.stage);
        cameraManager.innit();

        viewport = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, cameraManager.camera);
        viewport.apply(true);

        loadLevelContent(levelManager.getCurrentLevelIndex());
        hudManager.setLives(player.getLives());

        audioManager.loadMusic("level_music", "audio/music/xDeviruchi - Mysterious Dungeon.wav");
        audioManager.playMusic("level_music", true);
        audioManager.setMusicEnabled(true);
    }

    @Override
    public void render(float delta) {
        togglePause();
        popupMessageManager.update(delta);

        if (!isPaused) {
            logic(delta);
            if (game.getScreen() != this) {
                return;
            }
        }

        draw();
        drawPauseOverlay(delta);
        handlePauseOverlayActions();
        renderManager.batch.setProjectionMatrix(hudManager.stage.getCamera().combined);
        hudManager.stage.draw();
    }

    private void draw() {
        viewport.apply();
        ScreenUtils.clear(Color.BLACK);
        cameraManager.handleCamera(player, levelManager.getMapWorldWidth(), levelManager.getMapWorldHeight());
        levelManager.loadCurrentLevel(cameraManager.camera);
        renderManager.drawFrame(spriteManager, cameraManager.camera);
    }

    private void logic(float delta) {
        player.updateBoost(delta);
        spriteManager.handleInput(delta);

        if (levelManager.isAtLevelExit(player.getHitbox(), player.getRect())) {
            if (levelManager.goToNextLevel()) {
                loadLevelContent(levelManager.getCurrentLevelIndex());
            } else {
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return;
            }
        }

        hudManager.update(delta);
        hudManager.setLives(player.getLives());
        hudManager.setCoins(player.getCoinsInInventory());
        hudManager.setBoost(player.hasBoost(), player.getBoostProgress01());
        handleNeutralNpcPopupFlow();
    }

    private void togglePause() {
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
            case TOGGLE_SOUND -> {
                pauseOverlay.pressSound();
                boolean enable = !audioManager.isSoundEnabled();
                audioManager.setSoundEnabled(enable);
                audioManager.setMusicEnabled(enable);
            }
            case NONE -> {
                // No action.
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
        hudManager.stage.getViewport().update(width, height, true);
        if (popupMessageManager != null) {
            popupMessageManager.onResize();
        }
    }

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

    @Override
    public void dispose() {
        if (pauseOverlay != null) {
            pauseOverlay.dispose();
            pauseOverlay = null;
        }
        pauseInput = null;

        if (audioManager != null) {
            audioManager.dispose();
            audioManager = null;
        }

        if (renderManager != null && renderManager.batch != null) {
            renderManager.batch.dispose();
            renderManager.batch = null;
        }
        if (hudManager != null) {
            hudManager.dispose();
            hudManager = null;
        }
        if (popupMessageManager != null) {
            popupMessageManager.dispose();
            popupMessageManager = null;
        }
        if (levelManager != null) {
            levelManager.dispose();
            levelManager = null;
        }
    }

    private void initManagers() {
        renderManager = new RenderManager();
        spriteManager = new SpriteManager(game);
        levelManager = new LevelManager(game);
        cameraManager = new CameraManager();
    }

    private void createPlayer() {
        Animation<TextureRegion> playerIdle = loadAnimation("priest1_v1", 0.18f, Animation.PlayMode.LOOP);
        player = new Player(new Vector2(120, 50), spriteManager, playerIdle);
    }

    private void setupWorld() {
        spriteManager.setPlayer(player);
        spriteManager.setLevelManager(levelManager);
    }

    private void loadLevelContent(int levelIndex) {
        popupMessageManager.clear();
        spriteManager.clearLevelEntities();
        positionPlayerForLevel(levelIndex);

        Animation<TextureRegion> enemyIdle = loadAnimation("squeleton_idle", 0.18f, Animation.PlayMode.LOOP);
        Animation<TextureRegion> enemyMovement = loadAnimation("squeleton_movement", 0.10f, Animation.PlayMode.LOOP);
        Animation<TextureRegion> enemyAttack = loadAnimation("skeleton_attack", 0.08f, Animation.PlayMode.NORMAL);
        Animation<TextureRegion> enemyDamaged = loadAnimation("squeleton_damaged", 0.10f, Animation.PlayMode.NORMAL);
        Animation<TextureRegion> enemyDeath = loadAnimation("skeleton_death", 0.10f, Animation.PlayMode.NORMAL);

        Animation<TextureRegion> neutralIdle = loadAnimation("skull_v2", 0.10f, Animation.PlayMode.LOOP);

        Animation<TextureRegion> strongEnemyIdle = loadAnimation("vampire_idle", 0.18f, Animation.PlayMode.LOOP);
        Animation<TextureRegion> strongEnemyMovement = loadAnimation("vampire_movement", 0.10f, Animation.PlayMode.LOOP);
        Animation<TextureRegion> strongEnemyAttack = loadAnimation("vampire_attack", 0.08f, Animation.PlayMode.NORMAL);
        Animation<TextureRegion> strongEnemyDamaged = loadAnimation("vampire_damaged", 0.10f, Animation.PlayMode.NORMAL);
        Animation<TextureRegion> strongEnemyDeath = loadAnimation("vampire_death", 0.10f, Animation.PlayMode.NORMAL);

        if (levelIndex == 0) {
            loadFirstLevelContent(enemyIdle, enemyMovement, enemyAttack, enemyDamaged, enemyDeath, neutralIdle);
            return;
        }

        if (levelIndex == 1) {
            loadSecondLevelContent(
                enemyIdle,
                enemyMovement,
                enemyAttack,
                enemyDamaged,
                enemyDeath,
                neutralIdle,
                strongEnemyIdle,
                strongEnemyMovement,
                strongEnemyAttack,
                strongEnemyDamaged,
                strongEnemyDeath
            );
        }
    }

    private void loadFirstLevelContent(
        Animation<TextureRegion> enemyIdle,
        Animation<TextureRegion> enemyMovement,
        Animation<TextureRegion> enemyAttack,
        Animation<TextureRegion> enemyDamaged,
        Animation<TextureRegion> enemyDeath,
        Animation<TextureRegion> neutralIdle
    ) {
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

        spriteManager.addNeutral(new Neutral(
            new Vector2(140, 50),
            spriteManager,
            neutralIdle,
            MESSAGE_1
        ));

        spriteManager.addNeutral(new Neutral(
            new Vector2(50, 100),
            spriteManager,
            neutralIdle,
            MESSAGE_2
        ));

        spriteManager.addWorldKey(new Key(new Vector2(160, 50)
            ));

        spriteManager.addWorldKey(new Key(new Vector2(160, 60)
        ));


        Vector2[] coinSpawns = {
            new Vector2(105, 120),
            new Vector2(110, 120),
            new Vector2(110, 100),
            new Vector2(125, 100)
        };
        for (Vector2 pos : coinSpawns) {
            spriteManager.addWorldCoin(new Coin(pos));
        }

        spriteManager.addProjectileSource(new Vector2(145, 225), new Vector2(0f, -1f), 1.5f, false);
        spriteManager.addProjectileSource(new Vector2(175, 225), new Vector2(0f, -1f), 2.5f, false);
    }

    private void loadSecondLevelContent(
        Animation<TextureRegion> enemyIdle,
        Animation<TextureRegion> enemyMovement,
        Animation<TextureRegion> enemyAttack,
        Animation<TextureRegion> enemyDamaged,
        Animation<TextureRegion> enemyDeath,
        Animation<TextureRegion> neutralIdle,
        Animation<TextureRegion> strongEnemyIdle,
        Animation<TextureRegion> strongEnemyMovement,
        Animation<TextureRegion> strongEnemyAttack,
        Animation<TextureRegion> strongEnemyDamaged,
        Animation<TextureRegion> strongEnemyDeath
    ) {
        spriteManager.addEnemy(new Enemy(
            new Vector2(72, 32),
            spriteManager,
            3,
            enemyIdle,
            enemyMovement,
            enemyAttack,
            enemyDamaged,
            enemyDeath
        ));

        spriteManager.addEnemy(new Enemy(
            new Vector2(156, 108),
            spriteManager,
            3,
            enemyIdle,
            enemyMovement,
            enemyAttack,
            enemyDamaged,
            enemyDeath
        ));

        spriteManager.addEnemy(new Enemy(
            new Vector2(252, 184),
            spriteManager,
            3,
            enemyIdle,
            enemyMovement,
            enemyAttack,
            enemyDamaged,
            enemyDeath
        ));

        spriteManager.addStrongerEnemy(new StrongerEnemy(
            new Vector2(320, 48),
            spriteManager,
            5,
            strongEnemyIdle,
            strongEnemyMovement,
            strongEnemyAttack,
            strongEnemyDamaged,
            strongEnemyDeath
        ));

        Vector2[] keySpawns = {
            new Vector2(32, 32),
            new Vector2(168, 40),
            new Vector2(304, 192)
        };
        for (Vector2 pos : keySpawns) {
            spriteManager.addWorldKey(new Key(pos));
        }

        Vector2[] coinSpawns = {
            new Vector2(48, 80),
            new Vector2(96, 176),
            new Vector2(184, 96),
            new Vector2(232, 48),
            new Vector2(280, 144),
            new Vector2(336, 80)
        };
        for (Vector2 pos : coinSpawns) {
            spriteManager.addWorldCoin(new Coin(pos));
        }

        spriteManager.addProjectileSource(new Vector2(128, 224), new Vector2(0f, -1f), 1.6f, false);
        spriteManager.addProjectileSource(new Vector2(288, 16), new Vector2(-1f, 0f), 2.0f, false);
    }

    private void positionPlayerForLevel(int levelIndex) {
        Vector2 spawn = getLevelSpawn(levelIndex);
        player.getPosition().set(spawn);
        player.syncHitboxFromPosition();
    }

    private Vector2 getLevelSpawn(int levelIndex) {
        if (levelIndex == 0) return new Vector2(120, 50);
        if (levelIndex == 1) return new Vector2(24, 24);
        return new Vector2(16, 16);
    }

    private Animation<TextureRegion> loadAnimation(String regionName, float duration, Animation.PlayMode playMode) {
        return ResourceManager.buildIndexedAnimation(ATLAS_ID, regionName, duration, playMode);
    }

    private void handleNeutralNpcPopupFlow() {
        for (Neutral neutral : spriteManager.getNeutrals()) {
            if (neutral.isDead()) continue;
            if (neutral.hasInteractionTriggered()) continue;
            if (Intersector.overlapConvexPolygons(neutral.getHitbox(), player.getHitbox())) {
                neutral.markInteractionStarted();
                popupMessageManager.showMessage(neutral.getInteractionMessage());
            }
        }

        if (!popupMessageManager.isVisible()) {
            for (Neutral neutral : spriteManager.getNeutrals()) {
                if (neutral.isWaitingPopupDismiss()) {
                    neutral.onPopupDismissed();
                }
            }
        }
    }
}
