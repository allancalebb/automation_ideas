# Project Structure & Report Organization - FINAL

## ✅ Project Root - Clean & Organized

```
ZP_SmartTests/
│
├── 📋 Source Code & Configuration
│   ├── src/                    (Java source code - tests and main)
│   ├── mcp-servers/            (MCP Test Data Server)
│   ├── pom.xml                 (Maven build config)
│   └── .env                    (Credentials - git ignored)
│
├── 🚀 Scripts (All in Root)
│   ├── run-tests.sh            ← RUN TESTS (generates organized reports)
│   ├── view-reports.sh         ← View all test reports
│   ├── organize-reports.sh     ← Clean up existing reports
│   ├── cleanup-reports.sh      ← Remove legacy folders
│   └── reorganize-all.sh       ← Full project reorganization
│
├── 📖 Documentation
│   ├── README.md
│   ├── REPORT_ORGANIZATION.md
│   ├── REPORTS.md
│   ├── TEST_REPORT_GUIDE.md
│   ├── MCP_QUICKSTART.md
│   └── MCP_INTEGRATION_GUIDE.md
│
└── 📁 target/                  ← ALL BUILD & TEST ARTIFACTS
    ├── allure-reports/         ← HTML REPORTS (use these!)
    ├── allure-results-raw/     ← Raw test data (reference)
    ├── screenshots/            ← Test execution screenshots
    └── ... (standard Maven output)
```

---

## 📊 Report Organization Details

### Main Report Directory
```
target/allure-reports/
├── ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/     ← Timestamped report
│   ├── index.html                        ← OPEN THIS IN BROWSER
│   └── assets/                           ← Supporting files
│       ├── app.js                        (754 KB - Allure app)
│       ├── styles.css                    (1.5 MB - Styling)
│       ├── favicon.ico
│       ├── data/                         (Test results JSON)
│       ├── plugins/                      (Allure features)
│       ├── widgets/                      (UI components)
│       ├── export/                       (Export formats)
│       └── history/                      (Trends & history)
│
└── ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/    ← Previous reports...
```

### Raw Test Data (Reference Only)
```
target/allure-results-raw/
├── ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/
│   ├── *-result.json          (Test execution results)
│   ├── *-attachment.png       (Screenshots)
│   └── *-container.json       (Test suite structure)
│
├── legacy/                     (Old test data from previous runs)
└── ZP_SmartTests_2026-03-23_11-08-13_results/
```

### Screenshots
```
target/screenshots/
├── success_login_success_2026-03-23_11-04-13.png
├── success_logout_success_2026-03-23_11-04-13.png
└── ... (more screenshots)
```

---

## 🎯 Quick Usage Guide

### Run Tests & Generate Report
```bash
./run-tests.sh
```
This will:
1. Load credentials from `.env`
2. Execute test suite
3. Generate Allure report
4. Automatically organize: `target/allure-reports/ZP_SmartTests_*/`
5. Show report URL in console (copy & paste!)

### View Test Reports
```bash
./view-reports.sh
```
Lists all test reports with full paths (latest first)

### Re-organize Existing Reports
```bash
./organize-reports.sh
```
Moves supporting files to `assets/` subfolder for clean structure

### Full Project Cleanup
```bash
./reorganize-all.sh
```
Moves all test artifacts from root to `target/` directory

---

## 📂 Where Reports Are Saved

**Main Reports:**
```
target/allure-reports/ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/index.html
```

**Example URL for Browser:**
```
file:///Users/allan-3577/IdeaProjects/ZP_SmartTests/target/allure-reports/ZP_SmartTests_2026-03-23_11-03-57/index.html
```

**Raw Data:**
```
target/allure-results-raw/ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/
```

---

## 🔄 Complete Test Execution Flow

```
1. ./run-tests.sh
   ↓
2. Load .env credentials
   ↓
3. mvn clean test
   ├── Saves raw results → target/allure-results-raw/ZP_SmartTests_*/
   ├── Takes screenshots → target/screenshots/success_login_success_*.png
   └── Captures test data
   ↓
4. Generate Allure Report
   └── Reads from: target/allure-results-raw/
   ↓
5. Organize Report
   ├── Creates: target/allure-reports/ZP_SmartTests_*/
   ├── Puts: index.html at root
   └── Moves: assets/ subfolder
   ↓
6. Console Output
   └── Shows copyable URL for browser
```

---

## 📋 Benefits of This Structure

✅ **Clean Root Directory**
- Only source code, config, and scripts in project root
- All artifacts contained in `target/`

✅ **Organized Reports**
- Each report has unique timestamp
- Full test history maintained
- Easy to compare multiple runs

✅ **Easy Access**
- `index.html` at report root (quick opening)
- Assets organized in subfolder
- Copyable URLs in console

✅ **Better Navigation**
- `view-reports.sh` shows all available reports
- Timestamped naming prevents overwrites
- Raw data backed up for reference

✅ **Standard Maven Layout**
- Follows Maven conventions
- `mvn clean` removes `target/` safely
- Compatible with CI/CD pipelines

---

## 🛠️ Maintenance Commands

```bash
# View latest report
./view-reports.sh | head -5

# Open specific report in browser
open target/allure-reports/ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/index.html

# Clean all build artifacts (safe - target/ is rebuilt)
mvn clean

# Remove old reports (keep latest N)
cd target/allure-reports && rm -rf ZP_SmartTests_*[older timestamps]*

# See directory structure
tree target/ -L 2
```

---

## 📊 Directory Sizes

- **single report**: ~4.5 MB (app.js 754KB + styles.css 1.5MB + data)
- **raw test data**: ~928 B per run (JSON files + screenshots)
- **screenshots**: ~25 MB (high-resolution PNGs)

---

**Your project is now perfectly organized!** 🎉
