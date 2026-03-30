package com.zohopeopleqa.base;

import com.microsoft.playwright.*;
import com.zohopeopleqa.config.Config;
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

    // Instance-level — each test class thread gets its own complete Playwright stack.
    // Playwright Java is NOT thread-safe across threads; each Playwright instance
    // must be used from a single thread. This is the correct approach for parallel classes.
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    // Lock for synchronized session-state file I/O only (avoids concurrent write corruption)
    private static final Object SESSION_LOCK = new Object();

    private FileOutputStream testLogStream;
    private String currentLogPath;
    private static final String LOGS_DIR = "target/logs";

    // Thread-safe per-test log capture — routes System.out to the calling thread's log file.
    // Installed ONCE in @BeforeSuite; each @BeforeMethod registers / @AfterMethod deregisters.
    // Public so AllureAttachmentListener can flush the stream before reading the log file.
    public static final java.util.concurrent.ConcurrentHashMap<Long, FileOutputStream> THREAD_LOG_STREAMS =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile PrintStream REAL_OUT = null;

    // Thread-locals exposed to AllureAttachmentListener which reads them in onTestSuccess/onTestFailure
    public static final ThreadLocal<String> CURRENT_LOG_PATH = new ThreadLocal<>();
    public static final ThreadLocal<Page> CURRENT_PAGE = new ThreadLocal<>();

    private static synchronized void installThreadAwarePrintStream() {
        if (REAL_OUT != null) return; // already installed by another thread
        REAL_OUT = System.out;
        System.setOut(new PrintStream(new java.io.OutputStream() {
            private FileOutputStream currentLog() {
                return THREAD_LOG_STREAMS.get(Thread.currentThread().getId());
            }
            public void write(int b) throws java.io.IOException {
                REAL_OUT.write(b);
                FileOutputStream log = currentLog();
                if (log != null) try { log.write(b); } catch (java.io.IOException ignored) {}
            }
            public void write(byte[] b, int off, int len) throws java.io.IOException {
                REAL_OUT.write(b, off, len);
                FileOutputStream log = currentLog();
                if (log != null) try { log.write(b, off, len); } catch (java.io.IOException ignored) {}
            }
            public void flush() throws java.io.IOException {
                REAL_OUT.flush();
                FileOutputStream log = currentLog();
                if (log != null) try { log.flush(); } catch (java.io.IOException ignored) {}
            }
        }, true));
    }

    protected static final String SESSION_STATE_FILE = "session-state.json";
    protected static final String SESSION_META_FILE  = "session-meta.json";

    private static final String RUN_STARTED_AT =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    /**
     * Runs ONCE before any parallel test classes start.
     * 1. Writes Allure environment.properties
     * 2. Ensures ONE valid session-state.json exists (login if needed)
     *
     * By doing all login work here (single-threaded), we guarantee that every
     * parallel @BeforeClass only needs to READ the saved session file — no login,
     * no race conditions, no concurrent browser creation during login.
     */
    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        // Install the thread-aware PrintStream ONCE — must happen before any parallel class starts
        installThreadAwarePrintStream();

        // --- 1. Write Allure environment.properties ---
        try {
            String allureDir = System.getProperty("allure.results.directory", "target/allure-results");
            Files.createDirectories(Paths.get(allureDir));
            String user = com.zohopeopleqa.utils.EnvLoader.get("ZOHO_USER", "unknown");
            boolean headless = Boolean.parseBoolean(System.getenv().getOrDefault("PLAYWRIGHT_HEADLESS", "false"));
            String props = String.join("\n",
                    "Environment="  + Config.ENV.toUpperCase(),
                    "Target.URL="   + Config.BASE_URL,
                    "User="         + user,
                    "Browser=Chromium (Playwright)",
                    "Headless="     + headless,
                    "Run.Started="  + RUN_STARTED_AT,
                    "Java.Version=" + System.getProperty("java.version"),
                    "OS="           + System.getProperty("os.name") + " " + System.getProperty("os.version")
            );
            Files.write(Paths.get(allureDir, "environment.properties"), props.getBytes());
            System.out.println("[Allure] environment.properties written → " + Config.ENV.toUpperCase() + " / " + Config.BASE_URL);
        } catch (Exception e) {
            System.out.println("[Allure] Warning: could not write environment.properties: " + e.getMessage());
        }

        // --- 2. Ensure a valid session exists ---
        String email    = com.zohopeopleqa.utils.EnvLoader.get("ZOHO_USER");
        String password = com.zohopeopleqa.utils.EnvLoader.get("ZOHO_PASS");
        java.nio.file.Path sessionPath = Paths.get(SESSION_STATE_FILE);
        java.nio.file.Path metaPath    = Paths.get(SESSION_META_FILE);

        // Discard session if ZOHO_USER or environment has changed
        if (Files.exists(sessionPath) && Files.exists(metaPath)) {
            try {
                String meta = new String(Files.readAllBytes(metaPath));
                if (!meta.contains("\"" + email + "\"")) {
                    Files.deleteIfExists(sessionPath);
                    Files.deleteIfExists(metaPath);
                    System.out.println("[Suite] ZOHO_USER changed — discarding stale session.");
                } else if (!meta.contains("\"env\":\"" + Config.ENV + "\"")) {
                    Files.deleteIfExists(sessionPath);
                    Files.deleteIfExists(metaPath);
                    System.out.println("[Suite] Environment changed to " + Config.ENV.toUpperCase() + " — discarding stale session.");
                }
            } catch (Exception ignored) {}
        }

        // Validate existing session (headless, fast)
        if (Files.exists(sessionPath)) {
            if (suiteValidateSession(sessionPath)) {
                System.out.println("[Suite] Existing session is valid — skipping login.");
                return;
            }
            // Session expired
            try { Files.deleteIfExists(sessionPath); Files.deleteIfExists(metaPath); } catch (Exception ignored) {}
        }

        // No valid session — perform exactly ONE login now (before parallel classes start)
        System.out.println("[Suite] No valid session — logging in as: " + email);
        suiteDoLogin(email, password, sessionPath, metaPath);
        System.out.println("[Suite] Login complete — session saved.");
    }

    /** Validates the saved session by loading it in a headless browser and checking the landed URL. */
    private boolean suiteValidateSession(java.nio.file.Path sessionPath) {
        Playwright pw = Playwright.create();
        try {
            Browser b  = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext ctx = b.newContext(new Browser.NewContextOptions().setStorageStatePath(sessionPath));
            Page p = ctx.newPage();
            p.setDefaultTimeout(20000);
            p.setDefaultNavigationTimeout(20000);
            p.navigate(Config.BASE_URL);
            p.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                    new Page.WaitForLoadStateOptions().setTimeout(20000));
            p.waitForTimeout(3000);
            String url = p.url();
            ctx.close(); b.close();
            boolean valid = !url.contains("accounts.zoho.com") && !url.contains("/signin");
            System.out.println("[Suite] Session validation → " + (valid ? "VALID" : "EXPIRED") + " at " + url);
            return valid;
        } catch (Exception e) {
            System.out.println("[Suite] Session validation error: " + e.getMessage());
            return false;
        } finally {
            pw.close();
        }
    }

    /**
     * Performs a single login in a VISIBLE (non-headless) browser, saves cookies, then closes.
     * Always non-headless so the user can see the browser and resolve any Zoho challenges
     * (signin-block, CAPTCHA, MFA, daily limit notices) that require manual intervention.
     */
    private void suiteDoLogin(String email, String password,
                              java.nio.file.Path sessionPath, java.nio.file.Path metaPath) {
        Playwright pw = Playwright.create();
        try {
            // Always launch VISIBLE — user must be able to see and interact with Zoho challenges
            Browser b   = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext ctx = b.newContext();
            Page p = ctx.newPage();
            p.setDefaultTimeout(30000);
            p.setDefaultNavigationTimeout(30000);

            // Step 1: Navigate and click Sign In
            System.out.println("[Suite] Navigating to " + Config.BASE_URL);
            p.navigate(Config.BASE_URL);
            try {
                p.click("a:has-text('Sign In'), button:has-text('Sign In')",
                        new Page.ClickOptions().setTimeout(10000));
                p.waitForURL(url -> url.contains("accounts.zoho.com") || url.contains("signin"),
                        new Page.WaitForURLOptions().setTimeout(10000));
            } catch (Exception e) {
                // May already be on the login page (e.g. redirect happened automatically)
                System.out.println("[Suite] Sign In click skipped — already on auth page: " + p.url());
            }

            // Step 2: Email
            p.waitForSelector("input[type='email'], input[id='login_id'], input[name='login_id']",
                    new Page.WaitForSelectorOptions().setTimeout(15000));
            if (p.isVisible("input[type='email']")) p.fill("input[type='email']", email);
            else if (p.isVisible("input[id='login_id']"))  p.fill("input[id='login_id']", email);
            else p.locator("input[type='email'], input[name='username'], input[id='login_id']").first().fill(email);
            p.click("button:has-text('Next'), #nextbtn, button[type='submit']");

            // Step 3: Password
            p.waitForSelector("input[type='password'], input[name='password']",
                    new Page.WaitForSelectorOptions().setTimeout(15000));
            p.waitForTimeout(500);
            if (p.isVisible("input[type='password']")) p.fill("input[type='password']", password);
            else p.fill("input[id='password']", password);
            p.click("button:has-text('Sign in'), button:has-text('Sign In'), #signin_submit, #nextbtn, button[type='submit']");

            // Step 4: Dismiss optional popups
            p.waitForTimeout(2000);
            for (String btn : new String[]{"Skip", "Remind Later", "Not Now"}) {
                try { if (p.isVisible("button:has-text('" + btn + "')")) p.click("button:has-text('" + btn + "')"); }
                catch (Exception ignored) {}
            }

            // Step 5: Wait for app to load — with fallback for Zoho challenge pages
            p.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                    new Page.WaitForLoadStateOptions().setTimeout(30000));
            p.waitForTimeout(2000);
            String finalUrl = p.url();
            System.out.println("[Suite] Post-login URL: " + finalUrl);

            // Detect Zoho challenge/block pages (signin-block, rate-limit, CAPTCHA, announcement)
            if (finalUrl.contains("accounts.zoho.com")) {
                System.out.println();
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println("⚠️  ZOHO CHALLENGE DETECTED — MANUAL ACTION REQUIRED");
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println("  A browser window has opened showing a Zoho challenge.");
                System.out.println("  Please complete the login manually in that window.");
                System.out.println("  Waiting up to 5 minutes for you to finish...");
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println();

                // Wait for user to resolve the challenge and land on the Zoho People app
                p.setDefaultTimeout(300000);
                p.setDefaultNavigationTimeout(300000);
                p.waitForURL(url -> !url.contains("accounts.zoho.com"),
                        new Page.WaitForURLOptions().setTimeout(300000));
                p.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                        new Page.WaitForLoadStateOptions().setTimeout(30000));
                p.waitForTimeout(3000);
                finalUrl = p.url();
                System.out.println("[Suite] Manual login resolved — now at: " + finalUrl);
            }

            if (finalUrl.contains("accounts.zoho.com") || finalUrl.contains("/signin")) {
                throw new RuntimeException("Login failed — still on auth page: " + finalUrl);
            }

            // Save session
            ctx.storageState(new BrowserContext.StorageStateOptions().setPath(sessionPath));
            Files.write(metaPath, ("{\"user\":\"" + email + "\",\"env\":\"" + Config.ENV + "\"}").getBytes());
            System.out.println("[Suite] Session saved for: " + email);
            ctx.close(); b.close();
        } catch (Exception e) {
            throw new RuntimeException("[Suite] Login failed: " + e.getMessage(), e);
        } finally {
            pw.close();
        }
    }

    /**
     * Runs once per test class (each in its own parallel thread).
     * Session is guaranteed to exist from @BeforeSuite — just load it.
     * No login logic here; no locks needed (read-only file access).
     */
    @BeforeClass
    public void loginOnce() {
        boolean headless = Boolean.parseBoolean(
                System.getenv().getOrDefault("PLAYWRIGHT_HEADLESS", "false"));

        // Stamp Allure run-context labels
        String zohoUser = com.zohopeopleqa.utils.EnvLoader.get("ZOHO_USER", "unknown");
        Allure.label("environment", Config.ENV.toUpperCase());
        Allure.label("host", Config.BASE_DOMAIN);
        Allure.parameter("Target Environment", Config.ENV.toUpperCase() + " \u2192 " + Config.BASE_URL);
        Allure.parameter("Test User",          zohoUser);
        Allure.parameter("Browser",            "Chromium (Playwright)");
        Allure.parameter("Run Started",        RUN_STARTED_AT);

        // Each thread gets its own Playwright + Browser + Context — Playwright is NOT thread-safe
        // across threads. But all threads simply LOAD the session file; no login occurs here.
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));

        java.nio.file.Path sessionPath = Paths.get(SESSION_STATE_FILE);
        if (!Files.exists(sessionPath)) {
            throw new RuntimeException("[" + getClass().getSimpleName() + "] session-state.json missing — @BeforeSuite login must have failed.");
        }

        System.out.println("[" + getClass().getSimpleName() + "] Loading saved session (browser headless=" + headless + ")");
        context = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(sessionPath));
        page = context.newPage();
        page.setDefaultTimeout(45000);
        page.setDefaultNavigationTimeout(45000);

        // Navigate to the app — with valid cookies this goes straight in, no login page
        // Retry once if the first attempt times out (TEST env can be slow under parallel load)
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                page.navigate(Config.BASE_URL,
                        new Page.NavigateOptions().setTimeout(60000));
                page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                        new Page.WaitForLoadStateOptions().setTimeout(60000));
                break; // success
            } catch (Exception e) {
                if (attempt == 2) throw e;
                System.out.println("[" + getClass().getSimpleName() + "] Navigation attempt " + attempt + " timed out, retrying...");
                page.waitForTimeout(2000);
            }
        }
        page.waitForTimeout(2000);
        String url = page.url();
        if (url.contains("accounts.zoho.com") || url.contains("/signin")) {
            throw new RuntimeException("[" + getClass().getSimpleName() + "] Session rejected by Zoho — landed at: " + url);
        }
        // Reset to normal test timeout after navigation is done
        page.setDefaultTimeout(15000);
        page.setDefaultNavigationTimeout(15000);
        System.out.println("[" + getClass().getSimpleName() + "] Ready at: " + url);

        // Expose the page instance so AllureAttachmentListener can take failure screenshots
        CURRENT_PAGE.set(page);
    }

    @BeforeMethod
    public void startTestLogging(java.lang.reflect.Method method) {
        // Register this thread's log file in the thread-aware PrintStream installed by @BeforeSuite.
        // System.out.println calls from this thread now write to both the real console AND this file.
        try {
            new File(LOGS_DIR).mkdirs();
            String logPath = LOGS_DIR + "/" + getClass().getSimpleName() + "_" + method.getName() + ".log";
            currentLogPath = logPath;
            CURRENT_LOG_PATH.set(logPath);
            testLogStream = new FileOutputStream(logPath, false);
            THREAD_LOG_STREAMS.put(Thread.currentThread().getId(), testLogStream);
        } catch (Exception ignored) {}
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

        // Deregister this thread's log stream so no further output goes to a closed file,
        // then flush and close it. The log was already attached to Allure by AllureAttachmentListener
        // (which fires before @AfterMethod in the TestNG lifecycle).
        THREAD_LOG_STREAMS.remove(Thread.currentThread().getId());
        CURRENT_LOG_PATH.remove();
        try {
            if (testLogStream != null) { testLogStream.flush(); testLogStream.close(); }
        } catch (Exception ignored) {}
        // context stays open — shared across all tests in this class
    }

    @AfterClass
    public void teardownContext() {
        // Clear the page thread-local before tearing down — prevents stale references in the listener
        CURRENT_PAGE.remove();
        // Tear down the entire per-class Playwright stack
        if (context != null) { try { context.close(); } catch (Exception ignored) {} context = null; page = null; }
        if (browser != null)  { try { browser.close();  } catch (Exception ignored) {} browser = null; }
        if (playwright != null) { try { playwright.close(); } catch (Exception ignored) {} playwright = null; }
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
        System.out.println("Step 1: Navigating to " + Config.BASE_URL);
        try {
            page.navigate(Config.BASE_URL);
            
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
        // Use waitForLoadState + direct URL check instead of waitForURL to handle
        // Zoho's SPA hash-based routing, which may not trigger Playwright navigation events.
        System.out.println("Step 5: Waiting for Zoho People dashboard to load");
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                    new Page.WaitForLoadStateOptions().setTimeout(20000));
            // Allow SPA router to settle after the login redirect
            page.waitForTimeout(2000);
            String finalUrl = page.url();
            System.out.println("Post-login URL: " + finalUrl);

            if (finalUrl.contains("accounts.zoho.com") || finalUrl.contains("signin")) {
                throw new PlaywrightException("Login failed — still on auth page: " + finalUrl);
            }
            if (!finalUrl.contains("zoho.com")) {
                throw new PlaywrightException("Unexpected URL after login (not a Zoho domain): " + finalUrl);
            }

            System.out.println("✅ Login successful! Landed on: " + finalUrl);

            // Save session cookies — synchronized to prevent concurrent writes on first parallel run
            synchronized (SESSION_LOCK) {
                context.storageState(new BrowserContext.StorageStateOptions()
                        .setPath(Paths.get(SESSION_STATE_FILE)));
                try {
                    Files.write(Paths.get(SESSION_META_FILE),
                            ("{\"user\":\"" + email + "\"}").getBytes());
                } catch (Exception metaEx) {
                    System.out.println("[Session] Warning: could not write session meta: " + metaEx.getMessage());
                }
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
            // Wait for the main Zoho People app shell to be present
            page.waitForSelector("#page-wrapper, #servicPageContainer, .zp-mainWrapper, [class*='mainContent']",
                    new Page.WaitForSelectorOptions().setTimeout(8000));
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
        // Final settle — lets CSS transitions/animations finish painting
        page.waitForTimeout(1000);
    }

    /**
     * Saves a screenshot to disk ONLY — does NOT attach to Allure.
     * Used by @AfterMethod for backup/debugging purposes.
     */
    private void saveScreenshotDiskOnly(String name) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String screenshotDir = "screenshots/" + Config.ENV;
            String filename = String.format("%s/%s_%s.png", screenshotDir, name, timestamp);
            new File(screenshotDir).mkdirs();
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
        page.waitForTimeout(1000); // extra settle before capture
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String screenshotDir = "screenshots/" + Config.ENV;
            String filename = String.format("%s/%s_%s.png", screenshotDir, name, timestamp);
            new File(screenshotDir).mkdirs();
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
        page.waitForTimeout(1000); // extra settle before capture
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String screenshotDir = "screenshots/" + Config.ENV;
        new File(screenshotDir).mkdirs();

        // --- 1. Full-page screenshot (always first) ---
        try {
            String fullPageFile = String.format("%s/%s_fullpage_%s.png", screenshotDir, name, timestamp);
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
            String elementFile = String.format("%s/%s_element_%s.png", screenshotDir, name, timestamp);
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