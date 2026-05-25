package svalero.com.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class PauseOverlay {

    private static final float MENU_SCALE = 1.5f;
    private static final float OPTIONS_BUTTON_SCALE = 1.7f;
    private static final float SOUND_BUTTON_SCALE = 1.7f;
    private static final float OPTIONS_ANIMATION_FRAME_DURATION = 0.12f;
    private static final float BUTTON_PRESSED_FEEDBACK_SEC = 0.10f;

    private final Texture menuBackgroundTexture;
    private final Texture pauseWindowTexture;
    private final Texture buttonAtlasTexture;
    private final Texture soundButtonTexture;
    private final Texture dimTexture;

    private final TextureRegion[] optionsButtonFrames;
    private final TextureRegion[] resumeButtonFrames;
    private final TextureRegion[] quitButtonFrames;
    private final TextureRegion[][] soundButtonFrames;
    private final Animation<TextureRegion> optionsAnimation;
    private final Animation<TextureRegion> resumeAnimation;
    private final Animation<TextureRegion> quitAnimation;

    private final Rectangle menuBackgroundBounds;
    private final Rectangle optionsButtonBounds;
    private final Rectangle resumeButtonBounds;
    private final Rectangle quitButtonBounds;
    private final Rectangle soundButtonBounds;
    private final Rectangle pauseWindowBounds;

    private float optionsAnimationTime;
    private float optionsPressedTimer;
    private float resumeAnimationTime;
    private float resumePressedTimer;
    private float quitAnimationTime;
    private float quitPressedTimer;
    private float soundPressedTimer;
    private boolean soundEnabled;
    private boolean showPauseWindow;

    public PauseOverlay() {
        menuBackgroundTexture = new Texture(Gdx.files.internal("ui/pause_menu/menu_background.png"));
        pauseWindowTexture = new Texture(Gdx.files.internal("ui/pause_menu/pause_menu.png"));
        buttonAtlasTexture = new Texture(Gdx.files.internal("ui/pause_menu/button_atlas.png"));
        soundButtonTexture = new Texture(Gdx.files.internal("ui/pause_menu/sound_button.png"));
        dimTexture = createDimTexture();

        TextureRegion[][] buttonAtlasGrid = TextureRegion.split(buttonAtlasTexture, 140, 56);
        resumeButtonFrames = buttonAtlasGrid[0];
        optionsButtonFrames = buttonAtlasGrid[1];
        quitButtonFrames = buttonAtlasGrid[2];
        soundButtonFrames = TextureRegion.split(soundButtonTexture, 42, 42);

        optionsAnimation = new Animation<>(
            OPTIONS_ANIMATION_FRAME_DURATION,
            optionsButtonFrames[0], optionsButtonFrames[1], optionsButtonFrames[2], optionsButtonFrames[1]
        );
        resumeAnimation = new Animation<>(
            OPTIONS_ANIMATION_FRAME_DURATION,
            resumeButtonFrames[0], resumeButtonFrames[1], resumeButtonFrames[2], resumeButtonFrames[1]
        );
        quitAnimation = new Animation<>(
            OPTIONS_ANIMATION_FRAME_DURATION,
            quitButtonFrames[0], quitButtonFrames[1], quitButtonFrames[2], quitButtonFrames[1]
        );

        optionsAnimation.setPlayMode(Animation.PlayMode.LOOP);
        resumeAnimation.setPlayMode(Animation.PlayMode.LOOP);
        quitAnimation.setPlayMode(Animation.PlayMode.LOOP);

        menuBackgroundBounds = new Rectangle();
        optionsButtonBounds = new Rectangle();
        resumeButtonBounds = new Rectangle();
        quitButtonBounds = new Rectangle();
        soundButtonBounds = new Rectangle();
        pauseWindowBounds = new Rectangle();

        soundEnabled = true;
        showPauseWindow = false;
    }

    public void render(Batch batch, float delta) {
        updateLayout();
        updateAnimationTimers(delta);

        batch.setColor(1f, 1f, 1f, 0.6f);
        batch.draw(dimTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);

        batch.draw(
            menuBackgroundTexture,
            menuBackgroundBounds.x,
            menuBackgroundBounds.y,
            menuBackgroundBounds.width,
            menuBackgroundBounds.height
        );

        TextureRegion resumeFrame = resolveResumeButtonFrame();
        batch.draw(
            resumeFrame,
            resumeButtonBounds.x,
            resumeButtonBounds.y,
            resumeButtonBounds.width,
            resumeButtonBounds.height
        );

        TextureRegion optionsFrame = resolveOptionsButtonFrame();
        batch.draw(
            optionsFrame,
            optionsButtonBounds.x,
            optionsButtonBounds.y,
            optionsButtonBounds.width,
            optionsButtonBounds.height
        );

        TextureRegion quitFrame = resolveQuitButtonFrame();
        batch.draw(
            quitFrame,
            quitButtonBounds.x,
            quitButtonBounds.y,
            quitButtonBounds.width,
            quitButtonBounds.height
        );

        TextureRegion soundFrame = resolveSoundButtonFrame();
        batch.draw(
            soundFrame,
            soundButtonBounds.x,
            soundButtonBounds.y,
            soundButtonBounds.width,
            soundButtonBounds.height
        );

        if (showPauseWindow) {
            batch.draw(
                pauseWindowTexture,
                pauseWindowBounds.x,
                pauseWindowBounds.y,
                pauseWindowBounds.width,
                pauseWindowBounds.height
            );
        }
    }

    public Rectangle getResumeButtonBounds() {
        return resumeButtonBounds;
    }

    public Rectangle getOptionsButtonBounds() {
        return optionsButtonBounds;
    }

    public Rectangle getQuitButtonBounds() {
        return quitButtonBounds;
    }

    public Rectangle getSoundButtonBounds() {
        return soundButtonBounds;
    }

    public void pressResume() {
        resumePressedTimer = BUTTON_PRESSED_FEEDBACK_SEC;
        resumeAnimationTime = 0f;
    }

    public void pressOptions() {
        showPauseWindow = !showPauseWindow;
        optionsPressedTimer = BUTTON_PRESSED_FEEDBACK_SEC;
        optionsAnimationTime = 0f;
    }

    public void pressQuit() {
        quitPressedTimer = BUTTON_PRESSED_FEEDBACK_SEC;
        quitAnimationTime = 0f;
    }

    public void pressSound() {
        soundEnabled = !soundEnabled;
        soundPressedTimer = BUTTON_PRESSED_FEEDBACK_SEC;
    }

    public void onResumeGame() {
        showPauseWindow = false;
        optionsPressedTimer = 0f;
        resumePressedTimer = 0f;
        quitPressedTimer = 0f;
        soundPressedTimer = 0f;
    }

    public void dispose() {
        menuBackgroundTexture.dispose();
        pauseWindowTexture.dispose();
        buttonAtlasTexture.dispose();
        soundButtonTexture.dispose();
        dimTexture.dispose();
    }

    private void updateAnimationTimers(float delta) {
        optionsAnimationTime += delta;
        resumeAnimationTime += delta;
        quitAnimationTime += delta;

        if (optionsPressedTimer > 0f) optionsPressedTimer -= delta;
        if (resumePressedTimer > 0f) resumePressedTimer -= delta;
        if (quitPressedTimer > 0f) quitPressedTimer -= delta;
        if (soundPressedTimer > 0f) soundPressedTimer -= delta;
    }

    private void updateLayout() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        float menuW = menuBackgroundTexture.getWidth() * MENU_SCALE;
        float menuH = menuBackgroundTexture.getHeight() * MENU_SCALE;
        float menuX = (screenW - menuW) * 0.5f;
        float menuY = (screenH - menuH) * 0.5f;
        menuBackgroundBounds.set(menuX, menuY, menuW, menuH);

        float buttonW = optionsButtonFrames[0].getRegionWidth() * OPTIONS_BUTTON_SCALE;
        float buttonH = optionsButtonFrames[0].getRegionHeight() * OPTIONS_BUTTON_SCALE;
        float centerX = menuX + (menuW - buttonW) * 0.5f;
        resumeButtonBounds.set(centerX, menuY + menuH * 0.68f, buttonW, buttonH);
        optionsButtonBounds.set(centerX, menuY + menuH * 0.49f, buttonW, buttonH);
        quitButtonBounds.set(centerX, menuY + menuH * 0.30f, buttonW, buttonH);

        float soundW = soundButtonFrames[0][0].getRegionWidth() * SOUND_BUTTON_SCALE;
        float soundH = soundButtonFrames[0][0].getRegionHeight() * SOUND_BUTTON_SCALE;
        float padding = 12f;
        soundButtonBounds.set(menuX + menuW - soundW - padding, menuY + padding, soundW, soundH);

        float pauseW = pauseWindowTexture.getWidth() * 1.2f;
        float pauseH = pauseWindowTexture.getHeight() * 1.2f;
        pauseWindowBounds.set((screenW - pauseW) * 0.5f, (screenH - pauseH) * 0.5f, pauseW, pauseH);
    }

    private TextureRegion resolveResumeButtonFrame() {
        if (resumePressedTimer > 0f) return resumeButtonFrames[2];
        if (!isHovered(resumeButtonBounds)) return resumeButtonFrames[0];
        return resumeAnimation.getKeyFrame(resumeAnimationTime, true);
    }

    private TextureRegion resolveOptionsButtonFrame() {
        if (optionsPressedTimer > 0f) return optionsButtonFrames[2];
        if (!isHovered(optionsButtonBounds) && !showPauseWindow) return optionsButtonFrames[0];
        return optionsAnimation.getKeyFrame(optionsAnimationTime, true);
    }

    private TextureRegion resolveQuitButtonFrame() {
        if (quitPressedTimer > 0f) return quitButtonFrames[2];
        if (!isHovered(quitButtonBounds)) return quitButtonFrames[0];
        return quitAnimation.getKeyFrame(quitAnimationTime, true);
    }

    private TextureRegion resolveSoundButtonFrame() {
        int row = soundEnabled ? 0 : 1;
        if (soundPressedTimer > 0f) return soundButtonFrames[row][2];
        if (isHovered(soundButtonBounds)) return soundButtonFrames[row][1];
        return soundButtonFrames[row][0];
    }

    private boolean isHovered(Rectangle bounds) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        return bounds.contains(mouseX, mouseY);
    }

    private Texture createDimTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
