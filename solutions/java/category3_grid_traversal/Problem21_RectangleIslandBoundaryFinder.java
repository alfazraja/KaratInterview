package category3_grid_traversal;

import java.util.*;

public class Problem21_RectangleIslandBoundaryFinder {
    
    /**
     * Part 1: Fix variable naming bug
     * Row loop uses i, column loop also uses i (should be j)
     */
    public static int countBlackPixels(int[][] grid) {
        int count = 0;
        // Fixed: use i for rows, j for columns
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == 1) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Part 2: Find bounding box of black rectangle
     * Return top-left and bottom-right coordinates
     */
    public static int[] findRectangleBounds(int[][] grid) {
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == 1) {
                    minRow = Math.min(minRow, i);
                    maxRow = Math.max(maxRow, i);
                    minCol = Math.min(minCol, j);
                    maxCol = Math.max(maxCol, j);
                }
            }
        }
        
        if (minRow == Integer.MAX_VALUE) {
            return new int[]{-1, -1, -1, -1};  // No rectangle found
        }
        return new int[]{minRow, minCol, maxRow, maxCol};
    }
    
    /**
     * Part 3: Find all distinct black rectangles
     * Use DFS/BFS to identify separate connected components
     */
    public static List<int[]> findAllRectangles(int[][] grid) {
        List<int[]> rectangles = new ArrayList<>();
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == 1 && !visited[i][j]) {
                    // Found new component - find its bounds
                    int[] bounds = findComponentBounds(grid, visited, i, j);
                    rectangles.add(bounds);
                }
            }
        }
        return rectangles;
    }
    
    private static int[] findComponentBounds(int[][] grid, boolean[][] visited, int startRow, int startCol) {
        int minRow = startRow, maxRow = startRow;
        int minCol = startCol, maxCol = startCol;
        
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int row = curr[0], col = curr[1];
            
            minRow = Math.min(minRow, row);
            maxRow = Math.max(maxRow, row);
            minCol = Math.min(minCol, col);
            maxCol = Math.max(maxCol, col);
            
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                
                if (newRow >= 0 && newRow < grid.length && 
                    newCol >= 0 && newCol < grid[0].length &&
                    grid[newRow][newCol] == 1 && !visited[newRow][newCol]) {
                    visited[newRow][newCol] = true;
                    queue.offer(new int[]{newRow, newCol});
                }
            }
        }
        
        return new int[]{minRow, minCol, maxRow, maxCol};
    }
    
    public static void main(String[] args) {
        int[][] grid = {
            {0, 0, 1, 1, 0},
            {0, 1, 1, 1, 0},
            {0, 1, 1, 1, 0},
            {0, 0, 0, 0, 0}
        };
        
        // Test Part 1
        System.out.println("Black pixel count: " + countBlackPixels(grid));
        
        // Test Part 2
        int[] bounds = findRectangleBounds(grid);
        System.out.println("Rectangle bounds: [" + bounds[0] + "," + bounds[1] + "] to [" + bounds[2] + "," + bounds[3] + "]");
        
        // Test Part 3
        List<int[]> allRectangles = findAllRectangles(grid);
        System.out.println("All rectangles: " + allRectangles.size());
    }
}
