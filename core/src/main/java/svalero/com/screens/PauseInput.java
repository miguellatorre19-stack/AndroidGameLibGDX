package svalero.com.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class PauseInput {

    public PauseAction pollAction(PauseOverlay overlay) {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            return PauseAction.NONE;
        }

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (overlay.getResumeButtonBounds().contains(mouseX, mouseY)) {
            return PauseAction.RESUME;
        }
        if (overlay.getOptionsButtonBounds().contains(mouseX, mouseY)) {
            return PauseAction.TOGGLE_OPTIONS;
        }
        if (overlay.getQuitButtonBounds().contains(mouseX, mouseY)) {
            return PauseAction.QUIT_TO_MENU;
        }
        if (overlay.getSoundButtonBounds().contains(mouseX, mouseY)) {
            return PauseAction.TOGGLE_SOUND;
        }

        return PauseAction.NONE;
    }
}
