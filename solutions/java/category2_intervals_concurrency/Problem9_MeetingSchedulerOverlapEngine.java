package category2_intervals_concurrency;

import java.util.*;

/**
 * Problem 9: The Meeting Scheduler & Overlap Engine
 * 
 * Three-part progression:
 * - Part 1: Fix boundary logic (use < instead of <= for overlap detection)
 * - Part 2: Merge overlapping intervals and find available time slots
 * - Part 3: Find mutually available 45-minute window for multiple team members
 */
public class Problem9_MeetingSchedulerOverlapEngine {

    /**
     * Part 1: Bug Fix - Boundary Logic
     * Issue: Back-to-back appointments (0900-1000 and 1000-1100) flagged as overlapping
     * Solution: Use exclusive boundary checks (< instead of <=)
     */
    public static class Part1_BugFix {
        public static class Appointment {
            public int startTime; // 24-hour format (900 = 9:00 AM)
            public int endTime;
            public String title;

            public Appointment(int startTime, int endTime, String title) {
                this.startTime = startTime;
                this.endTime = endTime;
                this.title = title;
            }

            @Override
            public String toString() {
                return String.format("%04d-%04d: %s", startTime, endTime, title);
            }
        }

        /**
         * Check if two appointments overlap
         * Back-to-back (end1 == start2) is NOT an overlap
         */
        public static boolean hasConflict(Appointment a1, Appointment a2) {
            // Correct: use < (exclusive boundary)
            return a1.startTime < a2.endTime && a2.startTime < a1.endTime;
        }

        /**
         * Find all conflicts in appointment list
         */
        public static List<String> findConflicts(List<Appointment> appointments) {
            List<String> conflicts = new ArrayList<>();
            
            for (int i = 0; i < appointments.size(); i++) {
                for (int j = i + 1; j < appointments.size(); j++) {
                    if (hasConflict(appointments.get(i), appointments.get(j))) {
                        conflicts.add(String.format("%s CONFLICTS WITH %s",
                            appointments.get(i).title, appointments.get(j).title));
                    }
                }
            }
            return conflicts;
        }
    }

    /**
     * Part 2: Interval Merging
     * Merge overlapping appointments and find available slots during 0900-1700
     */
    public static class Part2_IntervalMerge {
        public static class TimeSlot {
            public int startTime;
            public int endTime;

            public TimeSlot(int startTime, int endTime) {
                this.startTime = startTime;
                this.endTime = endTime;
            }

            public int getDuration() {
                return endTime - startTime;
            }

            @Override
            public String toString() {
                return String.format("%04d-%04d (%d min)", startTime, endTime, getDuration());
            }
        }

        /**
         * Merge overlapping intervals
         * Time Complexity: O(n log n) for sorting + O(n) for merging
         */
        public static List<TimeSlot> mergeAppointments(List<Part1_BugFix.Appointment> appointments) {
            if (appointments == null || appointments.isEmpty()) {
                return new ArrayList<>();
            }

            // Sort by start time
            List<Part1_BugFix.Appointment> sorted = new ArrayList<>(appointments);
            sorted.sort((a, b) -> Integer.compare(a.startTime, b.startTime));

            List<TimeSlot> merged = new ArrayList<>();
            TimeSlot current = new TimeSlot(sorted.get(0).startTime, sorted.get(0).endTime);

            for (int i = 1; i < sorted.size(); i++) {
                Part1_BugFix.Appointment next = sorted.get(i);

                if (next.startTime <= current.endTime) {
                    // Overlapping or adjacent - merge
                    current.endTime = Math.max(current.endTime, next.endTime);
                } else {
                    // Non-overlapping - save current and start new
                    merged.add(current);
                    current = new TimeSlot(next.startTime, next.endTime);
                }
            }
            merged.add(current);

            return merged;
        }

        /**
         * Find available time slots during workday (0900-1700)
         */
        public static List<TimeSlot> findAvailableSlots(List<Part1_BugFix.Appointment> appointments) {
            final int WORKDAY_START = 900;
            final int WORKDAY_END = 1700;

            List<TimeSlot> merged = mergeAppointments(appointments);
            List<TimeSlot> available = new ArrayList<>();

            // Check before first appointment
            if (!merged.isEmpty() && merged.get(0).startTime > WORKDAY_START) {
                available.add(new TimeSlot(WORKDAY_START, merged.get(0).startTime));
            }

            // Check gaps between appointments
            for (int i = 0; i < merged.size() - 1; i++) {
                int gapStart = merged.get(i).endTime;
                int gapEnd = merged.get(i + 1).startTime;
                if (gapEnd > gapStart) {
                    available.add(new TimeSlot(gapStart, gapEnd));
                }
            }

            // Check after last appointment
            if (!merged.isEmpty() && merged.get(merged.size() - 1).endTime < WORKDAY_END) {
                available.add(new TimeSlot(merged.get(merged.size() - 1).endTime, WORKDAY_END));
            }

            // Handle case with no appointments
            if (merged.isEmpty()) {
                available.add(new TimeSlot(WORKDAY_START, WORKDAY_END));
            }

            return available;
        }
    }

    /**
     * Part 3: Multi-User Intersection
     * Find mutually available 45-minute window across multiple team members
     */
    public static class Part3_MultiUserIntersection {
        /**
         * Find common available time slot for all team members
         * Time Complexity: O(n*m*log(n*m)) where n = people, m = slots
         */
        public static Part2_IntervalMerge.TimeSlot findCommonAvailability(
            Map<String, List<Part1_BugFix.Appointment>> teamSchedules,
            int requiredDuration) {
            
            if (teamSchedules == null || teamSchedules.isEmpty()) {
                return null;
            }

            final int WORKDAY_START = 900;
            final int WORKDAY_END = 1700;

            // Get available slots for each person
            Map<String, List<Part2_IntervalMerge.TimeSlot>> availableSlots = new HashMap<>();
            for (Map.Entry<String, List<Part1_BugFix.Appointment>> entry : teamSchedules.entrySet()) {
                availableSlots.put(entry.getKey(), Part2_IntervalMerge.findAvailableSlots(entry.getValue()));
            }

            // Find intersection of all available periods
            List<String> people = new ArrayList<>(availableSlots.keySet());
            String firstPerson = people.get(0);
            List<Part2_IntervalMerge.TimeSlot> commonSlots = availableSlots.get(firstPerson);

            // Intersect with other people's availability
            for (int i = 1; i < people.size(); i++) {
                String person = people.get(i);
                List<Part2_IntervalMerge.TimeSlot> personSlots = availableSlots.get(person);

                List<Part2_IntervalMerge.TimeSlot> newCommon = new ArrayList<>();
                
                for (Part2_IntervalMerge.TimeSlot common : commonSlots) {
                    for (Part2_IntervalMerge.TimeSlot personSlot : personSlots) {
                        int overlapStart = Math.max(common.startTime, personSlot.startTime);
                        int overlapEnd = Math.min(common.endTime, personSlot.endTime);

                        if (overlapEnd - overlapStart >= requiredDuration) {
                            newCommon.add(new Part2_IntervalMerge.TimeSlot(overlapStart, overlapEnd));
                        }
                    }
                }
                commonSlots = newCommon;
            }

            // Return first available slot of required duration
            if (!commonSlots.isEmpty()) {
                return commonSlots.get(0);
            }

            return null;
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 9: Meeting Scheduler & Overlap Engine ===");

        // Part 1: Test boundary logic
        System.out.println("\n--- Part 1: Conflict Detection ---");
        List<Part1_BugFix.Appointment> appointments = Arrays.asList(
            new Part1_BugFix.Appointment(900, 1000, "Meeting A"),
            new Part1_BugFix.Appointment(1000, 1100, "Meeting B"), // Back-to-back, no conflict
            new Part1_BugFix.Appointment(1030, 1130, "Meeting C")  // Overlaps with B
        );
        List<String> conflicts = Part1_BugFix.findConflicts(appointments);
        System.out.println("Conflicts found: " + conflicts.size());
        for (String conflict : conflicts) {
            System.out.println("  " + conflict);
        }

        // Part 2: Test interval merging
        System.out.println("\n--- Part 2: Available Time Slots ---");
        List<Part2_IntervalMerge.TimeSlot> available = Part2_IntervalMerge.findAvailableSlots(appointments);
        System.out.println("Available slots (9:00-17:00):");
        for (Part2_IntervalMerge.TimeSlot slot : available) {
            System.out.println("  " + slot);
        }

        // Part 3: Test multi-user intersection
        System.out.println("\n--- Part 3: Common Availability ---");
        Map<String, List<Part1_BugFix.Appointment>> teamSchedules = new HashMap<>();
        teamSchedules.put("Alice", Arrays.asList(
            new Part1_BugFix.Appointment(900, 1000, "Task A"),
            new Part1_BugFix.Appointment(1400, 1500, "Task B")
        ));
        teamSchedules.put("Bob", Arrays.asList(
            new Part1_BugFix.Appointment(930, 1030, "Task C"),
            new Part1_BugFix.Appointment(1300, 1400, "Task D")
        ));
        teamSchedules.put("Carol", Arrays.asList(
            new Part1_BugFix.Appointment(1100, 1200, "Task E")
        ));

        Part2_IntervalMerge.TimeSlot common = Part3_MultiUserIntersection.findCommonAvailability(
            teamSchedules, 45);
        if (common != null) {
            System.out.println("Common 45-min slot: " + common);
        } else {
            System.out.println("No common availability found");
        }
    }
}
