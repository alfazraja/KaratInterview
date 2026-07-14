package category2_interval_math;

import java.util.*;

public class Problem20_PublicTransitBusDriverShiftPlanner {
    
    /**
     * Part 1: Fix rounding bug
     * Use ceiling for fractional hours instead of truncating downward
     */
    public static int calculateShiftDuration(double hours) {
        // Correct: use ceiling instead of truncating
        return (int) Math.ceil(hours);
    }
    
    /**
     * Part 2: Merge overlapping or continuous driving blocks
     * Monitor total continuous wheel-time
     */
    public static List<int[]> mergeDrivingSegments(int[][] segments) {
        if (segments.length == 0) return new ArrayList<>();
        
        // Sort by start time
        Arrays.sort(segments, (a, b) -> Integer.compare(a[0], b[0]));
        
        List<int[]> merged = new ArrayList<>();
        int start = segments[0][0];
        int end = segments[0][1];
        
        for (int i = 1; i < segments.length; i++) {
            if (segments[i][0] <= end) {
                // Overlapping or continuous (end == start means adjacent)
                end = Math.max(end, segments[i][1]);
            } else {
                // Gap - save previous segment
                merged.add(new int[]{start, end});
                start = segments[i][0];
                end = segments[i][1];
            }
        }
        merged.add(new int[]{start, end});
        return merged;
    }
    
    /**
     * Part 3: Insert mandatory 30-minute breaks
     * After 4 hours (240 minutes) of continuous driving
     * Find optimal insertion points
     */
    public static List<String> insertMandatoryBreaks(List<int[]> segments, int maxContinuousDrive) {
        List<String> schedule = new ArrayList<>();
        int totalDrive = 0;
        
        for (int[] segment : segments) {
            int segmentDuration = segment[1] - segment[0];
            
            if (totalDrive + segmentDuration <= maxContinuousDrive) {
                // Fits in current driving block
                schedule.add("DRIVE [" + segment[0] + "-" + segment[1] + "]");
                totalDrive += segmentDuration;
            } else if (totalDrive > 0) {
                // Need break before this segment
                int breakStart = segment[0] - 30;
                schedule.add("BREAK [" + breakStart + "-" + segment[0] + "]");
                schedule.add("DRIVE [" + segment[0] + "-" + segment[1] + "]");
                totalDrive = segmentDuration;
            } else {
                // Long segment - may need multiple breaks
                int currentTime = segment[0];
                int remaining = segmentDuration;
                
                while (remaining > 0) {
                    if (remaining > maxContinuousDrive) {
                        schedule.add("DRIVE [" + currentTime + "-" + (currentTime + maxContinuousDrive) + "]");
                        currentTime += maxContinuousDrive;
                        remaining -= maxContinuousDrive;
                        
                        if (remaining > 0) {
                            schedule.add("BREAK [" + currentTime + "-" + (currentTime + 30) + "]");
                            currentTime += 30;
                        }
                    } else {
                        schedule.add("DRIVE [" + currentTime + "-" + (currentTime + remaining) + "]");
                        remaining = 0;
                    }
                }
                totalDrive = 0;
            }
        }
        
        return schedule;
    }
    
    public static void main(String[] args) {
        // Test Part 1
        System.out.println("Shift duration for 7.5 hours: " + calculateShiftDuration(7.5));
        System.out.println("Shift duration for 8.0 hours: " + calculateShiftDuration(8.0));
        
        // Test Part 2
        int[][] segments = {{600, 720}, {720, 840}, {900, 1020}};
        List<int[]> merged = mergeDrivingSegments(segments);
        System.out.println("Merged driving segments:");
        for (int[] seg : merged) {
            System.out.println("  [" + seg[0] + ", " + seg[1] + "]");
        }
        
        // Test Part 3
        List<String> schedule = insertMandatoryBreaks(merged, 240);
        System.out.println("Schedule with breaks:");
        for (String item : schedule) {
            System.out.println("  " + item);
        }
    }
}
