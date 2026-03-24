# Report Organization Guide

## New Project Structure

After organizing your Allure reports, your project now has a clean structure:

```
ZP_SmartTests/
├── target/
│   ├── allure-reports/                 ← All organized reports here
│   │   ├── ZP_SmartTests_2026-03-23_11-03-57/
│   │   │   ├── index.html              ← Main entry point
│   │   │   └── assets/                 ← Supporting files
│   │   │       ├── app.js
│   │   │       ├── styles.css
│   │   │       ├── favicon.ico
│   │   │       ├── data/               ← Test data (JSON)
│   │   │       ├── plugins/            ← Allure plugins
│   │   │       ├── widgets/            ← UI components
│   │   │       ├── export/             ← Export formats
│   │   │       └── history/            ← Historical data
│   │   └── ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/  ← More reports...
│   ├── classes/                         ← Compiled test code
│   ├── site/                            ← Temporary build artifacts
│   └── test-classes/                    ← Compiled test files
├── src/
│   ├── main/                           ← Production code
│   │   └── java/com/zohopeopleqa/
│   │       └── mcp/                    ← MCP Test Data Client
│   └── test/                           ← Test code
│       └── java/com/zohopeopleqa/
│           ├── base/                   ← BaseTest
│           ├── tests/                  ← Test classes
│           └── utils/                  ← EnvLoader, utilities
├── allure-results/                      ← Raw test results from last run
├── screenshots/                         ← Test execution screenshots
├── mcp-servers/
│   └── test-data/                      ← MCP Test Data Server
├── pom.xml                              ← Maven configuration
├── run-tests.sh                         ← Run tests + generate report
├── organize-reports.sh                  ← Reorganize existing reports
├── cleanup-reports.sh                   ← Clean up legacy folders
└── view-reports.sh                      ← List all reports
```

## Report Organization Benefits

### ✅ Clean Root Level
- Only `index.html` at each report root for quick access
- All supporting files neatly organized in `assets/` subfolder
- Easier to navigate and understand report structure

### ✅ Timestamped Reports
- Each report has unique timestamp: `ZP_SmartTests_YYYY-MM-DD_HH-MM-SS`
- Full test history maintained
- Easy to compare multiple test runs

### ✅ Easy Access
```bash
# View all reports
./view-reports.sh

# Open specific report (macOS)
open target/allure-reports/ZP_SmartTests_2026-03-23_11-03-57/index.html

# Or use file:// URL in browser
file:///Users/allan-3577/IdeaProjects/ZP_SmartTests/target/allure-reports/ZP_SmartTests_2026-03-23_11-03-57/index.html
```

## Automated Organization

When you run tests with the updated scripts, reports are **automatically organized**:

```bash
./run-tests.sh
# ✅ Runs tests
# ✅ Generates Allure report
# ✅ Automatically organizes into assets/ folder
```

## Scripts Reference

### `run-tests.sh`
- Executes test suite with Playwright
- Generates timestamped Allure report
- **Automatically organizes** report structure
- Saves screenshots and results

### `organize-reports.sh`
- Reorganizes **existing reports** into new structure
- Moves supporting files to `assets/`
- Keeps only `index.html` at root level

### `cleanup-reports.sh`
- Removes old `allure-results-*` directories from root
- Keeps raw results in central `allure-results/` directory
- Cleans up project root for better organization

### `view-reports.sh`
- Lists all test reports in reverse chronological order (latest first)
- Shows full file paths and URLs
- Helps quickly access specific reports

## Files Organization Summary

```
Each Report Directory:
  index.html                    665 B   ← Entry point
  assets/                               ← Supporting files
    ├── app.js               772 KB    ← Allure application
    ├── styles.css         1.5 MB     ← Styling
    ├── favicon.ico          15 KB    ← Browser icon
    ├── data/                         ← Test data (JSON)
    │   ├── test-cases/
    │   ├── suites/
    │   └── categories/
    ├── plugins/                      ← Allure features
    ├── widgets/                      ← UI widgets
    ├── export/                       ← Export formats
    └── history/                      ← Historical trends
```

## Best Practices

1. **Run tests regularly**: `./run-tests.sh`
2. **Review reports**: `./view-reports.sh` → open latest
3. **Archive old reports**: Keep `target/allure-reports/` organized
4. **Clean root**: Run `./cleanup-reports.sh` periodically
5. **Delete old reports**: Remove specific `ZP_SmartTests_*/` directories if needed

## Summary of Changes

| Aspect | Before | After |
|--------|--------|-------|
| Root clutter | Many `allure-results-*` folders | Clean root, organized in `target/` |
| Report structure | Files mixed at root level | `index.html` + `assets/` subfolder |
| Navigation | Hard to find specific report | Timestamped, easily browsable |
| Space efficiency | Unclear file organization | Clear separation of concerns |
| Access | Manual path tracking needed | `./view-reports.sh` shows all |

---

**Your reports are now organized and ready for analysis!** 📊
