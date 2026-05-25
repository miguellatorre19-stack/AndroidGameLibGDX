package svalero.com;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import svalero.com.managers.AudioManager;
import svalero.com.screens.SplashScreen;

public class KeyFinder extends Game {

    public SpriteBatch batch;
    public TextureAtlas atlas;

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public AudioManager audioManager;

    public static AssetManager manager = new AssetManager();

    // create() method defines all assets and allocates the memory to them
    @Override
    public void create(){
        batch = new SpriteBatch();
        atlas = new TextureAtlas(Gdx.files.internal("Texture_Atlas/General_atlas.pack"));
        setScreen(new SplashScreen(this));
        //font has 15pt, but we need to scale it to our viewport by ratio of viewport height to screen height
    }

    @Override
    public void render (){
        manager.update();
        super.render();
    }

    @Override
    public void dispose(){
        if (atlas != null) {
            atlas.dispose();
            atlas = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }

}
