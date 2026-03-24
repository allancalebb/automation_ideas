# 📊 Test Reports Organization

Your test reports are now organized with meaningful names that include timestamp and project information.

## Report Naming Convention

```
ZP_SmartTests_YYYY-MM-DD_HH-MM-SS
```

**Example:**
```
ZP_SmartTests_2026-03-23_10-36-12
```

### Structure Breakdown:
- **ZP_SmartTests** — Project name (Zoho People Smart Tests)
- **2026-03-23** — Date (YYYY-MM-DD format)
- **10-36-12** — Time (HH-MM-SS format, 24-hour)

---

## Report Locations

### Test Results Data
```
allure-results-ZP_SmartTests_2026-03-23_10-36-12/
```
Contains raw test execution data (JSON files, screenshots, etc.)

### Allure HTML Report
```
target/allure-reports/ZP_SmartTests_2026-03-23_10-36-12/
```
The actual Allure report you view in the browser

---

## Running Tests & Reports

### Run Tests and Generate Report
```bash
./run-tests.sh
```

Output will show:
```
📋 Test Report Name: ZP_SmartTests_2026-03-23_10-36-12
...
📊 ALLURE REPORT LOCATION:
file:///Users/allan-3577/IdeaProjects/ZP_SmartTests/target/allure-reports/ZP_SmartTests_2026-03-23_10-36-12/index.html
```

### View All Previous Reports
```bash
./view-reports.sh
```

Shows all timestamped reports with links:
```
Available Allure Reports (Latest First):

✅ 2026-03-23_10-36-12
   📁 Report: ZP_SmartTests_2026-03-23_10-36-12
   🔗 file:///Users/allan-3577/IdeaProjects/ZP_SmartTests/target/allure-reports/ZP_SmartTests_2026-03-23_10-36-12/index.html

✅ 2026-03-23_10-34-17
   📁 Report: ZP_SmartTests_2026-03-23_10-34-17
   🔗 file:///Users/allan-3577/IdeaProjects/ZP_SmartTests/target/allure-reports/ZP_SmartTests_2026-03-23_10-34-17/index.html
```

### Open Latest Report (Interactive)
```bash
mvn allure:serve
```

---

## Report History

All timestamped reports are preserved. You can:

- **Compare test results over time** — Each run has its own timestamped report
- **Track test execution history** — See when tests ran and their outcomes
- **Keep audit trail** — All reports are archived by date/time

---

## Cleanup Old Reports (Optional)

To remove old reports and free up space:

```bash
# Keep only the 5 most recent reports
ls -td target/allure-reports/* | tail -n +6 | xargs rm -rf

# Remove all reports from a specific date
rm -rf target/allure-reports/ZP_SmartTests_2026-03-20*
```

---

## File Structure

```
ZP_SmartTests/
├── run-tests.sh                              (Run tests & generate report)
├── view-reports.sh                           (View all reports)
│
├── allure-results-ZP_SmartTests_2026-03-23_10-36-12/  (Latest raw data)
│   ├── *.json                                (Test execution data)
│   └── *.png                                 (Screenshots)
│
├── allure-results-ZP_SmartTests_2026-03-23_10-34-17/  (Previous raw data)
│   └── ...
│
└── target/
    ├── allure-reports/
    │   ├── ZP_SmartTests_2026-03-23_10-36-12/        (Latest report)
    │   │   ├── index.html                   (Main report file)
    │   │   └── ...
    │   │
    │   └── ZP_SmartTests_2026-03-23_10-34-17/        (Previous report)
    │       └── ...
    │
    └── allure-results/                       (Symlink to current results)
        └── ...
```

---

## Benefits of Timestamped Reports

✅ **No overwrites** — Each test run creates a new unique report  
✅ **Traceability** — Know exactly when each test ran  
✅ **Comparison** — Compare results across different test runs  
✅ **Archive** — Keep historical test data for auditing  
✅ **Organized** — Reports are clearly named and easy to find  

---

**Pro Tip:** Integrate with CI/CD by checking the report path from the shell script output!

```bash
# Capture the report path for use in CI/CD
REPORT_PATH=$(./run-tests.sh | grep "ALLURE REPORT LOCATION" | awk '{print $NF}')
echo "Report generated at: $REPORT_PATH"
```
