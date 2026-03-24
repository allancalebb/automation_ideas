# 🚀 Zoho People QA Smart Tests

## Overview
Automated browser testing for **Zoho People** HR management system using **Playwright** and **TestNG**.

**Current Status:** ✅ **ALL TESTS PASSING**

---

## 📋 Quick Start

### 1. Prerequisites
- Java 8+
- Maven 3.6+
- Access to Zoho People account

### 2. Setup Credentials
Edit `.env` file:
```env
ZOHO_USER=your-email@example.com
ZOHO_PASS=your-password
```
⚠️ This file is in `.gitignore` (never committed)

### 3. Run Tests
```bash
./run-tests.sh
```

---

## 🏗️ Project Structure

```
ZP_SmartTests/
├── src/
│   ├── main/java/org/example/
│   │   └── Main.java
│   ├── test/java/com/zohopeopleqa/
│   │   ├── base/
│   │   │   └── BaseTest.java          (Browser + Login setup)
│   │   └── tests/
│   │       └── LeaveBalanceIntegrityTest.java (Test cases)
│   └── resources/
│       └── testng.xml                 (TestNG config)
├── .env                               (Credentials - IGNORED)
├── run-tests.sh                       (Test runner script)
├── pom.xml                            (Maven config)
└── screenshots/                       (Test evidence)
```

---

## 🧪 Available Tests

### ✅ Login Test (ACTIVE)
**Test Name:** `verifyLoginAndDashboard`  
**Status:** PASSING  
**What it tests:**
- Navigate to Zoho People
- Two-step login process
- Dashboard access verification
- Screenshot capture on success

**Duration:** ~17 seconds

### 🔄 Logout Test (DISABLED)
**Test Name:** `verifyLogout`  
**Status:** DISABLED (ready to enable)  
**What it tests:**
- User logout functionality
- Redirect after logout
- Session termination

---

## 🛠️ Running Tests

### Option 1: Shell Script (Recommended)
```bash
./run-tests.sh
```
✅ Loads credentials from `.env`  
✅ Runs tests  
✅ Displays report link

### Option 2: Maven Direct
```bash
source .env && mvn clean test
```

### Option 3: IDE
Run directly in IntelliJ IDEA or Eclipse:
- Right-click `LeaveBalanceIntegrityTest.java`
- Select "Run"

---

## 📊 Test Reports

### Surefire Report (Default)
```bash
open target/surefire-reports/index.html
```

### Allure Report (Recommended)
```bash
mvn allure:report
mvn allure:serve
```

Features:
- 📸 Screenshots embedded
- 📝 Step-by-step breakdown
- 📊 Statistics & charts
- 🎯 Epic/Feature organization

---

## 🔑 Key Features

✅ **Robust Login Flow**
- Handles Zoho's two-step authentication
- Waits for all elements (15 second timeouts)
- Tolerant of UI changes (multiple selector fallbacks)

✅ **Auto Screenshot on Success**
- Captures dashboard after login
- Embeds in Allure reports
- Saves locally to `screenshots/` folder

✅ **Error Handling**
- Screenshots on failure
- Detailed error messages
- Allure attachments for debugging

✅ **Allure Integration**
- Epic/Feature/Story organization
- Critical severity marking
- Step-by-step execution trace
- Automatic timestamp

---

## 📝 Test Output Example

```
Testing login with email: user@example.com
Starting Zoho People login flow...
Step 1: Navigating to https://people.zoho.com
Waiting for Sign In button...
Sign In button clicked
Step 2: Entering email and clicking Next
Email entered and Next clicked
Step 3: Waiting for password field and entering password
Password entered and Sign In clicked
Step 4: Checking for optional popups/MFA
Step 5: Waiting for Zoho People dashboard to load
✅ Login successful! Landed on: https://people.zoho.com/...
Screenshot saved: screenshots/success_login_success_*.png
✅ Test PASSED: Login successful

[INFO] Tests run: 1, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS
```

---

## 🎯 Test Architecture

### BaseTest.java
- Browser initialization (Playwright Chromium)
- Login method with 5 steps
- Logout method
- Screenshot utilities
- Allure integration

### LeaveBalanceIntegrityTest.java
- Test cases using @Test annotation
- Epic/Feature/Story organization
- Severity levels (CRITICAL, NORMAL)
- Detailed descriptions
- Screenshot assertions

---

## 🔐 Credentials Management

### ✅ Secure Approach
```env
# .env file (NOT committed)
ZOHO_USER=your-email@example.com
ZOHO_PASS=your-password
```

### Load Before Running
```bash
source .env
mvn clean test
```

### CI/CD (GitHub Actions)
```yaml
env:
  ZOHO_USER: ${{ secrets.ZOHO_USER }}
  ZOHO_PASS: ${{ secrets.ZOHO_PASS }}
```

---

## 📦 Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Playwright | 1.42.0 | Browser automation |
| TestNG | 7.9.0 | Test framework |
| Allure | 2.25.0 | Advanced reporting |
| Maven | 3.2.5+ | Build tool |

---

## 🚦 Test Results Summary

```
├── verifyLoginAndDashboard
│   ├── Status: ✅ PASSED
│   ├── Duration: ~17s
│   ├── Severity: CRITICAL
│   └── Screenshot: ✅ Captured
│
└── verifyLogout (disabled)
    ├── Status: 🔄 DISABLED
    ├── Ready: YES
    └── Enable: Change enabled = false → true
```

---

## 🐛 Troubleshooting

### Tests failing?
```bash
# 1. Check credentials
cat .env

# 2. Clean and rebuild
mvn clean

# 3. Run with verbose output
mvn test -X
```

### Browser issues?
```bash
# Install Playwright browsers
mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"
```

### Report not showing?
```bash
# Clear allure results
rm -rf allure-results/
mvn allure:report
```

---

## 🎓 Learning Resources

- [Playwright Java Docs](https://playwright.dev/java/)
- [TestNG Documentation](https://testng.org/doc/)
- [Allure Framework](https://docs.qameta.io/)
- [Maven Guide](https://maven.apache.org/guides/)

---

## 📈 Future Enhancements

- [ ] Logout test implementation
- [ ] Leave balance validation tests
- [ ] Attendance tracking tests
- [ ] Parallel execution support
- [ ] Integration with CI/CD pipeline
- [ ] Email notifications on failure
- [ ] Performance metrics

---

## 📞 Support

For issues or questions:
1. Check TEST_REPORT_GUIDE.md
2. Review console output
3. Check screenshots in `screenshots/` folder
4. Review Allure report for detailed steps

---

## ✨ What's New

**Version 1.0**
- ✅ Login test with Playwright
- ✅ Allure reporting integration
- ✅ Screenshot capture
- ✅ Detailed error handling
- ✅ .env credential management
- ✅ Shell script runner

---

**Project Status:** Active Development  
**Last Updated:** 2026-03-19  
**Maintainer:** QA Team  
**License:** MIT
