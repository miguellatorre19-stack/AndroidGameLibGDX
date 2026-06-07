package svalero.com.managers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import svalero.com.characters.Player;

import static svalero.com.utils.Constants.*;

public class CameraManager {
    public OrthographicCamera camera;

    public CameraManager() {
        camera = new OrthographicCamera();
    }

    public void innit() {
        camera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
    }

    public void handleCamera(Player player, float worldWidth, float worldHeight) {
        if (player == null) return;

        float halfViewportWidth = camera.viewportWidth * 0.5f;
        float halfViewportHeight = camera.viewportHeight * 0.5f;

        float minX = halfViewportWidth;
        float minY = halfViewportHeight;
        float maxX = Math.max(minX, worldWidth - halfViewportWidth);
        float maxY = Math.max(minY, worldHeight - halfViewportHeight);

        float cameraX = MathUtils.clamp(player.getPosition().x, minX, maxX);
        float cameraY = MathUtils.clamp(player.getPosition().y, minY, maxY);

        camera.position.set(cameraX, cameraY, 0f);
        camera.update();
    }
}
