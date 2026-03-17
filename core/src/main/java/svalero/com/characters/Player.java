package svalero.com.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import lombok.Data;
import svalero.com.managers.SpriteManager;
import svalero.com.screens.MainMenuScreen;

import static com.badlogic.gdx.math.Interpolation.circle;
import static svalero.com.utils.Constants.PLAYER_RENDER_SCALE;

@Data
public class Player extends Character implements Disposable {

    Texture playerTexture;
    private boolean hasKey;

    public Player(Texture playerTexture, Vector2 position, SpriteManager spriteManager) {
        super(playerTexture, position, spriteManager);
        setRenderScale(PLAYER_RENDER_SCALE);
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }

    @Override
    public void attack() {

    }

    @Override
    public void die() {
        game.setScreen(new MainMenuScreen(game));
        dispose();
    }

    public void interact(){
    }

    public boolean hasKey() {
        return hasKey;
    }

    public void giveKey() {
        hasKey = true;
    }

    public void removeKey() {
        hasKey = false;
    }

    @Override
    public void update() {

    }

    public void resurrect(){

    }

    @Override
    public void checkColisions(SpriteManager spriteManager) {
        if (Intersector.overlaps(rect, rect)){
            affected();
        }
    }

    @Override
    public void affected() {
        lives -= 1;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    public float setPosition(float x) {
        return x;
    }
}
