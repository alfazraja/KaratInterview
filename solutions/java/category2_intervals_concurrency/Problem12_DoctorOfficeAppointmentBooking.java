package category2_intervals_concurrency;

import java.util.*;

/**
 * Problem 12: Doctor's Office Appointment Booking System
 * 
 * Three-part progression:
 * - Part 1: Validate appointments don't span multiple days
 * - Part 2: Find available appointment gaps matching duration requirement
 * - Part 3: Find appointment to reschedule for urgent patient with minimal disruption
 */
public class Problem12_DoctorOfficeAppointmentBooking {

    /**
     * Part 1: Bug Fix - Multi-day appointment validation
     * Issue: System crashes on overnight shifts (e.g., 22:00-02:00)
     * Solution: Add validation to block multi-day appointments
     */
    public static class Part1_BugFix {
        public static class Appointment {
            public String patientId;
            public int startTime;  // minutes since midnight
            public int endTime;    // minutes since midnight
            public String doctorId;

            public Appointment(String patientId, int startTime, int endTime, String doctorId) {
                this.patientId = patientId;
                this.startTime = startTime;
                this.endTime = endTime;
                this.doctorId = doctorId;
            }

            @Override
            public String toString() {
                return String.format("%s: %d-%d min (Dr. %s)", patientId, startTime, endTime, doctorId);
            }
        }

        /**
         * Validate appointment doesn't span multiple days
         */
        public static boolean isValidAppointment(int startTime, int endTime) {
            // Check bounds: 0-1440 minutes in a day (24 * 60)
            if (startTime < 0 || startTime >= 1440 || endTime <= 0 || endTime > 1440) {
                return false;
            }
            // End must be after start, same day
            return endTime > startTime;
        }

        /**
         * Check if appointment is valid before creating
         */
        public static Appointment createAppointment(String patientId, int startTime, int endTime, String doctorId) {
            if (!isValidAppointment(startTime, endTime)) {
                throw new IllegalArgumentException(
                    String.format("Invalid appointment: %d-%d (must be same day)", startTime, endTime));
            }
            return new Appointment(patientId, startTime, endTime, doctorId);
        }
    }

    /**
     * Part 2: Available Slot Finder
     * Find unbooked appointment gaps matching required duration
     */
    public static class Part2_AvailableSlotFinder {
        public static class AvailableSlot {
            public int startTime;
            public int endTime;
            public int durationMinutes;

            public AvailableSlot(int startTime, int endTime) {
                this.startTime = startTime;
                this.endTime = endTime;
                this.durationMinutes = endTime - startTime;
            }

            @Override
            public String toString() {
                return String.format("%d-%d (%d min)", startTime, endTime, durationMinutes);
            }
        }

        /**
         * Find available slots matching required duration
         * Time Complexity: O(n log n)
         */
        public static List<AvailableSlot> findAvailableSlots(
            List<Part1_BugFix.Appointment> appointments,
            int requiredDuration) {
            
            final int OFFICE_OPEN = 480;   // 8:00 AM (8*60)
            final int OFFICE_CLOSE = 1020; // 5:00 PM (17*60)

            if (appointments == null || appointments.isEmpty()) {
                List<AvailableSlot> slots = new ArrayList<>();
                if (OFFICE_CLOSE - OFFICE_OPEN >= requiredDuration) {
                    slots.add(new AvailableSlot(OFFICE_OPEN, OFFICE_CLOSE));
                }
                return slots;
            }

            // Sort appointments
            List<Part1_BugFix.Appointment> sorted = new ArrayList<>(appointments);
            sorted.sort((a, b) -> Integer.compare(a.startTime, b.startTime));

            List<AvailableSlot> available = new ArrayList<>();

            // Check before first appointment
            if (sorted.get(0).startTime - OFFICE_OPEN >= requiredDuration) {
                available.add(new AvailableSlot(OFFICE_OPEN, sorted.get(0).startTime));
            }

            // Check gaps between appointments
            for (int i = 0; i < sorted.size() - 1; i++) {
                int gapStart = sorted.get(i).endTime;
                int gapEnd = sorted.get(i + 1).startTime;
                if (gapEnd - gapStart >= requiredDuration) {
                    available.add(new AvailableSlot(gapStart, gapEnd));
                }
            }

            // Check after last appointment
            int lastEnd = sorted.get(sorted.size() - 1).endTime;
            if (OFFICE_CLOSE - lastEnd >= requiredDuration) {
                available.add(new AvailableSlot(lastEnd, OFFICE_CLOSE));
            }

            return available;
        }
    }

    /**
     * Part 3: Urgent Patient Rescheduling
     * Find appointment to reschedule with minimal disruption
     */
    public static class Part3_UrgentReschedule {
        public static class RescheduleOption {
            public Part1_BugFix.Appointment toReschedule;
            public Part2_AvailableSlotFinder.AvailableSlot newSlot;
            public int disruption; // number of affected appointments

            public RescheduleOption(Part1_BugFix.Appointment toReschedule,
                                  Part2_AvailableSlotFinder.AvailableSlot newSlot,
                                  int disruption) {
                this.toReschedule = toReschedule;
                this.newSlot = newSlot;
                this.disruption = disruption;
            }

            @Override
            public String toString() {
                return String.format("Reschedule %s to %s (affects %d appointments)",
                    toReschedule.patientId, newSlot, disruption);
            }
        }

        /**
         * Find appointment to reschedule for urgent 1-hour case
         * Minimizes disruption to other patients
         */
        public static RescheduleOption findOptimalReschedule(
            List<Part1_BugFix.Appointment> currentAppointments,
            int requiredDuration) {
            
            int OFFICE_OPEN = 480;
            int OFFICE_CLOSE = 1020;

            // Find longest appointment that could be rescheduled
            Part1_BugFix.Appointment candidate = null;
            int maxDuration = 0;
            
            for (Part1_BugFix.Appointment apt : currentAppointments) {
                int duration = apt.endTime - apt.startTime;
                if (duration >= requiredDuration && duration > maxDuration) {
                    candidate = apt;
                    maxDuration = duration;
                }
            }

            if (candidate == null) {
                return null;
            }

            // Remove candidate and find available slots
            List<Part1_BugFix.Appointment> remaining = new ArrayList<>(currentAppointments);
            remaining.remove(candidate);

            List<Part2_AvailableSlotFinder.AvailableSlot> slots =
                Part2_AvailableSlotFinder.findAvailableSlots(remaining, requiredDuration);

            if (slots.isEmpty()) {
                return null;
            }

            // Use first available slot
            Part2_AvailableSlotFinder.AvailableSlot newSlot = slots.get(0);
            
            // Calculate disruption (appointments affected by moving this one)
            int disruption = 0;
            for (Part1_BugFix.Appointment apt : currentAppointments) {
                if (!apt.equals(candidate)) {
                    // Check if they would be affected
                    if (!(apt.endTime <= candidate.startTime || apt.startTime >= candidate.endTime)) {
                        disruption++;
                    }
                }
            }

            return new RescheduleOption(candidate, newSlot, disruption);
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 12: Doctor's Office Appointment Booking ===");

        // Part 1: Test appointment validation
        System.out.println("\n--- Part 1: Appointment Validation ---");
        int[][] timeRanges = {
            {480, 540},    // 8:00-9:00 AM (valid)
            {1020, 1440},  // 5:00 PM-midnight (valid)
            {900, 100},    // 3:00 PM to 1:40 AM (invalid - crosses day)
            {-60, 100}     // invalid start
        };
        for (int[] range : timeRanges) {
            boolean valid = Part1_BugFix.isValidAppointment(range[0], range[1]);
            System.out.println("  " + range[0] + "-" + range[1] + ": " + (valid ? "VALID" : "INVALID"));
        }

        // Part 2: Test available slot finding
        System.out.println("\n--- Part 2: Available Appointment Slots ---");
        List<Part1_BugFix.Appointment> scheduled = Arrays.asList(
            Part1_BugFix.createAppointment("patient1", 480, 540, "dr1"),   // 8-9 AM
            Part1_BugFix.createAppointment("patient2", 600, 660, "dr1"),   // 10-11 AM
            Part1_BugFix.createAppointment("patient3", 720, 780, "dr1")    // 12-1 PM
        );
        List<Part2_AvailableSlotFinder.AvailableSlot> slots =
            Part2_AvailableSlotFinder.findAvailableSlots(scheduled, 60);
        System.out.println("Available 60-min slots:");
        for (Part2_AvailableSlotFinder.AvailableSlot slot : slots) {
            System.out.println("  " + slot);
        }

        // Part 3: Test urgent rescheduling
        System.out.println("\n--- Part 3: Urgent Patient Rescheduling ---");
        Part3_UrgentReschedule.RescheduleOption option =
            Part3_UrgentReschedule.findOptimalReschedule(scheduled, 60);
        if (option != null) {
            System.out.println("  " + option);
        } else {
            System.out.println("  No reschedule option available");
        }
    }
}
