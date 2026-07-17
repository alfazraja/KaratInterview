package category3_grid_traversal;

import java.util.*;

/**
 * Problem 28: Agricultural Drone Crop Health Matrix
 * 
 * Three-part progression:
 * - Part 1: Fix threshold comparison (correct infrared value threshold)
 * - Part 2: Find largest contiguous square of healthy crops
 * - Part 3: Trace water flow path based on elevation data
 */
public class Problem28_AgriculturalDroneCropHealth {

    /**
     * Part 1: Bug Fix - Threshold Correction
     * Issue: Low threshold flags healthy crops as damaged
     * Solution: Use correct threshold value
     */
    public static class Part1_BugFix {
        private static final double HEALTH_THRESHOLD = 70.0;

        /**
         * Determine if crop is healthy based on infrared reading
         */
        public static boolean isCropHealthy(double infraredValue) {
            return infraredValue >= HEALTH_THRESHOLD;
        }

        /**
         * Validate and filter crop health readings
         */
        public static List<Double> filterHealthyReadings(List<Double> readings) {
            List<Double> healthy = new ArrayList<>();
            for (double reading : readings) {
                if (isCropHealthy(reading)) {
                    healthy.add(reading);
                }
            }
            return healthy;
        }
    }

    /**
     * Part 2: Largest Healthy Crop Square
     * Find the largest contiguous square block meeting health requirement
     */
    public static class Part2_LargestHealthySquare {
        /**
         * Find largest square of healthy crops
         * Time Complexity: O(rows * cols * min(rows, cols))
         */
        public static int findLargestHealthySquare(int[][] healthMatrix) {
            if (healthMatrix == null || healthMatrix.length == 0) {
                return 0;
            }

            int rows = healthMatrix.length;
            int cols = healthMatrix[0].length;
            int maxSize = 0;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (healthMatrix[i][j] == 0) continue;

                    int maxSide = Math.min(rows - i, cols - j);

                    for (int side = 1; side <= maxSide; side++) {
                        if (isSquareHealthy(healthMatrix, i, j, side)) {
                            maxSize = Math.max(maxSize, side);
                        } else {
                            break;
                        }
                    }
                }
            }

            return maxSize;
        }

        private static boolean isSquareHealthy(int[][] matrix, int startRow, int startCol, int side) {
            for (int i = startRow; i < startRow + side; i++) {
                for (int j = startCol; j < startCol + side; j++) {
                    if (matrix[i][j] == 0) {
                        return false;
                    }
                }
            }
            return true;
        }

        /**
         * Find coordinates of largest healthy square
         */
        public static int[] findLargestHealthySquareCoords(int[][] healthMatrix) {
            if (healthMatrix == null || healthMatrix.length == 0) {
                return new int[]{-1, -1, 0};
            }

            int rows = healthMatrix.length;
            int cols = healthMatrix[0].length;
            int maxSize = 0;
            int topRow = -1, topCol = -1;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (healthMatrix[i][j] == 0) continue;

                    int maxSide = Math.min(rows - i, cols - j);

                    for (int side = 1; side <= maxSide; side++) {
                        if (isSquareHealthy(healthMatrix, i, j, side)) {
                            if (side > maxSize) {
                                maxSize = side;
                                topRow = i;
                                topCol = j;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }

            return new int[]{topRow, topCol, maxSize};
        }
    }

    /**
     * Part 3: Water Flow Path Tracer
     * Trace path rainwater takes from high to low elevation
     */
    public static class Part3_WaterFlowPath {
        private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        /**
         * Find path water takes flowing downhill
         * Time Complexity: O(rows * cols)
         */
        public static List<String> traceWaterFlow(int[][] elevationMatrix, int startRow, int startCol) {
            List<String> path = new ArrayList<>();
            if (elevationMatrix == null || elevationMatrix.length == 0) {
                return path;
            }

            int rows = elevationMatrix.length;
            int cols = elevationMatrix[0].length;
            Set<String> visited = new HashSet<>();

            dfsWaterFlow(elevationMatrix, startRow, startCol, visited, path, rows, cols);
            return path;
        }

        private static void dfsWaterFlow(int[][] elevation, int row, int col,
                                        Set<String> visited, List<String> path,
                                        int rows, int cols) {
            String key = row + "," + col;

            if (row < 0 || row >= rows || col < 0 || col >= cols) {
                return;
            }
            if (visited.contains(key)) {
                return;
            }

            visited.add(key);
            path.add(key);

            int currentHeight = elevation[row][col];
            int nextRow = row;
            int nextCol = col;
            int lowestHeight = currentHeight;

            for (int[] dir : DIRECTIONS) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                    continue;
                }

                int newHeight = elevation[newRow][newCol];
                if (newHeight < lowestHeight) {
                    lowestHeight = newHeight;
                    nextRow = newRow;
                    nextCol = newCol;
                }
            }

            if (lowestHeight < currentHeight) {
                dfsWaterFlow(elevation, nextRow, nextCol, visited, path, rows, cols);
            }
        }

        /**
         * Find all cells that water from given point would flow through
         */
        public static Set<String> getWaterflowCells(int[][] elevationMatrix, int startRow, int startCol) {
            List<String> path = traceWaterFlow(elevationMatrix, startRow, startCol);
            return new HashSet<>(path);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 28: Agricultural Drone Crop Health ===\n");

        System.out.println("Part 1: Health Threshold");
        List<Double> readings = Arrays.asList(65.0, 75.0, 80.0, 50.0, 85.0);
        List<Double> healthy = Part1_BugFix.filterHealthyReadings(readings);
        System.out.println("Healthy readings: " + healthy + "\n");

        System.out.println("Part 2: Largest Healthy Square");
        int[][] healthMatrix = {
            {1, 1, 1, 0},
            {1, 1, 1, 0},
            {1, 1, 1, 0},
            {0, 0, 0, 0}
        };
        int[] coords = Part2_LargestHealthySquare.findLargestHealthySquareCoords(healthMatrix);
        System.out.println("Largest square: top-left(" + coords[0] + "," + coords[1] + "), size=" + coords[2] + "\n");

        System.out.println("Part 3: Water Flow Path");
        int[][] elevationMatrix = {
            {5, 4, 3, 2},
            {6, 5, 4, 1},
            {7, 6, 5, 0},
            {8, 7, 6, 1}
        };
        List<String> waterPath = Part3_WaterFlowPath.traceWaterFlow(elevationMatrix, 0, 0);
        System.out.println("Water flow from (0,0): " + waterPath);
    }
}
