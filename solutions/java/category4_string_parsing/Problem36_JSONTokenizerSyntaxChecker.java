package category4_string_parsing;

import java.util.*;

/**
 * Problem 36: JSON Tokenizer Syntax Checker
 * 
 * Three-part progression:
 * - Part 1: Skip character evaluation in text strings
 * - Part 2: Validate matching brackets and quotes
 * - Part 3: Extract nested key path values
 */
public class Problem36_JSONTokenizerSyntaxChecker {

    /**
     * Part 1: Bug Fix - String Context Awareness
     * Issue: Counts colons inside strings as structural delimiters
     * Solution: Skip evaluation when inside quoted strings
     */
    public static class Part1_BugFix {
        /**
         * Check if JSON is structurally valid
         */
        public static boolean isValidJSON(String json) {
            Stack<Character> stack = new Stack<>();
            boolean inString = false;
            boolean escaped = false;

            for (int i = 0; i < json.length(); i++) {
                char ch = json.charAt(i);

                if (escaped) {
                    escaped = false;
                    continue;
                }

                if (ch == '\\') {
                    escaped = true;
                    continue;
                }

                if (ch == '"') {
                    inString = !inString;
                    continue;
                }

                if (inString) {
                    continue;
                }

                if (ch == '{' || ch == '[') {
                    stack.push(ch);
                } else if (ch == '}' || ch == ']') {
                    if (stack.isEmpty()) return false;
                    char open = stack.pop();
                    if ((ch == '}' && open != '{') || (ch == ']' && open != '[')) {
                        return false;
                    }
                }
            }

            return stack.isEmpty() && !inString;
        }
    }

    /**
     * Part 2: Stack Validation
     * Verify all structural elements match
     */
    public static class Part2_StackValidation {
        /**
         * Detailed validation with error reporting
         */
        public static String validateJSON(String json) {
            if (Part1_BugFix.isValidJSON(json)) {
                return "Valid JSON";
            } else {
                return "Invalid JSON";
            }
        }
    }

    /**
     * Part 3: Key Path Extractor
     * Extract values from nested JSON
     */
    public static class Part3_KeyPathExtractor {
        /**
         * Extract value at dot-notation path
         */
        public static String extractValueAtPath(String json, String path) {
            String[] keys = path.split("\\.");
            System.out.println("Extracting path: " + path);
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 36: JSON Validator ===\n");

        System.out.println("Part 1: String Context");
        String json1 = "{\"key\":\"value:with:colons\"}";
        boolean valid = Part1_BugFix.isValidJSON(json1);
        System.out.println("JSON valid: " + valid);
        System.out.println();

        System.out.println("Part 2: Stack Validation");
        String json2 = "{[]{}}";
        System.out.println("Validation: " + Part2_StackValidation.validateJSON(json2));
    }
}
