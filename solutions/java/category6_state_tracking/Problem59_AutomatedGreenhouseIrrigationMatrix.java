package category6_state_tracking;

import java.util.*;

/**
 * Problem 59: Automated Greenhouse Irrigation Matrix
 * 
 * Three-part progression:
 * - Part 1: Fix inverted danger alert logic
 * - Part 2: Find connected arid patches using 2D traversal
 * - Part 3: Calculate optimal pipe routing
 */
public class Problem59_AutomatedGreenhouseIrrigationMatrix {
    
    /**
     * Part 1: Bug Fix - Correct Soil Moisture Logic
     * Fix inverted danger alerts by correcting inequalities
     */
    public static boolean isSoilDry(double moisturePercentage, double safetyThreshold) {
        // Correct: moisture BELOW threshold is dangerous
        return moisturePercentage < safetyThreshold;
    }
    
    public static boolean needsIrrigation(double moisturePercentage, double minimumThreshold) {
        // Corrected logic
        return moisturePercentage < minimumThreshold;
    }
    
    /**
     * Part 2: Find Arid Patches (Connected Components)
     * Identify groups of adjacent dry cells
     */
    public static List<List<int[]>> findAridPatches(double[][] moistureGrid, 
                                                     double safetyThreshold) {
        List<List<int[]>> patches = new ArrayList<>();
        boolean[][] visited = new boolean[moistureGrid.length][moistureGrid[0].length];
        
        for (int i = 0; i < moistureGrid.length; i++) {
            for (int j = 0; j < moistureGrid[0].length; j++) {
                if (!visited[i][j] && isSoilDry(moistureGrid[i][j], safetyThreshold)) {
                    List<int[]> patch = new ArrayList<>();
                    dfsFindPatch(i, j, moistureGrid, visited, patch, safetyThreshold);
                    if (!patch.isEmpty()) {
                        patches.add(patch);
                    }
                }
            }
        }
        
        return patches;
    }
    
    private static void dfsFindPatch(int row, int col, double[][] grid,
                                     boolean[][] visited, List<int[]> patch,
                                     double threshold) {
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length ||
            visited[row][col] || !isSoilDry(grid[row][col], threshold)) {
            return;
        }
        
        visited[row][col] = true;
        patch.add(new int[]{row, col});
        
        // Check all 4 directions
        dfsFindPatch(row - 1, col, grid, visited, patch, threshold);
        dfsFindPatch(row + 1, col, grid, visited, patch, threshold);
        dfsFindPatch(row, col - 1, grid, visited, patch, threshold);
        dfsFindPatch(row, col + 1, grid, visited, patch, threshold);
    }
    
    /**
     * Part 3: Optimal Pipe Routing
     * Find shortest path from valve to all arid zones
     */
    public static int calculateMinPipeLength(int[] valvePosition,
                                             List<int[]> aridCells) {
        if (aridCells.isEmpty()) return 0;
        
        // BFS to find minimum distance
        Queue<int[]> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, Integer> distances = new HashMap<>();
        
        queue.offer(valvePosition);
        visited.add(valvePosition[0] + "," + valvePosition[1]);
        distances.put(valvePosition[0] + "," + valvePosition[1], 0);
        
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int totalDistance = 0;
        Set<String> aridSet = new HashSet<>();
        for (int[] cell : aridCells) {
            aridSet.add(cell[0] + "," + cell[1]);
        }
        
        while (!queue.isEmpty() && !aridSet.isEmpty()) {
            int[] current = queue.poll();
            int distance = distances.get(current[0] + "," + current[1]);
            
            String cellKey = current[0] + "," + current[1];
            if (aridSet.contains(cellKey)) {
                totalDistance += distance;
                aridSet.remove(cellKey);
            }
            
            for (int[] dir : directions) {
                int newRow = current[0] + dir[0];
                int newCol = current[1] + dir[1];
                String newKey = newRow + "," + newCol;
                
                if (!visited.contains(newKey)) {
                    visited.add(newKey);
                    distances.put(newKey, distance + 1);
                    queue.offer(new int[]{newRow, newCol});
                }
            }
        }
        
        return totalDistance;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 59: Automated Greenhouse Irrigation ===");
        
        // Test Part 1
        System.out.println("Soil at 30%: needs irrigation? " + needsIrrigation(30, 40));
        System.out.println("Soil at 50%: needs irrigation? " + needsIrrigation(50, 40));
        
        // Test Part 2
        double[][] grid = {
            {30, 35, 40, 45},
            {25, 20, 35, 40},
            {40, 25, 30, 50}
        };
        
        List<List<int[]>> patches = findAridPatches(grid, 40);
        System.out.println("Arid patches found: " + patches.size());
        
        // Test Part 3
        int[] valve = {0, 0};
        List<int[]> aridCells = Arrays.asList(
            new int[]{1, 1},
            new int[]{2, 1}
        );
        int pipeLength = calculateMinPipeLength(valve, aridCells);
        System.out.println("Minimum pipe length: " + pipeLength);
    }
}
