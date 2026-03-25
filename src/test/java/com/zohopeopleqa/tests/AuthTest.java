package com.zohopeopleqa.tests;

import com.zohopeopleqa.base.BaseTest;
import com.zohopeopleqa.config.Config;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import java.io.ByteArrayInputStream;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.*;

@Epic("Zoho People QA")
@Feature("Authentication")
public class AuthTest extends BaseTest {

    @Test(priority = 1, description = "Verify Zoho People login works with test account")
    @Story("User Login Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifies that a user can successfully login to Zoho People with valid credentials and reach the dashboard")
    public void verifyLoginAndDashboard() {
        String url = page.url();
        System.out.println("Verifying dashboard URL: " + url);
        boolean isOnDashboard = url.startsWith(Config.BASE_URL + "/") &&
                                (url.contains("/zp") || url.contains("/home") || url.contains("/dashboard"));

        String locatorUsed = "page.url() startsWith '" + Config.BASE_URL + "/' && contains('/zp'|'/home'|'/dashboard')";
        System.out.println("[Locator] " + locatorUsed);
        Allure.parameter("Locator / Condition", locatorUsed);
        Allure.parameter("Final URL", url);

        Assert.assertTrue(isOnDashboard,
                "Login failed — did not reach Zoho People dashboard. Current URL: " + url);

        takeScreenshotOnSuccess("login_success");
        System.out.println("✅ Test PASSED: Login successful — Zoho People dashboard reached");
    }

    @Test(priority = 2, description = "Verify logout functionality from Zoho People")
    @Story("User Logout Flow")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifies logout in a disposable browser context so the main session stays valid for future runs")
    public void verifyLogout() {
        System.out.println("Testing logout in a disposable session (main session preserved)...");

        String urlBeforeLogout = page.url();
        String preLogoutCondition = "page.url() startsWith '" + Config.BASE_URL + "/' && contains('/zp')";
        System.out.println("[Locator] " + preLogoutCondition);
        Allure.parameter("Locator / Condition (pre-logout)", preLogoutCondition);
        Allure.parameter("URL before logout", urlBeforeLogout);
        Assert.assertTrue(
                urlBeforeLogout.startsWith(Config.BASE_URL + "/") && urlBeforeLogout.contains("/zp"),
                "Not on dashboard before logout. URL: " + urlBeforeLogout
        );

        // Logout in a disposable copy of the session — main page/session-state.json untouched
        BrowserContext disposableContext = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(Paths.get(SESSION_STATE_FILE))
        );
        Page disposablePage = disposableContext.newPage();
        try {
            disposablePage.navigate(Config.BASE_URL);
            disposablePage.waitForURL(
                    url -> url.startsWith(Config.BASE_URL + "/") && url.contains("/zp"),
                    new Page.WaitForURLOptions().setTimeout(10000)
            );
            System.out.println("Disposable session active at: " + disposablePage.url());

            byte[] beforeBytes = disposablePage.screenshot();
            Allure.addAttachment("1. Dashboard before logout", "image/png",
                    new ByteArrayInputStream(beforeBytes), "png");

            logoutFromPage(disposablePage);

            String urlAfterLogout = disposablePage.url();
            boolean loggedOut = !urlAfterLogout.contains("/zp") && !urlAfterLogout.contains("/home");
            String postLogoutCondition = "disposablePage.url() not contains '/zp' && not contains '/home'";
            System.out.println("[Locator] " + postLogoutCondition);
            Allure.parameter("Locator / Condition (post-logout)", postLogoutCondition);
            Allure.parameter("URL after logout", urlAfterLogout);

            byte[] afterBytes = disposablePage.screenshot();
            Allure.addAttachment("2. Page after logout", "image/png",
                    new ByteArrayInputStream(afterBytes), "png");

            Assert.assertTrue(loggedOut,
                    "Logout failed — still on ZP page. URL: " + urlAfterLogout);
            System.out.println("✅ Test PASSED: Logout verified. Main session preserved for next run.");
        } finally {
            disposablePage.close();
            disposableContext.close();
        }
    }
}
