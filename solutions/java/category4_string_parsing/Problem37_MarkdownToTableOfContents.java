package category4_string_parsing;

import java.util.*;

/**
 * Problem 37: Markdown Header to TOC Generator
 * 
 * Three-part progression:
 * - Part 1: Distinguish actual headers from inline hashes
 * - Part 2: Extract and format header hierarchy
 * - Part 3: Generate URL-safe anchor links
 */
public class Problem37_MarkdownToTableOfContents {

    /**
     * Part 1: Bug Fix - Header Detection
     * Issue: Treats inline hashes as headers
     * Solution: Only lines starting with # are headers
     */
    public static class Part1_BugFix {
        /**
         * Determine if line is valid header
         */
        public static boolean isHeaderLine(String line) {
            if (line == null || line.isEmpty()) {
                return false;
            }
            return line.trim().startsWith("#") && line.charAt(0) == '#';
        }

        /**
         * Get header level (number of #'s)
         */
        public static int getHeaderLevel(String line) {
            int level = 0;
            for (char c : line.toCharArray()) {
                if (c == '#') level++;
                else break;
            }
            return level;
        }
    }

    /**
     * Part 2: String Processing
     * Extract headers and format hierarchy
     */
    public static class Part2_HeaderExtraction {
        public static class Header {
            public int level;
            public String title;
            public String anchor;

            public Header(int level, String title) {
                this.level = level;
                this.title = title;
                this.anchor = generateAnchor(title);
            }

            private static String generateAnchor(String title) {
                return "#" + title.toLowerCase().replaceAll("[^a-z0-9]", "-");
            }
        }

        /**
         * Extract headers from markdown
         */
        public static List<Header> extractHeaders(String markdown) {
            List<Header> headers = new ArrayList<>();
            String[] lines = markdown.split("\n");

            for (String line : lines) {
                if (Part1_BugFix.isHeaderLine(line)) {
                    int level = Part1_BugFix.getHeaderLevel(line);
                    String title = line.substring(level).trim();
                    headers.add(new Header(level, title));
                }
            }

            return headers;
        }

        /**
         * Format headers as table of contents
         */
        public static String formatTOC(List<Header> headers) {
            StringBuilder toc = new StringBuilder();

            for (Header h : headers) {
                for (int i = 1; i < h.level; i++) {
                    toc.append("  ");
                }
                toc.append("- [")
                   .append(h.title)
                   .append("](")
                   .append(h.anchor)
                   .append(")\n");
            }

            return toc.toString();
        }
    }

    /**
     * Part 3: Anchor Link Generation
     * Create URL-safe slugs
     */
    public static class Part3_AnchorLinkSluggifier {
        /**
         * Convert title to URL-safe anchor
         */
        public static String slugify(String title) {
            return "#" + title.toLowerCase()
                .replaceAll("[!?,.';\"]\", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Problem 37: Markdown TOC Generator ===\n");

        String markdown = "# Main Title\n## Section 1\n### Subsection\n## Section 2";

        System.out.println("Part 1: Header Detection");
        System.out.println("'# Title' is header: " + Part1_BugFix.isHeaderLine("# Title"));
        System.out.println("'See #1' is header: " + Part1_BugFix.isHeaderLine("See #1"));
        System.out.println();

        System.out.println("Part 2: TOC Generation");
        List<Part2_HeaderExtraction.Header> headers =
            Part2_HeaderExtraction.extractHeaders(markdown);
        String toc = Part2_HeaderExtraction.formatTOC(headers);
        System.out.println(toc);
    }
}
