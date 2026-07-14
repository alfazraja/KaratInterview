package category2_interval_math;

import java.util.*;

public class Problem18_MovieTheaterScreenShowtimer {
    
    static class Showtime {
        int startTime;
        int endTime;
        int cleaningBuffer;  // Minutes needed after movie
        
        Showtime(int start, int runtime, int cleaning) {
            this.startTime = start;
            this.endTime = start + runtime;
            this.cleaningBuffer = cleaning;
        }
    }
    
    /**
     * Part 1: Fix intermission calculation bug
     * Cleaning buffer should be added to END time, not start time
     */
    public static int calculateScreenOccupancy(Showtime show) {
        // Correct: showtime + cleaning buffer applied to end
        return show.endTime + show.cleaningBuffer;
    }
    
    /**
     * Part 2: Merge showtimes with cleaning intervals
     * Return consolidated blocks showing when screen is occupied
     */
    public static List<int[]> getScreenOccupancyBlocks(List<Showtime> showtimes) {
        if (showtimes.isEmpty()) return new ArrayList<>();
        
        // Sort by start time
        showtimes.sort((a, b) -> Integer.compare(a.startTime, b.startTime));
        
        List<int[]> blocks = new ArrayList<>();
        int blockStart = showtimes.get(0).startTime;
        int blockEnd = showtimes.get(0).endTime + showtimes.get(0).cleaningBuffer;
        
        for (int i = 1; i < showtimes.size(); i++) {
            Showtime show = showtimes.get(i);
            int showEnd = show.endTime + show.cleaningBuffer;
            
            if (show.startTime <= blockEnd) {
                // Overlapping - merge
                blockEnd = Math.max(blockEnd, showEnd);
            } else {
                // Gap - save block
                blocks.add(new int[]{blockStart, blockEnd});
                blockStart = show.startTime;
                blockEnd = showEnd;
            }
        }
        blocks.add(new int[]{blockStart, blockEnd});
        return blocks;
    }
    
    /**
     * Part 3: Find optimal movie sequence for max profit
     * Given movie runtimes and profit margins, maximize profit in operational window
     */
    public static int maxProfitScheduling(int[] runtimes, int[] profits, int operationalWindow) {
        int n = runtimes.length;
        
        // Create array of (runtime, profit, index)
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }
        
        // Sort by profit descending
        Arrays.sort(indices, (a, b) -> Integer.compare(profits[b], profits[a]));
        
        int maxProfit = 0;
        int timeUsed = 0;
        
        for (int idx : indices) {
            if (timeUsed + runtimes[idx] <= operationalWindow) {
                timeUsed += runtimes[idx];
                maxProfit += profits[idx];
            }
        }
        
        return maxProfit;
    }
    
    public static void main(String[] args) {
        // Test Part 1
        Showtime show = new Showtime(600, 120, 15);
        System.out.println("Screen occupied until: " + calculateScreenOccupancy(show));
        
        // Test Part 2
        List<Showtime> shows = Arrays.asList(
            new Showtime(600, 120, 15),
            new Showtime(740, 90, 15),
            new Showtime(900, 150, 15)
        );
        List<int[]> blocks = getScreenOccupancyBlocks(shows);
        System.out.println("Screen occupancy blocks:");
        for (int[] block : blocks) {
            System.out.println("  [" + block[0] + ", " + block[1] + "]");
        }
        
        // Test Part 3
        int[] runtimes = {60, 90, 120};
        int[] profits = {100, 150, 200};
        int maxProfit = maxProfitScheduling(runtimes, profits, 200);
        System.out.println("Max profit: " + maxProfit);
    }
}
