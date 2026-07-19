package category7_advanced_systems;

import java.util.*;

/**
 * Problem 62: Telecommunications Cellular Tower Handover Log
 * 
 * Three-part progression:
 * - Part 1: Fix division by zero in signal calculations
 * - Part 2: Track phone session state transitions
 * - Part 3: Map dead zones from GPS and signal data
 */
public class Problem62_TelecommunicationsCellularTowerHandoverLog {
    
    enum ConnectionState {
        TOWER_A_CONNECT, SIGNAL_DROP, TOWER_B_HANDOVER, DISCONNECTED
    }
    
    static class SignalReading {
        long timestamp;
        String towerId;
        double signalStrength;  // dBm
        double latitude;
        double longitude;
        double velocity;  // m/s
        
        SignalReading(long timestamp, String towerId, double strength,
                     double lat, double lon, double vel) {
            this.timestamp = timestamp;
            this.towerId = towerId;
            this.signalStrength = strength;
            this.latitude = lat;
            this.longitude = lon;
            this.velocity = vel;
        }
    }
    
    /**
     * Part 1: Bug Fix - Safe Division by Zero
     * Guard against zero signal conditions
     */
    public static double calculateSignalQuality(double signalStrength) {
        // Guard against division by zero
        if (signalStrength == 0.0) {
            return 0.0;
        }
        
        // Signal quality: -30 dBm (excellent) to -120 dBm (poor)
        // Normalize to 0-100 scale
        double quality = Math.max(0, Math.min(100, (signalStrength + 120) / 0.9));
        return quality;
    }
    
    /**
     * Part 2: Track Call State Transitions
     * Detect calls that dropped during handover
     */
    public static class CallTracker {
        Map<String, List<SignalReading>> callLogs = new HashMap<>();
        Map<String, ConnectionState> currentStates = new HashMap<>();
        
        public void logReading(String callId, SignalReading reading) {
            callLogs.computeIfAbsent(callId, k -> new ArrayList<>()).add(reading);
            
            // Update state based on signal strength
            if (reading.signalStrength > -100) {
                currentStates.put(callId, ConnectionState.TOWER_A_CONNECT);
            } else if (reading.signalStrength < -110) {
                currentStates.put(callId, ConnectionState.SIGNAL_DROP);
            }
        }
        
        public List<String> findDroppedCalls() {
            List<String> dropped = new ArrayList<>();
            
            for (Map.Entry<String, List<SignalReading>> entry : callLogs.entrySet()) {
                String callId = entry.getKey();
                List<SignalReading> readings = entry.getValue();
                readings.sort(Comparator.comparingLong(r -> r.timestamp));
                
                // Check for unhandled signal drops
                boolean hadDrop = false;
                boolean recovered = false;
                
                for (SignalReading reading : readings) {
                    if (reading.signalStrength < -110) {
                        hadDrop = true;
                    }
                    if (hadDrop && reading.signalStrength > -100) {
                        recovered = true;
                    }
                }
                
                if (hadDrop && !recovered) {
                    dropped.add(callId);
                }
            }
            
            return dropped;
        }
    }
    
    /**
     * Part 3: Dead Zone Trajectory Mapper
     * Identify clusters of signal drops by location
     */
    public static Map<String, List<double[]>> mapDeadZones(List<SignalReading> readings) {
        Map<String, List<double[]>> deadZones = new HashMap<>();
        
        for (SignalReading reading : readings) {
            if (reading.signalStrength < -110) {
                String gridCell = gridifyCoordinates(reading.latitude, reading.longitude);
                deadZones.computeIfAbsent(gridCell, k -> new ArrayList<>())
                         .add(new double[]{reading.latitude, reading.longitude});
            }
        }
        
        return deadZones;
    }
    
    private static String gridifyCoordinates(double lat, double lon) {
        int gridLat = (int) (lat * 100);
        int gridLon = (int) (lon * 100);
        return gridLat + "," + gridLon;
    }
    
    /**
     * Calculate dead zone density
     */
    public static List<String> identifyHighDensityDeadZones(Map<String, List<double[]>> deadZones,
                                                            int minDensity) {
        List<String> highDensity = new ArrayList<>();
        
        for (Map.Entry<String, List<double[]>> entry : deadZones.entrySet()) {
            if (entry.getValue().size() >= minDensity) {
                highDensity.add("Grid " + entry.getKey() + ": " + entry.getValue().size() + " drops");
            }
        }
        
        return highDensity;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 62: Cellular Tower Handover ===");
        
        // Test Part 1: Signal quality
        System.out.println("Signal -50 dBm quality: " + calculateSignalQuality(-50));
        System.out.println("Signal -100 dBm quality: " + calculateSignalQuality(-100));
        System.out.println("Signal 0 dBm (zero) quality: " + calculateSignalQuality(0));
        
        // Test Part 2: Call tracking
        CallTracker tracker = new CallTracker();
        tracker.logReading("call1", new SignalReading(1000, "tower1", -50, 40.7128, -74.0060, 0));
        tracker.logReading("call1", new SignalReading(2000, "tower1", -115, 40.7128, -74.0060, 0));
        
        List<String> dropped = tracker.findDroppedCalls();
        System.out.println("Dropped calls: " + dropped);
        
        // Test Part 3: Dead zones
        List<SignalReading> readings = Arrays.asList(
            new SignalReading(1000, "tower1", -120, 40.71, -74.00, 0),
            new SignalReading(1100, "tower1", -115, 40.71, -74.00, 0),
            new SignalReading(1200, "tower1", -118, 40.71, -74.00, 0)
        );
        
        Map<String, List<double[]>> zones = mapDeadZones(readings);
        System.out.println("Dead zones identified: " + zones.size());
    }
}
