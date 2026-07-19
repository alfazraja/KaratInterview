package category5_graph_traversal;

import java.util.*;

/**
 * Problem 47: Flight Connection Route Finder
 * 
 * Three-part progression:
 * - Part 1: Validate chronological departure/arrival
 * - Part 2: Find valid routing path using BFS
 * - Part 3: Optimize for shortest layover time
 */
public class Problem47_FlightConnectionRouteFinder {
    
    static class Flight {
        String source;
        String destination;
        int departureTime;
        int arrivalTime;
        
        Flight(String src, String dst, int dep, int arr) {
            this.source = src;
            this.destination = dst;
            this.departureTime = dep;
            this.arrivalTime = arr;
        }
    }
    
    /**
     * Part 1: Validate Flight Chronology
     * Ensure departure occurs before arrival
     */
    public static List<Flight> validateFlights(String[][] flightData) {
        List<Flight> validFlights = new ArrayList<>();
        
        for (String[] data : flightData) {
            String src = data[0];
            String dst = data[1];
            int dep = Integer.parseInt(data[2]);
            int arr = Integer.parseInt(data[3]);
            
            if (dep < arr) {
                validFlights.add(new Flight(src, dst, dep, arr));
            }
        }
        
        return validFlights;
    }
    
    /**
     * Part 2: Find Valid Route using BFS
     */
    public static List<Flight> findRoute(String start, String destination,
                                         List<Flight> flights) {
        Map<String, List<Flight>> graph = new HashMap<>();
        
        for (Flight flight : flights) {
            graph.computeIfAbsent(flight.source, k -> new ArrayList<>()).add(flight);
        }
        
        Queue<List<Flight>> queue = new LinkedList<>();
        queue.offer(new ArrayList<>());
        
        while (!queue.isEmpty()) {
            List<Flight> path = queue.poll();
            
            String current = path.isEmpty() ? start : 
                           path.get(path.size() - 1).destination;
            
            if (current.equals(destination)) {
                return path;
            }
            
            int latestArrival = path.isEmpty() ? 0 : 
                              path.get(path.size() - 1).arrivalTime;
            
            for (Flight flight : graph.getOrDefault(current, new ArrayList<>())) {
                if (flight.departureTime >= latestArrival) {
                    List<Flight> newPath = new ArrayList<>(path);
                    newPath.add(flight);
                    queue.offer(newPath);
                }
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Part 3: Shortest Layover Optimizer
     */
    public static List<Flight> findShortestLayoverRoute(String start, String destination,
                                                         List<Flight> flights) {
        Map<String, List<Flight>> graph = new HashMap<>();
        
        for (Flight flight : flights) {
            graph.computeIfAbsent(flight.source, k -> new ArrayList<>()).add(flight);
        }
        
        List<Flight> bestRoute = new ArrayList<>();
        int minTotalTime = Integer.MAX_VALUE;
        
        dfsShortestLayover(start, destination, graph, new ArrayList<>(), 
                          bestRoute, 0, minTotalTime);
        
        return bestRoute;
    }
    
    private static int dfsShortestLayover(String current, String destination,
                                          Map<String, List<Flight>> graph,
                                          List<Flight> path, List<Flight> bestRoute,
                                          int totalTime, int minTime) {
        if (current.equals(destination)) {
            if (totalTime < minTime) {
                bestRoute.clear();
                bestRoute.addAll(path);
                return totalTime;
            }
            return minTime;
        }
        
        int latestArrival = path.isEmpty() ? 0 : path.get(path.size() - 1).arrivalTime;
        
        for (Flight flight : graph.getOrDefault(current, new ArrayList<>())) {
            if (flight.departureTime >= latestArrival) {
                int layoverTime = flight.departureTime - latestArrival;
                path.add(flight);
                int newTotal = totalTime + (flight.arrivalTime - flight.departureTime) + layoverTime;
                minTime = dfsShortestLayover(flight.destination, destination, graph,
                                           path, bestRoute, newTotal, minTime);
                path.remove(path.size() - 1);
            }
        }
        
        return minTime;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 47: Flight Connection Route Finder ===");
        
        String[][] flightData = {
            {"NYC", "LA", "800", "1200"},
            {"LA", "SF", "1400", "1600"},
            {"NYC", "SF", "900", "1400"}
        };
        
        List<Flight> flights = validateFlights(flightData);
        System.out.println("Valid flights: " + flights.size());
        
        List<Flight> route = findRoute("NYC", "SF", flights);
        System.out.println("Route found with " + route.size() + " flights");
    }
}
