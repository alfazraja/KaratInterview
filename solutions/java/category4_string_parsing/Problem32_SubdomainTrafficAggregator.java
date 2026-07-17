package category4_string_parsing;

import java.util.*;

/**
 * Problem 32: Subdomain Traffic Aggregator
 * 
 * Three-part progression:
 * - Part 1: Fix log parsing (correct delimiter usage)
 * - Part 2: Aggregate traffic for domain hierarchy
 * - Part 3: Track traffic growth trends
 */
public class Problem32_SubdomainTrafficAggregator {

    /**
     * Part 1: Bug Fix - Delimiter Splitting
     * Issue: Uses space instead of comma for delimiter
     * Solution: Parse logs with correct delimiter
     */
    public static class Part1_BugFix {
        /**
         * Parse traffic log line: "50,mail.google.com"
         */
        public static Map.Entry<Integer, String> parseLogLine(String logLine) {
            if (logLine == null || logLine.isEmpty()) {
                return null;
            }

            String[] parts = logLine.split(",");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid format: " + logLine);
            }

            int traffic = Integer.parseInt(parts[0].trim());
            String domain = parts[1].trim();

            return new AbstractMap.SimpleEntry<>(traffic, domain);
        }

        /**
         * Parse multiple log lines
         */
        public static List<Map.Entry<Integer, String>> parseLogs(List<String> logs) {
            List<Map.Entry<Integer, String>> parsed = new ArrayList<>();
            for (String log : logs) {
                parsed.add(parseLogLine(log));
            }
            return parsed;
        }
    }

    /**
     * Part 2: Frequency Map - Domain Aggregation
     * Sum traffic for each domain hierarchy level
     */
    public static class Part2_DomainAggregation {
        /**
         * Get all subdomains from a domain
         */
        public static List<String> getSubdomains(String domain) {
            List<String> subdomains = new ArrayList<>();
            String[] parts = domain.split("\\.");

            for (int i = 0; i < parts.length; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = i; j < parts.length; j++) {
                    if (j > i) sb.append(".");
                    sb.append(parts[j]);
                }
                subdomains.add(sb.toString());
            }

            return subdomains;
        }

        /**
         * Aggregate traffic across domain hierarchy
         * Time Complexity: O(n * m) where n = logs, m = avg domain depth
         */
        public static Map<String, Integer> aggregateTraffic(List<Map.Entry<Integer, String>> logs) {
            Map<String, Integer> domainTraffic = new HashMap<>();

            for (Map.Entry<Integer, String> entry : logs) {
                int traffic = entry.getKey();
                String domain = entry.getValue();
                List<String> subdomains = getSubdomains(domain);

                for (String subdomain : subdomains) {
                    domainTraffic.put(subdomain,
                        domainTraffic.getOrDefault(subdomain, 0) + traffic);
                }
            }

            return domainTraffic;
        }
    }

    /**
     * Part 3: Traffic Trend Analysis
     * Identify growth patterns across time periods
     */
    public static class Part3_TrafficTrendDelta {
        /**
         * Calculate percentage growth between two maps
         */
        public static Map<String, Double> calculateGrowthRates(
            Map<String, Integer> thisYear,
            Map<String, Integer> lastYear) {

            Map<String, Double> growthRates = new HashMap<>();

            for (String domain : thisYear.keySet()) {
                int current = thisYear.getOrDefault(domain, 0);
                int previous = lastYear.getOrDefault(domain, 0);

                if (previous == 0) {
                    growthRates.put(domain, current > 0 ? 100.0 : 0.0);
                } else {
                    double growth = ((double) (current - previous) / previous) * 100;
                    growthRates.put(domain, growth);
                }
            }

            return growthRates;
        }

        /**
         * Find subdomain with highest growth
         */
        public static String findHighestGrowthSubdomain(
            Map<String, Integer> thisYear,
            Map<String, Integer> lastYear) {

            Map<String, Double> growthRates = calculateGrowthRates(thisYear, lastYear);
            String maxDomain = null;
            double maxGrowth = -Double.MAX_VALUE;

            for (Map.Entry<String, Double> entry : growthRates.entrySet()) {
                if (entry.getValue() > maxGrowth) {
                    maxGrowth = entry.getValue();
                    maxDomain = entry.getKey();
                }
            }

            return maxDomain;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 32: Subdomain Traffic Aggregator ===\n");

        System.out.println("Part 1: Log Parsing");
        List<String> logs = Arrays.asList(
            "50,mail.google.com",
            "100,google.com",
            "25,mail.google.com"
        );
        List<Map.Entry<Integer, String>> parsed = Part1_BugFix.parseLogs(logs);
        System.out.println("Parsed logs: " + parsed + "\n");

        System.out.println("Part 2: Domain Aggregation");
        Map<String, Integer> aggregated = Part2_DomainAggregation.aggregateTraffic(parsed);
        System.out.println("Aggregated traffic:\n");
        aggregated.forEach((domain, traffic) ->
            System.out.println("  " + domain + ": " + traffic)
        );
    }
}
