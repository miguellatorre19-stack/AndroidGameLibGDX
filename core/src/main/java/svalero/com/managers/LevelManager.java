package svalero.com.managers;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.net.IDN;

//se encarga de la carga de niveles (pantallas jugables) y de todos los elementos de los mismos(objetos, enemigos, mapa)
public class LevelManager {

    private TiledMap map;
    private TiledMapTileLayer floor;
    private TiledMapTileLayer walls;
    private TiledMapTileLayer elements;
    private MapLayer colisionLayer;
    private MapLayer objectLayer;
    private MapRenderer mapRenderer;

    int currentlevel;

    public void loadCurrentLevel(){
      mapRenderer.render();
    }

    public void restartCurrentLevel(){

    }

    public void loadColisionLayer(){
        int [] colisionLayer = {5};
        mapRenderer.render(colisionLayer);
    }

    public void loadItems(){

    }


}
