package com.zohopeopleqa.tests;

import com.zohopeopleqa.base.BaseTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.*;

@Epic("Zoho People QA")
@Feature("Settings Page")
public class SettingsPageTest extends BaseTest {

    @TmsLink("ZP-039")
    @Test(priority = 1, description = "[ZP-039] Verify Settings (Admin) gear icon is visible in navigation")
    @Story("Settings Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Settings/Admin gear icon (#zp_maintab_admin) is visible in the top navigation bar")
    public void verifySettingsAccessible() {
        System.out.println("Verifying Settings/Admin gear icon is accessible...");

        // The Admin/Settings gear icon is #zp_maintab_admin (title="Settings") in the top nav.
        // NOTE: For admin accounts Settings is the gear icon, NOT an option in the profile dropdown.
        page.waitForSelector("#zp_maintab_admin",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#zp_maintab_admin");
        String locatorUsed = "#zp_maintab_admin (gear icon — Settings/Admin tab, top navigation)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Settings/Admin gear icon (#zp_maintab_admin) not visible in navigation");

        takeElementScreenshot("#zp_maintab_admin", "settings_option_visible");
        System.out.println("✅ Test PASSED: Settings/Admin gear icon is accessible");
    }

    @TmsLink("ZP-040")
    @Test(priority = 2, description = "[ZP-040] Verify navigating to Settings/Admin page")
    @Story("Settings Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Admin/Settings gear icon and validates the page navigates to the admin/settings area")
    public void verifySettingsPageLoads() {
        System.out.println("Navigating to Settings/Admin page...");

        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));

        // Click the Admin/Settings gear icon to navigate to the settings area
        page.click("#zp_maintab_admin");
        System.out.println("Clicked Admin/Settings gear icon");

        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(15000));

        String url = page.url();
        boolean isOnSettingsPage = url.contains("admin") || url.contains("setting") || url.contains("Setting");
        String locatorUsed = "#zp_maintab_admin → page.url() contains 'admin' or 'setting'";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Current URL", url);
        Assert.assertTrue(isOnSettingsPage, "Did not navigate to Settings/Admin page. URL: " + url);

        takeScreenshotOnSuccess("settings_page_loaded");
        System.out.println("✅ Test PASSED: Settings/Admin page loaded. URL: " + url);
    }

    @TmsLink("ZP-041")
    @Test(priority = 3, description = "[ZP-041] Verify Settings/Admin page has visible content")
    @Story("Settings Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Settings/Admin page displays visible content after navigation")
    public void verifySettingsSectionsVisible() {
        System.out.println("Verifying settings/admin page content is visible...");

        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));

        // Navigate to admin/settings page (re-click to ensure we're there)
        page.click("#zp_maintab_admin");
        System.out.println("Clicked Admin/Settings gear icon");

        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Verify we landed on the admin/settings page
        String url = page.url();
        boolean isOnAdminPage = url.contains("admin") || url.contains("setting") || url.contains("Setting");
        Allure.parameter("Current URL", url);
        Assert.assertTrue(isOnAdminPage, "Did not navigate to admin/settings page. URL: " + url);

        // Verify the page has visible content — the #page-wrapper is the main content container
        // present on all Zoho People pages after navigation
        page.waitForSelector("#page-wrapper",
                new Page.WaitForSelectorOptions().setTimeout(10000));
        boolean hasContent = page.isVisible("#page-wrapper");
        String locatorUsed = "#page-wrapper (main content area visible after admin navigation)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(hasContent, "Admin/Settings page main content (#page-wrapper) is not visible");

        takeScreenshotOnSuccess("settings_sections_visible");
        System.out.println("✅ Test PASSED: Settings/Admin page is visible and has content");
    }

    @TmsLink("ZP-042")
    @Test(priority = 4, description = "[ZP-042] Verify org name is displayed in the Settings page header")
    @Story("Settings Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the organisation name heading is displayed in the Settings page header")
    public void verifyOrgNameInSettingsHeader() {
        System.out.println("Verifying org name is displayed in Settings header...");

        // Navigate to Settings and wait for the page to fully load
        page.click("#zp_maintab_admin");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector("#servicPageContainer", new Page.WaitForSelectorOptions().setTimeout(15000));

        // Use CSS locator without has-text to avoid apostrophe escaping issues
        // The first h5 inside the settings header area holds the org name
        page.waitForSelector("#servicPageContainer h5", new Page.WaitForSelectorOptions().setTimeout(15000));

        String actualText = page.locator("#servicPageContainer h5").first().innerText().trim();
        String locatorUsed = "#servicPageContainer h5 (first h5 — org name heading, Settings header)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Org name text", actualText);
        Assert.assertFalse(actualText.isEmpty(),
                "Org name heading should not be empty");

        takeElementScreenshot("#servicPageContainer h5", "org_name_settings_header");
        System.out.println("✅ Test PASSED: Org name '" + actualText + "' is displayed");
    }

    @TmsLink("ZP-043")
    @Test(priority = 5, description = "[ZP-043] Verify User License Usage widget is visible in Settings")
    @Story("Settings Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the User License Usage widget is visible in the Settings page header section")
    public void verifyUserLicenseUsageWidget() {
        System.out.println("Verifying User License Usage widget is visible...");

        // Confirmed via DOM inspection: em element with text "User License Usage" in settings header
        page.waitForSelector("em:has-text('User License Usage')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("em:has-text('User License Usage')");
        String actualText = page.locator("em:has-text('User License Usage')").innerText().trim();
        String locatorUsed = "em:has-text('User License Usage') (license widget label, Settings)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Widget label", actualText);
        Assert.assertTrue(isVisible, "User License Usage widget not visible");
        Assert.assertEquals(actualText, "User License Usage",
                "User License Usage label text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("em:has-text('User License Usage')", "user_license_usage_widget");
        System.out.println("✅ Test PASSED: User License Usage widget is visible");
    }

    @TmsLink("ZP-044")
    @Test(priority = 6, description = "[ZP-044] Verify Manage Accounts service tile navigates correctly")
    @Story("Settings Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Manage Accounts tile and validates the sub-page loads with the expected heading")
    public void verifyManageAccountsTile() {
        System.out.println("Verifying Manage Accounts service tile...");

        // Ensure we are on the Settings page
        page.click("#zp_maintab_admin");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Confirmed via DOM inspection: h5 "Manage Accounts" inside #servicPageContainer
        page.waitForSelector("#servicPageContainer h5:has-text('Manage Accounts')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#servicPageContainer h5:has-text('Manage Accounts')").innerText().trim();
        String locatorUsed = "#servicPageContainer h5:has-text('Manage Accounts') (service tile heading)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Tile heading", actualText);
        Assert.assertEquals(actualText, "Manage Accounts",
                "Manage Accounts tile text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#servicPageContainer", "manage_accounts_tile");

        // Click the tile and validate the sub-page URL
        System.out.println("Clicking Manage Accounts tile to validate navigation...");
        page.locator("#servicPageContainer h5:has-text('Manage Accounts')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        String subUrl = page.url();
        System.out.println("URL after clicking Manage Accounts tile: " + subUrl);
        Allure.parameter("Manage Accounts sub-page URL", subUrl);
        Assert.assertTrue(
                subUrl.contains("account") || subUrl.contains("manage") || subUrl.contains("admin"),
                "URL after clicking Manage Accounts tile should contain 'account', 'manage', or 'admin'. Got: '" + subUrl + "'");
        takeScreenshotOnSuccess("manage_accounts_subpage");

        // Return to Settings
        page.click("#zp_maintab_admin");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector("#servicPageContainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Manage Accounts tile present, sub-page loaded, returned to Settings");
    }

    @TmsLink("ZP-045")
    @Test(priority = 7, description = "[ZP-045] Verify Leave Tracker settings tile navigates to Leave configuration")
    @Story("Settings Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Leave Tracker tile and validates the Leave admin settings sub-page loads")
    public void verifyLeaveTrackerServiceTile() {
        System.out.println("Verifying Leave Tracker service tile...");

        // Ensure we are on the Settings page
        page.click("#zp_maintab_admin");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Confirmed via DOM inspection: h5 "Leave Tracker" inside #servicPageContainer
        page.waitForSelector("#servicPageContainer h5:has-text('Leave Tracker')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#servicPageContainer h5:has-text('Leave Tracker')").innerText().trim();
        String locatorUsed = "#servicPageContainer h5:has-text('Leave Tracker') (service tile heading)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Tile heading", actualText);
        Assert.assertEquals(actualText, "Leave Tracker",
                "Leave Tracker tile text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#servicPageContainer", "leave_tracker_service_tile");

        // Click the tile and validate the sub-page URL contains leave-related path
        System.out.println("Clicking Leave Tracker settings tile to validate navigation...");
        page.locator("#servicPageContainer h5:has-text('Leave Tracker')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        String subUrl = page.url();
        System.out.println("URL after clicking Leave Tracker settings tile: " + subUrl);
        Allure.parameter("Leave Tracker settings sub-page URL", subUrl);
        Assert.assertTrue(
                subUrl.contains("leave") || subUrl.contains("Leave"),
                "URL after clicking Leave Tracker settings tile should contain 'leave'. Got: '" + subUrl + "'");
        takeScreenshotOnSuccess("leave_tracker_settings_subpage");

        // Return to Settings
        page.click("#zp_maintab_admin");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector("#servicPageContainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Leave Tracker settings tile present, leave config sub-page loaded, returned to Settings");
    }

    @TmsLink("ZP-046")
    @Test(priority = 8, description = "[ZP-046] Verify Employee Information settings tile navigates to employee configuration")
    @Story("Settings Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Employee Information tile and validates the admin configuration sub-page loads")
    public void verifyEmployeeInformationTile() {
        System.out.println("Verifying Employee Information service tile...");

        // Ensure we are on the Settings page
        page.click("#zp_maintab_admin");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Confirmed via DOM inspection: h5 "Employee Information" inside #servicPageContainer
        page.waitForSelector("#servicPageContainer h5:has-text('Employee Information')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#servicPageContainer h5:has-text('Employee Information')").innerText().trim();
        String locatorUsed = "#servicPageContainer h5:has-text('Employee Information') (service tile heading)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Tile heading", actualText);
        Assert.assertEquals(actualText, "Employee Information",
                "Employee Information tile text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#servicPageContainer", "employee_info_tile");

        // Click the tile and validate the sub-page URL
        System.out.println("Clicking Employee Information tile to validate navigation...");
        page.locator("#servicPageContainer h5:has-text('Employee Information')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        String subUrl = page.url();
        System.out.println("URL after clicking Employee Information tile: " + subUrl);
        Allure.parameter("Employee Information sub-page URL", subUrl);
        Assert.assertTrue(
                subUrl.contains("employee") || subUrl.contains("Employee") || subUrl.contains("info")
                        || subUrl.contains("organization") || subUrl.contains("settings/service"),
                "URL should navigate to employee/organization settings after clicking Employee Information tile. Got: '" + subUrl + "'");
        takeScreenshotOnSuccess("employee_info_settings_subpage");

        // Return to Settings
        page.click("#zp_maintab_admin");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector("#servicPageContainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Employee Information tile present, config sub-page loaded, returned to Settings");
    }

    @TmsLink("ZP-047")
    @Test(priority = 9, description = "[ZP-047] Verify Attendance settings tile navigates to Attendance configuration")
    @Story("Settings Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Attendance tile and validates the Attendance admin configuration sub-page loads")
    public void verifyAttendanceTile() {
        System.out.println("Verifying Attendance service tile...");

        // Ensure we are on the Settings page
        page.click("#zp_maintab_admin");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Confirmed via DOM inspection: h5 "Attendance" inside #servicPageContainer
        page.waitForSelector("#servicPageContainer h5:has-text('Attendance')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#servicPageContainer h5:has-text('Attendance')").innerText().trim();
        String locatorUsed = "#servicPageContainer h5:has-text('Attendance') (service tile heading)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Tile heading", actualText);
        Assert.assertEquals(actualText, "Attendance",
                "Attendance tile text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#servicPageContainer", "attendance_tile");

        // Click the tile and validate the sub-page URL
        System.out.println("Clicking Attendance tile to validate navigation...");
        page.locator("#servicPageContainer h5:has-text('Attendance')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        String subUrl = page.url();
        System.out.println("URL after clicking Attendance tile: " + subUrl);
        Allure.parameter("Attendance settings sub-page URL", subUrl);
        Assert.assertTrue(
                subUrl.contains("attendance") || subUrl.contains("Attendance"),
                "URL should contain 'attendance' after clicking Attendance tile. Got: '" + subUrl + "'");
        takeScreenshotOnSuccess("attendance_settings_subpage");

        // Return to Settings
        page.click("#zp_maintab_admin");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector("#servicPageContainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Attendance tile present, attendance config sub-page loaded, returned to Settings");
    }

    @TmsLink("ZP-048")
    @Test(priority = 10, description = "[ZP-048] Verify Settings services grid has more than 5 tiles")
    @Story("Settings Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Settings services grid contains more than 5 service tile headings")
    public void verifyServiceGridHasMultipleTiles() {
        System.out.println("Verifying Settings services grid has more than 5 tiles...");

        // Confirmed via DOM inspection: #servicPageContainer contains 20+ h5 service tile headings
        page.waitForSelector("#servicPageContainer h5",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        int tileCount = page.locator("#servicPageContainer h5").count();
        String locatorUsed = "#servicPageContainer h5 (count — service tile headings)";
        System.out.println("[Locator] " + locatorUsed);
        System.out.println("Found " + tileCount + " service tiles");
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Tile count", String.valueOf(tileCount));
        Assert.assertTrue(tileCount > 5,
                "Expected more than 5 service tiles in Settings grid, but found: " + tileCount);

        takeElementScreenshot("#servicPageContainer", "service_grid_tiles");
        System.out.println("✅ Test PASSED: Settings services grid has " + tileCount + " tiles (> 5)");
    }

}
