package category5_graph_traversal;

import java.util.*;

/**
 * Problem 46: Software Build System Compilation Graph
 * 
 * Three-part progression:
 * - Part 1: Validate file existence before compilation
 * - Part 2: Topological sort for compilation order
 * - Part 3: Track incremental changes for minimal recompilation
 */
public class Problem46_SoftwareBuildSystemCompilationGraph {
    
    static class FileNode {
        String filename;
        Set<String> dependencies;
        boolean modified;
        
        FileNode(String filename) {
            this.filename = filename;
            this.dependencies = new HashSet<>();
            this.modified = false;
        }
    }
    
    /**
     * Part 1: Validate Dependencies
     * Check file existence and ignore missing dependencies
     */
    public static Map<String, FileNode> buildBuildGraph(String[] files, 
                                                         String[][] fileDependencies) {
        Set<String> validFiles = new HashSet<>(Arrays.asList(files));
        Map<String, FileNode> graph = new HashMap<>();
        
        for (String file : files) {
            graph.put(file, new FileNode(file));
        }
        
        for (String[] dep : fileDependencies) {
            String source = dep[0];
            String target = dep[1];
            
            if (validFiles.contains(source) && validFiles.contains(target)) {
                graph.get(source).dependencies.add(target);
            }
        }
        
        return graph;
    }
    
    /**
     * Part 2: Topological Sort for Compilation Order
     */
    public static List<String> getCompilationOrder(Map<String, FileNode> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        
        for (String file : graph.keySet()) {
            inDegree.put(file, 0);
        }
        
        for (FileNode node : graph.values()) {
            for (String dep : node.dependencies) {
                inDegree.put(dep, inDegree.getOrDefault(dep, 0) + 1);
            }
        }
        
        for (String file : inDegree.keySet()) {
            if (inDegree.get(file) == 0) {
                queue.offer(file);
            }
        }
        
        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            
            for (String dep : graph.get(current).dependencies) {
                inDegree.put(dep, inDegree.get(dep) - 1);
                if (inDegree.get(dep) == 0) {
                    queue.offer(dep);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Part 3: Incremental Build - Find files to recompile
     */
    public static Set<String> findFilesToRecompile(String modifiedFile, 
                                                    Map<String, FileNode> graph) {
        Set<String> toRecompile = new HashSet<>();
        dfsRecompile(modifiedFile, graph, toRecompile, new HashSet<>());
        return toRecompile;
    }
    
    private static void dfsRecompile(String file, Map<String, FileNode> graph,
                                     Set<String> toRecompile, Set<String> visited) {
        if (visited.contains(file)) return;
        visited.add(file);
        toRecompile.add(file);
        
        // Find all files that depend on this one
        for (FileNode node : graph.values()) {
            if (node.dependencies.contains(file) && !visited.contains(node.filename)) {
                dfsRecompile(node.filename, graph, toRecompile, visited);
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 46: Software Build System ===");
        
        String[] files = {"main.cpp", "util.cpp", "core.cpp", "ui.cpp"};
        String[][] deps = {
            {"util.cpp", "core.cpp"},
            {"core.cpp", "main.cpp"},
            {"ui.cpp", "main.cpp"}
        };
        
        Map<String, FileNode> graph = buildBuildGraph(files, deps);
        System.out.println("Build graph created");
        
        List<String> order = getCompilationOrder(graph);
        System.out.println("Compilation order: " + order);
        
        Set<String> recompile = findFilesToRecompile("core.cpp", graph);
        System.out.println("Files to recompile if core.cpp changes: " + recompile);
    }
}
