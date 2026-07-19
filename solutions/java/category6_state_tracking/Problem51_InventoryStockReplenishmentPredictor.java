package category6_state_tracking;

import java.util.*;

/**
 * Problem 51: Inventory Stock Replenishment Predictor
 * 
 * Three-part progression:
 * - Part 1: Handle zero-quantity edge cases in division
 * - Part 2: Track inventory through transaction lifecycle
 * - Part 3: Predict stockout risk using burn rates
 */
public class Problem51_InventoryStockReplenishmentPredictor {
    
    static class InventoryItem {
        String sku;
        int totalStock;
        int reserved;
        int available;
        int burnRate;  // units per day
        int leadTime;  // days
        
        InventoryItem(String sku, int total, int reserved, int burnRate, int leadTime) {
            this.sku = sku;
            this.totalStock = total;
            this.reserved = reserved;
            this.available = total - reserved;
            this.burnRate = burnRate;
            this.leadTime = leadTime;
        }
    }
    
    /**
     * Part 1: Bug Fix - Zero Division Guard
     * Add non-zero guard before dividing by stock counts
     */
    public static double calculateStockRatio(InventoryItem item) {
        if (item.totalStock == 0) {
            return 0.0;
        }
        return (double) item.available / item.totalStock;
    }
    
    /**
     * Part 2: Track Inventory Through Transaction States
     * States: RECEIVED, SOLD, RETURNED
     */
    public static class InventoryTracker {
        Map<String, InventoryItem> inventory;
        List<String> transactionLog;
        
        public InventoryTracker() {
            this.inventory = new HashMap<>();
            this.transactionLog = new ArrayList<>();
        }
        
        public void processTransaction(String sku, String type, int quantity) {
            InventoryItem item = inventory.computeIfAbsent(sku, 
                k -> new InventoryItem(sku, 0, 0, 0, 0));
            
            switch (type) {
                case "RECEIVED":
                    item.totalStock += quantity;
                    item.available += quantity;
                    break;
                case "SOLD":
                    if (item.available >= quantity) {
                        item.available -= quantity;
                        item.reserved += quantity;
                    }
                    break;
                case "RETURNED":
                    item.available += quantity;
                    item.reserved -= quantity;
                    break;
            }
            
            transactionLog.add(sku + ": " + type + " " + quantity);
        }
        
        public List<String> findCriticalItems() {
            List<String> critical = new ArrayList<>();
            
            for (InventoryItem item : inventory.values()) {
                if (item.available <= item.burnRate) {
                    critical.add(item.sku);
                }
            }
            
            return critical;
        }
    }
    
    /**
     * Part 3: Lead Time Buffer & Stockout Risk
     * Identify SKUs at risk of stockout
     */
    public static List<String> identifyStockoutRisk(Map<String, InventoryItem> inventory) {
        List<String> atRisk = new ArrayList<>();
        
        for (InventoryItem item : inventory.values()) {
            // Calculate days until stockout
            int daysUntilStockout = item.available / Math.max(item.burnRate, 1);
            
            // If burnout happens before new stock arrives, mark as at-risk
            if (daysUntilStockout < item.leadTime) {
                atRisk.add(item.sku);
            }
        }
        
        return atRisk;
    }
    
    /**
     * Predict future stockout date
     */
    public static int predictStockoutDays(InventoryItem item) {
        if (item.burnRate == 0) {
            return Integer.MAX_VALUE;  // Never runs out
        }
        return item.available / item.burnRate;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 51: Inventory Stock Replenishment ===");
        
        InventoryItem item1 = new InventoryItem("SKU_001", 100, 20, 5, 10);
        System.out.println("Stock ratio for SKU_001: " + calculateStockRatio(item1));
        
        InventoryTracker tracker = new InventoryTracker();
        tracker.processTransaction("SKU_002", "RECEIVED", 500);
        tracker.processTransaction("SKU_002", "SOLD", 100);
        tracker.processTransaction("SKU_002", "SOLD", 50);
        System.out.println("Transactions: " + tracker.transactionLog);
        
        Map<String, InventoryItem> inventory = new HashMap<>();
        inventory.put("SKU_001", new InventoryItem("SKU_001", 200, 50, 10, 5));
        inventory.put("SKU_002", new InventoryItem("SKU_002", 50, 20, 5, 10));
        
        List<String> atRisk = identifyStockoutRisk(inventory);
        System.out.println("Items at stockout risk: " + atRisk);
    }
}
