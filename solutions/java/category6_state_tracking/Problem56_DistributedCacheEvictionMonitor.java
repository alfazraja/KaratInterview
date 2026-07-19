package category6_state_tracking;

import java.util.*;

/**
 * Problem 56: Distributed Cache Eviction Monitor
 * 
 * Three-part progression:
 * - Part 1: Use wide data types for large memory sizes
 * - Part 2: Track cache key states (ALLOCATED, READ_HIT, EVICTED)
 * - Part 3: Simulate LRU (Least Recently Used) eviction
 */
public class Problem56_DistributedCacheEvictionMonitor {
    
    enum CacheState {
        ALLOCATED, READ_HIT, EVICTED
    }
    
    static class CacheKey {
        String key;
        long sizeBytes;  // Use long for large sizes
        CacheState state;
        long lastAccessTime;
        int hitCount;
        
        CacheKey(String key, long sizeBytes) {
            this.key = key;
            this.sizeBytes = sizeBytes;
            this.state = CacheState.ALLOCATED;
            this.lastAccessTime = System.currentTimeMillis();
            this.hitCount = 0;
        }
    }
    
    /**
     * Part 1: Bug Fix - Use Long for Memory Calculations
     * Avoid integer truncation when accumulating byte sizes
     */
    public static long calculateTotalMemoryUsage(List<CacheKey> keys) {
        long totalMemory = 0L;  // Use long, not int
        
        for (CacheKey key : keys) {
            if (key.state != CacheState.EVICTED) {
                totalMemory += key.sizeBytes;
            }
        }
        
        return totalMemory;
    }
    
    /**
     * Part 2: Track Cache Key States and Calculate Hit Ratios
     */
    public static class CacheMonitor {
        Map<String, CacheKey> cache;
        long maxCapacity;
        long currentUsage;
        
        public CacheMonitor(long maxCapacityBytes) {
            this.cache = new LinkedHashMap<>();
            this.maxCapacity = maxCapacityBytes;
            this.currentUsage = 0L;
        }
        
        public void allocateKey(String key, long sizeBytes) {
            if (currentUsage + sizeBytes <= maxCapacity) {
                CacheKey cacheKey = new CacheKey(key, sizeBytes);
                cache.put(key, cacheKey);
                currentUsage += sizeBytes;
            }
        }
        
        public void recordHit(String key) {
            CacheKey cacheKey = cache.get(key);
            if (cacheKey != null) {
                cacheKey.state = CacheState.READ_HIT;
                cacheKey.lastAccessTime = System.currentTimeMillis();
                cacheKey.hitCount++;
            }
        }
        
        public double calculateHitRatio() {
            int totalHits = 0;
            int totalKeys = 0;
            
            for (CacheKey key : cache.values()) {
                if (key.state != CacheState.EVICTED) {
                    totalHits += key.hitCount;
                    totalKeys++;
                }
            }
            
            return totalKeys == 0 ? 0.0 : (double) totalHits / totalKeys;
        }
        
        public List<String> findDeadKeys() {
            List<String> dead = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            long oneHourMs = 3600 * 1000;
            
            for (CacheKey key : cache.values()) {
                if (key.state == CacheState.ALLOCATED && 
                    (currentTime - key.lastAccessTime) > oneHourMs) {
                    dead.add(key.key);
                }
            }
            
            return dead;
        }
    }
    
    /**
     * Part 3: LRU Eviction Simulation
     */
    public static class LRUCache {
        int capacity;
        LinkedHashMap<String, CacheKey> cache;
        
        public LRUCache(int capacity) {
            this.capacity = capacity;
            this.cache = new LinkedHashMap<String, CacheKey>(capacity, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > capacity;
                }
            };
        }
        
        public void put(String key, CacheKey value) {
            if (cache.containsKey(key)) {
                value.hitCount++;
            }
            cache.put(key, value);
        }
        
        public CacheKey get(String key) {
            CacheKey value = cache.get(key);
            if (value != null) {
                value.hitCount++;
            }
            return value;
        }
        
        public List<String> getEvictionOrder() {
            return new ArrayList<>(cache.keySet());
        }
        
        public String predictNextEviction() {
            if (cache.isEmpty()) return null;
            return cache.keySet().iterator().next();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 56: Distributed Cache Eviction Monitor ===");
        
        List<CacheKey> keys = Arrays.asList(
            new CacheKey("key1", 1024L),
            new CacheKey("key2", 2048L),
            new CacheKey("key3", 512L)
        );
        
        long totalMemory = calculateTotalMemoryUsage(keys);
        System.out.println("Total memory usage: " + totalMemory + " bytes");
        
        CacheMonitor monitor = new CacheMonitor(10000L);
        monitor.allocateKey("key1", 1000L);
        monitor.allocateKey("key2", 2000L);
        monitor.recordHit("key1");
        monitor.recordHit("key1");
        System.out.println("Hit ratio: " + monitor.calculateHitRatio());
        
        LRUCache lru = new LRUCache(3);
        lru.put("a", new CacheKey("a", 100));
        lru.put("b", new CacheKey("b", 200));
        lru.put("c", new CacheKey("c", 300));
        System.out.println("Next eviction: " + lru.predictNextEviction());
    }
}
