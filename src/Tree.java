import java.util.*;

public class Tree implements Collection<Integer> {

    private Node root;
    private int size = 0;

    @Override
    public boolean add(Integer value) {
        root = insert(root, value);
        size++;
        return true;
    }

    private Node insert(Node node, int value) {
        if (node == null) {
            return new Node(value);
        }

        if (value < node.value) {
            node.left = insert(node.left, value);
        } else {
            node.right = insert(node.right, value);
        }

        return node;
    }

    @Override
    public boolean remove(Object o) {
        int value = (Integer) o;

        root = removeNode(root, value);
        size--;
        return true;
    }

    private Node removeNode(Node node, int value) {
        if (node == null) return null;

        if (value < node.value) {
            node.left = removeNode(node.left, value);
        } else if (value > node.value) {
            node.right = removeNode(node.right, value);
        } else {
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            Node min = findMin(node.right);
            node.value = min.value;
            node.right = removeNode(node.right, min.value);
        }

        return node;
    }

    private Node findMin(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    @Override
    public Iterator<Integer> iterator() {
        List<Integer> list = new ArrayList<>();
        inorder(root, list);
        return list.iterator();
    }

    private void inorder(Node node, List<Integer> list) {
        if (node == null) return;

        inorder(node.left, list);
        list.add(node.value);
        inorder(node.right, list);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override public boolean contains(Object o) { throw new UnsupportedOperationException(); }
    @Override public Object[] toArray() { throw new UnsupportedOperationException(); }
    @Override public <T> T[] toArray(T[] a) { throw new UnsupportedOperationException(); }
    @Override public boolean containsAll(Collection<?> c) { throw new UnsupportedOperationException(); }
    @Override public boolean addAll(Collection<? extends Integer> c) { throw new UnsupportedOperationException(); }
    @Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
    @Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
}