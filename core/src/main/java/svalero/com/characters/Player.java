package svalero.com.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import lombok.Data;
import svalero.com.managers.SpriteManager;
import svalero.com.screens.MainMenuScreen;

@Data
public class Player extends Character implements Disposable {

    Texture playerTexture = new Texture(Gdx.files.internal("characters/Character_animation/priests_idle/priest1/v1/priest1_v1_1.png"));

    public Player(Texture playerTexture, Vector2 position, SpriteManager spriteManager) {
        super(playerTexture, new Vector2(100,100), spriteManager);
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }

    @Override
    public void attack() {

    }

    @Override
    public void die() {
        game.setScreen(new MainMenuScreen(game));
        dispose();
    }

    @Override
    public void update() {

    }

    public void resurrect(){

    }

    @Override
    public void checkColisions(SpriteManager spriteManager) {
        if (Intersector.overlaps(rect, rect)){
        }
    }

    @Override
    public void affected() {
        lives -= 1;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    public float setPosition(float x) {
        return x;
    }
}
