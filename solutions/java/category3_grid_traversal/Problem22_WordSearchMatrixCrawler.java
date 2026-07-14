package category3_grid_traversal;

import java.util.*;

public class Problem22_WordSearchMatrixCrawler {
    
    /**
     * Part 1: Fix boundary check bug
     * Must check bounds BEFORE accessing grid, not after
     */
    public static boolean isValidCell(int row, int col, int rows, int cols) {
        // Correct: check boundaries first
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return false;
        }
        return true;
    }
    
    /**
     * Part 2: Search for word using right/down movement only
     * Return true if word can be constructed from any starting position
     */
    public static boolean searchWord(char[][] grid, String target) {
        if (grid == null || grid.length == 0 || target == null || target.length() == 0) {
            return false;
        }
        
        int rows = grid.length;
        int cols = grid[0].length;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (dfs(grid, target, 0, i, j, new boolean[rows][cols])) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean dfs(char[][] grid, String target, int index, int row, int col, boolean[][] visited) {
        // Check bounds and validity
        if (!isValidCell(row, col, grid.length, grid[0].length)) {
            return false;
        }
        
        if (visited[row][col] || grid[row][col] != target.charAt(index)) {
            return false;
        }
        
        if (index == target.length() - 1) {
            return true;  // Found complete word
        }
        
        visited[row][col] = true;
        
        // Only move right and down (Part 2)
        boolean found = dfs(grid, target, index + 1, row, col + 1, visited) ||  // right
                       dfs(grid, target, index + 1, row + 1, col, visited);     // down
        
        visited[row][col] = false;
        return found;
    }
    
    /**
     * Part 3: Search for word using all 4 directions
     * Cannot reuse same cell in path
     */
    public static boolean searchWordAllDirections(char[][] grid, String target) {
        if (grid == null || grid.length == 0 || target == null || target.length() == 0) {
            return false;
        }
        
        int rows = grid.length;
        int cols = grid[0].length;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (dfsAllDirections(grid, target, 0, i, j, new boolean[rows][cols])) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean dfsAllDirections(char[][] grid, String target, int index, int row, int col, boolean[][] visited) {
        if (!isValidCell(row, col, grid.length, grid[0].length)) {
            return false;
        }
        
        if (visited[row][col] || grid[row][col] != target.charAt(index)) {
            return false;
        }
        
        if (index == target.length() - 1) {
            return true;
        }
        
        visited[row][col] = true;
        
        // All 4 directions: up, down, left, right
        boolean found = dfsAllDirections(grid, target, index + 1, row - 1, col, visited) ||  // up
                       dfsAllDirections(grid, target, index + 1, row + 1, col, visited) ||  // down
                       dfsAllDirections(grid, target, index + 1, row, col - 1, visited) ||  // left
                       dfsAllDirections(grid, target, index + 1, row, col + 1, visited);    // right
        
        visited[row][col] = false;
        return found;
    }
    
    public static void main(String[] args) {
        char[][] grid = {
            {'A', 'B', 'C'},
            {'D', 'E', 'F'},
            {'G', 'H', 'I'}
        };
        
        // Test Part 2
        String target1 = "ADE";
        System.out.println("Search for '" + target1 + "': " + searchWord(grid, target1));
        System.out.println("Search for 'ABC': " + searchWord(grid, "ABC"));
        
        // Test Part 3
        System.out.println("Search 4-dir for 'ADE': " + searchWordAllDirections(grid, "ADE"));
        System.out.println("Search 4-dir for 'EDAB': " + searchWordAllDirections(grid, "EDAB"));
    }
}
