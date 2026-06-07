package svalero.com.characters.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.characters.Character;
import svalero.com.items.Key;
import svalero.com.managers.SpriteManager;

abstract class EnemyActor extends Character implements Disposable {

    protected final Animation<TextureRegion> idleAnimation;
    protected final Animation<TextureRegion> movementAnimation;
    protected final Animation<TextureRegion> attackAnimation;
    protected final Animation<TextureRegion> damagedAnimation;
    protected final Animation<TextureRegion> deathAnimation;

    private float attackTimer;
    private float damagedTimer;
    private float deathTimer;
    private boolean dying;

    protected EnemyActor(
        Vector2 position,
        SpriteManager spriteManager,
        int maxLives,
        Animation<TextureRegion> idleAnimation,
        Animation<TextureRegion> movementAnimation,
        Animation<TextureRegion> attackAnimation,
        Animation<TextureRegion> damagedAnimation,
        Animation<TextureRegion> deathAnimation
    ) {
        super(position, spriteManager, idleAnimation);
        this.idleAnimation = idleAnimation;
        this.movementAnimation = movementAnimation;
        this.attackAnimation = attackAnimation;
        this.damagedAnimation = damagedAnimation;
        this.deathAnimation = deathAnimation;
        this.lives = maxLives;
    }

    @Override
    public void render(Batch batch) {
        if (dead) return;
        super.render(batch);
    }

    protected void playAttackAnimation() {
        attackTimer = attackAnimation.getAnimationDuration();
        setAnimation(attackAnimation, true);
    }

    protected void playDamagedAnimation() {
        damagedTimer = damagedAnimation.getAnimationDuration();
        setAnimation(damagedAnimation, true);
    }

    protected void playDeathAnimation() {
        if (dying || dead) return;
        dying = true;
        deathTimer = deathAnimation.getAnimationDuration();
        setAnimation(deathAnimation, true);
    }

    protected void setIdleAnimation() {
        setAnimation(idleAnimation, false);
    }

    protected void setMovementAnimation() {
        setAnimation(movementAnimation, false);
    }

    protected boolean isDying() {
        return dying;
    }

    protected boolean playPriorityAnimation(float dt, Runnable onDeathAnimationFinished) {
        if (dying) {
            deathTimer -= dt;
            if (deathTimer <= 0f) {
                dead = true;
                if (onDeathAnimationFinished != null) {
                    onDeathAnimationFinished.run();
                }
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

    protected void loseLife(int amount) {
        lives -= amount;
    }

    protected int getLivesCount() {
        return lives;
    }

    protected Key createDropKey() {
        return new Key(new Vector2(position.x, position.y));
    }

    protected void setAnimation(Animation<TextureRegion> nextAnimation, boolean restart) {
        if (animation != nextAnimation || restart) {
            animation = nextAnimation;
            stateTime = 0f;
        }
    }

    @Override
    public void dispose() {
        // Animation frames belong to shared atlas and are disposed by AssetManager.
    }
}
