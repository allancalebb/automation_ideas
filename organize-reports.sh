#!/bin/bash

# Organize Allure Report Structure
# Moves supporting files into an "assets" subfolder
# Keeps only index.html at the root level of each report

TARGET_REPORTS_DIR="target/allure-reports"

if [ ! -d "$TARGET_REPORTS_DIR" ]; then
    echo "❌ Error: $TARGET_REPORTS_DIR not found!"
    exit 1
fi

echo "🔄 Organizing Allure Report Structure..."
echo ""

# Counter for organized reports
count=0

# Process each timestamped report directory
for report_dir in "$TARGET_REPORTS_DIR"/ZP_SmartTests_*; do
    if [ -d "$report_dir" ]; then
        report_name=$(basename "$report_dir")
        echo "📁 Processing: $report_name"
        
        # Create assets subfolder
        assets_dir="$report_dir/assets"
        mkdir -p "$assets_dir"
        
        # Move supporting files to assets (but keep index.html at root)
        for item in "$report_dir"/*; do
            item_name=$(basename "$item")
            
            # Skip index.html and assets folder
            if [ "$item_name" != "index.html" ] && [ "$item_name" != "assets" ]; then
                if [ -f "$item" ] || [ -d "$item" ]; then
                    mv "$item" "$assets_dir/" 2>/dev/null
                fi
            fi
        done
        
        # Update index.html to reference assets in paths if needed
        # (Allure structure is self-contained, so no path updates needed)
        
        count=$((count + 1))
        echo "   ✅ Organized: index.html at root, files in assets/"
    fi
done

echo ""
echo "================================"
echo "✅ Report Organization Complete!"
echo "================================"
echo ""
echo "📊 Organized Reports: $count"
echo "📁 Location: $TARGET_REPORTS_DIR/"
echo ""
echo "Report Structure:"
echo "  ZP_SmartTests_YYYY-MM-DD_HH-MM-SS/"
echo "  ├── index.html (main entry point)"
echo "  └── assets/"
echo "      ├── app.js"
echo "      ├── styles.css"
echo "      ├── favicon.ico"
echo "      ├── data/"
echo "      ├── plugins/"
echo "      ├── widgets/"
echo "      ├── export/"
echo "      └── history/"
