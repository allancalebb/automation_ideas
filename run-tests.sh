#!/bin/bash

# Load environment variables from .env
if [ -f .env ]; then
    set -a
    source .env
    set +a
    echo "✓ Loaded credentials from .env"
else
    echo "✗ Error: .env file not found!"
    exit 1
fi

echo ""
echo "================================"
echo "Running Zoho People Tests..."
echo "================================"
echo ""

# Generate timestamp and report names
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
REPORT_NAME="ZP_SmartTests_${TIMESTAMP}"
ALLURE_RESULTS_DIR="target/allure-results-raw/${REPORT_NAME}"
ALLURE_REPORT_DIR="target/allure-reports/${REPORT_NAME}"

echo "📋 Test Report Name: ${REPORT_NAME}"
echo ""
echo "📂 Directory Structure:"
echo "   Raw Results: ${ALLURE_RESULTS_DIR}"
echo "   Report:      ${ALLURE_REPORT_DIR}"
echo ""

# Clean stale root-level allure-results from previous runs
if [ -d "allure-results" ]; then
    rm -rf allure-results
    echo "🧹 Cleaned stale allure-results/ directory"
fi

# Create allure results directory in target
mkdir -p "$ALLURE_RESULTS_DIR"

# Create logs directory
LOGS_DIR="target/logs"
mkdir -p "$LOGS_DIR"
LOG_FILE="${LOGS_DIR}/${REPORT_NAME}.log"

# Run Maven tests with timestamped results directory and capture logs
mvn clean test \
    -Dallure.results.directory="${ALLURE_RESULTS_DIR}" 2>&1 | tee "$LOG_FILE"

# Capture the exit code
TEST_EXIT_CODE=$?

echo ""
echo "================================"
echo "Test Run Complete"
echo "================================"
echo ""
echo "📊 TEST REPORT INFORMATION:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Report Name: ${REPORT_NAME}"
REPORT_URL="file://$(pwd)/target/allure-reports/${REPORT_NAME}/index.html"
echo "Report URL:  $REPORT_URL"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Create target directories
mkdir -p "target/allure-reports"
mkdir -p "target/allure-results-raw"

# Generate and display Allure report - pass the FULL path to results directory
echo "📊 Generating Allure Report: ${REPORT_NAME}..."
mvn allure:report \
    -Dallure.results.directory="${ALLURE_RESULTS_DIR}" \
    > /dev/null 2>&1 || true  # Report generates fine; suppress allure-maven path warnings

# Copy generated report to reports directory
if [ -d "target/site/allure-report" ]; then
    mkdir -p "target/allure-reports/${REPORT_NAME}"
    cp -r "target/site/allure-report"/* "target/allure-reports/${REPORT_NAME}/"
    
    # Reorganize: move supporting files to assets folder
    REPORT_PATH="target/allure-reports/${REPORT_NAME}"
    mkdir -p "${REPORT_PATH}/assets"
    
    # Move files/folders to assets (keep only index.html at root)
    for item in "${REPORT_PATH}"/*; do
        item_name=$(basename "$item")
        if [ "$item_name" != "index.html" ] && [ "$item_name" != "assets" ]; then
            mv "$item" "${REPORT_PATH}/assets/" 2>/dev/null
        fi
    done
    
    echo "✅ Report created at: target/allure-reports/${REPORT_NAME}/index.html"
    echo "   └─ assets/ (app.js, styles.css, data, plugins, widgets, etc.)"
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "✅ TEST EXECUTION AND REPORT GENERATION COMPLETE!"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "📊 ALLURE REPORT LOCATION (Copy & Paste to Browser):"
echo ""
REPORT_URL="file://$(pwd)/target/allure-reports/${REPORT_NAME}/index.html"
echo "🔗 $REPORT_URL"
echo ""
echo "📋 Quick Copy:"
echo "   Command: open \"$REPORT_URL\""
echo ""

# Log report location to file for easy reference
LATEST_REPORT_FILE="LATEST_REPORT.txt"
echo "Report generated: ${REPORT_NAME}" > "$LATEST_REPORT_FILE"
echo "Allure URL: $REPORT_URL" >> "$LATEST_REPORT_FILE"
echo "Timestamp: $(date)" >> "$LATEST_REPORT_FILE"
echo "✓ Report location saved to: $LATEST_REPORT_FILE"
echo ""

echo "📝 View all test reports:"
echo "   ./view-reports.sh"
echo ""
echo "════════════════════════════════════════════════════════════════"
echo ""

# Generate simple HTML report automatically
echo "🔨 Generating simple HTML report..."
bash simple-report.sh

# Print simple report URL and save to LATEST_REPORT.txt
SIMPLE_REPORT=$(ls -t target/simple-reports/*.html 2>/dev/null | head -1)
if [ -n "$SIMPLE_REPORT" ]; then
    SIMPLE_REPORT_URL="file://$(pwd)/${SIMPLE_REPORT}"
    echo ""
    echo "════════════════════════════════════════════════════════════════"
    echo "📋 SIMPLE REPORT:"
    echo "🔗 $SIMPLE_REPORT_URL"
    echo "   Command: open \"$SIMPLE_REPORT_URL\""
    echo "════════════════════════════════════════════════════════════════"
    echo "Simple Report URL: $SIMPLE_REPORT_URL" >> "$LATEST_REPORT_FILE"
fi

echo ""
exit $TEST_EXIT_CODE


