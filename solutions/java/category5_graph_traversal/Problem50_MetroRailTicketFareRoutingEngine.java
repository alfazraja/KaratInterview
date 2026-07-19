package category5_graph_traversal;

import java.util.*;

/**
 * Problem 50: Metro Rail Ticket Fare Routing Engine
 * 
 * Three-part progression:
 * - Part 1: Fix double-charge bug at transfer points
 * - Part 2: Find minimum station hops using BFS
 * - Part 3: Calculate zone-based fares
 */
public class Problem50_MetroRailTicketFareRoutingEngine {
    
    static class Station {
        String name;
        int zone;
        
        Station(String name, int zone) {
            this.name = name;
            this.zone = zone;
        }
    }
    
    /**
     * Part 1: Build Metro Network Correctly
     * Avoid duplicate charging at transfer stations
     */
    public static Map<String, List<String>> buildMetroNetwork(String[][] railLines) {
        Map<String, List<String>> network = new HashMap<>();
        
        for (String[] line : railLines) {
            String station1 = line[0];
            String station2 = line[1];
            
            network.computeIfAbsent(station1, k -> new ArrayList<>()).add(station2);
            network.computeIfAbsent(station2, k -> new ArrayList<>()).add(station1);
        }
        
        return network;
    }
    
    /**
     * Part 2: Find Minimum Hops using BFS
     */
    public static int findMinimumHops(String source, String destination,
                                      Map<String, List<String>> network) {
        if (source.equals(destination)) return 0;
        
        Queue<String> queue = new LinkedList<>();
        Map<String, Integer> distance = new HashMap<>();
        
        queue.offer(source);
        distance.put(source, 0);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            for (String neighbor : network.getOrDefault(current, new ArrayList<>())) {
                if (!distance.containsKey(neighbor)) {
                    distance.put(neighbor, distance.get(current) + 1);
                    queue.offer(neighbor);
                    
                    if (neighbor.equals(destination)) {
                        return distance.get(neighbor);
                    }
                }
            }
        }
        
        return -1;  // No path found
    }
    
    /**
     * Part 3: Calculate Zone-Based Fare
     * Fare scales based on total number of zones crossed
     */
    public static int calculateFare(String source, String destination,
                                    Map<String, List<String>> network,
                                    Map<String, Station> stationMap) {
        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        
        queue.offer(source);
        parent.put(source, null);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(destination)) {
                // Reconstruct path and calculate zone difference
                List<String> path = new ArrayList<>();
                String node = destination;
                while (node != null) {
                    path.add(node);
                    node = parent.get(node);
                }
                
                int startZone = stationMap.get(source).zone;
                int endZone = stationMap.get(destination).zone;
                int zoneDifference = Math.abs(endZone - startZone);
                
                // Base fare + per-zone surcharge
                return 100 + (zoneDifference * 50);
            }
            
            for (String neighbor : network.getOrDefault(current, new ArrayList<>())) {
                if (!parent.containsKey(neighbor)) {
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
        
        return -1;  // No path found
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 50: Metro Rail Fare Routing ===");
        
        String[][] railLines = {
            {"Central", "West"},
            {"West", "North"},
            {"Central", "East"},
            {"East", "South"}
        };
        
        Map<String, List<String>> network = buildMetroNetwork(railLines);
        System.out.println("Metro network built");
        
        int hops = findMinimumHops("West", "South", network);
        System.out.println("Minimum hops from West to South: " + hops);
        
        Map<String, Station> stationMap = new HashMap<>();
        stationMap.put("Central", new Station("Central", 1));
        stationMap.put("West", new Station("West", 1));
        stationMap.put("East", new Station("East", 2));
        stationMap.put("North", new Station("North", 2));
        stationMap.put("South", new Station("South", 3));
        
        int fare = calculateFare("West", "South", network, stationMap);
        System.out.println("Fare from West to South: " + fare);
    }
}
