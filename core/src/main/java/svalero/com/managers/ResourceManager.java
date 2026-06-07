package svalero.com.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

// Carga y acceso centralizado a recursos gráficos.
public final class ResourceManager {
    public static final String GENERAL_ATLAS_ID = "general";
    public static final String ITEMS_ATLAS_ID = "items";
    public static final String SKILLS_ATLAS_ID = "skills";
    public static final String SCENE_INTERACTIONS_ID = "chest";
    public static final String HUD_INTERACTIONS_ID = "green";
    public static final String GENERAL_ATLAS_PATH = "Texture_Atlas/General_atlas.pack";
    public static final String ITEMS_ATLAS_PATH = "Texture_Atlas/items.atlas";
    public static final String SKILLS_ATLAS_PATH = "Texture_Atlas/skills.atlas";
    public static final String SCENE_INTERACTIONS_PATH = "Texture_Atlas/scene_interactions.atlas";
    public static final String HUD_INTERACTIONS_PATH = "Texture_Atlas/hud.atlas";

    private static final AssetManager ASSET_MANAGER = new AssetManager();
    private static final ObjectMap<String, String> ATLAS_PATHS = new ObjectMap<>();

    static {
        ATLAS_PATHS.put(GENERAL_ATLAS_ID, GENERAL_ATLAS_PATH);
        ATLAS_PATHS.put(ITEMS_ATLAS_ID, ITEMS_ATLAS_PATH);
        ATLAS_PATHS.put(SKILLS_ATLAS_ID, SKILLS_ATLAS_PATH);
        ATLAS_PATHS.put( SCENE_INTERACTIONS_ID,SCENE_INTERACTIONS_PATH);
        ATLAS_PATHS.put(HUD_INTERACTIONS_ID, HUD_INTERACTIONS_PATH);
    }

    // ----- Registro y carga -----

    public static void loadAtlas(String atlasId, String atlasPath) {
        ATLAS_PATHS.put(atlasId, atlasPath);
        enqueueAtlas(atlasId);
    }

    public static void loadAllResources() {
        for (String atlasId : ATLAS_PATHS.keys()) {
            enqueueAtlas(atlasId);
        }
    }

    public static void finishLoadingResources() {
        ASSET_MANAGER.finishLoading();
    }

    public static boolean update() {
        return ASSET_MANAGER.update();
    }

    // ----- Acceso -----

    public static TextureAtlas getAtlas(String atlasId) {
        return ASSET_MANAGER.get(getAtlasPathOrThrow(atlasId), TextureAtlas.class);
    }

    public static Array<TextureAtlas.AtlasRegion> getRegions(String atlasId, String regionName) {
        return getAtlas(atlasId).findRegions(regionName);
    }

    public static TextureRegion getRegion(String atlasId, String regionName) {
        return getAtlas(atlasId).findRegion(regionName);
    }

    // ----- Animaciones -----

    public static Animation<TextureRegion> buildIndexedAnimation(
        String atlasId,
        String regionName,
        float frameDuration,
        Animation.PlayMode playMode
    ) {
        Array<TextureAtlas.AtlasRegion> atlasRegions = getRegions(atlasId, regionName);
        if (atlasRegions == null || atlasRegions.size == 0) {
            throw new IllegalArgumentException("No indexed regions found for atlas/id: " + atlasId + "/" + regionName);
        }

        Array<TextureRegion> frames = new Array<>(atlasRegions.size);
        for (TextureAtlas.AtlasRegion atlasRegion : atlasRegions) {
            frames.add(atlasRegion);
        }
        return new Animation<>(frameDuration, frames, playMode);
    }

    // ----- Internals -----

    private static void enqueueAtlas(String atlasId) {
        String atlasPath = getAtlasPathOrThrow(atlasId);
        if (!ASSET_MANAGER.isLoaded(atlasPath, TextureAtlas.class) && !ASSET_MANAGER.contains(atlasPath)) {
            ASSET_MANAGER.load(atlasPath, TextureAtlas.class);
        }
    }

    private static String getAtlasPathOrThrow(String atlasId) {
        String path = ATLAS_PATHS.get(atlasId);
        if (path == null) {
            throw new IllegalArgumentException("Atlas id not registered: " + atlasId);
        }
        return path;
    }
}
