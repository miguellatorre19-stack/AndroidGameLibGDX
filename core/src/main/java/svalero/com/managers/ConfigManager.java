package svalero.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import svalero.com.utils.Constants;

//La clase ConfigurationManager es la encargada de almacenar y acceder a las preferencias del usuario.
public class ConfigManager {
    private static Preferences prefs = Gdx.app.getPreferences(Constants.APP_NAME);

    public static boolean isSoundEnabled(){
        return prefs.getBoolean("sound");
    }


}
