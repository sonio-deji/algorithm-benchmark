package framework;

import algorithms.BinarySearch;
import algorithms.BinarySearchTree;
import algorithms.LinearSearch;
import algorithms.LinkedListSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * BenchmarkEngine
 * ───────────────
 * Executes all four search algorithms repeatedly over multiple input sizes,
 * records execution time (nanoseconds) and memory consumption (bytes),
 * and returns a list of {@link BenchmarkResult} objects.
 *
 * Algorithms benchmarked:
 *   1. Linear Search       — unsorted int[]
 *   2. Linear Search       — singly linked list
 *   3. Binary Search       — sorted int[]
 *   4. BST Search          — binary search tree
 *
 * Warm-up rounds are performed before measurement to reduce JIT bias.
 *
 * Key design decision — consistent worst-case targets:
 *   - Linear Search (Array/LL) : target = last element → O(n) full traversal
 *   - Binary Search (Sorted)   : target = last element of sorted array
 *                                → maximum number of halvings (log₂ n steps)
 *   - BST Search               : target = median value → expected O(log n)
 */
public class BenchmarkEngine {

    private static final int WARMUP_ROUNDS      = 5;
    private static final int MEASUREMENT_ROUNDS = 20;

    public static final int[] INPUT_SIZES = {100, 1_000, 5_000, 10_000, 50_000, 100_000};

    // Stable names — used as keys in VisualizationEngine and ComplexityAnalyzer
    public static final String ALGO_LINEAR_ARRAY  = "Linear Search (Array)";
    public static final String ALGO_LINEAR_LL     = "Linear Search (Linked List)";
    public static final String ALGO_BINARY_ARRAY  = "Binary Search (Array)";
    public static final String ALGO_BST           = "BST Search";

    private final InputGenerator generator;

    public BenchmarkEngine() {
        this.generator = new InputGenerator();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public List<BenchmarkResult> runAll() {
        List<BenchmarkResult> results = new ArrayList<>();
        for (int n : INPUT_SIZES) {
            System.out.printf("  n = %,7d  ...%n", n);
            results.add(benchmarkLinearSearchArray(n));
            results.add(benchmarkLinearSearchLinkedList(n));
            results.add(benchmarkBinarySearchArray(n));
            results.add(benchmarkBSTSearch(n));
        }
        return results;
    }

    // ── Algorithm 1: Linear Search on unsorted array ─────────────────────────
    public BenchmarkResult benchmarkLinearSearchArray(int n) {
        int[] data   = generator.generate(n, InputGenerator.Strategy.RANDOM);
        int   target = generator.worstCaseTarget(data);

        for (int i = 0; i < WARMUP_ROUNDS; i++) LinearSearch.search(data, target);

        long[] times = new long[MEASUREMENT_ROUNDS];
        for (int r = 0; r < MEASUREMENT_ROUNDS; r++) {
            long t = System.nanoTime();
            LinearSearch.search(data, target);
            times[r] = System.nanoTime() - t;
        }
        return buildResult(ALGO_LINEAR_ARRAY, "Unsorted Array", n, times, 16L + (long) n * 4);
    }

    // ── Algorithm 2: Linear Search on singly linked list ─────────────────────
    public BenchmarkResult benchmarkLinearSearchLinkedList(int n) {
        int[]            data   = generator.generate(n, InputGenerator.Strategy.RANDOM);
        LinkedListSearch list   = LinkedListSearch.fromArray(data);
        int              target = generator.worstCaseTarget(data);

        for (int i = 0; i < WARMUP_ROUNDS; i++) list.search(target);

        long[] times = new long[MEASUREMENT_ROUNDS];
        for (int r = 0; r < MEASUREMENT_ROUNDS; r++) {
            long t = System.nanoTime();
            list.search(target);
            times[r] = System.nanoTime() - t;
        }
        return buildResult(ALGO_LINEAR_LL, "Singly Linked List", n, times, (long) n * 28);
    }

    // ── Algorithm 3: Binary Search on sorted array ────────────────────────────
    // The sort is performed OUTSIDE the timed loop — it is setup cost, not
    // search cost. This faithfully models the scenario where a sorted structure
    // is maintained and searched repeatedly.
    public BenchmarkResult benchmarkBinarySearchArray(int n) {
        int[] raw    = generator.generate(n, InputGenerator.Strategy.RANDOM);
        int[] sorted = generator.sortedCopy(raw);                // setup — NOT timed
        int   target = generator.binarySearchWorstCaseTarget(sorted);

        for (int i = 0; i < WARMUP_ROUNDS; i++) BinarySearch.search(sorted, target);

        long[] times = new long[MEASUREMENT_ROUNDS];
        for (int r = 0; r < MEASUREMENT_ROUNDS; r++) {
            long t = System.nanoTime();
            BinarySearch.search(sorted, target);
            times[r] = System.nanoTime() - t;
        }
        return buildResult(ALGO_BINARY_ARRAY, "Sorted Array", n, times, 16L + (long) n * 4);
    }

    // ── Algorithm 4: Search in Binary Search Tree ─────────────────────────────
    public BenchmarkResult benchmarkBSTSearch(int n) {
        int[]            data   = generator.generate(n, InputGenerator.Strategy.RANDOM);
        BinarySearchTree bst    = BinarySearchTree.fromArray(data);
        int              target = data[n / 2];

        for (int i = 0; i < WARMUP_ROUNDS; i++) bst.search(target);

        long[] times = new long[MEASUREMENT_ROUNDS];
        for (int r = 0; r < MEASUREMENT_ROUNDS; r++) {
            long t = System.nanoTime();
            bst.search(target);
            times[r] = System.nanoTime() - t;
        }
        return buildResult(ALGO_BST, "Binary Search Tree", n, times, (long) n * 36);
    }

    // ── Statistical helpers ───────────────────────────────────────────────────

    private BenchmarkResult buildResult(String algo, String ds, int n,
                                         long[] times, long memBytes) {
        long sum = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (long t : times) {
            sum += t;
            if (t < min) min = t;
            if (t > max) max = t;
        }
        long   avg    = sum / times.length;
        double stdDev = computeStdDev(times, avg);
        return new BenchmarkResult(algo, ds, n, avg, min, max, stdDev, memBytes);
    }

    private double computeStdDev(long[] values, long mean) {
        double variance = 0;
        for (long v : values) variance += Math.pow(v - mean, 2);
        return Math.sqrt(variance / values.length);
    }
}
