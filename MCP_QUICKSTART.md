# MCP Test Data Setup - Quick Start

## What Was Created

✅ **MCP Server** (`mcp-servers/test-data/`)
- Node.js server providing test data via MCP protocol
- 4 pre-configured test users (Employee, Manager, Admin)
- Leave balance tracking
- Pre-configured leave scenarios
- Environment configs for Staging/Production

✅ **Java MCP Client** (`src/main/java/com/zohopeopleqa/mcp/TestDataMCPClient.java`)
- HTTP client to communicate with MCP server
- Automatic fallback to `test-data.json` if server is unavailable
- Methods to fetch users, leave balances, scenarios, and environment configs

✅ **Test Data** (`mcp-servers/test-data/test-data.json`)
- 4 test users with different roles
- 3 pre-configured leave scenarios
- Leave balances for each user
- Environment configurations

✅ **Documentation**
- [MCP_INTEGRATION_GUIDE.md](MCP_INTEGRATION_GUIDE.md) - Complete integration examples
- [mcp-servers/test-data/README.md](mcp-servers/test-data/README.md) - Server details

---

## Quick Start (5 minutes)

### Step 1: Install Dependencies
```bash
# Install MCP server dependencies
cd mcp-servers/test-data
npm install

# Go back to project root
cd ../..

# Install Java dependencies
mvn clean install
```

### Step 2: Start MCP Server
In a separate terminal:
```bash
cd mcp-servers/test-data
export ZOHO_USER="your-email@zoho.com"
export ZOHO_PASS="your-password"
npm start
```

You should see:
```
Test Data MCP Server started
Available tools: get_test_user, list_test_users, get_leave_balance, get_leave_scenario, get_environment_config
```

### Step 3: Use in Your Tests
```java
import com.zohopeopleqa.mcp.TestDataMCPClient;

@Test
public void myTest() {
    // Get test user
    TestDataMCPClient.TestUser user = TestDataMCPClient.getTestUser("user-001");
    
    // Use in login
    loginToZohoPeople(user.getEmail(), user.getPassword());
    
    // Get leave data
    TestDataMCPClient.LeaveBalance balance = 
        TestDataMCPClient.getLeaveBalance(user.getId());
    
    System.out.println("Available leaves: " + balance.casualLeave.available);
}
```

### Step 4: Run Tests
```bash
mvn test
```

---

## Available Test Users

| ID | Email | Role | Use Case |
|---|---|---|---|
| `user-001` | `${ZOHO_USER}` | Employee | Primary test user (from env vars) |
| `user-002` | `secondary-test@zoho.com` | Employee | Secondary user for multi-user tests |
| `user-003` | `manager-test@zoho.com` | Manager | Approval workflows |
| `user-004` | `admin-test@zoho.com` | Admin | Configuration/admin tests |

You can also use role names directly:
```java
TestDataMCPClient.getTestUser("manager");  // Returns user-003
TestDataMCPClient.getTestUser("admin");    // Returns user-004
```

---

## Available Leave Scenarios

| Scenario ID | Type | Days | Use Case |
|---|---|---|---|
| `leave-001` | Casual Leave | 1 | Single-day absence |
| `leave-002` | Casual Leave | 5 | Multi-day vacation |
| `leave-003` | Sick Leave | 1 | Sick leave request |

```java
TestDataMCPClient.LeaveScenario scenario = 
    TestDataMCPClient.getLeaveScenario("leave-001");

// Or search by type
scenario = TestDataMCPClient.getLeaveScenario("multi-day");
```

---

## Automating Server Start (Optional)

Create a helper script to auto-start the MCP server:

**run-tests-with-mcp.sh:**
```bash
#!/bin/bash

# Start MCP server in background
cd mcp-servers/test-data
export ZOHO_USER="your-email@zoho.com"
export ZOHO_PASS="your-password"
npm start &
MCP_PID=$!

# Wait for server to initialize
sleep 2

# Run tests
cd ../..
mvn test

# Stop MCP server
kill $MCP_PID
```

Then:
```bash
chmod +x run-tests-with-mcp.sh
./run-tests-with-mcp.sh
```

---

## Key Benefits

✅ **No code changes needed** — Tests work with or without MCP server  
✅ **Centralized data** — Update test users without touching code  
✅ **Multi-user testing** — Support Manager, Admin, Employee roles  
✅ **Scalable** — Add unlimited users/scenarios by editing JSON  
✅ **Error resilient** — Graceful fallback if server is unavailable  

---

## Verification Checklist

- [ ] MCP server dependencies installed (`npm install`)
- [ ] Java dependencies installed (`mvn clean install`)
- [ ] MCP server starts without errors
- [ ] Test imports `TestDataMCPClient`
- [ ] Tests use `TestDataMCPClient.getTestUser()` instead of hardcoded credentials
- [ ] Tests run successfully with `mvn test`
- [ ] Allure reports show test results

---

## Next Steps

1. **Extend test data** — Add more users/scenarios to `test-data.json`
2. **Migrate tests** — Update existing tests to use MCP client (see [Integration Guide](MCP_INTEGRATION_GUIDE.md))
3. **Add more tools** — Create additional MCP tools for other test data (departments, managers, salary bands, etc.)
4. **CI/CD Integration** — Integrate MCP server startup into your build pipeline

---

## Troubleshooting

**Server won't start?**
- Make sure Node.js is installed: `node --version`
- Check environment variables are set: `echo $ZOHO_USER`
- Ensure port 3000 is free

**Tests still using hardcoded credentials?**
- Update test imports to include `TestDataMCPClient`
- Replace `System.getenv()` with `TestDataMCPClient.getTestUser()`

**Getting "User not found"?**
- Check test-data.json for correct userId
- Available IDs: user-001, user-002, user-003, user-004
- Or use role names: "manager", "admin", "employee"

---

**Questions?** See:
- [Full Integration Guide](MCP_INTEGRATION_GUIDE.md)
- [MCP Server README](mcp-servers/test-data/README.md)
