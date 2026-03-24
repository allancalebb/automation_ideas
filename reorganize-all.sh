#!/bin/bash

# Complete Project Structure Reorganization
# Moves all test artifacts into target/ directory for clean project root

echo "🔄 REORGANIZING PROJECT STRUCTURE"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Create target directories if they don't exist
mkdir -p target/allure-reports
mkdir -p target/allure-results-raw
mkdir -p target/screenshots

echo "📂 Creating target subdirectories..."
echo "   ✓ target/allure-reports/      (organized HTML reports)"
echo "   ✓ target/allure-results-raw/  (raw test data JSON/PNG)"
echo "   ✓ target/screenshots/         (test execution screenshots)"
echo ""

# Step 1: Move any root-level allure-results-* directories to target
echo "🔍 Scanning for root-level test result directories..."
moved_results=0
if ls allure-results-* 1> /dev/null 2>&1; then
    for dir in allure-results-*; do
        if [ -d "$dir" ]; then
            echo "   Moving: $dir → target/allure-results-raw/"
            mv "$dir" "target/allure-results-raw/" 2>/dev/null
            moved_results=$((moved_results + 1))
        fi
    done
fi

# Step 2: Move old allure-results directory to target if it exists
if [ -d "allure-results" ]; then
    # Check if it has test data
    file_count=$(find allure-results -type f 2>/dev/null | wc -l)
    if [ "$file_count" -gt 0 ]; then
        echo "   Moving: allure-results/ → target/allure-results-raw/legacy/"
        mkdir -p "target/allure-results-raw/legacy"
        cp -r allure-results/* "target/allure-results-raw/legacy/" 2>/dev/null
        rm -rf allure-results
    fi
fi

# Step 3: Move screenshots directory
if [ -d "screenshots" ]; then
    echo "   Moving: screenshots/ → target/screenshots/"
    cp -r screenshots/* "target/screenshots/" 2>/dev/null
    rm -rf screenshots
fi

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "✅ REORGANIZATION COMPLETE!"
echo "═══════════════════════════════════════════════════════════════"
echo ""

echo "📊 Result Summary:"
echo ""
echo "Raw Test Results Moved: $moved_results"
echo ""

echo "📂 NEW PROJECT STRUCTURE:"
echo ""
echo "   project-root/"
echo "   ├── src/                               (test & main code)"
echo "   ├── pom.xml                           (Maven config)"
echo "   ├── .env                              (credentials)"
echo "   ├── run-tests.sh                      (run & generate reports)"
echo "   ├── view-reports.sh                   (view all reports)"
echo "   ├── organize-reports.sh               (organize reports)"
echo "   ├── cleanup-reports.sh                (cleanup legacy)"
echo "   ├── reorganize-all.sh                 (full reorganization)"
echo "   ├── README.md"
echo "   ├── REPORT_ORGANIZATION.md"
echo "   │"
echo "   └── target/"
echo "       ├── allure-reports/               ← FINALIZED REPORTS"
echo "       │   └── ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/"
echo "       │       ├── index.html            (main entry point)"
echo "       │       └── assets/               (supporting files)"
echo "       │"
echo "       ├── allure-results-raw/           ← RAW TEST DATA"
echo "       │   ├── ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/"
echo "       │   │   ├── *-result.json        (test results)"
echo "       │   │   └── *-attachment.png     (screenshots)"
echo "       │   └── legacy/                   (old results)"
echo "       │"
echo "       ├── screenshots/                  ← ALL SCREENSHOTS"
echo "       │   ├── success_login_success_*.png"
echo "       │   └── ..."
echo "       │"
echo "       ├── site/                         (build artifacts)"
echo "       ├── classes/                      (compiled code)"
echo "       └── ... (other Maven build output)"
echo ""

echo "🚀 QUICK USAGE:"
echo ""
echo "   Run tests:      ./run-tests.sh"
echo "   View reports:   ./view-reports.sh"
echo "   Re-organize:    ./organize-reports.sh"
echo "   Full cleanup:   ./reorganize-all.sh"
echo ""

echo "✨ Your project root is now CLEAN! All test artifacts are in target/"
echo ""
