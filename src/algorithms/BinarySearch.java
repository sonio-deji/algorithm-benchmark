package algorithms;

/**
 * Binary Search on a sorted primitive integer array.
 *
 * PRE-CONDITION: arr must be sorted in ascending order.
 *
 * Time Complexity  : O(log n) average and worst case
 *                    O(1) best case (target at midpoint)
 * Space Complexity : O(1) — iterative implementation, no call stack growth
 */
public class BinarySearch {

    /**
     * Searches for {@code target} in the sorted array {@code arr}.
     * @return index of target if found, -1 otherwise
     */
    public static int search(int[] arr, int target) {
        int low  = 0;
        int high = arr.length - 1;

        while (low <= high) {
            int mid = low + ((high - low) >> 1); // avoids integer overflow
            if (arr[mid] == target) return mid;
            if (arr[mid] <  target) low  = mid + 1;
            else                    high = mid - 1;
        }
        return -1;
    }
}
