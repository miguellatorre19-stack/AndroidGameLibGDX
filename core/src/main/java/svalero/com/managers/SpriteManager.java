package svalero.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import svalero.com.KeyFinder;
import svalero.com.characters.Enemy;
import svalero.com.characters.Neutral;
import svalero.com.characters.Player;
import svalero.com.characters.Projectile;
import svalero.com.characters.StrongerEnemy;
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
    private final Array<StrongerEnemy> strongerEnemies;
    private final Array<Neutral> neutrals;
    private final Array<Projectile> projectiles;
    private final Array<ProjectileSource> projectileSources;

    protected Player player;
    private LevelManager levelManager;
    private boolean doorUnlockedThisFrame;

    public SpriteManager(KeyFinder game) {
        this.game = game;
        worldKeys = new Array<>();
        worldCoins = new Array<>();
        enemies = new Array<>();
        strongerEnemies = new Array<>();
        neutrals = new Array<>();
        projectiles = new Array<>();
        projectileSources = new Array<>();
        doorUnlockedThisFrame = false;

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

    public void addStrongerEnemy(StrongerEnemy strongerEnemy) {
        strongerEnemies.add(strongerEnemy);
    }

    public void addNeutral (Neutral neutral){
        neutrals.add(neutral);
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

    public void clearLevelEntities() {
        for (Key key : worldKeys) {
            key.dispose();
        }
        worldKeys.clear();

        for (Coin coin : worldCoins) {
            coin.dispose();
        }
        worldCoins.clear();

        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        enemies.clear();

        for (StrongerEnemy strongerEnemy : strongerEnemies) {
            strongerEnemy.dispose();
        }
        strongerEnemies.clear();

        neutrals.clear();
        projectiles.clear();
        projectileSources.clear();
        doorUnlockedThisFrame = false;
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

    public Array<StrongerEnemy> getStrongerEnemies() {
        return strongerEnemies;
    }

    public Array<Neutral> getNeutrals() {
        return neutrals;
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
        doorUnlockedThisFrame = false;
        updatePlayerMovement(dt);
        updateEnemies(dt);
        updateStrongerEnemies(dt);
        updateNeutrals();
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

        if (axisX) {
            player.updateFacingFromMovement(movement);
        }

        float previous = axisX ? player.getPosition().x : player.getPosition().y;
        if (axisX) {
            player.getPosition().x += movement;
        } else {
            player.getPosition().y += movement;
        }
        player.syncHitboxFromPosition();

        boolean blocked = levelManager.isBlocked(player.getHitbox(), player.getRect(), player.hasKey());
        if (levelManager.consumeDoorUnlockEvent()) {
            player.removeKey();
            doorUnlockedThisFrame = true;
        }

        if (blocked) {
            if (axisX) {
                player.getPosition().x = previous;
            } else {
                player.getPosition().y = previous;
            }
            player.syncHitboxFromPosition();
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

    private void updateNeutrals() {
        for (int i = neutrals.size - 1; i >= 0; i--) {
            Neutral neutral = neutrals.get(i);
            if (neutral.isDead()) {
                neutrals.removeIndex(i);
            }
        }
    }

    private void updateStrongerEnemies(float dt) {
        for (int i = strongerEnemies.size - 1; i >= 0; i--) {
            StrongerEnemy strongerEnemy = strongerEnemies.get(i);
            strongerEnemy.updateBehavior(player, dt, levelManager);
            if (strongerEnemy.isDead()) {
                strongerEnemies.removeIndex(i);
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
            if (levelManager.isBlocked(projectile.getHitbox(), bounds, false)) {
                projectiles.removeIndex(i);
                continue;
            }

            if (projectile.isFromPlayer()) {
                if (hitsAnyEnemy(projectile)) {
                    damageEnemiesAt(projectile);
                    projectiles.removeIndex(i);
                }
                continue;
            }

            if (!player.isDead() && Intersector.overlapConvexPolygons(player.getHitbox(), projectile.getHitbox())) {
                player.affected();
                projectiles.removeIndex(i);
                continue;
            }
            if (hitsAnyEnemy(projectile)) {
                damageEnemiesAt(projectile);
                projectiles.removeIndex(i);
            }
        }
    }

    public void damageEnemiesAt(Projectile projectile) {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && Intersector.overlapConvexPolygons(enemy.getHitbox(), projectile.getHitbox())) {
                enemy.onProjectileHit();
                return;
            }
        }
        for (StrongerEnemy strongerEnemy : strongerEnemies) {
            if (!strongerEnemy.isDead() && Intersector.overlapConvexPolygons(strongerEnemy.getHitbox(), projectile.getHitbox())) {
                strongerEnemy.onProjectileHit();
                return;
            }
        }
    }

    private boolean hitsAnyEnemy(Projectile projectile) {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && Intersector.overlapConvexPolygons(enemy.getHitbox(), projectile.getHitbox())) {
                return true;
            }
        }
        for (StrongerEnemy strongerEnemy : strongerEnemies) {
            if (!strongerEnemy.isDead() && Intersector.overlapConvexPolygons(strongerEnemy.getHitbox(), projectile.getHitbox())) {
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
