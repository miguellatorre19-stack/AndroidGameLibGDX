package svalero.com.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lombok.Data;
import svalero.com.KeyFinder;
import svalero.com.managers.SpriteManager;

@Data
public abstract class Character {
    private static final float DEFAULT_HITBOX_WIDTH_SCALE = 0.8f;
    private static final float DEFAULT_HITBOX_HEIGHT_SCALE = 0.8f;

    protected KeyFinder game;
    protected Vector2 velocity;
    protected Vector2 position;
    protected Animation<TextureRegion> animation;
    protected float stateTime;
    protected TextureRegion currentFrame;
    protected Polygon hitbox;
    protected Rectangle rect;
    protected int lives;
    protected boolean dead = false;
    protected SpriteManager spriteManager;
    protected float renderWidth;
    protected float renderHeight;
    protected boolean facingRight;
    protected float hitboxWidthScale;
    protected float hitboxHeightScale;
    protected float hitboxOffsetXRatio;
    protected float hitboxOffsetYRatio;

    public Character(Vector2 position, SpriteManager spriteManager, Animation<TextureRegion> animation){
        this.game = spriteManager.getGame();
        this.position = position;
        this.spriteManager = spriteManager;
        this.animation = animation;
        TextureRegion initialFrame = animation != null ? animation.getKeyFrame(0f, true) : null;
        renderWidth = initialFrame != null ? initialFrame.getRegionWidth() : 0f;
        renderHeight = initialFrame != null ? initialFrame.getRegionHeight() : 0f;
        rect = new Rectangle(position.x, position.y, renderWidth, renderHeight);
        hitbox = new Polygon();
        facingRight = true;
        hitboxWidthScale = DEFAULT_HITBOX_WIDTH_SCALE;
        hitboxHeightScale = DEFAULT_HITBOX_HEIGHT_SCALE;
        hitboxOffsetXRatio = 0f;
        hitboxOffsetYRatio = 0f;
        applyHitboxGeometry();
    }

    public TextureRegion getCurrentFrame(float delta) {
        if (animation == null) return null;
        stateTime += delta;
        return animation.getKeyFrame(stateTime, true);
    }

    public void render(Batch batch){
        currentFrame = getCurrentFrame(Gdx.graphics.getDeltaTime());
        if (currentFrame != null) {
            if (facingRight) {
                batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);
            } else {
                batch.draw(currentFrame, position.x + renderWidth, position.y, -renderWidth, renderHeight);
            }
        }
    }

    protected void setRenderScale(float scale){
        TextureRegion referenceFrame = animation != null ? animation.getKeyFrame(0f, true) : null;
        if (referenceFrame == null) return;
        renderWidth = referenceFrame.getRegionWidth() * scale;
        renderHeight = referenceFrame.getRegionHeight() * scale;
        applyHitboxGeometry();
    }

    protected void configureHitbox(float widthScale, float heightScale, float offsetXRatio, float offsetYRatio) {
        hitboxWidthScale = Math.max(0.1f, Math.min(widthScale, 1f));
        hitboxHeightScale = Math.max(0.1f, Math.min(heightScale, 1f));
        hitboxOffsetXRatio = Math.max(0f, Math.min(offsetXRatio, 1f));
        hitboxOffsetYRatio = Math.max(0f, Math.min(offsetYRatio, 1f));
        applyHitboxGeometry();
    }

    public void syncHitboxFromPosition() {
        rect.setPosition(
            position.x + (renderWidth * hitboxOffsetXRatio),
            position.y + (renderHeight * hitboxOffsetYRatio)
        );
        hitbox.setPosition(rect.x, rect.y);
    }

    public void updateFacingFromMovement(float moveX) {
        if (moveX > 0f) {
            facingRight = true;
        } else if (moveX < 0f) {
            facingRight = false;
        }
    }

    private void rebuildHitboxGeometry() {
        hitbox.setVertices(new float[]{
            0f, 0f,
            rect.width, 0f,
            rect.width, rect.height,
            0f, rect.height
        });
        hitbox.setPosition(rect.x, rect.y);
    }

    private void applyHitboxGeometry() {
        rect.setSize(renderWidth * hitboxWidthScale, renderHeight * hitboxHeightScale);
        syncHitboxFromPosition();
        rebuildHitboxGeometry();
    }

    public abstract void attack();
    public abstract void die();
    public abstract void update();
    public abstract void affected();
    public  boolean isDead(){
        return dead;
    };



}
