package category2_interval_math;

import java.util.*;

public class Problem16_CloudComputingBatchJobScheduler {
    
    // Part 1: Validate and fix corrupted job inputs
    static class Job {
        int startTime;
        int endTime;
        
        Job(int start, int end) {
            this.startTime = start;
            this.endTime = end;
        }
    }
    
    /**
     * Part 1: Bug Fix - Validate job inputs
     * Reject jobs where end time occurs before start time
     */
    public static List<Job> validateJobs(int[][] jobs) {
        List<Job> validJobs = new ArrayList<>();
        for (int[] job : jobs) {
            if (job[0] < job[1]) {  // Valid: start < end
                validJobs.add(new Job(job[0], job[1]));
            }
        }
        return validJobs;
    }
    
    /**
     * Part 2: Merge overlapping job intervals
     * Find periods when CPU is running continuously
     */
    public static List<int[]> mergeJobIntervals(List<Job> jobs) {
        if (jobs.isEmpty()) return new ArrayList<>();
        
        // Sort by start time
        jobs.sort((a, b) -> Integer.compare(a.startTime, b.startTime));
        
        List<int[]> merged = new ArrayList<>();
        int currentStart = jobs.get(0).startTime;
        int currentEnd = jobs.get(0).endTime;
        
        for (int i = 1; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            if (job.startTime <= currentEnd) {
                // Overlapping or adjacent - merge
                currentEnd = Math.max(currentEnd, job.endTime);
            } else {
                // No overlap - save and start new
                merged.add(new int[]{currentStart, currentEnd});
                currentStart = job.startTime;
                currentEnd = job.endTime;
            }
        }
        merged.add(new int[]{currentStart, currentEnd});
        return merged;
    }
    
    /**
     * Part 3: Cost minimization
     * Calculate optimal delay for background jobs
     * Server cost = active_minutes
     * Find best delay to reduce total cost
     */
    public static int calculateOptimalDelay(List<int[]> mergedIntervals, int delayWindow) {
        int minCost = Integer.MAX_VALUE;
        int bestDelay = 0;
        
        // Try different delays
        for (int delay = 0; delay <= delayWindow; delay++) {
            int totalCost = 0;
            for (int[] interval : mergedIntervals) {
                totalCost += (interval[1] - interval[0]);
            }
            
            if (totalCost < minCost) {
                minCost = totalCost;
                bestDelay = delay;
            }
        }
        
        return bestDelay;
    }
    
    public static void main(String[] args) {
        // Test Part 1
        int[][] jobs = {{100, 200}, {150, 250}, {50, 100}};
        List<Job> validJobs = validateJobs(jobs);
        System.out.println("Valid jobs: " + validJobs.size());
        
        // Test Part 2
        List<int[]> merged = mergeJobIntervals(validJobs);
        System.out.println("Merged intervals:");
        for (int[] interval : merged) {
            System.out.println("  [" + interval[0] + ", " + interval[1] + "]");
        }
        
        // Test Part 3
        int optimalDelay = calculateOptimalDelay(merged, 100);
        System.out.println("Optimal delay: " + optimalDelay);
    }
}
