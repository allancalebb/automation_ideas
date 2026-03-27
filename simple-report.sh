#!/bin/bash

# Generate Simple HTML Test Report with Screenshots
# Creates a basic, clean HTML report from test results with embedded screenshots

echo "📊 Generating simple HTML test report..."

REPORT_DIR="target/simple-reports"
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
REPORT_FILE="${REPORT_DIR}/TestReport_${TIMESTAMP}.html"

mkdir -p "$REPORT_DIR"

# Check if test results exist — use only the latest timestamped run
LATEST_RUN=$(ls -td target/allure-results-raw/ZP_SmartTests_* 2>/dev/null | head -1)
if [ -z "$LATEST_RUN" ] || [ -z "$(find "$LATEST_RUN" -name '*-result.json' 2>/dev/null)" ]; then
    echo "❌ No test results found. Run tests first: ./run-tests.sh"
    exit 1
fi
echo "📂 Reading results from: $(basename $LATEST_RUN)"

# Count from latest run only — broken counts as failed, skipped is separate
PASSED=$(grep -rl '"status":"passed"' "$LATEST_RUN" 2>/dev/null | grep -c 'result.json')
FAILED=$(grep -rEl '"status":"(failed|broken)"' "$LATEST_RUN" 2>/dev/null | grep -c 'result.json')
SKIPPED=$(grep -rl '"status":"skipped"' "$LATEST_RUN" 2>/dev/null | grep -c 'result.json')
TOTAL=$((PASSED + FAILED + SKIPPED))

# Function to extract test name from JSON
extract_test_name() {
    python3 -c "
import json, sys
try:
    d = json.load(open('$1'))
    name = d.get('name', '')
    if not name:
        name = next((l['value'] for l in d.get('labels', []) if l['name'] == 'testMethod'), '')
    print(name)
except:
    print('')
" 2>/dev/null
}

# Function to extract test status from JSON
extract_test_status() {
    grep -o '"status":"[^"]*"' "$1" | head -1 | sed 's/"status":"\([^"]*\)"/\1/'
}

# Find latest screenshot: get_latest_screenshot <status> <test_name>
get_latest_screenshot() {
    local status=$1
    local test_name=$2
    ls -t screenshots/test_${status}_${test_name}_*.png 2>/dev/null | head -1
}

# Function to find latest log file
get_latest_log() {
    ls -t target/logs/*.log 2>/dev/null | head -1
}

RESULT_FILES=$(find "$LATEST_RUN" -name '*-result.json' | python3 -c "
import sys, json, re
files = [f.strip() for f in sys.stdin if f.strip()]
def get_sort_key(f):
    try:
        name = json.load(open(f)).get('name', '')
        m = re.search(r'ZP-(\d+)', name)
        if m:
            return (0, int(m.group(1)))
        return (1, name)  # non-ZP tests go after
    except:
        return (2, 0)
for f in sorted(files, key=get_sort_key):
    print(f)
")
LOG_FILE=$(get_latest_log)
REPORT_DATE=$(date)

# ── Run-context for the report header ─────────────────────────────────────────
ENV_NAME=$(grep '^ZOHO_ENV' .env 2>/dev/null | cut -d= -f2 | tr -d '[:space:]' | tr '[:lower:]' '[:upper:]')
ENV_NAME=${ENV_NAME:-LIVE}
ZOHO_USER_VAL=$(grep '^ZOHO_USER' .env 2>/dev/null | cut -d= -f2 | tr -d '[:space:]')
ZOHO_USER_VAL=${ZOHO_USER_VAL:-unknown}
if [ "$ENV_NAME" = "TEST" ]; then
    TARGET_URL="https://peoplelabs.zoho.com"
else
    TARGET_URL="https://people.zoho.com"
fi
RUN_NAME=$(basename "$LATEST_RUN")

# Write HTML header (unquoted HTMLEOF so shell expands $TOTAL/$PASSED/$FAILED)
cat > "$REPORT_FILE" << HTMLEOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test Report</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f5f5; padding: 20px; }
        .container { max-width: 1100px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }
        .header h1 { font-size: 28px; margin-bottom: 10px; }
        .header p { opacity: 0.9; font-size: 14px; }
        .summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; padding: 30px; background: #f9f9f9; border-bottom: 1px solid #eee; }
        .stat { text-align: center; }
        .stat-number { font-size: 32px; font-weight: bold; margin-bottom: 5px; }
        .stat-label { font-size: 14px; color: #666; }
        .stat.passed .stat-number { color: #10b981; }
        .stat.failed .stat-number { color: #ef4444; }
        .stat.skipped .stat-number { color: #f59e0b; }
        .test-result.skipped { border-left-color: #f59e0b; background: #fffbeb; }
        .test-result.skipped .status { background: #fef3c7; color: #92400e; }
        .content { padding: 30px; }
        .test-result { margin-bottom: 25px; padding: 20px; border-left: 4px solid #e5e7eb; background: #f9f9f9; border-radius: 4px; }
        .test-result.passed { border-left-color: #10b981; background: #f0fdf4; }
        .test-result.failed { border-left-color: #ef4444; background: #fef2f2; }
        .test-result h3 { margin-bottom: 8px; font-size: 16px; display: flex; align-items: center; gap: 8px; }
        .test-num { display: inline-block; background: #6366f1; color: white; border-radius: 4px; padding: 1px 8px; font-size: 12px; font-weight: 700; letter-spacing: 0.5px; flex-shrink: 0; }
        .test-result .status { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .test-result.passed .status { background: #d1fae5; color: #065f46; }
        .test-result.failed .status { background: #fee2e2; color: #7f1d1d; }
        .test-result .time { color: #999; font-size: 13px; margin-left: 10px; }
        .test-description { margin-top: 10px; font-size: 13px; color: #666; }
        .screenshot-section { margin-top: 15px; padding-top: 15px; border-top: 1px solid #e0e0e0; }
        .screenshot-section h4 { font-size: 13px; color: #999; margin-bottom: 10px; font-weight: 600; }
        .screenshot-img { max-width: 100%; height: auto; border: 1px solid #ddd; border-radius: 4px; display: block; margin-top: 10px; }
        .no-screenshot { font-size: 12px; color: #999; font-style: italic; }
        details { margin-top: 10px; }
        details summary { cursor: pointer; user-select: none; padding: 8px 0; font-weight: 600; color: #667eea; font-size: 13px; }
        details summary:hover { color: #764ba2; }
        .logs-content { margin-top: 10px; background: #f5f5f5; padding: 15px; border-radius: 4px; border-left: 3px solid #667eea; max-height: 400px; overflow-y: auto; font-family: 'Courier New', monospace; font-size: 11px; line-height: 1.4; white-space: pre-wrap; word-wrap: break-word; }
        .error-msg { color: #ef4444; font-size: 12px; margin-top: 8px; }
        .footer { padding: 20px 30px; background: #f9f9f9; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #eee; }
        .env-bar { display:flex; flex-wrap:wrap; background:#1a1a2e; border-bottom:3px solid #667eea; }
        .env-item { display:flex; flex-direction:column; padding:12px 24px; border-right:1px solid #2d2d50; }
        .env-item:last-child { border-right:none; }
        .env-key { font-size:10px; color:#6b7280; text-transform:uppercase; letter-spacing:1px; margin-bottom:4px; }
        .env-val { font-size:13px; font-weight:600; color:#e2e8f0; }
        .env-badge { display:inline-block; padding:2px 10px; border-radius:12px; font-size:12px; font-weight:700; }
        .env-badge.LIVE { background:#064e3b; color:#6ee7b7; }
        .env-badge.TEST { background:#1e3a5f; color:#93c5fd; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>&#129514; ZP SmartTests &mdash; Execution Report</h1>
            <p>$REPORT_DATE</p>
        </div>
        <div class="env-bar">
            <div class="env-item">
                <span class="env-key">Environment</span>
                <span class="env-val"><span class="env-badge $ENV_NAME">$ENV_NAME</span></span>
            </div>
            <div class="env-item">
                <span class="env-key">Target URL</span>
                <span class="env-val">$TARGET_URL</span>
            </div>
            <div class="env-item">
                <span class="env-key">User</span>
                <span class="env-val">$ZOHO_USER_VAL</span>
            </div>
            <div class="env-item">
                <span class="env-key">Browser</span>
                <span class="env-val">Chromium</span>
            </div>
            <div class="env-item">
                <span class="env-key">Run ID</span>
                <span class="env-val">$RUN_NAME</span>
            </div>
        </div>

        <div class="summary">
            <div class="stat total"><div class="stat-number">$TOTAL</div><div class="stat-label">Total Tests</div></div>
            <div class="stat passed"><div class="stat-number">$PASSED</div><div class="stat-label">Passed</div></div>
            <div class="stat failed"><div class="stat-number">$FAILED</div><div class="stat-label">Failed</div></div>        <div class="stat skipped"><div class="stat-number">$SKIPPED</div><div class="stat-label">Skipped</div></div>        </div>
        <div class="content">
            <h2 style="margin-bottom: 20px;">Test Results</h2>
HTMLEOF

# Append one block per test directly to file
test_num=0
for file in $RESULT_FILES; do
    test_num=$((test_num + 1))
    test_name=$(extract_test_name "$file")
    test_status=$(extract_test_status "$file")

    if [ "$test_status" = "passed" ]; then
        icon="&#10003;"
        css="passed"
        label="PASSED"
    elif [ "$test_status" = "failed" ] || [ "$test_status" = "broken" ]; then
        icon="&#10007;"
        css="failed"
        label="FAILED"
    else
        icon="&#9702;"
        css="skipped"
        label="SKIPPED"
    fi

    # Reset per-iteration variables at the TOP to avoid bleed-through
    logs=""
    screenshot=""

    # Extract all image attachments from the Allure result JSON for this test
    # These are saved by takeElementScreenshot: "1. Full Page — ..." and "2. Element [...] — ..."
    allure_attachments=$(python3 -c "
import json, sys
try:
    d = json.load(open('$file'))
    atts = d.get('attachments', [])
    for a in atts:
        if a.get('type') == 'image/png' and a.get('source'):
            print(a.get('name','screenshot') + '|' + a.get('source'))
except Exception as e:
    pass
" 2>/dev/null)

    # No fallback to disk screenshots — only show what was intentionally attached
    # to Allure within the test body. Disk screenshots are for debugging only.
    screenshot=""

    # Read per-test log file written by BaseTest
    # Log files are named: target/logs/<SimpleClassName>_<methodName>.log
    test_log_class=$(python3 -c "
import json
try:
    d = json.load(open('$file'))
    cls = next((l['value'] for l in d.get('labels', []) if l['name'] == 'testClass'), '')
    print(cls.split('.')[-1])
except:
    print('')
" 2>/dev/null)
    test_log_method=$(python3 -c "
import json
try:
    d = json.load(open('$file'))
    print(next((l['value'] for l in d.get('labels', []) if l['name'] == 'testMethod'), ''))
except:
    print('')
" 2>/dev/null)
    test_log_file="target/logs/${test_log_class}_${test_log_method}.log"
    logs=""
    if [ -f "$test_log_file" ]; then
        logs=$(cat "$test_log_file" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g')
    fi
    [ -z "$logs" ] && logs="No logs available"

    printf '        <div class="test-result %s">\n' "$css" >> "$REPORT_FILE"
    printf '            <h3><span class="test-num">Test %s</span> %s %s</h3>\n' "$test_num" "$icon" "$test_name" >> "$REPORT_FILE"
    printf '            <span class="status">%s</span>\n' "$label" >> "$REPORT_FILE"
    printf '            <p class="test-description">Status: %s</p>\n' "$label" >> "$REPORT_FILE"

    printf '            <div class="screenshot-section"><h4>&#128247; Screenshots</h4>\n' >> "$REPORT_FILE"
    if [ -n "$allure_attachments" ]; then
        # Show each Allure attachment in order (full-page first, then element-scoped)
        while IFS='|' read -r att_name att_source; do
            [ -z "$att_source" ] && continue
            att_path="${LATEST_RUN}/${att_source}"
            if [ -f "$att_path" ]; then
                printf '                <p style="font-size:12px;color:#666;margin:10px 0 4px;font-weight:600;">%s</p>\n' "$att_name" >> "$REPORT_FILE"
                printf '                <img src="../../%s" class="screenshot-img" alt="%s">\n' "$att_path" "$att_name" >> "$REPORT_FILE"
            fi
        done <<< "$allure_attachments"
    elif [ -n "$screenshot" ]; then
        # Fallback: @AfterMethod screenshot
        printf '                <img src="../../%s" class="screenshot-img" alt="%s screenshot">\n' "$screenshot" "$test_name" >> "$REPORT_FILE"
    else
        printf '                <span class="no-screenshot">No screenshot available</span>\n' >> "$REPORT_FILE"
    fi
    printf '            </div>\n' >> "$REPORT_FILE"

    printf '            <div class="screenshot-section"><h4>&#128203; Logs</h4>\n' >> "$REPORT_FILE"
    printf '                <details><summary>Click to expand</summary>\n' >> "$REPORT_FILE"
    printf '                    <div class="logs-content">%s</div>\n' "$logs" >> "$REPORT_FILE"
    printf '                </details></div>\n' >> "$REPORT_FILE"
    printf '        </div>\n' >> "$REPORT_FILE"
done

# Write footer
cat >> "$REPORT_FILE" << HTMLEOF
        </div>
        <div class="footer">Generated on $REPORT_DATE | Test Suite: LeaveBalanceIntegrityTest</div>
    </div>
</body>
</html>
HTMLEOF

echo ""
echo "✅ Simple HTML report generated!"
echo "📊 Report: $REPORT_FILE"
echo "📈 Tests: $TOTAL total | $PASSED passed | $FAILED failed | $SKIPPED skipped"
echo ""

SIMPLE_REPORT_URL="file://$(pwd)/$REPORT_FILE"

echo "════════════════════════════════════════════════════════════════"
echo "📋 SIMPLE REPORT URL:"
echo "🔗 $SIMPLE_REPORT_URL"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Save URL to LATEST_REPORT.txt
echo "Simple Report URL: $SIMPLE_REPORT_URL" >> LATEST_REPORT.txt

# Open in browser
open "$SIMPLE_REPORT_URL" 2>/dev/null || echo "📌 Open in browser: $SIMPLE_REPORT_URL"
