package category1_state_machines;

import java.util.*;

/**
 * Problem 4: The Cloud Server Resource Telemetry Monitor
 * 
 * Three-part progression:
 * - Part 1: Fix parsing (handle delimiter splits instead of hardcoded indices)
 * - Part 2: Detect crash patterns (START -> CRASH within 15 seconds = unstable)
 * - Part 3: Calculate peak concurrency (max concurrent active tasks at any time)
 */
public class Problem4_CloudServerTelemetryMonitor {

    /**
     * Part 1: Bug Fix - Proper delimiter-based parsing
     * Issue: Index splitting uses hardcoded segment lengths, drops final state
     * Solution: Use delimiter split to extract all fields reliably
     */
    public static class Part1_BugFix {
        public static class ServerStatus {
            public String nodeId;
            public double cpuUsage;
            public String state;

            public ServerStatus(String nodeId, double cpuUsage, String state) {
                this.nodeId = nodeId;
                this.cpuUsage = cpuUsage;
                this.state = state;
            }

            @Override
            public String toString() {
                return String.format("%s: CPU=%.1f%%, State=%s", nodeId, cpuUsage, state);
            }
        }

        /**
         * Parse server status line correctly
         * Format: "node_1, 99.8, RUNNING"
         */
        public static ServerStatus parseServerStatus(String statusLine) {
            if (statusLine == null || statusLine.isEmpty()) {
                throw new IllegalArgumentException("Invalid status line");
            }

            // Split by comma delimiter
            String[] parts = statusLine.split(",");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid format: " + statusLine);
            }

            String nodeId = parts[0].trim();
            double cpuUsage = Double.parseDouble(parts[1].trim());
            String state = parts[2].trim().toUpperCase();

            return new ServerStatus(nodeId, cpuUsage, state);
        }

        /**
         * Parse multiple server status lines
         */
        public static List<ServerStatus> parseServerStatuses(List<String> lines) {
            List<ServerStatus> statuses = new ArrayList<>();
            for (String line : lines) {
                statuses.add(parseServerStatus(line));
            }
            return statuses;
        }
    }

    /**
     * Part 2: State Machine - Detect unstable nodes
     * Unstable = START -> CRASH within 15 seconds
     */
    public static class Part2_CrashDetection {
        public static class NodeEvent {
            public String nodeId;
            public String state; // START, RUNNING, CRASH
            public long timestamp;
            public double cpuUsage;

            public NodeEvent(String nodeId, String state, long timestamp, double cpuUsage) {
                this.nodeId = nodeId;
                this.state = state;
                this.timestamp = timestamp;
                this.cpuUsage = cpuUsage;
            }

            @Override
            public String toString() {
                return String.format("%s[%s]@%d(CPU=%.1f%%)", nodeId, state, timestamp, cpuUsage);
            }
        }

        /**
         * Find all unstable nodes (crash within 15 seconds of starting)
         * Time Complexity: O(n log n) for sorting + O(n) for detection
         */
        public static List<String> detectUnstableNodes(List<NodeEvent> events) {
            if (events == null || events.isEmpty()) {
                return new ArrayList<>();
            }

            // Group events by node
            Map<String, List<NodeEvent>> nodeEvents = new HashMap<>();
            for (NodeEvent event : events) {
                nodeEvents.putIfAbsent(event.nodeId, new ArrayList<>());
                nodeEvents.get(event.nodeId).add(event);
            }

            // Sort each node's events by timestamp
            for (List<NodeEvent> timeline : nodeEvents.values()) {
                timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
            }

            List<String> unstableNodes = new ArrayList<>();
            long CRASH_THRESHOLD = 15; // seconds

            // Check each node for instability
            for (Map.Entry<String, List<NodeEvent>> entry : nodeEvents.entrySet()) {
                String nodeId = entry.getKey();
                List<NodeEvent> timeline = entry.getValue();

                // Find START event
                long startTime = -1;
                for (NodeEvent event : timeline) {
                    if ("START".equals(event.state)) {
                        startTime = event.timestamp;
                    } else if ("CRASH".equals(event.state) && startTime != -1) {
                        // Check if crash happened within threshold
                        if (event.timestamp - startTime <= CRASH_THRESHOLD) {
                            unstableNodes.add(nodeId);
                            break; // Already marked as unstable
                        }
                    }
                }
            }

            return unstableNodes;
        }
    }

    /**
     * Part 3: Peak Concurrency - Maximum concurrent active tasks
     * Count nodes in active states (RUNNING, PROCESSING) at any time
     */
    public static class Part3_PeakConcurrency {
        public static class NodeSnapshot {
            public String nodeId;
            public String state;
            public long timestamp;
            public boolean isStart; // true for start event, false for end

            public NodeSnapshot(String nodeId, String state, long timestamp, boolean isStart) {
                this.nodeId = nodeId;
                this.state = state;
                this.timestamp = timestamp;
                this.isStart = isStart;
            }
        }

        /**
         * Calculate maximum concurrent active nodes
         * Active states: RUNNING, PROCESSING
         * Time Complexity: O(n log n) for sorting + O(n) for sweep
         */
        public static int calculatePeakConcurrency(List<Part2_CrashDetection.NodeEvent> events) {
            if (events == null || events.isEmpty()) {
                return 0;
            }

            // Create snapshots for active periods
            List<NodeSnapshot> snapshots = new ArrayList<>();
            Set<String> activeStates = new HashSet<>(Arrays.asList("RUNNING", "PROCESSING", "ACTIVE"));

            for (Part2_CrashDetection.NodeEvent event : events) {
                if (activeStates.contains(event.state)) {
                    snapshots.add(new NodeSnapshot(event.nodeId, event.state, event.timestamp, true));
                }
                // Assume node becomes inactive on crash, stop, or idle
                if ("CRASH".equals(event.state) || "STOPPED".equals(event.state) || "IDLE".equals(event.state)) {
                    snapshots.add(new NodeSnapshot(event.nodeId, event.state, event.timestamp, false));
                }
            }

            // Sort by timestamp, with starts before ends at same time
            snapshots.sort((a, b) -> {
                if (a.timestamp != b.timestamp) {
                    return Long.compare(a.timestamp, b.timestamp);
                }
                return Boolean.compare(!a.isStart, !b.isStart); // starts first
            });

            // Sweep line algorithm
            int currentActive = 0;
            int maxConcurrent = 0;
            Set<String> activeNodes = new HashSet<>();

            for (NodeSnapshot snapshot : snapshots) {
                if (snapshot.isStart) {
                    if (!activeNodes.contains(snapshot.nodeId)) {
                        activeNodes.add(snapshot.nodeId);
                        currentActive++;
                    }
                } else {
                    if (activeNodes.contains(snapshot.nodeId)) {
                        activeNodes.remove(snapshot.nodeId);
                        currentActive--;
                    }
                }
                maxConcurrent = Math.max(maxConcurrent, currentActive);
            }

            return maxConcurrent;
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 4: Cloud Server Telemetry Monitor ===");

        // Part 1: Test parsing
        System.out.println("\n--- Part 1: Server Status Parsing ---");
        List<String> statusLines = Arrays.asList(
            "node_1, 99.8, RUNNING",
            "node_2, 45.2, IDLE",
            "node_3, 12.5, PROCESSING"
        );
        List<Part1_BugFix.ServerStatus> statuses = Part1_BugFix.parseServerStatuses(statusLines);
        System.out.println("Parsed statuses:");
        for (Part1_BugFix.ServerStatus status : statuses) {
            System.out.println("  " + status);
        }

        // Part 2: Test crash detection
        System.out.println("\n--- Part 2: Crash Detection ---");
        List<Part2_CrashDetection.NodeEvent> events = Arrays.asList(
            new Part2_CrashDetection.NodeEvent("node1", "START", 1000, 10.0),
            new Part2_CrashDetection.NodeEvent("node1", "CRASH", 1010, 95.0), // unstable: 10 sec

            new Part2_CrashDetection.NodeEvent("node2", "START", 2000, 5.0),
            new Part2_CrashDetection.NodeEvent("node2", "RUNNING", 2010, 30.0),
            new Part2_CrashDetection.NodeEvent("node2", "CRASH", 3000, 98.0), // stable: 1000 sec
        );
        List<String> unstableNodes = Part2_CrashDetection.detectUnstableNodes(events);
        System.out.println("Unstable nodes (crash within 15 sec): " + unstableNodes);

        // Part 3: Test peak concurrency
        System.out.println("\n--- Part 3: Peak Concurrency ---");
        List<Part2_CrashDetection.NodeEvent> concurrencyEvents = Arrays.asList(
            new Part2_CrashDetection.NodeEvent("node1", "RUNNING", 1000, 50.0),
            new Part2_CrashDetection.NodeEvent("node2", "RUNNING", 1100, 50.0),
            new Part2_CrashDetection.NodeEvent("node3", "RUNNING", 1200, 50.0),
            new Part2_CrashDetection.NodeEvent("node1", "CRASHED", 1500, 100.0),
            new Part2_CrashDetection.NodeEvent("node4", "RUNNING", 1600, 50.0),
            new Part2_CrashDetection.NodeEvent("node2", "CRASHED", 1700, 100.0)
        );
        int peakConcurrency = Part3_PeakConcurrency.calculatePeakConcurrency(concurrencyEvents);
        System.out.println("Peak concurrent nodes: " + peakConcurrency);
    }
}
