package framework;

import java.util.Random;

/**
 * Generates integer datasets of varying sizes for benchmarking.
 *
 * Strategies:
 *  - RANDOM   : shuffled integers in [0, n*10]
 *  - SORTED   : ascending order (worst case for Linear / LinkedList)
 *  - REVERSED : descending order
 */
public class InputGenerator {

    public enum Strategy { RANDOM, SORTED, REVERSED }

    private final Random rng;

    public InputGenerator(long seed) {
        this.rng = new Random(seed);
    }

    public InputGenerator() {
        this(42L); // reproducible default seed
    }

    /**
     * Generates an integer array of the requested size using the given strategy.
     */
    public int[] generate(int size, Strategy strategy) {
        int[] arr = new int[size];
        switch (strategy) {
            case SORTED:
                for (int i = 0; i < size; i++) arr[i] = i;
                break;
            case REVERSED:
                for (int i = 0; i < size; i++) arr[i] = size - i;
                break;
            case RANDOM:
            default:
                for (int i = 0; i < size; i++) arr[i] = rng.nextInt(size * 10);
                fisherYatesShuffle(arr);
                break;
        }
        return arr;
    }

    /** Returns a target that is guaranteed to exist in the array (last element). */
    public int worstCaseTarget(int[] arr) {
        return arr[arr.length - 1]; // forces full traversal for linear / LL
    }

    /** Returns a target that is NOT present (forces full traversal always). */
    public int missingTarget(int[] arr) {
        return Integer.MAX_VALUE;
    }

    /**
     * Returns a sorted copy of {@code arr}.
     * Required as pre-condition for Binary Search on sorted array.
     * Uses Java's dual-pivot quicksort — O(n log n) — and is called once
     * during data-structure setup, not inside the timed benchmark loop.
     */
    public int[] sortedCopy(int[] arr) {
        int[] copy = arr.clone();
        java.util.Arrays.sort(copy);
        return copy;
    }

    /**
     * Returns a worst-case target for Binary Search: the last element of a
     * sorted array forces the maximum number of halvings before the value
     * is found (rightmost leaf of the implicit search tree).
     */
    public int binarySearchWorstCaseTarget(int[] sortedArr) {
        return sortedArr[sortedArr.length - 1];
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private void fisherYatesShuffle(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
    }
}
