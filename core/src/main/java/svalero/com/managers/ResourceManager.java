package svalero.com.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

//contiene el código que permite realizar la carga y el acceso a todos los recursos (assets). métodos sincronos y asincronos
public class ResourceManager {
    public static final String GENERAL_ATLAS_PATH = "Texture_Atlas/General_atlas.pack";
    public static final String MAP_KEY_TEXTURE_PATH = "interactables/items and trap_animation/keys/keys_1_1.png";

    public static AssetManager manager = new AssetManager();
    // the AssetManager needs to know how to load a specific type of asset. This functionality is implemented via AssetLoaders.

    public static BitmapFont font;

    //These calls will enqueue those assets for loading. We only queued assets to be loaded. The AssetManager does not yet load anything.
    public static void loadAllResources(){
        if (!manager.isLoaded(GENERAL_ATLAS_PATH, TextureAtlas.class)) {
            manager.load(GENERAL_ATLAS_PATH, TextureAtlas.class);
        }
        if (!manager.isLoaded(MAP_KEY_TEXTURE_PATH, Texture.class)) {
            manager.load(MAP_KEY_TEXTURE_PATH, Texture.class);
        }
        if (font == null) {
            font = new BitmapFont();
            font.setUseIntegerPositions(false);
        }

        //font has 15pt, but we need to scale it to our viewport by ratio of viewport height to screen height
    }

    public static void finishLoadingResources() {
        manager.finishLoading();
    }

    public static TextureAtlas getGeneralAtlas() {
        return manager.get(GENERAL_ATLAS_PATH, TextureAtlas.class);
    }

    //Obtiene todas las regiones de textura que forman una misma animación
    public static Array<TextureAtlas.AtlasRegion> getRegions(String name){
        return getGeneralAtlas().findRegions(name);
    }

    public static Texture getTexture(String path) {
        return manager.get(path, Texture.class);
    }

    public static Animation<TextureRegion> buildIndexedAnimation(
        String regionName,
        float frameDuration,
        Animation.PlayMode playMode
    ) {
        Array<TextureAtlas.AtlasRegion> atlasRegions = getRegions(regionName);
        if (atlasRegions == null || atlasRegions.size == 0) {
            throw new IllegalArgumentException("No indexed regions found for: " + regionName);
        }

        Array<TextureRegion> frames = new Array<>(atlasRegions.size);
        for (TextureAtlas.AtlasRegion atlasRegion : atlasRegions) {
            frames.add(atlasRegion);
        }
        return new Animation<>(frameDuration, frames, playMode);
    }

    public static boolean update(){
        return manager.update();
    }



    //Obtiene una región de textura o la primera de una animación

}
