package svalero.com.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.managers.ResourceManager;

public class Key extends Item implements Disposable {

    private static final String KEY_IDLE_REGION = "keys_1";
    private static final float KEY_FRAME_DURATION = 0.12f;

    public Key(Animation<TextureRegion> keyIdle, Vector2 position) {
        super(keyIdle, position);
    }

    public Key(Vector2 position) {
        this(
            ResourceManager.buildIndexedAnimation(KEY_IDLE_REGION, KEY_FRAME_DURATION, Animation.PlayMode.LOOP),
            position
        );
    }


    @Override
    public void render(Batch batch) {
        super.render(batch);
    }

    @Override
    public void dispose() {
        // Frames belong to the shared atlas and are disposed by ResourceManager.
    }
    public Circle getColision(){
        return bounds;
    }

}
