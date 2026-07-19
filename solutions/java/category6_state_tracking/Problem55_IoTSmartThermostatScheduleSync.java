package category6_state_tracking;

import java.util.*;

/**
 * Problem 55: IoT Smart Thermostat Schedule Sync
 * 
 * Three-part progression:
 * - Part 1: Verify temperature scale before applying offsets
 * - Part 2: Consolidate overlapping temperature overrides
 * - Part 3: Detect thermal runaway conditions
 */
public class Problem55_IoTSmartThermostatScheduleSync {
    
    enum TemperatureScale {
        FAHRENHEIT, CELSIUS
    }
    
    static class TemperatureOverride {
        long startTime;
        long endTime;
        double temperature;
        TemperatureScale scale;
        
        TemperatureOverride(long start, long end, double temp, TemperatureScale scale) {
            this.startTime = start;
            this.endTime = end;
            this.temperature = temp;
            this.scale = scale;
        }
    }
    
    /**
     * Part 1: Bug Fix - Verify Temperature Scale
     * Don't apply hardcoded offsets without checking scale flag
     */
    public static double convertToFahrenheit(double temp, TemperatureScale scale) {
        if (scale == TemperatureScale.FAHRENHEIT) {
            return temp;
        }
        // Celsius to Fahrenheit: F = C * 9/5 + 32
        return (temp * 9.0 / 5.0) + 32.0;
    }
    
    /**
     * Part 2: Consolidate Overlapping Temperature Overrides
     */
    public static List<TemperatureOverride> consolidateOverrides(
            List<TemperatureOverride> overrides) {
        if (overrides.isEmpty()) return new ArrayList<>();
        
        // Sort by start time
        overrides.sort(Comparator.comparingLong(o -> o.startTime));
        
        List<TemperatureOverride> consolidated = new ArrayList<>();
        TemperatureOverride current = overrides.get(0);
        
        for (int i = 1; i < overrides.size(); i++) {
            TemperatureOverride next = overrides.get(i);
            
            if (next.startTime <= current.endTime) {
                // Overlapping - merge
                current = new TemperatureOverride(
                    current.startTime,
                    Math.max(current.endTime, next.endTime),
                    (current.temperature + next.temperature) / 2.0,
                    current.scale
                );
            } else {
                // Gap - save current and start new
                consolidated.add(current);
                current = next;
            }
        }
        consolidated.add(current);
        
        return consolidated;
    }
    
    /**
     * Part 3: Detect Thermal Runaway
     * Heating element runs continuously while room temp doesn't increase
     */
    public static class ThermalRunawayDetector {
        static class SensorReading {
            long timestamp;
            double roomTemperature;
            boolean heatingActive;
            
            SensorReading(long timestamp, double roomTemp, boolean heating) {
                this.timestamp = timestamp;
                this.roomTemperature = roomTemp;
                this.heatingActive = heating;
            }
        }
        
        public static boolean detectThermalRunaway(List<SensorReading> readings,
                                                   long continuousDurationMinutes,
                                                   double tempIncreaseThreshold) {
            long continuousDurationMs = continuousDurationMinutes * 60 * 1000;
            
            for (int i = 0; i < readings.size() - 1; i++) {
                SensorReading start = readings.get(i);
                if (!start.heatingActive) continue;
                
                long startTime = start.timestamp;
                long endTime = startTime + continuousDurationMs;
                
                boolean continuousHeating = true;
                double tempChange = 0;
                
                for (int j = i + 1; j < readings.size(); j++) {
                    SensorReading current = readings.get(j);
                    
                    if (current.timestamp > endTime) break;
                    
                    if (!current.heatingActive) {
                        continuousHeating = false;
                        break;
                    }
                    
                    tempChange = current.roomTemperature - start.roomTemperature;
                }
                
                // Thermal runaway: heating active but temp unchanged
                if (continuousHeating && tempChange < tempIncreaseThreshold) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    /**
     * Generate optimized schedule
     */
    public static List<TemperatureOverride> generateOptimalSchedule(
            List<TemperatureOverride> userOverrides) {
        List<TemperatureOverride> consolidated = consolidateOverrides(userOverrides);
        
        // Apply any additional optimization logic here
        return consolidated;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 55: IoT Smart Thermostat Schedule Sync ===");
        
        // Test Part 1: Temperature conversion
        double fahrenheit = convertToFahrenheit(25, TemperatureScale.CELSIUS);
        System.out.println("25°C = " + fahrenheit + "°F");
        
        // Test Part 2: Consolidate overrides
        List<TemperatureOverride> overrides = Arrays.asList(
            new TemperatureOverride(1000, 2000, 72, TemperatureScale.FAHRENHEIT),
            new TemperatureOverride(1500, 2500, 74, TemperatureScale.FAHRENHEIT),
            new TemperatureOverride(3000, 4000, 70, TemperatureScale.FAHRENHEIT)
        );
        
        List<TemperatureOverride> consolidated = consolidateOverrides(overrides);
        System.out.println("Consolidated overrides: " + consolidated.size());
        
        // Test Part 3: Thermal runaway detection
        List<ThermalRunawayDetector.SensorReading> readings = Arrays.asList(
            new ThermalRunawayDetector.SensorReading(1000, 68.0, true),
            new ThermalRunawayDetector.SensorReading(2000, 68.2, true),
            new ThermalRunawayDetector.SensorReading(3000, 68.1, true)
        );
        
        boolean runaway = ThermalRunawayDetector.detectThermalRunaway(readings, 45, 0.5);
        System.out.println("Thermal runaway detected: " + runaway);
    }
}
