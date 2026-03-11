package svalero.com.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class ResourceManager {
    public static AssetManager manager = new AssetManager();
    // the AssetManager needs to know how to load a specific type of asset. This functionality is implemented via AssetLoaders.


    public static void loadAllResources(){
        manager.load("assets/Texture_Atlas/Dungeon_tiles.atlas", TextureAtlas.class);
    }


    public static boolean update(){
        return manager.update();
    }

    //Obtiene una región de textura o la primera de una animación
    public static TextureRegion getRegion(String name){
        return manager.get("assets/Texture_Atlas/Dungeon_tiles.atlas", TextureAtlas.class).findRegion(name);
    }
}
