package edu.vt.ece.hw5.sets;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OptimisticSet<T> implements Set<T> {
    private final Node<T> head;

    public OptimisticSet() {
        head = new Node<>(Integer.MIN_VALUE);
        head.next = new Node<>(Integer.MAX_VALUE);
    }

    private boolean validate(Node<T> pred, Node<T> curr) {
        /* YOUR IMPLEMENTATION HERE */
        // Check if pred and curr are adjacent nodes and both are part of the current list
        Node<T> node = head;
        while (node != null && node.key <= pred.key) {
            if (node == pred) {
                // Validate that pred.next is still curr
                return pred.next == curr;
            }
            node = node.next;
        }
        return false;
    }

    @Override
    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Node<T> pred = head;
            Node<T> curr = head.next;
            // Lock-free traversal
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock();
            try {
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.key == key) {
                            return false; // Item already exists
                        } else {
                            Node<T> newNode = new Node<>(item, curr);
                            pred.next = newNode;
                            return true;
                        }
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                pred.unlock();
            }
        }
    }

    @Override
    public boolean remove(T item) {
        int key = item.hashCode();
        while (true) {
            Node<T> pred = head;
            Node<T> curr = head.next;
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock();
            try{
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.key != key) {
                            return false; // Item not found
                        } else {
                            pred.next = curr.next;
                            return true;
                        }
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                pred.unlock();
            }
        }
    }

    @Override
    public boolean contains(T item) {
        int key = item.hashCode();
        while (true) {
            Node<T> pred = head;
            Node<T> curr = head.next;
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock();
            try {
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        return curr.key == key;
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                pred.unlock();
            }
        }
    }

    private static class Node<U> {
        private final Lock lock = new ReentrantLock();
        int key;
        Node<U> next;

        public Node(U item, Node<U> next) {
            this.key = item.hashCode();
            this.next = next;
        }

        public Node(int key) {
            this.key = key;
            next = null;
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }
    }
}
