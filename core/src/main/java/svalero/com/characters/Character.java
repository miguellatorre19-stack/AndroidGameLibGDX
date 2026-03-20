package svalero.com.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lombok.Data;
import svalero.com.KeyFinder;
import svalero.com.managers.SpriteManager;

@Data
public abstract class Character {

    protected KeyFinder game;
    protected Vector2 velocity;
    protected Vector2 position;
    protected Animation<TextureRegion> animation;
    protected float stateTime;
    protected TextureRegion currentFrame;
    protected Rectangle rect;
    protected int lives;
    protected boolean dead = false;
    protected SpriteManager spriteManager;
    protected float renderWidth;
    protected float renderHeight;

    public Character(Vector2 position, SpriteManager spriteManager, Animation<TextureRegion> animation){
        this.game = spriteManager.getGame();

        this.position = position;
        this.spriteManager = spriteManager;
        this.animation = animation;
        TextureRegion initialFrame = animation != null ? animation.getKeyFrame(0f, true) : null;
        renderWidth = initialFrame != null ? initialFrame.getRegionWidth() : 0f;
        renderHeight = initialFrame != null ? initialFrame.getRegionHeight() : 0f;
        rect = new Rectangle(position.x, position.y, renderWidth, renderHeight);
    }

    public TextureRegion getCurrentFrame(float delta) {
        if (animation == null) return null;
        stateTime += delta;
        return animation.getKeyFrame(stateTime, true);
    }

    public void render(Batch batch){
        currentFrame = getCurrentFrame(Gdx.graphics.getDeltaTime());
        if (currentFrame != null) {
            batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);
        }
    }

    protected void setRenderScale(float scale){
        TextureRegion referenceFrame = animation != null ? animation.getKeyFrame(0f, true) : null;
        if (referenceFrame == null) return;
        renderWidth = referenceFrame.getRegionWidth() * scale;
        renderHeight = referenceFrame.getRegionHeight() * scale;
        rect.setSize(renderWidth * 0.8f, renderHeight * 0.8f);
    }

    public abstract void attack();
    public abstract void die();
    public abstract void update();
    public abstract void affected();
    public  boolean isDead(){
        return dead;
    };



}
