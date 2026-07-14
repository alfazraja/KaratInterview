package category1_state_machines;

import java.util.*;

/**
 * TEST SUITE for Problem 2: E-Commerce Conversion Funnel Tracker
 * 
 * Comprehensive test cases covering:
 * - Part 1: Floating-point parsing and precision
 * - Part 2: Conversion funnel tracking
 * - Part 3: Subsequence pattern matching
 */
public class Problem2_Tests {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    // ==================== PART 1: FLOATING-POINT PARSING TESTS ====================

    public static void testPart1_ParseInteger() {
        System.out.println("[TEST] Part 1: Parse integer timestamp");
        double result = Problem2_ECommerceConversionFunnel.Part1_BugFix.parseTimestampSafely("1000");
        boolean pass = result == 1000.0;
        assertTest("Part 1: Integer parsing", pass, "Result: " + result);
    }

    public static void testPart1_ParseDecimalWithPrecision() {
        System.out.println("[TEST] Part 1: Parse decimal with precision");
        double result = Problem2_ECommerceConversionFunnel.Part1_BugFix.parseTimestampSafely("1234.567");
        boolean pass = Math.abs(result - 1234.567) < 0.0001;
        assertTest("Part 1: Decimal parsing", pass, "Result: " + result);
    }

    public static void testPart1_ParseZero() {
        System.out.println("[TEST] Part 1: Parse zero");
        double result = Problem2_ECommerceConversionFunnel.Part1_BugFix.parseTimestampSafely("0.0");
        boolean pass = result == 0.0;
        assertTest("Part 1: Zero parsing", pass, "Result: " + result);
    }

    public static void testPart1_ParseNegativeNumber() {
        System.out.println("[TEST] Part 1: Parse negative number");
        double result = Problem2_ECommerceConversionFunnel.Part1_BugFix.parseTimestampSafely("-100.5");
        boolean pass = result == -100.5;
        assertTest("Part 1: Negative number parsing", pass, "Result: " + result);
    }

    public static void testPart1_ParseInvalidThrowsException() {
        System.out.println("[TEST] Part 1: Invalid string throws exception");
        try {
            Problem2_ECommerceConversionFunnel.Part1_BugFix.parseTimestampSafely("invalid");
            assertTest("Part 1: Invalid input exception", false, "No exception thrown");
        } catch (IllegalArgumentException e) {
            assertTest("Part 1: Invalid input exception", true, "Exception: " + e.getMessage());
        }
    }

    public static void testPart1_ParseNullThrowsException() {
        System.out.println("[TEST] Part 1: Null input throws exception");
        try {
            Problem2_ECommerceConversionFunnel.Part1_BugFix.parseTimestampSafely(null);
            assertTest("Part 1: Null input exception", false, "No exception thrown");
        } catch (IllegalArgumentException e) {
            assertTest("Part 1: Null input exception", true, "Exception caught");
        }
    }

    public static void testPart1_CalculateDuration() {
        System.out.println("[TEST] Part 1: Calculate duration between timestamps");
        double duration = Problem2_ECommerceConversionFunnel.Part1_BugFix.calculateDuration("1000.5", "1050.3");
        boolean pass = Math.abs(duration - 49.8) < 0.01;
        assertTest("Part 1: Duration calculation", pass, "Duration: " + duration);
    }

    public static void testPart1_CalculateDurationReverse() {
        System.out.println("[TEST] Part 1: Calculate duration (reversed order)");
        double duration = Problem2_ECommerceConversionFunnel.Part1_BugFix.calculateDuration("1050.3", "1000.5");
        boolean pass = Math.abs(duration - 49.8) < 0.01; // Should use absolute value
        assertTest("Part 1: Duration (reverse)", pass, "Duration: " + duration);
    }

    // ==================== PART 2: CONVERSION FUNNEL TESTS ====================

    public static void testPart2_CompleteConversion() {
        System.out.println("\n[TEST] Part 2: Complete conversion funnel");
        List<Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent> events = Arrays.asList(
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "BROWSE", 1000, "prod1"),
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "ADD_TO_CART", 1100, "prod1"),
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "PURCHASE", 1200, "prod1")
        );
        
        Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.FunnelResult result = 
            Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.analyzeConversionFunnel(events);
        
        boolean pass = result.totalConversions == 1 && result.abandonedUserIds.isEmpty();
        assertTest("Part 2: Complete conversion", pass, 
            "Conversions: " + result.totalConversions + ", Abandoned: " + result.abandonedUserIds);
    }

    public static void testPart2_AbandonedAtAddToCart() {
        System.out.println("[TEST] Part 2: Abandoned at ADD_TO_CART stage");
        List<Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent> events = Arrays.asList(
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "BROWSE", 1000, "prod1"),
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "ADD_TO_CART", 1100, "prod1")
            // No PURCHASE
        );
        
        Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.FunnelResult result = 
            Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.analyzeConversionFunnel(events);
        
        boolean pass = result.totalConversions == 0 && result.abandonedUserIds.contains("user1");
        assertTest("Part 2: Abandoned at ADD_TO_CART", pass, 
            "Conversions: " + result.totalConversions + ", Abandoned: " + result.abandonedUserIds);
    }

    public static void testPart2_AbandonedAtBrowse() {
        System.out.println("[TEST] Part 2: Abandoned at BROWSE stage");
        List<Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent> events = Arrays.asList(
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "BROWSE", 1000, "prod1")
            // No ADD_TO_CART or PURCHASE
        );
        
        Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.FunnelResult result = 
            Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.analyzeConversionFunnel(events);
        
        boolean pass = result.totalConversions == 0 && result.abandonedUserIds.contains("user1");
        assertTest("Part 2: Abandoned at BROWSE", pass, "Abandoned users: " + result.abandonedUserIds);
    }

    public static void testPart2_MultipleUsers() {
        System.out.println("[TEST] Part 2: Multiple users with mixed outcomes");
        List<Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent> events = Arrays.asList(
            // User 1: Converts
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "BROWSE", 1000, "prod1"),
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "ADD_TO_CART", 1100, "prod1"),
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "PURCHASE", 1200, "prod1"),
            
            // User 2: Abandons
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user2", "BROWSE", 1050, "prod2"),
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user2", "ADD_TO_CART", 1150, "prod2")
        );
        
        Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.FunnelResult result = 
            Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.analyzeConversionFunnel(events);
        
        boolean pass = result.totalConversions == 1 && result.abandonedUserIds.contains("user2");
        assertTest("Part 2: Multiple users", pass, 
            "Conversions: " + result.totalConversions + ", Abandoned: " + result.abandonedUserIds);
    }

    public static void testPart2_OutOfOrderEvents() {
        System.out.println("[TEST] Part 2: Out-of-order events (should sort)");
        List<Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent> events = Arrays.asList(
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "PURCHASE", 1200, "prod1"),
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "BROWSE", 1000, "prod1"),
            new Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent("user1", "ADD_TO_CART", 1100, "prod1")
        );
        
        Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.FunnelResult result = 
            Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.analyzeConversionFunnel(events);
        
        boolean pass = result.totalConversions == 1; // Should still count as conversion after sorting
        assertTest("Part 2: Out-of-order events", pass, "Conversions: " + result.totalConversions);
    }

    public static void testPart2_EmptyInput() {
        System.out.println("[TEST] Part 2: Empty event list");
        List<Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.ClickEvent> events = new ArrayList<>();
        
        Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.FunnelResult result = 
            Problem2_ECommerceConversionFunnel.Part2_ConversionFunnel.analyzeConversionFunnel(events);
        
        boolean pass = result.totalConversions == 0 && result.abandonedUserIds.isEmpty();
        assertTest("Part 2: Empty input", pass, "Result: empty");
    }

    // ==================== PART 3: SUBSEQUENCE MATCHING TESTS ====================

    public static void testPart3_MatchesPattern() {
        System.out.println("\n[TEST] Part 3: History matches target pattern");
        List<String> history = Arrays.asList("home", "pricing", "login", "checkout");
        List<String> target = Arrays.asList("home", "pricing", "checkout");
        
        boolean result = Problem2_ECommerceConversionFunnel.Part3_SubsequenceMatcher.matchesSubsequence(history, target);
        
        assertTest("Part 3: Pattern match (with gap)", result, 
            "History: " + history + ", Target: " + target);
    }

    public static void testPart3_DoesNotMatchPattern() {
        System.out.println("[TEST] Part 3: History does NOT match target pattern");
        List<String> history = Arrays.asList("pricing", "home", "checkout");
        List<String> target = Arrays.asList("home", "pricing", "checkout");
        
        boolean result = Problem2_ECommerceConversionFunnel.Part3_SubsequenceMatcher.matchesSubsequence(history, target);
        
        assertTest("Part 3: Pattern mismatch", !result, 
            "History: " + history + ", Target: " + target);
    }

    public static void testPart3_ExactMatch() {
        System.out.println("[TEST] Part 3: Exact pattern match (no gaps)");
        List<String> history = Arrays.asList("home", "pricing", "checkout");
        List<String> target = Arrays.asList("home", "pricing", "checkout");
        
        boolean result = Problem2_ECommerceConversionFunnel.Part3_SubsequenceMatcher.matchesSubsequence(history, target);
        
        assertTest("Part 3: Exact match", result, "History equals target");
    }

    public static void testPart3_MultipleGaps() {
        System.out.println("[TEST] Part 3: Multiple gaps in pattern");
        List<String> history = Arrays.asList("home", "search", "pricing", "contact", "checkout");
        List<String> target = Arrays.asList("home", "pricing", "checkout");
        
        boolean result = Problem2_ECommerceConversionFunnel.Part3_SubsequenceMatcher.matchesSubsequence(history, target);
        
        assertTest("Part 3: Multiple gaps", result, "Successfully matched with gaps");
    }

    public static void testPart3_PartialMatch() {
        System.out.println("[TEST] Part 3: Partial match only");
        List<String> history = Arrays.asList("home", "pricing");
        List<String> target = Arrays.asList("home", "pricing", "checkout");
        
        boolean result = Problem2_ECommerceConversionFunnel.Part3_SubsequenceMatcher.matchesSubsequence(history, target);
        
        assertTest("Part 3: Partial match", !result, "Incomplete pattern");
    }

    public static void testPart3_EmptyHistory() {
        System.out.println("[TEST] Part 3: Empty history");
        List<String> history = new ArrayList<>();
        List<String> target = Arrays.asList("home", "pricing");
        
        boolean result = Problem2_ECommerceConversionFunnel.Part3_SubsequenceMatcher.matchesSubsequence(history, target);
        
        assertTest("Part 3: Empty history", !result, "No match possible");
    }

    public static void testPart3_EmptyTarget() {
        System.out.println("[TEST] Part 3: Empty target sequence");
        List<String> history = Arrays.asList("home", "pricing");
        List<String> target = new ArrayList<>();
        
        boolean result = Problem2_ECommerceConversionFunnel.Part3_SubsequenceMatcher.matchesSubsequence(history, target);
        
        assertTest("Part 3: Empty target", result, "Empty target always matches");
    }

    public static void testPart3_FindUsersWithPattern() {
        System.out.println("[TEST] Part 3: Find users matching pattern");
        Map<String, List<String>> userHistories = new HashMap<>();
        userHistories.put("user1", Arrays.asList("home", "pricing", "login", "checkout"));
        userHistories.put("user2", Arrays.asList("home", "logout", "pricing"));
        userHistories.put("user3", Arrays.asList("pricing", "home", "checkout"));
        
        List<String> targetSequence = Arrays.asList("home", "pricing", "checkout");
        List<String> matchingUsers = 
            Problem2_ECommerceConversionFunnel.Part3_SubsequenceMatcher.findUsersWithPattern(userHistories, targetSequence);
        
        boolean pass = matchingUsers.contains("user1") && !matchingUsers.contains("user3");
        assertTest("Part 3: Find matching users", pass, "Matching users: " + matchingUsers);
    }

    // ==================== UTILITY METHODS ====================

    private static void assertTest(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("  ✓ PASSED: " + testName);
            testsPassed++;
        } else {
            System.out.println("  ✗ FAILED: " + testName);
            System.out.println("    Details: " + details);
            testsFailed++;
        }
    }

    private static void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST SUMMARY - Problem 2");
        System.out.println("=".repeat(60));
        System.out.println("Passed: " + testsPassed);
        System.out.println("Failed: " + testsFailed);
        System.out.println("Total:  " + (testsPassed + testsFailed));
        System.out.println("Success Rate: " + String.format("%.1f%%", 
            (testsPassed * 100.0 / (testsPassed + testsFailed))));
        System.out.println("=".repeat(60));
    }

    // ==================== MAIN ====================

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PROBLEM 2: E-COMMERCE CONVERSION FUNNEL - TEST SUITE");
        System.out.println("=".repeat(60));

        // Part 1 Tests
        System.out.println("\n>>> PART 1: FLOATING-POINT PARSING <<<");
        testPart1_ParseInteger();
        testPart1_ParseDecimalWithPrecision();
        testPart1_ParseZero();
        testPart1_ParseNegativeNumber();
        testPart1_ParseInvalidThrowsException();
        testPart1_ParseNullThrowsException();
        testPart1_CalculateDuration();
        testPart1_CalculateDurationReverse();

        // Part 2 Tests
        System.out.println("\n>>> PART 2: CONVERSION FUNNEL <<<");
        testPart2_CompleteConversion();
        testPart2_AbandonedAtAddToCart();
        testPart2_AbandonedAtBrowse();
        testPart2_MultipleUsers();
        testPart2_OutOfOrderEvents();
        testPart2_EmptyInput();

        // Part 3 Tests
        System.out.println("\n>>> PART 3: SUBSEQUENCE MATCHING <<<");
        testPart3_MatchesPattern();
        testPart3_DoesNotMatchPattern();
        testPart3_ExactMatch();
        testPart3_MultipleGaps();
        testPart3_PartialMatch();
        testPart3_EmptyHistory();
        testPart3_EmptyTarget();
        testPart3_FindUsersWithPattern();

        printSummary();
    }
}
