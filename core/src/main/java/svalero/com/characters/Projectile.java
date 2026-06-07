package svalero.com.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import static svalero.com.utils.Constants.SCALE_MODIFIER;

public class Projectile {

    private final Texture texture;
    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle bounds;
    private final Polygon hitbox;
    private final boolean fromPlayer;

    public Projectile(Texture texture, Vector2 startPosition, Vector2 velocity, boolean fromPlayer) {
        this.texture = texture;
        this.position = new Vector2(startPosition);
        this.velocity = new Vector2(velocity);
        this.fromPlayer = fromPlayer;
        float width = texture.getWidth() * SCALE_MODIFIER;
        float height = texture.getHeight() * SCALE_MODIFIER;
        this.bounds = new Rectangle(position.x, position.y, width, height);
        this.hitbox = new Polygon(new float[]{
            0f, 0f,
            width, 0f,
            width, height,
            0f, height
        });
        this.hitbox.setPosition(position.x, position.y);
    }

    public void update(float dt) {
        position.mulAdd(velocity, dt);
        bounds.setPosition(position.x, position.y);
        hitbox.setPosition(position.x, position.y);
    }

    public void render(Batch batch) {
        batch.draw(texture, position.x, position.y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Polygon getHitbox() {
        return hitbox;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }
}
