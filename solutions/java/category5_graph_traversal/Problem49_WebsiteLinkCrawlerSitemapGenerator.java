package category5_graph_traversal;

import java.util.*;

/**
 * Problem 49: Website Link Crawler Sitemap Generator
 * 
 * Three-part progression:
 * - Part 1: Normalize URLs (http vs https)
 * - Part 2: DFS to crawl domain and collect links
 * - Part 3: Identify orphaned/unreachable pages
 */
public class Problem49_WebsiteLinkCrawlerSitemapGenerator {
    
    /**
     * Part 1: Normalize URLs
     * Treat http and https variants as same domain
     */
    public static String normalizeUrl(String url) {
        return url.replaceFirst("https?://", "").replaceAll("/$", "");
    }
    
    /**
     * Part 2: DFS Web Crawl
     * Collect all pages reachable from homepage
     */
    public static Set<String> crawlDomain(String homepage, 
                                          Map<String, List<String>> mockLinks) {
        Set<String> visited = new HashSet<>();
        String normalized = normalizeUrl(homepage);
        
        dfsCrawl(normalized, mockLinks, visited);
        return visited;
    }
    
    private static void dfsCrawl(String url, Map<String, List<String>> mockLinks,
                                 Set<String> visited) {
        if (visited.contains(url)) return;
        visited.add(url);
        
        for (String link : mockLinks.getOrDefault(url, new ArrayList<>())) {
            String normalized = normalizeUrl(link);
            dfsCrawl(normalized, mockLinks, visited);
        }
    }
    
    /**
     * Part 3: Find Orphaned Pages
     * Pages with zero internal links pointing to them
     */
    public static Set<String> findOrphanedPages(Set<String> allPages,
                                                Map<String, List<String>> mockLinks) {
        Set<String> reachable = new HashSet<>();
        
        for (List<String> links : mockLinks.values()) {
            for (String link : links) {
                reachable.add(normalizeUrl(link));
            }
        }
        
        Set<String> orphaned = new HashSet<>(allPages);
        orphaned.removeAll(reachable);
        
        return orphaned;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 49: Website Link Crawler ===");
        
        Map<String, List<String>> mockLinks = new HashMap<>();
        mockLinks.put("site.com", Arrays.asList(
            "https://site.com/about",
            "https://site.com/contact"
        ));
        mockLinks.put("site.com/about", Arrays.asList(
            "https://site.com",
            "https://site.com/team"
        ));
        mockLinks.put("site.com/contact", Arrays.asList(
            "https://site.com"
        ));
        mockLinks.put("site.com/team", new ArrayList<>());
        
        Set<String> crawled = crawlDomain("http://site.com", mockLinks);
        System.out.println("Crawled pages: " + crawled);
        
        Set<String> allPages = new HashSet<>(mockLinks.keySet());
        Set<String> orphaned = findOrphanedPages(allPages, mockLinks);
        System.out.println("Orphaned pages: " + orphaned);
    }
}
