package category2_intervals_concurrency;

import java.util.*;

/**
 * Problem 14: Cloud Computing Batch Job Scheduler
 * 
 * Three-part progression:
 * - Part 1: Validate job end time >= start time
 * - Part 2: Merge overlapping job execution windows
 * - Part 3: Find optimal delay to minimize hosting costs
 */
public class Problem14_CloudComputingBatchJobScheduler {

    /**
     * Part 1: Bug Fix - Job Time Validation
     * Issue: Crashes when end time < start time (data entry errors)
     * Solution: Reject corrupted inputs with validation
     */
    public static class Part1_BugFix {
        public static class BatchJob {
            public String jobId;
            public long startTime;  // epoch seconds
            public long endTime;    // epoch seconds
            public String priority; // URGENT, NORMAL, LOW

            public BatchJob(String jobId, long startTime, long endTime, String priority) {
                if (endTime < startTime) {
                    throw new IllegalArgumentException(
                        String.format("Invalid job times: end(%d) < start(%d)", endTime, startTime));
                }
                this.jobId = jobId;
                this.startTime = startTime;
                this.endTime = endTime;
                this.priority = priority;
            }

            public long getDuration() {
                return endTime - startTime;
            }

            @Override
            public String toString() {
                return String.format("%s: %d-%d (%.1f min, %s)",
                    jobId, startTime, endTime, getDuration() / 60.0, priority);
            }
        }

        /**
         * Validate job time range
         */
        public static boolean isValidJobTime(long startTime, long endTime) {
            return endTime >= startTime && startTime >= 0 && endTime >= 0;
        }
    }

    /**
     * Part 2: CPU Continuity Analysis
     * Find periods when CPU is running continuously
     */
    public static class Part2_ContinuityAnalysis {
        public static class CPUBlock {
            public long startTime;
            public long endTime;
            public long duration;
            public int jobCount;

            public CPUBlock(long startTime, long endTime, int jobCount) {
                this.startTime = startTime;
                this.endTime = endTime;
                this.duration = endTime - startTime;
                this.jobCount = jobCount;
            }

            @Override
            public String toString() {
                return String.format("%d-%d (%.1f min, %d jobs)", startTime, endTime, duration / 60.0, jobCount);
            }
        }

        /**
         * Merge overlapping job execution windows
         * Time Complexity: O(n log n)
         */
        public static List<CPUBlock> mergeCPURuntimes(List<Part1_BugFix.BatchJob> jobs) {
            if (jobs == null || jobs.isEmpty()) {
                return new ArrayList<>();
            }

            // Sort by start time
            List<Part1_BugFix.BatchJob> sorted = new ArrayList<>(jobs);
            sorted.sort((a, b) -> Long.compare(a.startTime, b.startTime));

            List<CPUBlock> blocks = new ArrayList<>();
            long currentStart = sorted.get(0).startTime;
            long currentEnd = sorted.get(0).endTime;
            int jobCount = 1;

            for (int i = 1; i < sorted.size(); i++) {
                Part1_BugFix.BatchJob next = sorted.get(i);

                if (next.startTime <= currentEnd) {
                    // Overlapping - merge
                    currentEnd = Math.max(currentEnd, next.endTime);
                    jobCount++;
                } else {
                    // Gap found - save current block
                    blocks.add(new CPUBlock(currentStart, currentEnd, jobCount));
                    currentStart = next.startTime;
                    currentEnd = next.endTime;
                    jobCount = 1;
                }
            }
            blocks.add(new CPUBlock(currentStart, currentEnd, jobCount));

            return blocks;
        }
    }

    /**
     * Part 3: Cost Minimization
     * Find optimal delay for non-urgent jobs to reduce costs
     */
    public static class Part3_CostMinimization {
        public static class DelayStrategy {
            public long optimalDelay;  // seconds to delay job
            public long costSavings;   // in cents (cost per minute * savings)
            public String reason;

            public DelayStrategy(long optimalDelay, long costSavings, String reason) {
                this.optimalDelay = optimalDelay;
                this.costSavings = costSavings;
                this.reason = reason;
            }

            @Override
            public String toString() {
                return String.format("Delay: %.1f min, Savings: $%.2f, Reason: %s",
                    optimalDelay / 60.0, costSavings / 100.0, reason);
            }
        }

        /**
         * Find optimal delay for non-urgent batch job
         * Costs: $1 per minute of CPU runtime
         */
        public static DelayStrategy findOptimalDelay(
            List<Part1_BugFix.BatchJob> urgentJobs,
            Part1_BugFix.BatchJob nonUrgentJob) {
            
            if (urgentJobs == null || urgentJobs.isEmpty()) {
                return new DelayStrategy(0, 0, "No concurrent urgent jobs");
            }

            // Find gaps in urgent job schedule
            List<Part2_ContinuityAnalysis.CPUBlock> blocks =
                Part2_ContinuityAnalysis.mergeCPURuntimes(urgentJobs);

            long jobDuration = nonUrgentJob.getDuration();
            long bestGap = Long.MAX_VALUE;
            long bestSavings = 0;

            // Check gaps between urgent jobs
            for (int i = 0; i < blocks.size() - 1; i++) {
                long gapStart = blocks.get(i).endTime;
                long gapEnd = blocks.get(i + 1).startTime;
                long gapDuration = gapEnd - gapStart;

                if (gapDuration >= jobDuration) {
                    // Job fits in gap
                    // Delay to end of current block
                    long delay = gapStart - nonUrgentJob.startTime;
                    // Savings: cost of overlapping period
                    long savingsAmount = Math.min(jobDuration, blocks.get(i).duration);
                    
                    if (delay >= 0 && savingsAmount > bestSavings) {
                        bestGap = delay;
                        bestSavings = savingsAmount; // in seconds, convert to cents
                    }
                }
            }

            if (bestGap == Long.MAX_VALUE) {
                return new DelayStrategy(0, 0, "No suitable gap found");
            }

            return new DelayStrategy(bestGap, bestSavings * 100, "Moved to gap between urgent jobs");
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 14: Cloud Computing Batch Job Scheduler ===");

        // Part 1: Test validation
        System.out.println("\n--- Part 1: Job Time Validation ---");
        long[][] timeRanges = {
            {100, 200},   // valid
            {200, 200},   // valid (0 duration)
            {300, 250}    // invalid (end < start)
        };
        for (long[] range : timeRanges) {
            try {
                Part1_BugFix.BatchJob job = new Part1_BugFix.BatchJob(
                    "job_" + range[0], range[0], range[1], "NORMAL");
                System.out.println("  " + job + " - VALID");
            } catch (IllegalArgumentException e) {
                System.out.println("  INVALID: " + e.getMessage());
            }
        }

        // Part 2: Test CPU continuity
        System.out.println("\n--- Part 2: CPU Runtime Blocks ---");
        List<Part1_BugFix.BatchJob> jobs = Arrays.asList(
            new Part1_BugFix.BatchJob("job1", 1000, 1600, "URGENT"),
            new Part1_BugFix.BatchJob("job2", 1200, 1800, "URGENT"),
            new Part1_BugFix.BatchJob("job3", 2400, 3000, "URGENT")
        );
        List<Part2_ContinuityAnalysis.CPUBlock> blocks =
            Part2_ContinuityAnalysis.mergeCPURuntimes(jobs);
        System.out.println("Merged CPU blocks:");
        for (Part2_ContinuityAnalysis.CPUBlock block : blocks) {
            System.out.println("  " + block);
        }

        // Part 3: Test cost optimization
        System.out.println("\n--- Part 3: Cost Optimization ---");
        Part1_BugFix.BatchJob nonUrgent =
            new Part1_BugFix.BatchJob("batch1", 1400, 1700, "LOW");
        Part3_CostMinimization.DelayStrategy strategy =
            Part3_CostMinimization.findOptimalDelay(jobs, nonUrgent);
        System.out.println("  " + strategy);
    }
}
