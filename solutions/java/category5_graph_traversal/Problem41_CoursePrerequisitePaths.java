package category5_graph_traversal;

import java.util.*;

/**
 * Problem 41: Course Pre-requisite Paths
 * 
 * Three-part progression:
 * - Part 1: Fix NullPointerException in graph construction
 * - Part 2: Find middle course in dependency chain
 * - Part 3: Detect circular prerequisite loops
 */
public class Problem41_CoursePrerequisitePaths {
    
    /**
     * Part 1: Bug Fix - NullPointerException Prevention
     * Issue: Appending to a neighbor list before initializing the map slot
     * Solution: Guard initialization with computeIfAbsent
     */
    public static Map<String, List<String>> buildDependencyGraph(String[][] coursePairs) {
        Map<String, List<String>> graph = new HashMap<>();
        
        for (String[] pair : coursePairs) {
            String courseA = pair[0];  // Prerequisite
            String courseB = pair[1];  // Depends on A
            
            // Fixed: Initialize list before appending
            graph.computeIfAbsent(courseA, k -> new ArrayList<>()).add(courseB);
            graph.computeIfAbsent(courseB, k -> new ArrayList<>());  // Ensure vertex exists
        }
        
        return graph;
    }
    
    /**
     * Part 2: Find Middle Course in Academic Timeline
     * Given prerequisite chain, find the center course(s)
     */
    public static String findMiddleCourse(Map<String, List<String>> graph) {
        if (graph.isEmpty()) return null;
        
        // Find the starting course (one with no prerequisites)
        String startCourse = null;
        Set<String> dependents = new HashSet<>();
        
        for (List<String> deps : graph.values()) {
            dependents.addAll(deps);
        }
        
        for (String course : graph.keySet()) {
            if (!dependents.contains(course)) {
                startCourse = course;
                break;
            }
        }
        
        if (startCourse == null) return null;
        
        // Trace the linear path
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        String current = startCourse;
        
        while (current != null && !visited.contains(current)) {
            path.add(current);
            visited.add(current);
            
            List<String> neighbors = graph.getOrDefault(current, new ArrayList<>());
            current = neighbors.isEmpty() ? null : neighbors.get(0);
        }
        
        // Return middle element
        if (path.isEmpty()) return null;
        return path.get(path.size() / 2);
    }
    
    /**
     * Part 3: Detect Circular Prerequisite Loops
     * Use DFS with color coding: 0=white, 1=gray, 2=black
     */
    public static boolean hasCyclicPrerequisite(Map<String, List<String>> graph) {
        Map<String, Integer> color = new HashMap<>();
        
        for (String course : graph.keySet()) {
            if (!color.containsKey(course)) {
                if (hasCycleDFS(course, graph, color)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static boolean hasCycleDFS(String course, Map<String, List<String>> graph, 
                                       Map<String, Integer> color) {
        color.put(course, 1);  // Mark as gray (in progress)
        
        List<String> neighbors = graph.getOrDefault(course, new ArrayList<>());
        for (String neighbor : neighbors) {
            if (!color.containsKey(neighbor)) {
                if (hasCycleDFS(neighbor, graph, color)) {
                    return true;
                }
            } else if (color.get(neighbor) == 1) {
                // Back edge found - cycle detected
                return true;
            }
        }
        
        color.put(course, 2);  // Mark as black (completed)
        return false;
    }
    
    /**
     * Get topological sort of courses (valid graduation order)
     */
    public static List<String> topologicalSort(Map<String, List<String>> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        
        // Initialize in-degrees
        for (String course : graph.keySet()) {
            inDegree.put(course, 0);
        }
        
        for (List<String> neighbors : graph.values()) {
            for (String neighbor : neighbors) {
                inDegree.put(neighbor, inDegree.getOrDefault(neighbor, 0) + 1);
            }
        }
        
        // Add all courses with in-degree 0
        for (String course : inDegree.keySet()) {
            if (inDegree.get(course) == 0) {
                queue.offer(course);
            }
        }
        
        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            
            for (String neighbor : graph.getOrDefault(current, new ArrayList<>())) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 41: Course Prerequisite Paths ===\n");
        
        // Test Part 1: Build graph
        String[][] coursePairs = {
            {"CS101", "CS201"},
            {"CS201", "CS301"},
            {"CS301", "CS401"}
        };
        Map<String, List<String>> graph = buildDependencyGraph(coursePairs);
        System.out.println("Graph built successfully: " + graph);
        System.out.println();
        
        // Test Part 2: Find middle course
        String middle = findMiddleCourse(graph);
        System.out.println("Middle course: " + middle);
        System.out.println();
        
        // Test Part 3: Detect cycles
        boolean hasCycle = hasCyclicPrerequisite(graph);
        System.out.println("Has cycle: " + hasCycle);
        System.out.println();
        
        // Topological sort
        List<String> topo = topologicalSort(graph);
        System.out.println("Topological order: " + topo);
        System.out.println();
        
        // Test with cycle
        String[][] cyclicPairs = {
            {"A", "B"},
            {"B", "C"},
            {"C", "A"}
        };
        Map<String, List<String>> cyclicGraph = buildDependencyGraph(cyclicPairs);
        boolean cycleFound = hasCyclicPrerequisite(cyclicGraph);
        System.out.println("Cyclic graph has cycle: " + cycleFound);
    }
}
