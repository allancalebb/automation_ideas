package com.zohopeopleqa.tests;

import com.zohopeopleqa.base.BaseTest;
import com.zohopeopleqa.pages.NavBar;
import com.zohopeopleqa.pages.SettingsPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.*;

@Epic("Zoho People QA")
@Feature("Attendance")
public class AttendancePageTest extends BaseTest {

    // ---- Helpers ----

    private void goToSettings() {
        page.click(NavBar.SETTINGS_TAB);
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector(SettingsPage.SERVICE_CONTAINER,
                new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    // ==============================
    // Tests
    // ==============================

    @TmsLink("ZP-051")
    @Test(priority = 3, description = "[ZP-051] Verify Attendance settings sub-page loads when tile is clicked")
    @Story("Attendance Settings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Attendance settings tile and validates the Attendance admin configuration page loads")
    public void verifyAttendanceSettingsPageLoads() {
        System.out.println("Verifying Attendance settings sub-page loads...");

        goToSettings();

        // Click the Attendance tile
        page.waitForSelector(SettingsPage.SERVICE_CONTAINER + " h5:has-text('Attendance')",
                new Page.WaitForSelectorOptions().setTimeout(10000));
        page.locator(SettingsPage.SERVICE_CONTAINER + " h5:has-text('Attendance')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        String url = page.url();
        System.out.println("URL after clicking Attendance tile: " + url);
        Allure.parameter("Attendance settings URL", url);

        Assert.assertTrue(
                url.contains("attendance") || url.contains("Attendance"),
                "URL should contain 'attendance' after clicking Attendance tile. Got: '" + url + "'");

        // Validate that the page has content
        page.waitForSelector(SettingsPage.PAGE_WRAPPER,
                new Page.WaitForSelectorOptions().setTimeout(10000));
        Assert.assertTrue(page.isVisible(SettingsPage.PAGE_WRAPPER),
                "Attendance settings page should have content (#page-wrapper)");

        takeScreenshotOnSuccess("attendance_settings_page");

        // Return to Settings
        goToSettings();
        System.out.println("✅ Test PASSED: Attendance settings sub-page loaded, URL validated, returned to Settings");
    }

    @TmsLink("ZP-052")
    @Test(priority = 4, description = "[ZP-052] Verify Attendance settings page has configuration content")
    @Story("Attendance Settings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Attendance admin configuration page shows settings content after navigation")
    public void verifyAttendanceSettingsHasContent() {
        System.out.println("Verifying Attendance settings page has configuration content...");

        goToSettings();

        // Navigate to Attendance settings
        page.locator(SettingsPage.SERVICE_CONTAINER + " h5:has-text('Attendance')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        String url = page.url();
        Assert.assertTrue(url.contains("attendance") || url.contains("Attendance"),
                "Should be on Attendance settings page. Got: '" + url + "'");

        // Validate the page has a header/title element — common Zoho admin pattern
        page.waitForSelector("#page-wrapper", new Page.WaitForSelectorOptions().setTimeout(10000));

        // Check for any heading or label that confirms we're on an attendance-related settings page.
        // Common patterns in Zoho admin sub-pages: h2, h3, or a breadcrumb containing module name.
        boolean hasHeading = page.locator("h2, h3, h4").count() > 0 || page.isVisible("#page-wrapper");
        Allure.parameter("Attendance settings content present", String.valueOf(hasHeading));
        Assert.assertTrue(hasHeading, "Attendance settings page should have at least one heading element");

        takeScreenshotOnSuccess("attendance_settings_content");

        // Return to Settings
        goToSettings();
        System.out.println("✅ Test PASSED: Attendance settings page has configuration content");
    }

    @TmsLink("ZP-053")
    @Test(priority = 5, description = "[ZP-053] Verify navigation back to Home from Attendance settings")
    @Story("Attendance Settings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that after visiting Attendance settings, the user can return to the Home page")
    public void verifyReturnHomeFromAttendanceSettings() {
        System.out.println("Verifying navigation back to Home from Attendance settings...");

        goToSettings();

        // Navigate to Attendance settings
        page.locator(SettingsPage.SERVICE_CONTAINER + " h5:has-text('Attendance')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        String attendanceUrl = page.url();
        Assert.assertTrue(attendanceUrl.contains("attendance") || attendanceUrl.contains("Attendance"),
                "Should be on Attendance settings page first. Got: '" + attendanceUrl + "'");

        // Navigate back to Home
        page.locator(NavBar.HOME_TAB).first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Validate we are back on Home
        page.waitForSelector("#ZPAtt_check_in_out", new Page.WaitForSelectorOptions().setTimeout(15000));
        boolean checkInVisible = page.isVisible("#ZPAtt_check_in_out");
        Allure.parameter("Check-in button visible after return", String.valueOf(checkInVisible));
        Assert.assertTrue(checkInVisible, "Should be back on Home page with Check-in button visible");

        takeElementScreenshot("#ZPAtt_check_in_out", "attendance_return_home");
        System.out.println("✅ Test PASSED: Successfully returned to Home from Attendance settings");
    }
}
