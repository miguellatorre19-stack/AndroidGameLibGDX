package svalero.com.managers;


import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

//contiene el código que permite el renderizado (o pintado) de todos los elementos del juego en la pantalla.
// los cálculos de dónde pintar a cada elemento del juego los realiza el SpriteManager
// y esta clase sólo tiene que pintar cada uno de ellos utilizando el método que cada uno tiene implementado

public class RenderManager {
    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;

    public void drawFrame(){
        batch.begin();
    }


}
