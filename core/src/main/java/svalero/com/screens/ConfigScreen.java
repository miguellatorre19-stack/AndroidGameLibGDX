package svalero.com.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import svalero.com.KeyFinder;
import svalero.com.managers.ResourceManager;
import svalero.com.utils.Constants;

public class ConfigScreen implements Screen {

    private final KeyFinder game;
    private Stage stage;
    private Preferences prefs;
    private String[] resolutions = new String[]{"1920x1080","1280x720","720x576"};
    private String selectedResolution;

    public ConfigScreen(KeyFinder game){
        this.game = game;
    }

    private void setFullScreenWindow(){
        Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
        Gdx.graphics.setFullscreenMode(mode);
    }

    private void setWindowMode(){
        setWindowMode(selectedResolution);
    }

    private void setWindowMode(String resolution){
        int width = Constants.SCREEN_WIDTH;
        int height = Constants.SCREEN_HEIGHT;
        if (resolution != null && resolution.contains("x")) {
            String[] parts = resolution.split("x");
            if (parts.length == 2) {
                try {
                    width = Integer.parseInt(parts[0].trim());
                    height = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ignored) {
                    // Falls back to default constants.
                }
            }
        }
        Gdx.graphics.setWindowedMode(width, height);
    }

    private String resolveInitialResolution() {
        String savedResolution = prefs.getString("resolution", "");
        for (String resolution : resolutions) {
            if (resolution.equals(savedResolution)) {
                return resolution;
            }
        }
        String currentResolution = Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();
        for (String resolution : resolutions) {
            if (resolution.equals(currentResolution)) {
                return resolution;
            }
        }
        return resolutions[0];
    }

    private void buildUi() {
        stage = new Stage();

        VisTable table = new VisTable(true);
        table.setFillParent(true);
        stage.addActor(table);

        VisTextButton resumeButton = new VisTextButton("BACK");
        resumeButton.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y){
                if (game != null) {
                    game.setScreen(new MainMenuScreen(game));
                }
            }
        });

        VisCheckBox fullScreenButton = new VisCheckBox("Full Screen");
        fullScreenButton.setChecked(Gdx.graphics.isFullscreen());

        selectedResolution = resolveInitialResolution();
        VisSelectBox<String> resolutionSelectBox = new VisSelectBox<>();
        resolutionSelectBox.setItems(resolutions);
        resolutionSelectBox.setSelected(selectedResolution);
        resolutionSelectBox.setDisabled(Gdx.graphics.isFullscreen());
        resolutionSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                selectedResolution = resolutionSelectBox.getSelected();
                prefs.putString("resolution", selectedResolution);
                prefs.flush();
                if (!Gdx.graphics.isFullscreen()) {
                    setWindowMode(selectedResolution);
                    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }
            }
        });

        fullScreenButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (Gdx.graphics.isFullscreen()) {
                    setWindowMode(selectedResolution);
                    resolutionSelectBox.setDisabled(false);
                } else {
                    setFullScreenWindow();
                    resolutionSelectBox.setDisabled(true);
                }
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                fullScreenButton.setChecked(Gdx.graphics.isFullscreen());
            }
        });

        VisCheckBox displayDataButton = new VisCheckBox("Show HUD");
        displayDataButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int FPS = Gdx.graphics.getFramesPerSecond();
            }
        });

        VisLabel resolutionLabel = new VisLabel("ML");

        table.row();
        table.add(resumeButton).center().width(200).height(100).pad(5);
        table.row();
        table.add(fullScreenButton).left().width(100).height(50).pad(10);
        table.add(displayDataButton).right().width(100).height(50).pad(10);
        table.row();
        table.add(resolutionLabel).left().pad(10);
        table.add(resolutionSelectBox).right().width(180).pad(10);
    }

    private void loadPreferences() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("keyfinder-config");
        }
    }

    @Override
    public void show() {
        if (!VisUI.isLoaded()) {
            VisUI.load();
        }
        loadPreferences();
        if (stage == null) {
            buildUi();
        }
        Gdx.input.setInputProcessor(stage);
        ResourceManager.loadAllResources();

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
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
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
        dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        VisUI.dispose();
    }
}
