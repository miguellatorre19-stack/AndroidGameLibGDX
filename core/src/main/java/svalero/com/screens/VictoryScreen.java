package svalero.com.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import svalero.com.KeyFinder;
import svalero.com.managers.AudioManager;
import svalero.com.managers.HighScoreManager;
import svalero.com.managers.ResourceManager;

import java.util.List;

public class VictoryScreen implements Screen {
    private static final int COIN_POINTS = 100;
    private static final int KEY_POINTS = 250;
    private static final int LIFE_POINTS = 500;

    private final KeyFinder game;
    private final int coins;
    private final int keys;
    private final int lives;
    private final int finalScore;
    private final String rank;

    private Stage stage;
    private AudioManager audioManager;
    private HighScoreManager.HighScoreRegistration highScoreRegistration;

    public VictoryScreen(KeyFinder game, int coins, int keys, int lives) {
        this.game = game;
        this.coins = Math.max(0, coins);
        this.keys = Math.max(0, keys);
        this.lives = Math.max(0, lives);
        this.finalScore = calculateScore(this.coins, this.keys, this.lives);
        this.rank = resolveRank(finalScore);
    }

    @Override
    public void show() {
        if (!VisUI.isLoaded()) {
            VisUI.load();
        }

        audioManager = new AudioManager();
        highScoreRegistration = new HighScoreManager().registerScore(finalScore);
        buildUi();
        Gdx.input.setInputProcessor(stage);
        ResourceManager.loadAllResources();
        audioManager.loadMusic("music_title", "audio/music/xDeviruchi - Title Theme .wav");
        audioManager.loadSfx("interface1", "audio/sound/interface1.mp3");
        audioManager.playMusic("music_title", true);
        audioManager.setMusicEnabled(true);
    }

    private void buildUi() {
        stage = new Stage();

        VisTable root = new VisTable(true);
        root.setFillParent(true);
        stage.addActor(root);

        VisWindow resultsWindow = new VisWindow("VICTORY");
        resultsWindow.setMovable(false);
        resultsWindow.pad(16f);

        VisLabel titleLabel = new VisLabel("FINAL SCORE");
        VisLabel rankLabel = new VisLabel("RANK  " + rank);
        VisLabel scoreLabel = new VisLabel(String.format("%05d", finalScore));
        VisLabel top10Label = new VisLabel(resolveTop10Headline());

        VisLabel coinsLabel = new VisLabel(String.format("Coins: %02d   x %03d = %04d", coins, COIN_POINTS, coins * COIN_POINTS));
        VisLabel keysLabel = new VisLabel(String.format("Keys:  %02d   x %03d = %04d", keys, KEY_POINTS, keys * KEY_POINTS));
        VisLabel livesLabel = new VisLabel(String.format("Lives: %02d   x %03d = %04d", lives, LIFE_POINTS, lives * LIFE_POINTS));

        VisTextButton playAgainButton = new VisTextButton("PLAY AGAIN");
        playAgainButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playUiClick();
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });

        VisTextButton menuButton = new VisTextButton("MAIN MENU");
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playUiClick();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        resultsWindow.add(titleLabel).center().padBottom(8f);
        resultsWindow.row();
        resultsWindow.add(rankLabel).center().padBottom(6f);
        resultsWindow.row();
        resultsWindow.add(scoreLabel).center().padBottom(18f);
        resultsWindow.row();
        resultsWindow.add(coinsLabel).left().padBottom(6f);
        resultsWindow.row();
        resultsWindow.add(keysLabel).left().padBottom(6f);
        resultsWindow.row();
        resultsWindow.add(livesLabel).left().padBottom(16f);
        resultsWindow.row();
        resultsWindow.add(top10Label).left().padBottom(8f);
        resultsWindow.row();
        resultsWindow.add(buildTop10Table()).left().padBottom(16f);
        resultsWindow.row();

        VisTable buttons = new VisTable(true);
        buttons.add(playAgainButton).width(180f).padRight(10f);
        buttons.add(menuButton).width(180f);
        resultsWindow.add(buttons).center();

        resultsWindow.pack();
        root.add(resultsWindow).center();
    }

    private void playUiClick() {
        if (audioManager != null) {
            audioManager.playSfx("interface1");
        }
    }

    private String resolveTop10Headline() {
        if (highScoreRegistration == null) {
            return "TOP 10";
        }
        if (!highScoreRegistration.enteredTop10()) {
            return "TOP 10  |  Current run did not enter";
        }
        return "TOP 10  |  Current run rank #" + highScoreRegistration.rank();
    }

    private VisTable buildTop10Table() {
        VisTable table = new VisTable(true);
        List<Integer> scores = highScoreRegistration != null
            ? highScoreRegistration.topScores()
            : List.of();

        if (scores.isEmpty()) {
            table.add(new VisLabel("No scores yet."));
            return table;
        }

        for (int i = 0; i < scores.size(); i++) {
            String marker = scores.get(i) == finalScore && highScoreRegistration != null
                && highScoreRegistration.enteredTop10()
                && i + 1 == highScoreRegistration.rank()
                ? "  NEW"
                : "";
            table.add(new VisLabel(String.format("#%02d   %05d%s", i + 1, scores.get(i), marker))).left();
            table.row();
        }
        return table;
    }

    private int calculateScore(int coins, int keys, int lives) {
        return coins * COIN_POINTS + keys * KEY_POINTS + lives * LIFE_POINTS;
    }

    private String resolveRank(int score) {
        if (score >= 2500) return "S";
        if (score >= 1800) return "A";
        if (score >= 1200) return "B";
        if (score >= 700) return "C";
        return "D";
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
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
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
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
