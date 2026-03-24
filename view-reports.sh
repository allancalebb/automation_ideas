#!/bin/bash

echo "📊 Zoho People Test Reports History"
echo "===================================="
echo ""

# Check if reports directory exists
if [ ! -d "target/allure-reports" ]; then
    echo "❌ No test reports found. Run tests first:"
    echo "   ./run-tests.sh"
    exit 1
fi

echo "Available Allure Reports (Latest First):"
echo ""

# List all timestamped reports in reverse chronological order
ls -td target/allure-reports/ZP_SmartTests_* 2>/dev/null | while read report_dir; do
    report_name=$(basename "$report_dir")
    report_file="${report_dir}/index.html"
    
    if [ -f "$report_file" ]; then
        # Extract timestamp from directory name
        timestamp=$(echo "$report_name" | sed 's/ZP_SmartTests_//')
        
        # Get test results summary if available
        if [ -f "${report_dir}/assets/data/test-cases/test-cases.json" ]; then
            summary="(with test data)"
        else
            summary=""
        fi
        
        echo "✅ $timestamp $summary"
        echo "   📁 Report: $report_name"
        echo "   📄 Entry: index.html"
        echo "   📦 Assets: assets/ (app.js, styles.css, data, plugins, widgets, etc.)"
        echo "   🔗 Open: file://$(pwd)/${report_file}"
        echo ""
    fi
done

echo ""
echo "📝 Open a specific report in browser:"
echo "   open target/allure-reports/ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/index.html"
echo ""
echo "🚀 Or open latest report in interactive mode:"
echo "   mvn allure:serve"
echo ""

