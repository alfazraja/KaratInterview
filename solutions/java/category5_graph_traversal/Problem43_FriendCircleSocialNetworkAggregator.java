package category5_graph_traversal;

import java.util.*;

/**
 * Problem 43: Friend Circle Social Network Aggregator
 * 
 * Three-part progression:
 * - Part 1: Fix bidirectional friendship relationship
 * - Part 2: Count connected components (friend circles)
 * - Part 3: Find critical node bridge
 */
public class Problem43_FriendCircleSocialNetworkAggregator {
    
    /**
     * Part 1: Bug Fix - Ensure Bidirectional Relationships
     * Issue: If A is friends with B, B should also be friends with A
     */
    public static void validateFriendshipMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] == 1 && matrix[j][i] != 1) {
                    matrix[j][i] = 1;  // Ensure symmetry
                }
            }
        }
    }
    
    /**
     * Part 2: Connected Components Counter
     * Find total number of isolated friend circles
     */
    public static int countFriendCircles(int[][] matrix) {
        if (matrix == null || matrix.length == 0) return 0;
        
        boolean[] visited = new boolean[matrix.length];
        int circleCount = 0;
        
        for (int i = 0; i < matrix.length; i++) {
            if (!visited[i]) {
                dfs(i, matrix, visited);
                circleCount++;
            }
        }
        
        return circleCount;
    }
    
    private static void dfs(int person, int[][] matrix, boolean[] visited) {
        visited[person] = true;
        
        for (int i = 0; i < matrix[person].length; i++) {
            if (matrix[person][i] == 1 && !visited[i]) {
                dfs(i, matrix, visited);
            }
        }
    }
    
    /**
     * Part 3: Find Critical Node Bridge
     * Identify person whose removal splits largest friend group
     */
    public static int findCriticalBridge(int[][] matrix) {
        int n = matrix.length;
        int maxSplit = 0;
        int criticalPerson = -1;
        
        for (int person = 0; person < n; person++) {
            // Simulate removing this person
            boolean[] visited = new boolean[n];
            visited[person] = true;  // Mark as removed
            
            int componentCount = 0;
            for (int i = 0; i < n; i++) {
                if (!visited[i]) {
                    dfs(i, matrix, visited);
                    componentCount++;
                }
            }
            
            if (componentCount > maxSplit) {
                maxSplit = componentCount;
                criticalPerson = person;
            }
        }
        
        return criticalPerson;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 43: Friend Circle Social Network ===");
        
        int[][] matrix = {
            {1, 1, 0, 0},
            {1, 1, 1, 0},
            {0, 1, 1, 0},
            {0, 0, 0, 1}
        };
        
        validateFriendshipMatrix(matrix);
        System.out.println("Matrix validated");
        
        int circles = countFriendCircles(matrix);
        System.out.println("Number of friend circles: " + circles);
        
        int critical = findCriticalBridge(matrix);
        System.out.println("Critical bridge person: " + critical);
    }
}
