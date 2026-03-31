package algorithms;

/**
 * Binary Search Tree (BST) with insert and search operations.
 *
 * Time Complexity  (balanced) : O(log n) average
 * Time Complexity  (skewed)   : O(n) worst case
 * Space Complexity             : O(n) for the tree; O(h) call stack during search
 */
public class BinarySearchTree {

    // ── Inner node class ─────────────────────────────────────────────────────
    public static class BSTNode {
        public int key;
        public BSTNode left, right;

        public BSTNode(int key) {
            this.key = key;
        }
    }

    // ── Root ─────────────────────────────────────────────────────────────────
    private BSTNode root;

    /** Inserts a key into the BST (iterative). */
    public void insert(int key) {
        BSTNode newNode = new BSTNode(key);
        if (root == null) { root = newNode; return; }

        BSTNode current = root;
        while (true) {
            if (key < current.key) {
                if (current.left == null) { current.left = newNode; return; }
                current = current.left;
            } else if (key > current.key) {
                if (current.right == null) { current.right = newNode; return; }
                current = current.right;
            } else {
                return; // duplicate — ignore
            }
        }
    }

    /**
     * Searches for {@code target} iteratively.
     * @return the node if found, {@code null} otherwise
     */
    public BSTNode search(int target) {
        BSTNode current = root;
        while (current != null) {
            if (target == current.key)  return current;
            current = (target < current.key) ? current.left : current.right;
        }
        return null;
    }

    // ── Convenience factory ───────────────────────────────────────────────────
    /**
     * Builds a BST from a shuffled copy of the array to get a balanced-ish tree.
     * Inserting a sorted array would produce a degenerate (O(n)) BST, which is
     * worth noting in the research paper as a threat to validity.
     */
    public static BinarySearchTree fromArray(int[] arr) {
        // Insert in the order supplied (caller decides ordering strategy)
        BinarySearchTree bst = new BinarySearchTree();
        for (int v : arr) bst.insert(v);
        return bst;
    }
}
