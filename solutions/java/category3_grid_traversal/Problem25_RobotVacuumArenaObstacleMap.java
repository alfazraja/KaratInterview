package category3_grid_traversal;

import java.util.*;

public class Problem25_RobotVacuumArenaObstacleMap {
    
    // Direction: 0=right, 1=down, 2=left, 3=up
    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
    private static final char[] DIR_NAMES = {'R', 'D', 'L', 'U'};
    
    /**
     * Part 1: Fix rotation matrix sign bug
     * Clockwise: direction = (direction + 1) % 4
     * Counter-clockwise: direction = (direction - 1 + 4) % 4
     */
    public static int rotateClockwise(int currentDir) {
        return (currentDir + 1) % 4;  // Correct sign
    }
    
    public static int rotateCounterClockwise(int currentDir) {
        return (currentDir - 1 + 4) % 4;  // Correct sign
    }
    
    /**
     * Part 2: Simulate vacuum path
     * Process commands: F=forward, L=turn left, R=turn right
     */
    public static Set<String> simulateVacuum(char[][] grid, int startRow, int startCol, String commands) {
        Set<String> cleaned = new HashSet<>();
        int row = startRow;
        int col = startCol;
        int direction = 0;  // Start facing right
        
        cleaned.add(row + "," + col);
        
        for (char cmd : commands.toCharArray()) {
            if (cmd == 'F') {
                // Move forward
                int newRow = row + DIRECTIONS[direction][0];
                int newCol = col + DIRECTIONS[direction][1];
                
                // Check bounds and obstacles
                if (newRow >= 0 && newRow < grid.length && 
                    newCol >= 0 && newCol < grid[0].length &&
                    grid[newRow][newCol] != '#') {
                    row = newRow;
                    col = newCol;
                    cleaned.add(row + "," + col);
                }
            } else if (cmd == 'L') {
                direction = rotateCounterClockwise(direction);
            } else if (cmd == 'R') {
                direction = rotateClockwise(direction);
            }
        }
        
        return cleaned;
    }
    
    /**
     * Part 3: Find unreachable floor coordinates
     * Use BFS from starting position
     */
    public static List<int[]> findUnreachableAreas(char[][] grid, int startRow, int startCol) {
        List<int[]> unreachable = new ArrayList<>();
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        
        // BFS to find all reachable cells
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int row = curr[0];
            int col = curr[1];
            
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                
                if (newRow >= 0 && newRow < grid.length && 
                    newCol >= 0 && newCol < grid[0].length &&
                    !visited[newRow][newCol] && grid[newRow][newCol] != '#') {
                    visited[newRow][newCol] = true;
                    queue.offer(new int[]{newRow, newCol});
                }
            }
        }
        
        // Find all unreachable floor coordinates
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != '#' && !visited[i][j]) {
                    unreachable.add(new int[]{i, j});
                }
            }
        }
        
        return unreachable;
    }
    
    public static void main(String[] args) {
        // Test Part 1
        System.out.println("Rotate CW from right(0): " + rotateClockwise(0));
        System.out.println("Rotate CCW from right(0): " + rotateCounterClockwise(0));
        
        // Test Part 2
        char[][] grid = {
            {'.', '.', '.', '.'},
            {'.', '#', '.', '.'},
            {'.', '.', '.', '.'},
            {'.', '.', '#', '.'}
        };
        Set<String> cleaned = simulateVacuum(grid, 0, 0, "FFRFFFRFF");
        System.out.println("Cells cleaned: " + cleaned.size());
        
        // Test Part 3
        List<int[]> unreachable = findUnreachableAreas(grid, 0, 0);
        System.out.println("Unreachable cells: " + unreachable.size());
        for (int[] cell : unreachable) {
            System.out.println("  [" + cell[0] + "," + cell[1] + "]");
        }
    }
}
