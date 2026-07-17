package category4_string_parsing;

import java.util.*;
import java.util.regex.*;

/**
 * Problem 39: Log File IP Address Filter
 * 
 * Three-part progression:
 * - Part 1: Validate IP address octets (0-255 range)
 * - Part 2: Extract and count unique IP addresses
 * - Part 3: Filter by CIDR subnet mask
 */
public class Problem39_LogFileIPAddressFilter {

    /**
     * Part 1: Bug Fix - IP Octet Validation
     * Issue: Regex accepts values > 255 (e.g., 256.100.0.1)
     * Solution: Validate each octet is 0-255
     */
    public static class Part1_BugFix {
        /**
         * Validate IP address format and value ranges
         */
        public static boolean isValidIPAddress(String ip) {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String part : parts) {
                try {
                    int octet = Integer.parseInt(part);
                    if (octet < 0 || octet > 255) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Part 2: String Processing
     * Extract IPs and count frequencies
     */
    public static class Part2_IPExtraction {
        /**
         * Extract IP addresses from log
         */
        public static Set<String> extractUniqueIPs(String logContent) {
            Set<String> ips = new HashSet<>();
            Pattern pattern = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
            Matcher matcher = pattern.matcher(logContent);

            while (matcher.find()) {
                String ip = matcher.group();
                if (Part1_BugFix.isValidIPAddress(ip)) {
                    ips.add(ip);
                }
            }

            return ips;
        }

        /**
         * Count IP occurrences
         */
        public static Map<String, Integer> countIPFrequencies(String logContent) {
            Map<String, Integer> frequencies = new HashMap<>();
            Pattern pattern = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
            Matcher matcher = pattern.matcher(logContent);

            while (matcher.find()) {
                String ip = matcher.group();
                if (Part1_BugFix.isValidIPAddress(ip)) {
                    frequencies.put(ip, frequencies.getOrDefault(ip, 0) + 1);
                }
            }

            return frequencies;
        }
    }

    /**
     * Part 3: Subnet Matcher
     * Filter IPs by CIDR range
     */
    public static class Part3_SubnetMatcher {
        /**
         * Check if IP is in CIDR range
         */
        public static boolean isIPInCIDR(String ip, String cidr) {
            String[] cidrParts = cidr.split("/");
            if (cidrParts.length != 2) {
                return false;
            }

            String network = cidrParts[0];
            int prefixLength = Integer.parseInt(cidrParts[1]);

            long ipNum = ipToLong(ip);
            long networkNum = ipToLong(network);
            long mask = (0xFFFFFFFFL << (32 - prefixLength));

            return (ipNum & mask) == (networkNum & mask);
        }

        private static long ipToLong(String ip) {
            String[] parts = ip.split("\\.");
            long result = 0;
            for (int i = 0; i < 4; i++) {
                result = result * 256 + Integer.parseInt(parts[i]);
            }
            return result;
        }

        /**
         * Filter IPs by subnet
         */
        public static Set<String> filterBySubnet(Set<String> ips, String cidr) {
            Set<String> filtered = new HashSet<>();
            for (String ip : ips) {
                if (isIPInCIDR(ip, cidr)) {
                    filtered.add(ip);
                }
            }
            return filtered;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 39: IP Address Filter ===\n");

        System.out.println("Part 1: Validation");
        System.out.println("192.168.1.1 valid: " + Part1_BugFix.isValidIPAddress("192.168.1.1"));
        System.out.println("256.100.0.1 valid: " + Part1_BugFix.isValidIPAddress("256.100.0.1"));
        System.out.println();

        System.out.println("Part 3: Subnet Filter");
        String ip = "192.168.1.10";
        boolean inSubnet = Part3_SubnetMatcher.isIPInCIDR(ip, "192.168.1.0/24");
        System.out.println(ip + " in 192.168.1.0/24: " + inSubnet);
    }
}
