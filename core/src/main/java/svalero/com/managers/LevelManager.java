package svalero.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;

//se encarga de la carga de niveles (pantallas jugables) y de todos los elementos de los mismos(objetos, enemigos, mapa)
public class LevelManager {

    private TiledMap map;
    private MapLayer colisionLayer;
    private MapLayer objectLayer;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Matrix4 projection = new Matrix4();
    private SpriteManager spriteManager;
    private Batch batch;

    int currentlevel;

    public LevelManager(){
        map = new TmxMapLoader().load("levels/maps/first_level_tutorial.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        batch = mapRenderer.getBatch();
        currentlevel = 1;
    }

    public void loadCurrentLevel(){
        if (currentlevel == 1){
            float w = Gdx.graphics.getWidth();
            float h = Gdx.graphics.getHeight();
            projection.setToOrtho2D(0, 0, w, h);
            mapRenderer.setView(projection, 0, 0, w, h);
            mapRenderer.render(new int []{0,1,2,3,4});
            loadColisionLayer();
        }
    }

    public void restartCurrentLevel(){
        loadCurrentLevel();
    }

    public void loadColisionLayer(){
        int [] colisionLayer = {5};
        mapRenderer.render(colisionLayer);
    }

    public void loadItems(){

    }


}
