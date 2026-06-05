package svalero.com.StateMachine;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import svalero.com.characters.enemy.Enemy;

public enum EnemyState implements State<Enemy> {

    PATRULLANDO {
        @Override
        public void enter(Enemy enemy) {
            enemy.onEnterPatrolState();
        }

        @Override
        public void update(Enemy enemy) {
            enemy.patrol();
            if (enemy.canAttackPlayer()) {
                enemy.getStateMachine().changeState(ATACANDO);
                return;
            }
            if (enemy.canSeePlayer()) {
                enemy.getStateMachine().changeState(PERSEGUIENDO);
            }
        }

        @Override
        public void exit(Enemy enemy) {
            enemy.onExitPatrolState();
        }

        @Override
        public boolean onMessage(Enemy enemy, Telegram telegram) {
            return false;
        }
    },

    PERSEGUIENDO {
        @Override
        public void enter(Enemy enemy) {
            enemy.onEnterChaseState();
        }

        @Override
        public void update(Enemy enemy) {
            if (!enemy.canTrackPlayer()) {
                enemy.getStateMachine().changeState(PATRULLANDO);
                return;
            }
            if (enemy.canAttackPlayer()) {
                enemy.getStateMachine().changeState(ATACANDO);
                return;
            }
            enemy.chasePlayerWithSteering();
        }

        @Override
        public void exit(Enemy enemy) {
            enemy.onExitChaseState();
        }

        @Override
        public boolean onMessage(Enemy enemy, Telegram telegram) {
            return false;
        }
    },

    ATACANDO {
        @Override
        public void enter(Enemy enemy) {
            enemy.onEnterAttackState();
        }

        @Override
        public void update(Enemy enemy) {
            enemy.tryAttackPlayer();

            if (enemy.canAttackPlayer()) {
                return;
            }
            if (enemy.canTrackPlayer()) {
                enemy.getStateMachine().changeState(PERSEGUIENDO);
            } else {
                enemy.getStateMachine().changeState(PATRULLANDO);
            }
        }

        @Override
        public void exit(Enemy enemy) {
            enemy.onExitAttackState();
        }

        @Override
        public boolean onMessage(Enemy enemy, Telegram telegram) {
            return false;
        }
    }
}
