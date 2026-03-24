# MCP Test Data Integration Guide

This guide shows how to integrate the Test Data MCP Server with your existing Playwright test suite.

## Setup Steps

### 1. Install MCP Server Dependencies
```bash
cd mcp-servers/test-data
npm install
```

### 2. Start the MCP Server
In a separate terminal:
```bash
cd mcp-servers/test-data
export ZOHO_USER="your-email@zoho.com"
export ZOHO_PASS="your-password"
npm start
```

The server will run on `localhost:3000` (by default). If the server isn't available, the client will automatically fall back to reading `test-data.json`.

### 3. Install Maven Dependencies
From your project root:
```bash
mvn clean install
```

This will install Jackson (required for the MCP client).

---

## Using the MCP Client in Your Tests

### Basic Usage: Get Primary Test User
```java
import com.zohopeopleqa.mcp.TestDataMCPClient;
import com.zohopeopleqa.mcp.TestDataMCPClient.TestUser;

public class MyTest extends BaseTest {
    
    @Test
    public void testLoginWithMCP() {
        // Get primary test user (uses environment variables)
        TestUser user = TestDataMCPClient.getTestUser("user-001");
        
        String email = user.getEmail();
        String password = user.getPassword();
        
        loginToZohoPeople(email, password);
        // ... rest of test
    }
}
```

### Get User by Role
```java
// Get any manager user
TestUser manager = TestDataMCPClient.getTestUser("manager");

// Get admin user
TestUser admin = TestDataMCPClient.getTestUser("admin");
```

### Get Leave Balance
```java
TestDataMCPClient.LeaveBalance balance = 
    TestDataMCPClient.getLeaveBalance("user-001");

int casualLeavesAvailable = balance.casualLeave.available;
int sickLeavesAvailable = balance.sickLeave.available;

System.out.println("Available casual leaves: " + casualLeavesAvailable);
```

### Get Pre-configured Leave Scenario
```java
TestDataMCPClient.LeaveScenario scenario = 
    TestDataMCPClient.getLeaveScenario("leave-001");

String startDate = scenario.startDate;  // "2026-03-24"
String endDate = scenario.endDate;      // "2026-03-24"
int days = scenario.days;               // 1
String type = scenario.type;            // "Casual Leave"
```

### List All Test Users
```java
import com.fasterxml.jackson.databind.JsonNode;

// List all users
JsonNode allUsers = TestDataMCPClient.listTestUsers(null, null);

// List only users tagged with "smoke-tests"
JsonNode smokeTestUsers = TestDataMCPClient.listTestUsers("smoke-tests", null);

// List only managers
JsonNode managers = TestDataMCPClient.listTestUsers(null, "Manager");
```

### Get Environment Configuration
```java
TestDataMCPClient.EnvironmentConfig env = 
    TestDataMCPClient.getEnvironmentConfig("Staging");

String url = env.url;  // "https://staging-people.zoho.com"
```

---

## Example: Complete Test Using MCP

```java
package com.zohopeopleqa.tests;

import com.zohopeopleqa.base.BaseTest;
import com.zohopeopleqa.mcp.TestDataMCPClient;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.*;

@Epic("Zoho People QA")
@Feature("Leave Management")
public class LeaveBalanceIntegrityTest extends BaseTest {

    @Test(description = "Test leave request with data from MCP")
    @Story("Leave Request Flow")
    @Severity(SeverityLevel.CRITICAL)
    public void testLeaveRequestWithMCP() {
        
        // Step 1: Get test user from MCP
        TestDataMCPClient.TestUser user = 
            TestDataMCPClient.getTestUser("user-001");
        
        Allure.parameter("User", user.getName());
        
        // Step 2: Login with credentials
        loginToZohoPeople(user.getEmail(), user.getPassword());
        
        // Step 3: Get leave scenario
        TestDataMCPClient.LeaveScenario scenario = 
            TestDataMCPClient.getLeaveScenario("leave-001");
        
        // Step 4: Submit leave request
        fillLeaveForm(
            scenario.startDate,
            scenario.endDate,
            scenario.type,
            scenario.reason
        );
        
        // Step 5: Verify leave balance is updated
        TestDataMCPClient.LeaveBalance balance = 
            TestDataMCPClient.getLeaveBalance(user.getId());
        
        Assert.assertTrue(
            balance.casualLeave.pending > 0,
            "Leave request was not submitted"
        );
        
        System.out.println("✅ Test PASSED: Leave request submitted successfully");
    }
    
    @Test(description = "Test multi-user leave workflow")
    @Story("Approval Workflow")
    public void testLeaveApprovalWorkflow() {
        
        // Get employee and manager users
        TestDataMCPClient.TestUser employee = 
            TestDataMCPClient.getTestUser("user-002");
        TestDataMCPClient.TestUser manager = 
            TestDataMCPClient.getTestUser("manager");
        
        // Employee submits leave
        loginToZohoPeople(employee.getEmail(), employee.getPassword());
        TestDataMCPClient.LeaveScenario scenario = 
            TestDataMCPClient.getLeaveScenario("multi-day");
        
        submitLeaveRequest(scenario);
        logout();
        
        // Manager approves leave
        loginToZohoPeople(manager.getEmail(), manager.getPassword());
        navigateToApprovals();
        approveLeaveRequest(employee.getName());
        logout();
        
        // Verify leave is approved
        loginToZohoPeople(employee.getEmail(), employee.getPassword());
        TestDataMCPClient.LeaveBalance balance = 
            TestDataMCPClient.getLeaveBalance(employee.getId());
        
        Assert.assertEquals(balance.casualLeave.used, 5, "Leave not approved");
        
        System.out.println("✅ Test PASSED: Approval workflow completed");
    }
}
```

---

## Integration Point: Update BaseTest.java (Optional)

You can add convenience methods to `BaseTest.java` for easier access:

```java
// In BaseTest.java
protected TestDataMCPClient.TestUser getPrimaryTestUser() {
    return TestDataMCPClient.getTestUser("user-001");
}

protected TestDataMCPClient.LeaveBalance getMyLeaveBalance(String userId) {
    return TestDataMCPClient.getLeaveBalance(userId);
}

protected TestDataMCPClient.LeaveScenario getLeaveScenario(String scenarioId) {
    return TestDataMCPClient.getLeaveScenario(scenarioId);
}
```

Then in your tests:
```java
@Test
public void myTest() {
    TestDataMCPClient.TestUser user = getPrimaryTestUser();
    // ...
}
```

---

## Fallback Behavior

The MCP client is designed with **graceful degradation**:

1. **MCP Server Available** → Use server (real-time data, dynamic updates)
2. **MCP Server Down** → Read from `test-data.json` (static test data)
3. **test-data.json Missing** → Fall back to environment variables

This means your tests will continue to work even if the MCP server isn't running.

---

## Updating Test Data

### Add New Test User
Edit `mcp-servers/test-data/test-data.json`:
```json
{
  "id": "user-005",
  "name": "Contractor User",
  "email": "contractor@example.com",
  "password": "Password123!",
  "role": "Contractor",
  "department": "Operations",
  "status": "active",
  "tags": ["contractor", "limited-access"]
}
```

Then your tests can use it:
```java
TestUser contractor = TestDataMCPClient.getTestUser("user-005");
```

### Add New Leave Scenario
```json
{
  "id": "leave-004",
  "name": "Compassionate Leave",
  "type": "Compassionate Leave",
  "days": 3,
  "startDate": "2026-04-10",
  "endDate": "2026-04-12",
  "userId": "user-001",
  "reason": "Family emergency",
  "status": "draft"
}
```

---

## Running Tests with MCP

### Option 1: Manual Server Start
```bash
# Terminal 1: Start MCP server
cd mcp-servers/test-data
npm start

# Terminal 2: Run tests
mvn test
```

### Option 2: Automated (CI/CD)
```bash
#!/bin/bash
# start-tests.sh

# Start MCP server in background
cd mcp-servers/test-data
npm start &
MCP_PID=$!

# Wait for server to be ready
sleep 2

# Run tests
cd ../..
mvn test

# Cleanup
kill $MCP_PID
```

Run with: `./start-tests.sh`

---

## Troubleshooting

**Q: "MCP Server not available" warning but tests still run?**  
A: That's expected! The client falls back to test-data.json automatically.

**Q: Getting "User not found" errors?**  
A: Check that the userId/role exists in test-data.json. Available users:
- `user-001` (Employee)
- `user-002` (Employee)
- `user-003` (Manager)
- `user-004` (Admin)
- `manager`, `admin` (by role)

**Q: Want to use different test data?**  
A: Edit `mcp-servers/test-data/test-data.json`, restart the server, and re-run tests.

**Q: Can I run tests without the MCP server?**  
A: Yes! Tests will automatically use test-data.json instead.

**Q: How do I extend test data dynamically?**  
A: Modify test-data.json → the MCP server will automatically use the new data.

---

## Benefits

✅ **Centralized Test Data** — Single source of truth for all test users and scenarios  
✅ **Multiple Test Users** — Support different roles without code changes  
✅ **Leave Scenarios** — Pre-configured test cases for common workflows  
✅ **Environment Configs** — Staging vs. Production settings  
✅ **Graceful Fallbacks** — Tests work with or without MCP server  
✅ **Easy Maintenance** — Update test data by editing JSON, no code changes  
✅ **Scalability** — Add unlimited test users and scenarios  

---

## Next Steps

1. ✅ Start MCP server: `npm start` in `mcp-servers/test-data/`
2. ✅ Update your tests to use `TestDataMCPClient`
3. ✅ Run tests: `mvn test`
4. ✅ Monitor Allure reports for coverage improvements
5. ✅ Add more test users/scenarios to `test-data.json` as needed

---

Questions? Check the MCP server README:
[mcp-servers/test-data/README.md](../mcp-servers/test-data/README.md)
