package svalero.com;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import svalero.com.screens.SplashScreen;

public class KeyFinder extends Game {

    public SpriteBatch batch;
    public BitmapFont font;

    public static AssetManager manager = new AssetManager();

    // create() method defines all assets and allocates the memory to them
    @Override
    public void create(){
        ((Game) Gdx.app.getApplicationListener()).setScreen(new SplashScreen());

        batch = new SpriteBatch();
        font = new BitmapFont();

        font.setUseIntegerPositions(false);
        //font has 15pt, but we need to scale it to our viewport by ratio of viewport height to screen height
    }

    @Override
    public void render (){
        manager.update();
        super.render();
    }

    @Override
    public void dispose(){
        font.dispose();
    }

}
