package category1_state_machines;

import java.util.*;

/**
 * Problem 7: Bank ATM Transaction Integrity Scanner
 * 
 * Three-part progression:
 * - Part 1: Fix numeric overflow (use long instead of short for transaction amounts)
 * - Part 2: Track user card sessions and detect unauthorized withdrawals
 * - Part 3: Detect velocity anomalies (multi-location withdrawals within 2 hours)
 */
public class Problem7_BankATMTransactionScanner {

    /**
     * Part 1: Bug Fix - Numeric overflow handling
     * Issue: Transaction amounts overflow when using short integers
     * Solution: Use long for large monetary amounts
     */
    public static class Part1_BugFix {
        /**
         * Parse transaction amount safely
         */
        public static long parseTransactionAmount(String amountStr) {
            if (amountStr == null || amountStr.isEmpty()) {
                throw new IllegalArgumentException("Invalid amount");
            }
            
            try {
                // Remove common currency symbols
                String cleaned = amountStr.replaceAll("[^0-9.-]", "");
                return Long.parseLong(cleaned);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse amount: " + amountStr, e);
            }
        }

        /**
         * Validate transaction doesn't overflow
         */
        public static boolean isValidAmount(String amountStr) {
            try {
                long amount = parseTransactionAmount(amountStr);
                // Check reasonable limits (e.g., max daily withdrawal)
                return amount > 0 && amount <= 1_000_000_00; // 1M dollars
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    /**
     * Part 2: State Machine - Card session tracking
     * Sequence: CARD_INSERTED -> PIN_ENTERED -> WITHDRAW_REQUEST -> CARD_EJECTED
     */
    public static class Part2_CardSessionTracking {
        public static class ATMTransaction {
            public String accountId;
            public String atmId;
            public String status; // CARD_INSERTED, PIN_ENTERED, WITHDRAW_REQUEST, CARD_EJECTED
            public long timestamp;
            public long amount; // in cents

            public ATMTransaction(String accountId, String atmId, String status, long timestamp, long amount) {
                this.accountId = accountId;
                this.atmId = atmId;
                this.status = status;
                this.timestamp = timestamp;
                this.amount = amount;
            }

            @Override
            public String toString() {
                return String.format("%s[%s]: %s @$%.2f [%s]",
                    accountId, atmId, status, amount / 100.0, timestamp);
            }
        }

        /**
         * Find accounts with unauthorized withdrawals
         * Unauthorized = money dispensed without PIN verification
         */
        public static List<String> findUnauthorizedWithdrawals(List<ATMTransaction> transactions) {
            if (transactions == null || transactions.isEmpty()) {
                return new ArrayList<>();
            }

            // Group by account
            Map<String, List<ATMTransaction>> accountSessions = new HashMap<>();
            for (ATMTransaction tx : transactions) {
                accountSessions.putIfAbsent(tx.accountId, new ArrayList<>());
                accountSessions.get(tx.accountId).add(tx);
            }

            // Sort each account's transactions
            for (List<ATMTransaction> session : accountSessions.values()) {
                session.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
            }

            List<String> unauthorizedAccounts = new ArrayList<>();

            for (Map.Entry<String, List<ATMTransaction>> entry : accountSessions.entrySet()) {
                String accountId = entry.getKey();
                List<ATMTransaction> session = entry.getValue();

                boolean pinEntered = false;
                boolean cardInserted = false;
                boolean moneyDispensed = false;

                for (ATMTransaction tx : session) {
                    switch (tx.status) {
                        case "CARD_INSERTED":
                            cardInserted = true;
                            pinEntered = false; // Reset on new card insertion
                            break;
                        case "PIN_ENTERED":
                            pinEntered = true;
                            break;
                        case "WITHDRAW_REQUEST":
                            moneyDispensed = true;
                            // Check if PIN was verified before withdrawal
                            if (!pinEntered) {
                                unauthorizedAccounts.add(accountId);
                                moneyDispensed = false; // Only flag once
                                break;
                            }
                            break;
                        case "CARD_EJECTED":
                            cardInserted = false;
                            break;
                    }
                }
            }

            return unauthorizedAccounts;
        }
    }

    /**
     * Part 3: Velocity Alert - Multi-location withdrawals
     * Flag: 2+ ATM withdrawals >50 miles apart within 2 hours
     */
    public static class Part3_VelocityAlert {
        public static class ATMLocation {
            public String atmId;
            public double latitude;
            public double longitude;

            public ATMLocation(String atmId, double latitude, double longitude) {
                this.atmId = atmId;
                this.latitude = latitude;
                this.longitude = longitude;
            }
        }

        /**
         * Calculate distance between two coordinates (simplified)
         * Using Haversine formula approximation
         */
        public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
            // Simplified: 1 degree latitude ≈ 69 miles, 1 degree longitude ≈ 55 miles
            double latDiff = Math.abs(lat2 - lat1);
            double lonDiff = Math.abs(lon2 - lon1);
            
            double latMiles = latDiff * 69;
            double lonMiles = lonDiff * 55;
            
            return Math.sqrt(latMiles * latMiles + lonMiles * lonMiles);
        }

        /**
         * Detect velocity anomalies
         */
        public static List<String> detectVelocityAnomalies(
            List<Part2_CardSessionTracking.ATMTransaction> transactions,
            Map<String, ATMLocation> atmLocations) {
            
            if (transactions == null || transactions.isEmpty()) {
                return new ArrayList<>();
            }

            // Group by account
            Map<String, List<Part2_CardSessionTracking.ATMTransaction>> accountTxs = new HashMap<>();
            for (Part2_CardSessionTracking.ATMTransaction tx : transactions) {
                accountTxs.putIfAbsent(tx.accountId, new ArrayList<>());
                accountTxs.get(tx.accountId).add(tx);
            }

            List<String> suspiciousAccounts = new ArrayList<>();
            long TWO_HOURS = 2 * 60 * 60 * 1000; // milliseconds
            double DISTANCE_THRESHOLD = 50; // miles

            for (Map.Entry<String, List<Part2_CardSessionTracking.ATMTransaction>> entry : accountTxs.entrySet()) {
                String accountId = entry.getKey();
                List<Part2_CardSessionTracking.ATMTransaction> txList = entry.getValue();

                // Get withdrawal transactions only
                List<Part2_CardSessionTracking.ATMTransaction> withdrawals = new ArrayList<>();
                for (Part2_CardSessionTracking.ATMTransaction tx : txList) {
                    if ("WITHDRAW_REQUEST".equals(tx.status)) {
                        withdrawals.add(tx);
                    }
                }

                // Sort by timestamp
                withdrawals.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

                // Check for velocity
                for (int i = 0; i < withdrawals.size() - 1; i++) {
                    Part2_CardSessionTracking.ATMTransaction tx1 = withdrawals.get(i);
                    Part2_CardSessionTracking.ATMTransaction tx2 = withdrawals.get(i + 1);

                    long timeDiff = tx2.timestamp - tx1.timestamp;
                    if (timeDiff <= TWO_HOURS) {
                        // Check distance
                        ATMLocation loc1 = atmLocations.get(tx1.atmId);
                        ATMLocation loc2 = atmLocations.get(tx2.atmId);

                        if (loc1 != null && loc2 != null) {
                            double distance = calculateDistance(
                                loc1.latitude, loc1.longitude,
                                loc2.latitude, loc2.longitude
                            );

                            if (distance > DISTANCE_THRESHOLD) {
                                suspiciousAccounts.add(accountId);
                                break;
                            }
                        }
                    }
                }
            }

            return suspiciousAccounts;
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 7: Bank ATM Transaction Integrity Scanner ===");

        // Part 1: Test amount parsing
        System.out.println("\n--- Part 1: Amount Parsing ---");
        String[] amounts = {"$500", "1000", "$999,999.99", "2500000"};
        for (String amount : amounts) {
            try {
                long parsed = Part1_BugFix.parseTransactionAmount(amount);
                System.out.println("  " + amount + " -> " + parsed + " cents");
            } catch (IllegalArgumentException e) {
                System.out.println("  " + amount + " -> ERROR: " + e.getMessage());
            }
        }

        // Part 2: Test unauthorized withdrawals
        System.out.println("\n--- Part 2: Unauthorized Withdrawal Detection ---");
        List<Part2_CardSessionTracking.ATMTransaction> transactions = Arrays.asList(
            // Account 1: Authorized
            new Part2_CardSessionTracking.ATMTransaction("acc1", "atm1", "CARD_INSERTED", 1000, 0),
            new Part2_CardSessionTracking.ATMTransaction("acc1", "atm1", "PIN_ENTERED", 1100, 0),
            new Part2_CardSessionTracking.ATMTransaction("acc1", "atm1", "WITHDRAW_REQUEST", 1200, 50000),
            new Part2_CardSessionTracking.ATMTransaction("acc1", "atm1", "CARD_EJECTED", 1300, 0),
            
            // Account 2: Unauthorized
            new Part2_CardSessionTracking.ATMTransaction("acc2", "atm2", "CARD_INSERTED", 2000, 0),
            new Part2_CardSessionTracking.ATMTransaction("acc2", "atm2", "WITHDRAW_REQUEST", 2100, 75000), // No PIN!
            new Part2_CardSessionTracking.ATMTransaction("acc2", "atm2", "CARD_EJECTED", 2200, 0)
        );
        List<String> unauthorized = Part2_CardSessionTracking.findUnauthorizedWithdrawals(transactions);
        System.out.println("Unauthorized accounts: " + unauthorized);

        // Part 3: Test velocity detection
        System.out.println("\n--- Part 3: Velocity Anomaly Detection ---");
        Map<String, Part3_VelocityAlert.ATMLocation> locations = new HashMap<>();
        locations.put("atm1", new Part3_VelocityAlert.ATMLocation("atm1", 40.7128, -74.0060)); // NYC
        locations.put("atm2", new Part3_VelocityAlert.ATMLocation("atm2", 40.7500, -73.9900)); // NYC (nearby)
        locations.put("atm3", new Part3_VelocityAlert.ATMLocation("atm3", 34.0522, -118.2437)); // LA (far)

        List<Part2_CardSessionTracking.ATMTransaction> velocityTxs = Arrays.asList(
            // Fast transactions nearby
            new Part2_CardSessionTracking.ATMTransaction("acc3", "atm1", "WITHDRAW_REQUEST", 3000, 10000),
            new Part2_CardSessionTracking.ATMTransaction("acc3", "atm2", "WITHDRAW_REQUEST", 3600000, 15000), // 1 hour later
            
            // Fast transactions far apart (suspicious)
            new Part2_CardSessionTracking.ATMTransaction("acc4", "atm1", "WITHDRAW_REQUEST", 4000, 20000),
            new Part2_CardSessionTracking.ATMTransaction("acc4", "atm3", "WITHDRAW_REQUEST", 4300000, 25000) // ~1h later, far location
        );
        List<String> suspicious = Part3_VelocityAlert.detectVelocityAnomalies(velocityTxs, locations);
        System.out.println("Suspicious accounts: " + suspicious);
    }
}
