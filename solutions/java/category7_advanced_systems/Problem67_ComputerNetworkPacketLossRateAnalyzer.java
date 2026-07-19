package category7_advanced_systems;

import java.util.*;

/**
 * Problem 67: Computer Network Packet Loss Rate Analyzer
 * 
 * Three-part progression:
 * - Part 1: Fix bitwise shift overflow for large packet counts
 * - Part 2: Calculate packet loss percentage across routes
 * - Part 3: Identify faulty links using loss correlation
 */
public class Problem67_ComputerNetworkPacketLossRateAnalyzer {
    
    static class PacketTrace {
        String routeId;
        long packetsSent;
        long packetsReceived;
        long timestamp;
        
        PacketTrace(String route, long sent, long received, long timestamp) {
            this.routeId = route;
            this.packetsSent = sent;
            this.packetsReceived = received;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Part 1: Bug Fix - Safe Bitwise Operations
     * Use long instead of int to prevent overflow with large packet counts
     */
    public static long calculatePacketsLost(long packetsSent, long packetsReceived) {
        // Use long arithmetic to prevent overflow
        return Math.max(0L, packetsSent - packetsReceived);
    }
    
    public static double calculateLossRate(long packetsSent, long packetsReceived) {
        if (packetsSent == 0) return 0.0;
        
        long lost = calculatePacketsLost(packetsSent, packetsReceived);
        return (double) lost / (double) packetsSent * 100.0;
    }
    
    /**
     * Part 2: Calculate Loss Percentage Per Route
     */
    public static class NetworkAnalyzer {
        List<PacketTrace> traces = new ArrayList<>();
        Map<String, List<PacketTrace>> tracesByRoute = new HashMap<>();
        
        public void recordTrace(PacketTrace trace) {
            traces.add(trace);
            tracesByRoute.computeIfAbsent(trace.routeId, k -> new ArrayList<>())
                        .add(trace);
        }
        
        public Map<String, Double> calculatePerRouteLopeRate() {
            Map<String, Double> lossRates = new HashMap<>();
            
            for (Map.Entry<String, List<PacketTrace>> entry : tracesByRoute.entrySet()) {
                String routeId = entry.getKey();
                List<PacketTrace> routeTraces = entry.getValue();
                
                long totalSent = 0;
                long totalReceived = 0;
                
                for (PacketTrace trace : routeTraces) {
                    totalSent += trace.packetsSent;
                    totalReceived += trace.packetsReceived;
                }
                
                double lossRate = calculateLossRate(totalSent, totalReceived);
                lossRates.put(routeId, lossRate);
            }
            
            return lossRates;
        }
        
        public List<String> getRoutesAboveThreshold(double thresholdPercent) {
            List<String> problematicRoutes = new ArrayList<>();
            
            for (Map.Entry<String, Double> entry : calculatePerRouteLopeRate().entrySet()) {
                if (entry.getValue() > thresholdPercent) {
                    problematicRoutes.add(entry.getKey() + ": " + 
                                        String.format("%.2f%%", entry.getValue()));
                }
            }
            
            return problematicRoutes;
        }
    }
    
    /**
     * Part 3: Correlation Analysis for Link Failure Detection
     */
    public static class LinkFaultDetector {
        Map<String, List<Double>> routeLossHistory = new HashMap<>();
        
        public void recordLossMetric(String routeId, double lossPercentage) {
            routeLossHistory.computeIfAbsent(routeId, k -> new ArrayList<>())
                           .add(lossPercentage);
        }
        
        public List<String> identifyFaultyLink(double correlationThreshold) {
            List<String> faultyLinks = new ArrayList<>();
            
            // Find routes with consistently high loss
            for (Map.Entry<String, List<Double>> entry : routeLossHistory.entrySet()) {
                String routeId = entry.getKey();
                List<Double> losses = entry.getValue();
                
                if (losses.size() < 3) continue;
                
                // Calculate average and variance
                double sum = 0.0;
                for (double loss : losses) {
                    sum += loss;
                }
                double avg = sum / losses.size();
                
                // High average loss indicates faulty link
                if (avg > correlationThreshold) {
                    faultyLinks.add(routeId + " (avg loss: " + 
                                  String.format("%.2f%%)", avg));
                }
            }
            
            return faultyLinks;
        }
        
        public double calculateCorrelation(String route1, String route2) {
            List<Double> losses1 = routeLossHistory.get(route1);
            List<Double> losses2 = routeLossHistory.get(route2);
            
            if (losses1 == null || losses2 == null || losses1.size() != losses2.size()) {
                return 0.0;
            }
            
            // Simple correlation calculation
            double mean1 = losses1.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double mean2 = losses2.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            
            double covariance = 0.0;
            double var1 = 0.0, var2 = 0.0;
            
            for (int i = 0; i < losses1.size(); i++) {
                covariance += (losses1.get(i) - mean1) * (losses2.get(i) - mean2);
                var1 += Math.pow(losses1.get(i) - mean1, 2);
                var2 += Math.pow(losses2.get(i) - mean2, 2);
            }
            
            if (var1 == 0 || var2 == 0) return 0.0;
            return covariance / Math.sqrt(var1 * var2);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 67: Network Packet Loss Analyzer ===");
        
        // Test Part 1: Safe calculations
        long sent = 1_000_000_000L;
        long received = 999_000_000L;
        
        long lost = calculatePacketsLost(sent, received);
        double lossRate = calculateLossRate(sent, received);
        
        System.out.println("Packets sent: " + sent);
        System.out.println("Packets lost: " + lost);
        System.out.println("Loss rate: " + String.format("%.4f%%", lossRate));
        
        // Test Part 2: Route analysis
        NetworkAnalyzer analyzer = new NetworkAnalyzer();
        analyzer.recordTrace(new PacketTrace("route1", 10000, 9900, System.currentTimeMillis()));
        analyzer.recordTrace(new PacketTrace("route1", 10000, 9800, System.currentTimeMillis()));
        analyzer.recordTrace(new PacketTrace("route2", 10000, 9950, System.currentTimeMillis()));
        
        Map<String, Double> losses = analyzer.calculatePerRouteLopeRate();
        System.out.println("\nPer-route loss rates: " + losses);
        
        List<String> problematic = analyzer.getRoutesAboveThreshold(1.5);
        System.out.println("Routes above 1.5% threshold: " + problematic);
        
        // Test Part 3: Link fault detection
        LinkFaultDetector detector = new LinkFaultDetector();
        detector.recordLossMetric("route1", 1.2);
        detector.recordLossMetric("route1", 1.5);
        detector.recordLossMetric("route1", 1.8);
        detector.recordLossMetric("route2", 0.1);
        
        List<String> faulty = detector.identifyFaultyLink(1.0);
        System.out.println("Faulty links: " + faulty);
    }
}
