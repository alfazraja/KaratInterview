package category7_advanced_systems;

import java.util.*;

/**
 * Problem 70: Distributed System Event Log Aggregator
 * 
 * Three-part progression:
 * - Part 1: Handle timestamp skew across distributed nodes
 * - Part 2: Merge event streams maintaining causal order
 * - Part 3: Detect anomalies from aggregated patterns
 */
public class Problem70_DistributedSystemEventLogAggregator {
    
    static class LogEvent {
        String nodeId;
        long localTimestamp;
        long systemTimestamp;  // Synced timestamp
        String eventType;
        String payload;
        
        LogEvent(String nodeId, long localTs, String eventType, String payload) {
            this.nodeId = nodeId;
            this.localTimestamp = localTs;
            this.eventType = eventType;
            this.payload = payload;
        }
    }
    
    /**
     * Part 1: Bug Fix - Handle Timestamp Skew
     * Synchronize clocks across distributed nodes
     */
    public static class ClockSynchronizer {
        Map<String, Long> nodeOffsets = new HashMap<>();
        
        public void calibrate(String nodeId, long localTime, long referenceTime) {
            long offset = referenceTime - localTime;
            nodeOffsets.put(nodeId, offset);
        }
        
        public long synchronizeTimestamp(String nodeId, long localTimestamp) {
            Long offset = nodeOffsets.getOrDefault(nodeId, 0L);
            return localTimestamp + offset;
        }
        
        public void recalibrateAll(Map<String, Long> latestLocalTimes, long referenceTime) {
            for (Map.Entry<String, Long> entry : latestLocalTimes.entrySet()) {
                calibrate(entry.getKey(), entry.getValue(), referenceTime);
            }
        }
    }
    
    /**
     * Part 2: Merge and Order Events
     * Maintain causal consistency
     */
    public static class EventAggregator {
        PriorityQueue<LogEvent> eventQueue;
        List<LogEvent> sortedEvents = new ArrayList<>();
        ClockSynchronizer synchronizer = new ClockSynchronizer();
        
        public EventAggregator() {
            this.eventQueue = new PriorityQueue<>((a, b) -> 
                Long.compare(a.systemTimestamp, b.systemTimestamp));
        }
        
        public void addEvent(LogEvent event) {
            // Synchronize timestamp
            event.systemTimestamp = synchronizer.synchronizeTimestamp(
                event.nodeId, event.localTimestamp);
            eventQueue.offer(event);
        }
        
        public List<LogEvent> getOrderedEvents() {
            sortedEvents.clear();
            while (!eventQueue.isEmpty()) {
                sortedEvents.add(eventQueue.poll());
            }
            return new ArrayList<>(sortedEvents);
        }
        
        public List<LogEvent> getEventsByNode(String nodeId) {
            List<LogEvent> nodeEvents = new ArrayList<>();
            for (LogEvent event : sortedEvents) {
                if (event.nodeId.equals(nodeId)) {
                    nodeEvents.add(event);
                }
            }
            return nodeEvents;
        }
    }
    
    /**
     * Part 3: Anomaly Detection
     */
    public static class AnomalyDetector {
        
        public static List<String> detectEventStorms(List<LogEvent> events, 
                                                     int eventsPerWindowThreshold,
                                                     long windowMs) {
            List<String> anomalies = new ArrayList<>();
            
            for (int i = 0; i < events.size(); i++) {
                LogEvent startEvent = events.get(i);
                int eventCount = 1;
                
                for (int j = i + 1; j < events.size(); j++) {
                    LogEvent currentEvent = events.get(j);
                    
                    if (currentEvent.systemTimestamp - startEvent.systemTimestamp <= windowMs) {
                        eventCount++;
                    } else {
                        break;
                    }
                }
                
                if (eventCount > eventsPerWindowThreshold) {
                    anomalies.add("Event storm detected: " + eventCount + 
                                " events in " + windowMs + "ms window");
                    i += eventCount - 1;  // Skip processed events
                }
            }
            
            return anomalies;
        }
        
        public static List<String> detectMissingNodes(List<LogEvent> events,
                                                      Set<String> expectedNodes,
                                                      long maxSilenceMs) {
            List<String> anomalies = new ArrayList<>();
            Map<String, Long> lastEventTime = new HashMap<>();
            
            for (LogEvent event : events) {
                lastEventTime.put(event.nodeId, event.systemTimestamp);
            }
            
            long currentTime = events.isEmpty() ? 0 : 
                             events.get(events.size() - 1).systemTimestamp;
            
            for (String nodeId : expectedNodes) {
                Long lastTime = lastEventTime.get(nodeId);
                if (lastTime == null || (currentTime - lastTime) > maxSilenceMs) {
                    anomalies.add("Node " + nodeId + " silent for > " + maxSilenceMs + "ms");
                }
            }
            
            return anomalies;
        }
        
        public static Map<String, Long> calculateEventRates(List<LogEvent> events) {
            Map<String, Long> eventCounts = new HashMap<>();
            
            for (LogEvent event : events) {
                eventCounts.put(event.nodeId, 
                              eventCounts.getOrDefault(event.nodeId, 0L) + 1);
            }
            
            return eventCounts;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 70: Distributed Event Log Aggregator ===");
        
        // Test Part 1: Clock synchronization
        ClockSynchronizer sync = new ClockSynchronizer();
        sync.calibrate("node1", 1000, 1050);  // node1 is 50ms behind
        sync.calibrate("node2", 1000, 1020);  // node2 is 20ms behind
        
        long syncedTime1 = sync.synchronizeTimestamp("node1", 2000);
        System.out.println("Node1 local 2000 -> synced: " + syncedTime1);
        
        // Test Part 2: Event aggregation
        EventAggregator aggregator = new EventAggregator();
        aggregator.synchronizer = sync;
        
        aggregator.addEvent(new LogEvent("node1", 1000, "START", "app started"));
        aggregator.addEvent(new LogEvent("node2", 1010, "CONNECT", "connection established"));
        aggregator.addEvent(new LogEvent("node1", 1100, "REQUEST", "processing request"));
        
        List<LogEvent> ordered = aggregator.getOrderedEvents();
        System.out.println("\nOrdered events: " + ordered.size());
        for (LogEvent evt : ordered) {
            System.out.println("  " + evt.nodeId + ": " + evt.eventType + 
                             " @ " + evt.systemTimestamp);
        }
        
        // Test Part 3: Anomaly detection
        List<String> storms = AnomalyDetector.detectEventStorms(ordered, 10, 1000);
        System.out.println("\nEvent storms: " + storms);
        
        Set<String> expectedNodes = new HashSet<>(Arrays.asList("node1", "node2", "node3"));
        List<String> silent = AnomalyDetector.detectMissingNodes(ordered, expectedNodes, 5000);
        System.out.println("Silent nodes: " + silent);
        
        Map<String, Long> rates = AnomalyDetector.calculateEventRates(ordered);
        System.out.println("Event rates: " + rates);
    }
}
