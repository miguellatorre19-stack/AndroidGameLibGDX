package svalero.com.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.items.Coin;
import svalero.com.items.Key;
import svalero.com.items.PowerUp;
import svalero.com.items.PowerUpType;
import svalero.com.managers.AudioManager;
import svalero.com.managers.ResourceManager;
import svalero.com.managers.SpriteManager;

import java.util.EnumMap;

import static svalero.com.utils.Constants.PlayerSpeed_PxPerSec;
import static svalero.com.utils.Constants.PLAYER_RENDER_SCALE;

public class Player extends Character implements Disposable {
    private static final float SPEED_POWER_UP_MULTIPLIER = 1.5f;
    private static final float MAX_MANA = 100f;
    private static final float ATTACK_MANA_COST = MAX_MANA * 0.25f;
    private static final float MANA_REGEN_PER_SEC = 2.5f;
    private static final float SLOW_SPEED_MULTIPLIER = 0.55f;
    private static final float ATTACK_DURATION_SEC = 0.22f;
    private static final float ATTACK_COOLDOWN_SEC = 0.28f;
    private static final float ATTACK_RANGE_PX = 32f;
    private static final float ATTACK_HEIGHT_PX = 18f;
    private static final float ATTACK_ANIMATION_FRAME_DURATION = 0.055f;

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
    private float slowTimeRemainingSec;
    private float mana;
    private float attackTimeRemainingSec;
    private float attackCooldownRemainingSec;
    private boolean attackDamageAvailable;
    private final Rectangle attackBounds;
    private final Polygon attackHitbox;
    private final Animation<TextureRegion> attackAnimation;

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
        slowTimeRemainingSec = 0f;
        mana = MAX_MANA;
        attackTimeRemainingSec = 0f;
        attackCooldownRemainingSec = 0f;
        attackDamageAvailable = false;
        attackBounds = new Rectangle();
        attackHitbox = new Polygon(new float[]{
            0f, 0f,
            ATTACK_RANGE_PX, 0f,
            ATTACK_RANGE_PX, ATTACK_HEIGHT_PX,
            0f, ATTACK_HEIGHT_PX
        });
        attackAnimation = ResourceManager.buildIndexedAnimation(
            ResourceManager.SKILLS_ATLAS_ID,
            "flamethrower_2",
            ATTACK_ANIMATION_FRAME_DURATION,
            Animation.PlayMode.NORMAL
        );
        audioManager = new AudioManager();
        audioManager.loadSfx("damaged", "audio/sound/armor-light.wav");
        audioManager.loadSfx("coin_pickup", "audio/sound/coin.mp3");
        audioManager.loadSfx("key_pickup", "audio/sound/keys_pickup.mp3");
    }

    @Override
    public void attack() {
        tryMeleeAttack();
    }

    public boolean tryMeleeAttack() {
        if (dead || isStunned()) return false;
        if (attackCooldownRemainingSec > 0f || isMeleeAttackActive()) return false;
        if (mana < ATTACK_MANA_COST) return false;

        mana -= ATTACK_MANA_COST;
        attackTimeRemainingSec = ATTACK_DURATION_SEC;
        attackCooldownRemainingSec = ATTACK_COOLDOWN_SEC;
        attackDamageAvailable = true;
        updateAttackHitbox();
        return true;
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

    public void updateManaAndAttack(float dt) {
        mana = Math.min(MAX_MANA, mana + MANA_REGEN_PER_SEC * dt);

        if (attackCooldownRemainingSec > 0f) {
            attackCooldownRemainingSec = Math.max(0f, attackCooldownRemainingSec - dt);
        }

        if (attackTimeRemainingSec > 0f) {
            attackTimeRemainingSec = Math.max(0f, attackTimeRemainingSec - dt);
            updateAttackHitbox();
            if (attackTimeRemainingSec == 0f) {
                attackDamageAvailable = false;
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

    public void slow(float durationSec) {
        if (dead || durationSec <= 0f) return;
        slowTimeRemainingSec = Math.max(slowTimeRemainingSec, durationSec);
    }

    public void updateStun(float dt) {
        if (!isStunned()) return;
        stunTimeRemainingSec -= dt;
        if (stunTimeRemainingSec <= 0f) {
            stunTimeRemainingSec = 0f;
        }
    }

    public void updateSlow(float dt) {
        if (!isSlowed()) return;
        slowTimeRemainingSec -= dt;
        if (slowTimeRemainingSec <= 0f) {
            slowTimeRemainingSec = 0f;
        }
    }

    public boolean isStunned() {
        return stunTimeRemainingSec > 0f;
    }

    public boolean isSlowed() {
        return slowTimeRemainingSec > 0f;
    }

    public float getStunTimeRemainingSec() {
        return stunTimeRemainingSec;
    }

    public float getSlowTimeRemainingSec() {
        return slowTimeRemainingSec;
    }

    public float getMoveSpeedPxPerSec() {
        if (isStunned()) return 0f;
        float speed = PlayerSpeed_PxPerSec;
        if (hasBoost()) {
            speed *= SPEED_POWER_UP_MULTIPLIER;
        }
        if (isSlowed()) {
            speed *= SLOW_SPEED_MULTIPLIER;
        }
        return speed;
    }

    public float getBoostTimeRemainingSec() {
        return getPowerUpTimeRemainingSec(PowerUpType.SPEED);
    }

    public float getBoostProgress01() {
        if (!hasBoost()) return 0f;
        return getPowerUpTimeRemainingSec(PowerUpType.SPEED) / PowerUpType.SPEED.durationSec();
    }

    public boolean isMeleeAttackActive() {
        return attackTimeRemainingSec > 0f;
    }

    public boolean canMeleeAttackDamage() {
        return isMeleeAttackActive() && attackDamageAvailable;
    }

    public void consumeMeleeAttackDamage() {
        attackDamageAvailable = false;
    }

    public Polygon getMeleeAttackHitbox() {
        updateAttackHitbox();
        return attackHitbox;
    }

    public float getManaProgress01() {
        return mana / MAX_MANA;
    }

    public float getMana() {
        return mana;
    }

    public float getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
        renderMeleeAttack(batch);
    }

    private boolean consumeShieldCharge() {
        if (!shieldChargeActive || !isPowerUpActive(PowerUpType.SHIELD)) return false;
        shieldChargeActive = false;
        activePowerUpTimers.put(PowerUpType.SHIELD, 0f);
        return true;
    }

    private void updateAttackHitbox() {
        float x = facingRight ? rect.x + rect.width : rect.x - ATTACK_RANGE_PX;
        float y = rect.y + rect.height * 0.5f - ATTACK_HEIGHT_PX * 0.5f;
        attackBounds.set(x, y, ATTACK_RANGE_PX, ATTACK_HEIGHT_PX);
        attackHitbox.setPosition(attackBounds.x, attackBounds.y);
    }

    private void renderMeleeAttack(Batch batch) {
        if (!isMeleeAttackActive()) return;

        float elapsed = ATTACK_DURATION_SEC - attackTimeRemainingSec;
        TextureRegion frame = attackAnimation.getKeyFrame(elapsed, false);
        float drawX = facingRight ? attackBounds.x : attackBounds.x + attackBounds.width;
        float drawWidth = facingRight ? attackBounds.width : -attackBounds.width;
        batch.draw(frame, drawX, attackBounds.y, drawWidth, attackBounds.height);
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
