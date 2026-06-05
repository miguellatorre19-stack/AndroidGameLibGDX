package svalero.com.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.items.Coin;
import svalero.com.items.Key;
import svalero.com.items.PowerUp;
import svalero.com.items.PowerUpType;
import svalero.com.managers.AudioManager;
import svalero.com.managers.SpriteManager;

import java.util.EnumMap;

import static svalero.com.utils.Constants.PlayerSpeed_PxPerSec;
import static svalero.com.utils.Constants.PLAYER_RENDER_SCALE;

public class Player extends Character implements Disposable {
    private static final float SPEED_POWER_UP_MULTIPLIER = 1.5f;

    private int keysInInventory;

    public int getCoinsInInventory() {
        return coinsInInventory;
    }

    public int getKeysInInventory() {
        return keysInInventory;
    }
    private int coinsInInventory;
    public int price;

    private final EnumMap<PowerUpType, Integer> powerUpInventory;
    private final EnumMap<PowerUpType, Float> activePowerUpTimers;
    private boolean shieldChargeActive;
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
        powerUpInventory = new EnumMap<>(PowerUpType.class);
        activePowerUpTimers = new EnumMap<>(PowerUpType.class);
        for (PowerUpType type : PowerUpType.values()) {
            powerUpInventory.put(type, 0);
            activePowerUpTimers.put(type, 0f);
        }
        shieldChargeActive = false;
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
        if (consumeShieldCharge()) return;

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

    public void getPowerUp(PowerUp powerUp) {
        if (powerUp == null || powerUp.isCollected()) return;
        if (!Intersector.overlaps(powerUp.getColision(), rect)) return;

        addPowerUp(powerUp.getType());
        audioManager.playSfx("key_pickup");
        powerUp.collect();
    }

    public void addPowerUp(PowerUpType type) {
        if (type == null) return;
        powerUpInventory.put(type, getPowerUpCount(type) + 1);
    }

    public boolean activatePowerUp(PowerUpType type) {
        if (type == null || getPowerUpCount(type) <= 0 || isPowerUpActive(type)) return false;

        powerUpInventory.put(type, getPowerUpCount(type) - 1);
        activePowerUpTimers.put(type, type.durationSec());
        if (type == PowerUpType.SHIELD) {
            shieldChargeActive = true;
        }
        return true;
    }

    public void updatePowerUps(float dt) {
        for (PowerUpType type : PowerUpType.values()) {
            float remaining = getPowerUpTimeRemainingSec(type);
            if (remaining <= 0f) continue;

            remaining = Math.max(0f, remaining - dt);
            activePowerUpTimers.put(type, remaining);
            if (remaining == 0f && type == PowerUpType.SHIELD) {
                shieldChargeActive = false;
            }
        }
    }

    public boolean isPowerUpActive(PowerUpType type) {
        return getPowerUpTimeRemainingSec(type) > 0f;
    }

    public int getPowerUpCount(PowerUpType type) {
        if (type == null) return 0;
        return powerUpInventory.getOrDefault(type, 0);
    }

    public float getPowerUpTimeRemainingSec(PowerUpType type) {
        if (type == null) return 0f;
        return activePowerUpTimers.getOrDefault(type, 0f);
    }

    public boolean hasMasterKeyActive() {
        return isPowerUpActive(PowerUpType.MASTER_KEY);
    }

    public boolean hasBoost(){
        return isPowerUpActive(PowerUpType.SPEED);
    }

    public void updateBoost(float dt) {
        updatePowerUps(dt);
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
        if (hasBoost()) return PlayerSpeed_PxPerSec * SPEED_POWER_UP_MULTIPLIER;
        return PlayerSpeed_PxPerSec;
    }

    public float getBoostTimeRemainingSec() {
        return getPowerUpTimeRemainingSec(PowerUpType.SPEED);
    }

    public float getBoostProgress01() {
        if (!hasBoost()) return 0f;
        return getPowerUpTimeRemainingSec(PowerUpType.SPEED) / PowerUpType.SPEED.durationSec();
    }

    private boolean consumeShieldCharge() {
        if (!shieldChargeActive || !isPowerUpActive(PowerUpType.SHIELD)) return false;
        shieldChargeActive = false;
        activePowerUpTimers.put(PowerUpType.SHIELD, 0f);
        return true;
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
