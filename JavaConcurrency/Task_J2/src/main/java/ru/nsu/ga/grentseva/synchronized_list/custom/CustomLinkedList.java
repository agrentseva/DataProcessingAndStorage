package ru.nsu.ga.grentseva.synchronized_list.custom;

import ru.nsu.ga.grentseva.synchronized_list.SortableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomLinkedList implements SortableList {

    private Node head;
    private Node tail;
    private int size;

    private final Object structureLock = new Object();

    @Override
    public void addFirst(String value) {
        synchronized (structureLock) {
            Node newNode = new Node(value);

            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                newNode.setNext(head);
                head.setPrev(newNode);
                head = newNode;
            }

            size++;
        }
    }

    public Node getHead() {
        synchronized (structureLock) {
            return head;
        }
    }

    public Node getTail() {
        synchronized (structureLock) {
            return tail;
        }
    }

    @Override
    public int size() {
        synchronized (structureLock) {
            return size;
        }
    }

    @Override
    public boolean compareAndSwap(int index) {
        Node previous;
        Node first;
        Node second;

        synchronized (structureLock) {
            if (index < 0 || index + 1 >= size) {
                return false;
            }

            first = head;

            for (int i = 0; i < index; i++) {
                first = first.getNext();
            }

            second = first.getNext();
            previous = first.getPrev();
        }

        if (previous == null) {
            synchronized (first) {
                synchronized (second) {
                    return swapIfNeeded(first, second, null);
                }
            }
        }

        synchronized (previous) {
            synchronized (first) {
                synchronized (second) {
                    return swapIfNeeded(first, second, previous);
                }
            }
        }
    }

    private boolean swapIfNeeded(
            Node first,
            Node second,
            Node previous
    ) {
        synchronized (structureLock) {
            if (first.getNext() != second) {
                return false;
            }

            if (second.getPrev() != first) {
                return false;
            }

            if (first.getPrev() != previous) {
                return false;
            }

            if (previous != null && previous.getNext() != first) {
                return false;
            }

            if (first.getValue().compareTo(second.getValue()) <= 0) {
                return false;
            }

            swapAdjacent(first, second);
            return true;
        }
    }

    private void swapAdjacent(Node first, Node second) {
        Node previous = first.getPrev();
        Node next = second.getNext();

        if (previous == null) {
            head = second;
        } else {
            previous.setNext(second);
        }

        second.setPrev(previous);
        second.setNext(first);

        first.setPrev(second);
        first.setNext(next);

        if (next == null) {
            tail = first;
        } else {
            next.setPrev(first);
        }
    }

    @Override
    public Iterable<String> snapshot() {
        List<String> result = new ArrayList<>();

        synchronized (structureLock) {
            Node current = head;

            while (current != null) {
                result.add(current.getValue());
                current = current.getNext();
            }
        }

        return result;
    }

    @Override
    public Iterator<String> iterator() {
        return snapshot().iterator();
    }
}