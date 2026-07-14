package category2_intervals_concurrency;

import java.util.*;

/**
 * Problem 15: Live Webinar Audience Concurrency Analyzer
 * 
 * Three-part progression:
 * - Part 1: Fix negative durations (enforce chronological input order)
 * - Part 2: Calculate total continuous webinar time with active viewers
 * - Part 3: Find peak audience minute with user IDs present
 */
public class Problem15_LiveWebinarAudienceConcurrencyAnalyzer {

    /**
     * Part 1: Bug Fix - Chronological Ordering
     * Issue: Leave time processed before join time, causing negative durations
     * Solution: Enforce input order sorting by time
     */
    public static class Part1_BugFix {
        public static class ViewerSession {
            public String userId;
            public long joinTime;  // epoch seconds
            public long leaveTime; // epoch seconds

            public ViewerSession(String userId, long joinTime, long leaveTime) {
                // Enforce join < leave
                if (leaveTime < joinTime) {
                    long temp = joinTime;
                    joinTime = leaveTime;
                    leaveTime = temp;
                }
                this.userId = userId;
                this.joinTime = joinTime;
                this.leaveTime = leaveTime;
            }

            public long getDuration() {
                return leaveTime - joinTime;
            }

            @Override
            public String toString() {
                return String.format("%s: %d-%d (%.1f min)", userId, joinTime, leaveTime, getDuration() / 60.0);
            }
        }
    }

    /**
     * Part 2: Continuous Airtime Calculation
     * Calculate total time webinar had at least one active viewer
     */
    public static class Part2_ContinuousAirtime {
        /**
         * Calculate total time with at least one viewer
         * Time Complexity: O(n log n)
         */
        public static long calculateActiveAirtimeSeconds(List<Part1_BugFix.ViewerSession> sessions) {
            if (sessions == null || sessions.isEmpty()) {
                return 0;
            }

            // Create events
            List<Event> events = new ArrayList<>();
            for (Part1_BugFix.ViewerSession session : sessions) {
                events.add(new Event(session.joinTime, true));
                events.add(new Event(session.leaveTime, false));
            }

            // Sort events
            events.sort((a, b) -> {
                if (a.time != b.time) {
                    return Long.compare(a.time, b.time);
                }
                // Joins before leaves at same time
                return Boolean.compare(!a.isJoin, !b.isJoin);
            });

            long totalActiveTime = 0;
            int activeViewers = 0;
            long lastTime = -1;

            for (Event event : events) {
                if (activeViewers > 0 && lastTime != -1 && event.time > lastTime) {
                    totalActiveTime += (event.time - lastTime);
                }

                if (event.isJoin) {
                    activeViewers++;
                } else {
                    activeViewers--;
                }

                lastTime = event.time;
            }

            return totalActiveTime;
        }

        private static class Event {
            long time;
            boolean isJoin;

            Event(long time, boolean isJoin) {
                this.time = time;
                this.isJoin = isJoin;
            }
        }
    }

    /**
     * Part 3: Peak Audience Window Finder
     * Find 1-minute peak with user IDs
     */
    public static class Part3_PeakAudienceWindow {
        public static class PeakWindow {
            public long startTime;
            public long endTime;
            public int audienceCount;
            public List<String> userIds;

            public PeakWindow(long startTime, long endTime, int count, List<String> userIds) {
                this.startTime = startTime;
                this.endTime = endTime;
                this.audienceCount = count;
                this.userIds = userIds;
            }

            @Override
            public String toString() {
                return String.format("Peak: %d-%d (%d viewers) [%s]",
                    startTime, endTime, audienceCount, String.join(", ", userIds));
            }
        }

        /**
         * Find 1-minute peak audience window
         * Time Complexity: O(n log n)
         */
        public static PeakWindow findPeakAudienceMinute(List<Part1_BugFix.ViewerSession> sessions) {
            if (sessions == null || sessions.isEmpty()) {
                return null;
            }

            // Create events
            List<Event> events = new ArrayList<>();
            for (Part1_BugFix.ViewerSession session : sessions) {
                events.add(new Event(session.joinTime, true, session.userId));
                events.add(new Event(session.leaveTime, false, session.userId));
            }

            // Sort events
            events.sort((a, b) -> {
                if (a.time != b.time) {
                    return Long.compare(a.time, b.time);
                }
                // Joins before leaves at same time
                return Boolean.compare(!a.isJoin, !b.isJoin);
            });

            int maxAudience = 0;
            long peakTime = -1;
            Set<String> peakUserIds = new HashSet<>();
            Set<String> activeUsers = new HashSet<>();

            for (Event event : events) {
                if (event.isJoin) {
                    activeUsers.add(event.userId);
                } else {
                    activeUsers.remove(event.userId);
                }

                if (activeUsers.size() > maxAudience) {
                    maxAudience = activeUsers.size();
                    peakTime = event.time;
                    peakUserIds = new HashSet<>(activeUsers);
                }
            }

            if (peakTime == -1) {
                return null;
            }

            return new PeakWindow(peakTime, peakTime + 60, maxAudience, new ArrayList<>(peakUserIds));
        }

        private static class Event {
            long time;
            boolean isJoin;
            String userId;

            Event(long time, boolean isJoin, String userId) {
                this.time = time;
                this.isJoin = isJoin;
                this.userId = userId;
            }
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 15: Live Webinar Audience Concurrency Analyzer ===");

        // Part 1: Test chronological ordering
        System.out.println("\n--- Part 1: Chronological Ordering ---");
        Part1_BugFix.ViewerSession session1 =
            new Part1_BugFix.ViewerSession("user1", 1000, 2000);
        Part1_BugFix.ViewerSession session2 =
            new Part1_BugFix.ViewerSession("user2", 2000, 1500); // Swapped - should auto-correct
        System.out.println("  Session 1: " + session1);
        System.out.println("  Session 2 (auto-corrected): " + session2);

        // Part 2: Test active airtime
        System.out.println("\n--- Part 2: Continuous Active Airtime ---");
        List<Part1_BugFix.ViewerSession> sessions = Arrays.asList(
            new Part1_BugFix.ViewerSession("user1", 1000, 2000),
            new Part1_BugFix.ViewerSession("user2", 1500, 2500),
            new Part1_BugFix.ViewerSession("user3", 2200, 2800)
        );
        long activeTime = Part2_ContinuousAirtime.calculateActiveAirtimeSeconds(sessions);
        System.out.println("  Total active airtime: " + activeTime + " seconds (" + (activeTime / 60.0) + " min)");

        // Part 3: Test peak audience
        System.out.println("\n--- Part 3: Peak Audience Window ---");
        Part3_PeakAudienceWindow.PeakWindow peak =
            Part3_PeakAudienceWindow.findPeakAudienceMinute(sessions);
        if (peak != null) {
            System.out.println("  " + peak);
        } else {
            System.out.println("  No peak found");
        }
    }
}
