# 📊 Test Reports Management

## Overview
Each test run now creates a **timestamped report** so you never lose previous test results.

---

## 📁 Report Location Structure

```
target/
├── surefire-reports-2026-03-19_13-07-09/    (Latest run)
│   ├── index.html                            (Main report)
│   └── [test classes and details]
├── surefire-reports-2026-03-19_13-05-00/    (Previous run)
│   └── index.html
├── surefire-reports-2026-03-19_13-00-30/    (Earlier run)
│   └── index.html
└── site/allure-maven-plugin/                (Allure report)
    └── index.html
```

---

## 🚀 View Reports

### Option 1: View Report History
```bash
./view-reports.sh
```

Shows all available reports with timestamps and links.

### Option 2: View Latest Report
```bash
./run-tests.sh
```

Prints the latest report link at the end.

### Option 3: Open Specific Report
```bash
open target/surefire-reports-2026-03-19_13-07-09/index.html
```

### Option 4: View All Reports in Finder
```bash
open target/
```

Browse all timestamped report directories.

---

## 📋 Report Details

Each timestamped report contains:

```
index.html              (Main dashboard)
summary.html           (Summary view)
EmailableReport.html   (Email-friendly format)
Test classes/
├── LeaveBalanceIntegrityTest.html
└── [other tests]
Results/
├── testng-results.xml  (XML format)
└── [detailed results]
```

---

## 🔍 Understanding Report Names

Report directory format: `surefire-reports-YYYY-MM-DD_HH-MM-SS`

Example: `surefire-reports-2026-03-19_13-07-09`
- **Date:** 2026-03-19 (March 19, 2026)
- **Time:** 13:07:09 (1:07 PM, 9 seconds)

---

## 📊 Comparing Reports

To compare test results over time:

1. Open two reports side-by-side
2. Check execution times
3. Review pass/fail history
4. Analyze any failures

---

## 🧹 Cleanup Old Reports

To keep only last N reports:

```bash
# Keep only last 5 reports (delete older ones)
ls -td target/surefire-reports-* | tail -n +6 | xargs rm -rf
```

Or manually by date:
```bash
# Delete reports older than 30 days
find target/surefire-reports-* -mtime +30 -exec rm -rf {} \;
```

---

## 📈 Report Statistics

View execution trends:

```bash
# Count total reports
ls -1 target/surefire-reports-* | wc -l

# List by newest first
ls -td target/surefire-reports-*

# List by oldest first
ls -tr target/surefire-reports-*
```

---

## 🎨 Allure Reports

For more advanced reporting:

```bash
# Generate and view Allure report
mvn allure:serve

# View without serving
open target/site/allure-maven-plugin/index.html
```

Allure provides:
- 📊 Statistics & trends
- 📸 Screenshots
- 📝 Step breakdown
- 🎯 Test categories
- 📅 History timeline

---

## 🔄 Automation Setup

Each run automatically:
- ✅ Creates timestamped report directory
- ✅ Runs Maven tests with custom report path
- ✅ Generates Allure report
- ✅ Prints links to both reports
- ✅ Preserves all previous reports

---

## 🛠️ If Reports Are Lost

If you accidentally cleaned reports:

```bash
# Rerun tests to generate new reports
./run-tests.sh

# Or rebuild everything
mvn clean test
```

---

## 📚 Scripts

| Script | Purpose |
|--------|---------|
| `run-tests.sh` | Run tests + create timestamped reports |
| `view-reports.sh` | List all available reports |

Both support viewing report history!

---

## ✨ Benefits

- ✅ Never lose test results
- ✅ Easy comparison over time
- ✅ Track test trends
- ✅ Archive results for compliance
- ✅ Debug via historical data
- ✅ Performance tracking

---

## 🎯 Quick Commands

```bash
# Run tests and see latest report link
./run-tests.sh

# View all reports history
./view-reports.sh

# Open target directory in Finder
open target/

# See which reports exist
ls -td target/surefire-reports-*
```

---

**Now each test run creates a unique timestamped report!** 📊✨
