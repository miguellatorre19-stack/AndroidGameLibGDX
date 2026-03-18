package svalero.com.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import svalero.com.managers.LevelManager;
import svalero.com.managers.SpriteManager;

import static svalero.com.utils.Constants.TILE_SIZE_PX;

public class Enemy extends Character implements Disposable {

    private static final float CHASE_SPEED_PX_PER_SEC = 24f;
    private static final float ATTACK_COOLDOWN_SEC = 0.6f;
    private static final float AGGRO_DISTANCE_PX = 2f * TILE_SIZE_PX;
    private static final float AGGRO_DISTANCE_PX_SQUARED = AGGRO_DISTANCE_PX * AGGRO_DISTANCE_PX;

    private float attackCooldown;

    public Enemy(Texture texture, Vector2 position, SpriteManager spriteManager, int maxLives) {
        super(texture, position, spriteManager);
        lives = maxLives;
        attackCooldown = 0f;
    }

    @Override
    public void render(Batch batch) {
        if (dead) return;
        super.render(batch);
    }

    @Override
    public void attack() {

    }

    @Override
    public void die() {
        if (dead) return;
        dead = true;
        dispose();
    }

    @Override
    public void update() {

    }

    @Override
    public void checkColisions(SpriteManager spriteManager) {

    }

    @Override
    public void affected() {
        if (dead) return;
        lives -= 1;
        if (lives <= 0) {
            die();
        }
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }

    public void onProjectileHit() {
        affected();
    }

    public void updateBehavior(Player player, float dt, LevelManager levelManager) {
        if (dead || player == null) return;

        if (attackCooldown > 0f) {
            attackCooldown -= dt;
        }

        float toPlayerX = player.getRect().x - rect.x;
        float toPlayerY = player.getRect().y - rect.y;
        float distanceSquared = toPlayerX * toPlayerX + toPlayerY * toPlayerY;

        if (distanceSquared <= AGGRO_DISTANCE_PX_SQUARED) {
            float distance = (float) Math.sqrt(distanceSquared);
            if (distance > 0.0001f) {
                float moveX = (toPlayerX / distance) * CHASE_SPEED_PX_PER_SEC * dt;
                float moveY = (toPlayerY / distance) * CHASE_SPEED_PX_PER_SEC * dt;
                moveWithCollision(moveX, moveY, levelManager);
            }
        }

        if (rect.overlaps(player.getRect()) && attackCooldown <= 0f) {
            attack();
            player.affected();
            attackCooldown = ATTACK_COOLDOWN_SEC;
        }
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
}
