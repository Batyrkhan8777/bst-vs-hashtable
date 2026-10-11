/**
 * Unbalanced Binary Search Tree.
 * All operations are iterative (not recursive), so a degenerate tree
 * (e.g. sorted input, height = n) does not cause StackOverflowError.
 */
public class BST implements IntMap {

    private static class Node {
        int key;
        int value;
        Node left;
        Node right;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node root;
    private int size;

    @Override
    public void insert(int key, int value) {
        if (root == null) {
            root = new Node(key, value);
            size++;
            return;
        }
        Node cur = root;
        while (true) {
            if (key == cur.key) {          // key already exists: update value
                cur.value = value;
                return;
            }
            if (key < cur.key) {
                if (cur.left == null) {
                    cur.left = new Node(key, value);
                    break;
                }
                cur = cur.left;
            } else {
                if (cur.right == null) {
                    cur.right = new Node(key, value);
                    break;
                }
                cur = cur.right;
            }
        }
        size++;
    }

    @Override
    public Integer search(int key) {
        Node cur = root;
        while (cur != null) {
            if (key == cur.key) {
                return cur.value;
            }
            cur = (key < cur.key) ? cur.left : cur.right;
        }
        return null;
    }

    @Override
    public boolean delete(int key) {
        // 1. find the node and its parent
        Node parent = null;
        Node cur = root;
        while (cur != null && cur.key != key) {
            parent = cur;
            cur = (key < cur.key) ? cur.left : cur.right;
        }
        if (cur == null) {
            return false;                  // key not found
        }

        // 2. two children: copy the in-order successor here, then delete the successor
        if (cur.left != null && cur.right != null) {
            Node succParent = cur;
            Node succ = cur.right;
            while (succ.left != null) {
                succParent = succ;
                succ = succ.left;
            }
            cur.key = succ.key;
            cur.value = succ.value;
            parent = succParent;
            cur = succ;                    // now cur has at most one child
        }

        // 3. zero or one child: replace cur by its child
        Node child = (cur.left != null) ? cur.left : cur.right;
        if (parent == null) {
            root = child;
        } else if (parent.left == cur) {
            parent.left = child;
        } else {
            parent.right = child;
        }
        size--;
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    /** Height of the tree (number of levels). Empty tree = 0. */
    public int height() {
        if (root == null) {
            return 0;
        }
        java.util.ArrayDeque<Node> queue = new java.util.ArrayDeque<>();
        queue.add(root);
        int h = 0;
        while (!queue.isEmpty()) {
            h++;
            for (int i = queue.size(); i > 0; i--) {
                Node n = queue.poll();
                if (n.left != null) queue.add(n.left);
                if (n.right != null) queue.add(n.right);
            }
        }
        return h;
    }
}
