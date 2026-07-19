package category5_graph_traversal;

import java.util.*;

/**
 * Problem 45: Corporate Org Chart Hierarchy Scanner
 * 
 * Three-part progression:
 * - Part 1: Prevent infinite loops from circular reporting
 * - Part 2: Build management chain to CEO
 * - Part 3: Find lowest common manager
 */
public class Problem45_CorporateOrgChartHierarchyScanner {
    
    /**
     * Part 1: Bug Fix - Circular Loop Prevention
     * Use visited set to detect and prevent circular manager chains
     */
    public static Map<String, String> buildOrgChart(String[][] employeeManagerPairs) {
        Map<String, String> orgChart = new HashMap<>();
        
        for (String[] pair : employeeManagerPairs) {
            String employee = pair[0];
            String manager = pair[1];
            
            // Check for direct circular reference
            if (employee.equals(manager)) {
                continue;
            }
            
            orgChart.put(employee, manager);
        }
        
        return orgChart;
    }
    
    /**
     * Part 2: Get Full Management Path to CEO
     */
    public static List<String> getManagementChain(String employee, 
                                                   Map<String, String> orgChart) {
        List<String> chain = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        String current = employee;
        
        while (current != null && !visited.contains(current)) {
            chain.add(current);
            visited.add(current);
            current = orgChart.get(current);
        }
        
        return chain;
    }
    
    /**
     * Part 3: Find Lowest Common Manager
     */
    public static String findLowestCommonManager(String employee1, String employee2,
                                                 Map<String, String> orgChart) {
        List<String> chain1 = getManagementChain(employee1, orgChart);
        Set<String> chain1Set = new HashSet<>(chain1);
        
        List<String> chain2 = getManagementChain(employee2, orgChart);
        
        for (String person : chain2) {
            if (chain1Set.contains(person)) {
                return person;
            }
        }
        
        return null;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 45: Corporate Org Chart Hierarchy ===");
        
        String[][] pairs = {
            {"Alice", "Bob"},
            {"Bob", "Charlie"},
            {"Charlie", "David"},
            {"Eve", "Bob"}
        };
        
        Map<String, String> orgChart = buildOrgChart(pairs);
        System.out.println("Org chart built");
        
        List<String> chain = getManagementChain("Alice", orgChart);
        System.out.println("Management chain for Alice: " + chain);
        
        String lcm = findLowestCommonManager("Alice", "Eve", orgChart);
        System.out.println("Lowest common manager: " + lcm);
    }
}
