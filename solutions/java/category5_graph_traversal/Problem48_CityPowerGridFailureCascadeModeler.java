package category5_graph_traversal;

import java.util.*;

/**
 * Problem 48: City Power Grid Failure Cascade Modeler
 * 
 * Three-part progression:
 * - Part 1: Fix shallow copy issues during iteration
 * - Part 2: Verify power distribution paths
 * - Part 3: Calculate cascade vulnerability
 */
public class Problem48_CityPowerGridFailureCascadeModeler {
    
    /**
     * Part 1: Build Grid with Safe Copying
     * Use deep copy to avoid modification during iteration
     */
    public static Map<String, List<String>> buildPowerGrid(String[][] connections) {
        Map<String, List<String>> grid = new HashMap<>();
        
        for (String[] connection : connections) {
            String source = connection[0];
            String target = connection[1];
            
            grid.computeIfAbsent(source, k -> new ArrayList<>()).add(target);
            grid.computeIfAbsent(target, k -> new ArrayList<>());
        }
        
        return grid;
    }
    
    /**
     * Part 2: Verify Distribution Path
     * Check if neighborhood station receives power
     */
    public static boolean isNeighborhoodPowered(String source, String target,
                                                Map<String, List<String>> grid) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        
        queue.offer(source);
        visited.add(source);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(target)) {
                return true;
            }
            
            for (String neighbor : grid.getOrDefault(current, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        
        return false;
    }
    
    /**
     * Part 3: Calculate Cascade Impact
     * How many nodes lose power if one substation fails
     */
    public static int calculateCascadeImpact(String failedNode,
                                             Map<String, List<String>> grid) {
        int impactCount = 0;
        
        for (String potentialSource : grid.keySet()) {
            if (potentialSource.equals(failedNode)) continue;
            
            int poweredBeforeFailure = 0;
            int poweredAfterFailure = 0;
            
            // Count powered nodes before failure
            Map<String, List<String>> tempGrid = new HashMap<>(grid);
            for (String node : grid.keySet()) {
                if (isNeighborhoodPowered(potentialSource, node, tempGrid)) {
                    poweredBeforeFailure++;
                }
            }
            
            // Count powered nodes after failure
            Map<String, List<String>> failedGrid = new HashMap<>();
            for (String node : grid.keySet()) {
                if (!node.equals(failedNode)) {
                    List<String> connections = new ArrayList<>(grid.get(node));
                    connections.remove(failedNode);
                    failedGrid.put(node, connections);
                }
            }
            
            for (String node : failedGrid.keySet()) {
                if (isNeighborhoodPowered(potentialSource, node, failedGrid)) {
                    poweredAfterFailure++;
                }
            }
            
            impactCount += (poweredBeforeFailure - poweredAfterFailure);
        }
        
        return impactCount;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 48: Power Grid Cascade Modeler ===");
        
        String[][] connections = {
            {"Plant_A", "Substation_1"},
            {"Substation_1", "Neighborhood_1"},
            {"Substation_1", "Neighborhood_2"},
            {"Plant_A", "Substation_2"},
            {"Substation_2", "Neighborhood_3"}
        };
        
        Map<String, List<String>> grid = buildPowerGrid(connections);
        System.out.println("Power grid built");
        
        boolean powered = isNeighborhoodPowered("Plant_A", "Neighborhood_1", grid);
        System.out.println("Neighborhood_1 powered: " + powered);
        
        int impact = calculateCascadeImpact("Substation_1", grid);
        System.out.println("Cascade impact if Substation_1 fails: " + impact);
    }
}
