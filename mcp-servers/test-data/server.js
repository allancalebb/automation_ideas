#!/usr/bin/env node

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// Load test data
const testDataPath = path.join(__dirname, 'test-data.json');
let testData = JSON.parse(fs.readFileSync(testDataPath, 'utf8'));

// Replace environment variables in test data
function resolveEnvVariable(str) {
  if (typeof str !== 'string') return str;
  return str.replace(/\$\{([^}]+)\}/g, (match, envVar) => {
    return process.env[envVar] || match;
  });
}

function resolveTestData(data) {
  if (typeof data === 'string') {
    return resolveEnvVariable(data);
  } else if (Array.isArray(data)) {
    return data.map(resolveTestData);
  } else if (typeof data === 'object' && data !== null) {
    const resolved = {};
    for (const [key, value] of Object.entries(data)) {
      resolved[key] = resolveTestData(value);
    }
    return resolved;
  }
  return data;
}

// Tool definitions
const tools = [
  {
    name: 'get_test_user',
    description: 'Get credentials and profile for a specific test user',
    inputSchema: {
      type: 'object',
      properties: {
        userId: {
          type: 'string',
          description: 'User ID (e.g., user-001, user-002, or role like "manager", "admin")'
        }
      },
      required: ['userId']
    }
  },
  {
    name: 'list_test_users',
    description: 'List all available test users with their roles and tags',
    inputSchema: {
      type: 'object',
      properties: {
        filterByTag: {
          type: 'string',
          description: 'Optional: filter users by tag (e.g., "smoke-tests", "manager-workflow")'
        },
        filterByRole: {
          type: 'string',
          description: 'Optional: filter users by role (e.g., "Manager", "Admin")'
        }
      }
    }
  },
  {
    name: 'get_leave_balance',
    description: 'Get leave balance for a specific user',
    inputSchema: {
      type: 'object',
      properties: {
        userId: {
          type: 'string',
          description: 'User ID to get leave balance for'
        },
        year: {
          type: 'number',
          description: 'Year (default: current year)'
        }
      },
      required: ['userId']
    }
  },
  {
    name: 'get_leave_scenario',
    description: 'Get a pre-configured leave request scenario for testing',
    inputSchema: {
      type: 'object',
      properties: {
        scenarioId: {
          type: 'string',
          description: 'Scenario ID (e.g., leave-001, leave-002) or type (e.g., "multi-day", "sick-leave")'
        }
      },
      required: ['scenarioId']
    }
  },
  {
    name: 'get_environment_config',
    description: 'Get configuration for a test environment',
    inputSchema: {
      type: 'object',
      properties: {
        environment: {
          type: 'string',
          description: 'Environment name (Staging, Production)',
          enum: ['Staging', 'Production']
        }
      },
      required: ['environment']
    }
  }
];

// Tool implementations
function get_test_user(args) {
  const userId = args.userId.toLowerCase();
  const user = testData.testUsers.find(
    u => u.id === args.userId || u.role.toLowerCase() === userId
  );
  
  if (!user) {
    return {
      error: `User not found: ${args.userId}`,
      availableUsers: testData.testUsers.map(u => ({ id: u.id, role: u.role, email: u.email }))
    };
  }
  
  return resolveTestData(user);
}

function list_test_users(args) {
  let users = testData.testUsers;
  
  if (args.filterByTag) {
    users = users.filter(u => u.tags && u.tags.includes(args.filterByTag));
  }
  
  if (args.filterByRole) {
    users = users.filter(u => u.role === args.filterByRole);
  }
  
  return {
    count: users.length,
    users: resolveTestData(users.map(u => ({
      id: u.id,
      name: u.name,
      role: u.role,
      department: u.department,
      tags: u.tags
    })))
  };
}

function get_leave_balance(args) {
  const year = args.year || new Date().getFullYear();
  const balance = testData.leaveBalances.find(
    b => b.userId === args.userId && b.year === year
  );
  
  if (!balance) {
    return {
      error: `No leave balance found for user ${args.userId} in year ${year}`,
      availableUsers: testData.leaveBalances.map(b => b.userId)
    };
  }
  
  return resolveTestData(balance);
}

function get_leave_scenario(args) {
  const scenarioId = args.scenarioId.toLowerCase();
  const scenario = testData.leaveScenarios.find(
    s => s.id === args.scenarioId || s.type.toLowerCase().includes(scenarioId)
  );
  
  if (!scenario) {
    return {
      error: `Scenario not found: ${args.scenarioId}`,
      availableScenarios: testData.leaveScenarios.map(s => ({
        id: s.id,
        name: s.name,
        type: s.type,
        days: s.days
      }))
    };
  }
  
  return resolveTestData(scenario);
}

function get_environment_config(args) {
  const env = testData.testEnvironments.find(
    e => e.name === args.environment
  );
  
  if (!env) {
    return {
      error: `Environment not found: ${args.environment}`,
      availableEnvironments: testData.testEnvironments.map(e => ({
        name: e.name,
        url: e.url
      }))
    };
  }
  
  return env;
}

// Process tool calls
function processTool(toolName, args) {
  switch (toolName) {
    case 'get_test_user':
      return get_test_user(args);
    case 'list_test_users':
      return list_test_users(args);
    case 'get_leave_balance':
      return get_leave_balance(args);
    case 'get_leave_scenario':
      return get_leave_scenario(args);
    case 'get_environment_config':
      return get_environment_config(args);
    default:
      return { error: `Unknown tool: ${toolName}` };
  }
}

// Simple stdio-based MCP server
console.error('Test Data MCP Server started');
console.error('Available tools:', tools.map(t => t.name).join(', '));

// Read from stdin line by line (for testing/debugging)
process.stdin.on('data', (data) => {
  try {
    const lines = data.toString().trim().split('\n');
    lines.forEach(line => {
      if (!line) return;
      const request = JSON.parse(line);
      
      if (request.method === 'tools/list') {
        console.log(JSON.stringify({ jsonrpc: '2.0', result: { tools } }));
      } else if (request.method === 'tools/call') {
        const result = processTool(request.params.name, request.params.arguments);
        console.log(JSON.stringify({ jsonrpc: '2.0', result }));
      }
    });
  } catch (error) {
    console.error('Error processing request:', error.message);
  }
});

// Keep process alive
setTimeout(() => {}, 24 * 60 * 60 * 1000);
