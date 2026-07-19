package category7_advanced_systems;

import java.util.*;

/**
 * Problem 61: Blockchain Block Header Sync Auditor
 * 
 * Three-part progression:
 * - Part 1: Fix hex string checksum formatting
 * - Part 2: Trace longest validated block chain
 * - Part 3: Detect malicious block chain reorganization
 */
public class Problem61_BlockchainBlockHeaderSyncAuditor {
    
    static class BlockHeader {
        String blockHash;      // Hexadecimal hash
        String parentHash;     // Previous block reference
        long timestamp;
        int nonce;
        
        BlockHeader(String hash, String parent, long timestamp, int nonce) {
            this.blockHash = hash;
            this.parentHash = parent;
            this.timestamp = timestamp;
            this.nonce = nonce;
        }
    }
    
    /**
     * Part 1: Bug Fix - Proper Hex String Formatting
     * Use zero-padding for consistent checksums
     */
    public static String formatHexChecksum(String hexString) {
        // Ensure proper format with zero-padding
        if (hexString == null || hexString.isEmpty()) {
            return "0";
        }
        
        // Remove 0x prefix if present
        String clean = hexString.startsWith("0x") ? hexString.substring(2) : hexString;
        
        // Validate hex format
        try {
            Long.parseLong(clean, 16);
            // Pad to 64 characters (256-bit hash)
            return String.format("%064x", new java.math.BigInteger(clean, 16));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Verify hash format is valid
     */
    public static boolean isValidBlockHash(String hash) {
        String formatted = formatHexChecksum(hash);
        return formatted != null && formatted.length() == 64;
    }
    
    /**
     * Part 2: Trace Longest Validated Block Chain
     * Find longest sequence from genesis to tip
     */
    public static class BlockchainValidator {
        Map<String, BlockHeader> blocks = new HashMap<>();
        Set<String> validatedBlocks = new HashSet<>();
        
        public void addBlock(BlockHeader header) {
            if (isValidBlockHash(header.blockHash)) {
                blocks.put(header.blockHash, header);
            }
        }
        
        public List<String> findLongestChain(String tipHash) {
            List<String> chain = new ArrayList<>();
            String current = tipHash;
            Set<String> visited = new HashSet<>();
            
            while (current != null && !visited.contains(current)) {
                chain.add(0, current);  // Add to front
                visited.add(current);
                validatedBlocks.add(current);
                
                BlockHeader header = blocks.get(current);
                current = (header != null) ? header.parentHash : null;
            }
            
            return chain;
        }
        
        public int getLongestChainLength() {
            int maxLength = 0;
            
            for (String hash : blocks.keySet()) {
                List<String> chain = findLongestChain(hash);
                maxLength = Math.max(maxLength, chain.size());
            }
            
            return maxLength;
        }
    }
    
    /**
     * Part 3: Detect Malicious Reorg
     * Flag when minor branch broadcasts chain with more blocks
     */
    public static class ReorgDetector {
        Map<String, Integer> chainLengths = new HashMap<>();
        
        public List<String> detectReorg(Map<String, BlockHeader> allBlocks,
                                        String mainChainTip,
                                        String alternateChainTip) {
            List<String> anomalies = new ArrayList<>();
            
            BlockchainValidator validator = new BlockchainValidator();
            for (BlockHeader header : allBlocks.values()) {
                validator.addBlock(header);
            }
            
            List<String> mainChain = validator.findLongestChain(mainChainTip);
            List<String> alternateChain = validator.findLongestChain(alternateChainTip);
            
            // Check if alternate chain is longer or equal to main chain
            if (alternateChain.size() >= mainChain.size()) {
                anomalies.add("ALERT: Alternate chain (" + alternateChain.size() + 
                            ") >= main chain (" + mainChain.size() + ")");
            }
            
            // Find divergence point
            int commonAncestors = 0;
            for (int i = 0; i < Math.min(mainChain.size(), alternateChain.size()); i++) {
                if (mainChain.get(i).equals(alternateChain.get(i))) {
                    commonAncestors++;
                } else {
                    break;
                }
            }
            
            if (commonAncestors < Math.min(mainChain.size(), alternateChain.size())) {
                anomalies.add("Chain divergence detected at depth: " + commonAncestors);
            }
            
            return anomalies;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 61: Blockchain Block Header Sync ===");
        
        // Test Part 1: Hex formatting
        String hash1 = "a1b2c3d4e5f6";
        String formatted = formatHexChecksum(hash1);
        System.out.println("Formatted hash: " + formatted);
        System.out.println("Is valid: " + isValidBlockHash(hash1));
        
        // Test Part 2: Chain validation
        BlockchainValidator validator = new BlockchainValidator();
        validator.addBlock(new BlockHeader(
            "a1b2c3d4e5f6000000000000000000000000000000000000000000000000000",
            "0",
            1000,
            1
        ));
        validator.addBlock(new BlockHeader(
            "b2c3d4e5f6a1000000000000000000000000000000000000000000000000000",
            "a1b2c3d4e5f6000000000000000000000000000000000000000000000000000",
            2000,
            2
        ));
        
        int chainLength = validator.getLongestChainLength();
        System.out.println("Longest chain length: " + chainLength);
    }
}
