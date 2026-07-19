package category7_advanced_systems;

import java.util.*;

/**
 * Problem 68: Content Delivery Network (CDN) Cache Hit Rate Monitor
 * 
 * Three-part progression:
 * - Part 1: Fix counter overflow in request tracking
 * - Part 2: Track cache hit/miss statistics
 * - Part 3: Implement cache eviction strategies
 */
public class Problem68_ContentDeliveryNetworkCacheHitRateMonitor {
    
    enum CacheStrategy {
        LRU, LFU, FIFO
    }
    
    static class CacheNode {
        String contentId;
        byte[] data;
        long lastAccessTime;
        int accessCount;
        long size;
        
        CacheNode(String id, byte[] data) {
            this.contentId = id;
            this.data = data;
            this.lastAccessTime = System.currentTimeMillis();
            this.accessCount = 0;
            this.size = data.length;
        }
    }
    
    /**
     * Part 1: Bug Fix - Use Long for Request Counters
     * Prevent integer overflow with large request volumes
     */
    public static class CacheStatistics {
        private long totalRequests = 0L;      // Use long, not int
        private long cacheHits = 0L;
        private long cacheMisses = 0L;
        
        public synchronized void recordHit() {
            cacheHits++;
            totalRequests++;
        }
        
        public synchronized void recordMiss() {
            cacheMisses++;
            totalRequests++;
        }
        
        public double calculateHitRate() {
            if (totalRequests == 0) return 0.0;
            return (double) cacheHits / (double) totalRequests * 100.0;
        }
        
        public double calculateMissRate() {
            return 100.0 - calculateHitRate();
        }
        
        public long getTotalRequests() {
            return totalRequests;
        }
    }
    
    /**
     * Part 2: Track Cache Performance Metrics
     */
    public static class CDNCache {
        Map<String, CacheNode> cache = new LinkedHashMap<>();
        long maxCapacity;
        long currentUsage = 0L;
        CacheStatistics stats = new CacheStatistics();
        
        public CDNCache(long maxCapacityBytes) {
            this.maxCapacity = maxCapacityBytes;
        }
        
        public boolean get(String contentId) {
            CacheNode node = cache.get(contentId);
            
            if (node != null) {
                node.lastAccessTime = System.currentTimeMillis();
                node.accessCount++;
                stats.recordHit();
                return true;
            }
            
            stats.recordMiss();
            return false;
        }
        
        public void put(String contentId, byte[] data) {
            // Check if already cached
            if (cache.containsKey(contentId)) {
                cache.get(contentId).accessCount++;
                return;
            }
            
            // Evict if necessary
            long dataSize = data.length;
            while (currentUsage + dataSize > maxCapacity && !cache.isEmpty()) {
                evictLRU();
            }
            
            // Add new content
            if (currentUsage + dataSize <= maxCapacity) {
                CacheNode node = new CacheNode(contentId, data);
                cache.put(contentId, node);
                currentUsage += dataSize;
            }
        }
        
        private void evictLRU() {
            String lruKey = null;
            long oldestTime = Long.MAX_VALUE;
            
            for (Map.Entry<String, CacheNode> entry : cache.entrySet()) {
                if (entry.getValue().lastAccessTime < oldestTime) {
                    oldestTime = entry.getValue().lastAccessTime;
                    lruKey = entry.getKey();
                }
            }
            
            if (lruKey != null) {
                CacheNode removed = cache.remove(lruKey);
                currentUsage -= removed.size;
            }
        }
        
        public double getHitRate() {
            return stats.calculateHitRate();
        }
    }
    
    /**
     * Part 3: Compare Cache Strategies
     */
    public static class CacheStrategyAnalyzer {
        
        public static int simulateLRU(List<String> requests, int cacheSize) {
            Set<String> cache = new LinkedHashSet<>();
            int hits = 0;
            
            for (String request : requests) {
                if (cache.contains(request)) {
                    hits++;
                    cache.remove(request);
                    cache.add(request);
                } else {
                    if (cache.size() >= cacheSize) {
                        Iterator<String> it = cache.iterator();
                        it.next();
                        it.remove();
                    }
                    cache.add(request);
                }
            }
            
            return hits;
        }
        
        public static int simulateLFU(List<String> requests, int cacheSize) {
            Map<String, Integer> frequency = new HashMap<>();
            Set<String> cache = new HashSet<>();
            int hits = 0;
            
            for (String request : requests) {
                if (cache.contains(request)) {
                    hits++;
                    frequency.put(request, frequency.get(request) + 1);
                } else {
                    if (cache.size() >= cacheSize) {
                        // Evict least frequently used
                        String lfu = frequency.entrySet().stream()
                            .min(Comparator.comparingInt(Map.Entry::getValue))
                            .map(Map.Entry::getKey)
                            .orElse(null);
                        
                        if (lfu != null) {
                            cache.remove(lfu);
                            frequency.remove(lfu);
                        }
                    }
                    
                    cache.add(request);
                    frequency.put(request, 1);
                }
            }
            
            return hits;
        }
        
        public static void compareStrategies(List<String> requests, int cacheSize) {
            int lruHits = simulateLRU(requests, cacheSize);
            int lfuHits = simulateLFU(requests, cacheSize);
            
            double lruRate = (double) lruHits / requests.size() * 100.0;
            double lfuRate = (double) lfuHits / requests.size() * 100.0;
            
            System.out.println("LRU hit rate: " + String.format("%.2f%%", lruRate));
            System.out.println("LFU hit rate: " + String.format("%.2f%%", lfuRate));
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 68: CDN Cache Hit Rate Monitor ===");
        
        // Test Part 1: Statistics tracking
        CacheStatistics stats = new CacheStatistics();
        for (int i = 0; i < 100; i++) {
            if (i % 3 == 0) stats.recordHit();
            else stats.recordMiss();
        }
        System.out.println("Hit rate: " + String.format("%.2f%%", stats.calculateHitRate()));
        System.out.println("Total requests: " + stats.getTotalRequests());
        
        // Test Part 2: CDN cache
        CDNCache cache = new CDNCache(10000);  // 10KB cache
        cache.put("content1", new byte[1000]);
        cache.put("content2", new byte[2000]);
        cache.put("content3", new byte[3000]);
        
        boolean hit1 = cache.get("content1");
        boolean miss1 = cache.get("content999");
        
        System.out.println("\nCache hit (content1): " + hit1);
        System.out.println("Cache miss (content999): " + miss1);
        System.out.println("Overall hit rate: " + String.format("%.2f%%", cache.getHitRate()));
        
        // Test Part 3: Strategy comparison
        List<String> requests = Arrays.asList(
            "content1", "content2", "content3", "content1", "content2",
            "content4", "content1", "content3", "content2", "content1"
        );
        
        System.out.println("\nCache strategy comparison (size=3):");
        CacheStrategyAnalyzer.compareStrategies(requests, 3);
    }
}
