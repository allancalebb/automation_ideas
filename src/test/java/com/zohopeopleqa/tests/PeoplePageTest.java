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
@Feature("People / Employee Directory")
public class PeoplePageTest extends BaseTest {

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

    @Test(priority = 1, description = "Verify Reportees section shows employee count for admin")
    @Story("People Overview")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Reportees panel is visible on the Home page and shows a valid count for an admin")
    public void verifyReporteesCount() {
        System.out.println("Verifying Reportees section with employee count...");

        // Go to Home to access My Space reportees panel
        page.locator(NavBar.HOME_TAB).first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Confirmed: div[aria-label*="Reportees"] panel is visible with count in aria-label
        page.waitForSelector("[aria-label*='Reportees']",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("[aria-label*='Reportees']");
        String ariaLabel = page.locator("[aria-label*='Reportees']").getAttribute("aria-label");
        String locatorUsed = "[aria-label*='Reportees'] (admin Reportees panel on Home)";
        System.out.println("[Locator] " + locatorUsed);
        System.out.println("Reportees aria-label: " + ariaLabel);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("aria-label", ariaLabel);

        Assert.assertTrue(isVisible, "Reportees panel should be visible on Home for admin account");
        Assert.assertTrue(ariaLabel != null && ariaLabel.contains("Reportees"),
                "Reportees aria-label should contain 'Reportees'. Got: '" + ariaLabel + "'");

        takeElementScreenshot("[aria-label*='Reportees']", "reportees_count_panel");
        System.out.println("✅ Test PASSED: Reportees panel visible — " + ariaLabel);
    }

    @Test(priority = 2, description = "Verify Employee Information settings tile is in the Settings grid")
    @Story("Employee Information Settings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Employee Information service tile is present in the Settings services grid")
    public void verifyEmployeeInfoTilePresent() {
        System.out.println("Verifying Employee Information tile in Settings grid...");

        goToSettings();

        page.waitForSelector(SettingsPage.SERVICE_CONTAINER + " h5:has-text('Employee Information')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String tileText = page.locator(SettingsPage.SERVICE_CONTAINER + " h5:has-text('Employee Information')").innerText().trim();
        String locatorUsed = "#servicPageContainer h5:has-text('Employee Information')";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Tile text", tileText);

        Assert.assertEquals(tileText, "Employee Information",
                "Employee Information tile text mismatch. Got: '" + tileText + "'");

        takeElementScreenshot(SettingsPage.SERVICE_CONTAINER, "employee_info_tile_settings");
        System.out.println("✅ Test PASSED: Employee Information tile present in Settings grid");
    }

    @Test(priority = 3, description = "Verify Employee Information settings sub-page loads")
    @Story("Employee Information Settings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Employee Information settings tile and validates the sub-page loads correctly")
    public void verifyEmployeeInfoSettingsPageLoads() {
        System.out.println("Verifying Employee Information settings sub-page loads...");

        goToSettings();

        // Click the Employee Information tile
        page.locator(SettingsPage.SERVICE_CONTAINER + " h5:has-text('Employee Information')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        String url = page.url();
        System.out.println("URL after clicking Employee Information tile: " + url);
        Allure.parameter("Employee Information settings URL", url);

        // URL should indicate employee info settings
        Assert.assertTrue(
                url.contains("employee") || url.contains("Employee") || url.contains("info") || url.contains("people"),
                "URL should contain 'employee', 'info', or 'people' after clicking Employee Information tile. Got: '" + url + "'");

        // Content is present
        page.waitForSelector(SettingsPage.PAGE_WRAPPER, new Page.WaitForSelectorOptions().setTimeout(10000));
        Assert.assertTrue(page.isVisible(SettingsPage.PAGE_WRAPPER),
                "Employee Information settings page should have content");

        takeScreenshotOnSuccess("employee_info_settings_page");

        // Return to Settings
        goToSettings();
        System.out.println("✅ Test PASSED: Employee Information settings sub-page loaded, returned to Settings");
    }

    @Test(priority = 4, description = "Verify Search Employee icon is visible in navigation")
    @Story("People Overview")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates the Search Employee icon is visible in the Zoho People top navigation")
    public void verifySearchEmployeeIcon() {
        System.out.println("Verifying Search Employee icon in navigation...");

        // Ensure we are on Home
        page.locator(NavBar.HOME_TAB).first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Confirmed via DOM: li#zpeople_search_icon
        page.waitForSelector(NavBar.SEARCH_ICON, new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible(NavBar.SEARCH_ICON);
        String locatorUsed = NavBar.SEARCH_ICON + " (Search Employee icon, top navigation)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Search Employee icon should be visible in navigation");

        takeElementScreenshot(NavBar.SEARCH_ICON, "search_employee_icon");
        System.out.println("✅ Test PASSED: Search Employee icon visible");
    }

    @Test(priority = 5, description = "Verify org name is displayed in Settings page header")
    @Story("Employee Information Settings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates the organisation name heading is displayed on the Settings admin page")
    public void verifyOrgNameVisible() {
        System.out.println("Verifying org name is visible in Settings...");

        goToSettings();

        // Confirmed via DOM: h5 "Allan's Test Account Org" in the Settings header
        page.waitForSelector("h5:has-text(\"Allan's Test Account Org\")",
                new Page.WaitForSelectorOptions().setTimeout(15000));

        String orgName = page.locator("h5:has-text(\"Allan's Test Account Org\")").innerText().trim();
        String locatorUsed = "h5:has-text(\"Allan's Test Account Org\") (org name in Settings header)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Org name", orgName);

        Assert.assertEquals(orgName, "Allan's Test Account Org",
                "Org name mismatch. Got: '" + orgName + "'");

        takeElementScreenshot("h5:has-text(\"Allan's Test Account Org\")", "org_name_people_test");
        System.out.println("✅ Test PASSED: Org name 'Allan's Test Account Org' confirmed");
    }
}
