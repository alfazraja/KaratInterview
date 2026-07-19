package category6_state_tracking;

import java.util.*;

/**
 * Problem 58: Cryptographic Token Transaction Vault
 * 
 * Three-part progression:
 * - Part 1: Implement synchronized access for thread-safe updates
 * - Part 2: Track transaction states (DEPOSIT, LOCK_STAKE, RELEASE, WITHDRAW)
 * - Part 3: Detect double-spend attempts
 */
public class Problem58_CryptographicTokenTransactionVault {
    
    enum TransactionState {
        DEPOSIT, LOCK_STAKE, RELEASE, WITHDRAW
    }
    
    static class Transaction {
        String transactionId;
        String accountId;
        long blockTimestamp;
        TransactionState state;
        double amount;
        
        Transaction(String txId, String accountId, long timestamp, 
                   TransactionState state, double amount) {
            this.transactionId = txId;
            this.accountId = accountId;
            this.blockTimestamp = timestamp;
            this.state = state;
            this.amount = amount;
        }
    }
    
    /**
     * Part 1: Thread-Safe Wallet Balance Updates
     * Use synchronized access for critical sections
     */
    public static class CryptoWallet {
        private Map<String, Double> balances = Collections.synchronizedMap(new HashMap<>());
        private List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());
        
        public synchronized void deposit(String accountId, double amount) {
            double currentBalance = balances.getOrDefault(accountId, 0.0);
            balances.put(accountId, currentBalance + amount);
            transactions.add(new Transaction(
                UUID.randomUUID().toString(),
                accountId,
                System.currentTimeMillis(),
                TransactionState.DEPOSIT,
                amount
            ));
        }
        
        public synchronized boolean withdraw(String accountId, double amount) {
            double currentBalance = balances.getOrDefault(accountId, 0.0);
            
            if (currentBalance >= amount) {
                balances.put(accountId, currentBalance - amount);
                transactions.add(new Transaction(
                    UUID.randomUUID().toString(),
                    accountId,
                    System.currentTimeMillis(),
                    TransactionState.WITHDRAW,
                    amount
                ));
                return true;
            }
            return false;
        }
        
        public synchronized double getBalance(String accountId) {
            return balances.getOrDefault(accountId, 0.0);
        }
    }
    
    /**
     * Part 2: Track Transaction Lifecycle States
     */
    public static class TransactionAuditor {
        Map<String, List<Transaction>> accountTransactions = new HashMap<>();
        
        public void recordTransaction(Transaction tx) {
            accountTransactions.computeIfAbsent(tx.accountId, k -> new ArrayList<>())
                               .add(tx);
        }
        
        public List<String> findAnomalies() {
            List<String> anomalies = new ArrayList<>();
            
            for (Map.Entry<String, List<Transaction>> entry : accountTransactions.entrySet()) {
                String accountId = entry.getKey();
                List<Transaction> txs = entry.getValue();
                
                // Sort by timestamp
                txs.sort(Comparator.comparingLong(t -> t.blockTimestamp));
                
                for (int i = 0; i < txs.size(); i++) {
                    Transaction tx = txs.get(i);
                    
                    // Check for invalid state transitions
                    if (tx.state == TransactionState.WITHDRAW) {
                        // Must have LOCK_STAKE before WITHDRAW
                        boolean hasLockStake = false;
                        for (int j = i - 1; j >= 0; j--) {
                            if (txs.get(j).state == TransactionState.LOCK_STAKE) {
                                hasLockStake = true;
                                break;
                            }
                            if (txs.get(j).state == TransactionState.RELEASE) {
                                break;  // Lock was released
                            }
                        }
                        
                        if (!hasLockStake) {
                            anomalies.add("Account " + accountId + " withdrew without lock: " + tx.transactionId);
                        }
                    }
                }
            }
            
            return anomalies;
        }
    }
    
    /**
     * Part 3: Detect Double-Spend Attempts
     * Flag accounts with matching withdrawal timestamps
     */
    public static List<String> detectDoubleSpend(List<Transaction> transactions) {
        List<String> doubleSpenders = new ArrayList<>();
        Map<Long, List<String>> withdrawalsByTime = new HashMap<>();
        
        for (Transaction tx : transactions) {
            if (tx.state == TransactionState.WITHDRAW) {
                withdrawalsByTime.computeIfAbsent(tx.blockTimestamp, k -> new ArrayList<>())
                                .add(tx.accountId);
            }
        }
        
        for (List<String> accounts : withdrawalsByTime.values()) {
            if (accounts.size() > 1) {
                // Multiple withdrawals at same timestamp - potential double spend
                Set<String> unique = new HashSet<>(accounts);
                if (unique.size() > 1) {
                    doubleSpenders.addAll(unique);
                }
            }
        }
        
        return doubleSpenders;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 58: Cryptographic Token Transaction Vault ===");
        
        CryptoWallet wallet = new CryptoWallet();
        wallet.deposit("account1", 1000.0);
        wallet.deposit("account1", 500.0);
        System.out.println("Balance: " + wallet.getBalance("account1"));
        
        boolean withdrawn = wallet.withdraw("account1", 200.0);
        System.out.println("Withdrawal successful: " + withdrawn);
        System.out.println("New balance: " + wallet.getBalance("account1"));
        
        TransactionAuditor auditor = new TransactionAuditor();
        auditor.recordTransaction(new Transaction("tx1", "account1", 1000, 
                                                 TransactionState.DEPOSIT, 100.0));
        System.out.println("Transaction audit complete");
    }
}
