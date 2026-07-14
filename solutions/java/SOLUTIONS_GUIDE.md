# Java Solutions Guide - Karat Interview Problems

## Category 1: Log State Machines & Sequential Event Tracking

These problems focus on **parsing sequential event logs**, **tracking state transitions**, and **detecting anomalies** in chronological data.

### Common Patterns

#### 1. **Data Parsing & Normalization (Part 1)**
- **Issue**: Raw input has formatting issues (whitespace, case mismatches, precision loss)
- **Solution**: Clean data before processing
- **Key Techniques**:
  - String trimming and case normalization
  - Safe numeric parsing (handle floats, avoid truncation)
  - Validation guards for null/empty checks

```java
// Example: Safe floating-point parsing
public static double parseTimestampSafely(String str) {
    if (str == null || str.isEmpty()) throw new IllegalArgumentException();
    return Double.parseDouble(str);
}
```

#### 2. **State Machine Implementation (Part 2)**
- **Goal**: Track object states through event sequences
- **Typical States**: ENTER/EXIT, OPENED/CLOSED, ACCEPT/REJECT
- **Algorithm**:
  1. Group events by entity (user, order, device)
  2. Sort chronologically by timestamp
  3. Transition through states
  4. Detect anomalies (invalid transitions)

```java
Map<String, List<Event>> entityEvents = new HashMap<>();
for (Event e : events) {
    entityEvents.putIfAbsent(e.entityId, new ArrayList<>());
    entityEvents.get(e.entityId).add(e);
}
// Sort and process each entity's timeline
for (List<Event> timeline : entityEvents.values()) {
    timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
    // Process state transitions...
}
```

#### 3. **Sliding Window Detection (Part 3)**
- **Use Case**: Find patterns within time windows (e.g., 3+ actions in 60 minutes)
- **Algorithm**: Two-pointer approach or circular buffer
- **Time Complexity**: O(n log n) with sorting

```java
int WINDOW_SIZE = 60 * 60; // seconds
for (int i = 0; i < events.size(); i++) {
    long windowStart = events.get(i).timestamp - WINDOW_SIZE;
    long windowEnd = events.get(i).timestamp;
    // Count events within [windowStart, windowEnd]
}
```

---

## Problem Solutions Summary

### Problem 1: Key-Card Badge Access System
**Core Concepts**: String normalization, state tracking, sliding window fraud detection

| Part | Algorithm | Time | Space | Key Data Structure |
|------|-----------|------|-------|--------------------|
| 1 | String cleaning | O(n) | O(n) | List<String> |
| 2 | State machine | O(n log n) | O(n) | Map<User, Map<Room, Count>> |
| 3 | Sliding window | O(n log n) | O(n) | List + Set for dedup |

**Edge Cases**:
- Multiple exits without entry (reset counter)
- Same user never exits room (track at end)
- Multiple badge IDs (same user, fraud detection)

### Problem 2: E-Commerce Conversion Funnel
**Core Concepts**: Floating-point precision, sequence matching, pattern detection

| Part | Algorithm | Time | Space | Key Data Structure |
|------|-----------|------|-------|--------------------|
| 1 | Float parsing | O(1) | O(1) | Double |
| 2 | Funnel tracking | O(n log n) | O(n) | Map<User, List<Event>> |
| 3 | Subsequence match | O(n*m) | O(1) | Two pointers |

**Optimization Tips**:
- Part 3: Use single pass (pointer approach) instead of nested loops
- Avoid re-parsing timestamps, do it once during input

### Problem 3: Food Delivery Driver Pipeline
**Core Concepts**: Timezone normalization, state machine, duration calculation

| Part | Algorithm | Time | Space | Key Data Structure |
|------|-----------|------|-------|--------------------|
| 1 | Epoch conversion | O(n) | O(n) | List<Long> |
| 2 | Pipeline analysis | O(n log n) | O(n) | Map<Order, Timeline> |
| 3 | Max wait finding | O(n log n) | O(n) | Driver grouping |

---

## General Best Practices

### 1. **Input Validation**
```java
if (input == null || input.isEmpty()) {
    return new ArrayList<>(); // or throw exception
}
```

### 2. **Chronological Processing**
```java
events.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
```

### 3. **Avoiding Duplicate Work**
```java
// Group first, then process
Map<String, List<Event>> grouped = new HashMap<>();
for (Event e : events) {
    grouped.putIfAbsent(e.id, new ArrayList<>());
    grouped.get(e.id).add(e);
}
```

### 4. **Detecting Incomplete Sequences**
```java
boolean completed = (state1 != -1 && state2 != -1 && state3 != -1);
if (!completed) {
    anomalies.add(entityId);
}
```

---

## Testing Checklist

- [ ] Empty input (null, empty list)
- [ ] Single element
- [ ] Duplicate events
- [ ] Out-of-order timestamps
- [ ] Missing intermediate states
- [ ] Boundary values (exactly at threshold)
- [ ] Large datasets (performance)

---

## Running Tests

```bash
# Compile
javac solutions/java/category1_state_machines/Problem1_KeyCardBadgeAccessSystem.java

# Run
java -cp solutions/java category1_state_machines.Problem1_KeyCardBadgeAccessSystem
```

---

## Related Problems
- Problem 4: Cloud Server Telemetry
- Problem 51: Inventory Stock Tracking
- Problem 54: Online Auction Sniping
