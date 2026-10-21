package ru.nsu.ga.grentseva.synchronized_list.standard;

import ru.nsu.ga.grentseva.synchronized_list.SortableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StandardList implements SortableList {

    private final List<String> list =
            Collections.synchronizedList(new ArrayList<>());

    @Override
    public int size() {
        synchronized (list) {
            return list.size();
        }
    }

    @Override
    public void addFirst(String value) {
        synchronized (list) {
            list.add(0, value);
        }
    }

    @Override
    public boolean compareAndSwap(int index) {
        synchronized (list) {
            if (index < 0 || index + 1 >= list.size()) {
                return false;
            }

            String first = list.get(index);
            String second = list.get(index + 1);

            if (first.compareTo(second) > 0) {
                list.set(index, second);
                list.set(index + 1, first);
                return true;
            }

            return false;
        }
    }

    @Override
    public Iterable<String> snapshot() {
        synchronized (list) {
            return new ArrayList<>(list);
        }
    }

    @Override
    public Iterator<String> iterator() {
        return snapshot().iterator();
    }
}