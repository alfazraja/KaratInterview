package category7_advanced_systems;

import java.util.*;

/**
 * Problem 66: Email Spam Classifier Word Frequency Counter
 * 
 * Three-part progression:
 * - Part 1: Clean and normalize email text
 * - Part 2: Build word frequency histogram
 * - Part 3: Classify as spam using feature extraction
 */
public class Problem66_EmailSpamClassifierWordFrequencyCounter {
    
    /**
     * Part 1: Bug Fix - Normalize Text for Analysis
     * Remove punctuation, convert to lowercase, split correctly
     */
    public static List<String> tokenizeEmail(String emailBody) {
        if (emailBody == null || emailBody.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Convert to lowercase
        String normalized = emailBody.toLowerCase();
        
        // Remove special characters but keep spaces
        normalized = normalized.replaceAll("[^a-z0-9\\s]", " ");
        
        // Split on whitespace and filter empty tokens
        List<String> tokens = new ArrayList<>();
        for (String token : normalized.split("\\s+")) {
            if (!token.isEmpty() && token.length() > 2) {  // Skip very short words
                tokens.add(token);
            }
        }
        
        return tokens;
    }
    
    /**
     * Part 2: Build Word Frequency Histogram
     */
    public static class SpamAnalyzer {
        Map<String, Integer> wordFrequency = new HashMap<>();
        List<String> spamIndicators = Arrays.asList(
            "click", "free", "offer", "limited", "urgent", "act", "now",
            "winner", "prize", "guaranteed", "viagra", "pharmacy"
        );
        
        public void analyzeEmail(String emailBody) {
            List<String> tokens = tokenizeEmail(emailBody);
            
            for (String token : tokens) {
                wordFrequency.put(token, wordFrequency.getOrDefault(token, 0) + 1);
            }
        }
        
        public Map<String, Integer> getFrequencyHistogram() {
            return new HashMap<>(wordFrequency);
        }
        
        public List<String> getTopWords(int count) {
            return wordFrequency.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(count)
                .map(Map.Entry::getKey)
                .toList();
        }
    }
    
    /**
     * Part 3: Spam Classification
     * Calculate spam probability based on word features
     */
    public static class SpamClassifier {
        Map<String, Double> spamWordWeights = new HashMap<>();
        double spamThreshold = 0.5;
        
        public SpamClassifier() {
            // Initialize spam indicator weights
            spamWordWeights.put("click", 0.8);
            spamWordWeights.put("free", 0.7);
            spamWordWeights.put("offer", 0.6);
            spamWordWeights.put("limited", 0.5);
            spamWordWeights.put("urgent", 0.7);
            spamWordWeights.put("winner", 0.9);
            spamWordWeights.put("prize", 0.85);
            spamWordWeights.put("guaranteed", 0.6);
        }
        
        public double calculateSpamScore(String emailBody) {
            List<String> tokens = tokenizeEmail(emailBody);
            if (tokens.isEmpty()) return 0.0;
            
            double spamScore = 0.0;
            int matchCount = 0;
            
            for (String token : tokens) {
                if (spamWordWeights.containsKey(token)) {
                    spamScore += spamWordWeights.get(token);
                    matchCount++;
                }
            }
            
            // Normalize score
            if (matchCount > 0) {
                spamScore /= tokens.size();
            }
            
            return Math.min(1.0, spamScore);
        }
        
        public boolean isSpam(String emailBody) {
            return calculateSpamScore(emailBody) >= spamThreshold;
        }
    }
    
    /**
     * Calculate TF-IDF for feature importance
     */
    public static double calculateTFIDF(String word, List<String> document, 
                                       List<List<String>> corpus) {
        // Term Frequency
        long wordCount = document.stream().filter(w -> w.equals(word)).count();
        double tf = (double) wordCount / document.size();
        
        // Inverse Document Frequency
        long docCount = corpus.stream().filter(doc -> doc.contains(word)).count();
        double idf = Math.log((double) corpus.size() / (docCount + 1));
        
        return tf * idf;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 66: Email Spam Classifier ===");
        
        // Test Part 1: Tokenization
        String email1 = "Click here for FREE offer!!! Limited time only!!!";
        List<String> tokens = tokenizeEmail(email1);
        System.out.println("Tokens: " + tokens);
        
        // Test Part 2: Frequency histogram
        SpamAnalyzer analyzer = new SpamAnalyzer();
        analyzer.analyzeEmail(email1);
        analyzer.analyzeEmail("You won! Prize winner! Guaranteed cash!");
        
        Map<String, Integer> histogram = analyzer.getFrequencyHistogram();
        System.out.println("Word frequency: " + histogram);
        
        List<String> topWords = analyzer.getTopWords(3);
        System.out.println("Top words: " + topWords);
        
        // Test Part 3: Classification
        SpamClassifier classifier = new SpamClassifier();
        double score1 = classifier.calculateSpamScore(email1);
        System.out.println("Email 1 spam score: " + String.format("%.2f", score1));
        System.out.println("Is spam: " + classifier.isSpam(email1));
        
        String email2 = "Please review the quarterly report attached.";
        double score2 = classifier.calculateSpamScore(email2);
        System.out.println("Email 2 spam score: " + String.format("%.2f", score2));
        System.out.println("Is spam: " + classifier.isSpam(email2));
    }
}
