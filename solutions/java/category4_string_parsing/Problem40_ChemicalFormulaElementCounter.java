package category4_string_parsing;

import java.util.*;

/**
 * Problem 40: Chemical Formula Element Counter
 * 
 * Three-part progression:
 * - Part 1: Handle implicit single atoms
 * - Part 2: Parse and count element frequencies
 * - Part 3: Expand nested parentheses with multipliers
 */
public class Problem40_ChemicalFormulaElementCounter {

    /**
     * Part 1: Bug Fix - Implicit Atoms
     * Issue: Doesn't count atoms without explicit multiplier
     * Solution: Default count to 1 when no number follows
     */
    public static class Part1_BugFix {
        /**
         * Verify implicit atom handling
         */
        public static Map<String, Integer> parseSimpleFormula(String formula) {
            Map<String, Integer> elements = new HashMap<>();
            int i = 0;

            while (i < formula.length()) {
                if (Character.isUpperCase(formula.charAt(i))) {
                    StringBuilder element = new StringBuilder();
                    element.append(formula.charAt(i));
                    i++;

                    while (i < formula.length() && Character.isLowerCase(formula.charAt(i))) {
                        element.append(formula.charAt(i));
                        i++;
                    }

                    int count = 1;
                    StringBuilder num = new StringBuilder();
                    while (i < formula.length() && Character.isDigit(formula.charAt(i))) {
                        num.append(formula.charAt(i));
                        i++;
                    }

                    if (num.length() > 0) {
                        count = Integer.parseInt(num.toString());
                    }

                    String elem = element.toString();
                    elements.put(elem, elements.getOrDefault(elem, 0) + count);
                } else {
                    i++;
                }
            }

            return elements;
        }
    }

    /**
     * Part 2: String Processing
     * Parse and count formulas
     */
    public static class Part2_FormulaParser {
        /**
         * Parse chemical formula (no nested parentheses)
         * Time Complexity: O(n) where n = formula length
         */
        public static Map<String, Integer> parseFormula(String formula) {
            return Part1_BugFix.parseSimpleFormula(formula);
        }
    }

    /**
     * Part 3: Nested Parentheses Expansion
     * Handle formulas like "Mg(OH)2" or "Ca(OH)2"
     */
    public static class Part3_NestedExpansion {
        /**
         * Parse formula with nested parentheses
         */
        public static Map<String, Integer> parseFormulaWithParentheses(String formula) {
            Map<String, Integer> result = new HashMap<>();
            Stack<Map<String, Integer>> stack = new Stack<>();
            Stack<Integer> multiplierStack = new Stack<>();

            int i = 0;
            stack.push(new HashMap<>());

            while (i < formula.length()) {
                if (formula.charAt(i) == '(') {
                    stack.push(new HashMap<>());
                    i++;
                } else if (formula.charAt(i) == ')') {
                    i++;
                    StringBuilder num = new StringBuilder();
                    while (i < formula.length() && Character.isDigit(formula.charAt(i))) {
                        num.append(formula.charAt(i));
                        i++;
                    }

                    int multiplier = num.length() > 0 ? Integer.parseInt(num.toString()) : 1;
                    Map<String, Integer> top = stack.pop();

                    for (Map.Entry<String, Integer> entry : top.entrySet()) {
                        String elem = entry.getKey();
                        int count = entry.getValue() * multiplier;
                        stack.peek().put(elem, stack.peek().getOrDefault(elem, 0) + count);
                    }
                } else if (Character.isUpperCase(formula.charAt(i))) {
                    StringBuilder element = new StringBuilder();
                    element.append(formula.charAt(i));
                    i++;

                    while (i < formula.length() && Character.isLowerCase(formula.charAt(i))) {
                        element.append(formula.charAt(i));
                        i++;
                    }

                    int count = 1;
                    StringBuilder num = new StringBuilder();
                    while (i < formula.length() && Character.isDigit(formula.charAt(i))) {
                        num.append(formula.charAt(i));
                        i++;
                    }

                    if (num.length() > 0) {
                        count = Integer.parseInt(num.toString());
                    }

                    String elem = element.toString();
                    stack.peek().put(elem, stack.peek().getOrDefault(elem, 0) + count);
                } else {
                    i++;
                }
            }

            return stack.pop();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 40: Chemical Formula Counter ===\n");

        System.out.println("Part 1: Implicit Atoms");
        Map<String, Integer> parsed = Part1_BugFix.parseSimpleFormula("H2O");
        System.out.println("H2O: " + parsed);
        System.out.println();

        System.out.println("Part 3: Nested Parentheses");
        parsed = Part3_NestedExpansion.parseFormulaWithParentheses("Mg(OH)2");
        System.out.println("Mg(OH)2: " + parsed);
    }
}
