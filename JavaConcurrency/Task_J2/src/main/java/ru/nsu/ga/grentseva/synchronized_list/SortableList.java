package ru.nsu.ga.grentseva.synchronized_list;

public interface SortableList extends Iterable<String> {

    int size();

    boolean compareAndSwap(int index);

    Iterable<String> snapshot();

    void addFirst(String value);
}