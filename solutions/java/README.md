# Karat Interview Solutions - Java Implementation

This directory contains comprehensive Java implementations for Karat interview problems, organized by category and difficulty.

## Structure

```
solutions/java/
├── category1_state_machines/
│   ├── Problem1_KeyCardBadgeAccessSystem.java
│   ├── Problem2_ECommerceConversionFunnel.java
│   ├── Problem3_FoodDeliveryDriverPipeline.java
│   └── ...
├── category2_interval_math/
│   ├── Problem11_MeetingSchedulerOverlapEngine.java
│   ├── Problem12_SharedCalendarFreebusy.java
│   └── ...
└── category3_grid_traversal/
    ├── Problem21_RectangleIslandBoundaryFinder.java
    ├── Problem22_WordSearchMatrixCrawler.java
    └── ...
```

## Problem Categories

### Category 1: Log State Machines & Sequential Event Tracking
Focuses on parsing logs, tracking state transitions, and detecting anomalies through sequential event analysis.

### Category 2: Interval Math, Timelines, & Concurrency Operations
Concerns merging intervals, finding overlaps, and managing concurrent time windows.

### Category 3: Grid Traversal & 2D Matrix Operations
Involves pathfinding, connected components, and matrix manipulation.

## Implementation Approach

Each problem follows a 3-step progression:

1. **Part 1 (Bug Fix/Basic Parsing)**: Fix data parsing issues and handle edge cases
2. **Part 2 (Core Algorithm)**: Implement the main business logic using appropriate data structures
3. **Part 3 (Advanced Optimization)**: Add optimization techniques and constraint evaluation

## How to Use

```bash
# Compile
javac solutions/java/category1_state_machines/Problem1_KeyCardBadgeAccessSystem.java

# Run
java -cp solutions/java category1_state_machines.Problem1_KeyCardBadgeAccessSystem
```

## Key Concepts Covered

- **Data Structures**: HashMap, HashSet, Queue, Stack, PriorityQueue
- **Algorithms**: BFS, DFS, Topological Sort, Interval Merging
- **Patterns**: State Machines, Sliding Window, Two Pointers
- **Edge Cases**: Null handling, empty collections, boundary conditions

---

**Status**: In Progress - Adding solutions incrementally
