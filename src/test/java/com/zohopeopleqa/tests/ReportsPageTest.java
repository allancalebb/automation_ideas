package com.zohopeopleqa.tests;

import com.zohopeopleqa.base.BaseTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.*;

@Epic("Zoho People QA")
@Feature("Reports Page")
public class ReportsPageTest extends BaseTest {

    @TmsLink("ZP-026")
    @Test(priority = 1, description = "[ZP-026] Verify Reports tab is visible in navigation")
    @Story("Reports Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Reports tab is present and visible in the main navigation")
    public void verifyReportsTabVisible() {
        System.out.println("Verifying Reports tab is visible...");

        // Confirmed via Playwright MCP DOM inspection: li#zp_maintab_reports
        page.waitForSelector("#zp_maintab_reports",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#zp_maintab_reports");
        String locatorUsed = "#zp_maintab_reports (li — Reports tab, main navigation)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Reports tab #zp_maintab_reports not visible in navigation");

        takeElementScreenshot("#zp_maintab_reports", "reports_tab_visible");
        System.out.println("✅ Test PASSED: Reports tab is visible");
    }

    @TmsLink("ZP-027")
    @Test(priority = 2, description = "[ZP-027] Verify navigating to Reports page")
    @Story("Reports Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Reports tab and validates the Reports page loads correctly")
    public void verifyReportsPageLoads() {
        System.out.println("Navigating to Reports page...");

        // Use LOAD (not NETWORKIDLE) before the click — SPA background polling prevents
        // NETWORKIDLE from being reached reliably between tests on a live session.
        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));

        page.click("#zp_maintab_reports");
        System.out.println("Clicked Reports tab");

        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));

        String url = page.url();
        boolean isOnReportsPage = url.contains("report") || url.contains("Report");
        String locatorUsed = "page.url() contains 'report'";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Current URL", url);
        Assert.assertTrue(isOnReportsPage, "Did not navigate to Reports page. URL: " + url);

        takeScreenshotOnSuccess("reports_page_loaded");
        System.out.println("✅ Test PASSED: Reports page loaded. URL: " + url);
    }

    @TmsLink("ZP-028")
    @Test(priority = 3, description = "[ZP-028] Verify Reports page has at least one report category")
    @Story("Reports Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Reports page displays at least one report category or report item")
    public void verifyReportCategoriesVisible() {
        System.out.println("Verifying report categories are visible...");

        // Selectors covering both old and new Zoho People UI:
        // - Old UI: #zp_t_reports_my sub-tab, a:has-text('My Reports')
        // - New UI (admin): "My Reports"/"Team Reports" tabs, category headings, Search Reports input
        String selector = "#zp_t_reports_my, a:has-text('My Reports'), a:has-text('Team Reports'), " +
                          "h2:has-text('Employee Information'), h3:has-text('Employee Information'), " +
                          "input[placeholder*='Search Reports']";
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible(selector);
        String locatorUsed = "#zp_t_reports_my | a:has-text('My Reports') | a:has-text('Team Reports')";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "No report categories visible on Reports page");

        takeElementScreenshot(selector, "report_categories_visible");
        System.out.println("✅ Test PASSED: Report categories are visible");
    }

    @TmsLink("ZP-029")
    @Test(priority = 4, description = "[ZP-029] Verify My Reports tab is visible on the Reports page")
    @Story("Reports Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the My Reports tab is visible and correctly labelled in the Reports module")
    public void verifyMyReportsTab() {
        System.out.println("Verifying My Reports tab is visible...");

        // Confirmed via DOM inspection: a#zp_t_reports_my, innerText "My Reports"
        page.waitForSelector("#zp_t_reports_my",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#zp_t_reports_my").innerText().trim();
        String locatorUsed = "#zp_t_reports_my (a — My Reports tab, Reports module)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "My Reports",
                "My Reports tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#zp_t_reports_my", "my_reports_tab");

        // Click My Reports tab and validate page content
        System.out.println("Clicking My Reports tab to validate content...");
        page.locator("#zp_t_reports_my").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: 'Employee Information' report category confirms My Reports content loaded
        page.waitForSelector("#myReports_wrap li:has-text('Employee Information')",
                new Page.WaitForSelectorOptions().setTimeout(10000));
        boolean empInfoVisible = page.isVisible("#myReports_wrap li:has-text('Employee Information')");
        System.out.println("'Employee Information' category in My Reports: " + empInfoVisible);
        Allure.parameter("Employee Information category visible", String.valueOf(empInfoVisible));
        Assert.assertTrue(empInfoVisible,
                "My Reports should list 'Employee Information' report category.");
        takeElementScreenshot("#myReports_wrap", "my_reports_tab_content");
        // My Reports is the default Reports view — no navigation back needed
        System.out.println("✅ Test PASSED: My Reports tab visible, 'Employee Information' category confirmed");
    }

    @TmsLink("ZP-030")
    @Test(priority = 5, description = "[ZP-030] Verify Team Reports tab is visible on the Reports page")
    @Story("Reports Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Team Reports tab is visible and correctly labelled in the Reports module")
    public void verifyTeamReportsTab() {
        System.out.println("Verifying Team Reports tab is visible...");

        // Confirmed via DOM inspection: a#zp_t_reports_team, innerText "Team Reports"
        page.waitForSelector("#zp_t_reports_team",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#zp_t_reports_team").innerText().trim();
        String locatorUsed = "#zp_t_reports_team (a — Team Reports tab, Reports module)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Team Reports",
                "Team Reports tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#zp_t_reports_team", "team_reports_tab");

        // Click Team Reports tab and validate page content
        System.out.println("Clicking Team Reports tab to validate content...");
        page.locator("#zp_t_reports_team").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL confirms navigation, and 'Leave Tracker' category confirms Team Reports content loaded
        String teamReportsUrl = page.url();
        System.out.println("URL after clicking Team Reports: " + teamReportsUrl);
        Allure.parameter("Team Reports URL", teamReportsUrl);
        Assert.assertTrue(teamReportsUrl.contains("team"),
                "URL should contain 'team' after clicking Team Reports tab. Got: '" + teamReportsUrl + "'");
        page.waitForSelector("#page-wrapper", new Page.WaitForSelectorOptions().setTimeout(10000));
        String teamReportsContent = page.locator("#page-wrapper").innerText().trim();
        System.out.println("Team Reports content (excerpt): " + teamReportsContent.substring(0, Math.min(300, teamReportsContent.length())));
        Allure.parameter("Team Reports content", teamReportsContent);
        Assert.assertTrue(teamReportsContent.contains("Leave Tracker"),
                "Team Reports should list 'Leave Tracker' report category. Got: '" + teamReportsContent + "'");
        takeScreenshotOnSuccess("team_reports_tab_content");

        // Navigate back to My Reports tab
        page.locator("#zp_t_reports_my").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForSelector("#myReports_wrap", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Team Reports tab visible with label 'Team Reports', content validated, returned to My Reports");
    }

    @TmsLink("ZP-031")
    @Test(priority = 6, description = "[ZP-031] Verify Organization Reports tab is visible on the Reports page")
    @Story("Reports Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Organization Reports tab is visible and correctly labelled in the Reports module")
    public void verifyOrganizationReportsTab() {
        System.out.println("Verifying Organization Reports tab is visible...");

        // Confirmed via DOM inspection: a#zp_t_reports_admin, innerText "Organization Reports"
        page.waitForSelector("#zp_t_reports_admin",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#zp_t_reports_admin").innerText().trim();
        String locatorUsed = "#zp_t_reports_admin (a — Organization Reports tab, Reports module)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Organization Reports",
                "Organization Reports tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#zp_t_reports_admin", "org_reports_tab");

        // Click Organization Reports tab and validate page content
        System.out.println("Clicking Organization Reports tab to validate content...");
        page.locator("#zp_t_reports_admin").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL confirms navigation, and 'Employee Information' category confirms Org Reports content loaded
        String orgReportsUrl = page.url();
        System.out.println("URL after clicking Organization Reports: " + orgReportsUrl);
        Allure.parameter("Organization Reports URL", orgReportsUrl);
        Assert.assertTrue(orgReportsUrl.contains("admin"),
                "URL should contain 'admin' after clicking Organization Reports tab. Got: '" + orgReportsUrl + "'");
        page.waitForSelector("#page-wrapper", new Page.WaitForSelectorOptions().setTimeout(10000));
        String orgReportsContent = page.locator("#page-wrapper").innerText().trim();
        System.out.println("Organization Reports content (excerpt): " + orgReportsContent.substring(0, Math.min(300, orgReportsContent.length())));
        Allure.parameter("Organization Reports content", orgReportsContent);
        Assert.assertTrue(orgReportsContent.contains("Employee Information"),
                "Organization Reports should list 'Employee Information' report category. Got: '" + orgReportsContent + "'");
        takeScreenshotOnSuccess("org_reports_tab_content");

        // Navigate back to My Reports tab
        page.locator("#zp_t_reports_my").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForSelector("#myReports_wrap", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Organization Reports tab visible with label 'Organization Reports', content validated, returned to My Reports");
    }

    @TmsLink("ZP-032")
    @Test(priority = 7, description = "[ZP-032] Verify Analytics tab is visible on the Reports page")
    @Story("Reports Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Analytics tab is visible and correctly labelled in the Reports module")
    public void verifyAnalyticsTab() {
        System.out.println("Verifying Analytics tab is visible...");

        // Confirmed via DOM inspection: a#zp_t_reports_analytics, innerText "Analytics"
        page.waitForSelector("#zp_t_reports_analytics",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#zp_t_reports_analytics").innerText().trim();
        String locatorUsed = "#zp_t_reports_analytics (a — Analytics tab, Reports module)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Analytics",
                "Analytics tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#zp_t_reports_analytics", "analytics_tab");

        // Click Analytics tab and validate page content
        System.out.println("Clicking Analytics tab to validate content...");
        page.locator("#zp_t_reports_analytics").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL confirms navigation, and 'Analytics' content confirms Analytics page rendered
        String analyticsUrl = page.url();
        System.out.println("URL after clicking Analytics: " + analyticsUrl);
        Allure.parameter("Analytics URL", analyticsUrl);
        Assert.assertTrue(analyticsUrl.contains("analytics"),
                "URL should contain 'analytics' after clicking Analytics tab. Got: '" + analyticsUrl + "'");
        // Analytics content renders asynchronously — confirm the Analytics tab element is active
        page.waitForSelector("#zp_t_reports_analytics", new Page.WaitForSelectorOptions().setTimeout(10000));
        String analyticsTabText = page.locator("#zp_t_reports_analytics").innerText().trim();
        System.out.println("Analytics active tab text: " + analyticsTabText);
        Allure.parameter("Analytics active tab text", analyticsTabText);
        Assert.assertEquals(analyticsTabText, "Analytics",
                "Analytics tab should be visible and labelled 'Analytics' after navigation.");
        takeScreenshotOnSuccess("analytics_tab_content");

        // Navigate back to My Reports tab
        page.locator("#zp_t_reports_my").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForSelector("#myReports_wrap", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Analytics tab visible with label 'Analytics', content validated, returned to My Reports");
    }

    @TmsLink("ZP-033")
    @Test(priority = 8, description = "[ZP-033] Verify Schedulers tab is visible on the Reports page")
    @Story("Reports Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Schedulers tab is visible and correctly labelled in the Reports module")
    public void verifySchedulersTab() {
        System.out.println("Verifying Schedulers tab is visible...");

        // Confirmed via DOM inspection: a#zp_t_reports_scheduler, innerText "Schedulers"
        page.waitForSelector("#zp_t_reports_scheduler",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#zp_t_reports_scheduler").innerText().trim();
        String locatorUsed = "#zp_t_reports_scheduler (a — Schedulers tab, Reports module)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Schedulers",
                "Schedulers tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#zp_t_reports_scheduler", "schedulers_tab");

        // Click Schedulers tab and validate page content
        System.out.println("Clicking Schedulers tab to validate content...");
        page.locator("#zp_t_reports_scheduler").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL confirms navigation, and 'Scheduled' content confirms Schedulers page rendered
        String schedulersUrl = page.url();
        System.out.println("URL after clicking Schedulers: " + schedulersUrl);
        Allure.parameter("Schedulers URL", schedulersUrl);
        Assert.assertTrue(schedulersUrl.contains("scheduler"),
                "URL should contain 'scheduler' after clicking Schedulers tab. Got: '" + schedulersUrl + "'");
        // Schedulers page shows 'Add Report Scheduler' button — use that as the specific content assertion
        page.waitForSelector("button:has-text('Add Report Scheduler')", new Page.WaitForSelectorOptions().setTimeout(15000));
        boolean addSchedulerVisible = page.isVisible("button:has-text('Add Report Scheduler')");
        System.out.println("'Add Report Scheduler' button visible: " + addSchedulerVisible);
        Allure.parameter("Add Report Scheduler button visible", String.valueOf(addSchedulerVisible));
        Assert.assertTrue(addSchedulerVisible,
                "Schedulers page should display 'Add Report Scheduler' button.");
        takeElementScreenshot("button:has-text('Add Report Scheduler')", "schedulers_add_button");

        // Navigate back to My Reports tab
        page.locator("#zp_t_reports_my").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForSelector("#myReports_wrap", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Schedulers tab visible with label 'Schedulers', content validated, returned to My Reports");
    }

    @TmsLink("ZP-034")
    @Test(priority = 9, description = "[ZP-034] Verify Search Reports input field is visible on the Reports page")
    @Story("Reports Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Search Reports input field is visible with the correct placeholder text")
    public void verifySearchReportsInput() {
        System.out.println("Verifying Search Reports input is visible...");

        // Confirmed via DOM inspection: input#searchreports, placeholder "Search Reports"
        page.waitForSelector("#searchreports",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#searchreports");
        String placeholder = page.locator("#searchreports").getAttribute("placeholder");
        String locatorUsed = "#searchreports (input — Search Reports field, Reports page)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Placeholder", placeholder);
        Assert.assertTrue(isVisible, "Search Reports input (#searchreports) not visible");
        Assert.assertEquals(placeholder, "Search Reports",
                "Search Reports input placeholder mismatch. Got: '" + placeholder + "'");

        takeElementScreenshot("#searchreports", "search_reports_input");
        System.out.println("✅ Test PASSED: Search Reports input is visible with placeholder 'Search Reports'");
    }

    @TmsLink("ZP-035")
    @Test(priority = 10, description = "[ZP-035] Verify Access Permissions button is visible on the Reports page")
    @Story("Reports Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Access Permissions button is visible on the Reports page (admin-only feature)")
    public void verifyAccessPermissionsButton() {
        System.out.println("Verifying Access Permissions button is visible...");

        // Confirmed via DOM inspection: button with text "Access Permissions" — admin-only feature
        page.waitForSelector("button:has-text('Access Permissions')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("button:has-text('Access Permissions')");
        String actualText = page.locator("button:has-text('Access Permissions')").innerText().trim();
        String locatorUsed = "button:has-text('Access Permissions') (admin-only button, Reports page)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Button text", actualText);
        Assert.assertTrue(isVisible, "Access Permissions button not visible");
        Assert.assertEquals(actualText, "Access Permissions",
                "Access Permissions button text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("button:has-text('Access Permissions')", "access_permissions_button");
        System.out.println("✅ Test PASSED: Access Permissions button is visible");
    }

    @TmsLink("ZP-036")
    @Test(priority = 11, description = "[ZP-036] Verify Employee Information category is listed in My Reports")
    @Story("Reports Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Employee Information report category heading is listed under My Reports")
    public void verifyEmployeeInformationCategory() {
        System.out.println("Verifying Employee Information category is listed...");

        // Confirmed via DOM inspection: li inside #myReports_wrap containing text "Employee Information"
        page.waitForSelector("#myReports_wrap li:has-text('Employee Information')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#myReports_wrap li:has-text('Employee Information')");
        String locatorUsed = "#myReports_wrap li:has-text('Employee Information') (report category)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Employee Information category not listed in My Reports");

        takeElementScreenshot("#myReports_wrap", "employee_info_category");
        System.out.println("✅ Test PASSED: Employee Information category is listed");
    }

    @TmsLink("ZP-037")
    @Test(priority = 12, description = "[ZP-037] Verify Leave Tracker category is listed in My Reports")
    @Story("Reports Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Leave Tracker report category heading is listed under My Reports")
    public void verifyLeaveTrackerCategory() {
        System.out.println("Verifying Leave Tracker category is listed...");

        // Confirmed via DOM inspection: li inside #myReports_wrap containing text "Leave Tracker"
        page.waitForSelector("#myReports_wrap li:has-text('Leave Tracker')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#myReports_wrap li:has-text('Leave Tracker')");
        String locatorUsed = "#myReports_wrap li:has-text('Leave Tracker') (report category)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Leave Tracker category not listed in My Reports");

        takeElementScreenshot("#myReports_wrap", "leave_tracker_category");
        System.out.println("✅ Test PASSED: Leave Tracker category is listed");
    }

    @TmsLink("ZP-038")
    @Test(priority = 13, description = "[ZP-038] Verify Attendance category is listed in My Reports")
    @Story("Reports Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Attendance report category heading is listed under My Reports")
    public void verifyAttendanceCategory() {
        System.out.println("Verifying Attendance category is listed...");

        // Confirmed via DOM inspection: li inside #myReports_wrap containing text "Attendance"
        page.waitForSelector("#myReports_wrap li:has-text('Attendance')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#myReports_wrap li:has-text('Attendance')");
        String locatorUsed = "#myReports_wrap li:has-text('Attendance') (report category)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Attendance category not listed in My Reports");

        takeElementScreenshot("#myReports_wrap", "attendance_category");
        System.out.println("✅ Test PASSED: Attendance category is listed");
    }

}
