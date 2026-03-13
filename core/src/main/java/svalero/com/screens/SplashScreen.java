package svalero.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import svalero.com.KeyFinder;
import svalero.com.managers.ResourceManager;

import java.awt.*;


public class SplashScreen implements Screen {

    private Texture splashTexture;
    private Image splashImage;
    private Stage stage;
    private boolean splashDone = false;
    private KeyFinder game;

    public SplashScreen(){
        splashTexture = new Texture(Gdx.files.internal("ui/key_splash_logo.png"));
        splashImage = new Image(splashTexture);
    }

    @Override
    public void show() {
        stage = new Stage();
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        splashImage.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(1f),
            Actions.delay(1.5f), Actions.run(new Runnable() {
                @Override
                public void run() {
                    splashDone = true;
                }
            })
        ));

        table.row().height(splashTexture.getHeight());
        table.add(splashImage).center();
        stage.addActor(table);

        ResourceManager.loadAllResources();
    }

    @Override
    public void render(float delta) {

        stage.act(delta);
        stage.draw();

        // Comprueba si se han cargado todos los recursos
        if (ResourceManager.update()) {
            // Si la animación ha terminado se muestra ya el menú principal
            if (splashDone) {
                ((Game) Gdx.app.getApplicationListener()).setScreen(new ConfigScreen());
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // Redimensiona la escena al redimensionar la ventana del juego
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
        splashTexture.dispose();
        stage.dispose();
    }
}
