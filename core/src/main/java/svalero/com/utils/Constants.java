package svalero.com.utils;

//contendrá todas las constantes que vayamos a utilizar en todo el proyecto del videojuego
public class Constants {

    public static final String APP_NAME = "KeyFinder";
    public static final float PlayerSpeed_PxPerSec = 34f;
    public static final float PLAYER_BOOST_DURATION_SEC = 20f;

    public static final int SCREEN_WIDTH =1280;
    public static final int SCREEN_HEIGHT = 720;

    public static final int TILE_SIZE_PX = 16;

    public static final int CAMERA_TILES_WIDE = 10;
    public static final int CAMERA_TILES_HIGH = 8;
    public static final int CAMERA_WIDTH = CAMERA_TILES_WIDE * TILE_SIZE_PX;
    public static final int CAMERA_HEIGHT = CAMERA_TILES_HIGH * TILE_SIZE_PX;

    public static final float PLAYER_RENDER_SCALE = 1f;

    public static final float SCALE_MODIFIER = 0.5f;

}
