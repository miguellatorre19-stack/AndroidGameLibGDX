package svalero.com.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static svalero.com.utils.Constants.SCREEN_HEIGHT;
import static svalero.com.utils.Constants.SCREEN_WIDTH;

public class HudManager implements Disposable {
    private static final int MAX_LIVES = 3;
    private static final int HEART_SIZE_PX = 32;
    private static final float HEART_SPACING = 8f;
    private static final float HUD_PADDING_TOP = 20f;
    private static final float HUD_PADDING_RIGHT = 22f;
    private static final float HUD_ROW_SPACING = 10f;
    private static final float HUD_MIN_WIDTH = 220f;
    private static final float FONT_SCALE = 1.35f;

    public Stage stage;
    private Viewport viewport;

    private Integer worldTimer;
    private float timeCount;
    private static Integer score;
    private boolean timeUp;
    private int key;
    private int lives;

    private static Label scoreLabel;
    private Label keyLabel;
    private Table heartsTable;
    private Texture fullHeartTexture;
    private Texture emptyHeartTexture;
    private TextureRegion fullHeartRegion;
    private TextureRegion emptyHeartRegion;
    private BitmapFont hudFont;

    public static Label getScoreLabel() {
        return scoreLabel;
    }

    public static Integer getScore() {
        return score;
    }

    public int getKey() {
        return key;
    }

    public Label getKeyLabel() {
        return keyLabel;
    }

    public HudManager (SpriteBatch sb){
        worldTimer = 250;
        timeCount = 0;
        score = 0;
        key = 0;
        lives = 0;

        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT);
        stage = new Stage(viewport, sb);
        fullHeartTexture = new Texture("hud/Life/Heart.png");
        emptyHeartTexture = new Texture("hud/Life/Heart1.png");
        fullHeartRegion = new TextureRegion(fullHeartTexture);
        emptyHeartRegion = new TextureRegion(emptyHeartTexture);
        hudFont = new BitmapFont();
        hudFont.getData().setScale(FONT_SCALE);

        scoreLabel =new Label(String.format("%06d", score), new Label.LabelStyle(hudFont, Color.valueOf("FFD166")));
        keyLabel = new Label(String.format("KEY %02d", key), new Label.LabelStyle(hudFont, Color.valueOf("7FDBFF")));
        heartsTable = new Table();
        heartsTable.right();

        Table table = new Table();
        table.top().right();
        table.padTop(HUD_PADDING_TOP).padRight(HUD_PADDING_RIGHT);
        table.defaults().right().padBottom(HUD_ROW_SPACING).minWidth(HUD_MIN_WIDTH);
        table.setFillParent(true);
        table.add(scoreLabel).right();
        table.row();
        table.add(keyLabel).right();
        table.row();
        table.add(heartsTable).right();

        stage.addActor(table);
    }

    public void update(float dt){
        timeCount += dt;
        if(timeCount >= 1){
            if (worldTimer > 0) {
                worldTimer--;
            } else {
                timeUp = true;
            }
            timeCount = 0;
        }
    }

    public static void addScore(int value){
        score += value;
        scoreLabel.setText(String.format("%06d", score));
    }

    public void setLives(int lives) {
        this.lives = lives;
        heartsTable.clearChildren();
        for (int i = 0; i < MAX_LIVES; i++) {
            TextureRegion region = i < lives ? fullHeartRegion : emptyHeartRegion;
            heartsTable.add(new Image(new TextureRegionDrawable(region))).size(HEART_SIZE_PX, HEART_SIZE_PX).padRight(HEART_SPACING);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        hudFont.dispose();
        fullHeartTexture.dispose();
        emptyHeartTexture.dispose();
    }


}
