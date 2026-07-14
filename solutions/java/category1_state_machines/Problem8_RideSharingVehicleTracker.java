package category1_state_machines;

import java.util.*;

/**
 * Problem 8: Ride-Sharing Vehicle Status Tracker
 * 
 * Three-part progression:
 * - Part 1: Fix GPS parsing (strip alphabetical symbols, parse as float)
 * - Part 2: Track ride state machine (AVAILABLE -> EN_ROUTE -> TRIP_ACTIVE -> COMPLETED)
 * - Part 3: Detect ghost rides (distance exceeds passenger route by 40%)
 */
public class Problem8_RideSharingVehicleTracker {

    /**
     * Part 1: Bug Fix - GPS coordinate parsing
     * Issue: GPS strings contain trailing N/S/E/W characters
     * Solution: Strip alphabetical symbols and parse as float
     */
    public static class Part1_BugFix {
        public static class GPSCoordinate {
            public double latitude;
            public double longitude;

            public GPSCoordinate(double latitude, double longitude) {
                this.latitude = latitude;
                this.longitude = longitude;
            }

            @Override
            public String toString() {
                return String.format("(%.6f, %.6f)", latitude, longitude);
            }
        }

        /**
         * Parse GPS coordinate string
         * Examples: "40.7128N, -74.0060W", "40.7128, -74.0060"
         */
        public static GPSCoordinate parseGPSCoordinate(String coordStr) {
            if (coordStr == null || coordStr.isEmpty()) {
                throw new IllegalArgumentException("Invalid coordinate");
            }

            // Split by comma
            String[] parts = coordStr.split(",");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid format: " + coordStr);
            }

            // Strip alphabetical characters and parse
            String latStr = parts[0].trim().replaceAll("[^0-9.-]", "");
            String lonStr = parts[1].trim().replaceAll("[^0-9.-]", "");

            double latitude = Double.parseDouble(latStr);
            double longitude = Double.parseDouble(lonStr);

            return new GPSCoordinate(latitude, longitude);
        }
    }

    /**
     * Part 2: State Machine - Track ride lifecycle
     */
    public static class Part2_RideStateMachine {
        public static class RideEvent {
            public String vehicleId;
            public String rideId;
            public String status; // AVAILABLE, EN_ROUTE_TO_PICKUP, TRIP_ACTIVE, COMPLETED
            public long timestamp;
            public Part1_BugFix.GPSCoordinate location;

            public RideEvent(String vehicleId, String rideId, String status,
                           long timestamp, Part1_BugFix.GPSCoordinate location) {
                this.vehicleId = vehicleId;
                this.rideId = rideId;
                this.status = status;
                this.timestamp = timestamp;
                this.location = location;
            }

            @Override
            public String toString() {
                return String.format("%s[%s]: %s @%s", vehicleId, rideId, status, location);
            }
        }

        /**
         * Find rides that went TRIP_ACTIVE without EN_ROUTE_TO_PICKUP
         */
        public static List<String> findIncompleteRides(List<RideEvent> events) {
            if (events == null || events.isEmpty()) {
                return new ArrayList<>();
            }

            // Group by ride
            Map<String, List<RideEvent>> rideEvents = new HashMap<>();
            for (RideEvent event : events) {
                rideEvents.putIfAbsent(event.rideId, new ArrayList<>());
                rideEvents.get(event.rideId).add(event);
            }

            // Sort each ride's events
            for (List<RideEvent> timeline : rideEvents.values()) {
                timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
            }

            List<String> incompleteRides = new ArrayList<>();

            for (Map.Entry<String, List<RideEvent>> entry : rideEvents.entrySet()) {
                String rideId = entry.getKey();
                List<RideEvent> timeline = entry.getValue();

                boolean hasEnRoute = false;
                boolean hasTripActive = false;

                for (RideEvent event : timeline) {
                    if ("EN_ROUTE_TO_PICKUP".equals(event.status)) {
                        hasEnRoute = true;
                    }
                    if ("TRIP_ACTIVE".equals(event.status)) {
                        hasTripActive = true;
                    }
                }

                // Flag if TRIP_ACTIVE but no EN_ROUTE
                if (hasTripActive && !hasEnRoute) {
                    incompleteRides.add(rideId);
                }
            }

            return incompleteRides;
        }
    }

    /**
     * Part 3: Ghost Ride Detection - Fraudulent distance
     * Find drivers with TRIP_ACTIVE distance > 40% of passenger route
     */
    public static class Part3_GhostRideDetection {
        /**
         * Calculate distance between two coordinates (Haversine formula)
         */
        public static double calculateDistance(
            Part1_BugFix.GPSCoordinate coord1,
            Part1_BugFix.GPSCoordinate coord2) {
            
            final int EARTH_RADIUS = 3959; // miles

            double lat1 = Math.toRadians(coord1.latitude);
            double lon1 = Math.toRadians(coord1.longitude);
            double lat2 = Math.toRadians(coord2.latitude);
            double lon2 = Math.toRadians(coord2.longitude);

            double dlat = lat2 - lat1;
            double dlon = lon2 - lon1;

            double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                      Math.cos(lat1) * Math.cos(lat2) *
                      Math.sin(dlon / 2) * Math.sin(dlon / 2);
            
            double c = 2 * Math.asin(Math.sqrt(a));
            
            return EARTH_RADIUS * c;
        }

        /**
         * Detect ghost rides with excessive distance
         */
        public static List<String> detectGhostRides(
            List<Part2_RideStateMachine.RideEvent> events,
            Map<String, Double> requestedDistances) {
            
            if (events == null || events.isEmpty()) {
                return new ArrayList<>();
            }

            // Group by ride
            Map<String, List<Part2_RideStateMachine.RideEvent>> rideEvents = new HashMap<>();
            for (Part2_RideStateMachine.RideEvent event : events) {
                rideEvents.putIfAbsent(event.rideId, new ArrayList<>());
                rideEvents.get(event.rideId).add(event);
            }

            List<String> ghostRides = new ArrayList<>();

            for (Map.Entry<String, List<Part2_RideStateMachine.RideEvent>> entry : rideEvents.entrySet()) {
                String rideId = entry.getKey();
                List<Part2_RideStateMachine.RideEvent> timeline = entry.getValue();

                // Sort by timestamp
                timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

                // Find TRIP_ACTIVE events
                Part1_BugFix.GPSCoordinate pickupLocation = null;
                Part1_BugFix.GPSCoordinate dropoffLocation = null;

                for (Part2_RideStateMachine.RideEvent event : timeline) {
                    if ("EN_ROUTE_TO_PICKUP".equals(event.status)) {
                        pickupLocation = event.location;
                    }
                    if ("COMPLETED".equals(event.status)) {
                        dropoffLocation = event.location;
                    }
                }

                // Calculate actual distance traveled
                if (pickupLocation != null && dropoffLocation != null) {
                    double actualDistance = calculateDistance(pickupLocation, dropoffLocation);
                    double requestedDistance = requestedDistances.getOrDefault(rideId, 0.0);

                    // Flag if actual > 140% of requested (40% excess)
                    if (actualDistance > requestedDistance * 1.4) {
                        ghostRides.add(rideId);
                    }
                }
            }

            return ghostRides;
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 8: Ride-Sharing Vehicle Status Tracker ===");

        // Part 1: Test GPS parsing
        System.out.println("\n--- Part 1: GPS Coordinate Parsing ---");
        String[] coordStrings = {
            "40.7128N, -74.0060W",
            "34.0522, -118.2437",
            "51.5074N, -0.1278W"
        };
        for (String coord : coordStrings) {
            try {
                Part1_BugFix.GPSCoordinate parsed = Part1_BugFix.parseGPSCoordinate(coord);
                System.out.println("  " + coord + " -> " + parsed);
            } catch (IllegalArgumentException e) {
                System.out.println("  " + coord + " -> ERROR: " + e.getMessage());
            }
        }

        // Part 2: Test ride state machine
        System.out.println("\n--- Part 2: Ride State Tracking ---");
        List<Part2_RideStateMachine.RideEvent> rideEvents = Arrays.asList(
            // Complete ride
            new Part2_RideStateMachine.RideEvent(
                "v1", "ride1", "AVAILABLE", 1000,
                new Part1_BugFix.GPSCoordinate(40.7128, -74.0060)),
            new Part2_RideStateMachine.RideEvent(
                "v1", "ride1", "EN_ROUTE_TO_PICKUP", 1100,
                new Part1_BugFix.GPSCoordinate(40.7200, -74.0100)),
            new Part2_RideStateMachine.RideEvent(
                "v1", "ride1", "TRIP_ACTIVE", 1200,
                new Part1_BugFix.GPSCoordinate(40.7200, -74.0100)),
            new Part2_RideStateMachine.RideEvent(
                "v1", "ride1", "COMPLETED", 1300,
                new Part1_BugFix.GPSCoordinate(40.7500, -74.0200)),
            
            // Incomplete ride
            new Part2_RideStateMachine.RideEvent(
                "v2", "ride2", "AVAILABLE", 2000,
                new Part1_BugFix.GPSCoordinate(40.7128, -74.0060)),
            new Part2_RideStateMachine.RideEvent(
                "v2", "ride2", "TRIP_ACTIVE", 2100,
                new Part1_BugFix.GPSCoordinate(40.7300, -74.0150)) // No EN_ROUTE!
        );
        List<String> incomplete = Part2_RideStateMachine.findIncompleteRides(rideEvents);
        System.out.println("Incomplete rides: " + incomplete);

        // Part 3: Test ghost ride detection
        System.out.println("\n--- Part 3: Ghost Ride Detection ---");
        Map<String, Double> requestedDistances = new HashMap<>();
        requestedDistances.put("ride1", 5.0); // 5 miles requested
        requestedDistances.put("ride2", 3.0); // 3 miles requested
        
        List<String> ghostRides = Part3_GhostRideDetection.detectGhostRides(rideEvents, requestedDistances);
        System.out.println("Ghost rides detected: " + ghostRides);
    }
}
