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

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private int currentlevel;
    private final Array<Rectangle> wallColisions;

    public LevelManager(KeyFinder game){
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.textureMinFilter = Texture.TextureFilter.Nearest;
        parameters.textureMagFilter = Texture.TextureFilter.Nearest;

        map = new TmxMapLoader().load("levels/maps/first_level_tutorial.tmx", parameters);
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        currentlevel = 1;
        wallColisions = new Array<>();
        storeColisions();
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

    public void storeColisions(){
        MapLayer colisionLayer =map.getLayers().get("colisions");

        for (MapObject colisions :  colisionLayer.getObjects()){
            if (colisions instanceof  RectangleMapObject rectangleMapObject){
                Rectangle rectangle = new Rectangle(rectangleMapObject.getRectangle());
                wallColisions.add(rectangle);
            }
        }
    }

    public boolean isBlocked(Rectangle playerBounds){
        if(playerBounds== null){
            return false;
        }
        for (Rectangle colisionRectangle : wallColisions){
            if(colisionRectangle.overlaps(playerBounds)){
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


}
