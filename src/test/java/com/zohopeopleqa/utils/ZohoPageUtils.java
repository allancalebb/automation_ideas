package com.zohopeopleqa.utils;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Utility class for common Zoho People page interactions used across multiple test classes.
 *
 * Usage:
 *   ZohoPageUtils.waitForDashboard(page);
 *   ZohoPageUtils.navigateTo(page, "Leave Tracker");
 *   ZohoPageUtils.assertCurrentUrlContains(page, "leave");
 */
public class ZohoPageUtils {

    private static final String BASE_URL = "https://people.zoho.com";

    // ─── Navigation ──────────────────────────────────────────────────────────

    /**
     * Waits for the Zoho People dashboard to be fully loaded and the URL to be correct.
     */
    public static void waitForDashboard(Page page) {
        page.waitForURL(
                url -> url.startsWith(BASE_URL + "/") &&
                       (url.contains("/zp") || url.contains("/home") || url.contains("/dashboard")),
                new Page.WaitForURLOptions().setTimeout(15000)
        );
        waitForNetworkIdle(page);
    }

    /**
     * Navigates to the Zoho People home dashboard URL directly.
     */
    public static void goToDashboard(Page page) {
        page.navigate(BASE_URL);
        waitForDashboard(page);
    }

    /**
     * Waits for network idle with a standard 15s timeout.
     */
    public static void waitForNetworkIdle(Page page) {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(15000));
        } catch (Exception ignored) {}
    }

    // ─── URL Assertions ───────────────────────────────────────────────────────

    /**
     * Returns true if the current URL contains the given fragment (case-insensitive).
     * Use with Assert.assertTrue() in tests.
     */
    public static boolean currentUrlContains(Page page, String fragment) {
        return page.url().toLowerCase().contains(fragment.toLowerCase());
    }

    /**
     * Returns the current page URL.
     */
    public static String currentUrl(Page page) {
        return page.url();
    }

    // ─── Element Helpers ─────────────────────────────────────────────────────

    /**
     * Clicks an element and waits for network to settle afterward.
     */
    public static void clickAndWait(Page page, String selector) {
        page.click(selector);
        waitForNetworkIdle(page);
    }

    /**
     * Waits for a selector to be visible, then returns true/false.
     * Times out gracefully with false instead of throwing.
     */
    public static boolean isVisibleAfterWait(Page page, String selector, int timeoutMs) {
        try {
            page.waitForSelector(selector,
                    new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
            return page.isVisible(selector);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads the innerText of a visible element. Returns empty string if not found.
     */
    public static String getText(Page page, String selector) {
        try {
            return page.locator(selector).first().innerText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Reads an attribute of an element. Returns empty string if not found.
     */
    public static String getAttribute(Page page, String selector, String attribute) {
        try {
            String val = page.locator(selector).first().getAttribute(attribute);
            return val != null ? val.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    // ─── Common Zoho UI Patterns ─────────────────────────────────────────────

    /**
     * Opens the profile avatar dropdown menu (top-right corner of Zoho People).
     */
    public static void openProfileDropdown(Page page) {
        page.waitForSelector(".zpl_Nvpicimg, li.zpl_Nvpicimg",
                new Page.WaitForSelectorOptions().setTimeout(10000));
        page.click(".zpl_Nvpicimg, li.zpl_Nvpicimg");
        page.waitForTimeout(800);
    }

    /**
     * Closes any open dropdown or modal by pressing Escape.
     */
    public static void pressEscape(Page page) {
        page.keyboard().press("Escape");
        page.waitForTimeout(300);
    }

    /**
     * Returns the display name of the currently logged-in user from the dashboard.
     * Uses the confirmed DOM selector: <b class="zpl_link">Name</b>
     */
    public static String getLoggedInUserName(Page page) {
        return getText(page, ".zpl_link");
    }
}
