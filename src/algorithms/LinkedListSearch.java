package algorithms;

/**
 * Singly Linked List with a search operation.
 * Time Complexity  : O(n) worst/average, O(1) best
 * Space Complexity : O(n) for the list; O(1) extra during search
 */
public class LinkedListSearch {

    // ── Inner node class ─────────────────────────────────────────────────────
    public static class Node {
        public int data;
        public Node next;

        public Node(int data) {
            this.data = data;
            this.next = null;
        }
    }

    // ── List head ────────────────────────────────────────────────────────────
    private Node head;

    /** Appends a new node at the tail of the list (O(n)). */
    public void append(int data) {
        Node newNode = new Node(data);
        if (head == null) {
            head = newNode;
            return;
        }
        Node current = head;
        while (current.next != null) current = current.next;
        current.next = newNode;
    }

    /**
     * Searches for {@code target} in the linked list.
     * @return the node containing target, or {@code null} if not found
     */
    public Node search(int target) {
        Node current = head;
        while (current != null) {
            if (current.data == target) return current;
            current = current.next;
        }
        return null;
    }

    // ── Convenience factory ───────────────────────────────────────────────────
    /**
     * Builds a LinkedListSearch from an int array (used by the benchmark engine).
     */
    public static LinkedListSearch fromArray(int[] arr) {
        LinkedListSearch list = new LinkedListSearch();
        for (int v : arr) list.append(v);
        return list;
    }
}
