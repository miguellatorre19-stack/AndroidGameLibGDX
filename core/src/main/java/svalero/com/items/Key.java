package svalero.com.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class Key extends Item implements Disposable {

    public Key(Texture keyTexture, Vector2 position) {
        super(keyTexture, position);
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }

    @Override
    public void dispose() {
        if (itemTexture != null){
            itemTexture.dispose();
            itemTexture = null;
        }
    }
    public Circle getColision(){
        return bounds;
    }

}
