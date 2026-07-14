package category1_state_machines;

import java.util.*;

/**
 * Problem 5: Package Logistics Route Auditor
 * 
 * Three-part progression:
 * - Part 1: Fix parsing (replace hardcoded substrings with delimiter split)
 * - Part 2: Validate package routes against routing graph
 * - Part 3: Detect anomalous routes exceeding optimal path by 3+ stops
 */
public class Problem5_PackageLogisticsRouteAuditor {

    /**
     * Part 1: Bug Fix - String parsing with delimiter
     * Issue: Hardcoded substring positions break with varying city name lengths
     * Solution: Use " -> " delimiter to extract city names
     */
    public static class Part1_BugFix {
        public static class RouteEntry {
            public String sourceCity;
            public String destinationCity;
            public long timestamp;

            public RouteEntry(String sourceCity, String destinationCity, long timestamp) {
                this.sourceCity = sourceCity;
                this.destinationCity = destinationCity;
                this.timestamp = timestamp;
            }

            @Override
            public String toString() {
                return String.format("%s -> %s", sourceCity, destinationCity);
            }
        }

        /**
         * Parse hub transfer log using delimiter
         * Format: "City1 -> City2, timestamp"
         */
        public static RouteEntry parseTransferLog(String logLine) {
            if (logLine == null || logLine.isEmpty()) {
                throw new IllegalArgumentException("Invalid log line");
            }

            // Split by comma first
            String[] parts = logLine.split(",");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid format: " + logLine);
            }

            String routePart = parts[0].trim();
            long timestamp = Long.parseLong(parts[1].trim());

            // Split route by arrow
            String[] cities = routePart.split(" -> ");
            if (cities.length < 2) {
                throw new IllegalArgumentException("Invalid route format: " + routePart);
            }

            String sourceCity = cities[0].trim();
            String destinationCity = cities[1].trim();

            return new RouteEntry(sourceCity, destinationCity, timestamp);
        }

        /**
         * Parse multiple transfer logs
         */
        public static List<RouteEntry> parseTransferLogs(List<String> lines) {
            List<RouteEntry> routes = new ArrayList<>();
            for (String line : lines) {
                routes.add(parseTransferLog(line));
            }
            return routes;
        }
    }

    /**
     * Part 2: Route Validation - Check against routing graph
     * Ensure package follows legitimate paths
     */
    public static class Part2_RouteValidation {
        public static class PackageRoute {
            public String packageId;
            public List<String> routePath; // cities visited in order
            public long startTime;
            public long endTime;

            public PackageRoute(String packageId, List<String> routePath, long startTime, long endTime) {
                this.packageId = packageId;
                this.routePath = routePath;
                this.startTime = startTime;
                this.endTime = endTime;
            }
        }

        /**
         * Validate route against routing graph
         * All consecutive city pairs must exist in the graph
         */
        public static boolean isValidRoute(
            PackageRoute route,
            Map<String, Set<String>> routingGraph) {
            
            if (route.routePath.size() < 2) {
                return false;
            }

            // Check each consecutive pair
            for (int i = 0; i < route.routePath.size() - 1; i++) {
                String current = route.routePath.get(i);
                String next = route.routePath.get(i + 1);

                // Check if edge exists in graph
                if (!routingGraph.containsKey(current) || 
                    !routingGraph.get(current).contains(next)) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Find all invalid routes
         */
        public static List<String> findInvalidRoutes(
            List<PackageRoute> routes,
            Map<String, Set<String>> routingGraph) {
            
            List<String> invalidPackageIds = new ArrayList<>();
            for (PackageRoute route : routes) {
                if (!isValidRoute(route, routingGraph)) {
                    invalidPackageIds.add(route.packageId);
                }
            }
            return invalidPackageIds;
        }
    }

    /**
     * Part 3: Anomaly Detection - Routes deviating from optimal path
     * Flag packages exceeding optimal path by 3+ intermediate stops
     */
    public static class Part3_AnomalyDetection {
        public static class PathAnalysis {
            public String packageId;
            public List<String> actualPath;
            public List<String> optimalPath;
            public int excessStops;
            public boolean isAnomaly;

            public PathAnalysis(String packageId, List<String> actualPath, List<String> optimalPath) {
                this.packageId = packageId;
                this.actualPath = actualPath;
                this.optimalPath = optimalPath;
                this.excessStops = actualPath.size() - optimalPath.size();
                this.isAnomaly = excessStops >= 3; // 3+ extra stops
            }

            @Override
            public String toString() {
                return String.format("%s: Actual=%d, Optimal=%d, Excess=%d %s",
                    packageId, actualPath.size(), optimalPath.size(), excessStops,
                    isAnomaly ? "[ANOMALY]" : "");
            }
        }

        /**
         * BFS to find shortest path in routing graph
         */
        public static List<String> findShortestPath(
            String source,
            String destination,
            Map<String, Set<String>> graph) {
            
            if (!graph.containsKey(source) || !graph.containsKey(destination)) {
                return new ArrayList<>();
            }

            Queue<List<String>> queue = new LinkedList<>();
            Set<String> visited = new HashSet<>();
            
            queue.add(Arrays.asList(source));
            visited.add(source);

            while (!queue.isEmpty()) {
                List<String> path = queue.poll();
                String current = path.get(path.size() - 1);

                if (current.equals(destination)) {
                    return path;
                }

                for (String neighbor : graph.get(current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        List<String> newPath = new ArrayList<>(path);
                        newPath.add(neighbor);
                        queue.add(newPath);
                    }
                }
            }

            return new ArrayList<>(); // No path found
        }

        /**
         * Analyze package routes for anomalies
         */
        public static List<PathAnalysis> detectRouteAnomalies(
            List<Part2_RouteValidation.PackageRoute> routes,
            Map<String, Set<String>> routingGraph) {
            
            List<PathAnalysis> analyses = new ArrayList<>();

            for (Part2_RouteValidation.PackageRoute route : routes) {
                if (route.routePath.size() < 2) continue;

                String source = route.routePath.get(0);
                String destination = route.routePath.get(route.routePath.size() - 1);

                List<String> optimalPath = findShortestPath(source, destination, routingGraph);
                
                if (!optimalPath.isEmpty()) {
                    PathAnalysis analysis = new PathAnalysis(
                        route.packageId,
                        route.routePath,
                        optimalPath
                    );
                    analyses.add(analysis);
                }
            }

            return analyses;
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 5: Package Logistics Route Auditor ===");

        // Part 1: Test parsing
        System.out.println("\n--- Part 1: Transfer Log Parsing ---");
        List<String> logs = Arrays.asList(
            "NewYork -> Boston, 1000",
            "Los Angeles -> SanFrancisco, 1100",
            "Chicago -> Dallas, 1200"
        );
        List<Part1_BugFix.RouteEntry> entries = Part1_BugFix.parseTransferLogs(logs);
        System.out.println("Parsed routes:");
        for (Part1_BugFix.RouteEntry entry : entries) {
            System.out.println("  " + entry);
        }

        // Part 2: Test route validation
        System.out.println("\n--- Part 2: Route Validation ---");
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("A", new HashSet<>(Arrays.asList("B", "C")));
        graph.put("B", new HashSet<>(Arrays.asList("D", "E")));
        graph.put("C", new HashSet<>(Arrays.asList("D")));
        graph.put("D", new HashSet<>(Arrays.asList("E")));
        graph.put("E", new HashSet<>());

        List<Part2_RouteValidation.PackageRoute> routes = Arrays.asList(
            new Part2_RouteValidation.PackageRoute("pkg1", Arrays.asList("A", "B", "D", "E"), 1000, 2000),
            new Part2_RouteValidation.PackageRoute("pkg2", Arrays.asList("A", "X", "E"), 1000, 2000) // Invalid: A->X doesn't exist
        );
        List<String> invalid = Part2_RouteValidation.findInvalidRoutes(routes, graph);
        System.out.println("Invalid routes: " + invalid);

        // Part 3: Test anomaly detection
        System.out.println("\n--- Part 3: Anomaly Detection ---");
        List<Part3_AnomalyDetection.PathAnalysis> analyses = 
            Part3_AnomalyDetection.detectRouteAnomalies(routes, graph);
        System.out.println("Route anomalies:");
        for (Part3_AnomalyDetection.PathAnalysis analysis : analyses) {
            System.out.println("  " + analysis);
        }
    }
}
