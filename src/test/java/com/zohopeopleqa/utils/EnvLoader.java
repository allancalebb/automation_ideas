package com.zohopeopleqa.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to load environment variables from .env file or system environment
 * Priority: .env file > System environment variables > null
 * Uses direct file reading instead of libraries to avoid parsing issues
 */
public class EnvLoader {
    
    private static Map<String, String> envVars = new HashMap<>();
    
    static {
        loadEnvFile();
    }
    
    /**
     * Load .env file directly
     */
    private static void loadEnvFile() {
        try {
            if (Files.exists(Paths.get(".env"))) {
                // Read file line by line
                Files.readAllLines(Paths.get(".env")).forEach(line -> {
                    // Skip empty lines and comments
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        return;
                    }
                    
                    // Parse KEY=VALUE
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();
                        
                        // Remove quotes if present (handles "value" or 'value')
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        
                        envVars.put(key, value);
                        System.out.println("✅ Loaded '" + key + "' from .env file (length: " + value.length() + ")");
                    }
                });
                
                System.out.println("✅ .env file loaded successfully (" + envVars.size() + " variables)");
            } else {
                System.out.println("⚠️ .env file not found, using system environment variables");
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading .env file: " + e.getMessage());
            System.out.println("⚠️ Falling back to system environment variables");
        }
    }
    
    /**
     * Get environment variable from .env file or system environment
     * @param key The environment variable name
     * @return The value, or null if not found
     */
    public static String get(String key) {
        String value = null;
        
        // Priority 1: Try to get from .env file (already loaded)
        value = envVars.get(key);
        if (value != null) {
            System.out.println("✅ Using '" + key + "' from .env file (length: " + value.length() + ", value: " + value + ")");
            return value;
        }
        
        // Priority 2: Try system environment variables
        value = System.getenv(key);
        if (value != null) {
            System.out.println("✅ Using '" + key + "' from system environment (length: " + value.length() + ")");
            return value;
        }
        
        // Priority 3: Not found
        System.err.println("❌ Environment variable '" + key + "' not found in .env or system environment");
        return null;
    }
    
    /**
     * Get environment variable with a default value
     * @param key The environment variable name
     * @param defaultValue Default value if not found
     * @return The value or default
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
}
