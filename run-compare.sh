#!/bin/bash

# ==========================================
# Environment Comparison Runner
# ==========================================
# Runs the test suite on both LIVE and TEST environments,
# then generates a side-by-side screenshot comparison report.
#
# Usage:
#   ./run-compare.sh          # Run both environments
#   ./run-compare.sh live      # Run only live, then compare
#   ./run-compare.sh test      # Run only test, then compare
#   ./run-compare.sh report    # Skip runs, just generate comparison report
# ==========================================

set -e

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

MODE="${1:-both}"

run_env() {
    local ENV_NAME=$1
    local ENV_UPPER
    ENV_UPPER=$(echo "$ENV_NAME" | tr '[:lower:]' '[:upper:]')
    echo ""
    echo "════════════════════════════════════════════════════════════════"
    echo "🔄 Running tests on ${ENV_UPPER} environment..."
    echo "════════════════════════════════════════════════════════════════"
    echo ""

    # Clear old screenshots for this env so we get a clean set
    rm -rf "screenshots/${ENV_NAME}"
    mkdir -p "screenshots/${ENV_NAME}"

    # Back up existing allure results from other environments before clean
    local BACKUP_DIR=$(mktemp -d)
    for dir in target/allure-results-raw/compare_*; do
        [ -d "$dir" ] && cp -r "$dir" "$BACKUP_DIR/"
    done

    # Run Maven tests with the environment override
    ZOHO_ENV="$ENV_NAME" mvn clean test \
        -Dallure.results.directory="target/allure-results-raw/compare_${ENV_NAME}" \
        2>&1 | tee "target/logs/compare_${ENV_NAME}.log" || true

    # Restore backed-up allure results from other environments
    for dir in "$BACKUP_DIR"/compare_*; do
        [ -d "$dir" ] && local dirname=$(basename "$dir")
        [ "$dirname" != "compare_${ENV_NAME}" ] && [ -d "$dir" ] && \
            cp -r "$dir" "target/allure-results-raw/"
    done
    rm -rf "$BACKUP_DIR"

    # Count results
    local RESULTS_DIR="target/allure-results-raw/compare_${ENV_NAME}"
    local PASSED=$(grep -rl '"status":"passed"' "$RESULTS_DIR" 2>/dev/null | grep -c 'result.json' || echo 0)
    local FAILED=$(grep -rEl '"status":"(failed|broken)"' "$RESULTS_DIR" 2>/dev/null | grep -c 'result.json' || echo 0)
    echo ""
    echo "✅ ${ENV_UPPER}: ${PASSED} passed, ${FAILED} failed"
    echo ""
}

mkdir -p target/logs

case "$MODE" in
    both)
        run_env "live"
        run_env "test"
        ;;
    live)
        run_env "live"
        ;;
    test)
        run_env "test"
        ;;
    report)
        echo "⏭️  Skipping test runs, generating comparison report only..."
        ;;
    *)
        echo "Usage: ./run-compare.sh [both|live|test|report]"
        exit 1
        ;;
esac

# ==========================================
# Generate comparison report
# ==========================================
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "📊 Generating comparison report..."
echo "════════════════════════════════════════════════════════════════"

REPORT_DIR="target/simple-reports"
mkdir -p "$REPORT_DIR"
REPORT_FILE="${REPORT_DIR}/EnvComparison.html"
export REPORT_FILE

LIVE_DIR="screenshots/live"
TEST_DIR="screenshots/test"

# Check that both directories have screenshots
LIVE_COUNT=$(find "$LIVE_DIR" -name '*.png' 2>/dev/null | wc -l | tr -d ' ')
TEST_COUNT=$(find "$TEST_DIR" -name '*.png' 2>/dev/null | wc -l | tr -d ' ')

if [ "$LIVE_COUNT" -eq 0 ] && [ "$TEST_COUNT" -eq 0 ]; then
    echo "❌ No screenshots found in either environment."
    echo "   Run: ./run-compare.sh both"
    exit 1
fi

echo "   Live screenshots: ${LIVE_COUNT}"
echo "   Test screenshots: ${TEST_COUNT}"

# Build the comparison report using Python for robust JSON/file processing
python3 << 'PYEOF'
import os, glob, re, html, base64, json, io
from datetime import datetime

try:
    from PIL import Image
    HAS_PILLOW = True
except ImportError:
    HAS_PILLOW = False
    print("⚠️  Pillow not installed — pixel-diff disabled. Install with: pip3 install Pillow")

live_dir = "screenshots/live"
test_dir = "screenshots/test"

def strip_timestamp(filename):
    """Remove the timestamp suffix from screenshot filenames to get a canonical name."""
    return re.sub(r'_\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2}\.png$', '', filename)

def get_screenshots(directory):
    """Get a dict of canonical_name → filepath for the latest screenshot per name."""
    shots = {}
    for f in sorted(glob.glob(os.path.join(directory, '*.png'))):
        basename = os.path.basename(f)
        canonical = strip_timestamp(basename)
        shots[canonical] = f
    return shots

def img_to_base64(filepath):
    """Convert image to base64 data URI."""
    try:
        with open(filepath, 'rb') as f:
            data = base64.b64encode(f.read()).decode('utf-8')
        return f'data:image/png;base64,{data}'
    except:
        return ''

def pil_img_to_base64(pil_img):
    """Convert a PIL Image to base64 data URI."""
    buf = io.BytesIO()
    pil_img.save(buf, format='PNG')
    data = base64.b64encode(buf.getvalue()).decode('utf-8')
    return f'data:image/png;base64,{data}'

def compute_diff(live_path, test_path, threshold=30):
    """Compare two screenshots pixel-by-pixel. Returns (diff_base64, diff_pct, total_pixels)."""
    if not HAS_PILLOW:
        return None, 0.0, 0

    try:
        img_live = Image.open(live_path).convert('RGBA')
        img_test = Image.open(test_path).convert('RGBA')

        # Resize to common dimensions (use the larger canvas)
        w = max(img_live.width, img_test.width)
        h = max(img_live.height, img_test.height)

        canvas_live = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        canvas_test = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        canvas_live.paste(img_live, (0, 0))
        canvas_test.paste(img_test, (0, 0))

        px_live = canvas_live.load()
        px_test = canvas_test.load()

        diff_img = Image.new('RGBA', (w, h), (0, 0, 0, 255))
        px_diff = diff_img.load()

        diff_count = 0
        total = w * h

        for y in range(h):
            for x in range(w):
                r1, g1, b1, a1 = px_live[x, y]
                r2, g2, b2, a2 = px_test[x, y]
                dr = abs(r1 - r2)
                dg = abs(g1 - g2)
                db = abs(b1 - b2)
                da = abs(a1 - a2)

                if dr + dg + db + da > threshold:
                    # Highlight diff pixels in red on a dimmed background
                    intensity = min(255, (dr + dg + db + da) * 2)
                    px_diff[x, y] = (255, 0, 0, intensity)
                    diff_count += 1
                else:
                    # Unchanged pixels shown dimmed
                    avg = (r1 + g1 + b1) // 3
                    px_diff[x, y] = (avg // 3, avg // 3, avg // 3, 200)

        diff_pct = (diff_count / total * 100) if total > 0 else 0.0
        return pil_img_to_base64(diff_img), diff_pct, total
    except Exception as e:
        print(f"  ⚠️  Diff failed: {e}")
        return None, 0.0, 0

def get_test_results(env_name):
    """Extract pass/fail status per test method from allure results."""
    results = {}
    results_dir = f"target/allure-results-raw/compare_{env_name}"
    if not os.path.isdir(results_dir):
        return results
    for f in glob.glob(os.path.join(results_dir, '*-result.json')):
        try:
            with open(f) as jf:
                data = json.load(jf)
            method = ''
            for label in data.get('labels', []):
                if label.get('name') == 'testMethod':
                    method = label.get('value', '')
                    break
            if method:
                results[method] = data.get('status', 'unknown')
        except:
            pass
    return results

live_shots = get_screenshots(live_dir)
test_shots = get_screenshots(test_dir)
all_names = sorted(set(list(live_shots.keys()) + list(test_shots.keys())))

# Pre-compute diffs for screenshots that exist in both environments
diff_data = {}  # canonical_name → (diff_b64, diff_pct)
if HAS_PILLOW:
    paired = [n for n in all_names if n in live_shots and n in test_shots]
    print(f"🔍 Computing pixel diffs for {len(paired)} paired screenshots...")
    for i, name in enumerate(paired):
        diff_b64, diff_pct, _ = compute_diff(live_shots[name], test_shots[name])
        diff_data[name] = (diff_b64, diff_pct)
        if (i + 1) % 10 == 0:
            print(f"   Processed {i+1}/{len(paired)} diffs...")
    print(f"✅ Pixel diffs computed for {len(paired)} screenshots")
    has_diff_count = sum(1 for _, (_, pct) in diff_data.items() if pct > 0.5)

live_results = get_test_results("live")
test_results = get_test_results("test")

# Count matches/mismatches/only-in
both_count = sum(1 for n in all_names if n in live_shots and n in test_shots)
live_only = sum(1 for n in all_names if n in live_shots and n not in test_shots)
test_only = sum(1 for n in all_names if n not in live_shots and n in test_shots)
has_diff_count_val = has_diff_count if HAS_PILLOW else 0
identical_count = both_count - has_diff_count_val

# Filter to only "success_" screenshots for the main comparison (skip error/login debug shots)
success_names = [n for n in all_names if n.startswith('success_')]
other_names = [n for n in all_names if not n.startswith('success_')]

timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

report_html = f'''<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Environment Comparison — {timestamp}</title>
<style>
    * {{ margin: 0; padding: 0; box-sizing: border-box; }}
    body {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f172a; color: #e2e8f0; padding: 20px; }}
    .header {{ text-align: center; margin-bottom: 30px; }}
    .header h1 {{ font-size: 28px; color: #f8fafc; margin-bottom: 8px; }}
    .header p {{ color: #94a3b8; font-size: 14px; }}
    .stats {{ display: flex; justify-content: center; gap: 20px; margin: 20px 0; flex-wrap: wrap; }}
    .stat {{ background: #1e293b; border-radius: 12px; padding: 16px 24px; text-align: center; min-width: 140px; }}
    .stat .number {{ font-size: 32px; font-weight: 700; }}
    .stat .label {{ font-size: 12px; color: #94a3b8; margin-top: 4px; text-transform: uppercase; letter-spacing: 1px; }}
    .stat.live .number {{ color: #22d3ee; }}
    .stat.test .number {{ color: #a78bfa; }}
    .stat.both .number {{ color: #34d399; }}
    .filter-bar {{ display: flex; justify-content: center; gap: 10px; margin: 20px 0; flex-wrap: wrap; }}
    .filter-btn {{ padding: 8px 20px; border-radius: 8px; border: 1px solid #334155; background: #1e293b; color: #94a3b8; cursor: pointer; font-size: 13px; transition: all 0.2s; }}
    .filter-btn:hover {{ background: #334155; color: #f8fafc; }}
    .filter-btn.active {{ background: #3b82f6; color: #fff; border-color: #3b82f6; }}
    .section-title {{ font-size: 20px; color: #f8fafc; margin: 30px 0 15px; padding-bottom: 8px; border-bottom: 1px solid #334155; }}
    .comparison {{ margin-bottom: 30px; background: #1e293b; border-radius: 12px; overflow: hidden; }}
    .comparison.hidden {{ display: none; }}
    .comp-header {{ padding: 12px 20px; background: #334155; display: flex; justify-content: space-between; align-items: center; }}
    .comp-header .name {{ font-weight: 600; font-size: 14px; }}
    .comp-header .badges {{ display: flex; gap: 8px; }}
    .badge {{ padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; text-transform: uppercase; }}
    .badge.passed {{ background: #065f46; color: #34d399; }}
    .badge.failed {{ background: #7f1d1d; color: #fca5a5; }}
    .badge.broken {{ background: #7f1d1d; color: #fca5a5; }}
    .badge.missing {{ background: #78350f; color: #fbbf24; }}
    .badge.live-label {{ background: #164e63; color: #22d3ee; }}
    .badge.test-label {{ background: #4c1d95; color: #a78bfa; }}
    .badge.diff-identical {{ background: #065f46; color: #34d399; }}
    .badge.diff-low {{ background: #365314; color: #a3e635; }}
    .badge.diff-medium {{ background: #78350f; color: #fbbf24; }}
    .badge.diff-high {{ background: #7f1d1d; color: #fca5a5; }}
    .comp-body {{ display: flex; gap: 0; }}
    .comp-side {{ flex: 1; padding: 10px; text-align: center; position: relative; }}
    .comp-side.live {{ border-right: 1px solid #334155; }}
    .comp-side.diff {{ border-left: 1px solid #334155; background: #111827; }}
    .comp-side img {{ max-width: 100%; height: auto; border-radius: 8px; cursor: pointer; transition: transform 0.2s; }}
    .comp-side img:hover {{ transform: scale(1.02); }}
    .comp-side .env-label {{ font-size: 11px; color: #64748b; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 1px; }}
    .diff-pct {{ font-size: 13px; font-weight: 700; margin-top: 6px; }}
    .no-img {{ padding: 60px 20px; color: #475569; font-style: italic; }}
    .modal-overlay {{ display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.9); z-index: 1000; justify-content: center; align-items: center; cursor: zoom-out; }}
    .modal-overlay.active {{ display: flex; }}
    .modal-overlay img {{ max-width: 95%; max-height: 95%; object-fit: contain; }}
    @media print {{
        body {{ background: #fff; color: #000; }}
        .filter-bar {{ display: none; }}
        .comparison {{ break-inside: avoid; }}
    }}
</style>
</head>
<body>
<div class="header">
    <h1>🔍 Environment Comparison</h1>
    <p>Live (people.zoho.com) vs Test (peoplelabs.zoho.com) — {timestamp}</p>
</div>

<div class="stats">
    <div class="stat both"><div class="number">{both_count}</div><div class="label">Both Envs</div></div>
    <div class="stat live"><div class="number">{live_only}</div><div class="label">Live Only</div></div>
    <div class="stat test"><div class="number">{test_only}</div><div class="label">Test Only</div></div>
    <div class="stat"><div class="number" style="color:#f8fafc">{len(all_names)}</div><div class="label">Total Screenshots</div></div>
    <div class="stat"><div class="number" style="color:#34d399">{identical_count}</div><div class="label">Identical</div></div>
    <div class="stat"><div class="number" style="color:#fca5a5">{has_diff_count_val}</div><div class="label">Has Differences</div></div>
</div>

<div class="stats">
    <div class="stat"><div class="number" style="color:#22d3ee">{sum(1 for v in live_results.values() if v=="passed")}</div><div class="label">Live Passed</div></div>
    <div class="stat"><div class="number" style="color:#fca5a5">{sum(1 for v in live_results.values() if v in ("failed","broken"))}</div><div class="label">Live Failed</div></div>
    <div class="stat"><div class="number" style="color:#a78bfa">{sum(1 for v in test_results.values() if v=="passed")}</div><div class="label">Test Passed</div></div>
    <div class="stat"><div class="number" style="color:#fca5a5">{sum(1 for v in test_results.values() if v in ("failed","broken"))}</div><div class="label">Test Failed</div></div>
</div>

<div class="filter-bar">
    <button class="filter-btn active" onclick="filterComparisons('all')">All</button>
    <button class="filter-btn" onclick="filterComparisons('success')">Success Screenshots</button>
    <button class="filter-btn" onclick="filterComparisons('other')">Other Screenshots</button>
    <button class="filter-btn" onclick="filterComparisons('both')">In Both Envs</button>
    <button class="filter-btn" onclick="filterComparisons('live-only')">Live Only</button>
    <button class="filter-btn" onclick="filterComparisons('test-only')">Test Only</button>
    <button class="filter-btn" onclick="filterComparisons('has-diff')">Has Differences</button>
    <button class="filter-btn" onclick="filterComparisons('identical')">Identical</button>
</div>

<div id="comparisons">
'''

def render_comparison(canonical_name, live_path, test_path, is_success=True):
    """Render a single comparison card with optional diff column."""
    display_name = canonical_name
    if display_name.startswith('success_'):
        display_name = display_name[8:]

    method_name = display_name

    live_status = live_results.get(method_name, '')
    test_status = test_results.get(method_name, '')

    has_live = live_path is not None
    has_test = test_path is not None
    category = 'success' if is_success else 'other'
    availability = 'both' if (has_live and has_test) else ('live-only' if has_live else 'test-only')

    # Get diff data if available
    diff_b64, diff_pct = diff_data.get(canonical_name, (None, 0.0))
    has_visual_diff = diff_b64 is not None and diff_pct > 0.5
    diff_level = 'identical'
    if diff_pct > 0.5:
        diff_level = 'has-diff'

    card = f'<div class="comparison" data-category="{category}" data-availability="{availability}" data-diff="{diff_level}">\n'
    card += f'  <div class="comp-header">\n'
    card += f'    <span class="name">{html.escape(display_name)}</span>\n'
    card += f'    <div class="badges">\n'

    if has_live and live_status:
        card += f'      <span class="badge live-label">LIVE</span><span class="badge {live_status}">{live_status}</span>\n'
    elif not has_live:
        card += f'      <span class="badge missing">No Live</span>\n'

    if has_test and test_status:
        card += f'      <span class="badge test-label">TEST</span><span class="badge {test_status}">{test_status}</span>\n'
    elif not has_test:
        card += f'      <span class="badge missing">No Test</span>\n'

    # Diff badge
    if diff_b64 is not None:
        if diff_pct <= 0.5:
            card += f'      <span class="badge diff-identical">IDENTICAL</span>\n'
        elif diff_pct <= 5.0:
            card += f'      <span class="badge diff-low">{diff_pct:.1f}% diff</span>\n'
        elif diff_pct <= 20.0:
            card += f'      <span class="badge diff-medium">{diff_pct:.1f}% diff</span>\n'
        else:
            card += f'      <span class="badge diff-high">{diff_pct:.1f}% diff</span>\n'

    card += f'    </div>\n'
    card += f'  </div>\n'
    card += f'  <div class="comp-body">\n'

    # Live side
    card += f'    <div class="comp-side live">\n'
    card += f'      <div class="env-label">Live</div>\n'
    if has_live:
        b64 = img_to_base64(live_path)
        card += f'      <img src="{b64}" alt="Live - {html.escape(display_name)}" onclick="openModal(this)" loading="lazy"/>\n'
    else:
        card += f'      <div class="no-img">No screenshot captured</div>\n'
    card += f'    </div>\n'

    # Diff side (only when both exist and Pillow computed a diff)
    if diff_b64 is not None:
        diff_color = '#34d399' if diff_pct <= 0.5 else '#fbbf24' if diff_pct <= 5.0 else '#fca5a5'
        card += f'    <div class="comp-side diff">\n'
        card += f'      <div class="env-label">Pixel Diff</div>\n'
        card += f'      <img src="{diff_b64}" alt="Diff - {html.escape(display_name)}" onclick="openModal(this)" loading="lazy"/>\n'
        card += f'      <div class="diff-pct" style="color:{diff_color}">{diff_pct:.2f}% pixels differ</div>\n'
        card += f'    </div>\n'

    # Test side
    card += f'    <div class="comp-side">\n'
    card += f'      <div class="env-label">Test</div>\n'
    if has_test:
        b64 = img_to_base64(test_path)
        card += f'      <img src="{b64}" alt="Test - {html.escape(display_name)}" onclick="openModal(this)" loading="lazy"/>\n'
    else:
        card += f'      <div class="no-img">No screenshot captured</div>\n'
    card += f'    </div>\n'

    card += f'  </div>\n'
    card += f'</div>\n'
    return card

# Render success screenshots first (main test comparisons)
if success_names:
    report_html += '<h2 class="section-title">Test Screenshots</h2>\n'
    for name in success_names:
        report_html += render_comparison(
            name,
            live_shots.get(name),
            test_shots.get(name),
            is_success=True
        )

# Then other screenshots (login errors, element screenshots, etc.)
if other_names:
    report_html += '<h2 class="section-title">Other Screenshots</h2>\n'
    for name in other_names:
        report_html += render_comparison(
            name,
            live_shots.get(name),
            test_shots.get(name),
            is_success=False
        )

report_html += '''
</div>

<div class="modal-overlay" id="modal" onclick="closeModal()">
    <img id="modal-img" src="" alt="Zoomed screenshot"/>
</div>

<script>
function filterComparisons(filter) {
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');

    document.querySelectorAll('.comparison').forEach(card => {
        const cat = card.dataset.category;
        const avail = card.dataset.availability;
        const diff = card.dataset.diff;
        let show = false;

        if (filter === 'all') show = true;
        else if (filter === 'success') show = cat === 'success';
        else if (filter === 'other') show = cat === 'other';
        else if (filter === 'both') show = avail === 'both';
        else if (filter === 'live-only') show = avail === 'live-only';
        else if (filter === 'test-only') show = avail === 'test-only';
        else if (filter === 'has-diff') show = diff === 'has-diff';
        else if (filter === 'identical') show = diff === 'identical';

        card.classList.toggle('hidden', !show);
    });
}

function openModal(img) {
    const modal = document.getElementById('modal');
    document.getElementById('modal-img').src = img.src;
    modal.classList.add('active');
}

function closeModal() {
    document.getElementById('modal').classList.remove('active');
}

document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModal(); });
</script>
</body>
</html>
'''

report_path = os.environ.get('REPORT_FILE', 'target/simple-reports/EnvComparison.html')
with open(report_path, 'w') as f:
    f.write(report_html)

print(f"✅ Comparison report generated: {report_path}")
print(f"   Success screenshots: {len(success_names)}")
print(f"   Other screenshots: {len(other_names)}")
PYEOF

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "📊 COMPARISON REPORT:"
REPORT_URL="file://$(pwd)/${REPORT_FILE}"
echo "🔗 $REPORT_URL"
echo ""
echo "   Command: open \"$REPORT_URL\""
echo "════════════════════════════════════════════════════════════════"
echo ""

# Open the report automatically
open "$REPORT_URL" 2>/dev/null || true
