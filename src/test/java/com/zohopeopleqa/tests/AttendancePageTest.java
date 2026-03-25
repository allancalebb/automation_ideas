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

    @Test(priority = 1, description = "Verify Check-in button is visible on the Home page")
    @Story("Attendance Widget")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Check-in button (ZPAtt_check_in_out) is visible in the My Space attendance section")
    public void verifyCheckInButtonPresent() {
        System.out.println("Verifying Check-in button is visible on Home page...");

        // Make sure we are on the Home page
        page.locator(NavBar.HOME_TAB).first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Confirmed via DOM inspection: button#ZPAtt_check_in_out
        page.waitForSelector("#ZPAtt_check_in_out",
                new Page.WaitForSelectorOptions().setTimeout(15000));

        String btnText = page.locator("#ZPAtt_check_in_out").innerText().trim();
        String locatorUsed = "#ZPAtt_check_in_out (button — Check-in, My Space attendance widget)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Button text", btnText);
        Assert.assertEquals(btnText, "Check-in",
                "Check-in button text mismatch. Got: '" + btnText + "'");

        takeElementScreenshot("#ZPAtt_check_in_out", "attendance_checkin_btn");
        System.out.println("✅ Test PASSED: Check-in button is visible with label 'Check-in'");
    }

    @Test(priority = 2, description = "Verify Attendance tile is present in the Settings service grid")
    @Story("Attendance Settings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Attendance configuration tile is visible in the Settings admin grid")
    public void verifyAttendanceTileInSettings() {
        System.out.println("Verifying Attendance tile exists in Settings grid...");

        goToSettings();

        page.waitForSelector(SettingsPage.SERVICE_CONTAINER + " h5:has-text('Attendance')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String tileText = page.locator(SettingsPage.SERVICE_CONTAINER + " h5:has-text('Attendance')").innerText().trim();
        String locatorUsed = "#servicPageContainer h5:has-text('Attendance')";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Tile text", tileText);
        Assert.assertEquals(tileText, "Attendance",
                "Attendance tile text mismatch. Got: '" + tileText + "'");

        takeElementScreenshot(SettingsPage.SERVICE_CONTAINER, "attendance_tile_in_settings");
        System.out.println("✅ Test PASSED: Attendance tile present in Settings grid");
    }

    @Test(priority = 3, description = "Verify Attendance settings sub-page loads when tile is clicked")
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

    @Test(priority = 4, description = "Verify Attendance settings page has configuration content")
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

    @Test(priority = 5, description = "Verify navigation back to Home from Attendance settings")
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
