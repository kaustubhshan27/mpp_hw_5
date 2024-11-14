package edu.vt.ece.hw5.sets;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeSet<T> implements Set<T> {
    private final Node<T> head;

    public LockFreeSet() {
        Node<T> tail = new Node<>(Integer.MAX_VALUE);
        head = new Node<>(Integer.MIN_VALUE, tail);
    }

    @Override
    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Window<T> window = find(head, key);
            Node<T> pred = window.pred, curr = window.curr;

            if (curr.key == key) {
                return false; // Item already exists
            } else {
                Node<T> newNode = new Node<>(item, curr);
                if (pred.next.compareAndSet(curr, newNode, false, false)) {
                    return true; // Successfully added
                }
            }
        }
    }

    @Override
    public boolean remove(T item) {
        int key = item.hashCode();
        while (true) {
            Window<T> window = find(head, key);
            Node<T> pred = window.pred, curr = window.curr;

            if (curr.key != key) {
                return false; // Item not found
            } else {
                Node<T> succ = curr.next.getReference();
                if (!curr.next.compareAndSet(succ, succ, false, true)) {
                    continue; // Retry if marking failed
                }
                pred.next.compareAndSet(curr, succ, false, false); // Physically remove node
                return true; // Successfully removed
            }
        }
    }

    @Override
    public boolean contains(T item) {
        int key = item.hashCode();
        Node<T> curr = head;
        while (curr.key < key) {
            curr = curr.next.getReference();
        }
        return curr.key == key && !curr.next.isMarked(); // Return true if item is present and not marked
    }

    private Window<T> find(Node<T> head, int key) {
        Node<T> pred, curr, succ;
        boolean[] marked = {false};

        retry:
        while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) { // Skip over marked nodes
                    if (!pred.next.compareAndSet(curr, succ, false, false)) {
                        continue retry; // Restart if CAS fails
                    }
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key) {
                    return new Window<>(pred, curr); // Return the window with unmarked nodes
                }
                pred = curr;
                curr = succ;
            }
        }
    }

    private static class Node<U> {
        int key;
        AtomicMarkableReference<Node<U>> next;

        public Node(U item, Node<U> next) {
            this.key = item.hashCode();
            this.next = new AtomicMarkableReference<>(next, false);
        }

        public Node(int key, Node<U> next) {
            this.key = key;
            this.next = new AtomicMarkableReference<>(next, false);
        }

        public Node(int key) {
            this(key, null);
        }
    }

    private static class Window<V> {
        public Node<V> pred, curr;

        public Window(Node<V> pred, Node<V> curr) {
            this.pred = pred;
            this.curr = curr;
        }
    }
}