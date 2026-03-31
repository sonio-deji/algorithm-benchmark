package algorithms;

/**
 * Linear Search on a primitive integer array.
 * Time Complexity  : O(n) worst/average, O(1) best
 * Space Complexity : O(1)
 */
public class LinearSearch {

    /**
     * Searches for {@code target} in {@code arr}.
     * @return index of target if found, -1 otherwise
     */
    public static int search(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) return i;
        }
        return -1;
    }
}
