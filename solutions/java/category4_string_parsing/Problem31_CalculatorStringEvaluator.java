package category4_string_parsing;

import java.util.*;

/**
 * Problem 31: The Calculator String Evaluator
 * 
 * Three-part progression:
 * - Part 1: Fix multi-digit number parsing
 * - Part 2: Evaluate mathematical expressions with +, - operators
 * - Part 3: Resolve nested parenthetical expressions
 */
public class Problem31_CalculatorStringEvaluator {

    /**
     * Part 1: Bug Fix - Multi-digit Number Parsing
     * Issue: Character-by-character reading splits multi-digit numbers
     * Solution: Accumulate consecutive digits into single number
     */
    public static class Part1_BugFix {
        /**
         * Parse multi-digit numbers from string
         */
        public static List<Integer> parseNumbers(String expression) {
            List<Integer> numbers = new ArrayList<>();
            int i = 0;
            
            while (i < expression.length()) {
                if (Character.isDigit(expression.charAt(i))) {
                    int num = 0;
                    while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                        num = num * 10 + (expression.charAt(i) - '0');
                        i++;
                    }
                    numbers.add(num);
                } else {
                    i++;
                }
            }
            
            return numbers;
        }

        /**
         * Verify correct parsing
         */
        public static void testParsing() {
            List<Integer> nums = parseNumbers("14 + 25 - 3");
            System.out.println("Parsed numbers: " + nums);
        }
    }

    /**
     * Part 2: State Stack - Basic Expression Evaluation
     * Evaluate expressions with +, - operators
     */
    public static class Part2_BasicEvaluation {
        /**
         * Evaluate simple expression: "5 + 3 - 2" = 6
         * Time Complexity: O(n) where n is length of expression
         */
        public static int evaluate(String expression) {
            if (expression == null || expression.isEmpty()) {
                return 0;
            }

            Stack<Integer> stack = new Stack<>();
            int currentNum = 0;
            char operator = '+';
            expression = expression.replaceAll(" ", "");

            for (int i = 0; i < expression.length(); i++) {
                char ch = expression.charAt(i);

                if (Character.isDigit(ch)) {
                    currentNum = currentNum * 10 + (ch - '0');
                }

                if (ch == '+' || ch == '-' || i == expression.length() - 1) {
                    if (operator == '+') {
                        stack.push(currentNum);
                    } else if (operator == '-') {
                        stack.push(-currentNum);
                    }

                    if (ch == '+' || ch == '-') {
                        operator = ch;
                        currentNum = 0;
                    }
                }
            }

            int result = 0;
            while (!stack.isEmpty()) {
                result += stack.pop();
            }

            return result;
        }
    }

    /**
     * Part 3: Parentheses Resolution
     * Handle nested parenthetical expressions
     */
    public static class Part3_ParenthesesResolution {
        /**
         * Evaluate expression with nested parentheses
         * Time Complexity: O(n)
         */
        public static int evaluateWithParentheses(String expression) {
            expression = expression.replaceAll(" ", "");
            Stack<Integer> numStack = new Stack<>();
            Stack<Character> opStack = new Stack<>();
            int currentNum = 0;

            for (int i = 0; i < expression.length(); i++) {
                char ch = expression.charAt(i);

                if (Character.isDigit(ch)) {
                    currentNum = currentNum * 10 + (ch - '0');
                } else if (ch == '(') {
                    numStack.push(currentNum);
                    opStack.push(ch);
                    currentNum = 0;
                } else if (ch == ')') {
                    if (!opStack.isEmpty() && opStack.peek() == '(') {
                        opStack.pop();
                        int val = currentNum;
                        currentNum = numStack.pop();

                        if (!opStack.isEmpty()) {
                            char op = opStack.pop();
                            if (op == '+') {
                                currentNum = currentNum + val;
                            } else if (op == '-') {
                                currentNum = currentNum - val;
                            }
                        }
                    }
                } else if (ch == '+' || ch == '-') {
                    if (!opStack.isEmpty() && opStack.peek() != '(') {
                        char prevOp = opStack.pop();
                        int prevNum = numStack.pop();
                        if (prevOp == '+') {
                            currentNum = prevNum + currentNum;
                        } else {
                            currentNum = prevNum - currentNum;
                        }
                    }
                    numStack.push(currentNum);
                    opStack.push(ch);
                    currentNum = 0;
                }
            }

            while (!opStack.isEmpty() && opStack.peek() != '(') {
                char op = opStack.pop();
                int prev = numStack.pop();
                if (op == '+') {
                    currentNum = prev + currentNum;
                } else {
                    currentNum = prev - currentNum;
                }
            }

            return currentNum;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 31: Calculator String Evaluator ===\n");

        System.out.println("Part 1: Multi-digit Parsing");
        Part1_BugFix.testParsing();
        System.out.println();

        System.out.println("Part 2: Basic Evaluation");
        int result = Part2_BasicEvaluation.evaluate("5 + 3 - 2");
        System.out.println("5 + 3 - 2 = " + result);
        System.out.println();

        System.out.println("Part 3: Parentheses Resolution");
        result = Part3_ParenthesesResolution.evaluateWithParentheses("5 + (10 - (3 + 2))");
        System.out.println("5 + (10 - (3 + 2)) = " + result);
    }
}
