package category3_grid_traversal;

import java.util.*;

/**
 * Problem 30: Castle Defense Wall Vision Map
 * 
 * Three-part progression:
 * - Part 1: Fix line-of-sight calculation using precise float slopes
 * - Part 2: Calculate visible cells from a tower using ray casting
 * - Part 3: Optimize tower placement to minimize blind spots
 */
public class Problem30_CastleDefenseWallVision {

    /**
     * Part 1: Bug Fix - Precise Line-of-Sight
     * Issue: Truncating float slopes to integers miscalculates obstacle blocking
     * Solution: Use floating-point precision for accurate line-of-sight
     */
    public static class Part1_BugFix {
        /**
         * Check if obstacle blocks line of sight using precise slopes
         */
        public static boolean isLineOfSightBlocked(int towerRow, int towerCol,
                                                   int targetRow, int targetCol,
                                                   char[][] grid) {
            int rows = grid.length;
            int cols = grid[0].length;

            int dr = targetRow - towerRow;
            int dc = targetCol - towerCol;

            int steps = Math.max(Math.abs(dr), Math.abs(dc));
            if (steps == 0) return false;

            double stepRow = (double) dr / steps;
            double stepCol = (double) dc / steps;

            double row = towerRow;
            double col = towerCol;

            for (int i = 1; i < steps; i++) {
                row += stepRow;
                col += stepCol;

                int r = Math.round((float) row);
                int c = Math.round((float) col);

                if (r < 0 || r >= rows || c < 0 || c >= cols) {
                    continue;
                }

                if (grid[r][c] == '#') {
                    return true;
                }
            }

            return false;
        }

        /**
         * Calculate exact distance with floating-point precision
         */
        public static double preciseDistance(int r1, int c1, int r2, int c2) {
            double dr = r1 - r2;
            double dc = c1 - c2;
            return Math.sqrt(dr * dr + dc * dc);
        }
    }

    /**
     * Part 2: Calculate Visible Cells
     * Determine all cells visible from tower location
     */
    public static class Part2_VisibilityCalculation {
        /**
         * Find all visible cells from tower
         * Time Complexity: O(rows * cols)
         */
        public static Set<String> getVisibleCells(char[][] grid, int towerRow, int towerCol) {
            Set<String> visible = new HashSet<>();

            if (grid == null || grid.length == 0) {
                return visible;
            }

            int rows = grid.length;
            int cols = grid[0].length;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (grid[i][j] == '#') {
                        continue;
                    }
                    if (i == towerRow && j == towerCol) {
                        continue;
                    }

                    if (!Part1_BugFix.isLineOfSightBlocked(towerRow, towerCol, i, j, grid)) {
                        visible.add(i + "," + j);
                    }
                }
            }

            return visible;
        }

        /**
         * Count visible floor cells from tower
         */
        public static int countVisibleCells(char[][] grid, int towerRow, int towerCol) {
            return getVisibleCells(grid, towerRow, towerCol).size();
        }
    }

    /**
     * Part 3: Optimal Tower Placement
     * Find best locations to minimize blind spots
     */
    public static class Part3_OptimalPlacement {
        /**
         * Calculate blind spots from tower position
         */
        public static Set<String> getBlindSpots(char[][] grid, int towerRow, int towerCol) {
            Set<String> blindSpots = new HashSet<>();

            if (grid == null || grid.length == 0) {
                return blindSpots;
            }

            Set<String> visible = Part2_VisibilityCalculation.getVisibleCells(grid, towerRow, towerCol);
            int rows = grid.length;
            int cols = grid[0].length;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (grid[i][j] != '#' && !visible.contains(i + "," + j)) {
                        blindSpots.add(i + "," + j);
                    }
                }
            }

            return blindSpots;
        }

        /**
         * Find optimal tower positions to minimize blind spots
         */
        public static List<String> findOptimalTowerLocations(char[][] grid, int numTowers) {
            List<String> bestPositions = new ArrayList<>();

            if (grid == null || grid.length == 0 || numTowers <= 0) {
                return bestPositions;
            }

            int rows = grid.length;
            int cols = grid[0].length;
            Set<String> fullyVisible = new HashSet<>();

            for (int tower = 0; tower < numTowers; tower++) {
                int bestRow = -1, bestCol = -1;
                int maxNewCoverage = 0;

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        if (grid[i][j] == '#') continue;
                        if (bestPositions.contains(i + "," + j)) continue;

                        Set<String> visible = Part2_VisibilityCalculation.getVisibleCells(grid, i, j);
                        int newCoverage = 0;

                        for (String cell : visible) {
                            if (!fullyVisible.contains(cell)) {
                                newCoverage++;
                            }
                        }

                        if (newCoverage > maxNewCoverage) {
                            maxNewCoverage = newCoverage;
                            bestRow = i;
                            bestCol = j;
                        }
                    }
                }

                if (bestRow == -1) break;

                String position = bestRow + "," + bestCol;
                bestPositions.add(position);
                fullyVisible.addAll(Part2_VisibilityCalculation.getVisibleCells(grid, bestRow, bestCol));
            }

            return bestPositions;
        }

        /**
         * Calculate coverage statistics for tower placement
         */
        public static void analyzePlacement(char[][] grid, List<String> towers) {
            Set<String> totalVisible = new HashSet<>();
            Set<String> blindSpots = new HashSet<>();

            int rows = grid.length;
            int cols = grid[0].length;

            for (String tower : towers) {
                String[] parts = tower.split(",");
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                totalVisible.addAll(Part2_VisibilityCalculation.getVisibleCells(grid, r, c));
            }

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (grid[i][j] != '#' && !totalVisible.contains(i + "," + j)) {
                        blindSpots.add(i + "," + j);
                    }
                }
            }

            System.out.println("Total towers: " + towers.size());
            System.out.println("Visible cells: " + totalVisible.size());
            System.out.println("Blind spots: " + blindSpots.size());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 30: Castle Defense Wall Vision ===\n");

        char[][] castle = {
            {'.', '.', '#', '.', '.'},
            {'.', '#', '#', '#', '.'},
            {'T', '.', '.', '.', '.'},
            {'.', '#', '#', '#', '.'},
            {'.', '.', '#', '.', '.'}
        };

        System.out.println("Part 1: Line-of-Sight Calculation");
        boolean blocked = Part1_BugFix.isLineOfSightBlocked(2, 0, 2, 4, castle);
        System.out.println("Line blocked from (2,0) to (2,4): " + blocked + "\n");

        System.out.println("Part 2: Visible Cells from Tower");
        Set<String> visible = Part2_VisibilityCalculation.getVisibleCells(castle, 2, 0);
        System.out.println("Visible cells from (2,0): " + visible);
        System.out.println("Count: " + visible.size() + "\n");

        System.out.println("Part 3: Optimal Tower Placement");
        List<String> optimalTowers = Part3_OptimalPlacement.findOptimalTowerLocations(castle, 2);
        System.out.println("Optimal tower positions: " + optimalTowers);
        Part3_OptimalPlacement.analyzePlacement(castle, optimalTowers);
    }
}
