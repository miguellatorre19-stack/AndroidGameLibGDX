package svalero.com.managers;


import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import svalero.com.characters.Enemy;
import svalero.com.characters.Neutral;
import svalero.com.characters.Projectile;
import svalero.com.characters.StrongerEnemy;
import svalero.com.items.Coin;
import svalero.com.items.Key;

//contiene el código que permite el renderizado (o pintado) de todos los elementos del juego en la pantalla.
// Los cálculos de dónde pintar a cada elemento del juego los realiza el SpriteManager
// y esta clase sólo tiene que pintar cada uno de ellos utilizando el método que cada uno tiene implementado,
//sólo los carga desde el fichero donde esté la información del mapa correspondiente.
public class RenderManager {
    public SpriteBatch batch;
    private SpriteManager spriteManager;
    private ResourceManager resourceManager;
    private BitmapFont font;

    public RenderManager() {
        // Required so drawFrame() can begin/end rendering safely.
        batch = new SpriteBatch();
    }

    public void drawFrame(SpriteManager spriteManager, OrthographicCamera camera){
        if (camera != null) {
            batch.setProjectionMatrix(camera.combined);
        }
        batch.begin();
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

}
