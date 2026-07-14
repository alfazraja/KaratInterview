package category1_state_machines;

import java.util.*;

/**
 * TEST SUITE for Problem 3: Food Delivery Driver Pipeline
 * 
 * Comprehensive test cases covering:
 * - Part 1: Timezone normalization
 * - Part 2: Delivery pipeline state machine
 * - Part 3: Longest wait time calculation
 */
public class Problem3_Tests {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    // ==================== PART 1: TIMEZONE NORMALIZATION TESTS ====================

    public static void testPart1_NormalizeInteger() {
        System.out.println("[TEST] Part 1: Normalize integer timestamp");
        long result = Problem3_FoodDeliveryDriverPipeline.Part1_TimezoneFix.normalizeToEpoch("1000", "UTC");
        boolean pass = result == 1000L;
        assertTest("Part 1: Integer normalization", pass, "Result: " + result);
    }

    public static void testPart1_NormalizeDecimal() {
        System.out.println("[TEST] Part 1: Normalize decimal timestamp");
        long result = Problem3_FoodDeliveryDriverPipeline.Part1_TimezoneFix.normalizeToEpoch("1234.567", "UTC");
        boolean pass = result == 1235L; // Rounded
        assertTest("Part 1: Decimal normalization", pass, "Result: " + result);
    }

    public static void testPart1_NormalizeZero() {
        System.out.println("[TEST] Part 1: Normalize zero");
        long result = Problem3_FoodDeliveryDriverPipeline.Part1_TimezoneFix.normalizeToEpoch("0.0", "UTC");
        boolean pass = result == 0L;
        assertTest("Part 1: Zero normalization", pass, "Result: " + result);
    }

    public static void testPart1_NormalizeMultipleTimestamps() {
        System.out.println("[TEST] Part 1: Normalize multiple timestamps");
        List<String> timestamps = Arrays.asList("1000", "1050.5", "1100.9");
        List<Long> result = Problem3_FoodDeliveryDriverPipeline.Part1_TimezoneFix.normalizeTimestamps(timestamps);
        
        boolean pass = result.size() == 3 && result.get(0) == 1000L && result.get(1) == 1051L && result.get(2) == 1101L;
        assertTest("Part 1: Multiple timestamps", pass, "Result: " + result);
    }

    public static void testPart1_InvalidTimestampThrows() {
        System.out.println("[TEST] Part 1: Invalid timestamp throws exception");
        try {
            Problem3_FoodDeliveryDriverPipeline.Part1_TimezoneFix.normalizeToEpoch("invalid", "UTC");
            assertTest("Part 1: Invalid input exception", false, "No exception thrown");
        } catch (IllegalArgumentException e) {
            assertTest("Part 1: Invalid input exception", true, "Exception caught");
        }
    }

    // ==================== PART 2: DELIVERY PIPELINE TESTS ====================

    public static void testPart2_CompletePipeline() {
        System.out.println("\n[TEST] Part 2: Complete delivery pipeline");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "ACCEPTED", 1000, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1100, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "DELIVERED", 1200, "customer_1")
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.PipelineAnalysis result = 
            Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.analyzePipeline(events);
        
        boolean pass = result.pickedUpButNotDelivered.isEmpty() && result.orderDurations.containsKey("order1");
        assertTest("Part 2: Complete pipeline", pass, 
            "Picked up not delivered: " + result.pickedUpButNotDelivered + 
            "\nDurations: " + result.orderDurations);
    }

    public static void testPart2_PickedUpNotDelivered() {
        System.out.println("[TEST] Part 2: Picked up but not delivered anomaly");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "ACCEPTED", 1000, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1100, "restaurant_A")
            // Never delivered!
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.PipelineAnalysis result = 
            Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.analyzePipeline(events);
        
        boolean pass = result.pickedUpButNotDelivered.contains("order1");
        assertTest("Part 2: Picked up not delivered", pass, 
            "Anomaly orders: " + result.pickedUpButNotDelivered);
    }

    public static void testPart2_NeverAccepted() {
        System.out.println("[TEST] Part 2: Order never accepted");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1100, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "DELIVERED", 1200, "customer_1")
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.PipelineAnalysis result = 
            Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.analyzePipeline(events);
        
        // Should still be tracked, duration may be from pickup to delivery
        boolean pass = result.orderStatuses.containsKey("order1");
        assertTest("Part 2: Order without acceptance", pass, "Tracked order: order1");
    }

    public static void testPart2_MultipleOrders() {
        System.out.println("[TEST] Part 2: Multiple orders with mixed states");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            // Order 1: Complete
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "ACCEPTED", 1000, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1100, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "DELIVERED", 1200, "customer_1"),
            
            // Order 2: Incomplete
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order2", "driver2", "ACCEPTED", 1050, "restaurant_B"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order2", "driver2", "PICKED_UP", 1150, "restaurant_B")
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.PipelineAnalysis result = 
            Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.analyzePipeline(events);
        
        boolean pass = result.pickedUpButNotDelivered.contains("order2") && result.orderStatuses.containsKey("order1");
        assertTest("Part 2: Multiple orders", pass, 
            "Incomplete: " + result.pickedUpButNotDelivered + ", Complete: " + result.orderStatuses.keySet());
    }

    public static void testPart2_OutOfOrderEvents() {
        System.out.println("[TEST] Part 2: Out-of-order events (should sort)");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "DELIVERED", 1200, "customer_1"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "ACCEPTED", 1000, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1100, "restaurant_A")
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.PipelineAnalysis result = 
            Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.analyzePipeline(events);
        
        boolean pass = result.pickedUpButNotDelivered.isEmpty() && result.orderStatuses.containsKey("order1");
        assertTest("Part 2: Out-of-order sorting", pass, "Order completed after sorting");
    }

    public static void testPart2_DurationCalculation() {
        System.out.println("[TEST] Part 2: Duration calculation");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "ACCEPTED", 1000, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1100, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "DELIVERED", 1500, "customer_1")
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.PipelineAnalysis result = 
            Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.analyzePipeline(events);
        
        boolean pass = result.orderDurations.get("order1") == 500; // 1500 - 1000
        assertTest("Part 2: Duration calculation", pass, 
            "Duration: " + result.orderDurations.get("order1") + " (expected: 500)");
    }

    // ==================== PART 3: LONGEST WAIT TIME TESTS ====================

    public static void testPart3_FindLongestWait() {
        System.out.println("\n[TEST] Part 3: Find longest wait at restaurant");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1000, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "DELIVERED", 1050, "customer_1"),
            
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order2", "driver2", "PICKED_UP", 2000, "restaurant_B"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order2", "driver2", "DELIVERED", 2500, "customer_2") // 500 second wait
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.WaitDuration longestWait = 
            Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.findLongestWait(events);
        
        boolean pass = longestWait != null && longestWait.driverId.equals("driver2") && longestWait.waitSeconds == 500;
        assertTest("Part 3: Longest wait detection", pass, "Longest wait: " + longestWait);
    }

    public static void testPart3_SingleOrderWait() {
        System.out.println("[TEST] Part 3: Single order wait time");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1000, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "DELIVERED", 1300, "customer_1")
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.WaitDuration longestWait = 
            Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.findLongestWait(events);
        
        boolean pass = longestWait != null && longestWait.waitSeconds == 300;
        assertTest("Part 3: Single order wait", pass, "Wait: " + longestWait.waitSeconds + " seconds");
    }

    public static void testPart3_NoWaitData() {
        System.out.println("[TEST] Part 3: No pickup/delivery pairs");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "ACCEPTED", 1000, "restaurant_A")
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.WaitDuration longestWait = 
            Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.findLongestWait(events);
        
        boolean pass = longestWait == null;
        assertTest("Part 3: No wait data", pass, "Result: null");
    }

    public static void testPart3_EmptyInput() {
        System.out.println("[TEST] Part 3: Empty event list");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = new ArrayList<>();
        
        Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.WaitDuration longestWait = 
            Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.findLongestWait(events);
        
        boolean pass = longestWait == null;
        assertTest("Part 3: Empty input", pass, "Result: null");
    }

    public static void testPart3_MultipleDrivers() {
        System.out.println("[TEST] Part 3: Multiple drivers comparison");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            // Driver 1: 200s wait
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1000, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "DELIVERED", 1200, "customer_1"),
            
            // Driver 2: 500s wait (longest)
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order2", "driver2", "PICKED_UP", 2000, "restaurant_B"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order2", "driver2", "DELIVERED", 2500, "customer_2"),
            
            // Driver 3: 100s wait
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order3", "driver3", "PICKED_UP", 3000, "restaurant_C"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order3", "driver3", "DELIVERED", 3100, "customer_3")
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.WaitDuration longestWait = 
            Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.findLongestWait(events);
        
        boolean pass = longestWait != null && longestWait.driverId.equals("driver2") && longestWait.waitSeconds == 500;
        assertTest("Part 3: Multiple drivers", pass, "Longest wait driver: " + longestWait.driverId);
    }

    public static void testPart3_WaitDurationCalculation() {
        System.out.println("[TEST] Part 3: Wait duration calculation in minutes");
        List<Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent> events = Arrays.asList(
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "PICKED_UP", 1000, "restaurant_A"),
            new Problem3_FoodDeliveryDriverPipeline.Part2_DriverStateMachine.DeliveryEvent(
                "order1", "driver1", "DELIVERED", 1600, "customer_1") // 600 seconds = 10 minutes
        );
        
        Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.WaitDuration longestWait = 
            Problem3_FoodDeliveryDriverPipeline.Part3_LongestWait.findLongestWait(events);
        
        boolean pass = longestWait != null && longestWait.waitSeconds == 600 && 
                      Math.abs(longestWait.waitSeconds / 60.0 - 10.0) < 0.1;
        assertTest("Part 3: Duration in minutes", pass, "Duration: " + (longestWait.waitSeconds / 60.0) + " minutes");
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
        System.out.println("TEST SUMMARY - Problem 3");
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
        System.out.println("PROBLEM 3: FOOD DELIVERY DRIVER PIPELINE - TEST SUITE");
        System.out.println("=".repeat(60));

        // Part 1 Tests
        System.out.println("\n>>> PART 1: TIMEZONE NORMALIZATION <<<");
        testPart1_NormalizeInteger();
        testPart1_NormalizeDecimal();
        testPart1_NormalizeZero();
        testPart1_NormalizeMultipleTimestamps();
        testPart1_InvalidTimestampThrows();

        // Part 2 Tests
        System.out.println("\n>>> PART 2: DELIVERY PIPELINE <<<");
        testPart2_CompletePipeline();
        testPart2_PickedUpNotDelivered();
        testPart2_NeverAccepted();
        testPart2_MultipleOrders();
        testPart2_OutOfOrderEvents();
        testPart2_DurationCalculation();

        // Part 3 Tests
        System.out.println("\n>>> PART 3: LONGEST WAIT TIME <<<");
        testPart3_FindLongestWait();
        testPart3_SingleOrderWait();
        testPart3_NoWaitData();
        testPart3_EmptyInput();
        testPart3_MultipleDrivers();
        testPart3_WaitDurationCalculation();

        printSummary();
    }
}
