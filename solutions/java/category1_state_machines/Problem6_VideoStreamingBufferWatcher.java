package category1_state_machines;

import java.util.*;

/**
 * Problem 6: Video Streaming Session Buffer Watcher
 * 
 * Three-part progression:
 * - Part 1: Fix floating-point precision loss in accumulation
 * - Part 2: Track video state transitions (PLAYING -> BUFFERING -> STALLED)
 * - Part 3: Identify users who abandoned during STALLED state and calc avg duration
 */
public class Problem6_VideoStreamingBufferWatcher {

    /**
     * Part 1: Bug Fix - High-precision floating-point accumulation
     * Issue: Precision loss when accumulating fractions using basic float
     * Solution: Use high-precision arithmetic (BigDecimal or double with care)
     */
    public static class Part1_BugFix {
        /**
         * Calculate total playback time with precision
         * Using double with proper accumulation
         */
        public static double calculateTotalPlaybackTime(List<Double> sessionDurations) {
            if (sessionDurations == null || sessionDurations.isEmpty()) {
                return 0.0;
            }

            // Sum using high-precision approach (Kahan summation)
            double sum = 0.0;
            double correction = 0.0;

            for (double duration : sessionDurations) {
                double y = duration - correction;
                double t = sum + y;
                correction = (t - sum) - y;
                sum = t;
            }

            return sum;
        }

        /**
         * Alternative: Using BigDecimal for exact precision
         */
        public static String calculateTotalPlaybackTimePrecise(List<String> sessionDurations) {
            if (sessionDurations == null || sessionDurations.isEmpty()) {
                return "0.0";
            }

            java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
            for (String duration : sessionDurations) {
                sum = sum.add(new java.math.BigDecimal(duration));
            }

            return sum.toString();
        }
    }

    /**
     * Part 2: State Machine - Track video playback states
     */
    public static class Part2_StateMachine {
        public static class BufferEvent {
            public String userId;
            public String state; // PLAYING, BUFFERING, STALLED
            public long timestamp;
            public double videoPosition; // seconds into video

            public BufferEvent(String userId, String state, long timestamp, double videoPosition) {
                this.userId = userId;
                this.state = state;
                this.timestamp = timestamp;
                this.videoPosition = videoPosition;
            }

            @Override
            public String toString() {
                return String.format("%s: %s@%.1fs [%d]", userId, state, videoPosition, timestamp);
            }
        }

        public static class BufferAnalysis {
            public String userId;
            public int stallCount; // number of distinct stalls
            public List<String> stallSessions; // sessions with 3+ distinct stalls

            public BufferAnalysis(String userId) {
                this.userId = userId;
                this.stallCount = 0;
                this.stallSessions = new ArrayList<>();
            }
        }

        /**
         * Find users with 3+ distinct buffering stalls
         * Time Complexity: O(n log n) for sorting + O(n) for analysis
         */
        public static List<String> findUsersWithMultipleStalls(List<BufferEvent> events) {
            if (events == null || events.isEmpty()) {
                return new ArrayList<>();
            }

            // Group by user
            Map<String, List<BufferEvent>> userEvents = new HashMap<>();
            for (BufferEvent event : events) {
                userEvents.putIfAbsent(event.userId, new ArrayList<>());
                userEvents.get(event.userId).add(event);
            }

            // Sort each user's events
            for (List<BufferEvent> timeline : userEvents.values()) {
                timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
            }

            List<String> affectedUsers = new ArrayList<>();

            for (Map.Entry<String, List<BufferEvent>> entry : userEvents.entrySet()) {
                String userId = entry.getKey();
                List<BufferEvent> timeline = entry.getValue();

                int stallCount = 0;
                boolean inBuffering = false;
                String prevState = null;

                // Count state transitions to STALLED from PLAYING
                for (BufferEvent event : timeline) {
                    if ("STALLED".equals(event.state) && !"STALLED".equals(prevState)) {
                        stallCount++;
                    }
                    prevState = event.state;
                }

                if (stallCount >= 3) {
                    affectedUsers.add(userId);
                }
            }

            return affectedUsers;
        }
    }

    /**
     * Part 3: Churn Predictor - Users abandoning during stalls
     * Calculate average stall duration before abandonment
     */
    public static class Part3_ChurnPrediction {
        public static class ChurnAnalysis {
            public String userId;
            public long abandonmentTime;
            public String lastState;
            public double avgStallDuration;
            public int totalStalls;

            public ChurnAnalysis(String userId) {
                this.userId = userId;
                this.totalStalls = 0;
                this.avgStallDuration = 0.0;
            }

            @Override
            public String toString() {
                return String.format("%s: Abandoned in %s, Avg stall=%.2fs, Total stalls=%d",
                    userId, lastState, avgStallDuration, totalStalls);
            }
        }

        /**
         * Find users who quit while STALLED and calculate average stall duration
         */
        public static List<ChurnAnalysis> analyzeChurnRisk(List<Part2_StateMachine.BufferEvent> events) {
            if (events == null || events.isEmpty()) {
                return new ArrayList<>();
            }

            // Group by user
            Map<String, List<Part2_StateMachine.BufferEvent>> userEvents = new HashMap<>();
            for (Part2_StateMachine.BufferEvent event : events) {
                userEvents.putIfAbsent(event.userId, new ArrayList<>());
                userEvents.get(event.userId).add(event);
            }

            List<ChurnAnalysis> churners = new ArrayList<>();

            for (Map.Entry<String, List<Part2_StateMachine.BufferEvent>> entry : userEvents.entrySet()) {
                String userId = entry.getKey();
                List<Part2_StateMachine.BufferEvent> timeline = entry.getValue();

                // Sort by timestamp
                timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

                // Check if user abandoned in STALLED state
                if (!timeline.isEmpty()) {
                    Part2_StateMachine.BufferEvent lastEvent = timeline.get(timeline.size() - 1);

                    if ("STALLED".equals(lastEvent.state)) {
                        ChurnAnalysis churn = new ChurnAnalysis(userId);
                        churn.abandonmentTime = lastEvent.timestamp;
                        churn.lastState = lastEvent.state;

                        // Calculate average stall duration
                        double totalStallTime = 0.0;
                        int stallCount = 0;
                        long stallStart = -1;

                        for (int i = 0; i < timeline.size(); i++) {
                            Part2_StateMachine.BufferEvent event = timeline.get(i);

                            if ("STALLED".equals(event.state)) {
                                if (stallStart == -1) {
                                    stallStart = event.timestamp;
                                }
                            } else {
                                if (stallStart != -1) {
                                    totalStallTime += (event.timestamp - stallStart);
                                    stallCount++;
                                    stallStart = -1;
                                }
                            }
                        }

                        // If still in STALLED at end
                        if (stallStart != -1) {
                            totalStallTime += (lastEvent.timestamp - stallStart);
                            stallCount++;
                        }

                        churn.totalStalls = stallCount;
                        if (stallCount > 0) {
                            churn.avgStallDuration = totalStallTime / stallCount;
                        }

                        churners.add(churn);
                    }
                }
            }

            return churners;
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 6: Video Streaming Buffer Watcher ===");

        // Part 1: Test precision
        System.out.println("\n--- Part 1: High-Precision Calculation ---");
        List<Double> durations = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        double total = Part1_BugFix.calculateTotalPlaybackTime(durations);
        System.out.println("Total playback: " + total + " seconds");

        List<String> precisiourations = Arrays.asList("1.1", "2.2", "3.3");
        String precisTotal = Part1_BugFix.calculateTotalPlaybackTimePrecise(precisiourations);
        System.out.println("Precise total: " + precisTotal + " seconds");

        // Part 2: Test state machine
        System.out.println("\n--- Part 2: Buffering Stall Detection ---");
        List<Part2_StateMachine.BufferEvent> events = Arrays.asList(
            new Part2_StateMachine.BufferEvent("user1", "PLAYING", 1000, 10.0),
            new Part2_StateMachine.BufferEvent("user1", "BUFFERING", 1100, 10.0),
            new Part2_StateMachine.BufferEvent("user1", "STALLED", 1200, 10.0),
            new Part2_StateMachine.BufferEvent("user1", "PLAYING", 1300, 15.0),
            
            new Part2_StateMachine.BufferEvent("user1", "BUFFERING", 1400, 20.0),
            new Part2_StateMachine.BufferEvent("user1", "STALLED", 1500, 20.0),
            new Part2_StateMachine.BufferEvent("user1", "PLAYING", 1600, 25.0),
            
            new Part2_StateMachine.BufferEvent("user1", "BUFFERING", 1700, 30.0),
            new Part2_StateMachine.BufferEvent("user1", "STALLED", 1800, 30.0),
            new Part2_StateMachine.BufferEvent("user1", "STALLED", 1900, 30.0) // 3rd stall
        );
        List<String> stallUsers = Part2_StateMachine.findUsersWithMultipleStalls(events);
        System.out.println("Users with 3+ stalls: " + stallUsers);

        // Part 3: Test churn prediction
        System.out.println("\n--- Part 3: Churn Risk Analysis ---");
        List<Part3_ChurnPrediction.ChurnAnalysis> churnAnalyses = 
            Part3_ChurnPrediction.analyzeChurnRisk(events);
        System.out.println("Churn risk analysis:");
        for (Part3_ChurnPrediction.ChurnAnalysis analysis : churnAnalyses) {
            System.out.println("  " + analysis);
        }
    }
}
