package category6_state_tracking;

import java.util.*;

/**
 * Problem 60: Logistics Fleet Maintenance Predictor
 * 
 * Three-part progression:
 * - Part 1: Handle missing diagnostic codes gracefully
 * - Part 2: Track vehicle states (ACTIVE, WARNING_LIGHT, REPAIR_SHOP, DECOMMISSIONED)
 * - Part 3: Predict component failures from sensor patterns
 */
public class Problem60_LogisticsFleetMaintenancePredictor {
    
    enum VehicleState {
        ACTIVE, WARNING_LIGHT, REPAIR_SHOP, DECOMMISSIONED
    }
    
    static class TelemetryReading {
        String vehicleId;
        long timestamp;
        String diagnosticCode;
        double engineTemp;
        double oilPressure;
        int mileage;
        
        TelemetryReading(String vehicleId, long timestamp, String diagnosticCode,
                        double engineTemp, double oilPressure, int mileage) {
            this.vehicleId = vehicleId;
            this.timestamp = timestamp;
            this.diagnosticCode = diagnosticCode;
            this.engineTemp = engineTemp;
            this.oilPressure = oilPressure;
            this.mileage = mileage;
        }
    }
    
    /**
     * Part 1: Bug Fix - Handle Missing Diagnostic Codes
     * Treat blank/missing codes as zero, not crash
     */
    public static int parseDiagnosticCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return 0;  // Default: no fault
        }
        
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Part 2: Track Vehicle Lifecycle States
     */
    public static class FleetTracker {
        Map<String, VehicleState> vehicleStates = new HashMap<>();
        Map<String, List<TelemetryReading>> telemetryLogs = new HashMap<>();
        
        public void recordTelemetry(TelemetryReading reading) {
            telemetryLogs.computeIfAbsent(reading.vehicleId, k -> new ArrayList<>())
                         .add(reading);
            
            // Update vehicle state based on diagnostics
            int code = parseDiagnosticCode(reading.diagnosticCode);
            if (code > 0) {
                vehicleStates.put(reading.vehicleId, VehicleState.WARNING_LIGHT);
            } else if (vehicleStates.getOrDefault(reading.vehicleId, VehicleState.ACTIVE) 
                      == VehicleState.ACTIVE) {
                vehicleStates.put(reading.vehicleId, VehicleState.ACTIVE);
            }
        }
        
        public List<String> findMissingRepairShop() {
            List<String> violations = new ArrayList<>();
            
            for (Map.Entry<String, List<TelemetryReading>> entry : telemetryLogs.entrySet()) {
                String vehicleId = entry.getKey();
                List<TelemetryReading> logs = entry.getValue();
                logs.sort(Comparator.comparingLong(t -> t.timestamp));
                
                boolean hasWarning = false;
                boolean visitedRepairShop = false;
                
                for (TelemetryReading reading : logs) {
                    if (parseDiagnosticCode(reading.diagnosticCode) > 0) {
                        hasWarning = true;
                    }
                    
                    VehicleState state = vehicleStates.get(vehicleId);
                    if (state == VehicleState.REPAIR_SHOP) {
                        visitedRepairShop = true;
                    }
                    
                    if (state == VehicleState.DECOMMISSIONED && hasWarning && !visitedRepairShop) {
                        violations.add(vehicleId + ": decommissioned without repair");
                    }
                }
            }
            
            return violations;
        }
    }
    
    /**
     * Part 3: Predict Failures from Sensor Correlation
     */
    public static List<String> predictFailures(Map<String, List<TelemetryReading>> logs) {
        List<String> predictions = new ArrayList<>();
        
        for (Map.Entry<String, List<TelemetryReading>> entry : logs.entrySet()) {
            String vehicleId = entry.getKey();
            List<TelemetryReading> readings = entry.getValue();
            readings.sort(Comparator.comparingLong(r -> r.timestamp));
            
            if (readings.size() < 3) continue;
            
            // Check for pattern: high temp + low pressure + warning codes
            int warningCount = 0;
            double avgTemp = 0;
            double avgPressure = 0;
            
            for (TelemetryReading reading : readings) {
                if (parseDiagnosticCode(reading.diagnosticCode) > 0) {
                    warningCount++;
                }
                avgTemp += reading.engineTemp;
                avgPressure += reading.oilPressure;
            }
            
            avgTemp /= readings.size();
            avgPressure /= readings.size();
            
            // Predict failure if 3+ related alerts
            if (warningCount >= 3 && avgTemp > 100 && avgPressure < 50) {
                predictions.add(vehicleId + ": HIGH FAILURE RISK");
            }
        }
        
        return predictions;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 60: Logistics Fleet Maintenance ===");
        
        // Test Part 1: Parse diagnostic codes
        System.out.println("Parse 'P0101': " + parseDiagnosticCode("P0101"));
        System.out.println("Parse '': " + parseDiagnosticCode(""));
        System.out.println("Parse null: " + parseDiagnosticCode(null));
        
        // Test Part 2: Fleet tracking
        FleetTracker tracker = new FleetTracker();
        tracker.recordTelemetry(new TelemetryReading("V001", 1000, "P0101", 105, 45, 50000));
        tracker.recordTelemetry(new TelemetryReading("V001", 2000, "P0102", 110, 40, 50100));
        tracker.recordTelemetry(new TelemetryReading("V001", 3000, "P0103", 115, 35, 50200));
        System.out.println("Vehicle states recorded");
        
        // Test Part 3: Predict failures
        Map<String, List<TelemetryReading>> logs = new HashMap<>();
        logs.put("V001", Arrays.asList(
            new TelemetryReading("V001", 1000, "P0101", 105, 45, 50000),
            new TelemetryReading("V001", 2000, "P0102", 110, 40, 50100),
            new TelemetryReading("V001", 3000, "P0103", 115, 35, 50200)
        ));
        
        List<String> predictions = predictFailures(logs);
        System.out.println("Failure predictions: " + predictions);
    }
}
