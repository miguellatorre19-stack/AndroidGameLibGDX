package svalero.com.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.items.Coin;
import svalero.com.items.Key;
import svalero.com.managers.SpriteManager;
import svalero.com.screens.MainMenuScreen;

import static svalero.com.utils.Constants.PLAYER_RENDER_SCALE;

public class Player extends Character implements Disposable {

    private int keysInInventory;

    public int getCoinsInInventory() {
        return coinsInInventory;
    }

    private int coinsInInventory;

    public int price;

    public void setPrice(int price) {
        this.price = price;
    }


    public Player(Vector2 position, SpriteManager spriteManager, Animation<TextureRegion> playerAnimation) {
        super(position, spriteManager, playerAnimation);
        setRenderScale(PLAYER_RENDER_SCALE);
        lives = 3;
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

    public void getCoin(Coin coin){
        if( coin == null)return;;

        if(Intersector.overlaps(coin.getColision(), rect)){
            coinsInInventory +=1;
            coin.collect();
        }
    }

    public void removeKey() {
        if (keysInInventory > 0) keysInInventory -=1;
    }

    public void removeCoins(){
        if (price >= coinsInInventory) return;
        if(coinsInInventory > 0) coinsInInventory -= price;
    }

    @Override
    public void update() {

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
        // Animation frames belong to the shared atlas and are disposed by AssetManager.
    }
}
