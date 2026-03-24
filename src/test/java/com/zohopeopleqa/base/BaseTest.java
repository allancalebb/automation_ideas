package com.zohopeopleqa.base;

import com.microsoft.playwright.*;
import org.testng.annotations.*;
import org.testng.ITestResult;
import io.qameta.allure.Allure;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class BaseTest {

    // Static — one shared instance for the entire suite
    protected static Playwright playwright;
    protected static Browser browser;
    protected static BrowserContext context;
    protected static Page page;

    private PrintStream originalOut;
    private FileOutputStream testLogStream;
    private static final String LOGS_DIR = "target/logs";
    private ByteArrayOutputStream preTestBuffer;

    protected static final String SESSION_STATE_FILE = "session-state.json";
    protected static final String SESSION_META_FILE  = "session-meta.json";

    @BeforeSuite
    public void setupSuite() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
        );

        // Restore saved session cookies if available — avoids a full login.
        // Also check that the saved session belongs to the same account as ZOHO_USER;
        // if the account changed, discard the old session and force a fresh login.
        java.nio.file.Path sessionPath = Paths.get(SESSION_STATE_FILE);
        java.nio.file.Path metaPath    = Paths.get(SESSION_META_FILE);
        String currentUser = com.zohopeopleqa.utils.EnvLoader.get("ZOHO_USER");
        boolean sessionUserMatches = false;
        if (Files.exists(metaPath)) {
            try {
                String meta = new String(Files.readAllBytes(metaPath));
                sessionUserMatches = meta.contains("\"" + currentUser + "\"");
            } catch (Exception ignored) {}
        }
        if (Files.exists(sessionPath) && sessionUserMatches) {
            System.out.println("[Session] Found saved session for " + currentUser + " — restoring cookies...");
            context = browser.newContext(
                    new Browser.NewContextOptions().setStorageStatePath(sessionPath)
            );
        } else {
            if (Files.exists(sessionPath) && !sessionUserMatches) {
                System.out.println("[Session] ZOHO_USER changed — discarding old session and forcing fresh login.");
                try { Files.deleteIfExists(sessionPath); Files.deleteIfExists(metaPath); } catch (Exception ignored) {}
            } else {
                System.out.println("[Session] No saved session found — will perform full login.");
            }
            context = browser.newContext();
        }

        page = context.newPage();
        page.setDefaultTimeout(15000);
        page.setDefaultNavigationTimeout(15000);
    }

    /**
     * Shared session setup — runs once per test class before any @Test methods.
     * Reuses saved session if the account matches; falls back to full login otherwise.
     * All test classes inherit this automatically — no need to override it.
     */
    @BeforeClass
    public void loginOnce() {
        // Capture @BeforeClass output so the first test's log file includes login messages
        preTestBuffer = new ByteArrayOutputStream();
        originalOut = System.out;
        final ByteArrayOutputStream buf = preTestBuffer;
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) throws java.io.IOException {
                originalOut.write(b); buf.write(b);
            }
            public void write(byte[] b, int off, int len) throws java.io.IOException {
                originalOut.write(b, off, len); buf.write(b, off, len);
            }
            public void flush() throws java.io.IOException {
                originalOut.flush(); buf.flush();
            }
        }, true));
        System.out.println("[Setup] Checking session state...");
        if (Files.exists(Paths.get(SESSION_STATE_FILE))) {
            try {
                page.navigate("https://people.zoho.com");
                page.waitForURL(
                        url -> url.startsWith("https://people.zoho.com/") &&
                               (url.contains("/zp") || url.contains("/home") || url.contains("/dashboard")),
                        new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(10000)
                );
                System.out.println("[Setup] Session still valid — skipping full login. URL: " + page.url());
                return;
            } catch (Exception e) {
                System.out.println("[Setup] Saved session expired or invalid — performing full login.");
                try {
                    Files.deleteIfExists(Paths.get(SESSION_STATE_FILE));
                    Files.deleteIfExists(Paths.get(SESSION_META_FILE));
                } catch (Exception ignored) {}
            }
        }
        String email = com.zohopeopleqa.utils.EnvLoader.get("ZOHO_USER");
        String password = com.zohopeopleqa.utils.EnvLoader.get("ZOHO_PASS");
        System.out.println("[Setup] Logging in as: " + email);
        loginToZohoPeople(email, password);
        System.out.println("[Setup] Login complete — shared session ready.");
    }

    @BeforeMethod
    public void startTestLogging(java.lang.reflect.Method method) {
        // Capture System.out to a per-test log file
        try {
            new File(LOGS_DIR).mkdirs();
            String logPath = LOGS_DIR + "/" + method.getName() + ".log";
            testLogStream = new FileOutputStream(logPath, false);

            // Flush any pre-test output (e.g. @BeforeClass login) into this test's log
            // Only the first test gets this — buffer is cleared afterwards
            if (preTestBuffer != null && preTestBuffer.size() > 0) {
                testLogStream.write(preTestBuffer.toByteArray());
                preTestBuffer.reset();
            }

            // Restore originalOut captured in setupBrowser, then tee to log file
            final FileOutputStream logOut = testLogStream;
            final PrintStream consoleOut = originalOut;
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) throws java.io.IOException {
                    consoleOut.write(b); logOut.write(b);
                }
                public void write(byte[] b, int off, int len) throws java.io.IOException {
                    consoleOut.write(b, off, len); logOut.write(b, off, len);
                }
                public void flush() throws java.io.IOException {
                    consoleOut.flush(); logOut.flush();
                }
            }, true));
        } catch (Exception e) {
            System.setOut(originalOut != null ? originalOut : System.out);
        }
    }

    @AfterMethod
    public void closeContext(ITestResult result) {
        // Save a backup screenshot to disk on pass/fail — NOT attached to Allure
        // (Allure already receives intentional screenshots from within each test body)
        String testName = result.getMethod().getMethodName();
        if (result.getStatus() == ITestResult.SUCCESS) {
            saveScreenshotDiskOnly("test_passed_" + testName);
            System.out.println("✅ Backup screenshot saved on PASS: test_passed_" + testName);
        } else if (result.getStatus() == ITestResult.FAILURE) {
            saveScreenshotDiskOnly("test_failed_" + testName);
            System.out.println("❌ Backup screenshot saved on FAIL: test_failed_" + testName);
        } else {
            // SKIP (or anything else) — no screenshot
            System.out.println("⏭️ Test SKIPPED: " + testName + " — no screenshot taken");
        }

        // Restore System.out and close test log file
        System.out.flush();
        System.setOut(originalOut != null ? originalOut : System.out);
        try {
            if (testLogStream != null) testLogStream.close();
        } catch (Exception ignored) {}
        // context stays open — shared across all tests in this class
    }

    @AfterSuite
    public void teardownSuite() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    /**
     * Performs Zoho People two-step login
     * Step 1: Navigate to login page and click Sign In
     * Step 2: Enter email and click Next
     * Step 3: Enter password and click Sign In
     * Step 4: Handle optional MFA/popups
     * Step 5: Wait for dashboard to load
     */
    protected void loginToZohoPeople(String email, String password) {
        System.out.println("Starting Zoho People login flow...");
        
        // Step 1: Navigate to Zoho People and click Sign In button
        System.out.println("Step 1: Navigating to https://people.zoho.com");
        try {
            page.navigate("https://people.zoho.com");
            
            // Wait for and click the Sign In button on the marketing page
            System.out.println("Waiting for Sign In button...");
            page.click("a:has-text('Sign In'), button:has-text('Sign In'), link:has-text('Sign In')", 
                    new Page.ClickOptions().setTimeout(10000));
            System.out.println("Sign In button clicked");
            
            // Wait for the actual login page to load
            page.waitForURL(url -> url.contains("accounts.zoho.com") || url.contains("signin"), 
                    new Page.WaitForURLOptions().setTimeout(10000));
        } catch (PlaywrightException e) {
            System.out.println("Navigation error: " + e.getMessage());
            takeScreenshot("login_navigation_error");
            throw e;
        }
        
        // Step 2: Enter email and click Next
        System.out.println("Step 2: Entering email and clicking Next");
        try {
            page.waitForSelector("input[type='email'], input[id='login_id'], input[name='login_id'], input[placeholder*='email' i], input[name='username']", 
                    new Page.WaitForSelectorOptions().setTimeout(10000));
            
            // Try multiple possible email field selectors
            if (page.isVisible("input[type='email']")) {
                page.fill("input[type='email']", email);
            } else if (page.isVisible("input[id='login_id']")) {
                page.fill("input[id='login_id']", email);
            } else if (page.isVisible("input[name='username']")) {
                page.fill("input[name='username']", email);
            } else {
                // Fallback: Find any visible input and next button
                page.locator("input[type='email'], input[name='username'], input[id='login_id']").first().fill(email);
            }
            
            // Click Next button
            page.click("button:has-text('Next'), input[type='button'][value='Next'], #nextbtn, button[type='submit']");
            System.out.println("Email entered and Next clicked");
        } catch (PlaywrightException e) {
            System.out.println("Error entering email: " + e.getMessage());
            takeScreenshot("login_email_error");
            throw e;
        }
        
        // Step 3: Wait for password field and enter password
        System.out.println("Step 3: Waiting for password field and entering password");
        try {
            page.waitForSelector("input[type='password'], input[name='password'], input[id='password']", 
                    new Page.WaitForSelectorOptions().setTimeout(10000));
            
            // Give it a moment for the field to be interactive
            page.waitForTimeout(500);
            
            // DEBUG: Log password before filling
            System.out.println("DEBUG - About to fill password field");
            System.out.println("DEBUG - Password value: '" + password + "'");
            System.out.println("DEBUG - Password length: " + password.length());
            System.out.println("DEBUG - Password trimmed: '" + password.trim() + "'");
            System.out.println("DEBUG - Password equals trimmed: " + password.equals(password.trim()));
            
            if (page.isVisible("input[type='password']")) {
                System.out.println("DEBUG - Found input[type='password'], filling it");
                page.fill("input[type='password']", password);
            } else {
                System.out.println("DEBUG - Found input[id='password'], filling it");
                page.fill("input[id='password']", password);
            }
            
            // Click Sign In button
            page.click("button:has-text('Sign in'), button:has-text('Sign In'), input[type='button'][value='Sign in'], input[type='button'][value='Sign In'], #signin_submit, #nextbtn, button[type='submit']");
            System.out.println("Password entered and Sign In clicked");
        } catch (PlaywrightException e) {
            System.out.println("Error entering password: " + e.getMessage());
            takeScreenshot("login_password_error");
            throw e;
        }
        
        // Step 4: Handle optional popups (MFA, Stay signed in, etc.)
        System.out.println("Step 4: Checking for optional popups/MFA");
        try {
            // Wait a moment for any popups to appear
            page.waitForTimeout(1000);
            
            // Check for and dismiss common popups
            if (page.isVisible("button:has-text('Skip')")){
                page.click("button:has-text('Skip')");
                System.out.println("Skipped optional popup");
            }
            if (page.isVisible("button:has-text('Remind Later')")){
                page.click("button:has-text('Remind Later')");
                System.out.println("Dismissed reminder popup");
            }
            if (page.isVisible("button:has-text('Not Now')")){
                page.click("button:has-text('Not Now')");
                System.out.println("Dismissed 'Not Now' popup");
            }
        } catch (Exception e) {
            System.out.println("No popup detected or popup handling failed, continuing...");
        }
        
        // Step 5: Wait for dashboard to load
        System.out.println("Step 5: Waiting for Zoho People dashboard to load");
        try {
            page.waitForURL(
                    url -> url.startsWith("https://people.zoho.com/") &&
                           (url.contains("/zp") || url.contains("/home") || url.contains("/dashboard")),
                    new Page.WaitForURLOptions().setTimeout(15000)
            );
            
            String finalUrl = page.url();
            System.out.println("✅ Login successful! Landed on: " + finalUrl);

            // Save session cookies so future runs skip full login
            context.storageState(new BrowserContext.StorageStateOptions()
                    .setPath(Paths.get(SESSION_STATE_FILE)));
            // Save the account email so we can detect account changes on the next run
            try {
                Files.write(Paths.get(SESSION_META_FILE),
                        ("{\"user\":\"" + email + "\"}").getBytes());
            } catch (Exception metaEx) {
                System.out.println("[Session] Warning: could not write session meta: " + metaEx.getMessage());
            }
            System.out.println("[Session] Session state saved for: " + email);
        } catch (PlaywrightException e) {
            System.out.println("Error waiting for dashboard: " + e.getMessage());
            System.out.println("Current URL: " + page.url());
            takeScreenshot("login_dashboard_wait_error");
            throw e;
        }
    }
    
    /**
     * Waits for the page to be visually ready before taking a screenshot.
     * NETWORKIDLE alone is not enough for SPAs like Zoho People — JS continues
     * rendering after network goes idle, leaving loading spinners on screen.
     * This method waits for common Zoho/SPA loading indicators to disappear.
     */
    protected void waitForPageReady() {
        try {
            // Wait for network to settle
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(10000));
        } catch (Exception ignored) {}
        try {
            // Wait for common Zoho loading spinners/overlays to disappear
            // These are the typical loading indicators found in Zoho People
            String[] spinnerSelectors = {
                ".zp-loading", ".zpLoading", ".zp_loading",
                ".loader", ".loading", "[class*='loading']",
                ".spinner", "[class*='spinner']",
                ".overlay", "[class*='overlay'][style*='display: block']",
                "img[src*='loading']", "img[src*='loader']", "img[alt*='loading' i]"
            };
            for (String spinner : spinnerSelectors) {
                try {
                    if (page.isVisible(spinner)) {
                        page.waitForSelector(spinner,
                                new Page.WaitForSelectorOptions()
                                        .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                                        .setTimeout(5000));
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        // Final short settle — lets any CSS transitions/animations finish painting
        page.waitForTimeout(500);
    }

    /**
     * Saves a screenshot to disk ONLY — does NOT attach to Allure.
     * Used by @AfterMethod for backup/debugging purposes.
     */
    private void saveScreenshotDiskOnly(String name) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = String.format("screenshots/%s_%s.png", name, timestamp);
            new File("screenshots").mkdirs();
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filename)));
            System.out.println("Backup screenshot saved: " + filename);
        } catch (Exception e) {
            System.out.println("Error saving backup screenshot: " + e.getMessage());
        }
    }

    /**
     * Takes a screenshot and saves it with timestamp
     */
    protected void takeScreenshot(String name) {
        waitForPageReady();
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = String.format("screenshots/%s_%s.png", name, timestamp);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filename)));
            
            // Attach to Allure report
            byte[] screenshotBytes = Files.readAllBytes(Paths.get(filename));
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshotBytes), "png");
            
            System.out.println("Screenshot saved: " + filename);
        } catch (Exception e) {
            System.out.println("Error taking screenshot: " + e.getMessage());
        }
    }
    
    /**
     * Takes screenshot on successful test completion
     */
    protected void takeScreenshotOnSuccess(String testName) {
        takeScreenshot("success_" + testName);
    }

    /**
     * Takes a screenshot of a specific element by CSS selector and attaches it to the Allure report.
     * Always attaches two screenshots to the Allure report:
     *   1. Full-page screenshot of the current page state.
     *   2. Element-scoped screenshot cropped to the asserted element (if visible).
     *      If the element is hidden or has no bounding box, attaches a second
     *      full-page screenshot labelled to indicate the element could not be cropped.
     */
    protected void takeElementScreenshot(String selector, String name) {
        waitForPageReady();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        // --- 1. Full-page screenshot (always first) ---
        try {
            String fullPageFile = String.format("screenshots/%s_fullpage_%s.png", name, timestamp);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fullPageFile)));
            byte[] fullPageBytes = Files.readAllBytes(Paths.get(fullPageFile));
            Allure.addAttachment("1. Full Page — " + name, "image/png",
                    new ByteArrayInputStream(fullPageBytes), "png");
            System.out.println("Full-page screenshot saved: " + fullPageFile);
        } catch (Exception e) {
            System.out.println("Error taking full-page screenshot: " + e.getMessage());
        }

        // --- 2. Element-scoped screenshot (below full-page) ---
        try {
            String elementFile = String.format("screenshots/%s_element_%s.png", name, timestamp);
            Locator locator = page.locator(selector);
            boolean foundAndVisible = locator.count() > 0 && locator.first().isVisible();

            if (foundAndVisible) {
                byte[] elementBytes = locator.first().screenshot(
                        new Locator.ScreenshotOptions().setPath(Paths.get(elementFile)));
                Allure.addAttachment("2. Element [" + selector + "] — " + name, "image/png",
                        new ByteArrayInputStream(elementBytes), "png");
                System.out.println("Element screenshot saved [" + selector + "]: " + elementFile);
            } else {
                // Element hidden — attach full-page again with a clear label
                byte[] fallbackBytes = page.screenshot(
                        new Page.ScreenshotOptions().setPath(Paths.get(elementFile)));
                Allure.addAttachment("2. Element [" + selector + "] hidden — full-page fallback", "image/png",
                        new ByteArrayInputStream(fallbackBytes), "png");
                System.out.println("Element [" + selector + "] not visible — full-page fallback attached");
            }
        } catch (Exception e) {
            System.out.println("Error taking element screenshot for [" + selector + "]: " + e.getMessage());
            try {
                byte[] fallback = page.screenshot();
                Allure.addAttachment("2. Element [" + selector + "] error — full-page fallback", "image/png",
                        new ByteArrayInputStream(fallback), "png");
            } catch (Exception ignored) {}
        }
    }
    
    /**
     * Performs logout on a specific page (used by verifyLogout with a disposable context).
     * This is the core logout logic — does not touch the main shared 'page'.
     */
    protected void logoutFromPage(Page targetPage) {
        System.out.println("Starting logout flow...");
        try {
            targetPage.waitForTimeout(1000);

            if (targetPage.isVisible("button[class*='avatar'], [class*='profile-icon'], .user-avatar, [title*='profile' i]")) {
                targetPage.click("button[class*='avatar'], [class*='profile-icon'], .user-avatar, [title*='profile' i]");
                System.out.println("Profile menu clicked");
                targetPage.waitForTimeout(1000);
            } else if (targetPage.isVisible("img[class*='avatar'], [class*='user-info']")) {
                targetPage.click("img[class*='avatar'], [class*='user-info']");
                System.out.println("User info clicked");
                targetPage.waitForTimeout(1000);
            }

            targetPage.waitForSelector(
                    "button:has-text('Logout'), button:has-text('Sign out'), a:has-text('Logout'), a:has-text('Sign out'), [class*='logout']",
                    new Page.WaitForSelectorOptions().setTimeout(5000));
            targetPage.click("button:has-text('Logout'), button:has-text('Sign out'), a:has-text('Logout'), a:has-text('Sign out')");
            System.out.println("Logout button clicked");

            targetPage.waitForURL(url -> url.contains("accounts.zoho.com") || !url.contains("/zp"),
                    new Page.WaitForURLOptions().setTimeout(10000));
            System.out.println("\u2705 Logout successful! Redirected to: " + targetPage.url());
        } catch (Exception e) {
            System.out.println("Warning: Logout encountered an issue: " + e.getMessage());
        }
    }

    /**
     * Performs logout from Zoho People
     */
    protected void logoutFromZohoPeople() {
        logoutFromPage(page);
    }

    // Keep old body here for reference — now delegated to logoutFromPage(page)
    @SuppressWarnings("unused")
    private void logoutFromZohoPeople_legacy() {
        System.out.println("Starting logout flow...");
        try {
            page.waitForTimeout(1000);
            
            // Try to find and click the profile/user menu icon
            if (page.isVisible("button[class*='avatar'], [class*='profile-icon'], .user-avatar, [title*='profile' i]")) {
                page.click("button[class*='avatar'], [class*='profile-icon'], .user-avatar, [title*='profile' i]");
                System.out.println("Profile menu clicked");
                page.waitForTimeout(1000);
            } else if (page.isVisible("img[class*='avatar'], [class*='user-info']")) {
                page.click("img[class*='avatar'], [class*='user-info']");
                System.out.println("User info clicked");
                page.waitForTimeout(1000);
            }
            
            // Step 2: Click logout from the dropdown menu
            // Wait for logout option to appear and click it
            page.waitForSelector("button:has-text('Logout'), button:has-text('Sign out'), a:has-text('Logout'), a:has-text('Sign out'), [class*='logout']", 
                    new Page.WaitForSelectorOptions().setTimeout(5000));
            
            page.click("button:has-text('Logout'), button:has-text('Sign out'), a:has-text('Logout'), a:has-text('Sign out')");
            System.out.println("Logout button clicked");
            
            // Step 3: Wait for redirect to login page
            page.waitForURL(url -> url.contains("accounts.zoho.com") || !url.contains("/zp"), 
                    new Page.WaitForURLOptions().setTimeout(10000));
            
            String finalUrl = page.url();
            System.out.println("✅ Logout successful! Redirected to: " + finalUrl);
            
        } catch (Exception e) {
            System.out.println("Warning: Logout encountered an issue: " + e.getMessage());
            takeScreenshot("logout_error");
            // Continue without throwing - page navigation might have completed
        }
    }
}