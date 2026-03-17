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
import svalero.com.KeyFinder;

//se encarga de la carga de niveles (pantallas jugables) y de todos los elementos de los mismos(objetos, enemigos, mapa)
public class LevelManager {

    private static final String COLLISION_LAYER = "colisions";
    private static final String COLLISION_LAYER_ALT = "collisions";
    private static final String DOOR_TAG = "puertas";

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapLayer collisionLayer;
    private int currentlevel;

    public LevelManager(KeyFinder game){
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.textureMinFilter = Texture.TextureFilter.Nearest;
        parameters.textureMagFilter = Texture.TextureFilter.Nearest;

        map = new TmxMapLoader().load("levels/maps/first_level_tutorial.tmx", parameters);
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        collisionLayer = map.getLayers().get(COLLISION_LAYER);
        if (collisionLayer == null) {
            collisionLayer = map.getLayers().get(COLLISION_LAYER_ALT);
        }
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
        if (playerBounds == null || collisionLayer == null) {
            return false;
        }

        for (MapObject object : collisionLayer.getObjects()) {
            if (!(object instanceof RectangleMapObject rectangleMapObject)) continue;

            if (isDoor(object) && hasKey) continue;

            if (rectangleMapObject.getRectangle().overlaps(playerBounds)) {
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

    private boolean isDoor(MapObject object) {
        String name = object.getName();
        String clazz = object.getProperties().get("class", String.class);
        String type = object.getProperties().get("type", String.class);

        return DOOR_TAG.equalsIgnoreCase(name)
            || DOOR_TAG.equalsIgnoreCase(clazz)
            || DOOR_TAG.equalsIgnoreCase(type);
    }

}
