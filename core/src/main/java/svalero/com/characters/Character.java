package svalero.com.characters;

import com.badlogic.gdx.graphics.Texture;
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
    protected float stateTime;
    protected TextureRegion currentFrame;
    protected Rectangle rect;
    protected int lives;
    protected boolean dead = false;
    protected Texture texture;
    protected SpriteManager spriteManager;
    protected float renderWidth;
    protected float renderHeight;

    public Character(Texture texture, Vector2 position, SpriteManager spriteManager){
        this.game = spriteManager.getGame();
        this.texture = texture;
        this.position = position;
        this.spriteManager = spriteManager;
        renderWidth = texture.getWidth();
        renderHeight = texture.getHeight();
        rect = new Rectangle(position.x, position.y, renderWidth, renderHeight);
    }

    public void render(Batch batch){
        batch.draw(texture, position.x, position.y, renderWidth, renderHeight);
    }

    protected void setRenderScale(float scale){
        renderWidth = texture.getWidth() * scale;
        renderHeight = texture.getHeight() * scale;
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
