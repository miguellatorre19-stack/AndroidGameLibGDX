package svalero.com.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import svalero.com.items.PowerUpType;

import java.util.EnumMap;

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
    private static final float BOOST_BAR_WIDTH = 170f;
    private static final float BOOST_BAR_HEIGHT = 16f;
    private static final float MANA_BAR_WIDTH = 170f;
    private static final float MANA_BAR_HEIGHT = 12f;
    private static final float POWER_UP_ICON_SIZE = 24f;


    public Stage stage;
    private Viewport viewport;

    private Integer worldTimer;
    private float timeCount;
    private static Integer score;
    private boolean timeUp;
    private int key;
    private int lives;
    private int coins;

    private static Label scoreLabel;
    private Label keyLabel;
    private Label coinLabel;
    private Table heartsTable;
    private Texture fullHeartTexture;
    private Texture emptyHeartTexture;
    private TextureRegion fullHeartRegion;
    private TextureRegion emptyHeartRegion;
    private BitmapFont hudFont;
    private Texture coinSymbol;
    private BoostBarActor boostBar;
    private ManaBarActor manaBar;
    private final EnumMap<PowerUpType, Label> powerUpLabels;
    private final EnumMap<PowerUpType, Texture> powerUpTextures;

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
        powerUpLabels = new EnumMap<>(PowerUpType.class);
        powerUpTextures = new EnumMap<>(PowerUpType.class);
        fullHeartRegion = new TextureRegion(fullHeartTexture);
        emptyHeartRegion = new TextureRegion(emptyHeartTexture);
        hudFont = new BitmapFont();
        hudFont.getData().setScale(FONT_SCALE);

        scoreLabel =new Label(String.format("%06d", score), new Label.LabelStyle(hudFont, Color.valueOf("FFD166")));
        keyLabel = new Label(String.format("KEY %02d", key), new Label.LabelStyle(hudFont, Color.valueOf("7FDBFF")));
        coinLabel = new Label(String.format("COINS X %02d", coins), new Label.LabelStyle(hudFont, Color.valueOf("7FDBFF")));
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
        table.add(coinLabel).left();
        table.row();
        table.add(heartsTable).right();
        table.row();
        Animation<TextureRegion> boostAnimation = ResourceManager.buildIndexedAnimation(
            ResourceManager.HUD_INTERACTIONS_ID,
            "green",
            0.10f,
            Animation.PlayMode.LOOP
        );
        boostBar = new BoostBarActor(boostAnimation, BOOST_BAR_WIDTH, BOOST_BAR_HEIGHT);
        boostBar.setVisible(false);
        table.add(boostBar).right().width(BOOST_BAR_WIDTH).height(BOOST_BAR_HEIGHT);
        table.row();
        manaBar = new ManaBarActor(MANA_BAR_WIDTH, MANA_BAR_HEIGHT);
        table.add(manaBar).right().width(MANA_BAR_WIDTH).height(MANA_BAR_HEIGHT);
        table.row();
        table.add(buildPowerUpTable()).right();

        stage.addActor(table);
    }

    private Table buildPowerUpTable() {
        Table powerUpTable = new Table();
        powerUpTable.defaults().left().padBottom(4f);

        addPowerUpRow(powerUpTable, PowerUpType.SPEED, "hud/Green_Potion.png");
        powerUpTable.row();
        addPowerUpRow(powerUpTable, PowerUpType.SHIELD, "hud/Blue_Potion.png");
        powerUpTable.row();
        addPowerUpRow(powerUpTable, PowerUpType.MASTER_KEY, "hud/Red_Potion.png");
        return powerUpTable;
    }

    private void addPowerUpRow(Table table, PowerUpType type, String texturePath) {
        Texture texture = new Texture(texturePath);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        powerUpTextures.put(type, texture);

        Label label = new Label("", new Label.LabelStyle(hudFont, Color.valueOf("EAF2FF")));
        label.setFontScale(0.8f);
        powerUpLabels.put(type, label);
        setPowerUp(type, 0, 0f);

        table.add(new Image(new TextureRegionDrawable(new TextureRegion(texture))))
            .size(POWER_UP_ICON_SIZE, POWER_UP_ICON_SIZE)
            .padRight(6f);
        table.add(label).minWidth(120f);
    }

    public void update(float dt){
        stage.act(dt);
        timeCount += dt;
        if(timeCount >= 1){
            if (worldTimer > 0) {
                worldTimer--;
            } else {
                timeUp = true;
            }
            timeCount = 0;
        }
        if (boostBar != null) {
            boostBar.updateTime(dt);
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

    public void setCoins(int coins){
        this.coins = coins;
        coinLabel.setText(String.format("COINS X %02d", this.coins));
    }

    public void setKeys(int keys){
        this.key = keys;
        keyLabel.setText(String.format("KEY %02d", this.key));
    }

    public void setBoost(boolean boosted, float progress01) {
        if (boostBar == null) return;
        boostBar.setProgress(progress01);
        boostBar.setVisible(boosted && progress01 > 0f);
    }

    public void setPowerUp(PowerUpType type, int count, float remainingSec) {
        Label label = powerUpLabels.get(type);
        if (label == null) return;

        String timerText = remainingSec > 0f ? String.format(" %.1fs", remainingSec) : "";
        label.setText(String.format("[%s] x%02d%s", type.activationLabel(), count, timerText));
    }

    public void setMana(float progress01) {
        if (manaBar == null) return;
        manaBar.setProgress(progress01);
    }

    @Override
    public void dispose() {
        stage.dispose();
        hudFont.dispose();
        fullHeartTexture.dispose();
        emptyHeartTexture.dispose();
        for (Texture texture : powerUpTextures.values()) {
            texture.dispose();
        }
        if (manaBar != null) {
            manaBar.dispose();
        }
    }

    private static class BoostBarActor extends Actor {
        private final Animation<TextureRegion> animation;
        private final TextureRegion clippedFrame;
        private float stateTime;
        private float progress;

        private BoostBarActor(Animation<TextureRegion> animation, float width, float height) {
            this.animation = animation;
            this.clippedFrame = new TextureRegion();
            this.stateTime = 0f;
            this.progress = 1f;
            setSize(width, height);
        }

        private void updateTime(float dt) {
            if (isVisible()) {
                stateTime += dt;
            }
        }

        private void setProgress(float progress01) {
            this.progress = MathUtils.clamp(progress01, 0f, 1f);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!isVisible() || animation == null || progress <= 0f) return;

            TextureRegion frame = animation.getKeyFrame(stateTime, true);
            int clippedWidth = Math.max(1, Math.round(frame.getRegionWidth() * progress));
            clippedFrame.setRegion(frame);
            clippedFrame.setRegionWidth(clippedWidth);

            batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);
            batch.draw(clippedFrame, getX(), getY(), getWidth() * progress, getHeight());
        }
    }

    private static class ManaBarActor extends Actor implements Disposable {
        private final Texture texture;
        private float progress;

        private ManaBarActor(float width, float height) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            texture = new Texture(pixmap);
            pixmap.dispose();
            progress = 1f;
            setSize(width, height);
        }

        private void setProgress(float progress01) {
            progress = MathUtils.clamp(progress01, 0f, 1f);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float x = getX();
            float y = getY();
            float width = getWidth();
            float height = getHeight();

            batch.setColor(0.05f, 0.08f, 0.16f, parentAlpha);
            batch.draw(texture, x, y, width, height);
            batch.setColor(0.25f, 0.62f, 1f, parentAlpha);
            batch.draw(texture, x + 1f, y + 1f, Math.max(0f, (width - 2f) * progress), height - 2f);
            batch.setColor(Color.WHITE);
        }

        @Override
        public void dispose() {
            texture.dispose();
        }
    }

}
