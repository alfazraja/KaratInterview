package category1_state_machines;

import java.util.*;

/**
 * Problem 3: The Food Delivery Driver Pipeline
 * 
 * Three-part progression:
 * - Part 1: Fix timezone mismatches (standardize to epoch seconds)
 * - Part 2: Track driver state machine (ACCEPTED -> PICKED_UP -> DELIVERED)
 * - Part 3: Find longest waiting duration at restaurant
 */
public class Problem3_FoodDeliveryDriverPipeline {

    /**
     * Part 1: Bug Fix - Timezone Standardization
     * Issue: Mix of local hours and UTC timestamps
     * Solution: Convert all to epoch seconds (unix timestamp)
     */
    public static class Part1_TimezoneFix {
        /**
         * Convert various timestamp formats to epoch seconds
         * Supports: UTC timestamp, local time with offset
         */
        public static long normalizeToEpoch(String timestampStr, String timezone) {
            // For simplicity, assuming input is already in seconds or convertible
            // In production, use java.time.* APIs
            try {
                // If it's already epoch seconds
                if (timestampStr.matches("\\d+")) {
                    return Long.parseLong(timestampStr);
                }
                // Handle decimal seconds
                double seconds = Double.parseDouble(timestampStr);
                return Math.round(seconds);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid timestamp: " + timestampStr, e);
            }
        }

        /**
         * Normalize a list of timestamps to ensure consistency
         */
        public static List<Long> normalizeTimestamps(List<String> timestamps) {
            List<Long> normalized = new ArrayList<>();
            for (String ts : timestamps) {
                normalized.add(normalizeToEpoch(ts, "UTC"));
            }
            return normalized;
        }
    }

    /**
     * Part 2: State Machine - Track Driver Pipeline
     * Sequence: ACCEPTED -> PICKED_UP -> DELIVERED
     */
    public static class Part2_DriverStateMachine {
        public static class DeliveryEvent {
            public String orderId;
            public String driverId;
            public String status; // ACCEPTED, PICKED_UP, DELIVERED
            public long timestamp; // epoch seconds
            public String location;

            public DeliveryEvent(String orderId, String driverId, String status, 
                               long timestamp, String location) {
                this.orderId = orderId;
                this.driverId = driverId;
                this.status = status;
                this.timestamp = timestamp;
                this.location = location;
            }

            @Override
            public String toString() {
                return String.format("%s:%s[%s]@%d", orderId, driverId, status, timestamp);
            }
        }

        public static class PipelineAnalysis {
            public List<String> pickedUpButNotDelivered; // orders picked but never delivered
            public Map<String, DeliveryEvent> orderStatuses;
            public Map<String, Long> orderDurations;

            public PipelineAnalysis() {
                this.pickedUpButNotDelivered = new ArrayList<>();
                this.orderStatuses = new HashMap<>();
                this.orderDurations = new HashMap<>();
            }
        }

        /**
         * Analyze delivery pipeline for anomalies
         * Find orders picked up but never delivered before log stream ends
         * Time Complexity: O(n log n) for sorting + O(n) for processing
         * Space Complexity: O(n)
         */
        public static PipelineAnalysis analyzePipeline(List<DeliveryEvent> events) {
            PipelineAnalysis analysis = new PipelineAnalysis();

            // Group by order ID and sort by timestamp
            Map<String, List<DeliveryEvent>> orderTimeline = new HashMap<>();
            for (DeliveryEvent event : events) {
                orderTimeline.putIfAbsent(event.orderId, new ArrayList<>());
                orderTimeline.get(event.orderId).add(event);
            }

            // Sort each order's events chronologically
            for (List<DeliveryEvent> timeline : orderTimeline.values()) {
                timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
            }

            // Analyze each order's pipeline
            for (Map.Entry<String, List<DeliveryEvent>> entry : orderTimeline.entrySet()) {
                String orderId = entry.getKey();
                List<DeliveryEvent> timeline = entry.getValue();

                boolean accepted = false;
                boolean pickedUp = false;
                boolean delivered = false;
                long acceptTime = 0, pickupTime = 0, deliveredTime = 0;

                for (DeliveryEvent event : timeline) {
                    switch (event.status) {
                        case "ACCEPTED":
                            accepted = true;
                            acceptTime = event.timestamp;
                            break;
                        case "PICKED_UP":
                            pickedUp = true;
                            pickupTime = event.timestamp;
                            break;
                        case "DELIVERED":
                            delivered = true;
                            deliveredTime = event.timestamp;
                            break;
                    }
                }

                // Check for anomaly: picked up but never delivered
                if (pickedUp && !delivered) {
                    analysis.pickedUpButNotDelivered.add(orderId);
                }

                // Track final status and duration
                if (delivered) {
                    DeliveryEvent lastEvent = timeline.get(timeline.size() - 1);
                    analysis.orderStatuses.put(orderId, lastEvent);
                    if (acceptTime > 0) {
                        analysis.orderDurations.put(orderId, deliveredTime - acceptTime);
                    }
                }
            }

            return analysis;
        }
    }

    /**
     * Part 3: Longest Wait Time - Find max duration at restaurant
     * Time between PICKED_UP and DELIVERED stages
     */
    public static class Part3_LongestWait {
        public static class WaitDuration {
            public String driverId;
            public String location;
            public long waitSeconds;
            public long startTime;
            public long endTime;

            public WaitDuration(String driverId, String location, 
                               long startTime, long endTime) {
                this.driverId = driverId;
                this.location = location;
                this.startTime = startTime;
                this.endTime = endTime;
                this.waitSeconds = endTime - startTime;
            }

            @Override
            public String toString() {
                return String.format("%s at %s: %d seconds (%.1f min)", 
                    driverId, location, waitSeconds, waitSeconds / 60.0);
            }
        }

        /**
         * Find driver with longest wait time at restaurant
         * Assuming PICKED_UP marks arrival at restaurant
         * and DELIVERED marks departure from restaurant
         * Time Complexity: O(n log n) for sorting + O(n) for analysis
         */
        public static WaitDuration findLongestWait(
            List<Part2_DriverStateMachine.DeliveryEvent> events) {
            
            if (events == null || events.isEmpty()) {
                return null;
            }

            // Group events by driver
            Map<String, List<Part2_DriverStateMachine.DeliveryEvent>> driverEvents = 
                new HashMap<>();
            for (Part2_DriverStateMachine.DeliveryEvent event : events) {
                driverEvents.putIfAbsent(event.driverId, new ArrayList<>());
                driverEvents.get(event.driverId).add(event);
            }

            WaitDuration maxWait = null;

            // Analyze each driver's pickup and delivery times
            for (Map.Entry<String, List<Part2_DriverStateMachine.DeliveryEvent>> entry : 
                 driverEvents.entrySet()) {
                
                String driverId = entry.getKey();
                List<Part2_DriverStateMachine.DeliveryEvent> driverEventList = entry.getValue();

                // Sort by timestamp
                driverEventList.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

                // Find paired PICKED_UP and DELIVERED events
                for (int i = 0; i < driverEventList.size() - 1; i++) {
                    Part2_DriverStateMachine.DeliveryEvent current = driverEventList.get(i);
                    
                    if ("PICKED_UP".equals(current.status)) {
                        // Find corresponding DELIVERED event
                        for (int j = i + 1; j < driverEventList.size(); j++) {
                            Part2_DriverStateMachine.DeliveryEvent next = driverEventList.get(j);
                            
                            if ("DELIVERED".equals(next.status)) {
                                WaitDuration wait = new WaitDuration(
                                    driverId, current.location,
                                    current.timestamp, next.timestamp
                                );
                                
                                // Update max if this wait is longer
                                if (maxWait == null || wait.waitSeconds > maxWait.waitSeconds) {
                                    maxWait = wait;
                                }
                                break;
                            }
                        }
                    }
                }
            }

            return maxWait;
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 3: Food Delivery Driver Pipeline ===");

        // Part 1: Timezone normalization
        System.out.println("\n--- Part 1: Timezone Normalization ---");
        List<String> rawTimestamps = Arrays.asList("1000", "1050.5", "1100");
        List<Long> normalized = Part1_TimezoneFix.normalizeTimestamps(rawTimestamps);
        System.out.println("Raw: " + rawTimestamps);
        System.out.println("Normalized: " + normalized);

        // Part 2: Pipeline analysis
        System.out.println("\n--- Part 2: Pipeline Analysis ---");
        List<Part2_DriverStateMachine.DeliveryEvent> deliveryEvents = Arrays.asList(
            new Part2_DriverStateMachine.DeliveryEvent("order1", "driver1", "ACCEPTED", 1000, "restaurant_A"),
            new Part2_DriverStateMachine.DeliveryEvent("order1", "driver1", "PICKED_UP", 1100, "restaurant_A"),
            new Part2_DriverStateMachine.DeliveryEvent("order1", "driver1", "DELIVERED", 1200, "customer_1"),
            
            new Part2_DriverStateMachine.DeliveryEvent("order2", "driver2", "ACCEPTED", 1050, "restaurant_B"),
            new Part2_DriverStateMachine.DeliveryEvent("order2", "driver2", "PICKED_UP", 1150, "restaurant_B")
            // order2 never delivered - anomaly!
        );
        Part2_DriverStateMachine.PipelineAnalysis analysis = 
            Part2_DriverStateMachine.analyzePipeline(deliveryEvents);
        System.out.println("Picked up but not delivered: " + analysis.pickedUpButNotDelivered);
        System.out.println("Order durations: " + analysis.orderDurations);

        // Part 3: Longest wait
        System.out.println("\n--- Part 3: Longest Wait at Restaurant ---");
        List<Part2_DriverStateMachine.DeliveryEvent> waitEvents = Arrays.asList(
            new Part2_DriverStateMachine.DeliveryEvent("order1", "driver1", "PICKED_UP", 1000, "restaurant_A"),
            new Part2_DriverStateMachine.DeliveryEvent("order1", "driver1", "DELIVERED", 1050, "customer_1"),
            
            new Part2_DriverStateMachine.DeliveryEvent("order2", "driver2", "PICKED_UP", 2000, "restaurant_B"),
            new Part2_DriverStateMachine.DeliveryEvent("order2", "driver2", "DELIVERED", 2500, "customer_2") // 500s wait
        );
        Part3_LongestWait.WaitDuration longestWait = 
            Part3_LongestWait.findLongestWait(waitEvents);
        System.out.println("Longest wait: " + longestWait);
    }
}
