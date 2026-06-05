package svalero.com.managers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ShortArray;
import svalero.com.KeyFinder;

// se encarga de la carga de niveles (pantallas jugables) y de todos los elementos de los mismos (objetos, enemigos, mapa)
public class LevelManager implements Disposable {

    private static final String[] LEVEL_MAPS = {
        "levels/maps/first_level_tutorial.tmx",
        "levels/maps/second_level.tmx"
    };

    private static final String SOLID_COLLISION_LAYER = "collisions_solid";
    private static final String COLLISION_LAYER = "colisions";
    private static final String COLLISION_LAYER_ALT = "collisions";
    private static final String TRIGGER_LAYER = "triggers";
    private static final String CLASS_PROPERTY = "class";
    private static final String CLASS_DOOR = "door";
    private static final String CLASS_EXIT = "exit";
    private static final String DOOR_TAG = "puertas";
    private static final String DOOR_TAG_ALT = "door";
    private static final String EXIT_DOOR_NAME = "door3";
    private static final String EXIT_DOOR_ID = "third_door";
    private static final String EXIT_COLLISION_ID = "exit";
    private static final String EXIT_COLLISION_NAME = "trampilla";

    private AudioManager audioManager;

    private static class CollisionShape {
        Rectangle bounds;
        Array<Polygon> convexParts = new Array<>();
    }

    private static class DoorArea {
        CollisionShape collisionShape;
        boolean isExitDoor;
        boolean unlocked;
        boolean lateral;
    }

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    private final Array<CollisionShape> staticCollisionAreas;
    private final Array<DoorArea> doorAreas;
    private final Array<CollisionShape> exitAreas;
    private final Array<Rectangle> lockedDoorBounds;
    private final Array<Rectangle> lockedLateralDoorBounds;
    private final TmxMapLoader.Parameters mapLoaderParameters;
    private final EarClippingTriangulator triangulator;

    private boolean doorUnlockedThisStep;
    private int currentLevelIndex;

    public LevelManager(KeyFinder game) {
        mapLoaderParameters = new TmxMapLoader.Parameters();
        mapLoaderParameters.textureMinFilter = Texture.TextureFilter.Nearest;
        mapLoaderParameters.textureMagFilter = Texture.TextureFilter.Nearest;

        staticCollisionAreas = new Array<>();
        doorAreas = new Array<>();
        exitAreas = new Array<>();
        lockedDoorBounds = new Array<>();
        lockedLateralDoorBounds = new Array<>();
        triangulator = new EarClippingTriangulator();
        doorUnlockedThisStep = false;

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
        lockedDoorBounds.clear();
        for (DoorArea door : doorAreas) {
            if (door.unlocked) continue;
            if (door.lateral) continue;
            lockedDoorBounds.add(door.collisionShape.bounds);
        }
        return lockedDoorBounds;
    }

    public Array<Rectangle> getLockedLateralDoorBounds() {
        lockedLateralDoorBounds.clear();
        for (DoorArea door : doorAreas) {
            if (door.unlocked) continue;
            if (!door.lateral) continue;
            lockedLateralDoorBounds.add(door.collisionShape.bounds);
        }
        return lockedLateralDoorBounds;
    }

    public boolean isBlocked(Polygon dynamicHitbox, Rectangle dynamicBounds, boolean hasKey) {
        doorUnlockedThisStep = false;
        if (dynamicHitbox == null || dynamicBounds == null) return false;

        if (isBlockedByStaticCollision(dynamicHitbox, dynamicBounds)) {
            return true;
        }

        for (DoorArea door : doorAreas) {
            if (door.unlocked) continue;
            if (!isOverlapping(dynamicHitbox, dynamicBounds, door.collisionShape)) continue;

            if (hasKey) {
                door.unlocked = true;
                doorUnlockedThisStep = true;
                continue;
            }
            return true;
        }
        return false;
    }

    public boolean consumeDoorUnlockEvent() {
        boolean unlocked = doorUnlockedThisStep;
        doorUnlockedThisStep = false;
        if (unlocked) {
            audioManager.playSfx("open_door");
        }
        return unlocked;
    }

    public boolean isAtLevelExit(Polygon dynamicHitbox, Rectangle dynamicBounds) {
        if (dynamicHitbox == null || dynamicBounds == null) return false;

        for (CollisionShape exitArea : exitAreas) {
            if (isOverlapping(dynamicHitbox, dynamicBounds, exitArea)) {
                return true;
            }
        }

        if (exitAreas.size > 0) {
            return false;
        }

        // Compatibilidad con niveles anteriores donde la salida era una puerta.
        for (DoorArea door : doorAreas) {
            if (!door.isExitDoor) continue;
            if (!door.unlocked) continue;
            if (isOverlapping(dynamicHitbox, dynamicBounds, door.collisionShape)) {
                return true;
            }
        }
        return false;
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

    public void loadItems() {
    }

    private void cacheCollisionData() {
        staticCollisionAreas.clear();
        doorAreas.clear();
        exitAreas.clear();

        MapLayer solidLayer = getFirstAvailableLayer(SOLID_COLLISION_LAYER, COLLISION_LAYER_ALT, COLLISION_LAYER);
        if (solidLayer != null) {
            cacheBlockingAndDoorDataFromLayer(solidLayer);
        }

        MapLayer triggerLayer = getFirstAvailableLayer(TRIGGER_LAYER);
        if (triggerLayer != null && triggerLayer != solidLayer) {
            cacheExitDataFromLayer(triggerLayer);
        }

        // Compatibilidad: si el mapa no usa layer "triggers", buscamos "exit" en la capa legacy.
        if (exitAreas.size == 0) {
            MapLayer legacyCollisionLayer = getFirstAvailableLayer(COLLISION_LAYER_ALT, COLLISION_LAYER);
            if (legacyCollisionLayer != null && legacyCollisionLayer != solidLayer) {
                cacheExitDataFromLayer(legacyCollisionLayer);
            }
        }
    }

    private MapLayer getFirstAvailableLayer(String... layerNames) {
        for (String layerName : layerNames) {
            if (layerName == null || layerName.isBlank()) continue;
            MapLayer layer = map.getLayers().get(layerName);
            if (layer != null) return layer;
        }
        return null;
    }

    private void cacheBlockingAndDoorDataFromLayer(MapLayer layer) {
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            CollisionShape shape = buildCollisionShape(object);
            if (shape == null) continue;

            if (isExitCollision(object)) {
                exitAreas.add(shape);
                continue;
            }
            if (isDoor(object)) {
                DoorArea door = new DoorArea();
                door.collisionShape = shape;
                door.isExitDoor = isExitDoor(object);
                door.unlocked = false;
                door.lateral = isLateralDoor(object);
                doorAreas.add(door);
                continue;
            }
            staticCollisionAreas.add(shape);
        }
    }

    private void cacheExitDataFromLayer(MapLayer layer) {
        if (layer == null) return;
        for (MapObject object : layer.getObjects()) {
            if (!isExitCollision(object)) continue;
            CollisionShape shape = buildCollisionShape(object);
            if (shape != null) {
                exitAreas.add(shape);
            }
        }
    }

    private CollisionShape buildCollisionShape(MapObject object) {
        if (object instanceof PolygonMapObject polygonMapObject) {
            return buildCollisionShapeFromPolygon(polygonMapObject.getPolygon());
        }
        if (object instanceof RectangleMapObject rectangleMapObject) {
            // Compatibilidad por si queda algun rectangulo en mapas viejos.
            return buildCollisionShapeFromRectangle(rectangleMapObject.getRectangle());
        }
        return null;
    }

    private CollisionShape buildCollisionShapeFromRectangle(Rectangle rectangle) {
        if (rectangle == null) return null;

        CollisionShape shape = new CollisionShape();
        shape.bounds = new Rectangle(rectangle);
        shape.convexParts.add(new Polygon(new float[]{
            rectangle.x, rectangle.y,
            rectangle.x + rectangle.width, rectangle.y,
            rectangle.x + rectangle.width, rectangle.y + rectangle.height,
            rectangle.x, rectangle.y + rectangle.height
        }));
        return shape;
    }

    private CollisionShape buildCollisionShapeFromPolygon(Polygon polygon) {
        if (polygon == null) return null;

        float[] vertices = polygon.getTransformedVertices();
        if (vertices == null || vertices.length < 6) return null;

        CollisionShape shape = new CollisionShape();
        shape.bounds = new Rectangle(polygon.getBoundingRectangle());

        ShortArray triangles = triangulator.computeTriangles(vertices);
        for (int i = 0; i + 2 < triangles.size; i += 3) {
            int i0 = triangles.get(i) * 2;
            int i1 = triangles.get(i + 1) * 2;
            int i2 = triangles.get(i + 2) * 2;

            shape.convexParts.add(new Polygon(new float[]{
                vertices[i0], vertices[i0 + 1],
                vertices[i1], vertices[i1 + 1],
                vertices[i2], vertices[i2 + 1]
            }));
        }

        if (shape.convexParts.size == 0) return null;
        return shape;
    }

    private boolean isBlockedByStaticCollision(Polygon dynamicHitbox, Rectangle dynamicBounds) {
        for (CollisionShape area : staticCollisionAreas) {
            if (isOverlapping(dynamicHitbox, dynamicBounds, area)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverlapping(Polygon dynamicHitbox, Rectangle dynamicBounds, CollisionShape shape) {
        if (shape == null) return false;
        if (!shape.bounds.overlaps(dynamicBounds)) return false;

        for (Polygon convexPart : shape.convexParts) {
            if (Intersector.overlapConvexPolygons(dynamicHitbox, convexPart)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDoor(MapObject object) {
        String name = object.getName();
        String clazz = readObjectClass(object);
        String type = object.getProperties().get("type", String.class);
        Object requiresKeyRaw = object.getProperties().get("requiresKey");
        String doorId = object.getProperties().get("doorID", String.class);

        return CLASS_DOOR.equalsIgnoreCase(clazz)
            || DOOR_TAG.equalsIgnoreCase(name)
            || DOOR_TAG_ALT.equalsIgnoreCase(name)
            || DOOR_TAG.equalsIgnoreCase(clazz)
            || DOOR_TAG_ALT.equalsIgnoreCase(clazz)
            || DOOR_TAG.equalsIgnoreCase(type)
            || DOOR_TAG_ALT.equalsIgnoreCase(type)
            || "true".equalsIgnoreCase(String.valueOf(requiresKeyRaw))
            || (doorId != null && !doorId.isBlank());
    }

    private boolean isExitDoor(MapObject object) {
        String name = object.getName();
        String doorId = object.getProperties().get("doorID", String.class);

        return EXIT_DOOR_NAME.equalsIgnoreCase(name)
            || EXIT_DOOR_ID.equalsIgnoreCase(doorId);
    }

    private boolean isExitCollision(MapObject object) {
        Object idRaw = object.getProperties().get("id");
        String name = object.getName();
        String clazz = readObjectClass(object);
        String type = object.getProperties().get("type", String.class);

        return CLASS_EXIT.equalsIgnoreCase(clazz)
            || EXIT_COLLISION_ID.equalsIgnoreCase(String.valueOf(idRaw))
            || EXIT_COLLISION_ID.equalsIgnoreCase(name)
            || EXIT_COLLISION_NAME.equalsIgnoreCase(name)
            || EXIT_COLLISION_ID.equalsIgnoreCase(clazz)
            || EXIT_COLLISION_ID.equalsIgnoreCase(type);
    }

    private boolean isLateralDoor(MapObject object) {
        if (object == null) return false;
        if (!object.getProperties().containsKey("lateral")) return false;

        String lateralValue = object.getProperties().get("lateral", String.class);
        if (lateralValue == null || lateralValue.isBlank()) {
            return true;
        }
        return "true".equalsIgnoreCase(lateralValue)
            || "1".equals(lateralValue)
            || "yes".equalsIgnoreCase(lateralValue);
    }

    private String readObjectClass(MapObject object) {
        String clazz = object.getProperties().get(CLASS_PROPERTY, String.class);
        if (clazz != null && !clazz.isBlank()) return clazz;
        // Compatibilidad con mapas viejos donde se usaba type.
        return object.getProperties().get("type", String.class);
    }

    private void loadLevelByIndex(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= LEVEL_MAPS.length) {
            throw new IllegalArgumentException("Invalid level index: " + levelIndex);
        }

        disposeCurrentMap();
        map = new TmxMapLoader().load(LEVEL_MAPS[levelIndex], mapLoaderParameters);
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        currentLevelIndex = levelIndex;
        doorUnlockedThisStep = false;
        cacheCollisionData();
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
    }

    @Override
    public void dispose() {
        disposeCurrentMap();
        staticCollisionAreas.clear();
        doorAreas.clear();
        exitAreas.clear();
        lockedDoorBounds.clear();
        lockedLateralDoorBounds.clear();
    }
}
