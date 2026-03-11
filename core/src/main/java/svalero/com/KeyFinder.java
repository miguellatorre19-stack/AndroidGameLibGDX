package svalero.com;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import svalero.com.managers.ResourceManager;
import svalero.com.screens.MainMenuScreen;

public class KeyFinder extends Game {

    public FitViewport viewport;
    public BitmapFont font;

    public static AssetManager manager = new AssetManager();

    // create() method defines all assets and allocates the memory to them
    @Override
    public void create(){
        font = new BitmapFont();
        viewport = new FitViewport(8 ,5);

        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());
        //font has 15pt, but we need to scale it to our viewport by ratio of viewport height to screen height

        this.setScreen(new MainMenuScreen(this));

    }

    @Override
    public void render (){
        if(manager.update())
        super.render();
    }

    @Override
    public void dispose(){
        font.dispose();
    }

}
