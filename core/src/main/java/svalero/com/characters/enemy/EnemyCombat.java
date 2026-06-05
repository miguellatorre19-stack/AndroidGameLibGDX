package svalero.com.characters.enemy;

import svalero.com.characters.Player;
import svalero.com.managers.AudioManager;

class EnemyCombat {

    private final EnemyActor actor;
    private final AudioManager audioManager;
    private final float attackCooldownSec;
    private float attackCooldown;
    private boolean disposed;

    EnemyCombat(EnemyActor actor, float attackCooldownSec) {
        this.actor = actor;
        this.attackCooldownSec = attackCooldownSec;
        this.attackCooldown = 0f;
        this.disposed = false;
        this.audioManager = new AudioManager();
        audioManager.loadSfx("enemy_attack", "audio/sound/armor-light.wav");
        audioManager.loadSfx("enemy_damaged", "audio/sound/vampire_attack.mp3");
        audioManager.loadSfx("enemy_death", "audio/sound/vampire_attack.mp3");
    }

    void update(float dt) {
        if (attackCooldown > 0f) {
            attackCooldown -= dt;
        }
    }

    void attack(Player target) {
        if (actor.isDead() || actor.isDying()) return;
        if (attackCooldown > 0f) return;

        actor.playAttackAnimation();
        audioManager.playSfx("enemy_attack");
        if (target != null) {
            target.affected();
        }
        attackCooldown = attackCooldownSec;
    }

    void takeDamage() {
        if (actor.isDead() || actor.isDying()) return;
        actor.loseLife(1);
        audioManager.playSfx("enemy_damaged");
        if (actor.getLivesCount() <= 0) {
            die();
            return;
        }
        actor.playDamagedAnimation();
    }

    void die() {
        if (actor.isDead() || actor.isDying()) return;
        actor.playDeathAnimation();
        audioManager.playSfx("enemy_death");
    }

    void dispose() {
        if (disposed) return;
        disposed = true;
        audioManager.dispose();
    }
}
