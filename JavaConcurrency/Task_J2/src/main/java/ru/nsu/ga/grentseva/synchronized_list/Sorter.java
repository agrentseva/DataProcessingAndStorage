package ru.nsu.ga.grentseva.synchronized_list;

import java.util.concurrent.atomic.AtomicLong;

public class Sorter implements Runnable {

    private final SortableList list;
    private final long delayMillis;
    private final AtomicLong stepCounter;

    public Sorter(
            SortableList list,
            long delayMillis,
            AtomicLong stepCounter
    ) {
        this.list = list;
        this.delayMillis = delayMillis;
        this.stepCounter = stepCounter;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            sortPass();
        }
    }

    private void sortPass() {
        int size = list.size();

        for (int i = 0; i < size - 1; i++) {
            stepCounter.incrementAndGet();

            list.compareAndSwap(i);

            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}