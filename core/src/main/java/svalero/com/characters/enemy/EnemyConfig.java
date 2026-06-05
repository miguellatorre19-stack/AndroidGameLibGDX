package svalero.com.characters.enemy;

import static svalero.com.utils.Constants.TILE_SIZE_PX;

public record EnemyConfig(
    int maxLives,
    float chaseSpeedPxPerSec,
    float attackCooldownSec,
    float aggroDistancePx,
    float patrolRadiusPx
) {

    public EnemyConfig {
        if (maxLives <= 0) throw new IllegalArgumentException("maxLives must be > 0");
        if (chaseSpeedPxPerSec <= 0f) throw new IllegalArgumentException("chaseSpeedPxPerSec must be > 0");
        if (attackCooldownSec < 0f) throw new IllegalArgumentException("attackCooldownSec must be >= 0");
        if (aggroDistancePx <= 0f) throw new IllegalArgumentException("aggroDistancePx must be > 0");
        if (patrolRadiusPx < 0f) throw new IllegalArgumentException("patrolRadiusPx must be >= 0");
    }

    public EnemyConfig withMaxLives(int maxLives) {
        return new EnemyConfig(maxLives, chaseSpeedPxPerSec, attackCooldownSec, aggroDistancePx, patrolRadiusPx);
    }

    public static EnemyConfig skeleton(int maxLives) {
        return new EnemyConfig(
            maxLives,
            24f,
            0.3f,
            5f * TILE_SIZE_PX,
            2f * TILE_SIZE_PX
        );
    }

    public static EnemyConfig stronger(int maxLives) {
        return new EnemyConfig(
            maxLives,
            24f,
            0.6f,
            12f * TILE_SIZE_PX,
            3f * TILE_SIZE_PX
        );
    }
}
