# 📊 Zoho People QA Test Report Guide

## Test Overview
```
✅ Tests run: 1
✅ Failures: 0
✅ Errors: 0
✅ SUCCESS
```

## 🏃 Running Tests

### Quick Start
```bash
./run-tests.sh
```

### Manual Run
```bash
source .env && mvn clean test
```

---

## 📈 Test Report Locations

### 1. **Surefire HTML Report** (Default TestNG Report)
Open this file in your browser:
```
target/surefire-reports/index.html
```

**Contains:**
- Test execution summary
- Pass/Fail statistics
- Test class details
- Execution times

---

### 2. **Allure Report** (Enhanced Report - RECOMMENDED ⭐)
Generate and view Allure reports:

```bash
# Generate Allure report
mvn allure:report

# Open in browser (opens automatically)
mvn allure:serve
```

**Features:**
- ✅ Visual test status (Pass/Fail/Skipped)
- 📸 Embedded screenshots
- 📝 Test steps breakdown
- ⏱️ Execution timeline
- 📊 Statistics and charts
- 🏷️ Epic, Feature, Story organization
- 📎 Attachments (screenshots, logs)

---

## 🧪 Test Details

### Test 1: `verifyLoginAndDashboard`
**Status:** ✅ PASSED

**What it does:**
1. Navigates to https://people.zoho.com
2. Clicks "Sign In" button
3. Enters email credentials
4. Enters password
5. Handles optional popups
6. Waits for dashboard to load
7. Captures screenshot on success
8. Verifies correct URL

**Duration:** ~17 seconds

**Evidence:**
- ✅ Screenshot: `success_login_success_*.png`
- ✅ Final URL: `https://people.zoho.com/.../zp#home/myspace/overview-actionlist`

---

## 📸 Screenshots

All test screenshots are saved to:
```
screenshots/
├── success_login_success_2026-03-19_13-04-17.png
├── login_error_*.png (if errors occur)
└── logout_*.png (when logout test runs)
```

Screenshots are:
- ✅ Saved locally on disk
- ✅ Embedded in Allure report
- ✅ Attached to HTML report

---

## 📋 Test Metrics

| Metric | Value |
|--------|-------|
| Total Tests | 1 |
| Passed | 1 |
| Failed | 0 |
| Skipped | 0 |
| Success Rate | 100% |
| Avg Duration | ~17s |

---

##🔍 Viewing Reports

### Option 1: Surefire Report
```bash
open target/surefire-reports/index.html
```

### Option 2: Allure Report (Recommended)
```bash
# Generates and serves report automatically
mvn allure:serve
```

### Option 3: Command Line Output
```bash
./run-tests.sh
```
Report link printed at the end ↓
```
📊 TEST REPORT:
file:///Users/allan-3577/IdeaProjects/ZP_SmartTests/target/surefire-reports/index.html
```

---

## 🎯 Test Annotations Used

```java
@Epic("Zoho People QA")              // High-level grouping
@Feature("Authentication")            // Feature grouping
@Story("User Login Flow")            // User story
@Severity(SeverityLevel.CRITICAL)    // Importance level
@Description("...")                   // Detailed description
@Step("...")                          // Step breakdown
```

---

## ✨ What's Included

### Login Test Flow
```
Step 1: Navigate to Zoho People ✅
  └─ Navigate to https://people.zoho.com
  └─ Click Sign In button

Step 2: Enter Email ✅
  └─ Fill email field
  └─ Click Next

Step 3: Enter Password ✅
  └─ Fill password field
  └─ Click Sign In

Step 4: Handle Popups ✅
  └─ Dismiss optional dialogs

Step 5: Verify Dashboard ✅
  └─ Wait for dashboard URL
  └─ Capture screenshot
  └─ Assert correct location
```

---

##🚀 Next Steps (Logout Test)

The logout test is currently **disabled** (`enabled = false`).

To enable and run:
1. Find `verifyLogout()` test
2. Change `enabled = false` to `enabled = true`
3. Run tests again: `./run-tests.sh`

---

## 📞 Troubleshooting

### Report not showing?
```bash
# Clear and rebuild
mvn clean
mvn test
```

### Want more details?
```bash
# Verbose output
mvn test -X
```

### Screenshots not appearing?
```bash
# Check screenshots folder
ls -la screenshots/
```

---

## 📚 Resources

- **Allure Docs:** https://docs.qameta.io/allure/
- **Playwright:** https://playwright.dev/java/
- **TestNG:** https://testng.org/doc/
- **Maven:** https://maven.apache.org/

---

**Last Updated:** 2026-03-19  
**Project:** Zoho People Smart Tests  
**Version:** 1.0-SNAPSHOT
