package category3_grid_traversal;

import java.util.*;

/**
 * Problem 29: Warehouse Forklift Routing Matrix
 * 
 * Three-part progression:
 * - Part 1: Add coordinate locking to prevent collision
 * - Part 2: Find shortest path through warehouse grid
 * - Part 3: Schedule collision-free routes for multiple forklifts
 */
public class Problem29_WarehouseForklifRouting {

    /**
     * Part 1: Bug Fix - Coordinate Locking
     * Issue: Two forklifts can occupy same coordinate simultaneously
     * Solution: Add coordinate locking mechanism
     */
    public static class Part1_BugFix {
        private static final Set<String> lockedCoordinates = new HashSet<>();

        /**
         * Lock a coordinate for a forklift
         */
        public static boolean lockCoordinate(int row, int col, String forklifId) {
            String key = row + "," + col;
            if (lockedCoordinates.contains(key)) {
                return false; // Already locked
            }
            lockedCoordinates.add(key);
            return true;
        }

        /**
         * Unlock a coordinate
         */
        public static void unlockCoordinate(int row, int col) {
            String key = row + "," + col;
            lockedCoordinates.remove(key);
        }

        /**
         * Check if coordinate is available
         */
        public static boolean isCoordinateAvailable(int row, int col) {
            String key = row + "," + col;
            return !lockedCoordinates.contains(key);
        }

        /**
         * Clear all locks
         */
        public static void clearAllLocks() {
            lockedCoordinates.clear();
        }
    }

    /**
     * Part 2: Shortest Path Algorithm
     * Find optimal route through warehouse avoiding obstacles
     */
    public static class Part2_ShortestPath {
        private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        public static class State {
            public int row, col, distance;

            public State(int row, int col, int distance) {
                this.row = row;
                this.col = col;
                this.distance = distance;
            }
        }

        /**
         * Find shortest path using BFS
         * Time Complexity: O(rows * cols)
         */
        public static List<String> findShortestPath(char[][] warehouse,
                                                    int startRow, int startCol,
                                                    int endRow, int endCol) {
            if (warehouse == null || warehouse.length == 0) {
                return new ArrayList<>();
            }

            int rows = warehouse.length;
            int cols = warehouse[0].length;
            Queue<State> queue = new LinkedList<>();
            Map<String, String> parent = new HashMap<>();
            Set<String> visited = new HashSet<>();

            String startKey = startRow + "," + startCol;
            String endKey = endRow + "," + endCol;

            queue.offer(new State(startRow, startCol, 0));
            visited.add(startKey);

            while (!queue.isEmpty()) {
                State current = queue.poll();

                if (current.row == endRow && current.col == endCol) {
                    // Found path, reconstruct it
                    return reconstructPath(parent, startKey, endKey);
                }

                // Try all 4 directions
                for (int[] dir : DIRECTIONS) {
                    int newRow = current.row + dir[0];
                    int newCol = current.col + dir[1];

                    if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                        continue;
                    }
                    if (warehouse[newRow][newCol] == '#') { // Wall
                        continue;
                    }

                    String key = newRow + "," + newCol;
                    if (visited.contains(key)) {
                        continue;
                    }

                    visited.add(key);
                    parent.put(key, current.row + "," + current.col);
                    queue.offer(new State(newRow, newCol, current.distance + 1));
                }
            }

            return new ArrayList<>(); // No path found
        }

        private static List<String> reconstructPath(Map<String, String> parent,
                                                    String startKey, String endKey) {
            List<String> path = new ArrayList<>();
            String current = endKey;

            while (current != null) {
                path.add(0, current);
                current = parent.get(current);
            }

            return path;
        }
    }

    /**
     * Part 3: Multi-Forklift Collision Avoidance
     * Schedule collision-free routes for multiple forklifts
     */
    public static class Part3_CollisionAvoidance {
        public static class ForklifRoute {
            public String forklifId;
            public List<String> path;
            public int startTime;

            public ForklifRoute(String id, List<String> path, int startTime) {
                this.forklifId = id;
                this.path = path;
                this.startTime = startTime;
            }
        }

        /**
         * Calculate time-space conflicts between two routes
         */
        private static boolean hasTimeSpaceConflict(ForklifRoute route1, ForklifRoute route2) {
            int maxLen = Math.max(route1.path.size(), route2.path.size());

            for (int step = 0; step < maxLen; step++) {
                int time1 = route1.startTime + step;
                int time2 = route2.startTime + step;

                // Get positions at this time
                String pos1 = step < route1.path.size() ? route1.path.get(step) :
                              route1.path.get(route1.path.size() - 1);
                String pos2 = step < route2.path.size() ? route2.path.get(step) :
                              route2.path.get(route2.path.size() - 1);

                // If at same position at overlapping times, conflict
                if (pos1.equals(pos2) && time1 == time2) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Schedule routes for multiple forklifts to avoid collisions
         */
        public static List<ForklifRoute> scheduleCollisionFreeRoutes(
            List<ForklifRoute> proposedRoutes) {

            List<ForklifRoute> scheduledRoutes = new ArrayList<>();
            int currentTime = 0;

            for (ForklifRoute route : proposedRoutes) {
                route.startTime = currentTime;

                // Check for conflicts with already scheduled routes
                boolean hasConflict = false;
                for (ForklifRoute scheduled : scheduledRoutes) {
                    if (hasTimeSpaceConflict(route, scheduled)) {
                        hasConflict = true;
                        break;
                    }
                }

                if (hasConflict) {
                    // Delay this route
                    currentTime += route.path.size() + 1;
                    route.startTime = currentTime;
                }

                scheduledRoutes.add(route);
            }

            return scheduledRoutes;
        }
    }

    // Test methods
    public static void main(String[] args) {
        System.out.println("=== Problem 29: Warehouse Forklift Routing ===\n");

        // Part 1: Test locking
        System.out.println("Part 1: Coordinate Locking");
        boolean locked = Part1_BugFix.lockCoordinate(1, 1, "forklift1");
        System.out.println("Lock (1,1) for forklift1: " + locked);
        locked = Part1_BugFix.lockCoordinate(1, 1, "forklift2");
        System.out.println("Lock (1,1) for forklift2: " + locked);
        System.out.println("(1,1) available: " + Part1_BugFix.isCoordinateAvailable(1, 1));
        Part1_BugFix.unlockCoordinate(1, 1);
        System.out.println("After unlock, (1,1) available: " + Part1_BugFix.isCoordinateAvailable(1, 1) + "\n");

        // Part 2: Test shortest path
        System.out.println("Part 2: Shortest Path");
        Part1_BugFix.clearAllLocks();
        char[][] warehouse = {
            {'.', '.', '.', '.', '.'},
            {'.', '#', '#', '.', '.'},
            {'.', '.', '.', '.', '.'},
            {'.', '.', '#', '#', '.'},
            {'.', '.', '.', '.', '.'}
        };

        List<String> path = Part2_ShortestPath.findShortestPath(warehouse, 0, 0, 4, 4);
        System.out.println("Shortest path from (0,0) to (4,4): " + path + "\n");

        // Part 3: Test collision avoidance
        System.out.println("Part 3: Collision-Free Scheduling");
        Part3_CollisionAvoidance.ForklifRoute route1 =
            new Part3_CollisionAvoidance.ForklifRoute("f1",
                Arrays.asList("0,0", "1,0", "2,0"), 0);
        Part3_CollisionAvoidance.ForklifRoute route2 =
            new Part3_CollisionAvoidance.ForklifRoute("f2",
                Arrays.asList("0,0", "0,1", "0,2"), 0);

        List<Part3_CollisionAvoidance.ForklifRoute> scheduled =
            Part3_CollisionAvoidance.scheduleCollisionFreeRoutes(
                Arrays.asList(route1, route2));

        for (Part3_CollisionAvoidance.ForklifRoute r : scheduled) {
            System.out.println("Forklift " + r.forklifId + " starts at time " + r.startTime +
                             ", path: " + r.path);
        }
    }
}
