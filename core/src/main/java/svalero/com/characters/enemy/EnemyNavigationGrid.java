package svalero.com.characters.enemy;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import svalero.com.managers.LevelManager;

import static svalero.com.utils.Constants.TILE_SIZE_PX;

class EnemyNavigationGrid implements IndexedGraph<EnemyNavigationGrid.Node> {

    private static final int[][] CARDINAL_DIRECTIONS = {
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1}
    };

    private final LevelManager levelManager;
    private final int width;
    private final int height;
    private final float agentWidth;
    private final float agentHeight;
    private final Node[] nodes;
    private final Array<Connection<Node>> emptyConnections = new Array<>(0);
    private final Array<Connection<Node>> reusableConnections = new Array<>(4);
    private final Rectangle probeBounds = new Rectangle();
    private final Polygon probePolygon = new Polygon();

    EnemyNavigationGrid(LevelManager levelManager, float agentWidth, float agentHeight) {
        this.levelManager = levelManager;
        this.width = Math.max(1, (int) Math.ceil(levelManager.getMapWorldWidth() / TILE_SIZE_PX));
        this.height = Math.max(1, (int) Math.ceil(levelManager.getMapWorldHeight() / TILE_SIZE_PX));
        this.agentWidth = Math.max(1f, agentWidth);
        this.agentHeight = Math.max(1f, agentHeight);
        this.nodes = new Node[width * height];
        buildNodes();
    }

    Node getNearestPassableNode(Vector2 worldPosition) {
        int baseX = worldToTileX(worldPosition.x);
        int baseY = worldToTileY(worldPosition.y);
        Node direct = getNode(baseX, baseY);
        if (direct != null && direct.passable) {
            return direct;
        }

        int maxRadius = Math.max(width, height);
        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int y = baseY - radius; y <= baseY + radius; y++) {
                for (int x = baseX - radius; x <= baseX + radius; x++) {
                    if (Math.abs(x - baseX) != radius && Math.abs(y - baseY) != radius) continue;
                    Node candidate = getNode(x, y);
                    if (candidate != null && candidate.passable) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    boolean hasLineOfSight(Vector2 fromCenter, Vector2 toCenter) {
        float distance = fromCenter.dst(toCenter);
        if (distance <= 0.001f) return true;

        float step = TILE_SIZE_PX * 0.5f;
        int samples = Math.max(1, (int) Math.ceil(distance / step));
        float probeSize = Math.max(2f, Math.min(agentWidth, agentHeight) * 0.25f);

        for (int i = 1; i < samples; i++) {
            float alpha = i / (float) samples;
            float x = fromCenter.x + (toCenter.x - fromCenter.x) * alpha;
            float y = fromCenter.y + (toCenter.y - fromCenter.y) * alpha;
            if (isBlockedAtCenter(x, y, probeSize, probeSize)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getIndex(Node node) {
        return node.index;
    }

    @Override
    public int getNodeCount() {
        return nodes.length;
    }

    @Override
    public Array<Connection<Node>> getConnections(Node fromNode) {
        if (fromNode == null || !fromNode.passable) {
            return emptyConnections;
        }

        reusableConnections.clear();
        for (int[] direction : CARDINAL_DIRECTIONS) {
            Node toNode = getNode(fromNode.tileX + direction[0], fromNode.tileY + direction[1]);
            if (toNode != null && toNode.passable) {
                reusableConnections.add(new GridConnection(fromNode, toNode));
            }
        }
        return reusableConnections;
    }

    private void buildNodes() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = toIndex(x, y);
                float centerX = x * TILE_SIZE_PX + TILE_SIZE_PX * 0.5f;
                float centerY = y * TILE_SIZE_PX + TILE_SIZE_PX * 0.5f;
                boolean passable = !isBlockedAtCenter(centerX, centerY, agentWidth, agentHeight);
                nodes[index] = new Node(index, x, y, centerX, centerY, passable);
            }
        }
    }

    private boolean isBlockedAtCenter(float centerX, float centerY, float width, float height) {
        probeBounds.set(
            centerX - width * 0.5f,
            centerY - height * 0.5f,
            width,
            height
        );
        probePolygon.setVertices(new float[]{
            0f, 0f,
            probeBounds.width, 0f,
            probeBounds.width, probeBounds.height,
            0f, probeBounds.height
        });
        probePolygon.setPosition(probeBounds.x, probeBounds.y);
        return levelManager.isBlocked(probePolygon, probeBounds, false);
    }

    private Node getNode(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return null;
        return nodes[toIndex(x, y)];
    }

    private int worldToTileX(float x) {
        return clamp((int) (x / TILE_SIZE_PX), 0, width - 1);
    }

    private int worldToTileY(float y) {
        return clamp((int) (y / TILE_SIZE_PX), 0, height - 1);
    }

    private int toIndex(int x, int y) {
        return y * width + x;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static class Node {
        final int index;
        final int tileX;
        final int tileY;
        final float centerX;
        final float centerY;
        final boolean passable;

        Node(int index, int tileX, int tileY, float centerX, float centerY, boolean passable) {
            this.index = index;
            this.tileX = tileX;
            this.tileY = tileY;
            this.centerX = centerX;
            this.centerY = centerY;
            this.passable = passable;
        }
    }

    static class ManhattanHeuristic implements Heuristic<Node> {
        @Override
        public float estimate(Node node, Node endNode) {
            return Math.abs(endNode.tileX - node.tileX) + Math.abs(endNode.tileY - node.tileY);
        }
    }

    private static class GridConnection implements Connection<Node> {
        private final Node fromNode;
        private final Node toNode;

        GridConnection(Node fromNode, Node toNode) {
            this.fromNode = fromNode;
            this.toNode = toNode;
        }

        @Override
        public float getCost() {
            return 1f;
        }

        @Override
        public Node getFromNode() {
            return fromNode;
        }

        @Override
        public Node getToNode() {
            return toNode;
        }
    }
}
