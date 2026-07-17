package category4_string_parsing;

import java.util.*;

/**
 * Problem 34: Text Justification & Word Wrap Engine
 * 
 * Three-part progression:
 * - Part 1: Fix loop boundary (include all words)
 * - Part 2: Format text with line width constraint
 * - Part 3: Balance spaces evenly with left-bias
 */
public class Problem34_TextJustificationWordWrap {

    /**
     * Part 1: Bug Fix - Loop Boundary
     * Issue: Exclusive boundary excludes last word
     * Solution: Use inclusive termination
     */
    public static class Part1_BugFix {
        /**
         * Verify all words are processed
         */
        public static void testBoundary(String[] words) {
            for (int i = 0; i < words.length; i++) {
                System.out.println("Word " + i + ": " + words[i]);
            }
        }
    }

    /**
     * Part 2: Text Wrapping
     * Format text into fixed-width lines
     */
    public static class Part2_TextFormatting {
        /**
         * Wrap text with max line width
         * Time Complexity: O(n) where n = total characters
         */
        public static List<String> formatText(String[] words, int maxWidth) {
            List<String> result = new ArrayList<>();
            List<String> currentLine = new ArrayList<>();
            int currentLength = 0;

            for (String word : words) {
                int neededLength = currentLength + word.length() + currentLine.size();

                if (neededLength <= maxWidth) {
                    currentLine.add(word);
                    currentLength += word.length();
                } else {
                    if (!currentLine.isEmpty()) {
                        result.add(joinLine(currentLine, maxWidth, false));
                    }
                    currentLine = new ArrayList<>();
                    currentLine.add(word);
                    currentLength = word.length();
                }
            }

            if (!currentLine.isEmpty()) {
                result.add(joinLine(currentLine, maxWidth, true));
            }

            return result;
        }

        private static String joinLine(List<String> words, int maxWidth, boolean isLastLine) {
            if (isLastLine) {
                String line = String.join(" ", words);
                while (line.length() < maxWidth) {
                    line += " ";
                }
                return line;
            }

            int gaps = words.size() - 1;
            int wordLength = 0;
            for (String w : words) {
                wordLength += w.length();
            }

            int totalSpaces = maxWidth - wordLength;
            int spacesPerGap = gaps == 0 ? 0 : totalSpaces / gaps;
            int extraSpaces = gaps == 0 ? 0 : totalSpaces % gaps;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < words.size(); i++) {
                sb.append(words.get(i));
                if (i < gaps) {
                    for (int j = 0; j < spacesPerGap; j++) {
                        sb.append(" ");
                    }
                    if (i < extraSpaces) {
                        sb.append(" ");
                    }
                }
            }

            return sb.toString();
        }
    }

    /**
     * Part 3: Balanced Spacing
     * Distribute extra spaces evenly, left-bias
     */
    public static class Part3_BalancedSpacing {
        /**
         * Format with balanced space distribution
         */
        public static List<String> formatBalanced(String[] words, int maxWidth) {
            return Part2_TextFormatting.formatText(words, maxWidth);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 34: Text Justification ===\n");

        String[] words = {"This", "is", "an", "example", "of", "text", "justification"};
        int maxWidth = 16;

        System.out.println("Part 1: Boundary Fix");
        Part1_BugFix.testBoundary(words);
        System.out.println();

        System.out.println("Part 2: Text Formatting");
        List<String> formatted = Part2_TextFormatting.formatText(words, maxWidth);
        for (String line : formatted) {
            System.out.println("|" + line + "|");
        }
    }
}
