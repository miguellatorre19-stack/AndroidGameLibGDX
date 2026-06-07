package svalero.com.characters.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import svalero.com.characters.Player;
import svalero.com.managers.LevelManager;
import svalero.com.managers.SpriteManager;

public class StrongerEnemy extends Enemy {
    private static final float PARALYZE_COOLDOWN_SEC = 20f;
    private static final float PARALYZE_DURATION_SEC = 1f;
    private static final float AGGRO_SLOW_DURATION_SEC = 2.25f;
    private static final float AGGRO_SLOW_COOLDOWN_SEC = 8f;

    private float paralyzeCooldown;
    private float aggroSlowCooldown;
    private Player trackedPlayer;

    public StrongerEnemy(
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
            EnemyConfig.stronger(maxLives),
            idleAnimation,
            movementAnimation,
            attackAnimation,
            damagedAnimation,
            deathAnimation
        );
    }

    public StrongerEnemy(
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
            config,
            idleAnimation,
            movementAnimation,
            attackAnimation,
            damagedAnimation,
            deathAnimation
        );
        paralyzeCooldown = 0f;
        aggroSlowCooldown = 0f;
    }

    @Override
    public void updateBehavior(Player player, float dt, LevelManager levelManager, Array<Enemy> nearbyEnemies) {
        trackedPlayer = player;
        if (paralyzeCooldown > 0f) {
            paralyzeCooldown -= dt;
        }
        if (aggroSlowCooldown > 0f) {
            aggroSlowCooldown -= dt;
        }

        super.updateBehavior(player, dt, levelManager, nearbyEnemies);
        tryParalyze(player);
    }

    @Override
    public void onEnterChaseState() {
        super.onEnterChaseState();
        if (trackedPlayer == null || trackedPlayer.isDead()) return;
        if (aggroSlowCooldown > 0f) return;

        trackedPlayer.slow(AGGRO_SLOW_DURATION_SEC);
        aggroSlowCooldown = AGGRO_SLOW_COOLDOWN_SEC;
    }

    private void tryParalyze(Player player) {
        if (player == null || player.isDead()) return;
        if (isDead() || paralyzeCooldown > 0f) return;
        if (!canSeePlayer()) return;

        player.stun(PARALYZE_DURATION_SEC);
        paralyzeCooldown = PARALYZE_COOLDOWN_SEC;
    }
}
