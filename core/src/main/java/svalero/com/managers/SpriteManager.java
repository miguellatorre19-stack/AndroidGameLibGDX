package svalero.com.managers;

//contiene todos los métodos que se encargan de gestionar la lógica del videojuego.
// Es el encargado de hacer que se mueva todo lo que debe moverse en el juego

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

import static svalero.com.utils.Constants.PlayerSpeed_PxPerSec;
//los cálculos de dónde pintar a cada elemento del juego los realiza el SpriteManager

public class SpriteManager  {

    private static final float PROJECTILE_SPEED_PX_PER_SEC = 120f;

    private static class ProjectileSource {
        Vector2 origin;
        Vector2 direction;
        float intervalSec;
        float timerSec;
        boolean fromPlayer;
    }

    private final KeyFinder game;
    protected  Player player;
    private final Array<Enemy> enemies;
    private final Array<Projectile> projectiles;
    private final Array<ProjectileSource> projectileSources;
    private final Texture projectileTexture;
    private LevelManager levelManager;
    private CameraManager cameraManager;

    public SpriteManager(KeyFinder game){
        // Game reference kept for future gameplay logic.
        this.game = game;
        enemies = new Array<>();
        projectiles = new Array<>();
        projectileSources = new Array<>();
        projectileTexture = new Texture("interactables/items and trap_animation/arrow/Just_arrow.png");
        projectileTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public KeyFinder getGame() {
        return game;
    }

    public void setPlayer(Player player) {
        // Inject the player instance used by input and rendering logic.
        this.player = player;
    }

    public void addEnemy(Enemy enemy) {
        if (enemy == null) return;
        enemies.add(enemy);
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }

    public void addProjectileSource(Vector2 origin, Vector2 direction, float intervalSec, boolean fromPlayer) {
        if (origin == null || direction == null || direction.isZero()) return;

        ProjectileSource source = new ProjectileSource();
        source.origin = new Vector2(origin);
        source.direction = new Vector2(direction).nor();
        source.intervalSec = Math.max(0.05f, intervalSec);
        source.timerSec = 0f;
        source.fromPlayer = fromPlayer;
        projectileSources.add(source);
    }

    public void setLevelManager(LevelManager levelManager){
        this.levelManager = levelManager;
    }

    public void setCameraManager(CameraManager cameraManager){
        this.cameraManager = cameraManager;
    }

    public void handleInput(float dt) {
        if (player == null) return;

        float dx = 0f;
        float dy = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dx += 1f;
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 1f;
        else if (Gdx.input.isKeyPressed(Input.Keys.UP)    || Gdx.input.isKeyPressed(Input.Keys.W)) dy += 1f;
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)  || Gdx.input.isKeyPressed(Input.Keys.S)) dy -=1f;

        float moveX = dx * PlayerSpeed_PxPerSec * dt;
        float moveY = dy * PlayerSpeed_PxPerSec * dt;

        // Move X first
        if (moveX != 0f) {
            float oldX = player.getPosition().x;
            player.getPosition().x += moveX;
            player.getRect().setPosition(player.getPosition().x, player.getPosition().y);

            if (levelManager != null && levelManager.isBlocked(player.getRect(), player.hasKey())) {
                player.getPosition().x = oldX;
                player.getRect().setPosition(player.getPosition().x, player.getPosition().y);
            }
        }

        // Move Y second
        if (moveY != 0f) {
            float oldY = player.getPosition().y;
            player.getPosition().y += moveY;
            player.getRect().setPosition(player.getPosition().x, player.getPosition().y);

            if (levelManager != null && levelManager.isBlocked(player.getRect(), player.hasKey())) {
                player.getPosition().y = oldY;
                player.getRect().setPosition(player.getPosition().x, player.getPosition().y);
            }
        }

        updateEnemies(dt);
        updateProjectileSources(dt);
        updateProjectiles(dt);
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

    public void damageEnemiesAt(Rectangle projectileBounds) {
        if (projectileBounds == null) return;

        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getRect().overlaps(projectileBounds)) {
                enemy.onProjectileHit();
                return;
            }
        }
    }

    public void spawnProjectile(Vector2 startPosition, Vector2 direction, boolean fromPlayer) {
        if (startPosition == null || direction == null) return;
        if (direction.isZero()) return;

        Vector2 velocity = new Vector2(direction).nor().scl(PROJECTILE_SPEED_PX_PER_SEC);
        projectiles.add(new Projectile(projectileTexture, startPosition, velocity, fromPlayer));
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

    private void updateProjectiles(float dt) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(dt);

            Rectangle bounds = projectile.getBounds();
            if (levelManager != null && levelManager.isBlocked(bounds, false)) {
                projectiles.removeIndex(i);
                continue;
            }

            if (projectile.isFromPlayer()) {
                if (hitsAnyEnemy(bounds)) {
                    damageEnemiesAt(bounds);
                    projectiles.removeIndex(i);
                    continue;
                }
            } else {
                if (!player.isDead() && player.getRect().overlaps(bounds)) {
                    player.affected();
                    projectiles.removeIndex(i);
                    continue;
                }
                if (hitsAnyEnemy(bounds)) {
                    damageEnemiesAt(bounds);
                    projectiles.removeIndex(i);
                    continue;
                }
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

}
