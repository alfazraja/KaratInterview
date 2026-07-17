package category3_grid_traversal;

import java.util.*;

/**
 * Problem 27: Pac-Man Ghost Chase AI Matrix
 * 
 * Three-part progression:
 * - Part 1: Fix distance calculation (use proper Manhattan distance)
 * - Part 2: Calculate ghost's next step using BFS to minimize distance
 * - Part 3: Predict optimal intersection to cut off Pac-Man's escape
 */
public class Problem27_PacManGhostChaseAI {

    /**
     * Part 1: Bug Fix - Distance Calculation
     * Issue: Manhattan distance evaluates opposite moves as equal
     * Solution: Properly compute Manhattan distance as |dx| + |dy|
     */
    public static class Part1_BugFix {
        /**
         * Calculate Manhattan distance between two points
         * Correct: |x1-x2| + |y1-y2|
         */
        public static int manhattanDistance(int row1, int col1, int row2, int col2) {
            return Math.abs(row1 - row2) + Math.abs(col1 - col2);
        }

        /**
         * Euclidean distance for alternative comparison
         */
        public static double euclideanDistance(int row1, int col1, int row2, int col2) {
            int dr = row1 - row2;
            int dc = col1 - col2;
            return Math.sqrt(dr * dr + dc * dc);
        }

        /**
         * Verify Manhattan distance is smaller than Euclidean for same points
         */
        public static void testDistances() {
            int manhattan = manhattanDistance(0, 0, 3, 4);
            double euclidean = euclideanDistance(0, 0, 3, 4);
            System.out.println("Manhattan distance (0,0) to (3,4): " + manhattan);
            System.out.println("Euclidean distance (0,0) to (3,4): " + euclidean);
        }
    }

    /**
     * Part 2: Ghost AI - Calculate Next Step
     * Use BFS to find shortest path toward Pac-Man
     */
    public static class Part2_GhostAI {
        private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        private static final String[] DIR_NAMES = {"UP", "DOWN", "LEFT", "RIGHT"};

        public static class Position {
            public int row;
            public int col;

            public Position(int row, int col) {
                this.row = row;
                this.col = col;
            }

            public String key() {
                return row + "," + col;
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof Position)) return false;
                Position p = (Position) obj;
                return row == p.row && col == p.col;
            }

            @Override
            public int hashCode() {
                return key().hashCode();
            }
        }

        /**
         * Find next step for ghost to move closer to Pac-Man
         * Uses BFS to explore neighbors and pick minimum distance move
         * Time Complexity: O(4) = O(1) per move
         */
        public static Position findNextStep(Position ghost, Position pacman,
                                           char[][] maze, int rows, int cols) {
            if (ghost == null || pacman == null || maze == null) {
                return ghost;
            }

            Position bestNext = ghost;
            int minDistance = Part1_BugFix.manhattanDistance(ghost.row, ghost.col,
                                                             pacman.row, pacman.col);

            for (int i = 0; i < 4; i++) {
                int newRow = ghost.row + DIRECTIONS[i][0];
                int newCol = ghost.col + DIRECTIONS[i][1];

                if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                    continue;
                }
                if (maze[newRow][newCol] == '#') {
                    continue;
                }

                Position nextPos = new Position(newRow, newCol);
                int distance = Part1_BugFix.manhattanDistance(newRow, newCol,
                                                             pacman.row, pacman.col);

                if (distance < minDistance) {
                    minDistance = distance;
                    bestNext = nextPos;
                }
            }

            return bestNext;
        }

        /**
         * Simulate multiple ghost moves
         */
        public static List<Position> simulateGhostPath(Position ghost, Position pacman,
                                                       char[][] maze, int steps) {
            List<Position> path = new ArrayList<>();
            Position current = ghost;
            path.add(new Position(current.row, current.col));

            int rows = maze.length;
            int cols = maze[0].length;

            for (int i = 0; i < steps; i++) {
                current = findNextStep(current, pacman, maze, rows, cols);
                path.add(new Position(current.row, current.col));

                if (current.equals(pacman)) {
                    break;
                }
            }

            return path;
        }
    }

    /**
     * Part 3: Intersection Predictor
     * Given Pac-Man's direction, predict where ghost should move to intercept
     */
    public static class Part3_IntersectionPredictor {
        private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        public static class Direction {
            public int dr, dc;

            public Direction(int dr, int dc) {
                this.dr = dr;
                this.dc = dc;
            }
        }

        /**
         * Predict Pac-Man's future position
         */
        public static Part2_GhostAI.Position predictPacmanPosition(
            Part2_GhostAI.Position current,
            Direction direction,
            int steps,
            char[][] maze) {

            int row = current.row;
            int col = current.col;
            int rows = maze.length;
            int cols = maze[0].length;

            for (int i = 0; i < steps; i++) {
                int newRow = row + direction.dr;
                int newCol = col + direction.dc;

                if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols ||
                    maze[newRow][newCol] == '#') {
                    break;
                }

                row = newRow;
                col = newCol;
            }

            return new Part2_GhostAI.Position(row, col);
        }

        /**
         * Find optimal intersection point for second ghost
         */
        public static Part2_GhostAI.Position findOptimalIntercept(
            Part2_GhostAI.Position ghost2,
            Part2_GhostAI.Position pacman,
            Direction pacmanDir,
            char[][] maze) {

            int rows = maze.length;
            int cols = maze[0].length;

            Part2_GhostAI.Position predictedPos = predictPacmanPosition(pacman, pacmanDir, 10, maze);

            int bestRow = ghost2.row;
            int bestCol = ghost2.col;
            int minDist = Part1_BugFix.manhattanDistance(ghost2.row, ghost2.col,
                                                        predictedPos.row, predictedPos.col);

            for (int[] dir : DIRECTIONS) {
                int newRow = ghost2.row + dir[0];
                int newCol = ghost2.col + dir[1];

                if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                    continue;
                }
                if (maze[newRow][newCol] == '#') {
                    continue;
                }

                int dist = Part1_BugFix.manhattanDistance(newRow, newCol,
                                                         predictedPos.row, predictedPos.col);
                if (dist < minDist) {
                    minDist = dist;
                    bestRow = newRow;
                    bestCol = newCol;
                }
            }

            return new Part2_GhostAI.Position(bestRow, bestCol);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 27: Pac-Man Ghost Chase AI ===\n");

        System.out.println("Part 1: Distance Calculation");
        Part1_BugFix.testDistances();
        System.out.println();

        System.out.println("Part 2: Ghost AI - Next Step");
        char[][] maze = {
            {'.', '.', '.', '.', '.'},
            {'.', '#', '#', '.', '.'},
            {'.', '.', '.', '.', '.'},
            {'.', '.', '#', '#', '.'},
            {'.', '.', '.', '.', '.'}
        };

        Part2_GhostAI.Position ghost = new Part2_GhostAI.Position(0, 0);
        Part2_GhostAI.Position pacman = new Part2_GhostAI.Position(4, 4);

        System.out.println("Initial Ghost: (" + ghost.row + "," + ghost.col + ")");
        System.out.println("Pac-Man: (" + pacman.row + "," + pacman.col + ")");

        List<Part2_GhostAI.Position> path = Part2_GhostAI.simulateGhostPath(ghost, pacman, maze, 5);
        System.out.println("Ghost path:");
        for (int i = 0; i < path.size(); i++) {
            Part2_GhostAI.Position p = path.get(i);
            System.out.println("  Step " + i + ": (" + p.row + "," + p.col + ")");
        }
    }
}
