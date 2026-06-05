package svalero.com.items;

import com.badlogic.gdx.Input;

public enum PowerUpType {
    SPEED("speed", Input.Keys.NUM_1, "1", 8f),
    SHIELD("shield", Input.Keys.NUM_2, "2", 12f),
    MASTER_KEY("master_key", Input.Keys.NUM_3, "3", 3f);

    private final String id;
    private final int activationKey;
    private final String activationLabel;
    private final float durationSec;

    PowerUpType(String id, int activationKey, String activationLabel, float durationSec) {
        this.id = id;
        this.activationKey = activationKey;
        this.activationLabel = activationLabel;
        this.durationSec = durationSec;
    }

    public String id() {
        return id;
    }

    public int activationKey() {
        return activationKey;
    }

    public String activationLabel() {
        return activationLabel;
    }

    public float durationSec() {
        return durationSec;
    }

    public static PowerUpType fromId(String id) {
        if (id == null) return null;
        String normalized = id.trim().toLowerCase().replace(' ', '_');
        for (PowerUpType type : values()) {
            if (type.id.equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
