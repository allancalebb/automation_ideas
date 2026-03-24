#!/bin/bash

# Open Latest Allure Report in Browser
# This script automatically starts a web server and opens the report

echo "🚀 Opening latest Allure report in your browser..."
echo ""

# Get the latest report from LATEST_REPORT.txt
LATEST_REPORT_FILE="LATEST_REPORT.txt"

if [ ! -f "$LATEST_REPORT_FILE" ]; then
    echo "❌ Error: No test reports found!"
    echo "Run tests first: ./run-tests.sh"
    exit 1
fi

# Extract report name from file
REPORT_NAME=$(head -1 "$LATEST_REPORT_FILE" | cut -d' ' -f3)
REPORT_DIR="target/allure-reports/${REPORT_NAME}"

if [ ! -d "$REPORT_DIR" ]; then
    echo "❌ Error: Report directory not found: $REPORT_DIR"
    exit 1
fi

echo "📊 Report: $REPORT_NAME"
echo "📂 Location: $REPORT_DIR"
echo ""

# Kill any existing server on port 8000
pkill -f "http.server 8000" 2>/dev/null

# Start the web server from the report directory
cd "$REPORT_DIR"
python3 -m http.server 8000 > /dev/null 2>&1 &
SERVER_PID=$!

# Give the server a moment to start
sleep 2

echo "✅ Web server started (PID: $SERVER_PID)"
echo "🌐 Opening report in browser..."
echo ""
echo "URL: http://localhost:8000"
echo ""

# Open in default browser
if command -v open &> /dev/null; then
    # macOS
    open "http://localhost:8000"
elif command -v xdg-open &> /dev/null; then
    # Linux
    xdg-open "http://localhost:8000"
elif command -v start &> /dev/null; then
    # Windows
    start "http://localhost:8000"
else
    echo "⚠️  Could not auto-open browser. Visit: http://localhost:8000"
fi

echo ""
echo "📝 To stop the server, run:"
echo "   pkill -f 'http.server 8000'"
echo ""
