package category5_graph_traversal;

import java.util.*;

/**
 * Problem 42: Automated Assembly Line Dependency Tracker
 * 
 * Three-part progression:
 * - Part 1: Fix reversed arrow relationship mapping
 * - Part 2: Topological Sort for valid execution order
 * - Part 3: Calculate minimum time with parallel execution
 */
public class Problem42_AutomatedAssemblyLineDependencyTracker {
    
    /**
     * Part 1: Bug Fix - Correct dependency direction
     * Issue: Arrow relationships are mapped backward
     * Solution: Ensure dependencies flow in correct direction
     */
    public static Map<String, List<String>> parseManufacturingTasks(String[] dependencies) {
        Map<String, List<String>> graph = new HashMap<>();
        
        for (String dependency : dependencies) {
            // Format: "Step_A -> Step_B" means Step_A must complete before Step_B
            String[] parts = dependency.split(" -> ");
            String prerequisite = parts[0].trim();
            String dependent = parts[1].trim();
            
            // Correct direction: prerequisite -> dependent
            graph.computeIfAbsent(prerequisite, k -> new ArrayList<>()).add(dependent);
            graph.computeIfAbsent(dependent, k -> new ArrayList<>());
        }
        
        return graph;
    }
    
    /**
     * Part 2: Topological Sort for Valid Execution Order
     * Generate sequence where all prerequisites complete before dependents
     */
    public static List<String> getExecutionOrder(Map<String, List<String>> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        
        // Initialize in-degrees
        for (String task : graph.keySet()) {
            inDegree.put(task, 0);
        }
        
        for (List<String> dependents : graph.values()) {
            for (String dependent : dependents) {
                inDegree.put(dependent, inDegree.getOrDefault(dependent, 0) + 1);
            }
        }
        
        // Add tasks with no prerequisites
        for (String task : inDegree.keySet()) {
            if (inDegree.get(task) == 0) {
                queue.offer(task);
            }
        }
        
        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            
            for (String dependent : graph.getOrDefault(current, new ArrayList<>())) {
                inDegree.put(dependent, inDegree.get(dependent) - 1);
                if (inDegree.get(dependent) == 0) {
                    queue.offer(dependent);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Part 3: Parallel Time Estimator
     * Calculate minimum time with two parallel robot arms
     * Each task takes exactly 1 minute
     */
    public static int calculateMinimumTime(Map<String, List<String>> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Integer> completionTime = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        
        // Initialize
        for (String task : graph.keySet()) {
            inDegree.put(task, 0);
            completionTime.put(task, 0);
        }
        
        for (List<String> dependents : graph.values()) {
            for (String dependent : dependents) {
                inDegree.put(dependent, inDegree.getOrDefault(dependent, 0) + 1);
            }
        }
        
        for (String task : inDegree.keySet()) {
            if (inDegree.get(task) == 0) {
                queue.offer(task);
            }
        }
        
        int time = 0;
        List<String> currentLevel = new ArrayList<>();
        
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            currentLevel.clear();
            
            for (int i = 0; i < levelSize; i++) {
                String current = queue.poll();
                currentLevel.add(current);
                completionTime.put(current, time + 1);
                
                for (String dependent : graph.getOrDefault(current, new ArrayList<>())) {
                    inDegree.put(dependent, inDegree.get(dependent) - 1);
                    if (inDegree.get(dependent) == 0) {
                        queue.offer(dependent);
                    }
                }
            }
            time++;
        }
        
        return time;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 42: Assembly Line Dependency Tracker ===");
        
        String[] dependencies = {
            "Step_A -> Step_B",
            "Step_A -> Step_C",
            "Step_B -> Step_D",
            "Step_C -> Step_D"
        };
        
        Map<String, List<String>> graph = parseManufacturingTasks(dependencies);
        System.out.println("Graph: " + graph);
        
        List<String> order = getExecutionOrder(graph);
        System.out.println("Execution order: " + order);
        
        int minTime = calculateMinimumTime(graph);
        System.out.println("Minimum time with parallel execution: " + minTime);
    }
}
