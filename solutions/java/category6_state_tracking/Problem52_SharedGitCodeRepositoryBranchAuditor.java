package category6_state_tracking;

import java.util.*;

/**
 * Problem 52: Shared Git Code Repository Branch Auditor
 * 
 * Three-part progression:
 * - Part 1: Parse hexadecimal commit hashes correctly
 * - Part 2: Trace branch history and divergence point
 * - Part 3: Detect merge conflicts
 */
public class Problem52_SharedGitCodeRepositoryBranchAuditor {
    
    static class Commit {
        String hash;
        String parent;
        String message;
        long timestamp;
        
        Commit(String hash, String parent, String message, long timestamp) {
            this.hash = hash;
            this.parent = parent;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Part 1: Parse Hexadecimal Hashes Correctly
     * Use proper hex string parsing, not basic integer parsing
     */
    public static Map<String, Commit> parseCommitLog(String[][] commitData) {
        Map<String, Commit> commits = new HashMap<>();
        
        for (String[] data : commitData) {
            String hash = data[0];  // Hexadecimal string
            String parent = data[1];
            String message = data[2];
            long timestamp = Long.parseLong(data[3]);
            
            // Verify hash is valid hex
            try {
                Long.parseLong(hash, 16);  // Parse as hexadecimal
                commits.put(hash, new Commit(hash, parent, message, timestamp));
            } catch (NumberFormatException e) {
                System.err.println("Invalid hash: " + hash);
            }
        }
        
        return commits;
    }
    
    /**
     * Part 2: Trace Branch History and Find Divergence Point
     */
    public static List<String> traceBranchHistory(String branchTip, 
                                                   Map<String, Commit> commits) {
        List<String> history = new ArrayList<>();
        String current = branchTip;
        Set<String> visited = new HashSet<>();
        
        while (current != null && !visited.contains(current)) {
            history.add(current);
            visited.add(current);
            Commit commit = commits.get(current);
            current = (commit != null) ? commit.parent : null;
        }
        
        return history;
    }
    
    /**
     * Find where two branches diverged
     */
    public static String findDivergencePoint(String branch1Tip, String branch2Tip,
                                            Map<String, Commit> commits) {
        List<String> history1 = traceBranchHistory(branch1Tip, commits);
        Set<String> history1Set = new HashSet<>(history1);
        
        List<String> history2 = traceBranchHistory(branch2Tip, commits);
        
        for (String commit : history2) {
            if (history1Set.contains(commit)) {
                return commit;  // Found common ancestor
            }
        }
        
        return null;
    }
    
    /**
     * Part 3: Detect Merge Conflicts
     * Identify files modified in both branches
     */
    public static Set<String> detectMergeConflicts(String branch1Tip, String branch2Tip,
                                                    Map<String, Commit> commits,
                                                    Map<String, Set<String>> commitToFiles) {
        String divergence = findDivergencePoint(branch1Tip, branch2Tip, commits);
        if (divergence == null) return new HashSet<>();
        
        List<String> history1 = traceBranchHistory(branch1Tip, commits);
        List<String> history2 = traceBranchHistory(branch2Tip, commits);
        
        Set<String> conflictFiles = new HashSet<>();
        Set<String> files1 = new HashSet<>();
        Set<String> files2 = new HashSet<>();
        
        // Collect files modified in branch1 since divergence
        for (String commit : history1) {
            if (commit.equals(divergence)) break;
            files1.addAll(commitToFiles.getOrDefault(commit, new HashSet<>()));
        }
        
        // Collect files modified in branch2 since divergence
        for (String commit : history2) {
            if (commit.equals(divergence)) break;
            files2.addAll(commitToFiles.getOrDefault(commit, new HashSet<>()));
        }
        
        // Find intersection - files modified in both branches
        files1.retainAll(files2);
        return files1;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 52: Git Repository Branch Auditor ===");
        
        String[][] commitData = {
            {"abc123def456", "fed654cba321", "Feature A", "1000"},
            {"fed654cba321", "123456789abc", "Base commit", "900"}
        };
        
        Map<String, Commit> commits = parseCommitLog(commitData);
        System.out.println("Commits parsed: " + commits.size());
        
        List<String> history = traceBranchHistory("abc123def456", commits);
        System.out.println("Branch history: " + history);
    }
}
