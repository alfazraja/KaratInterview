package category3_grid_traversal;

import java.util.*;

/**
 * Problem 26: Minesweeper Board Engine
 * 
 * Three-part progression:
 * - Part 1: Fix proximity mine count (add grid boundary checks)
 * - Part 2: Update empty cells with adjacent mine counts
 * - Part 3: Implement chain reaction reveal for zero-mine cells
 */
public class Problem26_MinesweeperBoardEngine {

    /**
     * Part 1: Bug Fix - Boundary Checking
     * Issue: Crashes when evaluating edge cells due to out-of-bounds neighbor access
     * Solution: Add grid boundary constraints before accessing neighbors
     */
    public static class Part1_BugFix {
        private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };

        /**
         * Check if cell is within bounds
         */
        public static boolean isValidCell(int row, int col, int rows, int cols) {
            return row >= 0 && row < rows && col >= 0 && col < cols;
        }

        /**
         * Count adjacent mines for a cell with boundary checks
         */
        public static int countAdjacentMines(int[][] board, int row, int col) {
            int rows = board.length;
            int cols = board[0].length;
            int mineCount = 0;

            for (int[] dir : DIRECTIONS) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                // Check bounds FIRST
                if (!isValidCell(newRow, newCol, rows, cols)) {
                    continue;
                }

                if (board[newRow][newCol] == -1) {  // -1 represents mine
                    mineCount++;
                }
            }

            return mineCount;
        }
    }

    /**
     * Part 2: Update Board with Mine Counts
     * For each empty cell, count and store adjacent mines
     */
    public static class Part2_MineCounter {
        private static final int MINE = -1;
        private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };

        /**
         * Update board: fill empty cells with adjacent mine counts
         * Time Complexity: O(rows * cols * 8) = O(rows * cols)
         * Space Complexity: O(1)
         */
        public static void updateBoard(int[][] board) {
            if (board == null || board.length == 0) {
                return;
            }

            int rows = board.length;
            int cols = board[0].length;
            int[][] result = new int[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (board[i][j] == MINE) {
                        result[i][j] = MINE;
                    } else {
                        // Count adjacent mines
                        int count = 0;
                        for (int[] dir : DIRECTIONS) {
                            int newRow = i + dir[0];
                            int newCol = j + dir[1];

                            if (Part1_BugFix.isValidCell(newRow, newCol, rows, cols) &&
                                board[newRow][newCol] == MINE) {
                                count++;
                            }
                        }
                        result[i][j] = count;
                    }
                }
            }

            // Copy result back to board
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    board[i][j] = result[i][j];
                }
            }
        }

        /**
         * Get board state as formatted string for debugging
         */
        public static String boardToString(int[][] board) {
            StringBuilder sb = new StringBuilder();
            for (int[] row : board) {
                for (int cell : row) {
                    if (cell == MINE) {
                        sb.append("* ");
                    } else {
                        sb.append(cell).append(" ");
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Part 3: Chain Reaction Reveal
     * When clicking a cell with 0 adjacent mines, recursively reveal neighbors
     */
    public static class Part3_ChainReactionReveal {
        private static final int MINE = -1;
        private static final int UNREVEALED = -2;
        private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };

        /**
         * Reveal cell with chain reaction for zero-mine cells
         * Time Complexity: O(rows * cols) in worst case (reveal entire board)
         * Space Complexity: O(rows * cols) for visited set
         */
        public static void revealCell(int[][] board, int row, int col) {
            if (board == null || board.length == 0) {
                return;
            }

            int rows = board.length;
            int cols = board[0].length;
            Set<String> revealed = new HashSet<>();
            dfsReveal(board, row, col, revealed, rows, cols);
        }

        private static void dfsReveal(int[][] board, int row, int col,
                                     Set<String> revealed, int rows, int cols) {
            // Boundary check
            if (!Part1_BugFix.isValidCell(row, col, rows, cols)) {
                return;
            }

            String key = row + "," + col;
            if (revealed.contains(key)) {
                return;
            }

            // Stop if mine
            if (board[row][col] == MINE || board[row][col] == UNREVEALED) {
                return;
            }

            revealed.add(key);

            // If zero adjacent mines, recursively reveal neighbors
            if (board[row][col] == 0) {
                for (int[] dir : DIRECTIONS) {
                    int newRow = row + dir[0];
                    int newCol = col + dir[1];
                    dfsReveal(board, newRow, newCol, revealed, rows, cols);
                }
            }
        }

        /**
         * Get set of revealed cells as coordinate strings
         */
        public static Set<String> getRevealedCells(int[][] board, int row, int col) {
            if (board == null || board.length == 0) {
                return new HashSet<>();
            }

            int rows = board.length;
            int cols = board[0].length;
            Set<String> revealed = new HashSet<>();
            dfsReveal(board, row, col, revealed, rows, cols);
            return revealed;
        }
    }

    // Test methods
    public static void main(String[] args) {
        System.out.println("=== Problem 26: Minesweeper Board Engine ===\n");

        // Part 1 Test: Boundary checking
        System.out.println("Part 1: Boundary Checking");
        int[][] testBoard = {
            {-1, 0, 0},
            {0, 0, 0},
            {0, 0, -1}
        };
        int count = Part1_BugFix.countAdjacentMines(testBoard, 0, 0);
        System.out.println("Corner cell mine count (should be 0): " + count);
        count = Part1_BugFix.countAdjacentMines(testBoard, 1, 1);
        System.out.println("Center cell mine count (should be 2): " + count + "\n");

        // Part 2 Test: Update board
        System.out.println("Part 2: Update Board with Mine Counts");
        int[][] board = {
            {-1, 0, 0},
            {0, 0, 0},
            {0, 0, -1}
        };
        System.out.println("Before:");
        System.out.println(Part2_MineCounter.boardToString(board));
        Part2_MineCounter.updateBoard(board);
        System.out.println("After:");
        System.out.println(Part2_MineCounter.boardToString(board));

        // Part 3 Test: Chain reaction
        System.out.println("Part 3: Chain Reaction Reveal");
        int[][] board2 = {
            {-1, 0, 0},
            {0, 0, 0},
            {0, 0, -1}
        };
        Part2_MineCounter.updateBoard(board2);
        Set<String> revealed = Part3_ChainReactionReveal.getRevealedCells(board2, 0, 1);
        System.out.println("Cells revealed from (0,1): " + revealed);
    }
}
