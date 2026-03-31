package framework;

/**
 * Stores a single benchmark observation.
 */
public class BenchmarkResult {

    public final String algorithmName;
    public final String dataStructure;
    public final int    inputSize;
    public final long   avgTimeNs;       // average execution time in nanoseconds
    public final long   minTimeNs;
    public final long   maxTimeNs;
    public final double stdDevNs;
    public final long   memoryBytes;     // approx. heap delta in bytes

    public BenchmarkResult(String algorithmName,
                           String dataStructure,
                           int    inputSize,
                           long   avgTimeNs,
                           long   minTimeNs,
                           long   maxTimeNs,
                           double stdDevNs,
                           long   memoryBytes) {
        this.algorithmName = algorithmName;
        this.dataStructure = dataStructure;
        this.inputSize     = inputSize;
        this.avgTimeNs     = avgTimeNs;
        this.minTimeNs     = minTimeNs;
        this.maxTimeNs     = maxTimeNs;
        this.stdDevNs      = stdDevNs;
        this.memoryBytes   = memoryBytes;
    }

    /** Convenience: time in milliseconds (double). */
    public double avgTimeMs() { return avgTimeNs / 1_000_000.0; }

    @Override
    public String toString() {
        return String.format("%-20s | %-15s | n=%-8d | avg=%8.4f ms | σ=%8.2f ns | mem=%,d B",
                algorithmName, dataStructure, inputSize, avgTimeMs(), stdDevNs, memoryBytes);
    }
}
