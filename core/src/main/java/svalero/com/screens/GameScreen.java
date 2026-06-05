package svalero.com.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
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
import svalero.com.characters.Neutral;
import svalero.com.characters.Player;
import svalero.com.items.PowerUpType;
import svalero.com.managers.AudioManager;
import svalero.com.managers.CameraManager;
import svalero.com.managers.HudManager;
import svalero.com.managers.LevelEntityFactory;
import svalero.com.managers.LevelManager;
import svalero.com.managers.PopupMessageManager;
import svalero.com.managers.RenderManager;
import svalero.com.managers.ResourceManager;
import svalero.com.managers.SpriteManager;

import static svalero.com.utils.Constants.CAMERA_HEIGHT;
import static svalero.com.utils.Constants.CAMERA_WIDTH;

public class GameScreen implements Screen {
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
        applyAudioMixDefaults();

        initManagers();
        createPlayer();
        setupWorld();

        audioManager = new AudioManager();
        audioManager.loadSfx("interface1", "audio/sound/interface1.mp3");
        pauseOverlay = new PauseOverlay();
        pauseInput = new PauseInput();
        hudManager = new HudManager(renderManager.batch);
        popupMessageManager = new PopupMessageManager(hudManager.stage);
        cameraManager.innit();

        viewport = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, cameraManager.camera);
        viewport.apply(true);

        loadLevelContent();
        hudManager.setLives(player.getLives());
        hudManager.setKeys(player.getKeysInInventory());
        hudManager.setCoins(player.getCoinsInInventory());
        updatePowerUpHud();

        audioManager.loadMusic("level_music", "audio/music/xDeviruchi - Mysterious Dungeon.wav");
        audioManager.setSoundEnabled(true);
        audioManager.playMusic("level_music", true);
        audioManager.setMusicEnabled(true);
    }

    private void applyAudioMixDefaults() {
        Preferences prefs = Gdx.app.getPreferences("keyfinder-audio");
        prefs.putFloat("musicVolume", 0.55f);
        prefs.putFloat("sfxVolume", 1.00f);
        prefs.flush();
    }

    @Override
    public void render(float delta) {
        togglePause();
        popupMessageManager.update(delta);
        boolean popupVisible = popupMessageManager.isVisible();

        if (!isPaused && !popupVisible) {
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
        renderManager.drawFrame(spriteManager, levelManager, cameraManager.camera);
    }

    private void logic(float delta) {
        player.updateBoost(delta);
        spriteManager.handleInput(delta);

        if (player.isDead()) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        if (levelManager.isAtLevelExit(player.getHitbox(), player.getRect())) {
            if (levelManager.goToNextLevel()) {
                loadLevelContent();
            } else {
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return;
            }
        }

        hudManager.update(delta);
        hudManager.setLives(player.getLives());
        hudManager.setKeys(player.getKeysInInventory());
        hudManager.setCoins(player.getCoinsInInventory());
        updatePowerUpHud();
        handleNeutralNpcPopupFlow();
    }

    private void updatePowerUpHud() {
        for (PowerUpType type : PowerUpType.values()) {
            hudManager.setPowerUp(
                type,
                player.getPowerUpCount(type),
                player.getPowerUpTimeRemainingSec(type)
            );
        }
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
                audioManager.playSfx("interface1");
                pauseOverlay.pressResume();
                resume();
            }
            case TOGGLE_OPTIONS -> {
                audioManager.playSfx("interface1");
                pauseOverlay.pressOptions();
            }
            case QUIT_TO_MENU -> {
                audioManager.playSfx("interface1");
                pauseOverlay.pressQuit();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
            case TOGGLE_SOUND -> {
                pauseOverlay.pressSound();
                boolean enable = !audioManager.isSoundEnabled();
                if (enable) {
                    audioManager.setSoundEnabled(true);
                    audioManager.playSfx("interface1");
                } else {
                    audioManager.playSfx("interface1");
                }
                if (!enable) {
                    audioManager.setSoundEnabled(false);
                }
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

        if (renderManager != null) {
            renderManager.dispose();
            renderManager = null;
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

    private void loadLevelContent() {
        popupMessageManager.clear();
        spriteManager.clearLevelEntities();
        new LevelEntityFactory(spriteManager, player).spawnAll(levelManager.getLevelObjects());
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
