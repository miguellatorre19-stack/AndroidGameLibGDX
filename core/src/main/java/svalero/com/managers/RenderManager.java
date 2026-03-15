package svalero.com.managers;


import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//contiene el código que permite el renderizado (o pintado) de todos los elementos del juego en la pantalla.
// Los cálculos de dónde pintar a cada elemento del juego los realiza el SpriteManager
// y esta clase sólo tiene que pintar cada uno de ellos utilizando el método que cada uno tiene implementado,
//sólo los carga desde el fichero donde esté la información del mapa correspondiente.
public class RenderManager {
    private SpriteBatch batch;
    private SpriteManager spriteManager;
    private BitmapFont font;

    public RenderManager() {
        // Required so drawFrame() can begin/end rendering safely.
        batch = new SpriteBatch();
    }

    public void drawFrame(SpriteManager spriteManager){

        batch.begin();
        spriteManager.player.render(batch);
        batch.end();

    }

}
