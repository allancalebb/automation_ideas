#!/bin/bash

# Clean up old test result directories
# Removes legacy allure-results-* directories from project root
# Keeps the organized reports in target/allure-reports/

echo "🧹 Cleaning up legacy test result directories..."
echo ""

# Find and remove old allure-results-* directories
removed_count=0

for dir in allure-results-*; do
    if [ -d "$dir" ]; then
        echo "🗑️  Removing: $dir"
        rm -rf "$dir"
        removed_count=$((removed_count + 1))
    fi
done

# Also clean up generic allure-results directory if it exists
if [ -d "allure-results" ] && [ ! -z "$(ls -A allure-results/ 2>/dev/null)" ]; then
    # Check if it's the current/active results directory
    # If it has test files, we keep it; otherwise remove
    file_count=$(find allure-results/ -type f 2>/dev/null | wc -l)
    
    if [ "$file_count" -gt 100 ]; then
        # Has many files, likely from a recent run - keep it
        echo "📁 Keeping: allure-results/ (active results from recent run)"
    else
        echo "🗑️  Removing: allure-results/"
        rm -rf "allure-results"
    fi
fi

echo ""
echo "===================================="
echo "✅ Cleanup Complete!"
echo "===================================="
echo ""
echo "📊 Removed: $removed_count directories"
echo ""
echo "📂 Current Structure:"
echo "  ├── target/allure-reports/          (organized reports)"
echo "  │   └── ZP_SmartTests_*/            (timestamped reports)"
echo "  │       ├── index.html              (main entry)"
echo "  │       └── assets/                 (supporting files)"
echo "  ├── allure-results/ or allure-results-ZP_SmartTests_*/ (raw results)"
echo "  └── ... (other project files)"
echo ""
