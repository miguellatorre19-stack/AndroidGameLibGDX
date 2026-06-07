package svalero.com.managers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector2;
import svalero.com.characters.Neutral;
import svalero.com.characters.Player;
import svalero.com.characters.enemy.Enemy;
import svalero.com.characters.enemy.EnemyConfig;
import svalero.com.characters.enemy.StrongerEnemy;
import svalero.com.items.Coin;
import svalero.com.items.Key;
import svalero.com.items.PowerUp;
import svalero.com.items.PowerUpType;

import static svalero.com.utils.Constants.TILE_SIZE_PX;

public final class LevelEntityFactory {
    private static final String ATLAS_ID = ResourceManager.GENERAL_ATLAS_ID;
    private static final String MESSAGE_INTRO_01 = "Woah! Creo que te has perdido, amigo. De algun modo has acabado en las antiguas catacumbas. "
        + "No lo vas a tener facil para huir. Para poder moverte hasta la salida, tendras que ir abriendo las puertas del laberinto. "
        + "Para ello necesitaras llaves, como esa de ahi. Son de un solo uso, asi que asegurate de como quieres usarlas.";
    private static final String MESSAGE_INTRO_02 = "Las calaveras flotantes somos efimeras. Una vez que interactues con nosotros, desapareceremos.";
    private static final String MESSAGE_INTRO_03 = "Este nivel es mas peligroso. Los vampiros pueden paralizarte si te ven.";

    private final SpriteManager spriteManager;
    private final Player player;
    private final LevelEntityAssets assets;

    public LevelEntityFactory(SpriteManager spriteManager, Player player) {
        this.spriteManager = spriteManager;
        this.player = player;
        this.assets = LevelEntityAssets.load();
    }

    public void spawnAll(Iterable<MapObject> objects) {
        for (MapObject object : objects) {
            spawn(object);
        }
    }

    private void spawn(MapObject object) {
        Vector2 position = position(object);
        switch (type(object)) {
            case "player_spawn" -> spawnPlayer(position);
            case "enemy" -> spawnEnemy(object, position);
            case "item" -> spawnItem(object, position);
            case "npc" -> spawnNpc(object, position);
            case "projectile_source" -> spawnProjectileSource(object, position);
            default -> {
                // Collision, trigger and decorative map objects are ignored by the entity factory.
            }
        }
    }

    private void spawnPlayer(Vector2 position) {
        player.getPosition().set(position);
        player.syncHitboxFromPosition();
    }

    private void spawnEnemy(MapObject object, Vector2 position) {
        String enemyType = readNormalizedString(object, "enemyType");
        int lives = readInt(object, "lives", isStrongEnemyType(enemyType) ? 5 : 3);
        EnemyConfig config = readEnemyConfig(object, isStrongEnemyType(enemyType), lives);

        if (isStrongEnemyType(enemyType)) {
            EnemyAnimationSet vampire = assets.vampire();
            spriteManager.addStrongerEnemy(new StrongerEnemy(
                position,
                spriteManager,
                config,
                vampire.idle(),
                vampire.movement(),
                vampire.attack(),
                vampire.damaged(),
                vampire.death()
            ));
            return;
        }

        EnemyAnimationSet skeleton = assets.skeleton();
        spriteManager.addEnemy(new Enemy(
            position,
            spriteManager,
            config,
            skeleton.idle(),
            skeleton.movement(),
            skeleton.attack(),
            skeleton.damaged(),
            skeleton.death()
        ));
    }

    private boolean isStrongEnemyType(String enemyType) {
        return "vampire".equals(enemyType)
            || "stronger".equals(enemyType)
            || "stronger_enemy".equals(enemyType);
    }

    private EnemyConfig readEnemyConfig(MapObject object, boolean strongEnemy, int lives) {
        EnemyConfig baseConfig = strongEnemy ? EnemyConfig.stronger(lives) : EnemyConfig.skeleton(lives);
        return new EnemyConfig(
            lives,
            readFloat(object, "moveSpeed", baseConfig.chaseSpeedPxPerSec()),
            readFloat(object, "attackCooldown", baseConfig.attackCooldownSec()),
            readDistancePx(object, "aggroDistance", "aggroTiles", baseConfig.aggroDistancePx()),
            readDistancePx(object, "patrolRadius", "patrolTiles", baseConfig.patrolRadiusPx())
        );
    }

    private void spawnItem(MapObject object, Vector2 position) {
        String itemType = readNormalizedString(object, "itemType");
        if ("key".equals(itemType)) {
            spriteManager.addWorldKey(new Key(position));
            return;
        }
        if ("coin".equals(itemType)) {
            spriteManager.addWorldCoin(new Coin(position));
            return;
        }

        PowerUpType powerUpType = "powerup".equals(itemType)
            ? PowerUpType.fromId(readNormalizedString(object, "powerUpType"))
            : PowerUpType.fromId(itemType);
        if (powerUpType != null) {
            spriteManager.addWorldPowerUp(new PowerUp(powerUpType, position));
        }
    }

    private void spawnNpc(MapObject object, Vector2 position) {
        String npcType = readNormalizedString(object, "npcType");
        if (!npcType.isBlank() && !"skull".equals(npcType)) return;

        String messageId = readNormalizedString(object, "messageId");
        spriteManager.addNeutral(new Neutral(
            position,
            spriteManager,
            assets.skullIdle(),
            readMessage(messageId)
        ));
    }

    private void spawnProjectileSource(MapObject object, Vector2 position) {
        Vector2 direction = new Vector2(
            readFloat(object, "dirX", 0f),
            readFloat(object, "dirY", -1f)
        );
        if (direction.isZero()) {
            direction.set(0f, -1f);
        }

        spriteManager.addProjectileSource(
            position,
            direction,
            readFloat(object, "interval", 1.5f),
            readFloat(object, "initialDelay", 0f),
            readFloat(object, "speed", 120f),
            readBoolean(object, "fromPlayer", false)
        );
    }

    private String readMessage(String messageId) {
        return switch (messageId) {
            case "intro_01" -> MESSAGE_INTRO_01;
            case "intro_02" -> MESSAGE_INTRO_02;
            case "intro_03" -> MESSAGE_INTRO_03;
            default -> "";
        };
    }

    private static Vector2 position(MapObject object) {
        if (object instanceof RectangleMapObject rectangleMapObject) {
            return new Vector2(rectangleMapObject.getRectangle().x, rectangleMapObject.getRectangle().y);
        }
        if (object instanceof PointMapObject pointMapObject) {
            return new Vector2(pointMapObject.getPoint().x, pointMapObject.getPoint().y);
        }
        return new Vector2(readFloat(object, "x", 0f), readFloat(object, "y", 0f));
    }

    private static String type(MapObject object) {
        return readNormalizedString(object, "type", "class");
    }

    private static String readNormalizedString(MapObject object, String... keys) {
        return normalize(readString(object, keys));
    }

    private static String readString(MapObject object, String... keys) {
        MapProperties properties = object.getProperties();
        for (String key : keys) {
            if (!properties.containsKey(key)) continue;
            Object value = properties.get(key);
            if (value != null) return String.valueOf(value);
        }
        return "";
    }

    private static int readInt(MapObject object, String key, int defaultValue) {
        Object value = object.getProperties().get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) return defaultValue;

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static float readFloat(MapObject object, String key, float defaultValue) {
        Object value = object.getProperties().get(key);
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value == null) return defaultValue;

        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static float readDistancePx(MapObject object, String pixelKey, String tileKey, float defaultValue) {
        if (object.getProperties().containsKey(pixelKey)) {
            return readFloat(object, pixelKey, defaultValue);
        }
        if (object.getProperties().containsKey(tileKey)) {
            return readFloat(object, tileKey, defaultValue / TILE_SIZE_PX) * TILE_SIZE_PX;
        }
        return defaultValue;
    }

    private static boolean readBoolean(MapObject object, String key, boolean defaultValue) {
        Object value = object.getProperties().get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) return defaultValue;

        String text = String.valueOf(value);
        return "true".equalsIgnoreCase(text)
            || "1".equals(text)
            || "yes".equalsIgnoreCase(text);
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase().replace(' ', '_');
    }

    private record EnemyAnimationSet(
        Animation<TextureRegion> idle,
        Animation<TextureRegion> movement,
        Animation<TextureRegion> attack,
        Animation<TextureRegion> damaged,
        Animation<TextureRegion> death
    ) {
    }

    private record LevelEntityAssets(
        EnemyAnimationSet skeleton,
        EnemyAnimationSet vampire,
        Animation<TextureRegion> skullIdle
    ) {
        static LevelEntityAssets load() {
            return new LevelEntityAssets(
                new EnemyAnimationSet(
                    animation("squeleton_idle", 0.18f, Animation.PlayMode.LOOP),
                    animation("squeleton_movement", 0.10f, Animation.PlayMode.LOOP),
                    animation("skeleton_attack", 0.08f, Animation.PlayMode.NORMAL),
                    animation("squeleton_damaged", 0.10f, Animation.PlayMode.NORMAL),
                    animation("skeleton_death", 0.10f, Animation.PlayMode.NORMAL)
                ),
                new EnemyAnimationSet(
                    animation("vampire_idle", 0.18f, Animation.PlayMode.LOOP),
                    animation("vampire_movement", 0.10f, Animation.PlayMode.LOOP),
                    animation("vampire_attack", 0.08f, Animation.PlayMode.NORMAL),
                    animation("vampire_damaged", 0.10f, Animation.PlayMode.NORMAL),
                    animation("vampire_death", 0.10f, Animation.PlayMode.NORMAL)
                ),
                animation("skull_v2", 0.10f, Animation.PlayMode.LOOP)
            );
        }

        private static Animation<TextureRegion> animation(
            String regionName,
            float duration,
            Animation.PlayMode playMode
        ) {
            return ResourceManager.buildIndexedAnimation(ATLAS_ID, regionName, duration, playMode);
        }
    }
}
