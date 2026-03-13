package svalero.com.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import svalero.com.KeyFinder;
import svalero.com.managers.RenderManager;

public class MainMenuScreen implements Screen {

    final KeyFinder game;

    public RenderManager renderManager;

    //he Screen interface does not provide any sort of create() method, so we instead use a constructor.
    // The only parameter for the constructor necessary for this game is an instance of Drop, so that we can call upon its methods and fields if necessary.
     public MainMenuScreen(final KeyFinder game){
         this.game = game;
     }

    @Override
    public void show() {

    }

    // we need to call game’s SpriteBatch and BitmapFont instances instead of creating our own.
    @Override
    public void render(float v) {
        ScreenUtils.clear((Color.BLACK));



        renderManager.batch.begin();


        game.font.draw(renderManager.batch, "Welcome to KeyFinder!!! ", 1, 1.5f);
        game.font.draw(renderManager.batch, "Tap anywhere to begin!", 1, 1);

        renderManager.batch.end();

        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
            dispose();
        }

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
