package framework;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ResultsDatabase
 * ───────────────
 * Persists benchmark results to a CSV file so they can be imported into
 * spreadsheet tools or Python/R for further statistical analysis.
 *
 * CSV columns:
 *   timestamp, algorithm, data_structure, input_size,
 *   avg_time_ms, min_time_ns, max_time_ns, std_dev_ns, memory_bytes
 */
public class ResultsDatabase {

    private static final String HEADER =
            "timestamp,algorithm,data_structure,input_size," +
            "avg_time_ms,min_time_ns,max_time_ns,std_dev_ns,memory_bytes";

    private final Path outputPath;

    public ResultsDatabase(String filePath) {
        this.outputPath = Paths.get(filePath);
    }

    /** Writes all results to the CSV file, overwriting any previous content. */
    public void save(List<BenchmarkResult> results) throws IOException {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write(HEADER);
            writer.newLine();
            for (BenchmarkResult r : results) {
                writer.write(toCsvRow(r, timestamp));
                writer.newLine();
            }
        }
        System.out.println("[ResultsDatabase] Saved " + results.size()
                + " records → " + outputPath.toAbsolutePath());
    }

    /** Appends a single result row (useful for streaming). */
    public void append(BenchmarkResult r) throws IOException {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        boolean exists = Files.exists(outputPath);
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(outputPath.toFile(), true))) {
            if (!exists) { writer.write(HEADER); writer.newLine(); }
            writer.write(toCsvRow(r, timestamp));
            writer.newLine();
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────
    private String toCsvRow(BenchmarkResult r, String ts) {
        return String.join(",",
                ts,
                quote(r.algorithmName),
                quote(r.dataStructure),
                String.valueOf(r.inputSize),
                String.format("%.6f", r.avgTimeMs()),
                String.valueOf(r.minTimeNs),
                String.valueOf(r.maxTimeNs),
                String.format("%.2f", r.stdDevNs),
                String.valueOf(r.memoryBytes)
        );
    }

    private String quote(String s) { return "\"" + s + "\""; }
}
