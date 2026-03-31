package framework;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * VisualizationEngine
 * ───────────────────
 * Generates a self-contained HTML report with four Chart.js graphs:
 *   1. Runtime Growth          — line chart per algorithm
 *   2. Memory Consumption      — bar chart per algorithm
 *   3. Algorithm Comparison    — multi-line chart (all 4 algorithms)
 *   4. Empirical vs Theoretical — measured data + O(log n), O(n), O(n²) curves
 *
 * Colour palette (distinguishable on dark background):
 *   Linear Search (Array)       — Blue   #3B82F6
 *   Linear Search (Linked List) — Red    #EF4444
 *   Binary Search (Array)       — Green  #10B981
 *   BST Search                  — Orange #F59E0B
 */
public class VisualizationEngine {

    // ── Colour palette: border, fill, bar ────────────────────────────────────
    private static final String[][] PALETTE = {
        {"rgba(59,130,246,1)",  "rgba(59,130,246,0.15)",  "rgba(59,130,246,0.7)"},   // Blue
        {"rgba(239,68,68,1)",   "rgba(239,68,68,0.15)",   "rgba(239,68,68,0.7)"},    // Red
        {"rgba(16,185,129,1)",  "rgba(16,185,129,0.15)",  "rgba(16,185,129,0.7)"},   // Green
        {"rgba(245,158,11,1)",  "rgba(245,158,11,0.15)",  "rgba(245,158,11,0.7)"},   // Orange
    };

    // Theoretical overlay colours
    private static final String COL_LOGN  = "rgba(167,139,250,1)";   // violet
    private static final String COL_N     = "rgba(251,191,36,1)";    // yellow
    private static final String COL_NLOGN = "rgba(251,113,133,1)";   // pink
    private static final String COL_N2    = "rgba(148,163,184,1)";   // slate

    private final String outputHtmlPath;

    public VisualizationEngine(String outputHtmlPath) {
        this.outputHtmlPath = outputHtmlPath;
    }

    // ── Public entry point ────────────────────────────────────────────────────

    public void generate(List<BenchmarkResult> results) throws IOException {
        // Preserve insertion order — BenchmarkEngine produces a consistent order
        Map<String, List<BenchmarkResult>> byAlgo = new LinkedHashMap<>();
        for (BenchmarkResult r : results)
            byAlgo.computeIfAbsent(r.algorithmName, k -> new ArrayList<>()).add(r);

        int[]  sizes  = BenchmarkEngine.INPUT_SIZES;
        String labels = toJsArray(Arrays.stream(sizes).mapToObj(String::valueOf)
                .collect(Collectors.toList()));

        String runtimeDs     = buildLineDatasets(byAlgo, sizes, false);
        String memDs         = buildBarDatasets(byAlgo, sizes);
        String compDs        = buildLineDatasets(byAlgo, sizes, false); // same as runtime
        String theoDs        = buildTheoreticalDatasets(byAlgo, sizes);

        String html = buildHtml(labels, runtimeDs, memDs, compDs, theoDs, byAlgo.size());
        Files.writeString(Paths.get(outputHtmlPath), html);
        System.out.println("[VisualizationEngine] Report → "
                + Paths.get(outputHtmlPath).toAbsolutePath());
    }

    // ── Dataset builders ──────────────────────────────────────────────────────

    private String buildLineDatasets(Map<String, List<BenchmarkResult>> byAlgo,
                                      int[] sizes, boolean fill) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (Map.Entry<String, List<BenchmarkResult>> e : byAlgo.entrySet()) {
            if (idx > 0) sb.append(",");
            String border = PALETTE[idx % PALETTE.length][0];
            String bg     = fill ? PALETTE[idx % PALETTE.length][1] : "transparent";
            List<Double> times = getTimesMs(e.getValue(), sizes);
            sb.append(String.format(
                "{label:'%s',data:%s,borderColor:'%s',backgroundColor:'%s'," +
                "tension:0.4,fill:%s,pointRadius:5,borderWidth:2}",
                e.getKey(), toJsArray(times), border, bg, fill ? "true" : "false"));
            idx++;
        }
        return sb.toString();
    }

    private String buildBarDatasets(Map<String, List<BenchmarkResult>> byAlgo, int[] sizes) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (Map.Entry<String, List<BenchmarkResult>> e : byAlgo.entrySet()) {
            if (idx > 0) sb.append(",");
            String color = PALETTE[idx % PALETTE.length][2];
            List<Double> mem = getMemKB(e.getValue(), sizes);
            sb.append(String.format(
                "{label:'%s',data:%s,backgroundColor:'%s',borderWidth:1}",
                e.getKey(), toJsArray(mem), color));
            idx++;
        }
        return sb.toString();
    }

    private String buildTheoreticalDatasets(Map<String, List<BenchmarkResult>> byAlgo,
                                              int[] sizes) {
        // Start with all empirical series
        StringBuilder sb = new StringBuilder(buildLineDatasets(byAlgo, sizes, false));

        // Normalise theoretical curves to the first linear-search-array data point
        String baseAlgo = BenchmarkEngine.ALGO_LINEAR_ARRAY;
        List<BenchmarkResult> base = byAlgo.getOrDefault(baseAlgo,
                byAlgo.values().iterator().next());
        double t0 = base.get(0).avgTimeMs();
        double n0 = sizes[0];

        List<String> logN  = new ArrayList<>();
        List<String> onV   = new ArrayList<>();
        List<String> nlogn = new ArrayList<>();
        List<String> on2   = new ArrayList<>();

        for (int n : sizes) {
            logN.add(fmt(t0 * log2(n)  / log2(n0)));
            onV.add(fmt(t0 * (n / n0)));
            nlogn.add(fmt(t0 * (n * log2(n)) / (n0 * log2(n0))));
            on2.add(fmt(t0 * Math.pow(n / n0, 2)));
        }

        sb.append(theo("O(log n)", logN,  COL_LOGN,  "[4,3]"));
        sb.append(theo("O(n)",     onV,   COL_N,     "[6,3]"));
        sb.append(theo("O(n log n)", nlogn, COL_NLOGN, "[3,3]"));
        sb.append(theo("O(n\u00B2)", on2, COL_N2,    "[10,5]"));

        return sb.toString();
    }

    private String theo(String label, List<String> vals, String color, String dash) {
        return String.format(
            ",{label:'Theoretical %s',data:[%s],borderColor:'%s'," +
            "borderDash:%s,fill:false,tension:0,pointRadius:0,borderWidth:2}",
            label, String.join(",", vals), color, dash);
    }

    // ── HTML template ─────────────────────────────────────────────────────────

    private String buildHtml(String labels, String runtimeDs, String memDs,
                              String compDs, String theoDs, int algoCount) {
        return "<!DOCTYPE html><html lang='en'><head>"
            + "<meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
            + "<title>Algorithm Benchmarking Report — COSC 742 Group 1</title>"
            + "<script src='https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js'></script>"
            + "<style>"
            + "*, *::before, *::after{box-sizing:border-box;}"
            + "body{font-family:'Segoe UI',system-ui,sans-serif;background:#0f172a;"
            + "color:#e2e8f0;margin:0;padding:24px;}"
            + "h1{text-align:center;color:#38bdf8;font-size:1.5rem;letter-spacing:.5px;margin-bottom:4px;}"
            + "p.sub{text-align:center;color:#94a3b8;font-size:.9rem;margin:0 0 28px;}"
            + ".grid{display:grid;grid-template-columns:repeat(2,1fr);gap:20px;"
            + "max-width:1280px;margin:auto;}"
            + ".card{background:#1e293b;border-radius:12px;padding:20px;"
            + "box-shadow:0 4px 24px rgba(0,0,0,.5);}"
            + ".card h2{font-size:.75rem;text-transform:uppercase;letter-spacing:.8px;"
            + "color:#7dd3fc;margin:0 0 16px;font-weight:600;}"
            + "canvas{max-height:340px;}"
            + ".legend{display:flex;flex-wrap:wrap;gap:8px;margin-bottom:12px;}"
            + "@media(max-width:860px){.grid{grid-template-columns:1fr;}}"
            + "</style></head><body>"
            + "<h1>&#x1F4CA; Algorithm Benchmarking Report</h1>"
            + "<p class='sub'>COSC 742 &mdash; Group 1 | Searching Algorithms Across Data Structures</p>"
            + "<div class='grid'>"
            + card("1. Runtime Growth per Algorithm",          "runtimeChart")
            + card("2. Memory Consumption per Algorithm",      "memoryChart")
            + card("3. Algorithm Comparison (All 4)",          "compChart")
            + card("4. Empirical vs Theoretical Complexity",   "theoChart")
            + "</div><script>"
            + lineChart("runtimeChart", labels, runtimeDs, "Input Size (n)", "Execution Time (ms)")
            + barChart("memoryChart",   labels, memDs,     "Input Size (n)", "Memory (KB)")
            + lineChart("compChart",    labels, compDs,    "Input Size (n)", "Execution Time (ms)")
            + lineChart("theoChart",    labels, theoDs,    "Input Size (n)", "Execution Time (ms)")
            + "</script></body></html>";
    }

    private String card(String title, String id) {
        return "<div class='card'><h2>" + title + "</h2>"
             + "<canvas id='" + id + "'></canvas></div>";
    }

    private String lineChart(String id, String labels, String datasets,
                              String xLabel, String yLabel) {
        return "new Chart(document.getElementById('" + id + "'),{"
             + "type:'line',"
             + "data:{labels:" + labels + ",datasets:[" + datasets + "]},"
             + options(xLabel, yLabel) + "});";
    }

    private String barChart(String id, String labels, String datasets,
                             String xLabel, String yLabel) {
        return "new Chart(document.getElementById('" + id + "'),{"
             + "type:'bar',"
             + "data:{labels:" + labels + ",datasets:[" + datasets + "]},"
             + options(xLabel, yLabel) + "});";
    }

    private String options(String xLabel, String yLabel) {
        return "options:{responsive:true,interaction:{mode:'index',intersect:false},"
             + "plugins:{legend:{labels:{color:'#e2e8f0',font:{size:11}}}},"
             + "scales:{"
             + "x:{ticks:{color:'#94a3b8'},grid:{color:'rgba(148,163,184,0.1)'},"
             + "title:{display:true,text:'" + xLabel + "',color:'#94a3b8'}},"
             + "y:{ticks:{color:'#94a3b8'},grid:{color:'rgba(148,163,184,0.1)'},"
             + "title:{display:true,text:'" + yLabel + "',color:'#94a3b8'}}}}";
    }

    // ── Data helpers ──────────────────────────────────────────────────────────

    private List<Double> getTimesMs(List<BenchmarkResult> list, int[] sizes) {
        Map<Integer, Double> map = list.stream()
                .collect(Collectors.toMap(r -> r.inputSize, BenchmarkResult::avgTimeMs));
        return Arrays.stream(sizes).mapToObj(n -> map.getOrDefault(n, 0.0))
                .collect(Collectors.toList());
    }

    private List<Double> getMemKB(List<BenchmarkResult> list, int[] sizes) {
        Map<Integer, Double> map = list.stream()
                .collect(Collectors.toMap(r -> r.inputSize, r -> r.memoryBytes / 1024.0));
        return Arrays.stream(sizes).mapToObj(n -> map.getOrDefault(n, 0.0))
                .collect(Collectors.toList());
    }

    private <T> String toJsArray(List<T> items) {
        return "[" + items.stream().map(Object::toString).collect(Collectors.joining(",")) + "]";
    }

    private String fmt(double v) { return String.format("%.8f", v); }
    private double log2(double n) { return Math.log(n) / Math.log(2); }
}
