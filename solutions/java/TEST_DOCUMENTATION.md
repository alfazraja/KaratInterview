# Test Suite Documentation

## Running All Tests

### Compile Tests
```bash
javac -cp solutions/java solutions/java/category1_state_machines/Problem*_Tests.java
```

### Run Individual Test Suites
```bash
# Problem 1 Tests
java -cp solutions/java category1_state_machines.Problem1_Tests

# Problem 2 Tests
java -cp solutions/java category1_state_machines.Problem2_Tests

# Problem 3 Tests
java -cp solutions/java category1_state_machines.Problem3_Tests
```

### Run All Tests in Sequence
```bash
#!/bin/bash
echo "=== Running All Test Suites ==="
java -cp solutions/java category1_state_machines.Problem1_Tests
java -cp solutions/java category1_state_machines.Problem2_Tests
java -cp solutions/java category1_state_machines.Problem3_Tests
echo "=== All Tests Complete ==="
```

---

## Test Coverage Summary

### Problem 1: Key-Card Badge Access System
**Total Tests: 14**

#### Part 1: Data Parsing (3 tests)
- ✓ Simple data cleaning with whitespace
- ✓ Empty string handling
- ✓ Mixed case normalization

#### Part 2: State Machine (5 tests)
- ✓ Normal entry/exit sequence
- ✓ Exit without entry detection
- ✓ Entry without exit detection
- ✓ Multiple rooms handling
- ✓ Out-of-order timestamp sorting

#### Part 3: Fraud Detection (6 tests)
- ✓ Single badge (no fraud)
- ✓ Multiple badges within 60-min window
- ✓ Multiple badges outside 60-min window
- ✓ Multiple users (selective fraud)
- ✓ Exactly 3 badges threshold
- ✓ Empty input handling
- ✓ Less than 3 badges (no fraud)

---

### Problem 2: E-Commerce Conversion Funnel
**Total Tests: 24**

#### Part 1: Floating-Point Parsing (8 tests)
- ✓ Integer parsing
- ✓ Decimal with precision
- ✓ Zero value
- ✓ Negative numbers
- ✓ Invalid input exception
- ✓ Null input exception
- ✓ Duration calculation
- ✓ Reverse order duration

#### Part 2: Conversion Funnel (6 tests)
- ✓ Complete conversion
- ✓ Abandoned at ADD_TO_CART
- ✓ Abandoned at BROWSE
- ✓ Multiple users with mixed outcomes
- ✓ Out-of-order event sorting
- ✓ Empty input

#### Part 3: Subsequence Matching (10 tests)
- ✓ Pattern match with gaps
- ✓ Pattern mismatch
- ✓ Exact match (no gaps)
- ✓ Multiple gaps
- ✓ Partial match only
- ✓ Empty history
- ✓ Empty target sequence
- ✓ Find users with pattern

---

### Problem 3: Food Delivery Driver Pipeline
**Total Tests: 20**

#### Part 1: Timezone Normalization (5 tests)
- ✓ Integer normalization
- ✓ Decimal normalization
- ✓ Zero normalization
- ✓ Multiple timestamps
- ✓ Invalid input exception

#### Part 2: Delivery Pipeline (6 tests)
- ✓ Complete pipeline
- ✓ Picked up not delivered anomaly
- ✓ Never accepted
- ✓ Multiple orders mixed states
- ✓ Out-of-order event sorting
- ✓ Duration calculation

#### Part 3: Longest Wait Time (9 tests)
- ✓ Find longest wait
- ✓ Single order wait
- ✓ No wait data
- ✓ Empty input
- ✓ Multiple drivers comparison
- ✓ Wait duration in minutes

---

## Test Case Categories

### 1. **Normal/Happy Path Tests**
Test the expected behavior with valid input
- Example: Complete funnel conversion, normal entry/exit

### 2. **Edge Case Tests**
Test boundary conditions
- Example: Exactly 3 badges, zero timestamp, empty lists

### 3. **Error Handling Tests**
Test exception throwing and error conditions
- Example: Invalid timestamp, null input, out-of-order data

### 4. **Integration Tests**
Test multiple components working together
- Example: Multiple users, multiple orders, sorting + state tracking

---

## Test Assertions

Each test uses a consistent assertion pattern:

```java
boolean pass = /* condition */;
assertTest("Test name", pass, "Details: " + value);
```

**Output format:**
- ✓ PASSED: Test name
- ✗ FAILED: Test name
  - Details: Explanation

---

## Expected Test Output

```
============================================================
PROBLEM 1: KEY-CARD BADGE ACCESS SYSTEM - TEST SUITE
============================================================

>>> PART 1: DATA PARSING <<<
[TEST] Part 1: Cleaning simple data with whitespace
  ✓ PASSED: Part 1: Simple data cleaning
...
============================================================
TEST SUMMARY - Problem 1
============================================================
Passed: 14
Failed: 0
Total:  14
Success Rate: 100.0%
============================================================
```

---

## Debugging Failed Tests

If a test fails:

1. **Check the details output** - Shows what values were actually returned
2. **Verify test assumptions** - Ensure input data is correct
3. **Add print statements** - Debug the solution code
4. **Check edge cases** - Consider boundary values

Example debugging:
```java
List<String> fraudUsers = detectCardSharingFraud(swipes);
System.out.println("Expected: [user1], Got: " + fraudUsers);
```

---

## Test Maintenance

When modifying solution code:
1. Run the full test suite
2. Verify all tests still pass
3. Add new tests for new features
4. Update test documentation

---

## Performance Notes

**Test Execution Time:**
- Problem 1 Tests: ~50-100ms
- Problem 2 Tests: ~100-200ms
- Problem 3 Tests: ~50-100ms
- Total: ~200-400ms for all tests

Tests are fast since they use small datasets. Production code should handle large datasets.

---

## Continuous Integration

These tests can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run Java Tests
  run: |
    javac -cp solutions/java solutions/java/category1_state_machines/Problem*_Tests.java
    java -cp solutions/java category1_state_machines.Problem1_Tests
    java -cp solutions/java category1_state_machines.Problem2_Tests
    java -cp solutions/java category1_state_machines.Problem3_Tests
```
