package category5_graph_traversal;

import java.util.*;

/**
 * Problem 44: Genealogic Common Ancestry Tracker
 * 
 * Three-part progression:
 * - Part 1: Fix infinite recursion from self-references
 * - Part 2: Find individuals with zero known parents
 * - Part 3: Find shared common ancestors
 */
public class Problem44_GenealogicCommonAncestryTracker {
    
    static class Person {
        String name;
        Set<String> parents;
        
        Person(String name) {
            this.name = name;
            this.parents = new HashSet<>();
        }
    }
    
    /**
     * Part 1: Bug Fix - Prevent Self-References
     * Guard against circular parent-child relationships
     */
    public static Map<String, Person> buildFamilyTree(String[][] parentChildPairs) {
        Map<String, Person> family = new HashMap<>();
        
        for (String[] pair : parentChildPairs) {
            String parent = pair[0];
            String child = pair[1];
            
            // Validate: parent cannot be same as child
            if (parent.equals(child)) {
                continue;  // Skip invalid self-reference
            }
            
            family.computeIfAbsent(parent, Person::new);
            family.computeIfAbsent(child, Person::new);
            family.get(child).parents.add(parent);
        }
        
        return family;
    }
    
    /**
     * Part 2: Find Roots (Zero Known Parents)
     */
    public static List<String> findRootIndividuals(Map<String, Person> family) {
        List<String> roots = new ArrayList<>();
        
        for (Person person : family.values()) {
            if (person.parents.isEmpty()) {
                roots.add(person.name);
            }
        }
        
        return roots;
    }
    
    /**
     * Part 3: Find Shared Ancestor
     */
    public static String findCommonAncestor(String person1, String person2, 
                                           Map<String, Person> family) {
        Set<String> ancestors1 = getAllAncestors(person1, family, new HashSet<>());
        Set<String> ancestors2 = getAllAncestors(person2, family, new HashSet<>());
        
        ancestors1.retainAll(ancestors2);
        
        if (ancestors1.isEmpty()) return null;
        return ancestors1.iterator().next();
    }
    
    private static Set<String> getAllAncestors(String person, Map<String, Person> family, 
                                               Set<String> visited) {
        Set<String> ancestors = new HashSet<>();
        
        if (!family.containsKey(person) || visited.contains(person)) {
            return ancestors;
        }
        
        visited.add(person);
        Person p = family.get(person);
        
        for (String parent : p.parents) {
            ancestors.add(parent);
            ancestors.addAll(getAllAncestors(parent, family, visited));
        }
        
        return ancestors;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 44: Genealogic Common Ancestry ===");
        
        String[][] pairs = {
            {"John", "Alice"},
            {"John", "Bob"},
            {"Alice", "Charlie"},
            {"Bob", "David"}
        };
        
        Map<String, Person> family = buildFamilyTree(pairs);
        System.out.println("Family tree built");
        
        List<String> roots = findRootIndividuals(family);
        System.out.println("Root individuals: " + roots);
        
        String ancestor = findCommonAncestor("Charlie", "David", family);
        System.out.println("Common ancestor of Charlie and David: " + ancestor);
    }
}
