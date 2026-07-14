package category3_grid_traversal;

import java.util.*;

public class Problem23_TreasureHuntGridPathValidation {
    
    /**
     * Part 1: Fix diagonal movement bug
     * Only allow horizontal/vertical moves, not diagonal
     * Check axis movements independently
     */
    public static boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Only horizontal or vertical movement (Manhattan distance = 1)
        int rowDiff = Math.abs(fromRow - toRow);
        int colDiff = Math.abs(fromCol - toCol);
        
        // Valid if moving in ONE direction only
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);
    }
    
    /**
     * Part 2: BFS to verify path exists from start to end
     * Walls block movement
     */
    public static boolean pathExists(int rows, int cols, int[] start, int[] end, Set<String> walls) {
        if (Arrays.equals(start, end)) return true;
        
        Queue<int[]> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        
        queue.offer(start);
        visited.add(start[0] + "," + start[1]);
        
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            
            for (int[] dir : directions) {
                int newRow = curr[0] + dir[0];
                int newCol = curr[1] + dir[1];
                
                // Check bounds
                if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                    continue;
                }
                
                String coord = newRow + "," + newCol;
                
                // Check if wall or visited
                if (walls.contains(coord) || visited.contains(coord)) {
                    continue;
                }
                
                if (newRow == end[0] && newCol == end[1]) {
                    return true;
                }
                
                visited.add(coord);
                queue.offer(new int[]{newRow, newCol});
            }
        }
        
        return false;
    }
    
    /**
     * Part 3: Find shortest path collecting all coins
     * Return minimum path length
     */
    public static int shortestPathWithCoins(int rows, int cols, int[] start, int[] end, List<int[]> coins) {
        if (coins.isEmpty()) {
            return bfsDistance(rows, cols, start, end, new HashSet<>());
        }
        
        // Use dynamic programming with bitmask for TSP variant
        int numCoins = coins.size();
        int n = numCoins + 2;  // +2 for start and end
        
        // Build distance matrix
        int[][] dist = new int[n][n];
        List<int[]> points = new ArrayList<>();
        points.add(start);
        points.addAll(coins);
        points.add(end);
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    dist[i][j] = bfsDistance(rows, cols, points.get(i), points.get(j), new HashSet<>());
                }
            }
        }
        
        // DP with bitmask
        int[][] dp = new int[1 << numCoins][n];
        for (int[] row : dp) Arrays.fill(row, Integer.MAX_VALUE);
        
        dp[0][0] = 0;  // Start at position 0 with no coins collected
        
        for (int mask = 0; mask < (1 << numCoins); mask++) {
            for (int u = 0; u < n; u++) {
                if (dp[mask][u] == Integer.MAX_VALUE) continue;
                
                // Try going to each coin
                for (int coin = 0; coin < numCoins; coin++) {
                    if ((mask & (1 << coin)) == 0) {  // Not yet collected
                        int newMask = mask | (1 << coin);
                        int v = coin + 1;  // Coin position in points array
                        dp[newMask][v] = Math.min(dp[newMask][v], dp[mask][u] + dist[u][v]);
                    }
                }
                
                // Try going to end
                if (mask == (1 << numCoins) - 1) {  // All coins collected
                    dp[mask][n-1] = Math.min(dp[mask][n-1], dp[mask][u] + dist[u][n-1]);
                }
            }
        }
        
        return dp[(1 << numCoins) - 1][n - 1];
    }
    
    private static int bfsDistance(int rows, int cols, int[] start, int[] end, Set<String> walls) {
        if (Arrays.equals(start, end)) return 0;
        
        Queue<int[]> queue = new LinkedList<>();
        Map<String, Integer> dist = new HashMap<>();
        
        queue.offer(start);
        dist.put(start[0] + "," + start[1], 0);
        
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int currDist = dist.get(curr[0] + "," + curr[1]);
            
            for (int[] dir : directions) {
                int newRow = curr[0] + dir[0];
                int newCol = curr[1] + dir[1];
                
                if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) continue;
                
                String coord = newRow + "," + newCol;
                if (walls.contains(coord) || dist.containsKey(coord)) continue;
                
                if (newRow == end[0] && newCol == end[1]) {
                    return currDist + 1;
                }
                
                dist.put(coord, currDist + 1);
                queue.offer(new int[]{newRow, newCol});
            }
        }
        
        return Integer.MAX_VALUE;  // No path
    }
    
    public static void main(String[] args) {
        // Test Part 1
        System.out.println("Valid move right: " + isValidMove(0, 0, 0, 1));
        System.out.println("Valid move down: " + isValidMove(0, 0, 1, 0));
        System.out.println("Invalid diagonal: " + isValidMove(0, 0, 1, 1));
        
        // Test Part 2
        Set<String> walls = new HashSet<>();
        boolean exists = pathExists(5, 5, new int[]{0, 0}, new int[]{4, 4}, walls);
        System.out.println("Path exists: " + exists);
        
        // Test Part 3
        List<int[]> coins = Arrays.asList(new int[]{1, 1}, new int[]{3, 3});
        int shortestPath = shortestPathWithCoins(5, 5, new int[]{0, 0}, new int[]{4, 4}, coins);
        System.out.println("Shortest path with coins: " + shortestPath);
    }
}
