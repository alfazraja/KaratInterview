package category1_state_machines;

import java.util.*;

/**
 * Problem 1: The Key-Card Badge Access System
 * 
 * Three-part progression:
 * - Part 1: Fix data parsing (trim whitespace, normalize case)
 * - Part 2: Track state transitions and detect anomalies
 * - Part 3: Detect card-sharing fraud using sliding window
 */
public class Problem1_KeyCardBadgeAccessSystem {

    /**
     * Part 1: Bug Fix - Data Parsing
     * Issue: Raw text has trailing whitespace and case mismatches
     * Solution: Clean and normalize input data
     */
    public static class Part1_BugFix {
        public static List<String> cleanAccessLogs(List<String> rawLogs) {
            List<String> cleanedLogs = new ArrayList<>();
            for (String log : rawLogs) {
                // Remove trailing whitespace and convert to uppercase
                String cleaned = log.trim().toUpperCase();
                cleanedLogs.add(cleaned);
            }
            return cleanedLogs;
        }
    }

    /**
     * Part 2: State Machine - Track Room Access
     * Issues: Identify users who exited without entry, or entered without exit
     * Data Structure: HashMap<userId, List<AccessEvents>>
     */
    public static class Part2_StateMachine {
        public static class AccessRecord {
            public String userId;
            public long timestamp;
            public String action; // "ENTER" or "EXIT"
            public String roomId;

            public AccessRecord(String userId, long timestamp, String action, String roomId) {
                this.userId = userId;
                this.timestamp = timestamp;
                this.action = action;
                this.roomId = roomId;
            }
        }

        public static class AnomalyResult {
            public List<String> exitedWithoutEntry;
            public List<String> enteredWithoutExit;

            public AnomalyResult() {
                this.exitedWithoutEntry = new ArrayList<>();
                this.enteredWithoutExit = new ArrayList<>();
            }
        }

        /**
         * Analyze access logs for safety anomalies
         * Time Complexity: O(n log n) for sorting + O(n) for processing
         * Space Complexity: O(n)
         */
        public static AnomalyResult detectAnomalies(List<AccessRecord> records) {
            AnomalyResult result = new AnomalyResult();
            
            // Group by user and room
            Map<String, Map<String, Integer>> userRoomState = new HashMap<>();
            
            // Sort by timestamp to process chronologically
            List<AccessRecord> sortedRecords = new ArrayList<>(records);
            sortedRecords.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

            for (AccessRecord record : sortedRecords) {
                String key = record.userId + ":" + record.roomId;
                
                userRoomState.putIfAbsent(record.userId, new HashMap<>());
                Map<String, Integer> roomState = userRoomState.get(record.userId);
                
                int currentCount = roomState.getOrDefault(key, 0);
                
                if ("ENTER".equals(record.action)) {
                    currentCount++;
                } else if ("EXIT".equals(record.action)) {
                    currentCount--;
                    // Exit without entry detected
                    if (currentCount < 0) {
                        result.exitedWithoutEntry.add(record.userId);
                        currentCount = 0; // Reset to prevent cascading errors
                    }
                }
                
                roomState.put(key, currentCount);
            }

            // Check for users still in rooms (entered but never exited)
            for (Map.Entry<String, Map<String, Integer>> userEntry : userRoomState.entrySet()) {
                String userId = userEntry.getKey();
                Map<String, Integer> roomState = userEntry.getValue();
                
                for (int count : roomState.values()) {
                    if (count > 0) {
                        result.enteredWithoutExit.add(userId);
                        break;
                    }
                }
            }

            return result;
        }
    }

    /**
     * Part 3: Fraud Detection - Sliding Window
     * Issue: Detect card-sharing fraud (3+ swipes within 60-minute window)
     * Algorithm: Use sliding window with timestamps
     */
    public static class Part3_FraudDetection {
        public static class BadgeSwipe {
            public String userId;
            public long timestamp; // in seconds
            public String badgeId;

            public BadgeSwipe(String userId, long timestamp, String badgeId) {
                this.userId = userId;
                this.timestamp = timestamp;
                this.badgeId = badgeId;
            }
        }

        /**
         * Detect badge-sharing fraud
         * Find any user swiping 3+ times within any 60-minute window
         * Time Complexity: O(n log n) for sorting + O(n) for sliding window
         * Space Complexity: O(n)
         */
        public static List<String> detectCardSharingFraud(List<BadgeSwipe> swipes) {
            if (swipes == null || swipes.size() < 3) {
                return new ArrayList<>();
            }

            // Sort by timestamp
            List<BadgeSwipe> sortedSwipes = new ArrayList<>(swipes);
            sortedSwipes.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

            Set<String> fraudUsers = new HashSet<>();
            int WINDOW_SECONDS = 60 * 60; // 60 minutes

            // Use sliding window with two pointers
            for (int i = 0; i < sortedSwipes.size(); i++) {
                BadgeSwipe currentSwipe = sortedSwipes.get(i);
                long windowStart = currentSwipe.timestamp - WINDOW_SECONDS;
                long windowEnd = currentSwipe.timestamp;

                // Count swipes in this window
                int count = 0;
                Set<String> uniqueBadges = new HashSet<>();

                for (int j = i; j < sortedSwipes.size(); j++) {
                    BadgeSwipe swipe = sortedSwipes.get(j);
                    
                    // Check if within window
                    if (swipe.timestamp > windowEnd) {
                        break;
                    }
                    
                    if (swipe.timestamp >= windowStart) {
                        if (swipe.userId.equals(currentSwipe.userId)) {
                            count++;
                            uniqueBadges.add(swipe.badgeId);
                        }
                    }
                }

                // If same user swiped 3+ times with different badges, it's fraud
                if (uniqueBadges.size() >= 3) {
                    fraudUsers.add(currentSwipe.userId);
                }
            }

            return new ArrayList<>(fraudUsers);
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 1: Key-Card Badge Access System ===");
        
        // Part 1: Test data cleaning
        System.out.println("\n--- Part 1: Data Parsing ---");
        List<String> rawLogs = Arrays.asList(
            "John, 0900, enter ",
            "  alice, 0910, exit  ",
            "BOB, 0920, enter"
        );
        List<String> cleaned = Part1_BugFix.cleanAccessLogs(rawLogs);
        System.out.println("Original: " + rawLogs);
        System.out.println("Cleaned: " + cleaned);

        // Part 2: Test anomaly detection
        System.out.println("\n--- Part 2: Anomaly Detection ---");
        List<Part2_StateMachine.AccessRecord> records = Arrays.asList(
            new Part2_StateMachine.AccessRecord("user1", 1000, "ENTER", "room1"),
            new Part2_StateMachine.AccessRecord("user1", 1100, "EXIT", "room1"),
            new Part2_StateMachine.AccessRecord("user2", 1200, "EXIT", "room2"), // anomaly
            new Part2_StateMachine.AccessRecord("user3", 1300, "ENTER", "room3") // no exit
        );
        Part2_StateMachine.AnomalyResult anomalies = Part2_StateMachine.detectAnomalies(records);
        System.out.println("Exited without entry: " + anomalies.exitedWithoutEntry);
        System.out.println("Entered without exit: " + anomalies.enteredWithoutExit);

        // Part 3: Test fraud detection
        System.out.println("\n--- Part 3: Fraud Detection (60-min window) ---");
        List<Part3_FraudDetection.BadgeSwipe> swipes = Arrays.asList(
            new Part3_FraudDetection.BadgeSwipe("user1", 1000, "badge1"),
            new Part3_FraudDetection.BadgeSwipe("user1", 1200, "badge2"),
            new Part3_FraudDetection.BadgeSwipe("user1", 1400, "badge3"), // fraud: 3 different badges in 60min
            new Part3_FraudDetection.BadgeSwipe("user2", 5000, "badge4")
        );
        List<String> fraudUsers = Part3_FraudDetection.detectCardSharingFraud(swipes);
        System.out.println("Suspected fraud users: " + fraudUsers);
    }
}
