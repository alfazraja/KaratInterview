package category6_state_tracking;

import java.util.*;

/**
 * Problem 54: Online Auction Sniping Prevention Engine
 * 
 * Three-part progression:
 * - Part 1: Use UTC timestamps instead of localized strings
 * - Part 2: Track auction state transitions
 * - Part 3: Implement soft-close buffer for anti-sniping
 */
public class Problem54_OnlineAuctionSnipingPreventionEngine {
    
    enum AuctionState {
        OPEN, BID_PLACED, EXTENDED, CLOSED
    }
    
    static class AuctionBid {
        String bidderId;
        double amount;
        long timestamp;  // UTC epoch milliseconds
        
        AuctionBid(String bidderId, double amount, long timestamp) {
            this.bidderId = bidderId;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }
    
    static class Auction {
        String auctionId;
        long closingTime;  // UTC epoch
        AuctionState state;
        List<AuctionBid> bids;
        
        Auction(String auctionId, long closingTime) {
            this.auctionId = auctionId;
            this.closingTime = closingTime;
            this.state = AuctionState.OPEN;
            this.bids = new ArrayList<>();
        }
    }
    
    /**
     * Part 1: Bug Fix - Use UTC Timestamps
     * Parse using absolute universal epochs, not localized strings
     */
    public static long parseUtcTimestamp(String utcString) {
        // Assume input is ISO 8601 or epoch milliseconds
        try {
            return Long.parseLong(utcString);  // Direct epoch parsing
        } catch (NumberFormatException e) {
            // Handle ISO 8601 format if needed
            return System.currentTimeMillis();
        }
    }
    
    /**
     * Part 2: Track Auction State Transitions
     */
    public static class AuctionTracker {
        Map<String, Auction> auctions;
        
        public AuctionTracker() {
            this.auctions = new HashMap<>();
        }
        
        public void createAuction(String auctionId, long closingTime) {
            auctions.put(auctionId, new Auction(auctionId, closingTime));
        }
        
        public void placeBid(String auctionId, String bidderId, double amount, long timestamp) {
            Auction auction = auctions.get(auctionId);
            if (auction == null) return;
            
            if (timestamp < auction.closingTime) {
                AuctionBid bid = new AuctionBid(bidderId, amount, timestamp);
                auction.bids.add(bid);
                auction.state = AuctionState.BID_PLACED;
            } else {
                // Bid after closing time - potential violation
                System.err.println("Bid placed after closing time!");
            }
        }
        
        public List<String> findAnomalies() {
            List<String> anomalies = new ArrayList<>();
            
            for (Auction auction : auctions.values()) {
                for (AuctionBid bid : auction.bids) {
                    // Check if bid placed past closing threshold
                    if (bid.timestamp >= auction.closingTime) {
                        anomalies.add("Auction " + auction.auctionId + 
                                    ": Bid by " + bid.bidderId + " at " + bid.timestamp);
                    }
                }
            }
            
            return anomalies;
        }
    }
    
    /**
     * Part 3: Soft-Close Buffer (Anti-Sniping)
     * Extend closing time if bid arrives within final 2 minutes
     */
    public static long applySoftCloseBuffer(long originalClosingTime, long bidTimestamp,
                                            long bufferMinutes) {
        long bufferMs = bufferMinutes * 60 * 1000;
        long snipeThreshold = originalClosingTime - bufferMs;
        
        if (bidTimestamp > snipeThreshold && bidTimestamp < originalClosingTime) {
            // Bid within buffer window - extend closing time
            return bidTimestamp + bufferMs;
        }
        
        return originalClosingTime;
    }
    
    /**
     * Manage anti-sniping extension
     */
    public static Map<String, Long> manageAuctionExtensions(Map<String, Auction> auctions,
                                                            long bufferMinutes) {
        Map<String, Long> newClosingTimes = new HashMap<>();
        
        for (Auction auction : auctions.values()) {
            long currentClosing = auction.closingTime;
            
            for (AuctionBid bid : auction.bids) {
                long newClosing = applySoftCloseBuffer(currentClosing, bid.timestamp, bufferMinutes);
                if (newClosing > currentClosing) {
                    currentClosing = newClosing;
                    auction.state = AuctionState.EXTENDED;
                }
            }
            
            if (currentClosing > auction.closingTime) {
                newClosingTimes.put(auction.auctionId, currentClosing);
            }
        }
        
        return newClosingTimes;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 54: Online Auction Sniping Prevention ===");
        
        AuctionTracker tracker = new AuctionTracker();
        long originalClosingTime = System.currentTimeMillis() + 10000;
        
        tracker.createAuction("AUC_001", originalClosingTime);
        tracker.placeBid("AUC_001", "Bidder_A", 100.0, System.currentTimeMillis());
        System.out.println("Auctions tracked");
        
        long softClosingTime = applySoftCloseBuffer(originalClosingTime, 
            originalClosingTime - 30000, 2);
        System.out.println("Original closing: " + originalClosingTime);
        System.out.println("Soft closing: " + softClosingTime);
    }
}
