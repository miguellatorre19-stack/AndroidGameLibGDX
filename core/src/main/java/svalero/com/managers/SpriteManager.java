package svalero.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import svalero.com.KeyFinder;
import svalero.com.characters.Enemy;
import svalero.com.characters.Player;
import svalero.com.characters.Projectile;
import svalero.com.items.Coin;
import svalero.com.items.Key;

public class SpriteManager {
    private static final float PROJECTILE_SPEED_PX_PER_SEC = 120f;
    private static final float MIN_PROJECTILE_INTERVAL_SEC = 0.05f;

    private static class ProjectileSource {
        Vector2 origin;
        Vector2 direction;
        float intervalSec;
        float timerSec;
        boolean fromPlayer;
    }

    private final KeyFinder game;
    private final Texture projectileTexture;

    private final Array<Key> worldKeys;
    private final Array<Coin> worldCoins;
    private final Array<Enemy> enemies;
    private final Array<Projectile> projectiles;
    private final Array<ProjectileSource> projectileSources;

    protected Player player;
    private LevelManager levelManager;

    public SpriteManager(KeyFinder game) {
        this.game = game;
        worldKeys = new Array<>();
        worldCoins = new Array<>();
        enemies = new Array<>();
        projectiles = new Array<>();
        projectileSources = new Array<>();

        projectileTexture = new Texture("2D_Pixel_Dungeon_Asset_Pack/items and trap_animation/arrow/Just_arrow.png");
        projectileTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    // ----- Setup / dependency wiring -----

    public KeyFinder getGame() {
        return game;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    // ----- World registration -----

    public void addWorldKey(Key key) {
        worldKeys.add(key);
    }

    public void addWorldCoin(Coin coin) {
        worldCoins.add(coin);
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void addProjectileSource(Vector2 origin, Vector2 direction, float intervalSec, boolean fromPlayer) {
        ProjectileSource source = new ProjectileSource();
        source.origin = new Vector2(origin);
        source.direction = new Vector2(direction).nor();
        source.intervalSec = Math.max(MIN_PROJECTILE_INTERVAL_SEC, intervalSec);
        source.timerSec = 0f;
        source.fromPlayer = fromPlayer;
        projectileSources.add(source);
    }

    // ----- Read-only collections for rendering -----

    public Array<Key> getWorldKeys() {
        return worldKeys;
    }

    public Array<Coin> getWorldCoins() {
        return worldCoins;
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }

    // ----- Per-frame update -----

    // Compatibilidad con llamadas actuales desde GameScreen.
    public void handleInput(float dt) {
        update(dt);
    }

    public void update(float dt) {
        updatePlayerMovement(dt);
        updateEnemies(dt);
        updateProjectileSources(dt);
        updateProjectiles(dt);
        updateWorldKeys();
        updateWorldCoins();
    }

    private void updatePlayerMovement(float dt) {
        if (player == null) return;

        float dx = 0f;
        float dy = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dx += 1f;
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 1f;
        else if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) dy += 1f;
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 1f;

        float moveSpeedPxPerSec = player.getMoveSpeedPxPerSec();
        movePlayerAxis(dx * moveSpeedPxPerSec * dt, true);
        movePlayerAxis(dy * moveSpeedPxPerSec * dt, false);
    }

    private void movePlayerAxis(float movement, boolean axisX) {
        if (movement == 0f) return;

        float previous = axisX ? player.getPosition().x : player.getPosition().y;
        if (axisX) {
            player.getPosition().x += movement;
        } else {
            player.getPosition().y += movement;
        }
        player.getRect().setPosition(player.getPosition().x, player.getPosition().y);

        boolean blocked = levelManager.isBlocked(player.getRect(), player.hasKey());
        if (levelManager.consumeDoorUnlockEvent()) {
            player.removeKey();
        }

        if (blocked) {
            if (axisX) {
                player.getPosition().x = previous;
            } else {
                player.getPosition().y = previous;
            }
            player.getRect().setPosition(player.getPosition().x, player.getPosition().y);
        }
    }

    private void updateEnemies(float dt) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.updateBehavior(player, dt, levelManager);
            if (enemy.isDead()) {
                enemies.removeIndex(i);
            }
        }
    }

    private void updateProjectileSources(float dt) {
        for (ProjectileSource source : projectileSources) {
            source.timerSec += dt;
            while (source.timerSec >= source.intervalSec) {
                source.timerSec -= source.intervalSec;
                spawnProjectile(source.origin, source.direction, source.fromPlayer);
            }
        }
    }

    public void spawnProjectile(Vector2 startPosition, Vector2 direction, boolean fromPlayer) {
        Vector2 velocity = new Vector2(direction).nor().scl(PROJECTILE_SPEED_PX_PER_SEC);
        projectiles.add(new Projectile(projectileTexture, startPosition, velocity, fromPlayer));
    }

    private void updateProjectiles(float dt) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(dt);

            Rectangle bounds = projectile.getBounds();
            if (levelManager.isBlocked(bounds, false)) {
                projectiles.removeIndex(i);
                continue;
            }

            if (projectile.isFromPlayer()) {
                if (hitsAnyEnemy(bounds)) {
                    damageEnemiesAt(bounds);
                    projectiles.removeIndex(i);
                }
                continue;
            }

            if (!player.isDead() && player.getRect().overlaps(bounds)) {
                player.affected();
                projectiles.removeIndex(i);
                continue;
            }
            if (hitsAnyEnemy(bounds)) {
                damageEnemiesAt(bounds);
                projectiles.removeIndex(i);
            }
        }
    }

    public void damageEnemiesAt(Rectangle projectileBounds) {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getRect().overlaps(projectileBounds)) {
                enemy.onProjectileHit();
                return;
            }
        }
    }

    private boolean hitsAnyEnemy(Rectangle bounds) {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getRect().overlaps(bounds)) {
                return true;
            }
        }
        return false;
    }

    private void updateWorldKeys() {
        for (int i = worldKeys.size - 1; i >= 0; i--) {
            Key key = worldKeys.get(i);
            player.getKey(key);
            if (key.isCollected()) {
                key.dispose();
                worldKeys.removeIndex(i);
            }
        }
    }

    private void updateWorldCoins() {
        for (int i = worldCoins.size - 1; i >= 0; i--) {
            Coin coin = worldCoins.get(i);
            player.getCoin(coin);
            if (coin.isCollected()) {
                coin.dispose();
                worldCoins.removeIndex(i);
            }
        }
    }
}
