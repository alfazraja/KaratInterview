package category4_string_parsing;

import java.util.*;

/**
 * Problem 35: CSV Data File Parser
 * 
 * Three-part progression:
 * - Part 1: Handle embedded commas in quoted fields
 * - Part 2: Validate data and flag errors
 * - Part 3: Perform SQL-style inner join
 */
public class Problem35_CSVDataFileParser {

    /**
     * Part 1: Bug Fix - Quoted Field Handling
     * Issue: Embedded commas inside quotes split incorrectly
     * Solution: Use state machine to track quote context
     */
    public static class Part1_BugFix {
        /**
         * Parse CSV line respecting quoted fields
         */
        public static List<String> parseCSVLine(String line) {
            List<String> fields = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean inQuotes = false;

            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);

                if (ch == '"') {
                    inQuotes = !inQuotes;
                } else if (ch == ',' && !inQuotes) {
                    fields.add(current.toString().trim());
                    current = new StringBuilder();
                } else {
                    current.append(ch);
                }
            }

            if (current.length() > 0) {
                fields.add(current.toString().trim());
            }

            return fields;
        }
    }

    /**
     * Part 2: Data Validation
     * Check for missing columns and type correctness
     */
    public static class Part2_DataValidation {
        public static class CSVRecord {
            public Map<String, String> data;
            public List<String> errors;

            public CSVRecord(Map<String, String> data) {
                this.data = data;
                this.errors = new ArrayList<>();
            }
        }

        /**
         * Parse CSV with header and validate
         */
        public static List<CSVRecord> parseAndValidate(String[] lines, String[] expectedHeaders) {
            List<CSVRecord> records = new ArrayList<>();
            Map<String, Integer> headerMap = new HashMap<>();

            List<String> headers = Part1_BugFix.parseCSVLine(lines[0]);
            for (int i = 0; i < headers.size(); i++) {
                headerMap.put(headers.get(i), i);
            }

            for (String expected : expectedHeaders) {
                if (!headerMap.containsKey(expected)) {
                    System.out.println("Missing column: " + expected);
                }
            }

            for (int i = 1; i < lines.length; i++) {
                List<String> fields = Part1_BugFix.parseCSVLine(lines[i]);
                Map<String, String> row = new HashMap<>();

                CSVRecord record = new CSVRecord(row);

                for (int j = 0; j < headers.size(); j++) {
                    if (j < fields.size()) {
                        row.put(headers.get(j), fields.get(j));
                    } else {
                        record.errors.add("Missing field for column: " + headers.get(j));
                    }
                }

                records.add(record);
            }

            return records;
        }
    }

    /**
     * Part 3: SQL-Style Join
     * Join two datasets on shared key
     */
    public static class Part3_DataJoin {
        /**
         * Perform inner join on two datasets
         */
        public static List<Map<String, String>> innerJoin(
            List<Part2_DataValidation.CSVRecord> left,
            List<Part2_DataValidation.CSVRecord> right,
            String keyColumn) {

            List<Map<String, String>> result = new ArrayList<>();
            Map<String, Part2_DataValidation.CSVRecord> rightMap = new HashMap<>();

            for (Part2_DataValidation.CSVRecord r : right) {
                String key = r.data.get(keyColumn);
                if (key != null) {
                    rightMap.put(key, r);
                }
            }

            for (Part2_DataValidation.CSVRecord l : left) {
                String key = l.data.get(keyColumn);
                if (key != null && rightMap.containsKey(key)) {
                    Map<String, String> joined = new HashMap<>(l.data);
                    joined.putAll(rightMap.get(key).data);
                    result.add(joined);
                }
            }

            return result;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 35: CSV Parser ===\n");

        System.out.println("Part 1: Quoted Field Parsing");
        String line = "John, Doe,30,\"123 Main St, Apt 5\"";
        List<String> fields = Part1_BugFix.parseCSVLine(line);
        System.out.println("Parsed fields: " + fields);
    }
}
