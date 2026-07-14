package category2_interval_math;

import java.util.*;

public class Problem17_LiveWebinarAudienceConcurrency {
    
    static class ViewerSession {
        String userId;
        int joinTime;
        int leaveTime;
        
        ViewerSession(String userId, int join, int leave) {
            this.userId = userId;
            this.joinTime = join;
            this.leaveTime = leave;
        }
    }
    
    /**
     * Part 1: Fix negative watch durations
     * Ensure chronological order (join before leave)
     */
    public static List<ViewerSession> validateAndSortSessions(int[][] sessions, String[] userIds) {
        List<ViewerSession> validSessions = new ArrayList<>();
        
        for (int i = 0; i < sessions.length; i++) {
            int time1 = sessions[i][0];
            int time2 = sessions[i][1];
            
            // Ensure chronological order
            int joinTime = Math.min(time1, time2);
            int leaveTime = Math.max(time1, time2);
            
            if (joinTime < leaveTime) {
                validSessions.add(new ViewerSession(userIds[i], joinTime, leaveTime));
            }
        }
        return validSessions;
    }
    
    /**
     * Part 2: Compute total continuous viewing time
     * Merge all overlapping viewer intervals
     */
    public static int getTotalContinuousViewingTime(List<ViewerSession> sessions) {
        if (sessions.isEmpty()) return 0;
        
        // Sort by join time
        sessions.sort((a, b) -> Integer.compare(a.joinTime, b.joinTime));
        
        int totalTime = 0;
        int currentStart = sessions.get(0).joinTime;
        int currentEnd = sessions.get(0).leaveTime;
        
        for (int i = 1; i < sessions.size(); i++) {
            ViewerSession session = sessions.get(i);
            if (session.joinTime <= currentEnd) {
                // Overlapping
                currentEnd = Math.max(currentEnd, session.leaveTime);
            } else {
                // Gap - add previous interval
                totalTime += (currentEnd - currentStart);
                currentStart = session.joinTime;
                currentEnd = session.leaveTime;
            }
        }
        totalTime += (currentEnd - currentStart);
        return totalTime;
    }
    
    /**
     * Part 3: Find peak concurrency window
     * Return the 1-minute interval with most viewers and their user IDs
     */
    public static Map<String, Object> getPeakConcurrencyWindow(List<ViewerSession> sessions) {
        if (sessions.isEmpty()) return new HashMap<>();
        
        // Events: time, type (0=join, 1=leave), userId
        List<int[]> events = new ArrayList<>();
        for (ViewerSession session : sessions) {
            events.add(new int[]{session.joinTime, 0, session.userId.hashCode()});
            events.add(new int[]{session.leaveTime, 1, session.userId.hashCode()});
        }
        
        // Sort events by time, joins before leaves
        events.sort((a, b) -> {
            if (a[0] != b[0]) return Integer.compare(a[0], b[0]);
            return Integer.compare(a[1], b[1]);  // joins (0) before leaves (1)
        });
        
        int maxViewers = 0;
        int peakTime = 0;
        int currentViewers = 0;
        
        for (int[] event : events) {
            if (event[1] == 0) {
                currentViewers++;
            } else {
                currentViewers--;
            }
            
            if (currentViewers > maxViewers) {
                maxViewers = currentViewers;
                peakTime = event[0];
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("peakTime", peakTime);
        result.put("concurrentViewers", maxViewers);
        return result;
    }
    
    public static void main(String[] args) {
        int[][] sessions = {{10, 20}, {15, 30}, {5, 25}, {40, 50}};
        String[] userIds = {"user1", "user2", "user3", "user4"};
        
        // Test Part 1
        List<ViewerSession> validSessions = validateAndSortSessions(sessions, userIds);
        System.out.println("Valid sessions: " + validSessions.size());
        
        // Test Part 2
        int totalTime = getTotalContinuousViewingTime(validSessions);
        System.out.println("Total continuous viewing time: " + totalTime);
        
        // Test Part 3
        Map<String, Object> peak = getPeakConcurrencyWindow(validSessions);
        System.out.println("Peak concurrency: " + peak);
    }
}
