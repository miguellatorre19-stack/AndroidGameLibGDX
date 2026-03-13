package svalero.com.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import svalero.com.KeyFinder;
import svalero.com.managers.RenderManager;

public class MainMenuScreen implements Screen {

    private final KeyFinder game;
    private Stage stage;

    public RenderManager renderManager;

    public MainMenuScreen(KeyFinder game) {
        this.game = game;
    }

    public void buildUI(){

        stage = new Stage();

        VisTable table = new VisTable(true);
        table.setFillParent(true);
        stage.addActor(table);

        VisTextButton playButton = new VisTextButton("PLAY");
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
                // Ir a jugar
                dispose();
            }
        });

        VisTextButton configurationButton = new VisTextButton("CONFIG");
        configurationButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ConfigScreen(game));
            }
        });

        VisTextButton quitButton = new VisTextButton("QUIT");
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
                dispose();
            }
        });

        table.row();
        table.add(playButton).left().height(100).width(100).pad(10);
        table.add(configurationButton).center().height(100).width(100).pad(10);
        table.add(quitButton).right().height(100).width(100).pad(10);
    }

    @Override
    public void show() {
        if (!VisUI.isLoaded())
            VisUI.load();

        buildUI();
        Gdx.input.setInputProcessor(stage);

    }

    // we need to call game’s SpriteBatch and BitmapFont instances instead of creating our own.
    @Override
    public void render(float v) {
        ScreenUtils.clear((Color.BLACK));

//        renderManager.batch = new SpriteBatch();
//
//        renderManager.batch.begin();
//
//        game.font.draw(renderManager.batch, "Welcome to KeyFinder!!! ", 1, 1.5f);
//        game.font.draw(renderManager.batch, "Tap anywhere to begin!", 1, 1);
//
//        renderManager.batch.end();
        stage.act(v);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);

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
        VisUI.dispose();
        stage.dispose();
    }
}
