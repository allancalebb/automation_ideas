package com.zohopeopleqa.config;

import com.zohopeopleqa.utils.EnvLoader;

/**
 * Central configuration for test environment targeting.
 *
 * Set ZOHO_ENV in your .env file (or as a system/CI env var):
 *   ZOHO_ENV=live   → https://people.zoho.com     (production, default)
 *   ZOHO_ENV=test   → https://peoplelabs.zoho.com  (test / labs environment)
 *
 * All test files and BaseTest reference Config.BASE_URL / Config.BASE_DOMAIN
 * so you only need to change ZOHO_ENV in one place to switch environments.
 */
public class Config {

    /** The active environment name: "live" or "test" */
    public static final String ENV;

    /** Root domain (no trailing slash), e.g. "people.zoho.com" or "peoplelabs.zoho.com" */
    public static final String BASE_DOMAIN;

    /** Full base URL (no trailing slash), e.g. "https://people.zoho.com" */
    public static final String BASE_URL;

    static {
        String env = EnvLoader.get("ZOHO_ENV", "live").trim().toLowerCase();
        ENV = env;

        switch (env) {
            case "test":
                BASE_DOMAIN = "peoplelabs.zoho.com";
                break;
            case "live":
            default:
                BASE_DOMAIN = "people.zoho.com";
                break;
        }

        BASE_URL = "https://" + BASE_DOMAIN;
        System.out.println("[Config] Environment: " + ENV.toUpperCase()
                + " → " + BASE_URL);
    }

    private Config() {}
}
