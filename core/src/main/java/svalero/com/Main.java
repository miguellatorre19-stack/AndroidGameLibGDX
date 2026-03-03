package svalero.com;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch SpriteBatch;
    private FitViewport viewport;
    private Texture pjTexture;
    private Sprite pjSprite;
    private Texture backgroundTexture;
    private Sound sound;
    Music music;
    Vector2 touchPos;

    @Override
    public void create() {
        SpriteBatch = new SpriteBatch();
        //will ensure that no matter what size our window is, the full game view will always be visible.
        // The parameters determine how large our visible game world will be in game units.
        viewport = new FitViewport(5 ,4 );

        pjTexture = new Texture("2D_Pixel_Dungeon_Asset_Pack/Character_animation/monsters_idle/vampire/v1/vampire_v1_1.png");
        backgroundTexture = new Texture("2D_Pixel_Dungeon_Asset_Pack/Dungeon_Tileset_at.png");
        SpriteBatch = new SpriteBatch();
        pjSprite = new Sprite(pjTexture);
        pjSprite.setSize(0.5f, 0.5f);
        touchPos = new Vector2();
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();// retrieve the current delta
        //Delta time is the measured time between frames.
        // If we multiply our movement by delta time, the movement will be consistent no matter what hardware we run this game on.
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                pjSprite.translateX(speed * delta); // Move the bucket right
                // todo: Do something when the user presses the right arrow
            }
            else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
                pjSprite.translateX(-speed * delta);// Move the pj left
            }

            if (Gdx.input.isKeyPressed(Input.Keys.UP)){
                pjSprite.translateY(speed *delta); //Move the pj up
            }
            else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)){
                pjSprite.translateY(-speed * delta);//Move the pj down
            }

            if (Gdx.input.isTouched()) { // If the user has clicked or tapped the screen
                // todo:React to the player touching the screen
                touchPos.set(Gdx.input.getX(), Gdx.input.getY()); // Get where the touch happened on screen
                viewport.unproject(touchPos); // Convert the units to the world units of the viewport
                pjSprite.setCenterX(touchPos.x); // Change the horizontally centered position of the bucket
            }
    }

    private void logic() {
        // Store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // Store the bucket size for brevity
        float bucketWidth = pjSprite.getWidth();
        float bucketHeight = pjSprite.getHeight();

        // Clamp x to values between 0 and worldWidth
        pjSprite.setX(MathUtils.clamp(pjSprite.getX(), 0, worldWidth));
    }

    private void draw() {
        viewport.apply();
        ScreenUtils.clear(Color.BLACK);
        // clears the screen. It’s a good practice to clear the screen every frame.
        // Otherwise, you’ll get weird graphical errors. You can use any color you want, but we’ll just settle on Black this time.
        SpriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        //shows how the Viewport is applied to the SpriteBatch. This is necessary for the images to be shown in the correct place.
        SpriteBatch.begin();

        float worldWidth = viewport.getWorldWidth() * 1.5f;
        float worldHeight = viewport.getWorldHeight() * 2;
        // store the worldWidth and worldHeight as local variables for brevity
        SpriteBatch.draw(backgroundTexture, 0 ,0, worldWidth, worldHeight );
        pjSprite.draw(SpriteBatch);

        SpriteBatch.end();
    }

    @Override
    public void dispose() {
        SpriteBatch.dispose();

    }
}
