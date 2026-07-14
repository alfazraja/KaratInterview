package category2_intervals_concurrency;

import java.util.*;

/**
 * Problem 10: Shared Calendar Free-Busy Optimizer
 * 
 * Three-part progression:
 * - Part 1: Fix time sorting (string "11:00 AM" sorts before "1:30 PM" alphabetically)
 * - Part 2: Merge overlapping calendar entries into free/busy blocks
 * - Part 3: Find peak conflict hour (max overlapping invites at any minute)
 */
public class Problem10_SharedCalendarFreeBusyOptimizer {

    /**
     * Part 1: Bug Fix - Time Parsing and Sorting
     * Issue: String-based times like "11:00 AM" sort alphabetically, breaking chronology
     * Solution: Parse to minutes since midnight for proper sorting
     */
    public static class Part1_BugFix {
        /**
         * Parse time string to minutes since midnight
         * Formats: "11:00 AM", "2:30 PM", "23:45"
         */
        public static int parseTimeToMinutes(String timeStr) {
            if (timeStr == null || timeStr.isEmpty()) {
                throw new IllegalArgumentException("Invalid time string");
            }

            timeStr = timeStr.trim();
            boolean isPM = timeStr.toUpperCase().contains("PM");
            boolean isAM = timeStr.toUpperCase().contains("AM");

            // Remove AM/PM
            String timePart = timeStr.replaceAll("(?i)[AP]M", "").trim();

            String[] parts = timePart.split(":");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid time format: " + timeStr);
            }

            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());

            // Convert 12-hour to 24-hour format
            if (isAM && hours == 12) {
                hours = 0;
            } else if (isPM && hours != 12) {
                hours += 12;
            }

            return hours * 60 + minutes;
        }

        /**
         * Format minutes since midnight back to time string
         */
        public static String formatMinutesToTime(int minutes) {
            int hours = minutes / 60;
            int mins = minutes % 60;
            String period = hours >= 12 ? "PM" : "AM";
            if (hours > 12) hours -= 12;
            if (hours == 0) hours = 12;
            return String.format("%d:%02d %s", hours, mins, period);
        }
    }

    /**
     * Part 2: Calendar Merging
     * Consolidate free/busy blocks into unified timeline
     */
    public static class Part2_CalendarMerging {
        public static class CalendarEntry {
            public String title;
            public int startMinutes; // minutes since midnight
            public int endMinutes;
            public String status; // BUSY or FREE

            public CalendarEntry(String title, int startMinutes, int endMinutes, String status) {
                this.title = title;
                this.startMinutes = startMinutes;
                this.endMinutes = endMinutes;
                this.status = status;
            }

            @Override
            public String toString() {
                return String.format("%s: %s-%s [%s]",
                    title,
                    Part1_BugFix.formatMinutesToTime(startMinutes),
                    Part1_BugFix.formatMinutesToTime(endMinutes),
                    status);
            }
        }

        /**
         * Merge overlapping calendar entries
         * Time Complexity: O(n log n)
         */
        public static List<CalendarEntry> mergeCalendar(List<CalendarEntry> entries) {
            if (entries == null || entries.isEmpty()) {
                return new ArrayList<>();
            }

            // Sort by start time
            List<CalendarEntry> sorted = new ArrayList<>(entries);
            sorted.sort((a, b) -> Integer.compare(a.startMinutes, b.startMinutes));

            List<CalendarEntry> merged = new ArrayList<>();
            CalendarEntry current = new CalendarEntry(
                sorted.get(0).title,
                sorted.get(0).startMinutes,
                sorted.get(0).endMinutes,
                sorted.get(0).status
            );

            for (int i = 1; i < sorted.size(); i++) {
                CalendarEntry next = sorted.get(i);

                if (next.startMinutes <= current.endMinutes) {
                    // Overlapping - merge
                    current.endMinutes = Math.max(current.endMinutes, next.endMinutes);
                    // Prioritize BUSY over FREE
                    if ("BUSY".equals(next.status)) {
                        current.status = "BUSY";
                    }
                } else {
                    // Gap found - save current and start new
                    merged.add(current);
                    current = new CalendarEntry(next.title, next.startMinutes, next.endMinutes, next.status);
                }
            }
            merged.add(current);

            return merged;
        }
    }

    /**
     * Part 3: Peak Conflict Finder
     * Find the minute with most overlapping meeting invitations
     */
    public static class Part3_PeakConflict {
        public static class ConflictPeak {
            public int peakMinute;
            public String peakTime;
            public int overlapCount;
            public List<String> meetingTitles;

            public ConflictPeak(int peakMinute, int overlapCount, List<String> meetingTitles) {
                this.peakMinute = peakMinute;
                this.peakTime = Part1_BugFix.formatMinutesToTime(peakMinute);
                this.overlapCount = overlapCount;
                this.meetingTitles = meetingTitles;
            }

            @Override
            public String toString() {
                return String.format("Peak conflict at %s: %d meetings (%s)",
                    peakTime, overlapCount, String.join(", ", meetingTitles));
            }
        }

        /**
         * Find peak conflict hour using sweep line algorithm
         * Time Complexity: O(n log n)
         */
        public static ConflictPeak findPeakConflict(List<Part2_CalendarMerging.CalendarEntry> entries) {
            if (entries == null || entries.isEmpty()) {
                return null;
            }

            // Create events for sweep line
            List<Event> events = new ArrayList<>();
            for (Part2_CalendarMerging.CalendarEntry entry : entries) {
                events.add(new Event(entry.startMinutes, true, entry.title));
                events.add(new Event(entry.endMinutes, false, entry.title));
            }

            // Sort events
            events.sort((a, b) -> {
                if (a.time != b.time) {
                    return Integer.compare(a.time, b.time);
                }
                // Starts before ends at same time
                return Boolean.compare(!a.isStart, !b.isStart);
            });

            int maxOverlap = 0;
            int peakMinute = -1;
            Set<String> peakMeetings = new HashSet<>();
            Set<String> activeMeetings = new HashSet<>();

            for (Event event : events) {
                if (event.isStart) {
                    activeMeetings.add(event.title);
                } else {
                    activeMeetings.remove(event.title);
                }

                if (activeMeetings.size() > maxOverlap) {
                    maxOverlap = activeMeetings.size();
                    peakMinute = event.time;
                    peakMeetings = new HashSet<>(activeMeetings);
                }
            }

            if (peakMinute == -1) {
                return null;
            }

            return new ConflictPeak(peakMinute, maxOverlap, new ArrayList<>(peakMeetings));
        }

        private static class Event {
            int time;
            boolean isStart;
            String title;

            Event(int time, boolean isStart, String title) {
                this.time = time;
                this.isStart = isStart;
                this.title = title;
            }
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 10: Shared Calendar Free-Busy Optimizer ===");

        // Part 1: Test time parsing
        System.out.println("\n--- Part 1: Time Parsing ---");
        String[] times = {"11:00 AM", "1:30 PM", "9:00 AM", "6:45 PM"};
        List<String> sortedByString = new ArrayList<>(Arrays.asList(times));
        sortedByString.sort(String::compareTo);
        
        System.out.println("Alphabetical sort (WRONG): " + sortedByString);
        
        List<String> sortedByMinutes = new ArrayList<>(Arrays.asList(times));
        sortedByMinutes.sort((a, b) -> Integer.compare(
            Part1_BugFix.parseTimeToMinutes(a),
            Part1_BugFix.parseTimeToMinutes(b)
        ));
        System.out.println("Chronological sort (CORRECT): " + sortedByMinutes);

        // Part 2: Test calendar merging
        System.out.println("\n--- Part 2: Calendar Merging ---");
        List<Part2_CalendarMerging.CalendarEntry> entries = Arrays.asList(
            new Part2_CalendarMerging.CalendarEntry("Meeting A", 540, 600, "BUSY"),   // 9:00-10:00 AM
            new Part2_CalendarMerging.CalendarEntry("Meeting B", 570, 630, "BUSY"),  // 9:30-10:30 AM (overlaps)
            new Part2_CalendarMerging.CalendarEntry("Lunch", 720, 780, "BUSY"),      // 12:00-1:00 PM
            new Part2_CalendarMerging.CalendarEntry("Meeting C", 1020, 1080, "BUSY") // 5:00-6:00 PM
        );
        List<Part2_CalendarMerging.CalendarEntry> merged = Part2_CalendarMerging.mergeCalendar(entries);
        System.out.println("Merged calendar:");
        for (Part2_CalendarMerging.CalendarEntry entry : merged) {
            System.out.println("  " + entry);
        }

        // Part 3: Test peak conflict
        System.out.println("\n--- Part 3: Peak Conflict ---");
        Part3_PeakConflict.ConflictPeak peak = Part3_PeakConflict.findPeakConflict(entries);
        if (peak != null) {
            System.out.println("  " + peak);
        } else {
            System.out.println("  No conflicts found");
        }
    }
}
