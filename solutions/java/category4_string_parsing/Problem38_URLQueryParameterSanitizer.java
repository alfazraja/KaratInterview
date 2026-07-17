package category4_string_parsing;

import java.util.*;
import java.net.URLDecoder;

/**
 * Problem 38: URL Query Parameter Sanitizer
 * 
 * Three-part progression:
 * - Part 1: Handle embedded equals in values
 * - Part 2: Parse and decode URL parameters
 * - Part 3: Redact sensitive information
 */
public class Problem38_URLQueryParameterSanitizer {

    /**
     * Part 1: Bug Fix - Equals Sign Handling
     * Issue: Splits on all equals signs, truncating values
     * Solution: Split only on first equals sign
     */
    public static class Part1_BugFix {
        /**
         * Parse query parameter safely
         */
        public static Map.Entry<String, String> parseParameter(String param) {
            int eqIndex = param.indexOf('=');
            if (eqIndex == -1) {
                return new AbstractMap.SimpleEntry<>(param, "");
            }

            String key = param.substring(0, eqIndex);
            String value = param.substring(eqIndex + 1);
            return new AbstractMap.SimpleEntry<>(key, value);
        }
    }

    /**
     * Part 2: URL Decoding
     * Parse and decode query strings
     */
    public static class Part2_ParsingAndDecoding {
        /**
         * Parse URL query string into parameter map
         */
        public static Map<String, String> parseQueryString(String queryString) {
            Map<String, String> params = new HashMap<>();

            if (queryString == null || queryString.isEmpty()) {
                return params;
            }

            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                Map.Entry<String, String> param = Part1_BugFix.parseParameter(pair);
                String key = decodeURL(param.getKey());
                String value = decodeURL(param.getValue());
                params.put(key, value);
            }

            return params;
        }

        /**
         * Decode URL-encoded string
         */
        public static String decodeURL(String encoded) {
            try {
                return URLDecoder.decode(encoded, "UTF-8");
            } catch (Exception e) {
                return encoded;
            }
        }
    }

    /**
     * Part 3: PII Redaction
     * Obscure sensitive information
     */
    public static class Part3_PIIRedaction {
        private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList(
            "password", "email", "ssn", "creditcard", "token", "api_key"
        ));

        /**
         * Redact sensitive parameters
         */
        public static Map<String, String> redactSensitiveData(Map<String, String> params) {
            Map<String, String> redacted = new HashMap<>(params);

            for (String key : redacted.keySet()) {
                if (SENSITIVE_KEYS.contains(key.toLowerCase())) {
                    redacted.put(key, "***REDACTED***");
                }
            }

            return redacted;
        }

        /**
         * Parse, decode, and redact URL
         */
        public static Map<String, String> sanitizeURL(String url) {
            int qIndex = url.indexOf('?');
            if (qIndex == -1) {
                return new HashMap<>();
            }

            String queryString = url.substring(qIndex + 1);
            Map<String, String> params = Part2_ParsingAndDecoding.parseQueryString(queryString);
            return redactSensitiveData(params);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 38: URL Sanitizer ===\n");

        System.out.println("Part 1: Equals Handling");
        Map.Entry<String, String> param = Part1_BugFix.parseParameter("token=abc=def=");
        System.out.println("Key: " + param.getKey() + ", Value: " + param.getValue());
        System.out.println();

        System.out.println("Part 3: PII Redaction");
        String url = "https://example.com?email=user@test.com&password=secret123&id=5";
        Map<String, String> sanitized = Part3_PIIRedaction.sanitizeURL(url);
        System.out.println("Sanitized: " + sanitized);
    }
}
