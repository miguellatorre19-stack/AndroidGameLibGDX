package svalero.com.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.items.Key;
import svalero.com.managers.LevelManager;
import svalero.com.managers.SpriteManager;

import static svalero.com.utils.Constants.TILE_SIZE_PX;

public class StrongerEnemy extends Character implements Disposable {

    private static final float CHASE_SPEED_PX_PER_SEC = 24f;
    private static final float ATTACK_COOLDOWN_SEC = 0.6f;
    private static final float AGGRO_DISTANCE_PX = 12f * TILE_SIZE_PX;
    private static final float AGGRO_DISTANCE_PX_SQUARED = AGGRO_DISTANCE_PX * AGGRO_DISTANCE_PX;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> movementAnimation;
    private final Animation<TextureRegion> attackAnimation;
    private final Animation<TextureRegion> damagedAnimation;
    private final Animation<TextureRegion> deathAnimation;

    private float attackCooldown;
    private float attackTimer;
    private float damagedTimer;
    private float deathTimer;
    private boolean dying;

    public StrongerEnemy(Vector2 position,
                         SpriteManager spriteManager,
                         int maxLives,
                         Animation<TextureRegion> animation,
                         Animation<TextureRegion> idleAnimation,
                         Animation<TextureRegion> movementAnimation,
                         Animation<TextureRegion> attackAnimation,
                         Animation<TextureRegion> damagedAnimation,
                         Animation<TextureRegion> deathAnimation) {

        super(position, spriteManager, animation);
        this.idleAnimation = idleAnimation;
        this.movementAnimation = movementAnimation;
        this.attackAnimation = attackAnimation;
        this.damagedAnimation = damagedAnimation;
        this.deathAnimation = deathAnimation;
    }

    @Override
    public void render(Batch batch) {
        if (dead) return;
        super.render(batch);
    }

    @Override
    public void attack() {
        attackTimer = attackAnimation.getAnimationDuration();
        setAnimation(attackAnimation, true);
    }


    @Override
    public void die() {
        if (dead || dying) return;
        dying = true;
        deathTimer = deathAnimation.getAnimationDuration();
        setAnimation(deathAnimation, true);
    }

    @Override
    public void update() {

    }

    @Override
    public void affected() {
        if (dead || dying) return;
        lives -= 1;
        if (lives <= 0) {
            die();
            return;
        }
        damagedTimer = damagedAnimation.getAnimationDuration();
        setAnimation(damagedAnimation, true);
    }

    @Override
    public void dispose() {
        // Animation frames belong to the shared atlas and are disposed by AssetManager.
    }

    public void onProjectileHit() {
        affected();
    }

    public void updateBehavior(Player player, float dt, LevelManager levelManager) {
        if (dead || player == null) return;

        if (attackCooldown > 0f) attackCooldown -= dt;
        if (playPriorityAnimation(dt)) return;

        boolean moved = chasePlayer(player, dt, levelManager);

        if (rect.overlaps(player.getRect()) && attackCooldown <= 0f) {
            attack();
            player.affected();
            attackCooldown = ATTACK_COOLDOWN_SEC;
            return;
        }

        if (moved) {
            setAnimation(movementAnimation, false);
        } else {
            setAnimation(idleAnimation, false);
        }
    }

    private boolean playPriorityAnimation(float dt) {
        if (dying) {
            deathTimer -= dt;
            if (deathTimer <= 0f) {
                dead = true;
                spriteManager.addWorldKey(drop());
                dispose();
            }
            return true;
        }

        if (damagedTimer > 0f) {
            damagedTimer -= dt;
            setAnimation(damagedAnimation, false);
            return true;
        }

        if (attackTimer > 0f) {
            attackTimer -= dt;
            setAnimation(attackAnimation, false);
            return true;
        }

        return false;
    }

    private boolean chasePlayer(Player player, float dt, LevelManager levelManager) {
        float enemyCenterX = rect.x + rect.width * 0.5f;
        float enemyCenterY = rect.y + rect.height * 0.5f;
        float playerCenterX = player.getRect().x + player.getRect().width * 0.5f;
        float playerCenterY = player.getRect().y + player.getRect().height * 0.5f;

        float toPlayerX = playerCenterX - enemyCenterX;
        float toPlayerY = playerCenterY - enemyCenterY;
        float distanceSquared = toPlayerX * toPlayerX + toPlayerY * toPlayerY;
        if (distanceSquared > AGGRO_DISTANCE_PX_SQUARED) return false;

        float distance = (float) Math.sqrt(distanceSquared);
        if (distance <= 0.0001f) return false;

        float moveX = (toPlayerX / distance) * CHASE_SPEED_PX_PER_SEC * dt;
        float moveY = (toPlayerY / distance) * CHASE_SPEED_PX_PER_SEC * dt;
        float oldX = position.x;
        float oldY = position.y;
        moveWithCollision(moveX, moveY, levelManager);
        return oldX != position.x || oldY != position.y;
    }

    private void moveWithCollision(float moveX, float moveY, LevelManager levelManager) {
        if (moveX != 0f) {
            float oldX = position.x;
            position.x += moveX;
            rect.setPosition(position.x, position.y);
            if (levelManager != null && levelManager.isBlocked(rect, false)) {
                position.x = oldX;
                rect.setPosition(position.x, position.y);
            }
        }

        if (moveY != 0f) {
            float oldY = position.y;
            position.y += moveY;
            rect.setPosition(position.x, position.y);
            if (levelManager != null && levelManager.isBlocked(rect, false)) {
                position.y = oldY;
                rect.setPosition(position.x, position.y);
            }
        }

        position.x = MathUtils.clamp(position.x, 0f, Float.MAX_VALUE);
        position.y = MathUtils.clamp(position.y, 0f, Float.MAX_VALUE);
        rect.setPosition(position.x, position.y);
    }

    private void setAnimation(Animation<TextureRegion> nextAnimation, boolean restart) {
        if (animation != nextAnimation || restart) {
            animation = nextAnimation;
            stateTime = 0f;
        }
    }

    private Key drop(){
        return new Key(
            new Vector2(position.x, position.y)
        );
    }
}
