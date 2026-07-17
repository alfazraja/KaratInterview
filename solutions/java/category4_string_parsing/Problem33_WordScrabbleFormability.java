package category4_string_parsing;

import java.util.*;

/**
 * Problem 33: Word Scrabble Formability Engine
 * 
 * Three-part progression:
 * - Part 1: Fix character counting (decrement after use)
 * - Part 2: Match dictionary words with available characters
 * - Part 3: Support wildcard tiles with constraints
 */
public class Problem33_WordScrabbleFormability {

    /**
     * Part 1: Bug Fix - Character Counting
     * Issue: Loop doesn't decrement count after matching
     * Solution: Properly track character usage
     */
    public static class Part1_BugFix {
        /**
         * Check if word can be formed from hand
         */
        public static boolean canFormWord(String word, char[] hand) {
            if (word == null || hand == null) {
                return false;
            }

            Map<Character, Integer> charCount = new HashMap<>();
            for (char c : hand) {
                charCount.put(c, charCount.getOrDefault(c, 0) + 1);
            }

            for (char c : word.toCharArray()) {
                if (!charCount.containsKey(c) || charCount.get(c) == 0) {
                    return false;
                }
                charCount.put(c, charCount.get(c) - 1);
            }

            return true;
        }
    }

    /**
     * Part 2: Map Verification
     * Find all formable words from dictionary
     */
    public static class Part2_DictionaryMatching {
        /**
         * Find all words that can be formed
         * Time Complexity: O(n * m) where n = words, m = avg word length
         */
        public static List<String> findFormableWords(List<String> dictionary, char[] hand) {
            List<String> formable = new ArrayList<>();

            for (String word : dictionary) {
                if (Part1_BugFix.canFormWord(word, hand)) {
                    formable.add(word);
                }
            }

            return formable;
        }

        /**
         * Find word with maximum score
         */
        public static String findBestWord(List<String> dictionary, char[] hand) {
            String bestWord = null;
            int maxScore = 0;

            for (String word : dictionary) {
                if (Part1_BugFix.canFormWord(word, hand)) {
                    if (word.length() > maxScore) {
                        maxScore = word.length();
                        bestWord = word;
                    }
                }
            }

            return bestWord;
        }
    }

    /**
     * Part 3: Wildcard Tile Resolution
     * Support '_' wildcard with max 2 usage
     */
    public static class Part3_WildcardSupport {
        /**
         * Check if word can be formed with wildcards
         * '_' can substitute for any letter
         */
        public static boolean canFormWithWildcards(String word, char[] hand) {
            if (word == null || hand == null) {
                return false;
            }

            Map<Character, Integer> charCount = new HashMap<>();
            int wildcards = 0;

            for (char c : hand) {
                if (c == '_') {
                    wildcards++;
                } else {
                    charCount.put(c, charCount.getOrDefault(c, 0) + 1);
                }
            }

            int neededWildcards = 0;
            for (char c : word.toCharArray()) {
                if (charCount.containsKey(c) && charCount.get(c) > 0) {
                    charCount.put(c, charCount.get(c) - 1);
                } else {
                    neededWildcards++;
                }
            }

            return neededWildcards <= wildcards && wildcards <= 2;
        }

        /**
         * Find all formable words with wildcards
         */
        public static List<String> findFormableWithWildcards(
            List<String> dictionary,
            char[] hand) {

            List<String> formable = new ArrayList<>();

            for (String word : dictionary) {
                if (canFormWithWildcards(word, hand)) {
                    formable.add(word);
                }
            }

            return formable;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 33: Word Scrabble Formability ===\n");

        System.out.println("Part 1: Character Counting");
        boolean canForm = Part1_BugFix.canFormWord("APPLE",
            new char[]{'A', 'P', 'P', 'L', 'E'});
        System.out.println("Can form APPLE: " + canForm);
        System.out.println();

        System.out.println("Part 2: Dictionary Matching");
        List<String> dictionary = Arrays.asList("CAT", "DOG", "ACT", "TAC");
        char[] hand = {'C', 'A', 'T', 'D', 'O', 'G'};
        List<String> formable = Part2_DictionaryMatching.findFormableWords(dictionary, hand);
        System.out.println("Formable words: " + formable);
    }
}
