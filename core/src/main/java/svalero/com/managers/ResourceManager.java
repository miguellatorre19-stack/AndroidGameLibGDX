package svalero.com.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

//contiene el código que permite realizar la carga y el acceso a todos los recursos (assets). métodos sincronos y asincronos
public class ResourceManager {
    public static AssetManager manager = new AssetManager();
    // the AssetManager needs to know how to load a specific type of asset. This functionality is implemented via AssetLoaders.


    TiledMap map = new TmxMapLoader().load("");


    public static void loadAllResources(){
        manager.load("Texture_Atlas/Dungeon_tiles.atlas", TextureAtlas.class);
    }
    //These calls will enqueue those assets for loading. We only queued assets to be loaded. The AssetManager does not yet load anything.

    public static boolean update(){
        return manager.update();
    }

    //Obtiene una región de textura o la primera de una animación
    public static TextureRegion getRegion(String name){
        return manager.get("Texture_Atlas/Dungeon_tiles.atlas", TextureAtlas.class).findRegion(name);
    }
}
