package ru.nsu.ga.grentseva.synchronized_list;

import org.junit.jupiter.api.Test;
import ru.nsu.ga.grentseva.synchronized_list.custom.CustomLinkedList;
import ru.nsu.ga.grentseva.synchronized_list.standard.StandardList;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SorterTest {

    @Test
    void shouldRunWithCustomList() throws InterruptedException {
        CustomLinkedList list = new CustomLinkedList();

        addTestValues(list);

        runSorter(list);

        assertTrue(isSorted(list));
    }

    @Test
    void shouldRunWithStandardList() throws InterruptedException {
        StandardList list = new StandardList();

        addTestValues(list);

        runSorter(list);

        assertTrue(isSorted(list));
    }

    @Test
    void shouldCountSortingSteps() throws InterruptedException {
        CustomLinkedList list = new CustomLinkedList();

        addTestValues(list);

        AtomicLong stepCounter = new AtomicLong();

        Thread sorter = new Thread(
                new Sorter(list, 1, stepCounter)
        );

        sorter.start();

        Thread.sleep(100);

        sorter.interrupt();
        sorter.join();

        assertTrue(stepCounter.get() > 0);
    }

    @Test
    void shouldStopAfterInterrupt() throws InterruptedException {
        StandardList list = new StandardList();

        addTestValues(list);

        AtomicLong stepCounter = new AtomicLong();

        Thread sorter = new Thread(
                new Sorter(list, 1, stepCounter)
        );

        sorter.start();

        Thread.sleep(50);

        sorter.interrupt();
        sorter.join(1000);

        assertFalse(sorter.isAlive());
    }

    @Test
    void shouldWorkWithSeveralSorterThreads()
            throws InterruptedException {

        CustomLinkedList list = new CustomLinkedList();

        addTestValues(list);

        AtomicLong stepCounter = new AtomicLong();

        Thread[] sorters = new Thread[4];

        for (int i = 0; i < sorters.length; i++) {
            sorters[i] = new Thread(
                    new Sorter(list, 1, stepCounter)
            );

            sorters[i].start();
        }

        Thread.sleep(200);

        for (Thread sorter : sorters) {
            sorter.interrupt();
        }

        for (Thread sorter : sorters) {
            sorter.join(1000);
        }

        assertTrue(stepCounter.get() > 0);
        assertTrue(isSorted(list));

        for (Thread sorter : sorters) {
            assertFalse(sorter.isAlive());
        }
    }

    private void addTestValues(SortableList list) {
        list.addFirst("a");
        list.addFirst("e");
        list.addFirst("c");
        list.addFirst("b");
        list.addFirst("d");
    }

    private void runSorter(SortableList list)
            throws InterruptedException {

        AtomicLong stepCounter = new AtomicLong();

        Thread sorter = new Thread(
                new Sorter(list, 1, stepCounter)
        );

        sorter.start();

        Thread.sleep(100);

        sorter.interrupt();
        sorter.join(1000);

        assertTrue(stepCounter.get() > 0);
        assertFalse(sorter.isAlive());
    }

    private boolean isSorted(SortableList list) {
        List<String> values = new java.util.ArrayList<>();

        for (String value : list) {
            values.add(value);
        }

        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i).compareTo(values.get(i + 1)) > 0) {
                return false;
            }
        }

        return true;
    }
}