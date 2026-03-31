#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# run.sh  –  Compiles and runs the Algorithm Benchmarking Framework
# Usage:  chmod +x run.sh && ./run.sh
# ─────────────────────────────────────────────────────────────────────────────

set -e
SRC="src"
BIN="bin"

echo "🔨 Compiling..."
mkdir -p "$BIN"

find "$SRC" -name "*.java" | xargs javac --release 17 -d "$BIN" -sourcepath "$SRC"

echo "🚀 Running..."
cd "$BIN"
java -Xmx512m Main

echo ""
echo "📄 Output files:"
echo "   $(pwd)/benchmark_results.csv   (raw data)"
echo "   $(pwd)/benchmark_report.html   (open in browser)"
