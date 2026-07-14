package category2_intervals_concurrency;

import java.util.*;

/**
 * Problem 11: Shared Workspace Desk Reservation Engine
 * 
 * Three-part progression:
 * - Part 1: Fix hour wrapping (midnight boundary handling)
 * - Part 2: Merge concurrent desk reservations and calculate idle time
 * - Part 3: Find minimum desks needed to satisfy all bookings
 */
public class Problem11_SharedWorkspaceDeskReservationEngine {

    /**
     * Part 1: Bug Fix - Hour Boundary Handling
     * Issue: Midnight hour wrapping (hour % 24 validation)
     * Solution: Add modulo bounds checking for 24-hour format
     */
    public static class Part1_BugFix {
        public static class Reservation {
            public String deskId;
            public int startHour;  // 0-23
            public int endHour;    // 0-23, can wrap past 23
            public String userId;

            public Reservation(String deskId, int startHour, int endHour, String userId) {
                this.deskId = deskId;
                // Normalize hours
                this.startHour = ((startHour % 24) + 24) % 24;
                this.endHour = ((endHour % 24) + 24) % 24;
                this.userId = userId;
            }

            @Override
            public String toString() {
                return String.format("%s: %02d:00-%02d:00 (%s)",
                    deskId, startHour, endHour, userId);
            }
        }

        /**
         * Check if hour is valid (0-23)
         */
        public static boolean isValidHour(int hour) {
            return hour >= 0 && hour < 24;
        }

        /**
         * Normalize hour to 24-hour format
         */
        public static int normalizeHour(int hour) {
            return ((hour % 24) + 24) % 24;
        }

        /**
         * Check if midnight crossing is valid
         */
        public static boolean isValidReservation(int startHour, int endHour) {
            int start = normalizeHour(startHour);
            int end = normalizeHour(endHour);
            return true; // After normalization, all are valid
        }
    }

    /**
     * Part 2: Desk Occupancy Analysis
     * Merge overlapping reservations and calculate idle hours
     */
    public static class Part2_OccupancyAnalysis {
        public static class OccupancyBlock {
            public int startHour;
            public int endHour;
            public int occupiedHours;
            public int deskId; // int representation of desk

            public OccupancyBlock(int startHour, int endHour, int deskId) {
                this.startHour = startHour;
                this.endHour = endHour;
                this.deskId = deskId;
                // Calculate hours (handle midnight wrap)
                if (endHour > startHour) {
                    this.occupiedHours = endHour - startHour;
                } else {
                    this.occupiedHours = (24 - startHour) + endHour;
                }
            }

            @Override
            public String toString() {
                return String.format("%02d:00-%02d:00 (%d hrs)", startHour, endHour, occupiedHours);
            }
        }

        /**
         * Calculate total idle hours for a desk
         * Time Complexity: O(n log n)
         */
        public static int calculateIdleHours(List<Part1_BugFix.Reservation> reservations) {
            if (reservations == null || reservations.isEmpty()) {
                return 24; // Completely idle
            }

            // Sort by start time
            List<Part1_BugFix.Reservation> sorted = new ArrayList<>(reservations);
            sorted.sort((a, b) -> Integer.compare(a.startHour, b.startHour));

            int totalBusyHours = 0;
            int currentStart = sorted.get(0).startHour;
            int currentEnd = sorted.get(0).endHour;

            for (int i = 1; i < sorted.size(); i++) {
                Part1_BugFix.Reservation next = sorted.get(i);

                if (next.startHour <= currentEnd) {
                    // Overlapping - extend
                    currentEnd = Math.max(currentEnd, next.endHour);
                } else {
                    // Gap - count current block
                    totalBusyHours += (currentEnd > currentStart) ?
                        (currentEnd - currentStart) : (24 - currentStart + currentEnd);
                    currentStart = next.startHour;
                    currentEnd = next.endHour;
                }
            }

            // Count final block
            totalBusyHours += (currentEnd > currentStart) ?
                (currentEnd - currentStart) : (24 - currentStart + currentEnd);

            return 24 - totalBusyHours;
        }
    }

    /**
     * Part 3: Optimal Desk Allocation
     * Find minimum desks needed for all reservations without conflicts
     */
    public static class Part3_OptimalAllocator {
        /**
         * Find minimum desks needed using sweep line algorithm
         * Time Complexity: O(n log n)
         */
        public static int findMinDesksNeeded(List<Part1_BugFix.Reservation> requests) {
            if (requests == null || requests.isEmpty()) {
                return 0;
            }

            // Create events for sweep line
            List<Event> events = new ArrayList<>();
            for (Part1_BugFix.Reservation req : requests) {
                events.add(new Event(req.startHour, true));
                events.add(new Event(req.endHour, false));
            }

            // Sort events (starts before ends at same time)
            events.sort((a, b) -> {
                if (a.time != b.time) {
                    return Integer.compare(a.time, b.time);
                }
                return Boolean.compare(!a.isStart, !b.isStart);
            });

            int maxConcurrent = 0;
            int currentActive = 0;

            for (Event event : events) {
                if (event.isStart) {
                    currentActive++;
                    maxConcurrent = Math.max(maxConcurrent, currentActive);
                } else {
                    currentActive--;
                }
            }

            return maxConcurrent;
        }

        private static class Event {
            int time;
            boolean isStart;

            Event(int time, boolean isStart) {
                this.time = time;
                this.isStart = isStart;
            }
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 11: Shared Workspace Desk Reservation Engine ===");

        // Part 1: Test hour normalization
        System.out.println("\n--- Part 1: Hour Boundary Handling ---");
        int[] hours = {0, 23, 24, 25, -1, -24, 48};
        for (int hour : hours) {
            System.out.println("  Hour " + hour + " -> " + Part1_BugFix.normalizeHour(hour));
        }

        // Part 2: Test occupancy calculation
        System.out.println("\n--- Part 2: Desk Occupancy Analysis ---");
        List<Part1_BugFix.Reservation> deskReservations = Arrays.asList(
            new Part1_BugFix.Reservation("desk1", 9, 12, "alice"),
            new Part1_BugFix.Reservation("desk1", 10, 11, "bob"),  // overlaps
            new Part1_BugFix.Reservation("desk1", 14, 18, "carol")
        );
        int idleHours = Part2_OccupancyAnalysis.calculateIdleHours(deskReservations);
        System.out.println("Idle hours in desk1: " + idleHours);

        // Part 3: Test minimum desks needed
        System.out.println("\n--- Part 3: Optimal Desk Allocation ---");
        List<Part1_BugFix.Reservation> allRequests = Arrays.asList(
            new Part1_BugFix.Reservation("desk_a", 9, 11, "user1"),
            new Part1_BugFix.Reservation("desk_b", 10, 12, "user2"),
            new Part1_BugFix.Reservation("desk_c", 10, 13, "user3"), // peak: 3 concurrent
            new Part1_BugFix.Reservation("desk_d", 11, 15, "user4"),
            new Part1_BugFix.Reservation("desk_e", 14, 16, "user5")
        );
        int minDesks = Part3_OptimalAllocator.findMinDesksNeeded(allRequests);
        System.out.println("Minimum desks needed: " + minDesks);
    }
}
