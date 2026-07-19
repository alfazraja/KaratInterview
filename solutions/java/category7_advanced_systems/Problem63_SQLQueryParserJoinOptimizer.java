package category7_advanced_systems;

import java.util.*;

/**
 * Problem 63: SQL Query Parser Join Optimizer
 * 
 * Three-part progression:
 * - Part 1: Track bracket nesting in complex queries
 * - Part 2: Build dependency tree for table joins
 * - Part 3: Optimize join order for minimal index scans
 */
public class Problem63_SQLQueryParserJoinOptimizer {
    
    static class TableNode {
        String tableName;
        List<String> foreignKeys;
        int estimatedRows;
        
        TableNode(String name, int rows) {
            this.tableName = name;
            this.foreignKeys = new ArrayList<>();
            this.estimatedRows = rows;
        }
    }
    
    /**
     * Part 1: Bug Fix - Track Bracket Nesting
     * Properly handle nested subqueries
     */
    public static boolean validateQueryBrackets(String query) {
        Stack<Character> stack = new Stack<>();
        boolean inQuote = false;
        char quoteChar = '\0';
        
        for (int i = 0; i < query.length(); i++) {
            char ch = query.charAt(i);
            
            // Handle string literals
            if ((ch == '\'' || ch == '"') && (i == 0 || query.charAt(i-1) != '\\')) {
                if (!inQuote) {
                    inQuote = true;
                    quoteChar = ch;
                } else if (ch == quoteChar) {
                    inQuote = false;
                }
                continue;
            }
            
            if (inQuote) continue;
            
            // Track brackets
            if (ch == '(' || ch == '[') {
                stack.push(ch);
            } else if (ch == ')' || ch == ']') {
                if (stack.isEmpty()) return false;
                char opening = stack.pop();
                if ((ch == ')' && opening != '(') || (ch == ']' && opening != '[')) {
                    return false;
                }
            }
        }
        
        return stack.isEmpty() && !inQuote;
    }
    
    /**
     * Part 2: Build Dependency Tree
     * Parse JOIN relationships
     */
    public static class QueryParser {
        Map<String, TableNode> tables = new HashMap<>();
        
        public void addTable(String name, int estimatedRows) {
            tables.put(name, new TableNode(name, estimatedRows));
        }
        
        public void addJoinRelationship(String table1, String table2, String foreignKey) {
            TableNode node = tables.get(table1);
            if (node != null) {
                node.foreignKeys.add(table2);
            }
        }
        
        public List<String> getOptimalJoinOrder() {
            // Order tables by estimated rows (smallest first)
            List<TableNode> sorted = new ArrayList<>(tables.values());
            sorted.sort(Comparator.comparingInt(t -> t.estimatedRows));
            
            List<String> order = new ArrayList<>();
            for (TableNode node : sorted) {
                order.add(node.tableName);
            }
            return order;
        }
    }
    
    /**
     * Part 3: Join Order Optimization
     * Minimize index scans by filtering small tables first
     */
    public static class JoinOptimizer {
        Map<String, TableNode> tables;
        
        public JoinOptimizer(Map<String, TableNode> tables) {
            this.tables = tables;
        }
        
        public List<String> optimizeJoinSequence() {
            List<TableNode> ordered = new ArrayList<>(tables.values());
            
            // Sort by: indexed tables first, then by size
            ordered.sort((t1, t2) -> {
                // Smaller tables first (reduce join cardinality)
                return Integer.compare(t1.estimatedRows, t2.estimatedRows);
            });
            
            List<String> sequence = new ArrayList<>();
            for (TableNode node : ordered) {
                sequence.add(node.tableName);
            }
            return sequence;
        }
        
        public int estimateJoinCost(List<String> joinOrder) {
            int cost = 0;
            int cardinality = 1;
            
            for (String table : joinOrder) {
                TableNode node = tables.get(table);
                if (node != null) {
                    // Cost increases with cardinality
                    cost += node.estimatedRows * cardinality;
                    cardinality *= node.estimatedRows;
                }
            }
            
            return cost;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 63: SQL Query Parser Join Optimizer ===");
        
        // Test Part 1: Bracket validation
        String query1 = "SELECT * FROM (SELECT * FROM users WHERE id IN (1,2,3))";
        System.out.println("Query valid: " + validateQueryBrackets(query1));
        
        String query2 = "SELECT * FROM (SELECT * FROM users WHERE id IN (1,2,3)";
        System.out.println("Query valid (unmatched): " + validateQueryBrackets(query2));
        
        // Test Part 2: Query parsing
        QueryParser parser = new QueryParser();
        parser.addTable("users", 1000);
        parser.addTable("orders", 5000);
        parser.addTable("products", 500);
        
        List<String> order = parser.getOptimalJoinOrder();
        System.out.println("Optimal join order: " + order);
        
        // Test Part 3: Join optimization
        JoinOptimizer optimizer = new JoinOptimizer(parser.tables);
        int cost = optimizer.estimateJoinCost(order);
        System.out.println("Estimated join cost: " + cost);
    }
}
