package framework;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ComplexityAnalyzer
 * ──────────────────
 * Estimates which theoretical complexity class best fits the empirical timing
 * data using ordinary least-squares (OLS) linear regression after transforming
 * the independent variable (n) according to each candidate function.
 *
 * Candidate complexity models:
 *   O(log n)   : f(n) = log₂(n)          ← critical for Binary Search & BST
 *   O(n)       : f(n) = n
 *   O(n log n) : f(n) = n · log₂(n)
 *   O(n²)      : f(n) = n²
 *
 * Regression model:  T(n) ≈ a · f(n) + b
 * Best-fit class    = highest R² coefficient of determination.
 * R² ≥ 0.95 is considered strong evidence for the fitted class.
 */
public class ComplexityAnalyzer {

    public enum ComplexityClass {
        O_LOG_N, O_N, O_N_LOG_N, O_N_SQUARED, UNKNOWN;

        @Override
        public String toString() {
            return switch (this) {
                case O_LOG_N     -> "O(log n)";
                case O_N         -> "O(n)";
                case O_N_LOG_N   -> "O(n log n)";
                case O_N_SQUARED -> "O(n\u00B2)";
                default          -> "Unknown";
            };
        }
    }

    public static class FitResult {
        public final String          algorithmName;
        public final ComplexityClass bestFit;
        public final double          rSquaredLogN;
        public final double          rSquaredLinear;
        public final double          rSquaredNLogN;
        public final double          rSquaredQuadratic;

        FitResult(String name, ComplexityClass best,
                  double r2logn, double r2n, double r2nlogn, double r2n2) {
            this.algorithmName    = name;
            this.bestFit          = best;
            this.rSquaredLogN     = r2logn;
            this.rSquaredLinear   = r2n;
            this.rSquaredNLogN    = r2nlogn;
            this.rSquaredQuadratic = r2n2;
        }

        @Override
        public String toString() {
            return String.format(
                "%-30s → Best fit: %-12s | R²: log n=%.4f  n=%.4f  n·log n=%.4f  n²=%.4f",
                algorithmName, bestFit,
                rSquaredLogN, rSquaredLinear, rSquaredNLogN, rSquaredQuadratic);
        }
    }

    /**
     * Analyses timing results for a single algorithm name.
     */
    public FitResult analyze(List<BenchmarkResult> results, String algorithmName) {
        List<BenchmarkResult> filtered = results.stream()
                .filter(r -> r.algorithmName.equals(algorithmName))
                .collect(Collectors.toList());

        if (filtered.size() < 3)
            return new FitResult(algorithmName, ComplexityClass.UNKNOWN, 0, 0, 0, 0);

        double[] ns = filtered.stream().mapToDouble(r -> r.inputSize).toArray();
        double[] ts = filtered.stream().mapToDouble(r -> (double) r.avgTimeNs).toArray();

        double r2logn  = rSquared(transform(ns, n -> log2(n)),         ts);
        double r2n     = rSquared(transform(ns, n -> n),               ts);
        double r2nlogn = rSquared(transform(ns, n -> n * log2(n)),     ts);
        double r2n2    = rSquared(transform(ns, n -> n * n),           ts);

        double maxR2 = Math.max(r2logn, Math.max(r2n, Math.max(r2nlogn, r2n2)));

        ComplexityClass best;
        if      (maxR2 == r2logn)  best = ComplexityClass.O_LOG_N;
        else if (maxR2 == r2n)     best = ComplexityClass.O_N;
        else if (maxR2 == r2nlogn) best = ComplexityClass.O_N_LOG_N;
        else                       best = ComplexityClass.O_N_SQUARED;

        return new FitResult(algorithmName, best, r2logn, r2n, r2nlogn, r2n2);
    }

    // ── Math helpers ──────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface Transform { double apply(double n); }

    private double[] transform(double[] ns, Transform fn) {
        double[] out = new double[ns.length];
        for (int i = 0; i < ns.length; i++) out[i] = fn.apply(ns[i]);
        return out;
    }

    private double log2(double n) { return Math.log(n) / Math.log(2); }

    /** Pearson R² for simple OLS regression of y on x. */
    private double rSquared(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0;
        for (int i = 0; i < n; i++) { sumX += x[i]; sumY += y[i]; }
        double meanX = sumX / n, meanY = sumY / n;

        double ssXY = 0, ssXX = 0, ssTot = 0;
        for (int i = 0; i < n; i++) {
            ssXY  += (x[i] - meanX) * (y[i] - meanY);
            ssXX  += (x[i] - meanX) * (x[i] - meanX);
            ssTot += (y[i] - meanY) * (y[i] - meanY);
        }
        if (ssXX == 0 || ssTot == 0) return 0;
        double slope = ssXY / ssXX;
        double b     = meanY - slope * meanX;

        double ssRes = 0;
        for (int i = 0; i < n; i++) {
            double yHat = slope * x[i] + b;
            ssRes += Math.pow(y[i] - yHat, 2);
        }
        return 1.0 - (ssRes / ssTot);
    }
}
