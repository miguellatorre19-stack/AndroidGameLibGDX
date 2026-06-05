package svalero.com.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class PowerUp extends Item implements Disposable {
    private static final float WORLD_SIZE_PX = 16f;
    private static final float COLLISION_RADIUS_PX = 8f;

    private final PowerUpType type;
    private final Texture texture;

    public PowerUp(PowerUpType type, Vector2 position) {
        this(type, loadTexture(type), position);
    }

    private PowerUp(PowerUpType type, Texture texture, Vector2 position) {
        super(new Animation<>(1f, new TextureRegion(texture)), position);
        this.type = type;
        this.texture = texture;
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.bounds = new Circle(
            position.x + COLLISION_RADIUS_PX,
            position.y + COLLISION_RADIUS_PX,
            COLLISION_RADIUS_PX
        );
    }

    public PowerUpType getType() {
        return type;
    }

    public Circle getColision() {
        return bounds;
    }

    @Override
    public void render(Batch batch) {
        if (hasBeenCollected) return;
        batch.draw(texture, position.x, position.y, WORLD_SIZE_PX, WORLD_SIZE_PX);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    private static Texture loadTexture(PowerUpType type) {
        return switch (type) {
            case SPEED -> new Texture("hud/Green_Potion.png");
            case SHIELD -> new Texture("hud/Blue_Potion.png");
            case MASTER_KEY -> new Texture("hud/Red_Potion.png");
        };
    }
}
