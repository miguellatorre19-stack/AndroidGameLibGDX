package svalero.com.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.managers.ResourceManager;

public class Coin extends Item implements Disposable {

    private static final String ATLAS_ID = ResourceManager.ITEMS_ATLAS_ID;
    private static final String COIN_IDLE = "coin";
    private static final float KEY_FRAME_DURATION = 0.12f;

    public Coin(Animation<TextureRegion> Coin, Vector2 position) {
        super(Coin, position);
    }

    public Coin(Vector2 position){
        this(
            ResourceManager.buildIndexedAnimation(ATLAS_ID, COIN_IDLE, KEY_FRAME_DURATION, Animation.PlayMode.LOOP),
            position
        );
    }

    public void render(Batch batch){
        super.render(batch);
    }

    @Override
    public void dispose() {

    }

    public Circle getColision(){
        return bounds;
    }
}
