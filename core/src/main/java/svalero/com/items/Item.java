package svalero.com.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public abstract class Item {

    protected Animation<TextureRegion> itemAnimation;
    protected Vector2 position;
    protected Circle bounds;
    protected boolean hasBeenCollected = false;
    protected float stateTime = 0f;

    public Item(Animation<TextureRegion> itemAnimation, Vector2 position){
        this.itemAnimation = itemAnimation;
        this.position = new Vector2(position);
        TextureRegion initialFrame = itemAnimation.getKeyFrame(0f, true);
        float radius = initialFrame.getRegionWidth() * 0.5f;
        this.bounds = new Circle(position.x + radius, position.y + radius, radius);
    }

    public void render(Batch batch) {
        if (hasBeenCollected) return;
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion frame = itemAnimation.getKeyFrame(stateTime, true);
        batch.draw(frame, position.x, position.y);
    }

    public boolean isCollected() {
        return hasBeenCollected;
    }

    public void collect() {
        hasBeenCollected = true;
    }

}
