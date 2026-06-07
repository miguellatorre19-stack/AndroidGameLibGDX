package svalero.com.managers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.KeyFinder;

// se encarga de la carga de niveles (pantallas jugables) y de todos los elementos de los mismos (objetos, enemigos, mapa)
public class LevelManager implements Disposable {

    private static final String[] LEVEL_MAPS = {
        "levels/maps/first_level_tutorial.tmx",
        "levels/maps/second_level.tmx"
    };

    private AudioManager audioManager;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    private final LevelCollisionSystem collisionSystem;
    private final TmxMapLoader.Parameters mapLoaderParameters;

    private int currentLevelIndex;

    public LevelManager(KeyFinder game) {
        mapLoaderParameters = new TmxMapLoader.Parameters();
        mapLoaderParameters.textureMinFilter = Texture.TextureFilter.Nearest;
        mapLoaderParameters.textureMagFilter = Texture.TextureFilter.Nearest;

        collisionSystem = new LevelCollisionSystem();

        audioManager = new AudioManager();
        audioManager.loadSfx("open_door", "audio/sound/06_door_close_2.mp3");

        loadLevelByIndex(0);
    }

    public OrthogonalTiledMapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public float getMapWorldWidth() {
        int width = map.getProperties().get("width", Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        return width * tileWidth;
    }

    public float getMapWorldHeight() {
        int height = map.getProperties().get("height", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        return height * tileHeight;
    }

    public void loadCurrentLevel(OrthographicCamera camera) {
        if (camera == null) return;
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    public Array<Rectangle> getLockedDoorBounds() {
        return collisionSystem.getLockedDoorBounds();
    }

    public Array<Rectangle> getLockedLateralDoorBounds() {
        return collisionSystem.getLockedLateralDoorBounds();
    }

    public boolean isBlocked(Polygon dynamicHitbox, Rectangle dynamicBounds, boolean hasKey) {
        return collisionSystem.isBlocked(dynamicHitbox, dynamicBounds, hasKey);
    }

    public boolean consumeDoorUnlockEvent() {
        boolean unlocked = collisionSystem.consumeDoorUnlockEvent();
        if (unlocked) {
            audioManager.playSfx("open_door");
        }
        return unlocked;
    }

    public boolean isAtLevelExit(Polygon dynamicHitbox, Rectangle dynamicBounds) {
        return collisionSystem.isAtLevelExit(dynamicHitbox, dynamicBounds);
    }

    public void restartCurrentLevel(OrthographicCamera camera) {
        loadLevelByIndex(currentLevelIndex);
        loadCurrentLevel(camera);
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public int getCurrentLevelNumber() {
        return currentLevelIndex + 1;
    }

    public int getTotalLevels() {
        return LEVEL_MAPS.length;
    }

    public boolean hasNextLevel() {
        return currentLevelIndex + 1 < LEVEL_MAPS.length;
    }

    public boolean goToNextLevel() {
        if (!hasNextLevel()) return false;
        loadLevelByIndex(currentLevelIndex + 1);
        return true;
    }

    public Array<MapObject> getLevelObjects() {
        Array<MapObject> objects = new Array<>();
        if (map == null) return objects;

        for (MapLayer layer : map.getLayers()) {
            for (MapObject object : layer.getObjects()) {
                objects.add(object);
            }
        }
        return objects;
    }

    public void loadItems() {
    }

    private void loadLevelByIndex(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= LEVEL_MAPS.length) {
            throw new IllegalArgumentException("Invalid level index: " + levelIndex);
        }

        disposeCurrentMap();
        map = new TmxMapLoader().load(LEVEL_MAPS[levelIndex], mapLoaderParameters);
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        currentLevelIndex = levelIndex;
        collisionSystem.cache(map);
    }

    private void disposeCurrentMap() {
        if (mapRenderer != null) {
            mapRenderer.dispose();
            mapRenderer = null;
        }
        if (map != null) {
            map.dispose();
            map = null;
        }
        collisionSystem.clear();
    }

    @Override
    public void dispose() {
        disposeCurrentMap();
        collisionSystem.clear();
    }
}
