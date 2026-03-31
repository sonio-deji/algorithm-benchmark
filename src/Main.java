import framework.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Main
 * ────
 * Entry point for the Algorithm Benchmarking Framework.
 *
 * Execution pipeline:
 *   1. Run all benchmarks (BenchmarkEngine)
 *   2. Print tabular results to console
 *   3. Persist results to CSV (ResultsDatabase)
 *   4. Estimate empirical complexity class (ComplexityAnalyzer)
 *   5. Generate HTML visualization report (VisualizationEngine)
 */
public class Main {

    private static final String CSV_OUTPUT  = "benchmark_results.csv";
    private static final String HTML_OUTPUT = "benchmark_report.html";

    public static void main(String[] args) throws IOException {

        printBanner();

        // ── 1. Run benchmarks ─────────────────────────────────────────────────
        System.out.println("⏳  Running benchmarks (this may take ~30 seconds)...\n");
        BenchmarkEngine engine  = new BenchmarkEngine();
        List<BenchmarkResult> results = engine.runAll();

        // ── 2. Console output ─────────────────────────────────────────────────
        System.out.printf("%-20s | %-15s | %-10s | %-14s | %-12s | %s%n",
                "Algorithm", "Data Structure", "n", "Avg Time (ms)", "StdDev (ns)", "Memory (B)");
        System.out.println("-".repeat(95));
        results.forEach(System.out::println);

        // ── 3. Save CSV ───────────────────────────────────────────────────────
        System.out.println();
        ResultsDatabase db = new ResultsDatabase(CSV_OUTPUT);
        db.save(results);

        // ── 4. Complexity analysis ────────────────────────────────────────────
        System.out.println("\n── Complexity Analysis ──────────────────────────────────────");
        ComplexityAnalyzer analyzer = new ComplexityAnalyzer();
        for (String algo : Arrays.asList(
                BenchmarkEngine.ALGO_LINEAR_ARRAY,
                BenchmarkEngine.ALGO_LINEAR_LL,
                BenchmarkEngine.ALGO_BINARY_ARRAY,
                BenchmarkEngine.ALGO_BST)) {
            ComplexityAnalyzer.FitResult fit = analyzer.analyze(results, algo);
            System.out.printf("  %-20s → %s%n", algo, fit);
        }

        // ── 5. HTML visualization ─────────────────────────────────────────────
        System.out.println();
        VisualizationEngine viz = new VisualizationEngine(HTML_OUTPUT);
        viz.generate(results);

        System.out.println("\n✅  Done! Open benchmark_report.html in your browser to view graphs.");
    }

    private static void printBanner() {
        System.out.println("""
        ╔════════════════════════════════════════════════════════════════╗
        ║   Algorithm Benchmarking Framework — COSC 742                  ║
        ║   Group 1: Searching Algorithms                                ║
        ║   1. Linear Search  — Unsorted Array                           ║
        ║   2. Linear Search  — Singly Linked List                       ║
        ║   3. Binary Search  — Sorted Array                             ║
        ║   4. BST Search     — Binary Search Tree                       ║
        ╚════════════════════════════════════════════════════════════════╝
        """);
    }
}
