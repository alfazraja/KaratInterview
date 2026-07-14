package category3_grid_traversal;

import java.util.*;

public class Problem24_BattleshipBoardGameValidator {
    
    /**
     * Part 1: Fix case sensitivity bug
     * Enforce uniform uppercase string conversions
     */
    public static char normalizeCharacter(char c) {
        return Character.toUpperCase(c);
    }
    
    /**
     * Part 2: Find all valid ships
     * Ships must be straight horizontal or vertical lines
     */
    public static List<String> findValidShips(char[][] board) {
        List<String> ships = new ArrayList<>();
        boolean[][] visited = new boolean[10][10];
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                char normalized = normalizeCharacter(board[i][j]);
                if (normalized == 'S' && !visited[i][j]) {
                    // Try to find ship starting from this position
                    String ship = findShipSegment(board, visited, i, j);
                    if (!ship.isEmpty()) {
                        ships.add(ship);
                    }
                }
            }
        }
        
        return ships;
    }
    
    private static String findShipSegment(char[][] board, boolean[][] visited, int row, int col) {
        StringBuilder ship = new StringBuilder();
        ship.append("[").append(row).append(",").append(col).append("]");
        visited[row][col] = true;
        
        // Check if ship continues horizontally
        boolean isHorizontal = false;
        if (col + 1 < 10 && normalizeCharacter(board[row][col + 1]) == 'S') {
            isHorizontal = true;
        }
        
        // Check if ship continues vertically
        boolean isVertical = false;
        if (row + 1 < 10 && normalizeCharacter(board[row + 1][col]) == 'S') {
            isVertical = true;
        }
        
        if (isHorizontal) {
            // Extend horizontally
            int c = col + 1;
            while (c < 10 && normalizeCharacter(board[row][c]) == 'S' && !visited[row][c]) {
                ship.append("-[").append(row).append(",").append(c).append("]");
                visited[row][c] = true;
                c++;
            }
        } else if (isVertical) {
            // Extend vertically
            int r = row + 1;
            while (r < 10 && normalizeCharacter(board[r][col]) == 'S' && !visited[r][col]) {
                ship.append("-[").append(r).append(",").append(col).append("]");
                visited[r][col] = true;
                r++;
            }
        }
        
        return ship.toString();
    }
    
    /**
     * Part 3: Find best position to shoot
     * Highest probability of hitting a ship
     */
    public static int[] findBestShot(char[][] board, char[][] shotBoard) {
        int maxScore = -1;
        int[] bestCell = new int[]{-1, -1};
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (shotBoard[i][j] == '.') {  // Not yet shot
                    int score = calculateCellScore(board, i, j);
                    if (score > maxScore) {
                        maxScore = score;
                        bestCell = new int[]{i, j};
                    }
                }
            }
        }
        
        return bestCell;
    }
    
    private static int calculateCellScore(char[][] board, int row, int col) {
        int score = 0;
        
        // Check adjacent cells for ships
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (newRow >= 0 && newRow < 10 && newCol >= 0 && newCol < 10) {
                if (normalizeCharacter(board[newRow][newCol]) == 'S') {
                    score += 2;  // High priority if adjacent to ship
                }
            }
        }
        
        // Check if on water or ship
        if (normalizeCharacter(board[row][col]) == 'S') {
            score += 10;  // Highest priority - ship cell
        }
        
        return score;
    }
    
    public static void main(String[] args) {
        // Test Part 1
        System.out.println("Normalized 's': " + normalizeCharacter('s'));
        System.out.println("Normalized 'S': " + normalizeCharacter('S'));
        
        // Test Part 2
        char[][] board = new char[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = '.';
            }
        }
        // Add a ship
        board[0][0] = 'S';
        board[0][1] = 's';
        board[0][2] = 'S';
        board[2][3] = 'S';
        board[3][3] = 'S';
        
        List<String> ships = findValidShips(board);
        System.out.println("Ships found: " + ships.size());
        for (String ship : ships) {
            System.out.println("  " + ship);
        }
        
        // Test Part 3
        char[][] shotBoard = new char[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                shotBoard[i][j] = '.';
            }
        }
        int[] bestShot = findBestShot(board, shotBoard);
        System.out.println("Best shot: [" + bestShot[0] + "," + bestShot[1] + "]");
    }
}
