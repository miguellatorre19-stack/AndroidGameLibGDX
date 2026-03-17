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

            if (levelManager != null && levelManager.isBlocked(player.getRect())) {
                player.getPosition().x = oldX;
                player.getRect().setPosition(player.getPosition().x, player.getPosition().y);
            }
        }

        // Move Y second
        if (moveY != 0f) {
            float oldY = player.getPosition().y;
            player.getPosition().y += moveY;
            player.getRect().setPosition(player.getPosition().x, player.getPosition().y);

            if (levelManager != null && levelManager.isBlocked(player.getRect())) {
                player.getPosition().y = oldY;
                player.getRect().setPosition(player.getPosition().x, player.getPosition().y);
            }
        }
    }




}
