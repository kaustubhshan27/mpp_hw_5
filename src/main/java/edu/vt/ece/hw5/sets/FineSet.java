package edu.vt.ece.hw5.sets;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FineSet<T> implements Set<T>{
    private final Node<T> head;

    public FineSet() {
        head = new Node<>(Integer.MIN_VALUE);
        head.next = new Node<>(Integer.MAX_VALUE);
    }

    @Override
    public boolean add(T item) {
        int key = item.hashCode();
        head.lock();
        Node<T> pred = head;
        try {
            Node<T> curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.key == key) {
                    // Item already exists
                    return false;
                } else {
                    // Insert new node between pred and curr
                    Node<T> newNode = new Node<>(item, curr);
                    pred.next = newNode;
                    return true;
                }
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    @Override
    public boolean remove(T item) {
        int key = item.hashCode();
        head.lock();
        Node<T> pred = head;
        try {
            Node<T> curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.key == key) {
                    // Item found; remove it
                    pred.next = curr.next;
                    return true;
                } else {
                    // Item not found
                    return false;
                }
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    @Override
    public boolean contains(T item) {
        int key = item.hashCode();
        head.lock();
        Node<T> pred = head;
        try {
            Node<T> curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                return curr.key == key;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
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
