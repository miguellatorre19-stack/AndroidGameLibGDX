package svalero.com.characters.enemy;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import svalero.com.StateMachine.EnemyState;
import svalero.com.characters.Player;
import svalero.com.managers.LevelManager;

import static svalero.com.utils.Constants.TILE_SIZE_PX;

class EnemyAIController {

    private static final float ARRIVE_TOLERANCE_PX = 3f;
    private static final float ARRIVE_TIME_TO_TARGET_SEC = 0.1f;
    private static final float VELOCITY_DAMPING = 0.85f;
    private static final float PATH_REFRESH_INTERVAL_SEC = 0.35f;
    private static final float WAYPOINT_REACHED_DISTANCE_PX = 4f;
    private static final float SEPARATION_RADIUS_PX = 12f;
    private static final float SEPARATION_STRENGTH = 0.9f;
    private static final float PATROL_STUCK_SWITCH_SEC = 0.45f;

    private final Enemy owner;
    private final float chaseSpeedPxPerSec;
    private final float aggroDistancePxSquared;

    private final StateMachine<Enemy, EnemyState> stateMachine;
    private final AgentSteerable steeringOwner;
    private final TargetSteerable steeringTarget;
    private final SteeringAcceleration<Vector2> steeringOutput;
    private final Arrive<Vector2> chaseBehavior;
    private final Arrive<Vector2> patrolBehavior;
    private final DefaultGraphPath<EnemyNavigationGrid.Node> path;
    private final EnemyNavigationGrid.ManhattanHeuristic pathHeuristic;
    private final Vector2 pathTargetPosition;
    private final Vector2 ownerCenterPosition;
    private final Vector2 playerCenterPosition;
    private final Vector2 separationVector;

    private final Vector2 patrolPointA;
    private final Vector2 patrolPointB;
    private Vector2 currentPatrolTarget;
    private float patrolStuckTimer;

    private Player currentPlayer;
    private Array<Enemy> currentNearbyEnemies;
    private LevelManager currentLevelManager;
    private LevelManager navigationLevelManager;
    private EnemyNavigationGrid navigationGrid;
    private com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder<EnemyNavigationGrid.Node> pathFinder;
    private float currentDt;
    private float pathRefreshTimer;
    private int pathIndex;
    private int navigationLevelIndex;

    EnemyAIController(Enemy owner, float chaseSpeedPxPerSec, float aggroDistancePx, float patrolRadiusPx) {
        this.owner = owner;
        this.chaseSpeedPxPerSec = chaseSpeedPxPerSec;
        this.aggroDistancePxSquared = aggroDistancePx * aggroDistancePx;

        steeringOwner = new AgentSteerable(owner.aiPosition());
        steeringOwner.setMaxLinearSpeed(chaseSpeedPxPerSec);
        steeringOwner.setMaxLinearAcceleration(chaseSpeedPxPerSec * 4f);
        steeringTarget = new TargetSteerable();
        steeringOutput = new SteeringAcceleration<>(new Vector2());

        chaseBehavior = new Arrive<>(steeringOwner, steeringTarget)
            .setTimeToTarget(ARRIVE_TIME_TO_TARGET_SEC)
            .setArrivalTolerance(ARRIVE_TOLERANCE_PX)
            .setDecelerationRadius(2.5f * TILE_SIZE_PX);

        patrolBehavior = new Arrive<>(steeringOwner, steeringTarget)
            .setTimeToTarget(ARRIVE_TIME_TO_TARGET_SEC)
            .setArrivalTolerance(ARRIVE_TOLERANCE_PX)
            .setDecelerationRadius(1.5f * TILE_SIZE_PX);

        path = new DefaultGraphPath<>();
        pathHeuristic = new EnemyNavigationGrid.ManhattanHeuristic();
        pathTargetPosition = new Vector2();
        ownerCenterPosition = new Vector2();
        playerCenterPosition = new Vector2();
        separationVector = new Vector2();
        pathRefreshTimer = 0f;
        pathIndex = 0;
        navigationLevelIndex = -1;

        patrolPointA = new Vector2(owner.aiPosition().x - patrolRadiusPx, owner.aiPosition().y);
        patrolPointB = new Vector2(owner.aiPosition().x + patrolRadiusPx, owner.aiPosition().y);
        currentPatrolTarget = patrolPointB;
        patrolStuckTimer = 0f;
        clampPatrolPoints();

        stateMachine = new DefaultStateMachine<>(owner, EnemyState.PATRULLANDO);
    }

    void update(Player player, LevelManager levelManager, float dt, Array<Enemy> nearbyEnemies) {
        currentPlayer = player;
        currentLevelManager = levelManager;
        currentDt = dt;
        currentNearbyEnemies = nearbyEnemies;
        stateMachine.update();
    }

    StateMachine<Enemy, EnemyState> getStateMachine() {
        return stateMachine;
    }

    boolean canSeePlayer() {
        return canTrackPlayer() && hasLineOfSightToPlayer();
    }

    boolean canTrackPlayer() {
        if (currentPlayer == null) return false;
        float enemyCenterX = owner.aiRect().x + owner.aiRect().width * 0.5f;
        float enemyCenterY = owner.aiRect().y + owner.aiRect().height * 0.5f;
        float playerCenterX = currentPlayer.getRect().x + currentPlayer.getRect().width * 0.5f;
        float playerCenterY = currentPlayer.getRect().y + currentPlayer.getRect().height * 0.5f;
        float toPlayerX = playerCenterX - enemyCenterX;
        float toPlayerY = playerCenterY - enemyCenterY;
        float distanceSquared = toPlayerX * toPlayerX + toPlayerY * toPlayerY;
        return distanceSquared <= aggroDistancePxSquared;
    }

    boolean canAttackPlayer() {
        return currentPlayer != null
            && Intersector.overlapConvexPolygons(owner.aiHitbox(), currentPlayer.getHitbox());
    }

    void patrol() {
        steeringTarget.setPosition(currentPatrolTarget);
        boolean moved = applySteering(patrolBehavior);

        if (moved) {
            patrolStuckTimer = 0f;
        } else {
            patrolStuckTimer += currentDt;
        }

        boolean reachedTarget = owner.aiPosition().dst2(currentPatrolTarget) <= ARRIVE_TOLERANCE_PX * ARRIVE_TOLERANCE_PX;
        if (reachedTarget || patrolStuckTimer >= PATROL_STUCK_SWITCH_SEC) {
            switchPatrolTarget();
        }

        if (moved) {
            owner.setMovementAnimation();
        } else {
            owner.setIdleAnimation();
        }
    }

    void chasePlayerWithSteering() {
        if (currentPlayer == null) return;
        boolean moved;
        if (hasLineOfSightToPlayer()) {
            clearPath();
            steeringTarget.setPosition(currentPlayer.getPosition());
            moved = applySteering(chaseBehavior);
        } else {
            moved = followPathToPlayer();
        }

        if (moved) {
            owner.setMovementAnimation();
        } else {
            owner.setIdleAnimation();
        }
    }

    void tryAttackPlayer() {
        if (!canAttackPlayer()) {
            owner.setIdleAnimation();
            return;
        }
        owner.attack(currentPlayer);
    }

    void onEnterPatrolState() {
        steeringOwner.getLinearVelocity().scl(0.8f);
        patrolStuckTimer = 0f;
    }

    void onEnterAttackState() {
        steeringOwner.getLinearVelocity().setZero();
    }

    private boolean applySteering(Arrive<Vector2> behavior) {
        behavior.calculateSteering(steeringOutput);
        Vector2 acceleration = steeringOutput.linear;
        steeringOwner.getLinearVelocity().mulAdd(acceleration, currentDt);
        applyEnemySeparation(steeringOwner.getLinearVelocity());
        steeringOwner.getLinearVelocity().scl(VELOCITY_DAMPING);
        steeringOwner.getLinearVelocity().limit(chaseSpeedPxPerSec);

        float moveX = steeringOwner.getLinearVelocity().x * currentDt;
        float moveY = steeringOwner.getLinearVelocity().y * currentDt;
        float oldX = owner.aiPosition().x;
        float oldY = owner.aiPosition().y;
        moveWithCollision(moveX, moveY, currentLevelManager);
        boolean moved = oldX != owner.aiPosition().x || oldY != owner.aiPosition().y;
        if (!moved) {
            steeringOwner.getLinearVelocity().scl(0.5f);
        }
        return moved;
    }

    private void applyEnemySeparation(Vector2 velocity) {
        if (currentNearbyEnemies == null || currentNearbyEnemies.size <= 1) return;

        separationVector.setZero();
        float ownerCenterX = owner.aiRect().x + owner.aiRect().width * 0.5f;
        float ownerCenterY = owner.aiRect().y + owner.aiRect().height * 0.5f;
        float personalSpace = Math.max(
            SEPARATION_RADIUS_PX,
            Math.max(owner.aiRect().width, owner.aiRect().height) * 0.65f
        );
        float personalSpace2 = personalSpace * personalSpace;

        for (Enemy other : currentNearbyEnemies) {
            if (other == owner || other.isDead()) continue;

            float otherCenterX = other.aiRect().x + other.aiRect().width * 0.5f;
            float otherCenterY = other.aiRect().y + other.aiRect().height * 0.5f;
            float awayX = ownerCenterX - otherCenterX;
            float awayY = ownerCenterY - otherCenterY;
            float distance2 = awayX * awayX + awayY * awayY;
            if (distance2 <= 0.0001f || distance2 > personalSpace2) continue;

            float distance = (float) Math.sqrt(distance2);
            float proximity = 1f - distance / personalSpace;
            separationVector.x += (awayX / distance) * proximity;
            separationVector.y += (awayY / distance) * proximity;
        }

        if (separationVector.isZero()) return;

        separationVector.nor().scl(chaseSpeedPxPerSec * SEPARATION_STRENGTH);
        velocity.add(separationVector);
    }

    private boolean followPathToPlayer() {
        if (currentLevelManager == null || currentPlayer == null) return false;

        pathRefreshTimer -= currentDt;
        if (pathRefreshTimer <= 0f || path.getCount() == 0 || pathIndex >= path.getCount()) {
            recalculatePathToPlayer();
        }

        if (path.getCount() == 0 || pathIndex >= path.getCount()) {
            return false;
        }

        EnemyNavigationGrid.Node waypoint = path.get(pathIndex);
        pathTargetPosition.set(
            waypoint.centerX - owner.aiRect().width * 0.5f,
            waypoint.centerY - owner.aiRect().height * 0.5f
        );

        if (owner.aiPosition().dst2(pathTargetPosition) <= WAYPOINT_REACHED_DISTANCE_PX * WAYPOINT_REACHED_DISTANCE_PX) {
            pathIndex++;
            if (pathIndex >= path.getCount()) {
                return false;
            }
            waypoint = path.get(pathIndex);
            pathTargetPosition.set(
                waypoint.centerX - owner.aiRect().width * 0.5f,
                waypoint.centerY - owner.aiRect().height * 0.5f
            );
        }

        steeringTarget.setPosition(pathTargetPosition);
        return applySteering(chaseBehavior);
    }

    private void recalculatePathToPlayer() {
        ensureNavigationGrid();
        path.clear();
        pathIndex = 0;
        pathRefreshTimer = PATH_REFRESH_INTERVAL_SEC;

        if (navigationGrid == null || pathFinder == null || currentPlayer == null) return;

        EnemyNavigationGrid.Node startNode = navigationGrid.getNearestPassableNode(getOwnerCenter());
        EnemyNavigationGrid.Node endNode = navigationGrid.getNearestPassableNode(getPlayerCenter());
        if (startNode == null || endNode == null) return;

        boolean found = pathFinder.searchNodePath(startNode, endNode, pathHeuristic, path);
        if (!found || path.getCount() == 0) {
            path.clear();
            return;
        }

        pathIndex = path.getCount() > 1 ? 1 : 0;
    }

    private boolean hasLineOfSightToPlayer() {
        if (currentLevelManager == null || currentPlayer == null) return false;
        ensureNavigationGrid();
        if (navigationGrid == null) return false;
        return navigationGrid.hasLineOfSight(getOwnerCenter(), getPlayerCenter());
    }

    private void ensureNavigationGrid() {
        if (currentLevelManager == null) return;
        int currentLevelIndex = currentLevelManager.getCurrentLevelIndex();
        if (
            navigationGrid != null
                && navigationLevelManager == currentLevelManager
                && navigationLevelIndex == currentLevelIndex
        ) {
            return;
        }

        navigationLevelManager = currentLevelManager;
        navigationLevelIndex = currentLevelIndex;
        navigationGrid = new EnemyNavigationGrid(
            currentLevelManager,
            owner.aiRect().width,
            owner.aiRect().height
        );
        pathFinder = new com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder<>(navigationGrid);
        clearPath();
    }

    private void clearPath() {
        path.clear();
        pathIndex = 0;
        pathRefreshTimer = 0f;
    }

    private Vector2 getOwnerCenter() {
        return ownerCenterPosition.set(
            owner.aiRect().x + owner.aiRect().width * 0.5f,
            owner.aiRect().y + owner.aiRect().height * 0.5f
        );
    }

    private Vector2 getPlayerCenter() {
        return playerCenterPosition.set(
            currentPlayer.getRect().x + currentPlayer.getRect().width * 0.5f,
            currentPlayer.getRect().y + currentPlayer.getRect().height * 0.5f
        );
    }

    private void moveWithCollision(float moveX, float moveY, LevelManager levelManager) {
        if (moveX != 0f) {
            owner.aiUpdateFacingFromMovement(moveX);
            float oldX = owner.aiPosition().x;
            owner.aiPosition().x += moveX;
            owner.aiSyncHitboxFromPosition();
            if (levelManager != null && levelManager.isBlocked(owner.aiHitbox(), owner.aiRect(), false)) {
                owner.aiPosition().x = oldX;
                steeringOwner.getLinearVelocity().x = 0f;
                owner.aiSyncHitboxFromPosition();
            }
        }

        if (moveY != 0f) {
            float oldY = owner.aiPosition().y;
            owner.aiPosition().y += moveY;
            owner.aiSyncHitboxFromPosition();
            if (levelManager != null && levelManager.isBlocked(owner.aiHitbox(), owner.aiRect(), false)) {
                owner.aiPosition().y = oldY;
                steeringOwner.getLinearVelocity().y = 0f;
                owner.aiSyncHitboxFromPosition();
            }
        }

        owner.aiPosition().x = MathUtils.clamp(owner.aiPosition().x, 0f, Float.MAX_VALUE);
        owner.aiPosition().y = MathUtils.clamp(owner.aiPosition().y, 0f, Float.MAX_VALUE);
        owner.aiSyncHitboxFromPosition();
    }

    private void clampPatrolPoints() {
        patrolPointA.x = Math.max(0f, patrolPointA.x);
        patrolPointA.y = Math.max(0f, patrolPointA.y);
        patrolPointB.x = Math.max(0f, patrolPointB.x);
        patrolPointB.y = Math.max(0f, patrolPointB.y);
    }

    private void switchPatrolTarget() {
        currentPatrolTarget = currentPatrolTarget == patrolPointA ? patrolPointB : patrolPointA;
        patrolStuckTimer = 0f;
        steeringOwner.getLinearVelocity().scl(0.35f);
    }

    private static class AgentSteerable implements Steerable<Vector2> {
        private final Vector2 position;
        private final Vector2 linearVelocity;
        private boolean tagged;
        private float zeroLinearSpeedThreshold;
        private float maxLinearSpeed;
        private float maxLinearAcceleration;

        AgentSteerable(Vector2 position) {
            this.position = position;
            this.linearVelocity = new Vector2();
            this.zeroLinearSpeedThreshold = 0.001f;
        }

        @Override
        public Vector2 getLinearVelocity() {
            return linearVelocity;
        }

        @Override
        public float getAngularVelocity() {
            return 0f;
        }

        @Override
        public float getBoundingRadius() {
            return 1f;
        }

        @Override
        public boolean isTagged() {
            return tagged;
        }

        @Override
        public void setTagged(boolean tagged) {
            this.tagged = tagged;
        }

        @Override
        public float getZeroLinearSpeedThreshold() {
            return zeroLinearSpeedThreshold;
        }

        @Override
        public void setZeroLinearSpeedThreshold(float value) {
            this.zeroLinearSpeedThreshold = value;
        }

        @Override
        public float getMaxLinearSpeed() {
            return maxLinearSpeed;
        }

        @Override
        public void setMaxLinearSpeed(float maxLinearSpeed) {
            this.maxLinearSpeed = maxLinearSpeed;
        }

        @Override
        public float getMaxLinearAcceleration() {
            return maxLinearAcceleration;
        }

        @Override
        public void setMaxLinearAcceleration(float maxLinearAcceleration) {
            this.maxLinearAcceleration = maxLinearAcceleration;
        }

        @Override
        public float getMaxAngularSpeed() {
            return 0f;
        }

        @Override
        public void setMaxAngularSpeed(float maxAngularSpeed) {
        }

        @Override
        public float getMaxAngularAcceleration() {
            return 0f;
        }

        @Override
        public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        @Override
        public float getOrientation() {
            if (linearVelocity.isZero()) return 0f;
            return vectorToAngle(linearVelocity);
        }

        @Override
        public void setOrientation(float orientation) {
        }

        @Override
        public float vectorToAngle(Vector2 vector) {
            return (float) Math.atan2(-vector.x, vector.y);
        }

        @Override
        public Vector2 angleToVector(Vector2 outVector, float angle) {
            outVector.x = -MathUtils.sin(angle);
            outVector.y = MathUtils.cos(angle);
            return outVector;
        }

        @Override
        public Location<Vector2> newLocation() {
            return new SimpleLocation();
        }
    }

    private static class TargetSteerable implements Steerable<Vector2> {
        private final Vector2 position = new Vector2();
        private final Vector2 zeroVelocity = new Vector2();

        public void setPosition(Vector2 position) {
            this.position.set(position);
        }

        @Override
        public Vector2 getLinearVelocity() {
            return zeroVelocity;
        }

        @Override
        public float getAngularVelocity() {
            return 0f;
        }

        @Override
        public float getBoundingRadius() {
            return 0f;
        }

        @Override
        public boolean isTagged() {
            return false;
        }

        @Override
        public void setTagged(boolean tagged) {
        }

        @Override
        public float getZeroLinearSpeedThreshold() {
            return 0.001f;
        }

        @Override
        public void setZeroLinearSpeedThreshold(float value) {
        }

        @Override
        public float getMaxLinearSpeed() {
            return 0f;
        }

        @Override
        public void setMaxLinearSpeed(float maxLinearSpeed) {
        }

        @Override
        public float getMaxLinearAcceleration() {
            return 0f;
        }

        @Override
        public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        }

        @Override
        public float getMaxAngularSpeed() {
            return 0f;
        }

        @Override
        public void setMaxAngularSpeed(float maxAngularSpeed) {
        }

        @Override
        public float getMaxAngularAcceleration() {
            return 0f;
        }

        @Override
        public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        @Override
        public float getOrientation() {
            return 0f;
        }

        @Override
        public void setOrientation(float orientation) {
        }

        @Override
        public float vectorToAngle(Vector2 vector) {
            return (float) Math.atan2(-vector.x, vector.y);
        }

        @Override
        public Vector2 angleToVector(Vector2 outVector, float angle) {
            outVector.x = -MathUtils.sin(angle);
            outVector.y = MathUtils.cos(angle);
            return outVector;
        }

        @Override
        public Location<Vector2> newLocation() {
            return new SimpleLocation();
        }
    }

    private static class SimpleLocation implements Location<Vector2> {
        private final Vector2 position = new Vector2();
        private float orientation;

        @Override
        public Vector2 getPosition() {
            return position;
        }

        @Override
        public float getOrientation() {
            return orientation;
        }

        @Override
        public void setOrientation(float orientation) {
            this.orientation = orientation;
        }

        @Override
        public float vectorToAngle(Vector2 vector) {
            return (float) Math.atan2(-vector.x, vector.y);
        }

        @Override
        public Vector2 angleToVector(Vector2 outVector, float angle) {
            outVector.x = -MathUtils.sin(angle);
            outVector.y = MathUtils.cos(angle);
            return outVector;
        }

        @Override
        public Location<Vector2> newLocation() {
            return new SimpleLocation();
        }
    }
}
