# Unit Testing Summary

This document explains all the JUnit test cases that were created to ensure the Warehouse Management System works correctly.

## Test Coverage Summary

**Total Tests**: 25 unit tests
**All Tests Status**: ✅ PASSING

**Test Breakdown by Component**:
- CreateWarehouseUseCaseTest: 6 tests
- ArchiveWarehouseUseCaseTest: 4 tests
- ReplaceWarehouseUseCaseTest: 7 tests
- LocationGatewayTest: 8 tests

---

## 1. CreateWarehouseUseCaseTest (6 Tests)

This test class verifies that creating new warehouses works correctly and all business rules are enforced.

### Test 1: Successful Warehouse Creation
**What it tests**: Creating a warehouse with valid data should work

**Scenario**:
- We want to create a warehouse with code "MWH.TEST"
- Location is "AMSTERDAM-001" (which exists in the system)
- Capacity is 500 units
- Initial stock is 100 units

**What happens**:
- The system accepts the request
- A new warehouse is created successfully
- The warehouse gets saved to the database

**Why this matters**: Confirms that the basic warehouse creation process works when everything is correct.

---

### Test 2: Duplicate Business Unit Code Rejection
**What it tests**: You cannot create two warehouses with the same code

**Scenario**:
- A warehouse with code "MWH.TEST" already exists
- Someone tries to create another warehouse with the same code "MWH.TEST"

**What happens**:
- The system rejects the request immediately
- Error message shown: "Warehouse already exists with business unit code: MWH.TEST"
- No new warehouse is created

**Why this matters**: Prevents confusion by ensuring each warehouse has a unique identifier. Just like how you can't have two people with the same employee ID.

---

### Test 3: Invalid Location Rejection
**What it tests**: You can only create warehouses in locations that exist in the system

**Scenario**:
- Someone tries to create a warehouse at location "INVALID-LOCATION"
- This location code doesn't exist in the system

**What happens**:
- The system rejects the request
- Error message shown: "Location not found with identifier: AMSTERDAM-001"
- No warehouse is created

**Why this matters**: Prevents creating warehouses in non-existent or invalid locations, maintaining data integrity.

---

### Test 4: Maximum Warehouses Per Location Limit
**What it tests**: Each location can only have 5 active warehouses

**Scenario**:
- Location "AMSTERDAM-001" already has 5 active warehouses
- Someone tries to create a 6th warehouse at the same location

**What happens**:
- The system rejects the request
- Error message shown: "Maximum number of warehouses (5) reached for location: AMSTERDAM-001"
- No new warehouse is created

**Why this matters**: Prevents overcrowding and maintains operational efficiency. Too many warehouses in one location could cause logistical problems.

---

### Test 5: Capacity Exceeds Location Maximum
**What it tests**: Warehouse capacity cannot be bigger than what the location can handle

**Scenario**:
- Location "AMSTERDAM-001" has a maximum capacity of 1000 units
- Someone tries to create a warehouse with capacity of 1001 units

**What happens**:
- The system rejects the request
- Error message shown: "Warehouse capacity (1001) exceeds location maximum capacity (1000)"
- No warehouse is created

**Why this matters**: You can't plan for more storage space than physically exists. This is like trying to park 6 cars in a garage that only fits 5.

---

### Test 6: Stock Cannot Exceed Capacity
**What it tests**: You can't have more items than storage space

**Scenario**:
- Creating a warehouse with capacity of 500 units
- But trying to set initial stock as 600 units

**What happens**:
- The system rejects the request
- Error message shown: "Warehouse stock (600) exceeds warehouse capacity (500)"
- No warehouse is created

**Why this matters**: It's physically impossible to store more items than you have space for. This validation prevents unrealistic data.

---

## 2. ArchiveWarehouseUseCaseTest (4 Tests)

This test class verifies that archiving (soft-deleting) warehouses works correctly.

### Test 1: Successful Warehouse Archive
**What it tests**: Archiving a valid, active warehouse works

**Scenario**:
- An active warehouse with code "MWH.TEST" exists
- We want to archive it (mark it as inactive)

**What happens**:
- The system finds the warehouse
- Sets the "archivedAt" timestamp to the current date/time
- The warehouse is now marked as archived
- The data is still in the database (not deleted)

**Why this matters**: Confirms that the basic archive operation works. Warehouses aren't permanently deleted, allowing us to keep historical records.

---

### Test 2: Cannot Archive Non-Existent Warehouse
**What it tests**: You can't archive a warehouse that doesn't exist

**Scenario**:
- Someone tries to archive warehouse code "NON-EXISTENT"
- No warehouse with this code exists in the system

**What happens**:
- The system rejects the request
- Error message shown: "Warehouse not found with business unit code: NON-EXISTENT"
- Nothing changes in the database

**Why this matters**: Prevents errors from trying to archive warehouses that don't exist.

---

### Test 3: Cannot Archive Already Archived Warehouse
**What it tests**: You can't archive a warehouse that's already archived

**Scenario**:
- Warehouse "MWH.TEST" was archived yesterday
- Someone tries to archive it again today

**What happens**:
- The system rejects the request
- Error message shown: "Warehouse is already archived: MWH.TEST"
- The archived date doesn't change

**Why this matters**: Prevents duplicate archive operations and maintains accurate archive timestamps.

---

### Test 4: Invalid Business Unit Code Handling
**What it tests**: System handles invalid warehouse codes gracefully

**Scenario**:
- Someone provides null or empty string as warehouse code
- This is clearly invalid data

**What happens**:
- The system rejects the request immediately
- Clear error message is shown
- No database queries are attempted

**Why this matters**: Validates input data before doing expensive database operations, improving system performance and providing quick feedback on errors.

---

## 3. ReplaceWarehouseUseCaseTest (7 Tests)

This test class verifies that replacing warehouses (moving operations to a new location) works correctly.

### Test 1: Successful Warehouse Replacement
**What it tests**: Complete warehouse replacement process works

**Scenario**:
- Active warehouse "MWH.TEST" exists at "AMSTERDAM-001" with 800 items
- We want to move it to "ROTTERDAM-001" with new capacity of 1500 units

**What happens**:
1. System finds the active warehouse "MWH.TEST"
2. Archives the old warehouse at Amsterdam
3. Creates a new warehouse at Rotterdam
4. New warehouse has same business code "MWH.TEST"
5. New warehouse capacity is 1500 units
6. Stock of 800 items is transferred

**Why this matters**: Confirms the entire warehouse relocation process works smoothly when all conditions are met.

---

### Test 2: Cannot Replace Non-Existent Warehouse
**What it tests**: You can't replace a warehouse that doesn't exist

**Scenario**:
- Someone tries to replace warehouse "NON-EXISTENT"
- No active warehouse with this code exists

**What happens**:
- The system rejects the request
- Error message shown: "No active warehouse found with business unit code: NON-EXISTENT"
- Nothing changes in the database

**Why this matters**: Prevents confusion by ensuring you can only replace warehouses that actually exist and are currently active.

---

### Test 3: New Location Must Exist
**What it tests**: The new location must be valid

**Scenario**:
- Replacing warehouse "MWH.TEST"
- Trying to move it to location "INVALID-LOCATION" which doesn't exist

**What happens**:
- The system rejects the request
- Error message shown: "Location not found with identifier: AMSTERDAM-001"
- Old warehouse remains unchanged

**Why this matters**: Ensures we don't try to move warehouse operations to non-existent locations.

---

### Test 4: New Capacity Must Accommodate Current Stock
**What it tests**: New warehouse must have space for existing items

**Scenario**:
- Old warehouse has 800 items currently stored
- New warehouse has capacity of only 500 units

**What happens**:
- The system rejects the request
- Error message shown: "New warehouse capacity (500) cannot accommodate current stock (800) from old warehouse"
- Replacement is cancelled

**Why this matters**: You can't move to a smaller warehouse if it can't fit all your current inventory. This prevents data loss and operational issues.

---

### Test 5: Stock Matching Validation
**What it tests**: Stock levels must match between old and new warehouse

**Scenario**:
- Old warehouse has 800 items
- New warehouse is specified with 900 items (doesn't match)

**What happens**:
- The system rejects the request
- Error message shown: Stock mismatch error
- Replacement is cancelled

**Why this matters**: During warehouse replacement, stock should transfer exactly - no items should appear or disappear. This ensures inventory accuracy.

---

### Test 6: New Warehouse Capacity Validation
**What it tests**: Even during replacement, capacity limits apply

**Scenario**:
- Replacing a warehouse
- New warehouse capacity (1001) exceeds location maximum (1000)

**What happens**:
- The system rejects the request
- Error message shown: "Warehouse capacity (1001) exceeds location maximum capacity (1000)"
- Replacement is cancelled

**Why this matters**: All warehouse creation rules still apply during replacement. The new warehouse must follow all the same business rules.

---

### Test 7: Stock Capacity Validation for New Warehouse
**What it tests**: Even during replacement, stock can't exceed capacity

**Scenario**:
- Replacing a warehouse
- New warehouse capacity is 1000 units but trying to set stock as 1100 units

**What happens**:
- The system rejects the request
- Error message shown: Stock exceeds capacity error
- Replacement is cancelled

**Why this matters**: Physical constraints apply everywhere - you can't have more items than storage space, even during warehouse transfers.

---

## 4. LocationGatewayTest (8 Tests)

This test class verifies that the location lookup and validation system works correctly.

### Test 1: Successful Location Resolution - Amsterdam
**What it tests**: Looking up "AMSTERDAM-001" returns correct information

**Scenario**:
- Request location details for "AMSTERDAM-001"

**What happens**:
- System finds the location
- Returns complete information:
  - Name: "AMSTERDAM-001"
  - Maximum capacity: 1000 units
  - Maximum warehouses: 5

**Why this matters**: Confirms the system can successfully find and return location information for valid location codes.

---

### Test 2: Successful Location Resolution - Rotterdam
**What it tests**: Looking up "ROTTERDAM-001" works

**Scenario**:
- Request location details for "ROTTERDAM-001"

**What happens**:
- System finds the location
- Returns correct Rotterdam location information

**Why this matters**: Verifies that location lookup works for different locations, not just one specific case.

---

### Test 3: Non-Existent Location Error
**What it tests**: Looking up a location that doesn't exist throws an error

**Scenario**:
- Request location details for "UNKNOWN-LOCATION"
- This location code doesn't exist in the system

**What happens**:
- System cannot find the location
- Error is thrown: "Location not found with identifier: UNKNOWN-LOCATION"

**Why this matters**: System properly handles cases where the requested location doesn't exist, providing clear error messages.

---

### Test 4: Null Location Identifier Error
**What it tests**: Passing null as location code throws an error

**Scenario**:
- Request location details with null as the identifier
- This is invalid input

**What happens**:
- System rejects the request immediately
- Error is thrown: "Location identifier cannot be null or blank"
- No database lookup is attempted

**Why this matters**: Validates input data before processing, preventing crashes and providing immediate feedback on invalid requests.

---

### Test 5: Empty String Location Identifier Error
**What it tests**: Passing empty string as location code throws an error

**Scenario**:
- Request location details with "" (empty string) as the identifier

**What happens**:
- System rejects the request immediately
- Error is thrown: "Location identifier cannot be null or blank"
- No database lookup is attempted

**Why this matters**: Another form of invalid input that must be caught early to prevent errors.

---

### Test 6: Blank String Location Identifier Error
**What it tests**: Passing blank string (just spaces) as location code throws an error

**Scenario**:
- Request location details with "   " (just spaces) as the identifier

**What happens**:
- System rejects the request immediately
- Error is thrown: "Location identifier cannot be null or blank"
- No database lookup is attempted

**Why this matters**: Even sneaky inputs like strings that are just whitespace are properly validated and rejected.

---

### Test 7: Case Sensitivity Test
**What it tests**: Location codes are case-sensitive

**Scenario**:
- Looking up "amsterdam-001" (lowercase)
- The actual location code is "AMSTERDAM-001" (uppercase)

**What happens**:
- System doesn't find a match
- Error is thrown: Location not found

**Why this matters**: Confirms that location codes must be exact matches - "AMSTERDAM-001" and "amsterdam-001" are treated as different codes.

---

### Test 8: Multiple Location Lookups
**What it tests**: Location lookup works repeatedly

**Scenario**:
- Look up "AMSTERDAM-001"
- Then look up "ROTTERDAM-001"
- Then look up "AMSTERDAM-001" again

**What happens**:
- Each lookup succeeds independently
- Correct information returned each time
- System handles multiple requests without issues

**Why this matters**: Verifies that the location lookup service is reliable and can handle multiple requests without errors.

---

## Testing Framework and Tools

**JUnit 5**: Main testing framework for organizing and running tests
**Mockito**: Used to create "fake" versions of dependencies for isolated testing
**AssertJ**: Provides readable assertions (checks) in tests
**JaCoCo**: Measures how much of the code is covered by tests

## Test Structure

Each test follows this pattern:

1. **Given** (Setup): Prepare the test data and conditions
2. **When** (Action): Execute the code being tested
3. **Then** (Verify): Check that the result is correct

This makes tests easy to understand and maintain.

---

## Code Coverage Results

**Overall Coverage: 94% on Business Logic**

- Warehouse Use Cases: 94% covered
- Location Gateway: 100% covered
- Domain Models: 100% covered
- Exception Classes: 79% covered

This means almost all of our important business code has automated tests verifying it works correctly.

---

## Benefits of This Test Suite

1. **Confidence**: We know the code works because tests prove it
2. **Fast Feedback**: Tests run in seconds, catching bugs immediately
3. **Regression Prevention**: Old features keep working when we add new ones
4. **Documentation**: Tests show exactly how each feature should behave
5. **Refactoring Safety**: Can improve code structure without fear of breaking things
6. **Quality Assurance**: All business rules are verified automatically

---

## How to Run the Tests

**Run all tests**:
```bash
mvn test
```

**Run specific test class**:
```bash
mvn test -Dtest=CreateWarehouseUseCaseTest
```

**Run tests and generate coverage report**:
```bash
mvn test jacoco:report
```

Coverage report will be in: `target/site/jacoco/index.html`

---