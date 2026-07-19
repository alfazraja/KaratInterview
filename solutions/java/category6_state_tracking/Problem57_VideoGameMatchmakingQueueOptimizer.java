package category6_state_tracking;

import java.util.*;

/**
 * Problem 57: Video Game Matchmaking Queue Optimizer
 * 
 * Three-part progression:
 * - Part 1: Validate tick count chronology
 * - Part 2: Calculate concurrent queue load
 * - Part 3: Dynamic skill-based grouping
 */
public class Problem57_VideoGameMatchmakingQueueOptimizer {
    
    static class PlayerQueue {
        String playerId;
        long startTick;
        long endTick;
        int skillRating;
        
        PlayerQueue(String playerId, long startTick, long endTick, int skillRating) {
            this.playerId = playerId;
            this.startTick = startTick;
            this.endTick = endTick;
            this.skillRating = skillRating;
        }
    }
    
    /**
     * Part 1: Bug Fix - Validate Chronological Ticks
     * Ensure start tick occurs before end tick
     */
    public static List<PlayerQueue> validateQueueLogs(String[][] queueData) {
        List<PlayerQueue> validQueues = new ArrayList<>();
        
        for (String[] data : queueData) {
            String playerId = data[0];
            long startTick = Long.parseLong(data[1]);
            long endTick = Long.parseLong(data[2]);
            int skillRating = Integer.parseInt(data[3]);
            
            if (startTick < endTick) {
                validQueues.add(new PlayerQueue(playerId, startTick, endTick, skillRating));
            }
        }
        
        return validQueues;
    }
    
    /**
     * Part 2: Merge Queue Windows for Peak Analysis
     * Calculate concurrent server load during peak tournament hours
     */
    public static int calculatePeakConcurrentLoad(List<PlayerQueue> queues) {
        if (queues.isEmpty()) return 0;
        
        List<long[]> events = new ArrayList<>();
        
        for (PlayerQueue queue : queues) {
            events.add(new long[]{queue.startTick, 1});   // Player joins
            events.add(new long[]{queue.endTick, -1});    // Player leaves
        }
        
        events.sort((a, b) -> {
            if (a[0] != b[0]) return Long.compare(a[0], b[0]);
            return Long.compare(a[1], b[1]);  // Leaves before joins
        });
        
        int maxLoad = 0;
        int currentLoad = 0;
        
        for (long[] event : events) {
            currentLoad += event[1];
            maxLoad = Math.max(maxLoad, currentLoad);
        }
        
        return maxLoad;
    }
    
    /**
     * Part 3: Dynamic Skill Grouping for Lobbies
     * Create 4-player balanced lobbies
     */
    public static List<List<String>> createBalancedLobbies(List<PlayerQueue> queues,
                                                           int skillTolerance) {
        List<List<String>> lobbies = new ArrayList<>();
        List<PlayerQueue> waiting = new ArrayList<>(queues);
        waiting.sort(Comparator.comparingInt(q -> q.skillRating));
        
        while (waiting.size() >= 4) {
            List<String> lobby = new ArrayList<>();
            PlayerQueue first = waiting.remove(0);
            lobby.add(first.playerId);
            
            // Find 3 players within skill tolerance
            for (int i = 0; i < waiting.size() && lobby.size() < 4; i++) {
                PlayerQueue player = waiting.get(i);
                if (Math.abs(player.skillRating - first.skillRating) <= skillTolerance) {
                    lobby.add(player.playerId);
                    waiting.remove(i);
                    i--;  // Adjust index after removal
                }
            }
            
            if (lobby.size() == 4) {
                lobbies.add(lobby);
            } else {
                waiting.add(0, first);  // Put back if couldn't form lobby
                break;
            }
        }
        
        return lobbies;
    }
    
    /**
     * Expand skill tolerance for long-waiting players
     */
    public static List<List<String>> expandMatchingForWaiters(List<PlayerQueue> queues,
                                                              int baseSkillTolerance,
                                                              long maxWaitTicks) {
        long currentTick = System.currentTimeMillis();
        List<PlayerQueue> expandedQueues = new ArrayList<>();
        
        for (PlayerQueue queue : queues) {
            long waitTime = currentTick - queue.startTick;
            int tolerance = baseSkillTolerance;
            
            if (waitTime > maxWaitTicks) {
                tolerance = (int) (baseSkillTolerance * 1.5);  // Expand tolerance
            }
            
            expandedQueues.add(queue);
        }
        
        return createBalancedLobbies(expandedQueues, baseSkillTolerance);
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 57: Video Game Matchmaking Queue ===");
        
        String[][] queueData = {
            {"Player1", "1000", "5000", "1500"},
            {"Player2", "2000", "6000", "1550"},
            {"Player3", "1500", "7000", "1480"},
            {"Player4", "3000", "8000", "1520"}
        };
        
        List<PlayerQueue> queues = validateQueueLogs(queueData);
        System.out.println("Valid queues: " + queues.size());
        
        int peakLoad = calculatePeakConcurrentLoad(queues);
        System.out.println("Peak concurrent load: " + peakLoad + " players");
        
        List<List<String>> lobbies = createBalancedLobbies(queues, 100);
        System.out.println("Lobbies created: " + lobbies.size());
    }
}
