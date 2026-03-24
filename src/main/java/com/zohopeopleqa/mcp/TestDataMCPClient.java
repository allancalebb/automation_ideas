package com.zohopeopleqa.mcp;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * MCP Client for fetching test data from the Test Data MCP Server
 * Communicates with the MCP server running on localhost
 */
public class TestDataMCPClient {
    
    private static final String MCP_SERVER_HOST = "localhost";
    private static final int MCP_SERVER_PORT = 3000; // Default port
    private static final ObjectMapper mapper = new ObjectMapper();
    private static boolean mcpAvailable = false;
    
    static {
        // Check if MCP server is available on startup
        mcpAvailable = isServerAvailable();
        if (!mcpAvailable) {
            System.out.println("⚠️ MCP Server not available at " + MCP_SERVER_HOST + ":" + MCP_SERVER_PORT);
            System.out.println("Falling back to environment variables and test-data.json");
        }
    }
    
    /**
     * Get a test user by ID or role
     * @param userId User ID (e.g., "user-001") or role (e.g., "manager", "admin")
     * @return User data including email and password
     */
    public static TestUser getTestUser(String userId) {
        try {
            if (mcpAvailable) {
                JsonNode result = callMCPTool("get_test_user", "{\"userId\": \"" + userId + "\"}");
                return mapper.treeToValue(result, TestUser.class);
            }
        } catch (Exception e) {
            System.err.println("❌ Error calling get_test_user: " + e.getMessage());
        }
        
        // Fallback to test-data.json
        return getTestUserFromFile(userId);
    }
    
    /**
     * List all test users with optional filtering
     * @param filterByTag Optional tag filter (e.g., "smoke-tests")
     * @param filterByRole Optional role filter (e.g., "Manager")
     * @return List of users matching the filter
     */
    public static JsonNode listTestUsers(String filterByTag, String filterByRole) {
        try {
            if (mcpAvailable) {
                StringBuilder params = new StringBuilder("{");
                boolean first = true;
                
                if (filterByTag != null) {
                    params.append("\"filterByTag\": \"").append(filterByTag).append("\"");
                    first = false;
                }
                if (filterByRole != null) {
                    if (!first) params.append(", ");
                    params.append("\"filterByRole\": \"").append(filterByRole).append("\"");
                }
                params.append("}");
                
                return callMCPTool("list_test_users", params.toString());
            }
        } catch (Exception e) {
            System.err.println("❌ Error calling list_test_users: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get leave balance for a user
     * @param userId User ID
     * @param year Year (optional, defaults to current year)
     * @return Leave balance data
     */
    public static LeaveBalance getLeaveBalance(String userId, Integer year) {
        try {
            if (mcpAvailable) {
                String params = "{\"userId\": \"" + userId + "\"" + 
                               (year != null ? ", \"year\": " + year : "") + "}";
                JsonNode result = callMCPTool("get_leave_balance", params);
                return mapper.treeToValue(result, LeaveBalance.class);
            }
        } catch (Exception e) {
            System.err.println("❌ Error calling get_leave_balance: " + e.getMessage());
        }
        
        // Fallback to test-data.json
        return getLeaveBalanceFromFile(userId, year);
    }
    
    /**
     * Get a leave request scenario
     * @param scenarioId Scenario ID (e.g., "leave-001") or type (e.g., "multi-day")
     * @return Scenario data
     */
    public static LeaveScenario getLeaveScenario(String scenarioId) {
        try {
            if (mcpAvailable) {
                JsonNode result = callMCPTool("get_leave_scenario", "{\"scenarioId\": \"" + scenarioId + "\"}");
                return mapper.treeToValue(result, LeaveScenario.class);
            }
        } catch (Exception e) {
            System.err.println("❌ Error calling get_leave_scenario: " + e.getMessage());
        }
        
        // Fallback to test-data.json
        return getLeaveScenarioFromFile(scenarioId);
    }
    
    /**
     * Get environment configuration
     * @param environment Environment name ("Staging" or "Production")
     * @return Environment config
     */
    public static EnvironmentConfig getEnvironmentConfig(String environment) {
        try {
            if (mcpAvailable) {
                JsonNode result = callMCPTool("get_environment_config", 
                    "{\"environment\": \"" + environment + "\"}");
                return mapper.treeToValue(result, EnvironmentConfig.class);
            }
        } catch (Exception e) {
            System.err.println("❌ Error calling get_environment_config: " + e.getMessage());
        }
        
        // Fallback to test-data.json
        return getEnvironmentConfigFromFile(environment);
    }
    
    // ========== Private helper methods ==========
    
    private static JsonNode callMCPTool(String toolName, String params) throws Exception {
        String payload = "{\"method\": \"tools/call\", \"params\": {\"name\": \"" + 
                        toolName + "\", \"arguments\": " + params + "}}";
        
        URL url = new URI("http", null, MCP_SERVER_HOST, MCP_SERVER_PORT, "/", null, null).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes();
            os.write(input, 0, input.length);
        }
        
        if (conn.getResponseCode() == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                JsonNode jsonResponse = mapper.readTree(response.toString());
                return jsonResponse.get("result");
            }
        } else {
            throw new RuntimeException("MCP Server returned: " + conn.getResponseCode());
        }
    }
    
    private static boolean isServerAvailable() {
        try {
            URL url = new URI("http", null, MCP_SERVER_HOST, MCP_SERVER_PORT, "/health", null, null).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int code = conn.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ========== Fallback file-based methods ==========
    
    private static TestUser getTestUserFromFile(String userId) {
        try {
            String content = new String(Files.readAllBytes(
                Paths.get("mcp-servers/test-data/test-data.json")));
            JsonNode root = mapper.readTree(content);
            JsonNode users = root.get("testUsers");
            
            for (JsonNode user : users) {
                if (user.get("id").asText().equals(userId) || 
                    user.get("role").asText().equalsIgnoreCase(userId)) {
                    // Resolve environment variables
                    return resolveTestUser(mapper.treeToValue(user, TestUser.class));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading test-data.json: " + e.getMessage());
        }
        
        // Return default from environment variables
        return new TestUser();
    }
    
    private static TestUser resolveTestUser(TestUser user) {
        if (user.getPassword() != null && user.getPassword().contains("${")) {
            user.setPassword(resolveEnvVar(user.getPassword()));
        }
        if (user.getEmail() != null && user.getEmail().contains("${")) {
            user.setEmail(resolveEnvVar(user.getEmail()));
        }
        return user;
    }
    
    private static String resolveEnvVar(String str) {
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String envVar = matcher.group(1);
            String envValue = System.getenv(envVar);
            matcher.appendReplacement(sb, envValue != null ? Matcher.quoteReplacement(envValue) : matcher.group(0));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private static LeaveBalance getLeaveBalanceFromFile(String userId, Integer year) {
        try {
            String content = new String(Files.readAllBytes(
                Paths.get("mcp-servers/test-data/test-data.json")));
            JsonNode root = mapper.readTree(content);
            JsonNode balances = root.get("leaveBalances");
            int targetYear = year != null ? year : java.time.Year.now().getValue();
            
            for (JsonNode balance : balances) {
                if (balance.get("userId").asText().equals(userId) &&
                    balance.get("year").asInt() == targetYear) {
                    return mapper.treeToValue(balance, LeaveBalance.class);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading test-data.json: " + e.getMessage());
        }
        return null;
    }
    
    private static LeaveScenario getLeaveScenarioFromFile(String scenarioId) {
        try {
            String content = new String(Files.readAllBytes(
                Paths.get("mcp-servers/test-data/test-data.json")));
            JsonNode root = mapper.readTree(content);
            JsonNode scenarios = root.get("leaveScenarios");
            
            for (JsonNode scenario : scenarios) {
                if (scenario.get("id").asText().equals(scenarioId) ||
                    scenario.get("type").asText().toLowerCase().contains(scenarioId.toLowerCase())) {
                    return mapper.treeToValue(scenario, LeaveScenario.class);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading test-data.json: " + e.getMessage());
        }
        return null;
    }
    
    private static EnvironmentConfig getEnvironmentConfigFromFile(String environment) {
        try {
            String content = new String(Files.readAllBytes(
                Paths.get("mcp-servers/test-data/test-data.json")));
            JsonNode root = mapper.readTree(content);
            JsonNode envs = root.get("testEnvironments");
            
            for (JsonNode env : envs) {
                if (env.get("name").asText().equals(environment)) {
                    return mapper.treeToValue(env, EnvironmentConfig.class);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading test-data.json: " + e.getMessage());
        }
        return null;
    }
    
    // ========== Data classes ==========
    
    public static class TestUser {
        public String id;
        public String name;
        public String email;
        public String password;
        public String role;
        public String department;
        public String status;
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { 
            return email != null ? email : System.getenv("ZOHO_USER");
        }
        public String getPassword() { 
            return password != null ? password : System.getenv("ZOHO_PASS");
        }
        public String getRole() { return role; }
        public String getDepartment() { return department; }
        public String getStatus() { return status; }
        
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class LeaveBalance {
        public String userId;
        public int year;
        public LeaveType casualLeave;
        public LeaveType sickLeave;
        public LeaveType unpaidLeave;
        
        public static class LeaveType {
            public int allocated;
            public int used;
            public int pending;
            public int available;
        }
    }
    
    public static class LeaveScenario {
        public String id;
        public String name;
        public String type;
        public int days;
        public String startDate;
        public String endDate;
        public String userId;
        public String reason;
        public String status;
    }
    
    public static class EnvironmentConfig {
        public String name;
        public String url;
        public String dataRefreshInterval;
        public String status;
        public String warning;
    }
}
