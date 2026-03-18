package svalero.com.managers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import svalero.com.KeyFinder;

//se encarga de la carga de niveles (pantallas jugables) y de todos los elementos de los mismos(objetos, enemigos, mapa)
public class LevelManager {

    private static final String COLLISION_LAYER = "colisions";
    private static final String COLLISION_LAYER_ALT = "collisions";
    private static final String DOOR_TAG = "puertas";
    private static final String DOOR_TAG_ALT = "door";
    private static final String EXIT_DOOR_NAME = "door3";
    private static final String EXIT_DOOR_ID = "third_door";

    private static class CollisionArea {
        Rectangle bounds;
        boolean isDoor;
        boolean isExitDoor;
        boolean unlocked;
    }

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Array<CollisionArea> collisionAreas;
    private boolean doorUnlockedThisStep;
    private int currentlevel;

    public LevelManager(KeyFinder game){
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.textureMinFilter = Texture.TextureFilter.Nearest;
        parameters.textureMagFilter = Texture.TextureFilter.Nearest;

        map = new TmxMapLoader().load("levels/maps/first_level_tutorial.tmx", parameters);
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        collisionAreas = new Array<>();
        doorUnlockedThisStep = false;
        cacheCollisionAreas();
        currentlevel = 1;
    }

    public OrthogonalTiledMapRenderer getMapRenderer(){
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

    public void loadCurrentLevel(OrthographicCamera camera){
        if (camera == null) return;

        if (currentlevel == 1){
            mapRenderer.setView(camera);
            mapRenderer.render();
        }
    }

    public boolean isBlocked(Rectangle playerBounds) {
        return isBlocked(playerBounds, false);
    }

    public boolean isBlocked(Rectangle playerBounds, boolean hasKey){
        doorUnlockedThisStep = false;

        if (playerBounds == null) {
            return false;
        }

        for (CollisionArea area : collisionAreas) {
            if (area.unlocked) continue;
            if (!area.bounds.overlaps(playerBounds)) continue;

            if (area.isDoor && hasKey) {
                area.unlocked = true;
                doorUnlockedThisStep = true;
                continue;
            }
            if (!area.isDoor) {
                return true;
            }
            if (area.isDoor && !area.unlocked) {
                return true;
            }
        }
        return false;
    }

    public boolean consumeDoorUnlockEvent() {
        boolean unlocked = doorUnlockedThisStep;
        doorUnlockedThisStep = false;
        return unlocked;
    }

    public boolean isAtLevelExit(Rectangle playerBounds) {
        if (playerBounds == null) return false;

        for (CollisionArea area : collisionAreas) {
            if (!area.isExitDoor) continue;
            if (!area.unlocked) continue;
            if (area.bounds.overlaps(playerBounds)) {
                return true;
            }
        }
        return false;
    }

    public void restartCurrentLevel(OrthographicCamera camera){
        loadCurrentLevel(camera);
    }

    public void loadItems(){

    }

    private void cacheCollisionAreas() {
        collisionAreas.clear();

        MapLayer collisionLayer = map.getLayers().get(COLLISION_LAYER);
        if (collisionLayer == null) {
            collisionLayer = map.getLayers().get(COLLISION_LAYER_ALT);
        }
        if (collisionLayer == null) return;

        for (MapObject object : collisionLayer.getObjects()) {
            if (!(object instanceof RectangleMapObject rectangleMapObject)) continue;

            CollisionArea area = new CollisionArea();
            area.bounds = new Rectangle(rectangleMapObject.getRectangle());
            area.isDoor = isDoor(object);
            area.isExitDoor = isExitDoor(object);
            area.unlocked = false;
            collisionAreas.add(area);
        }
    }

    private boolean isDoor(MapObject object) {
        String name = object.getName();
        String clazz = object.getProperties().get("class", String.class);
        String type = object.getProperties().get("type", String.class);
        Object requiresKeyRaw = object.getProperties().get("requiresKey");
        String doorId = object.getProperties().get("doorID", String.class);

        return DOOR_TAG.equalsIgnoreCase(name)
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

}
