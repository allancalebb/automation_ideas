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

    @Test(priority = 1, description = "Verify Settings (Admin) gear icon is visible in navigation")
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

    @Test(priority = 2, description = "Verify navigating to Settings/Admin page")
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

    @Test(priority = 3, description = "Verify Settings/Admin page has visible content")
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

    @Test(priority = 4, description = "Verify org name is displayed in the Settings page header")
    @Story("Settings Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the organisation name heading is displayed in the Settings page header")
    public void verifyOrgNameInSettingsHeader() {
        System.out.println("Verifying org name is displayed in Settings header...");

        // Confirmed via DOM inspection: h5 with text "Allan's Test Account Org" inside settings page header
        page.waitForSelector("h5:has-text(\"Allan's Test Account Org\")",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("h5:has-text(\"Allan's Test Account Org\")").innerText().trim();
        String locatorUsed = "h5:has-text(\"Allan's Test Account Org\") (org name heading, Settings header)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Org name text", actualText);
        Assert.assertEquals(actualText, "Allan's Test Account Org",
                "Org name heading text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("h5:has-text(\"Allan's Test Account Org\")", "org_name_settings_header");
        System.out.println("✅ Test PASSED: Org name 'Allan's Test Account Org' is displayed");
    }

    @Test(priority = 5, description = "Verify User License Usage widget is visible in Settings")
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

    @Test(priority = 6, description = "Verify Manage Accounts service tile is visible in Settings")
    @Story("Settings Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Manage Accounts service tile is visible in the Settings services grid")
    public void verifyManageAccountsTile() {
        System.out.println("Verifying Manage Accounts service tile is visible...");

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
        System.out.println("✅ Test PASSED: Manage Accounts service tile is visible");
    }

    @Test(priority = 7, description = "Verify Leave Tracker service tile is visible in Settings")
    @Story("Settings Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Leave Tracker service tile is visible in the Settings services grid")
    public void verifyLeaveTrackerServiceTile() {
        System.out.println("Verifying Leave Tracker service tile is visible...");

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
        System.out.println("✅ Test PASSED: Leave Tracker service tile is visible");
    }

    @Test(priority = 8, description = "Verify Employee Information service tile is visible in Settings")
    @Story("Settings Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Employee Information service tile is visible in the Settings services grid")
    public void verifyEmployeeInformationTile() {
        System.out.println("Verifying Employee Information service tile is visible...");

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
        System.out.println("✅ Test PASSED: Employee Information service tile is visible");
    }

    @Test(priority = 9, description = "Verify Attendance service tile is visible in Settings")
    @Story("Settings Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Attendance service tile is visible in the Settings services grid")
    public void verifyAttendanceTile() {
        System.out.println("Verifying Attendance service tile is visible...");

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
        System.out.println("✅ Test PASSED: Attendance service tile is visible");
    }

    @Test(priority = 10, description = "Verify Settings services grid has more than 5 tiles")
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
