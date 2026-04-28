import java.util.*;

public class Tree<T extends Comparable<T>> implements Collection<T> {

    private Node<T> root;
    private int size = 0;

    @Override
    public boolean add(T value) {
        root = insert(root, value);
        size++;
        return true;
    }

    private Node<T> insert(Node<T> node, T value) {
        if (node == null) {
            return new Node<>(value);
        }

        int cmp = value.compareTo(node.value);
        if (cmp < 0) {
            node.left = insert(node.left, value);
        } else if (cmp > 0) {
            node.right = insert(node.right, value);
        }

        return node;
    }

    @Override
    public boolean remove(Object o) {
        @SuppressWarnings("unchecked")
        T value = (T) o;

        root = removeNode(root, value);
        size--;
        return true;
    }

    private Node<T> removeNode(Node<T> node, T value) {
        if (node == null) return null;

        int cmp = value.compareTo(node.value);
        
        if (cmp < 0) {
            node.left = removeNode(node.left, value);
        } else if (cmp > 0) {
            node.right = removeNode(node.right, value);
        } else {
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            Node<T> min = findMin(node.right);
            node.value = min.value;
            node.right = removeNode(node.right, min.value);
        }

        return node;
    }

    private Node<T> findMin(Node<T> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    @Override
    public Iterator<T> iterator() {
        List<T> list = new ArrayList<>();
        inorder(root, list);
        return list.iterator();
    }

    private void inorder(Node<T> node, List<T> list) {
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
    @Override public <T1> T1[] toArray(T1[] a) { throw new UnsupportedOperationException(); }
    @Override public boolean containsAll(Collection<?> c) { throw new UnsupportedOperationException(); }
    @Override public boolean addAll(Collection<? extends Integer> c) { throw new UnsupportedOperationException(); }
    @Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
    @Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
}
