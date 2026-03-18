package svalero.com.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public abstract class Item {

    protected Texture itemTexture;
    protected Vector2 position;
    protected Circle bounds;
    protected boolean hasBeenCollected = false;

    public Item(Texture itemTexture, Vector2 position){
        this.itemTexture = itemTexture;
        this.position = new Vector2(position);
        float radius = itemTexture.getWidth() * 0.5f;
        this.bounds = new Circle(position.x + radius, position.y + radius, radius);
    }

    public void render(Batch batch) {
        if (hasBeenCollected) return;
        batch.draw(itemTexture, position.x, position.y);
    }

    public boolean isCollected() {
        return hasBeenCollected;
    }

    public void collect() {
        hasBeenCollected = true;
    }

}
