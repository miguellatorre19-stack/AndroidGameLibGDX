package svalero.com.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.items.Coin;
import svalero.com.items.Key;
import svalero.com.managers.AudioManager;
import svalero.com.managers.SpriteManager;
import svalero.com.screens.MainMenuScreen;

import static svalero.com.utils.Constants.PLAYER_BOOST_DURATION_SEC;
import static svalero.com.utils.Constants.PlayerSpeed_PxPerSec;
import static svalero.com.utils.Constants.PLAYER_RENDER_SCALE;

public class Player extends Character implements Disposable {
    private static final float BOOST_SPEED_MULTIPLIER = 1.5f;

    private int keysInInventory;

    public int getCoinsInInventory() {
        return coinsInInventory;
    }

    public int getKeysInInventory() {
        return keysInInventory;
    }
    private int coinsInInventory;
    public int price;

    private boolean isBoosted;
    private float boostTimeRemainingSec;
    private float stunTimeRemainingSec;

    private AudioManager audioManager;
    public void setPrice(int price) {
        this.price = price;
    }


    public Player(Vector2 position, SpriteManager spriteManager, Animation<TextureRegion> playerAnimation) {
        super(position, spriteManager, playerAnimation);
        setRenderScale(PLAYER_RENDER_SCALE);
        configureHitbox(0.68f, 0.72f, 0.16f, 0.08f);
        lives = 3;
        isBoosted = false;
        boostTimeRemainingSec = 0f;
        stunTimeRemainingSec = 0f;
        audioManager = new AudioManager();
        audioManager.loadSfx("damaged", "audio/sound/armor-light.wav");
        audioManager.loadSfx("coin_pickup", "audio/sound/coin.mp3");
        audioManager.loadSfx("key_pickup", "audio/sound/keys_pickup.mp3");
    }

    @Override
    public void attack() {

    }

    @Override
    public void die() {
        // El flujo de cambio de pantalla por muerte lo gestiona GameScreen para
        // asegurar que se liberen correctamente todos los managers (incluido audio).
    }

    @Override
    public void affected() {
        if (dead) return;
        audioManager.playSfx("damaged");
        lives -= 1;
        if (lives <= 0) {
            dead = true;
            die();
        }
    }

    public boolean hasKey() {
        return keysInInventory > 0;
    }

    public void getKey(Key key){
        if (key == null || key.isCollected()) return;

        if (Intersector.overlaps(key.getColision(), rect)){
            keysInInventory +=1;
            audioManager.playSfx("key_pickup");
            key.collect();
        }
    }

    public void removeKey() {
        if (keysInInventory > 0) {
            keysInInventory -=1;
        }
    }

    public void getCoin(Coin coin){
        if (coin == null || coin.isCollected()) return;
        if (Intersector.overlaps(coin.getColision(), rect)) {
            audioManager.playSfx("coin_pickup");
            coinsInInventory +=1;
            coin.collect();
        }
    }
    public void removeCoins(){
        if (price >= coinsInInventory) return;
        if(coinsInInventory > 0) coinsInInventory -= price;
    }


    public boolean hasBoost(){
        return isBoosted && boostTimeRemainingSec > 0f;
    }

    public void updateBoost(float dt) {
        if (!hasBoost()) return;

        boostTimeRemainingSec -= dt;
        if (boostTimeRemainingSec <= 0f) {
            boostTimeRemainingSec = 0f;
            isBoosted = false;
        }
    }

    public void stun(float durationSec) {
        if (dead || durationSec <= 0f) return;
        stunTimeRemainingSec = Math.max(stunTimeRemainingSec, durationSec);
    }

    public void updateStun(float dt) {
        if (!isStunned()) return;
        stunTimeRemainingSec -= dt;
        if (stunTimeRemainingSec <= 0f) {
            stunTimeRemainingSec = 0f;
        }
    }

    public boolean isStunned() {
        return stunTimeRemainingSec > 0f;
    }

    public float getMoveSpeedPxPerSec() {
        if (isStunned()) return 0f;
        if (hasBoost()) return PlayerSpeed_PxPerSec * BOOST_SPEED_MULTIPLIER;
        return PlayerSpeed_PxPerSec;
    }

    public float getBoostTimeRemainingSec() {
        return boostTimeRemainingSec;
    }

    public float getBoostProgress01() {
        if (!hasBoost()) return 0f;
        return boostTimeRemainingSec / PLAYER_BOOST_DURATION_SEC;
    }

    @Override
    public void update() {
        // Boost timer is updated from GameScreen logic(delta) to keep all gameplay timers centralized.
    }

    @Override
    public void dispose() {
        // Animation frames belong to the shared atlas and are disposed by AssetManager.
    }
}
