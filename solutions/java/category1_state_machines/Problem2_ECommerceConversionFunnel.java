package category1_state_machines;

import java.util.*;

/**
 * Problem 2: The E-Commerce Conversion Funnel Tracker
 * 
 * Three-part progression:
 * - Part 1: Fix floating-point parsing (decimal precision)
 * - Part 2: Track conversion funnel (BROWSE -> ADD_TO_CART -> PURCHASE)
 * - Part 3: Match subsequences allowing gaps (e.g., BROWSE -> CHECKOUT, skipping ADD_TO_CART)
 */
public class Problem2_ECommerceConversionFunnel {

    /**
     * Part 1: Bug Fix - Floating Point Precision
     * Issue: Timestamps read as truncated integers, losing decimal precision
     * Solution: Safely parse decimal strings to double/BigDecimal
     */
    public static class Part1_BugFix {
        /**
         * Parse timestamp string with safe decimal handling
         * Examples: "1234.567", "1000.0", "999"
         */
        public static double parseTimestampSafely(String timestampStr) {
            if (timestampStr == null || timestampStr.isEmpty()) {
                throw new IllegalArgumentException("Invalid timestamp");
            }
            try {
                return Double.parseDouble(timestampStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse timestamp: " + timestampStr, e);
            }
        }

        /**
         * Calculate duration with proper floating-point precision
         */
        public static double calculateDuration(String startStr, String endStr) {
            double start = parseTimestampSafely(startStr);
            double end = parseTimestampSafely(endStr);
            return Math.abs(end - start);
        }
    }

    /**
     * Part 2: State Machine - Track Conversion Funnel
     * Sequence: BROWSE -> ADD_TO_CART -> PURCHASE
     */
    public static class Part2_ConversionFunnel {
        public static class ClickEvent {
            public String userId;
            public String action; // BROWSE, ADD_TO_CART, PURCHASE
            public double timestamp;
            public String productId;

            public ClickEvent(String userId, String action, double timestamp, String productId) {
                this.userId = userId;
                this.action = action;
                this.timestamp = timestamp;
                this.productId = productId;
            }

            @Override
            public String toString() {
                return String.format("%s:%s@%.1f", userId, action, timestamp);
            }
        }

        public static class FunnelResult {
            public int totalConversions;
            public List<String> abandonedUserIds;
            public Map<String, Integer> userConversionCount;

            public FunnelResult() {
                this.totalConversions = 0;
                this.abandonedUserIds = new ArrayList<>();
                this.userConversionCount = new HashMap<>();
            }
        }

        /**
         * Track conversion funnel: BROWSE -> ADD_TO_CART -> PURCHASE
         * Time Complexity: O(n log n) for sorting + O(n) for processing
         * Space Complexity: O(n)
         */
        public static FunnelResult analyzeConversionFunnel(List<ClickEvent> events) {
            FunnelResult result = new FunnelResult();

            // Group events by user and sort by timestamp
            Map<String, List<ClickEvent>> userEvents = new HashMap<>();
            for (ClickEvent event : events) {
                userEvents.putIfAbsent(event.userId, new ArrayList<>());
                userEvents.get(event.userId).add(event);
            }

            // Sort each user's events by timestamp
            for (List<ClickEvent> userEventList : userEvents.values()) {
                userEventList.sort((a, b) -> Double.compare(a.timestamp, b.timestamp));
            }

            // Check conversion funnel for each user
            for (Map.Entry<String, List<ClickEvent>> entry : userEvents.entrySet()) {
                String userId = entry.getKey();
                List<ClickEvent> userClickList = entry.getValue();

                int browseIdx = -1, addToCartIdx = -1, purchaseIdx = -1;

                // Find the sequence BROWSE -> ADD_TO_CART -> PURCHASE
                for (int i = 0; i < userClickList.size(); i++) {
                    ClickEvent event = userClickList.get(i);
                    
                    if (browseIdx == -1 && "BROWSE".equals(event.action)) {
                        browseIdx = i;
                    } else if (browseIdx != -1 && addToCartIdx == -1 && "ADD_TO_CART".equals(event.action)) {
                        addToCartIdx = i;
                    } else if (browseIdx != -1 && addToCartIdx != -1 && purchaseIdx == -1 && "PURCHASE".equals(event.action)) {
                        purchaseIdx = i;
                        break; // Found complete funnel
                    }
                }

                // If complete funnel found
                if (purchaseIdx != -1) {
                    result.totalConversions++;
                    result.userConversionCount.put(userId, 1);
                } else {
                    // User abandoned at some stage
                    result.abandonedUserIds.add(userId);
                }
            }

            return result;
        }
    }

    /**
     * Part 3: Subsequence Matching - Allow gaps in sequence
     * Find if user followed pattern like [home, pricing, checkout]
     * allowing other steps in between
     */
    public static class Part3_SubsequenceMatcher {
        /**
         * Check if user's click history contains target sequence as subsequence
         * (not necessarily consecutive)
         * Time Complexity: O(n) where n is length of user history
         * Space Complexity: O(1)
         */
        public static boolean matchesSubsequence(List<String> userHistory, List<String> targetSequence) {
            if (targetSequence == null || targetSequence.isEmpty()) {
                return true;
            }
            if (userHistory == null || userHistory.isEmpty()) {
                return false;
            }

            int targetIdx = 0;
            for (String action : userHistory) {
                // If current action matches next target, advance target
                if (targetIdx < targetSequence.size() && 
                    action.equals(targetSequence.get(targetIdx))) {
                    targetIdx++;
                }
                // If we've matched all targets, return true
                if (targetIdx == targetSequence.size()) {
                    return true;
                }
            }

            return targetIdx == targetSequence.size();
        }

        /**
         * Find all users who followed the target business flow
         */
        public static List<String> findUsersWithPattern(
            Map<String, List<String>> userHistories,
            List<String> targetSequence) {
            
            List<String> matchingUsers = new ArrayList<>();
            
            for (Map.Entry<String, List<String>> entry : userHistories.entrySet()) {
                String userId = entry.getKey();
                List<String> history = entry.getValue();
                
                if (matchesSubsequence(history, targetSequence)) {
                    matchingUsers.add(userId);
                }
            }
            
            return matchingUsers;
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 2: E-Commerce Conversion Funnel ===");

        // Part 1: Test floating-point parsing
        System.out.println("\n--- Part 1: Floating-Point Parsing ---");
        String[] timestamps = {"1234.567", "1000.0", "999.999"};
        for (String ts : timestamps) {
            double parsed = Part1_BugFix.parseTimestampSafely(ts);
            System.out.println("Parsed \"" + ts + "\" -> " + parsed);
        }
        double duration = Part1_BugFix.calculateDuration("1000.5", "1050.3");
        System.out.println("Duration from 1000.5 to 1050.3: " + duration);

        // Part 2: Test conversion funnel
        System.out.println("\n--- Part 2: Conversion Funnel Analysis ---");
        List<Part2_ConversionFunnel.ClickEvent> events = Arrays.asList(
            new Part2_ConversionFunnel.ClickEvent("user1", "BROWSE", 1000, "prod1"),
            new Part2_ConversionFunnel.ClickEvent("user1", "ADD_TO_CART", 1100, "prod1"),
            new Part2_ConversionFunnel.ClickEvent("user1", "PURCHASE", 1200, "prod1"),
            
            new Part2_ConversionFunnel.ClickEvent("user2", "BROWSE", 1050, "prod2"),
            new Part2_ConversionFunnel.ClickEvent("user2", "ADD_TO_CART", 1150, "prod2"),
            // user2 never purchases
            
            new Part2_ConversionFunnel.ClickEvent("user3", "BROWSE", 2000, "prod3")
            // user3 only browses
        );
        Part2_ConversionFunnel.FunnelResult funnelResult = 
            Part2_ConversionFunnel.analyzeConversionFunnel(events);
        System.out.println("Total conversions: " + funnelResult.totalConversions);
        System.out.println("Abandoned users: " + funnelResult.abandonedUserIds);

        // Part 3: Test subsequence matching
        System.out.println("\n--- Part 3: Subsequence Matching ---");
        Map<String, List<String>> userHistories = new HashMap<>();
        userHistories.put("user1", Arrays.asList("home", "pricing", "login", "checkout"));
        userHistories.put("user2", Arrays.asList("home", "logout", "pricing"));
        userHistories.put("user3", Arrays.asList("pricing", "home", "checkout"));
        
        List<String> targetSequence = Arrays.asList("home", "pricing", "checkout");
        System.out.println("Target sequence: " + targetSequence);
        System.out.println("User histories:" + userHistories);
        
        List<String> matchingUsers = 
            Part3_SubsequenceMatcher.findUsersWithPattern(userHistories, targetSequence);
        System.out.println("Users matching pattern: " + matchingUsers);
    }
}
