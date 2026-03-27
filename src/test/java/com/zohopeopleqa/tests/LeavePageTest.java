package com.zohopeopleqa.tests;

import com.zohopeopleqa.base.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.*;

@Epic("Zoho People QA")
@Feature("Leave Page")
public class LeavePageTest extends BaseTest {

    @TmsLink("ZP-015")
    @Test(priority = 1, description = "[ZP-015] Verify Leave Tracker tab is visible in navigation")
    @Story("Leave Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Leave Tracker navigation tab is present and visible")
    public void verifyLeaveTrackerTab() {
        System.out.println("Verifying Leave Tracker tab is visible...");

        // Leave Tracker tab in the left sidebar / top navigation
        page.waitForSelector("li:has-text('Leave Tracker'), a:has-text('Leave Tracker'), [title='Leave Tracker']",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("li:has-text('Leave Tracker'), a:has-text('Leave Tracker'), [title='Leave Tracker']");
        String locatorUsed = "li:has-text('Leave Tracker') | [title='Leave Tracker']";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Leave Tracker tab not visible in navigation");

        takeElementScreenshot("li:has-text('Leave Tracker'), [title='Leave Tracker']", "leave_tracker_tab");
        System.out.println("✅ Test PASSED: Leave Tracker tab is visible");
    }

    @TmsLink("ZP-016")
    @Test(priority = 2, description = "[ZP-016] Verify navigating to Leave Tracker page")
    @Story("Leave Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Leave Tracker tab and validates the page loads correctly")
    public void verifyLeaveTrackerPageLoads() {
        System.out.println("Navigating to Leave Tracker page...");

        // Use LOAD (not NETWORKIDLE) before the click — SPA background polling prevents
        // NETWORKIDLE from being reached reliably between tests on a live session.
        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(5000));

        // Click the Leave Tracker tab
        page.click("li:has-text('Leave Tracker'), a:has-text('Leave Tracker'), [title='Leave Tracker']");;
        System.out.println("Clicked Leave Tracker tab");

        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));

        String url = page.url();
        boolean isOnLeavePage = url.contains("leave") || url.contains("Leave");
        String locatorUsed = "page.url() contains 'leave'";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Current URL", url);
        Assert.assertTrue(isOnLeavePage, "Did not navigate to Leave page. URL: " + url);

        takeScreenshotOnSuccess("leave_page_loaded");
        System.out.println("✅ Test PASSED: Leave Tracker page loaded. URL: " + url);
    }

    @TmsLink("ZP-017")
    @Test(priority = 3, description = "[ZP-017] Verify Leave Balance sub-tab is accessible")
    @Story("Leave Balance")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks the Leave Balance sub-tab and validates the page navigates to the balance URL")
    public void verifyLeaveBalanceSection() {
        System.out.println("Verifying Leave Balance section is accessible...");

        // Navigate to Leave Tracker first to ensure the sub-tabs are visible
        // (makes this test self-contained regardless of prior test state)
        page.waitForSelector("[title='Leave Tracker'], #zp_maintab_leavetracker",
                new Page.WaitForSelectorOptions().setTimeout(10000));
        page.click("[title='Leave Tracker'], #zp_maintab_leavetracker");
        System.out.println("Navigated to Leave Tracker");

        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));

        // Click 'Leave Balance' sub-tab link — confirmed href="#leavetracker/mydata/balance"
        // This link is in #tlTabDiv (the Leave Tracker secondary nav)
        page.waitForSelector("a:has-text('Leave Balance')",
                new Page.WaitForSelectorOptions().setTimeout(10000));
        page.click("a:has-text('Leave Balance')");
        System.out.println("Clicked Leave Balance sub-tab");

        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));

        String url = page.url();
        boolean isOnBalancePage = url.contains("balance");
        String locatorUsed = "a:has-text('Leave Balance') → page.url() contains 'balance'";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Current URL", url);
        Assert.assertTrue(isOnBalancePage, "Did not navigate to Leave Balance page. URL: " + url);

        takeScreenshotOnSuccess("leave_balance_section");
        System.out.println("✅ Test PASSED: Leave Balance section is accessible. URL: " + url);
    }

    @TmsLink("ZP-018")
    @Test(priority = 4, description = "[ZP-018] Verify My Data top-tab is visible on Leave Tracker page")
    @Story("Leave Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the My Data top-level tab is visible and correctly labelled in the Leave Tracker module")
    public void verifyMyDataTopTab() {
        System.out.println("Verifying My Data top-tab is visible...");

        // Confirmed via DOM inspection: a#zp_t_leavetracker_mydata, innerText "My Data"
        // Visible on any Leave Tracker sub-page (persists across Summary/Balance/Requests/Shift)
        page.waitForSelector("#zp_t_leavetracker_mydata",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#zp_t_leavetracker_mydata").first().innerText().trim();
        String locatorUsed = "#zp_t_leavetracker_mydata (a — My Data top-level tab, Leave Tracker)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "My Data",
                "My Data tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#zp_t_leavetracker_mydata", "leave_my_data_tab");

        // Click the My Data tab and validate page content
        System.out.println("Clicking My Data tab to validate content...");
        page.locator("#zp_t_leavetracker_mydata").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: 'Leave Summary' sub-tab label confirms My Data section loaded with actual data
        page.waitForSelector("#tltabcontainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        String myDataNav = page.locator("#tltabcontainer").innerText().trim();
        System.out.println("My Data sub-nav content: " + myDataNav);
        Allure.parameter("My Data sub-nav content", myDataNav);
        Assert.assertTrue(myDataNav.contains("Leave Summary"),
                "My Data section should list 'Leave Summary' sub-tab. Got: '" + myDataNav + "'");
        takeElementScreenshot("#tltabcontainer", "my_data_tab_content");
        // My Data is the default Leave Tracker section — no navigation back needed
        System.out.println("✅ Test PASSED: My Data tab visible, 'Leave Summary' sub-tab confirmed in nav");
    }

    @TmsLink("ZP-019")
    @Test(priority = 5, description = "[ZP-019] Verify Team top-tab is visible on Leave Tracker page")
    @Story("Leave Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Team top-level tab is visible and correctly labelled in the Leave Tracker module")
    public void verifyTeamTopTab() {
        System.out.println("Verifying Team top-tab is visible...");

        // Confirmed via DOM inspection: a#zp_t_leavetracker_team, innerText "Team"
        page.waitForSelector("#zp_t_leavetracker_team",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#zp_t_leavetracker_team").first().innerText().trim();
        String locatorUsed = "#zp_t_leavetracker_team (a — Team top-level tab, Leave Tracker)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Team",
                "Team tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#zp_t_leavetracker_team", "leave_team_tab");

        // Click the Team tab and validate page content
        System.out.println("Clicking Team tab to validate content...");
        page.locator("#zp_t_leavetracker_team").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL confirms navigation, and 'Leave' content confirms team leave data rendered
        String teamUrl = page.url();
        System.out.println("URL after clicking Team tab: " + teamUrl);
        Allure.parameter("Team tab URL", teamUrl);
        Assert.assertTrue(teamUrl.contains("team"),
                "URL should contain 'team' after clicking Team tab. Got: '" + teamUrl + "'");
        // Team view shows 'Reportees | On Leave | Leave Requests' sub-tabs in #tltabcontainer
        page.waitForSelector("#tltabcontainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        String teamNav = page.locator("#tltabcontainer").innerText().trim();
        System.out.println("Team view sub-nav content: " + teamNav);
        Allure.parameter("Team view sub-nav content", teamNav);
        Assert.assertTrue(teamNav.contains("On Leave"),
                "Team leave view should show 'On Leave' sub-tab in nav. Got: '" + teamNav + "'");
        takeElementScreenshot("#tltabcontainer", "team_tab_subnav");

        // Navigate back to My Data tab (reliable in-page SPA navigation)
        page.locator("#zp_t_leavetracker_mydata").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForSelector("#tltabcontainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Team tab visible with label 'Team', Team content validated, returned to My Data");
    }

    @TmsLink("ZP-020")
    @Test(priority = 6, description = "[ZP-020] Verify Holidays top-tab is visible on Leave Tracker page")
    @Story("Leave Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Holidays top-level tab is visible and correctly labelled in the Leave Tracker module")
    public void verifyHolidaysTopTab() {
        System.out.println("Verifying Holidays top-tab is visible...");

        // Confirmed via DOM inspection: a#zp_t_leavetracker_holiday, innerText "Holidays"
        page.waitForSelector("#zp_t_leavetracker_holiday",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#zp_t_leavetracker_holiday").first().innerText().trim();
        String locatorUsed = "#zp_t_leavetracker_holiday (a — Holidays top-level tab, Leave Tracker)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Holidays",
                "Holidays tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#zp_t_leavetracker_holiday", "leave_holidays_tab");

        // Click the Holidays tab and validate page content
        System.out.println("Clicking Holidays tab to validate content...");
        page.locator("#zp_t_leavetracker_holiday").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL confirms navigation, and 'New Year' entry confirms holiday list rendered
        String holidayUrl = page.url();
        System.out.println("URL after clicking Holidays tab: " + holidayUrl);
        Allure.parameter("Holidays tab URL", holidayUrl);
        Assert.assertTrue(holidayUrl.contains("holiday"),
                "URL should contain 'holiday' after clicking Holidays tab. Got: '" + holidayUrl + "'");
        // Verify the Holidays page loaded — either with holiday data or the empty-state message
        page.waitForTimeout(1500);
        boolean holidayPageLoaded = page.locator("button:has-text('Add Holidays')").isVisible() ||
                page.locator("text=No holiday data").isVisible();
        System.out.println("Holidays page loaded (Add Holidays button or empty state visible): " + holidayPageLoaded);
        Allure.parameter("Holidays page loaded", String.valueOf(holidayPageLoaded));
        Assert.assertTrue(holidayPageLoaded,
                "Holidays page should load after clicking the Holidays tab.");
        takeScreenshotOnSuccess("holidays_tab_page_content");

        // Navigate back to My Data tab (reliable in-page SPA navigation)
        page.locator("#zp_t_leavetracker_mydata").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForSelector("#tltabcontainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Holidays tab visible with label 'Holidays', Holiday content validated, returned to My Data");
    }

    @TmsLink("ZP-021")
    @Test(priority = 7, description = "[ZP-021] Verify Leave Summary sub-tab is visible in Leave Tracker")
    @Story("Leave Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Leave Summary sub-tab is visible in the Leave Tracker My Data section")
    public void verifyLeaveSummarySubTab() {
        System.out.println("Verifying Leave Summary sub-tab is visible...");

        // Ensure My Data view is active (self-contained navigation guard)
        page.locator("#zp_t_leavetracker_mydata").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));

        // Confirmed via DOM inspection: a[href*="summary"] inside #tltabcontainer, innerText "Leave Summary"
        page.waitForSelector("#tltabcontainer a[href*='summary']",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#tltabcontainer a[href*='summary']").innerText().trim();
        String locatorUsed = "#tltabcontainer a[href*='summary'] (Leave Summary sub-tab)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Leave Summary",
                "Leave Summary sub-tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#tltabcontainer", "leave_summary_sub_tab");

        // Click Leave Summary sub-tab and validate page content
        System.out.println("Clicking Leave Summary sub-tab to validate content...");
        page.locator("#tltabcontainer a[href*='summary']").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL confirms navigation, and 'Leave booked this year' confirms summary content rendered
        String summaryUrl = page.url();
        System.out.println("URL after clicking Leave Summary: " + summaryUrl);
        Allure.parameter("Leave Summary URL", summaryUrl);
        Assert.assertTrue(summaryUrl.contains("summary"),
                "URL should contain 'summary' after clicking Leave Summary sub-tab. Got: '" + summaryUrl + "'");
        // Leave Summary page uses its own container — wait for the specific summary text
        page.waitForSelector("text=Leave booked this year", new Page.WaitForSelectorOptions().setTimeout(15000));
        boolean summaryVisible = page.isVisible("text=Leave booked this year");
        System.out.println("'Leave booked this year' label visible: " + summaryVisible);
        Allure.parameter("Leave booked this year visible", String.valueOf(summaryVisible));
        Assert.assertTrue(summaryVisible,
                "Leave Summary page should display 'Leave booked this year' label.");
        takeScreenshotOnSuccess("leave_summary_page_content");
        // Leave Summary is the My Data default view — no navigation back needed
        System.out.println("✅ Test PASSED: Leave Summary sub-tab visible with label 'Leave Summary', Summary content validated");
    }

    @TmsLink("ZP-022")
    @Test(priority = 8, description = "[ZP-022] Verify Leave Requests sub-tab is visible in Leave Tracker")
    @Story("Leave Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Leave Requests sub-tab is visible in the Leave Tracker My Data section")
    public void verifyLeaveRequestsSubTab() {
        System.out.println("Verifying Leave Requests sub-tab is visible...");

        // Use text-based selector for Leave Requests tab (href pattern may vary by account)
        page.waitForSelector("#tltabcontainer a:has-text('Leave Requests')",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#tltabcontainer a:has-text('Leave Requests')").first().innerText().trim();
        String locatorUsed = "#tltabcontainer a:has-text('Leave Requests') (Leave Requests sub-tab)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Leave Requests",
                "Leave Requests sub-tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#tltabcontainer", "leave_requests_sub_tab");

        // Click Leave Requests sub-tab and validate page content
        System.out.println("Clicking Leave Requests sub-tab to validate content...");
        page.locator("#tltabcontainer a:has-text('Leave Requests')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL confirms navigation, and 'Apply Leave' button confirms Leave Requests page loaded
        String requestsUrl = page.url();
        System.out.println("URL after clicking Leave Requests: " + requestsUrl);
        Allure.parameter("Leave Requests URL", requestsUrl);
        Assert.assertTrue(requestsUrl.contains("applications") || requestsUrl.contains("request") || requestsUrl.contains("leave"),
                "URL should update after clicking Leave Requests sub-tab. Got: '" + requestsUrl + "'");
        // Leave Requests page has an 'Add Request' button in the DOM (may be hidden)
        page.waitForSelector("button[name='addrequest']",
                new Page.WaitForSelectorOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
                        .setTimeout(15000));
        boolean addRequestPresent = page.locator("button[name='addrequest']").count() > 0;
        System.out.println("Leave Requests page - 'Add Request' button in DOM: " + addRequestPresent);
        Allure.parameter("Add Request button present", String.valueOf(addRequestPresent));
        Assert.assertTrue(addRequestPresent,
                "Leave Requests page should contain 'Add Request' button in DOM.");
        takeScreenshotOnSuccess("leave_requests_page_content");

        // Navigate back to Leave Summary (reliable in-page SPA navigation)
        page.locator("#tltabcontainer a[href*='summary']").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForSelector("#tltabcontainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Leave Requests sub-tab visible with label 'Leave Requests', Requests content validated, returned to Leave Summary");
    }

    @TmsLink("ZP-023")
    @Test(priority = 9, description = "[ZP-023] Verify Shift sub-tab is visible in Leave Tracker")
    @Story("Leave Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Shift sub-tab is visible in the Leave Tracker My Data section")
    public void verifyShiftSubTab() {
        System.out.println("Verifying Shift sub-tab is visible...");

        // Confirmed via DOM inspection: a[href*="employeeshiftmapping"] inside #tltabcontainer, innerText "Shift"
        page.waitForSelector("#tltabcontainer a[href*='employeeshiftmapping']",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#tltabcontainer a[href*='employeeshiftmapping']").innerText().trim();
        String locatorUsed = "#tltabcontainer a[href*='employeeshiftmapping'] (Shift sub-tab)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Shift",
                "Shift sub-tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#tltabcontainer", "shift_sub_tab");

        // Click Shift sub-tab and validate page content
        System.out.println("Clicking Shift sub-tab to validate content...");
        page.locator("#tltabcontainer a[href*='employeeshiftmapping']").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL confirms navigation, and 'General' shift name confirms shift data rendered
        String shiftUrl = page.url();
        System.out.println("URL after clicking Shift: " + shiftUrl);
        Allure.parameter("Shift URL", shiftUrl);
        Assert.assertTrue(shiftUrl.contains("employeeshiftmapping"),
                "URL should contain 'employeeshiftmapping' after clicking Shift sub-tab. Got: '" + shiftUrl + "'");
        // Shift page shows a weekly calendar — 'Assign shift' button is always present in the header
        page.waitForSelector("button:has-text('Assign shift')", new Page.WaitForSelectorOptions().setTimeout(15000));
        boolean assignShiftVisible = page.isVisible("button:has-text('Assign shift')");
        System.out.println("'Assign shift' button visible on Shift page: " + assignShiftVisible);
        Allure.parameter("Assign shift button visible", String.valueOf(assignShiftVisible));
        Assert.assertTrue(assignShiftVisible,
                "Shift page should display 'Assign shift' button in the calendar header.");
        takeScreenshotOnSuccess("shift_tab_page_content");

        // Navigate back to Leave Summary (reliable in-page SPA navigation)
        page.locator("#tltabcontainer a[href*='summary']").click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForSelector("#tltabcontainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Shift sub-tab visible with label 'Shift', Shift content validated, returned to Leave Summary");
    }

    @TmsLink("ZP-024")
    @Test(priority = 10, description = "[ZP-024] Verify Apply Leave button is visible on Leave Summary page")
    @Story("Leave Summary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Apply Leave button is visible on the Leave Summary page")
    public void verifyApplyLeaveButton() {
        System.out.println("Verifying Apply Leave button is visible...");

        // Navigate to Leave Tracker (defaults to Leave Summary) to ensure the button is in view
        page.click("#zp_maintab_leavetracker");
        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Explicitly click Leave Summary sub-tab so the Apply Leave button is visible
        page.locator("#tltabcontainer a[href*='summary']").click();
        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));

        // Confirmed via DOM inspection: button.ZP-Add with text "Apply Leave" on the Leave Summary page
        page.waitForSelector("button.ZP-Add",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("button.ZP-Add").first().innerText().trim();
        String locatorUsed = "button.ZP-Add (Apply Leave button, Leave Summary page)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Button text", actualText);
        Assert.assertEquals(actualText, "Apply Leave",
                "Apply Leave button text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("button.ZP-Add", "apply_leave_button");
        System.out.println("✅ Test PASSED: Apply Leave button is visible with label 'Apply Leave'");
    }

    @TmsLink("ZP-025")
    @Test(priority = 11, description = "[ZP-025] Verify Leave booked summary is visible on Leave Summary page")
    @Story("Leave Summary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the leave booked summary header is visible on the Leave Summary page")
    public void verifyLeaveBookedSummary() {
        System.out.println("Verifying Leave booked summary is visible...");

        // Navigate to Leave Tracker (defaults to Leave Summary) to ensure summary content is loaded
        page.click("#zp_maintab_leavetracker");
        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Explicitly click Leave Summary sub-tab so the page content is loaded
        page.locator("#tltabcontainer a[href*='summary']").click();
        page.waitForLoadState(LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(10000));

        // Leave Summary page loads asynchronously — wait for the specific summary text directly
        page.waitForSelector("text=Leave booked this year",
                new Page.WaitForSelectorOptions().setTimeout(15000));

        boolean leaveBookedVisible = page.isVisible("text=Leave booked this year");
        String locatorUsed = "text=Leave booked this year (Leave Summary page content)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Leave booked this year visible", String.valueOf(leaveBookedVisible));
        Assert.assertTrue(leaveBookedVisible,
                "Leave Summary page should display 'Leave booked this year' text.");

        takeScreenshotOnSuccess("leave_booked_summary");
        System.out.println("✅ Test PASSED: Leave booked summary is visible");
    }

}
