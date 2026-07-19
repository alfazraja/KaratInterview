package category7_advanced_systems;

import java.math.BigDecimal;
import java.util.*;

/**
 * Problem 64: Retail Point-of-Sale (POS) Receipt Auditor
 * 
 * Three-part progression:
 * - Part 1: Use BigDecimal for precise decimal arithmetic
 * - Part 2: Track checkout states and coupon application
 * - Part 3: Detect return fraud patterns
 */
public class Problem64_RetailPointOfSaleReceiptAuditor {
    
    enum CheckoutState {
        SCAN_ITEM, APPLY_COUPON, VOID_ITEM, PAYMENT_SUCCESS
    }
    
    static class LineItem {
        String itemCode;
        BigDecimal unitPrice;
        int quantity;
        BigDecimal discount;
        
        LineItem(String code, BigDecimal price, int qty) {
            this.itemCode = code;
            this.unitPrice = price;
            this.quantity = qty;
            this.discount = BigDecimal.ZERO;
        }
        
        public BigDecimal getLineTotal() {
            BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
            return subtotal.subtract(discount);
        }
    }
    
    /**
     * Part 1: Bug Fix - Use BigDecimal for Price Calculations
     * Avoid floating-point rounding errors
     */
    public static BigDecimal calculateReceiptTotal(List<LineItem> items, BigDecimal taxRate) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (LineItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
        }
        
        BigDecimal tax = subtotal.multiply(taxRate);
        return subtotal.add(tax);
    }
    
    /**
     * Part 2: Track Checkout States
     * Identify anomalies in state transitions
     */
    public static class CheckoutTracker {
        Map<String, List<CheckoutState>> transactionStates = new HashMap<>();
        Map<String, BigDecimal> couponAmounts = new HashMap<>();
        Map<String, BigDecimal> finalTotals = new HashMap<>();
        
        public void recordState(String transactionId, CheckoutState state) {
            transactionStates.computeIfAbsent(transactionId, k -> new ArrayList<>())
                            .add(state);
        }
        
        public void applyCoupon(String transactionId, BigDecimal amount) {
            couponAmounts.put(transactionId, amount);
        }
        
        public void recordPayment(String transactionId, BigDecimal amount) {
            finalTotals.put(transactionId, amount);
        }
        
        public List<String> findAnomalies() {
            List<String> anomalies = new ArrayList<>();
            
            for (Map.Entry<String, List<CheckoutState>> entry : transactionStates.entrySet()) {
                String txId = entry.getKey();
                List<CheckoutState> states = entry.getValue();
                
                // Check if coupon remained after payment
                if (states.contains(CheckoutState.APPLY_COUPON) && 
                    states.contains(CheckoutState.PAYMENT_SUCCESS)) {
                    
                    int couponIdx = states.indexOf(CheckoutState.APPLY_COUPON);
                    int paymentIdx = states.indexOf(CheckoutState.PAYMENT_SUCCESS);
                    
                    if (couponIdx < paymentIdx) {
                        // Check if coupon was voided
                        boolean voided = false;
                        for (int i = couponIdx + 1; i < paymentIdx; i++) {
                            if (states.get(i) == CheckoutState.VOID_ITEM) {
                                voided = true;
                                break;
                            }
                        }
                        
                        if (!voided) {
                            anomalies.add("Transaction " + txId + ": Coupon not voided before payment");
                        }
                    }
                }
            }
            
            return anomalies;
        }
    }
    
    /**
     * Part 3: Return Fraud Detection
     */
    public static class FraudDetector {
        Map<String, List<Long>> customerTransactions = new HashMap<>();
        Map<String, BigDecimal> refundAmounts = new HashMap<>();
        
        public void recordTransaction(String customerId, String txId, BigDecimal amount, long timestamp) {
            customerTransactions.computeIfAbsent(customerId, k -> new ArrayList<>())
                                .add(timestamp);
        }
        
        public void recordRefund(String customerId, BigDecimal amount) {
            refundAmounts.put(customerId, 
                            refundAmounts.getOrDefault(customerId, BigDecimal.ZERO)
                                       .add(amount));
        }
        
        public List<String> flagSuspiciousRefunds(BigDecimal highValueThreshold, long hoursWindow) {
            List<String> suspicious = new ArrayList<>();
            long windowMs = hoursWindow * 3600 * 1000;
            
            for (Map.Entry<String, BigDecimal> entry : refundAmounts.entrySet()) {
                String customerId = entry.getKey();
                BigDecimal totalRefunds = entry.getValue();
                
                // Flag if high-value refunds within 24 hours of purchase
                if (totalRefunds.compareTo(highValueThreshold) > 0) {
                    List<Long> txTimes = customerTransactions.getOrDefault(customerId, new ArrayList<>());
                    
                    if (!txTimes.isEmpty()) {
                        long newestTx = txTimes.stream().max(Long::compareTo).orElse(0L);
                        long now = System.currentTimeMillis();
                        
                        if ((now - newestTx) < windowMs) {
                            suspicious.add("Customer " + customerId + ": " + totalRefunds + 
                                         " refunded within " + hoursWindow + "h");
                        }
                    }
                }
            }
            
            return suspicious;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 64: Retail POS Receipt Auditor ===");
        
        // Test Part 1: Precise decimal arithmetic
        List<LineItem> items = Arrays.asList(
            new LineItem("ITEM1", new BigDecimal("19.99"), 2),
            new LineItem("ITEM2", new BigDecimal("5.50"), 1)
        );
        
        BigDecimal total = calculateReceiptTotal(items, new BigDecimal("0.08"));
        System.out.println("Receipt total: $" + total);
        
        // Test Part 2: Checkout tracking
        CheckoutTracker tracker = new CheckoutTracker();
        tracker.recordState("TX001", CheckoutState.SCAN_ITEM);
        tracker.recordState("TX001", CheckoutState.APPLY_COUPON);
        tracker.recordState("TX001", CheckoutState.PAYMENT_SUCCESS);
        
        List<String> anomalies = tracker.findAnomalies();
        System.out.println("Anomalies: " + anomalies);
        
        // Test Part 3: Fraud detection
        FraudDetector detector = new FraudDetector();
        detector.recordTransaction("CUST001", "TX001", new BigDecimal("500"), System.currentTimeMillis());
        detector.recordRefund("CUST001", new BigDecimal("499"));
        
        List<String> suspicious = detector.flagSuspiciousRefunds(new BigDecimal("100"), 24);
        System.out.println("Suspicious refunds: " + suspicious);
    }
}
