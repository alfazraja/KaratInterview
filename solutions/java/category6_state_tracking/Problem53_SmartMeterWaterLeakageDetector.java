package category6_state_tracking;

import java.util.*;

/**
 * Problem 53: Smart Meter Water Leakage Detector
 * 
 * Three-part progression:
 * - Part 1: Use decimals instead of integer division
 * - Part 2: Sliding window to detect continuous flow
 * - Part 3: District mass balance analysis
 */
public class Problem53_SmartMeterWaterLeakageDetector {
    
    static class MeterReading {
        long timestamp;
        double flowRate;  // gallons per minute
        
        MeterReading(long timestamp, double flowRate) {
            this.timestamp = timestamp;
            this.flowRate = flowRate;
        }
    }
    
    /**
     * Part 1: Bug Fix - Use Floating Point Division
     * Calculate precise flow rates without integer truncation
     */
    public static double calculateAverageFlow(List<MeterReading> readings) {
        if (readings.isEmpty()) return 0.0;
        
        double totalFlow = 0.0;
        for (MeterReading reading : readings) {
            totalFlow += reading.flowRate;
        }
        
        return totalFlow / (double) readings.size();  // Correct: float division
    }
    
    /**
     * Part 2: Sliding Window - Detect Never-Zero Flow
     * Find households where water never stops flowing
     */
    public static List<String> findHouseholdsWithContinuousFlow(Map<String, List<MeterReading>> households,
                                                                 int windowSizeHours) {
        List<String> suspects = new ArrayList<>();
        
        for (Map.Entry<String, List<MeterReading>> entry : households.entrySet()) {
            String householdId = entry.getKey();
            List<MeterReading> readings = entry.getValue();
            
            if (readings.size() < 2) continue;
            
            // Sort by timestamp
            readings.sort(Comparator.comparingLong(r -> r.timestamp));
            
            // Check sliding window
            for (int i = 0; i < readings.size() - 1; i++) {
                long windowStart = readings.get(i).timestamp;
                long windowEnd = windowStart + (windowSizeHours * 3600);
                
                boolean continuousFlow = true;
                for (int j = i; j < readings.size(); j++) {
                    MeterReading reading = readings.get(j);
                    if (reading.timestamp > windowEnd) break;
                    if (reading.flowRate == 0.0) {
                        continuousFlow = false;
                        break;
                    }
                }
                
                if (continuousFlow) {
                    suspects.add(householdId);
                    break;
                }
            }
        }
        
        return suspects;
    }
    
    /**
     * Part 3: District Mass Balance
     * Compare main pipe vs sum of consumer endpoints
     */
    public static double calculateDistrictLeak(double mainPipeInflow,
                                               List<Double> consumerOutflows) {
        double totalConsumption = 0.0;
        for (double outflow : consumerOutflows) {
            totalConsumption += outflow;
        }
        
        double leakage = mainPipeInflow - totalConsumption;
        return Math.max(0.0, leakage);  // Leakage is non-negative
    }
    
    /**
     * Calculate leakage percentage
     */
    public static double calculateLeakagePercentage(double mainPipeInflow,
                                                     List<Double> consumerOutflows) {
        if (mainPipeInflow == 0.0) return 0.0;
        
        double leakage = calculateDistrictLeak(mainPipeInflow, consumerOutflows);
        return (leakage / mainPipeInflow) * 100.0;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 53: Smart Meter Water Leakage ===");
        
        List<MeterReading> readings = Arrays.asList(
            new MeterReading(1000, 1.5),
            new MeterReading(2000, 2.0),
            new MeterReading(3000, 1.8)
        );
        
        double avgFlow = calculateAverageFlow(readings);
        System.out.println("Average flow rate: " + avgFlow + " gpm");
        
        double leakage = calculateDistrictLeak(1000.0, Arrays.asList(800.0, 150.0));
        System.out.println("District leakage: " + leakage + " gallons");
        
        double leakagePercent = calculateLeakagePercentage(1000.0, Arrays.asList(800.0, 150.0));
        System.out.println("Leakage percentage: " + leakagePercent + "%");
    }
}
