package svalero.com.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import svalero.com.managers.SpriteManager;

public class Neutral extends Character{

    private final Animation<TextureRegion> idleAnimation;
    private final String interactionMessage;
    private boolean dying;
    private boolean interactionTriggered;
    private boolean waitingPopupDismiss;

    public Neutral(Vector2 position, SpriteManager spriteManager, Animation<TextureRegion> idleAnimation, String interactionMessage) {
        super(position, spriteManager, idleAnimation);
        this.idleAnimation = idleAnimation;
        this.interactionMessage = interactionMessage;
        dying = false;
        interactionTriggered = false;
        waitingPopupDismiss = false;
    }

    public String getInteractionMessage() {
        return interactionMessage;
    }

    public boolean hasInteractionTriggered() {
        return interactionTriggered;
    }

    public boolean isWaitingPopupDismiss() {
        return waitingPopupDismiss;
    }

    public void markInteractionStarted() {
        interactionTriggered = true;
        waitingPopupDismiss = true;
    }

    public void onPopupDismissed() {
        if (!waitingPopupDismiss) return;
        waitingPopupDismiss = false;
        die();
    }

    @Override
    public void attack() {

    }

    @Override
    public void render(Batch batch) {
        if (dead) return;
        super.render(batch);
    }

    @Override
    public void die() {
        if (dead || dying) return;
        dying = true;
        dead = true;
    }

    @Override
    public void update() {

    }

    @Override
    public void affected() {

    }
}
