package category1_state_machines;

import java.util.*;

/**
 * TEST SUITE for Problem 1: The Key-Card Badge Access System
 * 
 * Comprehensive test cases covering:
 * - Part 1: Data parsing and normalization
 * - Part 2: State machine and anomaly detection
 * - Part 3: Fraud detection using sliding window
 */
public class Problem1_Tests {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    // ==================== PART 1: DATA PARSING TESTS ====================

    public static void testPart1_CleaningSimpleData() {
        System.out.println("\n[TEST] Part 1: Cleaning simple data with whitespace");
        List<String> input = Arrays.asList(
            "John, 0900, enter ",
            "  alice, 0910, exit  ",
            "BOB, 0920, ENTER"
        );
        List<String> result = Problem1_KeyCardBadgeAccessSystem.Part1_BugFix.cleanAccessLogs(input);
        
        boolean pass = result.get(0).equals("JOHN, 0900, ENTER") &&
                      result.get(1).equals("ALICE, 0910, EXIT") &&
                      result.get(2).equals("BOB, 0920, ENTER");
        
        assertTest("Part 1: Simple data cleaning", pass, 
            "Input: " + input + "\nOutput: " + result);
    }

    public static void testPart1_EmptyAndNullHandling() {
        System.out.println("[TEST] Part 1: Empty string handling");
        List<String> input = Arrays.asList(
            "  ",
            "alice, 1000, enter  "
        );
        List<String> result = Problem1_KeyCardBadgeAccessSystem.Part1_BugFix.cleanAccessLogs(input);
        
        boolean pass = !result.get(0).isEmpty() && result.get(1).equals("ALICE, 1000, ENTER");
        assertTest("Part 1: Empty string handling", pass, "Result: " + result);
    }

    public static void testPart1_MixedCaseNormalization() {
        System.out.println("[TEST] Part 1: Mixed case normalization");
        List<String> input = Arrays.asList(
            "JoHn, 0900, EnTeR ",
            "aLiCe, 0910, ExIt  "
        );
        List<String> result = Problem1_KeyCardBadgeAccessSystem.Part1_BugFix.cleanAccessLogs(input);
        
        boolean allUppercase = result.stream().allMatch(s -> s.equals(s.toUpperCase()));
        assertTest("Part 1: Case normalization", allUppercase, "Result: " + result);
    }

    // ==================== PART 2: STATE MACHINE TESTS ====================

    public static void testPart2_NormalEntryExit() {
        System.out.println("\n[TEST] Part 2: Normal entry and exit sequence");
        List<Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord> records = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1000, "ENTER", "room1"),
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1100, "EXIT", "room1")
        );
        
        Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AnomalyResult result = 
            Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.detectAnomalies(records);
        
        boolean pass = result.exitedWithoutEntry.isEmpty() && result.enteredWithoutExit.isEmpty();
        assertTest("Part 2: Normal entry/exit", pass, 
            "Exited without entry: " + result.exitedWithoutEntry + 
            "\nEntered without exit: " + result.enteredWithoutExit);
    }

    public static void testPart2_ExitWithoutEntry() {
        System.out.println("[TEST] Part 2: Exit without prior entry");
        List<Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord> records = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1000, "EXIT", "room1"),
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user2", 1100, "ENTER", "room2"),
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user2", 1200, "EXIT", "room2")
        );
        
        Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AnomalyResult result = 
            Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.detectAnomalies(records);
        
        boolean pass = result.exitedWithoutEntry.contains("user1");
        assertTest("Part 2: Exit without entry detection", pass, 
            "Anomaly users: " + result.exitedWithoutEntry);
    }

    public static void testPart2_EntryWithoutExit() {
        System.out.println("[TEST] Part 2: Entry without exit");
        List<Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord> records = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1000, "ENTER", "room1"),
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user2", 1100, "ENTER", "room2"),
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user2", 1200, "EXIT", "room2")
        );
        
        Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AnomalyResult result = 
            Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.detectAnomalies(records);
        
        boolean pass = result.enteredWithoutExit.contains("user1");
        assertTest("Part 2: Entry without exit detection", pass, 
            "Users still inside: " + result.enteredWithoutExit);
    }

    public static void testPart2_MultipleRooms() {
        System.out.println("[TEST] Part 2: User entering multiple rooms");
        List<Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord> records = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1000, "ENTER", "room1"),
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1100, "ENTER", "room2"),
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1200, "EXIT", "room1"),
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1300, "EXIT", "room2")
        );
        
        Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AnomalyResult result = 
            Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.detectAnomalies(records);
        
        boolean pass = result.exitedWithoutEntry.isEmpty() && result.enteredWithoutExit.isEmpty();
        assertTest("Part 2: Multiple rooms handling", pass, "Result anomalies: " + pass);
    }

    public static void testPart2_OutOfOrderTimestamps() {
        System.out.println("[TEST] Part 2: Out-of-order timestamps (should sort)");
        List<Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord> records = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1200, "EXIT", "room1"),
            new Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AccessRecord("user1", 1000, "ENTER", "room1")
        );
        
        Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.AnomalyResult result = 
            Problem1_KeyCardBadgeAccessSystem.Part2_StateMachine.detectAnomalies(records);
        
        // After sorting, entry comes before exit, so no anomaly
        boolean pass = result.exitedWithoutEntry.isEmpty() && result.enteredWithoutExit.isEmpty();
        assertTest("Part 2: Out-of-order timestamp handling", pass, "Anomalies: " + pass);
    }

    // ==================== PART 3: FRAUD DETECTION TESTS ====================

    public static void testPart3_SingleBadgeNoFraud() {
        System.out.println("\n[TEST] Part 3: Single badge - no fraud");
        List<Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe> swipes = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1000, "badge1"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1100, "badge1"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 2000, "badge1")
        );
        
        List<String> fraudUsers = Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.detectCardSharingFraud(swipes);
        
        boolean pass = fraudUsers.isEmpty();
        assertTest("Part 3: Single badge - no fraud", pass, "Fraud users: " + fraudUsers);
    }

    public static void testPart3_MultipleSwipesWithinWindow() {
        System.out.println("[TEST] Part 3: Multiple different badges within 60-min window");
        List<Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe> swipes = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1000, "badge1"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1200, "badge2"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1400, "badge3") // within 60 min (1400-1000=400s < 3600s)
        );
        
        List<String> fraudUsers = Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.detectCardSharingFraud(swipes);
        
        boolean pass = fraudUsers.contains("user1");
        assertTest("Part 3: Fraud detection within window", pass, "Fraud users: " + fraudUsers);
    }

    public static void testPart3_SwipesOutsideWindow() {
        System.out.println("[TEST] Part 3: Multiple badges outside 60-min window");
        long window = 60 * 60; // 3600 seconds
        List<Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe> swipes = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1000, "badge1"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1200, "badge2"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1000 + window + 100, "badge3") // outside window
        );
        
        List<String> fraudUsers = Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.detectCardSharingFraud(swipes);
        
        boolean pass = !fraudUsers.contains("user1");
        assertTest("Part 3: No fraud outside window", pass, "Fraud users: " + fraudUsers);
    }

    public static void testPart3_MultipleUsers() {
        System.out.println("[TEST] Part 3: Multiple users - selective fraud detection");
        List<Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe> swipes = Arrays.asList(
            // User 1: Fraud
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1000, "badge1"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1200, "badge2"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1400, "badge3"),
            
            // User 2: No fraud
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user2", 2000, "badge4"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user2", 2100, "badge4")
        );
        
        List<String> fraudUsers = Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.detectCardSharingFraud(swipes);
        
        boolean pass = fraudUsers.contains("user1") && !fraudUsers.contains("user2");
        assertTest("Part 3: Selective fraud detection", pass, "Fraud users: " + fraudUsers);
    }

    public static void testPart3_ExactThreshold() {
        System.out.println("[TEST] Part 3: Exactly 3 badges at threshold");
        List<Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe> swipes = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1000, "badge1"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1200, "badge2"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1400, "badge3")
        );
        
        List<String> fraudUsers = Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.detectCardSharingFraud(swipes);
        
        boolean pass = fraudUsers.contains("user1"); // Should detect at exactly 3
        assertTest("Part 3: Threshold boundary (3 badges)", pass, "Fraud users: " + fraudUsers);
    }

    public static void testPart3_EmptyInput() {
        System.out.println("[TEST] Part 3: Empty input handling");
        List<Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe> swipes = new ArrayList<>();
        
        List<String> fraudUsers = Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.detectCardSharingFraud(swipes);
        
        boolean pass = fraudUsers.isEmpty();
        assertTest("Part 3: Empty input", pass, "Result: " + fraudUsers);
    }

    public static void testPart3_LessThanThreshold() {
        System.out.println("[TEST] Part 3: Less than 3 badges - no fraud");
        List<Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe> swipes = Arrays.asList(
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1000, "badge1"),
            new Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.BadgeSwipe("user1", 1200, "badge2")
        );
        
        List<String> fraudUsers = Problem1_KeyCardBadgeAccessSystem.Part3_FraudDetection.detectCardSharingFraud(swipes);
        
        boolean pass = fraudUsers.isEmpty();
        assertTest("Part 3: Below threshold", pass, "Fraud users: " + fraudUsers);
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
        System.out.println("TEST SUMMARY - Problem 1");
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
        System.out.println("PROBLEM 1: KEY-CARD BADGE ACCESS SYSTEM - TEST SUITE");
        System.out.println("=".repeat(60));

        // Part 1 Tests
        System.out.println("\n>>> PART 1: DATA PARSING <<<");
        testPart1_CleaningSimpleData();
        testPart1_EmptyAndNullHandling();
        testPart1_MixedCaseNormalization();

        // Part 2 Tests
        System.out.println("\n>>> PART 2: STATE MACHINE <<<");
        testPart2_NormalEntryExit();
        testPart2_ExitWithoutEntry();
        testPart2_EntryWithoutExit();
        testPart2_MultipleRooms();
        testPart2_OutOfOrderTimestamps();

        // Part 3 Tests
        System.out.println("\n>>> PART 3: FRAUD DETECTION <<<");
        testPart3_SingleBadgeNoFraud();
        testPart3_MultipleSwipesWithinWindow();
        testPart3_SwipesOutsideWindow();
        testPart3_MultipleUsers();
        testPart3_ExactThreshold();
        testPart3_EmptyInput();
        testPart3_LessThanThreshold();

        printSummary();
    }
}
