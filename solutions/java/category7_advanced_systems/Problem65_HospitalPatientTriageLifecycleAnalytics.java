package category7_advanced_systems;

import java.util.*;

/**
 * Problem 65: Hospital Patient Triage Lifecycle Analytics
 * 
 * Three-part progression:
 * - Part 1: Split unstructured vital signs from text notes
 * - Part 2: Track patient movement through care stages
 * - Part 3: Identify resource bottlenecks
 */
public class Problem65_HospitalPatientTriageLifecycleAnalytics {
    
    enum PatientState {
        ADMITTED, TRIAGE, ICU_TRANSFER, DISCHARGED
    }
    
    static class VitalSigns {
        int heartRate;
        double temperature;  // Fahrenheit
        int bloodPressureSystolic;
        int bloodPressureDiastolic;
        int respiratoryRate;
        
        VitalSigns(int hr, double temp, int bpSys, int bpDia, int rr) {
            this.heartRate = hr;
            this.temperature = temp;
            this.bloodPressureSystolic = bpSys;
            this.bloodPressureDiastolic = bpDia;
            this.respiratoryRate = rr;
        }
    }
    
    static class PatientRecord {
        String patientId;
        VitalSigns vitals;
        String notes;
        long timestamp;
        PatientState currentState;
        
        PatientRecord(String id, long timestamp) {
            this.patientId = id;
            this.timestamp = timestamp;
            this.currentState = PatientState.ADMITTED;
        }
    }
    
    /**
     * Part 1: Bug Fix - Parse Structured Data from Unstructured Text
     * Extract vital signs from mixed text/numeric fields
     */
    public static VitalSigns parseVitalSigns(String rawData) {
        // Expected format: "HR:85 Temp:98.6 BP:120/80 RR:16"
        try {
            int hr = 0, bpSys = 0, bpDia = 0, rr = 0;
            double temp = 0.0;
            
            String[] parts = rawData.split("\\s+");
            
            for (String part : parts) {
                if (part.startsWith("HR:")) {
                    hr = Integer.parseInt(part.substring(3));
                } else if (part.startsWith("Temp:")) {
                    temp = Double.parseDouble(part.substring(5));
                } else if (part.startsWith("BP:")) {
                    String[] bp = part.substring(3).split("/");
                    bpSys = Integer.parseInt(bp[0]);
                    bpDia = Integer.parseInt(bp[1]);
                } else if (part.startsWith("RR:")) {
                    rr = Integer.parseInt(part.substring(3));
                }
            }
            
            return new VitalSigns(hr, temp, bpSys, bpDia, rr);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Part 2: Track Patient State Transitions
     */
    public static class TriageTracker {
        Map<String, List<PatientRecord>> patientTimelines = new HashMap<>();
        
        public void recordPatientState(PatientRecord record) {
            patientTimelines.computeIfAbsent(record.patientId, k -> new ArrayList<>())
                           .add(record);
        }
        
        public List<String> findDirectDischarges() {
            List<String> violations = new ArrayList<>();
            
            for (Map.Entry<String, List<PatientRecord>> entry : patientTimelines.entrySet()) {
                String patientId = entry.getKey();
                List<PatientRecord> records = entry.getValue();
                records.sort(Comparator.comparingLong(r -> r.timestamp));
                
                // Check for ADMITTED -> DISCHARGED without TRIAGE
                for (int i = 0; i < records.size() - 1; i++) {
                    PatientRecord current = records.get(i);
                    PatientRecord next = records.get(i + 1);
                    
                    if (current.currentState == PatientState.ADMITTED &&
                        next.currentState == PatientState.DISCHARGED) {
                        violations.add("Patient " + patientId + ": Discharged without triage");
                    }
                }
            }
            
            return violations;
        }
    }
    
    /**
     * Part 3: Identify Resource Bottlenecks
     * Find wait times between stages
     */
    public static class ResourceBottleneckAnalyzer {
        Map<String, List<PatientRecord>> timelines;
        
        public ResourceBottleneckAnalyzer(Map<String, List<PatientRecord>> timelines) {
            this.timelines = timelines;
        }
        
        public long findMaxWaitTime(PatientState fromState, PatientState toState) {
            long maxWait = 0;
            
            for (List<PatientRecord> records : timelines.values()) {
                records.sort(Comparator.comparingLong(r -> r.timestamp));
                
                for (int i = 0; i < records.size() - 1; i++) {
                    PatientRecord current = records.get(i);
                    PatientRecord next = records.get(i + 1);
                    
                    if (current.currentState == fromState && next.currentState == toState) {
                        long waitTime = next.timestamp - current.timestamp;
                        maxWait = Math.max(maxWait, waitTime);
                    }
                }
            }
            
            return maxWait;
        }
        
        public String identifyBottleneck() {
            long admittedToTriageWait = findMaxWaitTime(PatientState.ADMITTED, PatientState.TRIAGE);
            long triageToICUWait = findMaxWaitTime(PatientState.TRIAGE, PatientState.ICU_TRANSFER);
            long icuToDischargWait = findMaxWaitTime(PatientState.ICU_TRANSFER, PatientState.DISCHARGED);
            
            long maxWait = Math.max(admittedToTriageWait, 
                                   Math.max(triageToICUWait, icuToDischargWait));
            
            if (maxWait == admittedToTriageWait) {
                return "ADMITTED->TRIAGE bottleneck: " + maxWait + "ms";
            } else if (maxWait == triageToICUWait) {
                return "TRIAGE->ICU bottleneck: " + maxWait + "ms";
            } else {
                return "ICU->DISCHARGED bottleneck: " + maxWait + "ms";
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 65: Hospital Patient Triage ===");
        
        // Test Part 1: Parse vital signs
        String rawVitals = "HR:85 Temp:98.6 BP:120/80 RR:16";
        VitalSigns vitals = parseVitalSigns(rawVitals);
        if (vitals != null) {
            System.out.println("Parsed vitals - HR: " + vitals.heartRate + 
                             ", Temp: " + vitals.temperature);
        }
        
        // Test Part 2: Track patient states
        TriageTracker tracker = new TriageTracker();
        PatientRecord p1 = new PatientRecord("P001", 1000);
        p1.currentState = PatientState.ADMITTED;
        tracker.recordPatientState(p1);
        
        PatientRecord p2 = new PatientRecord("P001", 2000);
        p2.currentState = PatientState.TRIAGE;
        tracker.recordPatientState(p2);
        
        List<String> violations = tracker.findDirectDischarges();
        System.out.println("Discharge violations: " + violations.size());
        
        // Test Part 3: Bottleneck analysis
        Map<String, List<PatientRecord>> timelines = new HashMap<>();
        ResourceBottleneckAnalyzer analyzer = new ResourceBottleneckAnalyzer(timelines);
        System.out.println("Bottleneck: " + analyzer.identifyBottleneck());
    }
}
