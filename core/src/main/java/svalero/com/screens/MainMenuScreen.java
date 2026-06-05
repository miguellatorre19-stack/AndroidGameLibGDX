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
import com.kotcrab.vis.ui.widget.VisTextArea;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import svalero.com.KeyFinder;
import svalero.com.managers.AudioManager;
import svalero.com.managers.ResourceManager;

public class MainMenuScreen implements Screen {

    private final KeyFinder game;
    private Stage stage;
    private AudioManager audioManager;

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
                playUiClick();
                game.setScreen(new GameScreen(game));
                // Ir a jugar
                dispose();
            }
        });

        VisTextButton configurationButton = new VisTextButton("CONFIG");
        configurationButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playUiClick();
                game.setScreen(new ConfigScreen(game));
                dispose();
            }
        });

        VisTextButton quitButton = new VisTextButton("QUIT");
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playUiClick();
                Gdx.app.exit();
                dispose();
            }
        });

        VisTextButton instructionsButton = new VisTextButton("INFO");
        instructionsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playUiClick();
                VisWindow infoWindow = new VisWindow("INFO");

                VisTextArea infoText = new VisTextArea(
                    "Bienvenido a KeyFinder.\n" +
                    "Recoge llaves para abrir puertas y avanza hasta la salida.\n" +
                        "¡Cuidado con los enemigos!"
                );
                infoText.getStyle().fontColor = Color.WHITE;
                infoText.getStyle().disabledFontColor = Color.WHITE;
                infoText.setDisabled(true);

                VisTextButton closeButton = new VisTextButton("CERRAR");
                closeButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        playUiClick();
                        infoWindow.remove();
                    }
                });

                infoWindow.add(infoText).width(420).height(120).pad(12);
                infoWindow.row();
                infoWindow.add(closeButton).padBottom(12);
                infoWindow.pack();
                infoWindow.setPosition(
                    (stage.getWidth() - infoWindow.getWidth()) * 0.5f,
                    (stage.getHeight() - infoWindow.getHeight()) * 0.5f
                );
                stage.addActor(infoWindow);
            }
        });

        table.row();
        table.add(playButton).left().height(100).width(100).pad(10);
        table.add(configurationButton).center().height(100).width(100).pad(10);
        table.add(quitButton).right().height(100).width(100).pad(10);
        table.row();
        table.add(instructionsButton).colspan(3).center().height(80).width(120).padTop(8);
    }

    @Override
    public void show() {
        if (!VisUI.isLoaded())
            VisUI.load();
        audioManager = new AudioManager();
        buildUI();
        Gdx.input.setInputProcessor(stage);
        ResourceManager.loadAllResources();
        audioManager.loadMusic("music_title", "audio/music/xDeviruchi - Title Theme .wav");
        audioManager.loadSfx("interface1", "audio/sound/interface1.mp3");
        audioManager.playMusic("music_title", true);
        audioManager.setMusicEnabled(true);
    }

    private void playUiClick() {
        if (audioManager != null) {
            audioManager.playSfx("interface1");
        }
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
        stage.getViewport().update(width, height, true);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        if (audioManager != null) {
            audioManager.stopMusic();
        }
    }

    @Override
    public void dispose() {
        if (audioManager != null) {
            audioManager.dispose();
            audioManager = null;
        }
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
    }
}
