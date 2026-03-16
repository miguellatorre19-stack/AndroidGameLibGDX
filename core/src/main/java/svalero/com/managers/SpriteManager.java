package svalero.com.managers;

//contiene todos los métodos que se encargan de gestionar la lógica del videojuego.
// Es el encargado de hacer que se mueva todo lo que debe moverse en el juego

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import svalero.com.KeyFinder;
import svalero.com.characters.Player;

import static svalero.com.utils.Constants.*;
//los cálculos de dónde pintar a cada elemento del juego los realiza el SpriteManager

public class SpriteManager  {

    private final KeyFinder game;
    protected  Player player;
    private Vector2 vector;
    private LevelManager levelManager;
    private CameraManager cameraManager;

    public SpriteManager(KeyFinder game){
        // Game reference kept for future gameplay logic.
        this.game = game;
    }

    public KeyFinder getGame() {
        return game;
    }

    public void setPlayer(Player player) {
        // Inject the player instance used by input and rendering logic.
        this.player = player;
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
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)  || Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 1f;

        player.getPosition().x += dx * PlayerSpeed_PxPerSec * dt;
        player.getPosition().y += dy * PlayerSpeed_PxPerSec * dt;

        player.getRect().setPosition(player.getPosition().x, player.getPosition().y);
    }




}
