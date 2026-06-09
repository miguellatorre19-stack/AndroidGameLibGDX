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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
    private static final int HEART_SIZE_PX = 38;
    private static final float HEART_SPACING = 10f;
    private static final float HUD_PADDING_TOP = 18f;
    private static final float HUD_PADDING_RIGHT = 18f;
    private static final float HUD_PANEL_WIDTH = 348f;
    private static final float HUD_SECTION_SPACING = 12f;
    private static final float HUD_PANEL_PADDING = 16f;
    private static final float HUD_CARD_PADDING = 10f;
    private static final float FONT_SCALE = 1.10f;
    private static final float SCORE_SCALE = 1.72f;
    private static final float VALUE_SCALE = 1.18f;
    private static final float CAPTION_SCALE = 0.76f;
    private static final float STATUS_SCALE = 0.94f;
    private static final float BOOST_BAR_WIDTH = 282f;
    private static final float BOOST_BAR_HEIGHT = 20f;
    private static final float MANA_BAR_WIDTH = 282f;
    private static final float MANA_BAR_HEIGHT = 18f;
    private static final float POWER_UP_ICON_SIZE = 28f;
    private static final float STAT_CARD_WIDTH = 150f;


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
    private Label statusLabel;
    private Table statusTable;
    private Table heartsTable;
    private Texture fullHeartTexture;
    private Texture emptyHeartTexture;
    private Texture uiPixelTexture;
    private TextureRegion fullHeartRegion;
    private TextureRegion emptyHeartRegion;
    private BitmapFont hudFont;
    private Drawable panelBackground;
    private Drawable cardBackground;
    private Drawable statusReadyBackground;
    private Drawable statusSlowBackground;
    private Drawable statusStunBackground;
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
        uiPixelTexture = createSolidTexture();
        powerUpLabels = new EnumMap<>(PowerUpType.class);
        powerUpTextures = new EnumMap<>(PowerUpType.class);
        fullHeartRegion = new TextureRegion(fullHeartTexture);
        emptyHeartRegion = new TextureRegion(emptyHeartTexture);
        hudFont = new BitmapFont();
        hudFont.getData().setScale(FONT_SCALE);
        panelBackground = createTintedDrawable("091626D8");
        cardBackground = createTintedDrawable("10263BCF");
        statusReadyBackground = createTintedDrawable("17344ECF");
        statusSlowBackground = createTintedDrawable("5A4216D8");
        statusStunBackground = createTintedDrawable("5E162CD8");

        scoreLabel = createLabel(String.format("%06d", score), "FFD166", SCORE_SCALE);
        keyLabel = createLabel(String.format("%02d", key), "F5FBFF", VALUE_SCALE);
        coinLabel = createLabel(String.format("%02d", coins), "F5FBFF", VALUE_SCALE);
        heartsTable = new Table();
        heartsTable.center();

        Table root = new Table();
        root.top().right();
        root.padTop(HUD_PADDING_TOP).padRight(HUD_PADDING_RIGHT);
        root.setFillParent(true);

        Table panel = new Table();
        panel.setBackground(panelBackground);
        panel.pad(HUD_PANEL_PADDING);
        panel.defaults().growX().padBottom(HUD_SECTION_SPACING);

        panel.add(buildHeaderBlock());
        panel.row();
        panel.add(buildResourcesBlock());
        panel.row();
        panel.add(buildVitalityBlock());
        panel.row();
        panel.add(buildStatusBlock());
        panel.row();
        Animation<TextureRegion> boostAnimation = ResourceManager.buildIndexedAnimation(
            ResourceManager.HUD_INTERACTIONS_ID,
            "green",
            0.10f,
            Animation.PlayMode.LOOP
        );
        boostBar = new BoostBarActor(boostAnimation, uiPixelTexture, BOOST_BAR_WIDTH, BOOST_BAR_HEIGHT);
        panel.add(buildBarBlock("BLESSING", "Speed burst", "6EEB83", boostBar));
        panel.row();
        manaBar = new ManaBarActor(uiPixelTexture, MANA_BAR_WIDTH, MANA_BAR_HEIGHT);
        panel.add(buildBarBlock("MANA", "Attack resource", "67C6FF", manaBar));
        panel.row();
        panel.add(buildPowerUpBlock()).padBottom(0f);

        root.add(panel).width(HUD_PANEL_WIDTH).top().right();
        stage.addActor(root);
        setStatusEffects(false, 0f, false, 0f);
    }

    private Table buildHeaderBlock() {
        Table header = new Table();
        header.defaults().left();
        header.add(createLabel("PRIEST STATUS", "8FD3FF", 0.88f)).left();
        header.row();
        header.add(scoreLabel).left().padTop(2f);
        return header;
    }

    private Table buildResourcesBlock() {
        Table resources = new Table();
        resources.defaults().padRight(8f);
        resources.add(buildStatCard("KEYS", "Keys carried", "7FDBFF", keyLabel)).width(STAT_CARD_WIDTH).fillX();
        resources.add(buildStatCard("COINS", "Current stash", "FFD166", coinLabel)).width(STAT_CARD_WIDTH).fillX().padRight(0f);
        return resources;
    }

    private Table buildVitalityBlock() {
        Table vitality = new Table();
        vitality.setBackground(cardBackground);
        vitality.pad(HUD_CARD_PADDING);
        vitality.defaults().left();
        vitality.add(createLabel("VITAL", "FF8FA3", CAPTION_SCALE)).left();
        vitality.row();
        vitality.add(heartsTable).center().padTop(6f);
        return vitality;
    }

    private Table buildStatusBlock() {
        statusTable = new Table();
        statusTable.pad(HUD_CARD_PADDING);
        statusTable.defaults().left();
        statusTable.add(createLabel("STATUS", "8FD3FF", CAPTION_SCALE)).left();
        statusTable.row();
        statusLabel = createLabel("", "D7E8FF", STATUS_SCALE);
        statusTable.add(statusLabel).left().padTop(4f);
        return statusTable;
    }

    private Table buildBarBlock(String title, String subtitle, String accentHex, Actor barActor) {
        Table block = new Table();
        block.setBackground(cardBackground);
        block.pad(HUD_CARD_PADDING);
        block.defaults().left();
        block.add(createLabel(title, accentHex, CAPTION_SCALE)).left();
        block.row();
        block.add(createLabel(subtitle, "8CA8C8", 0.70f)).left().padTop(2f).padBottom(8f);
        block.row();
        block.add(barActor).width(BOOST_BAR_WIDTH).height(barActor.getHeight()).left();
        return block;
    }

    private Table buildPowerUpBlock() {
        Table powerUpBlock = new Table();
        powerUpBlock.setBackground(cardBackground);
        powerUpBlock.pad(HUD_CARD_PADDING);
        powerUpBlock.defaults().left();
        powerUpBlock.add(createLabel("RELICS", "B8F2E6", CAPTION_SCALE)).left();
        powerUpBlock.row();
        powerUpBlock.add(buildPowerUpTable()).left().padTop(6f);
        return powerUpBlock;
    }

    private Table buildPowerUpTable() {
        Table powerUpTable = new Table();
        powerUpTable.defaults().left().padBottom(6f);

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

        Label label = createLabel("", "EAF2FF", 0.82f);
        powerUpLabels.put(type, label);
        setPowerUp(type, 0, 0f);

        table.add(new Image(new TextureRegionDrawable(new TextureRegion(texture))))
            .size(POWER_UP_ICON_SIZE, POWER_UP_ICON_SIZE)
            .padRight(8f);
        table.add(label).minWidth(220f);
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
            heartsTable.add(new Image(new TextureRegionDrawable(region)))
                .size(HEART_SIZE_PX, HEART_SIZE_PX)
                .padRight(i == MAX_LIVES - 1 ? 0f : HEART_SPACING);
        }
    }

    public void setCoins(int coins){
        this.coins = coins;
        coinLabel.setText(String.format("%02d", this.coins));
    }

    public void setKeys(int keys){
        this.key = keys;
        keyLabel.setText(String.format("%02d", this.key));
    }

    public void setBoost(boolean boosted, float progress01) {
        if (boostBar == null) return;
        boostBar.setProgress(progress01);
        boostBar.setActive(boosted && progress01 > 0f);
    }

    public void setPowerUp(PowerUpType type, int count, float remainingSec) {
        Label label = powerUpLabels.get(type);
        if (label == null) return;

        String timerText = remainingSec > 0f ? String.format("  %.1fs", remainingSec) : "";
        label.setText(String.format("%s  x%02d%s", powerUpTitle(type), count, timerText));
    }

    public void setMana(float progress01) {
        if (manaBar == null) return;
        manaBar.setProgress(progress01);
    }

    public void setStatusEffects(boolean stunned, float stunRemainingSec, boolean slowed, float slowRemainingSec) {
        if (statusLabel == null || statusTable == null) return;

        if (stunned && stunRemainingSec > 0f) {
            statusLabel.setText(String.format("PARALYZED %.1fs", stunRemainingSec));
            statusLabel.setColor(Color.valueOf("FFF3F5"));
            statusTable.setBackground(statusStunBackground);
            return;
        }

        if (slowed && slowRemainingSec > 0f) {
            statusLabel.setText(String.format("SLOWED %.1fs", slowRemainingSec));
            statusLabel.setColor(Color.valueOf("FFF5D8"));
            statusTable.setBackground(statusSlowBackground);
            return;
        }

        statusLabel.setText("CLEAR");
        statusLabel.setColor(Color.valueOf("D7E8FF"));
        statusTable.setBackground(statusReadyBackground);
    }

    @Override
    public void dispose() {
        stage.dispose();
        hudFont.dispose();
        fullHeartTexture.dispose();
        emptyHeartTexture.dispose();
        uiPixelTexture.dispose();
        for (Texture texture : powerUpTextures.values()) {
            texture.dispose();
        }
    }

    private static class BoostBarActor extends Actor {
        private final Animation<TextureRegion> animation;
        private final Texture uiPixelTexture;
        private final TextureRegion clippedFrame;
        private float stateTime;
        private float progress;
        private boolean active;

        private BoostBarActor(Animation<TextureRegion> animation, Texture uiPixelTexture, float width, float height) {
            this.animation = animation;
            this.uiPixelTexture = uiPixelTexture;
            this.clippedFrame = new TextureRegion();
            this.stateTime = 0f;
            this.progress = 0f;
            this.active = false;
            setSize(width, height);
        }

        private void updateTime(float dt) {
            if (active) {
                stateTime += dt;
            }
        }

        private void setActive(boolean active) {
            this.active = active;
        }

        private void setProgress(float progress01) {
            this.progress = MathUtils.clamp(progress01, 0f, 1f);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float x = getX();
            float y = getY();
            float width = getWidth();
            float height = getHeight();

            batch.setColor(0.04f, 0.06f, 0.11f, 0.95f * parentAlpha);
            batch.draw(uiPixelTexture, x, y, width, height);
            batch.setColor(0.11f, 0.18f, 0.27f, parentAlpha);
            batch.draw(uiPixelTexture, x + 2f, y + 2f, width - 4f, height - 4f);
            if (!active || animation == null || progress <= 0f) {
                batch.setColor(Color.WHITE);
                return;
            }

            TextureRegion frame = animation.getKeyFrame(stateTime, true);
            float innerX = x + 3f;
            float innerY = y + 3f;
            float innerWidth = width - 6f;
            float innerHeight = height - 6f;
            int clippedWidth = Math.max(1, Math.round(frame.getRegionWidth() * progress));
            clippedFrame.setRegion(frame);
            clippedFrame.setRegionWidth(clippedWidth);

            batch.setColor(0.65f, 1f, 0.78f, 0.24f * parentAlpha);
            batch.draw(uiPixelTexture, innerX, innerY, innerWidth * progress, innerHeight);
            batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);
            batch.draw(clippedFrame, innerX, innerY, innerWidth * progress, innerHeight);
            batch.setColor(Color.WHITE);
        }
    }

    private static class ManaBarActor extends Actor {
        private final Texture uiPixelTexture;
        private float progress;

        private ManaBarActor(Texture uiPixelTexture, float width, float height) {
            this.uiPixelTexture = uiPixelTexture;
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

            batch.setColor(0.04f, 0.06f, 0.11f, 0.95f * parentAlpha);
            batch.draw(uiPixelTexture, x, y, width, height);
            batch.setColor(0.11f, 0.18f, 0.27f, parentAlpha);
            batch.draw(uiPixelTexture, x + 2f, y + 2f, width - 4f, height - 4f);
            batch.setColor(0.25f, 0.62f, 1f, parentAlpha);
            batch.draw(uiPixelTexture, x + 3f, y + 3f, Math.max(0f, (width - 6f) * progress), height - 6f);
            batch.setColor(0.71f, 0.92f, 1f, 0.22f * parentAlpha);
            batch.draw(uiPixelTexture, x + 3f, y + height * 0.5f, Math.max(0f, (width - 6f) * progress), (height - 6f) * 0.5f);
            batch.setColor(Color.WHITE);
        }
    }

    private Label createLabel(String text, String colorHex, float scale) {
        Label label = new Label(text, new Label.LabelStyle(hudFont, Color.valueOf(colorHex)));
        label.setFontScale(scale);
        return label;
    }

    private Table buildStatCard(String title, String subtitle, String accentHex, Label valueLabel) {
        Table card = new Table();
        card.setBackground(cardBackground);
        card.pad(HUD_CARD_PADDING);
        card.defaults().left();
        card.add(createLabel(title, accentHex, CAPTION_SCALE)).left();
        card.row();
        card.add(createLabel(subtitle, "8CA8C8", 0.68f)).left().padTop(2f);
        card.row();
        card.add(valueLabel).left().padTop(8f);
        return card;
    }

    private Texture createSolidTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Drawable createTintedDrawable(String colorHex) {
        return new TextureRegionDrawable(new TextureRegion(uiPixelTexture)).tint(Color.valueOf(colorHex));
    }

    private String powerUpTitle(PowerUpType type) {
        if (type == null) return "UNKNOWN";
        return switch (type) {
            case SPEED -> "HASTE  [1]";
            case SHIELD -> "SHIELD [2]";
            case MASTER_KEY -> "MASTER [3]";
        };
    }

}
