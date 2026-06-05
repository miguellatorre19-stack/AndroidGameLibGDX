package svalero.com.characters.enemy;

import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import svalero.com.StateMachine.EnemyState;
import svalero.com.characters.Player;
import svalero.com.managers.LevelManager;
import svalero.com.managers.SpriteManager;

public class Enemy extends EnemyActor {

    private final EnemyCombat combat;
    private final EnemyAIController aiController;

    public Enemy(
        Vector2 position,
        SpriteManager spriteManager,
        int maxLives,
        Animation<TextureRegion> idleAnimation,
        Animation<TextureRegion> movementAnimation,
        Animation<TextureRegion> attackAnimation,
        Animation<TextureRegion> damagedAnimation,
        Animation<TextureRegion> deathAnimation
    ) {
        this(
            position,
            spriteManager,
            EnemyConfig.skeleton(maxLives),
            idleAnimation,
            movementAnimation,
            attackAnimation,
            damagedAnimation,
            deathAnimation
        );
    }

    public Enemy(
        Vector2 position,
        SpriteManager spriteManager,
        EnemyConfig config,
        Animation<TextureRegion> idleAnimation,
        Animation<TextureRegion> movementAnimation,
        Animation<TextureRegion> attackAnimation,
        Animation<TextureRegion> damagedAnimation,
        Animation<TextureRegion> deathAnimation
    ) {
        super(
            position,
            spriteManager,
            config.maxLives(),
            idleAnimation,
            movementAnimation,
            attackAnimation,
            damagedAnimation,
            deathAnimation
        );
        this.combat = new EnemyCombat(this, config.attackCooldownSec());
        this.aiController = new EnemyAIController(
            this,
            config.chaseSpeedPxPerSec(),
            config.aggroDistancePx(),
            config.patrolRadiusPx()
        );
    }

    @Override
    public void attack() {
        combat.attack(null);
    }

    void attack(Player target) {
        combat.attack(target);
    }

    @Override
    public void die() {
        combat.die();
    }

    @Override
    public void update() {
        // AI update is handled by SpriteManager.updateEnemies.
    }

    @Override
    public void affected() {
        combat.takeDamage();
    }

    @Override
    public void dispose() {
        super.dispose();
        combat.dispose();
    }

    public void onProjectileHit() {
        combat.takeDamage();
    }

    public void updateBehavior(Player player, float dt, LevelManager levelManager) {
        updateBehavior(player, dt, levelManager, null);
    }

    public void updateBehavior(Player player, float dt, LevelManager levelManager, Array<Enemy> nearbyEnemies) {
        if (dead || player == null) return;
        combat.update(dt);
        if (playPriorityAnimation(dt, this::handleDeathAnimationEnd)) return;
        aiController.update(player, levelManager, dt, nearbyEnemies);
    }

    private void handleDeathAnimationEnd() {
        spriteManager.addWorldKey(createDropKey());
        dispose();
    }

    public StateMachine<Enemy, EnemyState> getStateMachine() {
        return aiController.getStateMachine();
    }

    public boolean canSeePlayer() {
        return aiController.canSeePlayer();
    }

    public boolean canTrackPlayer() {
        return aiController.canTrackPlayer();
    }

    public boolean canAttackPlayer() {
        return aiController.canAttackPlayer();
    }

    public void patrol() {
        aiController.patrol();
    }

    public void chasePlayerWithSteering() {
        aiController.chasePlayerWithSteering();
    }

    public void tryAttackPlayer() {
        aiController.tryAttackPlayer();
    }

    public void onEnterPatrolState() {
        aiController.onEnterPatrolState();
    }

    public void onExitPatrolState() {
    }

    public void onEnterChaseState() {
    }

    public void onExitChaseState() {
    }

    public void onEnterAttackState() {
        aiController.onEnterAttackState();
    }

    public void onExitAttackState() {
    }

    Vector2 aiPosition() {
        return position;
    }

    Rectangle aiRect() {
        return rect;
    }

    Polygon aiHitbox() {
        return hitbox;
    }

    void aiSyncHitboxFromPosition() {
        syncHitboxFromPosition();
    }

    void aiUpdateFacingFromMovement(float moveX) {
        updateFacingFromMovement(moveX);
    }
}
