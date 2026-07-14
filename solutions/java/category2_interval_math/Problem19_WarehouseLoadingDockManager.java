package category2_interval_math;

import java.util.*;

public class Problem19_WarehouseLoadingDockManager {
    
    /**
     * Part 1: Fix key conflict bug
     * Multiple trucks can arrive at same second
     * Use Map<Integer, List<String>> instead of Map<Integer, String>
     */
    public static Map<Integer, List<String>> parseArrivalLogs(int[] times, String[] truckIds) {
        Map<Integer, List<String>> arrivals = new HashMap<>();
        
        for (int i = 0; i < times.length; i++) {
            arrivals.computeIfAbsent(times[i], k -> new ArrayList<>())
                   .add(truckIds[i]);
        }
        return arrivals;
    }
    
    /**
     * Part 2: Merge overlapping dock occupancy times
     * Map total hours a loading dock was busy
     */
    public static List<int[]> mergeDockOccupancy(int[][] occupancyTimes) {
        if (occupancyTimes.length == 0) return new ArrayList<>();
        
        // Sort by start time
        Arrays.sort(occupancyTimes, (a, b) -> Integer.compare(a[0], b[0]));
        
        List<int[]> merged = new ArrayList<>();
        int start = occupancyTimes[0][0];
        int end = occupancyTimes[0][1];
        
        for (int i = 1; i < occupancyTimes.length; i++) {
            if (occupancyTimes[i][0] <= end) {
                // Overlapping
                end = Math.max(end, occupancyTimes[i][1]);
            } else {
                // Gap
                merged.add(new int[]{start, end});
                start = occupancyTimes[i][0];
                end = occupancyTimes[i][1];
            }
        }
        merged.add(new int[]{start, end});
        return merged;
    }
    
    /**
     * Part 3: Find bottleneck - longest truck queue window
     * Identify window when all docks are full
     */
    public static int[] findBottleneckWindow(int[][] truckOccupancy, int numDocks) {
        // Sort by arrival time
        Arrays.sort(truckOccupancy, (a, b) -> Integer.compare(a[0], b[0]));
        
        int maxWait = 0;
        int bottleneckStart = 0;
        int bottleneckEnd = 0;
        
        // Use timeline events to find peak concurrency
        List<int[]> events = new ArrayList<>();
        for (int[] occupancy : truckOccupancy) {
            events.add(new int[]{occupancy[0], 1});  // arrival
            events.add(new int[]{occupancy[1], -1}); // departure
        }
        
        events.sort((a, b) -> {
            if (a[0] != b[0]) return Integer.compare(a[0], b[0]);
            return Integer.compare(a[1], b[1]);
        });
        
        int currentTrucks = 0;
        for (int[] event : events) {
            currentTrucks += event[1];
            if (currentTrucks > maxWait) {
                maxWait = currentTrucks;
                bottleneckStart = event[0];
            }
        }
        
        return new int[]{bottleneckStart, maxWait};
    }
    
    public static void main(String[] args) {
        // Test Part 1
        int[] times = {100, 100, 150, 200};
        String[] truckIds = {"truck1", "truck2", "truck3", "truck4"};
        Map<Integer, List<String>> arrivals = parseArrivalLogs(times, truckIds);
        System.out.println("Arrivals: " + arrivals);
        
        // Test Part 2
        int[][] occupancy = {{100, 200}, {150, 250}, {200, 300}};
        List<int[]> merged = mergeDockOccupancy(occupancy);
        System.out.println("Merged occupancy:");
        for (int[] period : merged) {
            System.out.println("  [" + period[0] + ", " + period[1] + "]");
        }
        
        // Test Part 3
        int[] bottleneck = findBottleneckWindow(occupancy, 2);
        System.out.println("Bottleneck: time=" + bottleneck[0] + ", trucks=" + bottleneck[1]);
    }
}
