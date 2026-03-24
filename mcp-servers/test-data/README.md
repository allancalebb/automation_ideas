# Test Data MCP Server for Zoho People QA

This MCP server provides centralized test data management for your Playwright/TestNG automation suite.

## Features

✅ **Multiple Test Users** — Different roles (Employee, Manager, Admin)  
✅ **Leave Scenarios** — Pre-configured test cases for leave requests  
✅ **Leave Balances** — Track allocated vs. used leave  
✅ **Environment Configs** — Staging and Production settings  
✅ **Environment Variables** — Resolves `${ZOHO_USER}` and `${ZOHO_PASS}` at runtime  

## Setup

### 1. Install Dependencies
```bash
cd mcp-servers/test-data
npm install
```

### 2. Configure Environment Variables
```bash
export ZOHO_USER="your-email@zoho.com"
export ZOHO_PASS="your-password"
```

### 3. Start the Server
```bash
npm start
```

## Available Tools

### `get_test_user`
Get credentials and profile for a specific test user.

**Parameters:**
- `userId` (string, required): User ID or role (e.g., "user-001", "manager", "admin")

**Example:**
```json
{
  "method": "tools/call",
  "params": {
    "name": "get_test_user",
    "arguments": { "userId": "user-001" }
  }
}
```

### `list_test_users`
List all available test users with filtering options.

**Parameters:**
- `filterByTag` (string, optional): e.g., "smoke-tests", "regression"
- `filterByRole` (string, optional): e.g., "Manager", "Admin"

### `get_leave_balance`
Get leave balance for a specific user.

**Parameters:**
- `userId` (string, required): User ID
- `year` (number, optional): Year (default: current year)

### `get_leave_scenario`
Get a pre-configured leave request scenario.

**Parameters:**
- `scenarioId` (string, required): Scenario ID or type (e.g., "leave-001", "multi-day", "sick-leave")

### `get_environment_config`
Get configuration for a test environment.

**Parameters:**
- `environment` (string, required): "Staging" or "Production"

## Integration with Your Tests

### Option 1: Java/Playwright Integration (HTTP Wrapper)
Create a simple HTTP wrapper to call the MCP server from your Java tests:

```java
// In BaseTest.java
protected String getTestUserEmail(String userId) {
    // Call MCP server via HTTP
    return mcpClient.getTool("get_test_user", userId).getEmail();
}
```

### Option 2: Pre-fetch at Test Runtime
Run the MCP server in the background and fetch test data before tests:

```bash
# Start MCP server in background
npm start &
MCP_PID=$!

# Run tests
mvn test

# Stop MCP server
kill $MCP_PID
```

### Option 3: Direct File Access
Reference `test-data.json` directly in your tests:

```java
ObjectMapper mapper = new ObjectMapper();
TestData data = mapper.readValue(new File("mcp-servers/test-data/test-data.json"), TestData.class);
```

## Test Data Structure

### Test Users
- **user-001**: Primary test user (Employee) - uses environment variables
- **user-002**: Secondary test user (Employee)
- **user-003**: Manager for approval workflows
- **user-004**: Admin for configuration tests

### Leave Types
- Casual Leave (8 days/year)
- Sick Leave (6 days/year)
- Unpaid Leave (unlimited)

### Scenarios
- Single-day leave request
- Multi-day vacation
- Sick leave with reason
- (Add more as needed)

## Extending Test Data

Edit `test-data.json` to add:
- More test users with different roles
- Additional leave scenarios
- Custom attributes (department, manager, salary band, etc.)

**Example: Add a new user**
```json
{
  "id": "user-005",
  "name": "Contractor Test User",
  "email": "contractor@example.com",
  "password": "ContractorPass123!",
  "role": "Contractor",
  "department": "Operations",
  "status": "active",
  "tags": ["contractor", "limited-access"]
}
```

## MCP Protocol Details

The server implements the Model Context Protocol for standardized tool discovery and execution.

**Supported methods:**
- `tools/list` — Returns available tools and schemas
- `tools/call` — Executes a tool with parameters

## Troubleshooting

**Q: Server starts but tools aren't available**  
A: Ensure environment variables are exported before starting the server:
```bash
export ZOHO_USER="..." ZOHO_PASS="..." && npm start
```

**Q: Getting "User not found" errors**  
A: Check `test-data.json` for correct user IDs and roles.

**Q: Want to add more test data?**  
A: Edit `test-data.json` and restart the server.

---

**Next Steps:**
1. Start the MCP server: `npm start`
2. Add HTTP wrapper in your Java tests to call it
3. Update test cases to use dynamic test data instead of hardcoded values
4. Run tests with `./run-tests.sh`
