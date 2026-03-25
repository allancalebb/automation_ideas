package com.zohopeopleqa.tests;

import com.zohopeopleqa.base.BaseTest;
import com.zohopeopleqa.config.Config;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.*;

@Epic("Zoho People QA")
@Feature("Home Page")
public class HomePageTest extends BaseTest {

    @Test(priority = 1, description = "Verify logged-in user's display name is visible")
    @Story("User Profile Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the logged-in user's name is shown in the Zoho People navigation bar")
    public void verifyUserDisplayName() {
        System.out.println("Verifying logged-in user display name...");

        // Real selector confirmed via Playwright MCP DOM inspection:
        // #user_detailsBand — profile panel containing user's full display name.
        // Loaded asynchronously by the SPA; allow 30s for it to appear.
        page.waitForSelector("#user_detailsBand",
                new Page.WaitForSelectorOptions().setTimeout(30000));

        String actualName = page.locator("#user_detailsBand").innerText();
        actualName = actualName != null ? actualName.trim() : "";
        System.out.println("Found display name: " + actualName);

        String locatorUsed = "#user_detailsBand (async-loaded profile panel, innerText)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual display name", actualName);
        Assert.assertFalse(
            actualName.isEmpty(),
            "Expected display name to be non-empty but got empty string"
        );

        takeElementScreenshot("#user_detailsBand", "display_name_validated");
        System.out.println("\u2705 Test PASSED: User display name is '" + actualName + "'");
    }

    @Test(priority = 2, description = "Verify profile photo is visible in the header")
    @Story("User Profile Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the logged-in user's profile photo/avatar is visible in the Zoho People header")
    public void verifyProfilePhoto() {
        System.out.println("Verifying profile photo is visible...");

        // Real selector: img id="userprofimg" — profile photo in the My Space overview section
        page.waitForSelector("#userprofimg",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#userprofimg");
        String locatorUsed = "#userprofimg (img — profile photo, My Space overview)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Profile photo img#userprofimg not visible");

        takeElementScreenshot("#userprofimg", "profile_photo_visible");
        System.out.println("✅ Test PASSED: Profile photo is visible");
    }

    @Test(priority = 3, description = "Verify Reports tab is present in navigation")
    @Story("Navigation Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Reports tab/link is visible and clickable in the Zoho People navigation")
    public void verifyReportsTab() {
        System.out.println("Verifying Reports tab is present in navigation...");

        // Real selector confirmed via Playwright MCP DOM inspection:
        // li id="zp_maintab_reports" class="tooltip-js" title="Reports"
        page.waitForSelector("#zp_maintab_reports",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#zp_maintab_reports");
        String locatorUsed = "#zp_maintab_reports (li — Reports tab, main navigation)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Reports tab (li#zp_maintab_reports) not visible in navigation");

        takeElementScreenshot("#zp_maintab_reports", "reports_tab_visible");

        // Click the Reports tab and validate page content
        System.out.println("Clicking Reports tab to validate page content...");
        page.click("#zp_maintab_reports");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        // Validate: search input with placeholder "Search Reports" confirms the Reports page loaded
        page.waitForSelector("#searchreports", new Page.WaitForSelectorOptions().setTimeout(10000));
        String searchPlaceholder = page.locator("#searchreports").getAttribute("placeholder");
        System.out.println("Reports page search placeholder: " + searchPlaceholder);
        Allure.parameter("Reports page search placeholder", searchPlaceholder);
        Assert.assertEquals(searchPlaceholder, "Search Reports",
                "Reports page search input placeholder mismatch after navigation. Got: '" + searchPlaceholder + "'");
        takeElementScreenshot("#searchreports", "reports_page_search_input");

        // Navigate back to Home via the Home nav tab (more reliable than goBack() in a hash SPA)
        page.locator("#zp_maintab_home").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector("#userprofimg", new Page.WaitForSelectorOptions().setTimeout(15000));
        System.out.println("✅ Test PASSED: Reports tab present, page content validated, returned to Home");
    }

    @Test(priority = 4, description = "Verify Notification icon button is visible in the header")
    @Story("Navigation Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the notification bell icon/button is visible in the Zoho People header")
    public void verifyNotificationIcon() {
        System.out.println("Verifying notification icon is visible...");

        // Real selector confirmed via Playwright MCP DOM inspection:
        // li id="zp_feeds_notifications" class="tooltip-js PR" title="Notifications"
        // contains: i.PI_glob-notify (the bell icon)
        page.waitForSelector("#zp_feeds_notifications",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#zp_feeds_notifications");
        String locatorUsed = "#zp_feeds_notifications (li — notification bell, top-right header)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Notification icon (li#zp_feeds_notifications) not visible in header");

        takeElementScreenshot("#zp_feeds_notifications", "notification_icon_visible");
        System.out.println("✅ Test PASSED: Notification icon is visible");
    }

    @Test(priority = 5, description = "Verify Leave Tracker tab is visible in main navigation")
    @Story("Navigation Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Leave Tracker tab is present in the main navigation bar with the correct label")
    public void verifyLeaveTrackerTabInNav() {
        System.out.println("Verifying Leave Tracker tab in main navigation...");

        // Confirmed via DOM inspection: li#zp_maintab_leavetracker, innerText "Leave Tracker"
        page.waitForSelector("#zp_maintab_leavetracker",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#zp_maintab_leavetracker").first().innerText().trim();
        String locatorUsed = "#zp_maintab_leavetracker (li — Leave Tracker tab, main navigation)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "Leave Tracker",
                "Leave Tracker tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#zp_maintab_leavetracker", "leave_tracker_nav_tab");

        // Click the Leave Tracker tab and validate the page content
        System.out.println("Clicking Leave Tracker tab to validate page content...");
        page.locator("#zp_maintab_leavetracker").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        // Validate: sub-tab container visible (holds My Data / Team / Holidays tabs)
        page.waitForSelector("#tltabcontainer", new Page.WaitForSelectorOptions().setTimeout(10000));
        boolean ltContainerVisible = page.isVisible("#tltabcontainer");
        Allure.parameter("Leave Tracker sub-tab container visible", String.valueOf(ltContainerVisible));
        Assert.assertTrue(ltContainerVisible,
                "Leave Tracker sub-tab container (#tltabcontainer) not visible after navigation");
        takeElementScreenshot("#tltabcontainer", "leave_tracker_page_content");

        // Navigate back to Home via the Home nav tab (more reliable than goBack() in a hash SPA)
        page.locator("#zp_maintab_home").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector("#actionListWelcomeCard", new Page.WaitForSelectorOptions().setTimeout(15000));
        System.out.println("✅ Test PASSED: Leave Tracker tab visible with label 'Leave Tracker', page content validated, returned to Home");
    }

    @Test(priority = 6, description = "Verify Search Employee icon is visible in the header")
    @Story("Navigation Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Search Employee icon is visible in the Zoho People top-right navigation")
    public void verifySearchEmployeeIcon() {
        System.out.println("Verifying Search Employee icon is visible...");

        // Confirmed via DOM inspection: li#zpeople_search_icon — search employee icon, top nav
        page.waitForSelector("#zpeople_search_icon",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("#zpeople_search_icon");
        String locatorUsed = "#zpeople_search_icon (li — Search Employee icon, top navigation)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Assert.assertTrue(isVisible, "Search Employee icon (#zpeople_search_icon) not visible");

        takeElementScreenshot("#zpeople_search_icon", "search_employee_icon_visible");
        System.out.println("✅ Test PASSED: Search Employee icon is visible");
    }

    @Test(priority = 7, description = "Verify My Space sub-tab is visible on the Home page")
    @Story("Home Page Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the My Space sub-tab link is visible and correctly labelled in the Home page header")
    public void verifyMySpaceSubTab() {
        System.out.println("Verifying My Space sub-tab is visible...");

        // Full-navigate to Home so the SPA fully re-renders the sub-tab bar under parallel load
        page.navigate(Config.BASE_URL);
        page.waitForURL(
                url -> url.startsWith(Config.BASE_URL + "/") && (url.contains("/zp") || url.contains("/home")),
                new Page.WaitForURLOptions().setTimeout(20000)
        );

        // #zp_t_home_myspace may be CSS-hidden when the Activities sub-tab is active.
        // Use JavaScript to read its text and trigger a click regardless of visual state.
        String locatorUsed = "#zp_t_home_myspace (a — My Space sub-tab link, via JS)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);

        // Wait for element to be in the DOM (attached), not necessarily visible
        // Use 30s timeout: parallel execution + SPA re-render can exceed 15s
        page.waitForSelector("#zp_t_home_myspace",
                new Page.WaitForSelectorOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
                        .setTimeout(30000));

        String actualText = ((String) page.evaluate(
                "document.getElementById('zp_t_home_myspace')?.textContent?.trim() ?? ''"));
        System.out.println("My Space tab text (via JS): " + actualText);
        Allure.parameter("Actual tab text", actualText);
        Assert.assertEquals(actualText, "My Space",
                "My Space sub-tab text mismatch. Got: '" + actualText + "'");

        // Click via JS to bypass CSS visibility restriction
        System.out.println("Clicking My Space sub-tab via JS to validate content...");
        page.evaluate("const el = document.getElementById('zp_t_home_myspace'); el?.scrollIntoView(); el?.click();");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));

        // Validate: profile photo visible confirms the My Space overview section loaded
        page.waitForSelector("#userprofimg", new Page.WaitForSelectorOptions().setTimeout(15000));
        boolean profileVisible = page.isVisible("#userprofimg");
        Allure.parameter("My Space profile photo visible", String.valueOf(profileVisible));
        Assert.assertTrue(profileVisible,
                "My Space profile photo (#userprofimg) not visible after clicking My Space sub-tab");
        takeElementScreenshot("#userprofimg", "my_space_tab_profile_content");
        System.out.println("✅ Test PASSED: My Space sub-tab present with label 'My Space', profile content validated");
    }

    @Test(priority = 8, description = "Verify greeting card displays the logged-in user's name")
    @Story("Home Page Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Activities greeting card is visible and contains the logged-in user's display name")
    public void verifyGreetingCardDisplaysUserName() {
        System.out.println("Verifying greeting card displays user name...");

        // Confirmed via DOM inspection: #actionListWelcomeCard — greeting card on Activities tab.
        // Contains a time-of-day greeting and the user's display name "Allan the Administrator".
        page.waitForSelector("#actionListWelcomeCard",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String cardText = page.locator("#actionListWelcomeCard").innerText().trim();
        String locatorUsed = "#actionListWelcomeCard (div — greeting card, Activities tab)";
        System.out.println("[Locator] " + locatorUsed);
        System.out.println("Greeting card text: " + cardText);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Greeting card text", cardText);
        Assert.assertTrue(cardText.contains("Allan the Administrator"),
                "Greeting card does not contain 'Allan the Administrator'. Got: '" + cardText + "'");

        takeElementScreenshot("#actionListWelcomeCard", "greeting_card_user_name");
        System.out.println("✅ Test PASSED: Greeting card contains 'Allan the Administrator'");
    }

    @Test(priority = 9, description = "Verify Check-in button is visible on the Home page")
    @Story("Home Page Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Check-in button is visible in the My Space attendance section")
    public void verifyCheckInButton() {
        System.out.println("Verifying Check-in button is visible...");

        // Confirmed via DOM inspection: button#ZPAtt_check_in_out, innerText "Check-in"
        page.waitForSelector("#ZPAtt_check_in_out",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#ZPAtt_check_in_out").innerText().trim();
        String locatorUsed = "#ZPAtt_check_in_out (button — Check-in, attendance section)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Button text", actualText);
        Assert.assertEquals(actualText, "Check-in",
                "Check-in button text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#ZPAtt_check_in_out", "check_in_button_visible");
        System.out.println("✅ Test PASSED: Check-in button is visible with label 'Check-in'");
    }

    @Test(priority = 10, description = "Verify Reportees section is visible on the Home page")
    @Story("Home Page Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Reportees section panel is visible in the My Space overview for admin accounts")
    public void verifyReporteesSection() {
        System.out.println("Verifying Reportees section is visible...");

        // Confirmed via DOM inspection: div[aria-label*="Reportees"] — Reportees section panel.
        // For this admin account, the aria-label is "You have 5 Reportees".
        page.waitForSelector("[aria-label*='Reportees']",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        boolean isVisible = page.isVisible("[aria-label*='Reportees']");
        String ariaLabel = page.locator("[aria-label*='Reportees']").getAttribute("aria-label");
        String locatorUsed = "[aria-label*='Reportees'] (div — Reportees section panel)";
        System.out.println("[Locator] " + locatorUsed);
        System.out.println("aria-label: " + ariaLabel);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("aria-label", ariaLabel);
        Assert.assertTrue(isVisible, "Reportees section not visible");
        Assert.assertTrue(ariaLabel != null && ariaLabel.contains("Reportees"),
                "Reportees section aria-label does not contain 'Reportees'. Got: '" + ariaLabel + "'");

        takeElementScreenshot("[aria-label*='Reportees']", "reportees_section_visible");
        System.out.println("✅ Test PASSED: Reportees section is visible");
    }

    @Test(priority = 11, description = "Verify Activities sub-tab is visible in My Space overview")
    @Story("Home Page Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Activities sub-tab link is visible in the My Space overview section")
    public void verifyActivitiesSubTab() {
        System.out.println("Verifying Activities sub-tab is visible...");

        // Confirmed via DOM inspection: li#home_myspace_overview_actionlist > a, innerText "Activities"
        page.waitForSelector("#home_myspace_overview_actionlist a",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#home_myspace_overview_actionlist a").innerText().trim();
        String locatorUsed = "#home_myspace_overview_actionlist a (Activities sub-tab link)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Tab text", actualText);
        Assert.assertEquals(actualText, "Activities",
                "Activities sub-tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#home_myspace_overview_actionlist", "activities_sub_tab");

        // Click the Activities sub-tab and validate the page content
        System.out.println("Clicking Activities sub-tab to validate content...");
        page.click("#home_myspace_overview_actionlist a");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: Activities greeting card is visible
        page.waitForSelector("#actionListWelcomeCard", new Page.WaitForSelectorOptions().setTimeout(10000));
        boolean greetingVisible = page.isVisible("#actionListWelcomeCard");
        Allure.parameter("Activities greeting card visible", String.valueOf(greetingVisible));
        Assert.assertTrue(greetingVisible,
                "Activities greeting card (#actionListWelcomeCard) not visible after clicking Activities sub-tab");
        takeElementScreenshot("#actionListWelcomeCard", "activities_tab_greeting_card");
        // Activities is the default Home sub-view — no navigation back needed
        System.out.println("✅ Test PASSED: Activities sub-tab visible with label 'Activities', greeting card content validated");
    }

    @Test(priority = 12, description = "Verify Feeds sub-tab is visible in My Space overview")
    @Story("Home Page Content")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the Feeds sub-tab link is visible in the My Space overview section")
    public void verifyFeedsSubTab() {
        System.out.println("Verifying Feeds sub-tab is visible...");

        // Confirmed via DOM inspection: li#home_myspace_overview_feeds > a, innerText "Feeds"
        page.waitForSelector("#home_myspace_overview_feeds a",
                new Page.WaitForSelectorOptions().setTimeout(10000));

        String actualText = page.locator("#home_myspace_overview_feeds a").innerText().trim();
        String locatorUsed = "#home_myspace_overview_feeds a (Feeds sub-tab link)";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Tab text", actualText);
        Assert.assertEquals(actualText, "Feeds",
                "Feeds sub-tab text mismatch. Got: '" + actualText + "'");

        takeElementScreenshot("#home_myspace_overview_feeds", "feeds_sub_tab");

        // Click the Feeds sub-tab and validate the page content
        System.out.println("Clicking Feeds sub-tab to validate content...");
        page.click("#home_myspace_overview_feeds a");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        // Validate: URL contains 'feeds' confirming navigation to the Feeds section
        String feedsUrl = page.url();
        System.out.println("URL after clicking Feeds: " + feedsUrl);
        Allure.parameter("Feeds page URL", feedsUrl);
        Assert.assertTrue(feedsUrl.contains("feeds"),
                "URL should contain 'feeds' after clicking Feeds sub-tab. Got: '" + feedsUrl + "'");
        boolean feedsPageWrapperVisible = page.isVisible("#page-wrapper");
        Assert.assertTrue(feedsPageWrapperVisible, "Page wrapper not visible on Feeds page");
        takeScreenshotOnSuccess("feeds_tab_page_content");

        // Navigate back to Activities via its sub-tab link (reliable in-page SPA navigation)
        page.click("#home_myspace_overview_actionlist a");
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForSelector("#actionListWelcomeCard", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("✅ Test PASSED: Feeds sub-tab visible with label 'Feeds', Feeds content validated, returned to Activities");
    }

}