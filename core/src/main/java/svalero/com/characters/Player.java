package svalero.com.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import lombok.Data;
import svalero.com.items.Key;
import svalero.com.managers.SpriteManager;
import svalero.com.screens.MainMenuScreen;

import static svalero.com.utils.Constants.PLAYER_RENDER_SCALE;

@Data
public class Player extends Character implements Disposable {

    Texture playerTexture;
    private int keysInInventory;

    public Player(Texture playerTexture, Vector2 position, SpriteManager spriteManager) {
        super(playerTexture, position, spriteManager);
        setRenderScale(PLAYER_RENDER_SCALE);
        lives = 3;
        keysInInventory = 0;
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

    public boolean hasKey() {
        return keysInInventory > 0;
    }

    public void getKey(Key key){
        if (key == null || key.isCollected()) return;

        if (Intersector.overlaps(key.getColision(), rect)){
            keysInInventory +=1;
            key.collect();
        }
    }
    public void removeKey() {
        if (keysInInventory > 0) {
            keysInInventory -=1;
        }
    }

    @Override
    public void update() {

    }

    public void resurrect(){

    }

    @Override
    public void affected() {
        if (dead) return;
        lives -= 1;
        if (lives <= 0) {
            dead = true;
            die();
        }
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    public float setPosition(float x) {
        return x;
    }
}
