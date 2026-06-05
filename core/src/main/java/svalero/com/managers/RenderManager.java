package svalero.com.managers;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Array;
import svalero.com.characters.enemy.Enemy;
import svalero.com.characters.Neutral;
import svalero.com.characters.Projectile;
import svalero.com.characters.enemy.StrongerEnemy;
import svalero.com.items.Coin;
import svalero.com.items.Key;

//contiene el código que permite el renderizado (o pintado) de todos los elementos del juego en la pantalla.
// Los cálculos de dónde pintar a cada elemento del juego los realiza el SpriteManager
// y esta clase sólo tiene que pintar cada uno de ellos utilizando el método que cada uno tiene implementado,
//sólo los carga desde el fichero donde esté la información del mapa correspondiente.
public class RenderManager implements Disposable {
    public SpriteBatch batch;
    private SpriteManager spriteManager;
    private ResourceManager resourceManager;
    private BitmapFont font;
    private final Texture doorTexture;
    private final Texture lateralDoorTexture;

    public RenderManager() {
        // Required so drawFrame() can begin/end rendering safely.
        batch = new SpriteBatch();
        doorTexture = new Texture("2D_Pixel_Dungeon_Asset_Pack/door.png");
        lateralDoorTexture = new Texture("2D_Pixel_Dungeon_Asset_Pack/door_lateral.png");
        doorTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        lateralDoorTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public void drawFrame(SpriteManager spriteManager, LevelManager levelManager, OrthographicCamera camera){
        if (camera != null) {
            batch.setProjectionMatrix(camera.combined);
        }
        batch.begin();
        drawLockedDoors(levelManager);

        for (Key key : spriteManager.getWorldKeys()) {
            key.render(batch);
        }

        for (Coin coin : spriteManager.getWorldCoins()) {
            coin.render(batch);
        }

        spriteManager.player.render(batch);
        for (Enemy enemy : spriteManager.getEnemies()) {
            if (!enemy.isDead()) {
                enemy.render(batch);
            }
        }
        for (StrongerEnemy strongerEnemy : spriteManager.getStrongerEnemies()) {
            if (!strongerEnemy.isDead()) {
                strongerEnemy.render(batch);
            }
        }
        for (Neutral neutral : spriteManager.getNeutrals()) {
            if (!neutral.isDead()) {
                neutral.render(batch);
            }
        }
        for (Projectile projectile : spriteManager.getProjectiles()) {
            projectile.render(batch);
        }

        batch.end();
    }

    private void drawLockedDoors(LevelManager levelManager) {
        if (levelManager == null) return;
        Array<Rectangle> lockedDoorBounds = levelManager.getLockedDoorBounds();
        for (Rectangle bounds : lockedDoorBounds) {
            batch.draw(doorTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        }

        Array<Rectangle> lockedLateralDoorBounds = levelManager.getLockedLateralDoorBounds();
        for (Rectangle bounds : lockedLateralDoorBounds) {
            batch.draw(lateralDoorTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    @Override
    public void dispose() {
        if (doorTexture != null) {
            doorTexture.dispose();
        }
        if (lateralDoorTexture != null) {
            lateralDoorTexture.dispose();
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }
}
