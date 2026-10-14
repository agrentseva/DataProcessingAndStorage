package ru.nsu.ga.grentseva.synchronized_list;

import org.junit.jupiter.api.Test;
import ru.nsu.ga.grentseva.synchronized_list.custom.CustomLinkedList;
import ru.nsu.ga.grentseva.synchronized_list.standard.StandardList;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BenchmarkTest {

    private static final int ELEMENT_COUNT = 10;
    private static final int SORTER_COUNT = 4;
    private static final long DELAY_MILLIS = 1000;
    private static final long TEST_TIME_MILLIS = 2000;

    @Test
    void compareImplementations() throws InterruptedException {
        long customSteps = runBenchmark(new CustomLinkedList());
        long standardSteps = runBenchmark(new StandardList());

        System.out.println();
        System.out.println("=== Benchmark results ===");
        System.out.println("Elements: " + ELEMENT_COUNT);
        System.out.println("Sorter threads: " + SORTER_COUNT);
        System.out.println("Delay: " + DELAY_MILLIS + " ms");
        System.out.println("Test duration: " + TEST_TIME_MILLIS + " ms");
        System.out.println();

        System.out.println("CustomLinkedList steps: " + customSteps);
        System.out.println("StandardList steps: " + standardSteps);

        System.out.println();
        System.out.println("Theoretical steps per complete pass of one thread: "
                + (ELEMENT_COUNT - 1));
        System.out.println("Theoretical steps per complete pass of all threads: "
                + SORTER_COUNT * (ELEMENT_COUNT - 1));

        assertTrue(customSteps > 0);
        assertTrue(standardSteps > 0);
    }

    private long runBenchmark(SortableList list) throws InterruptedException {
        addTestValues(list);

        AtomicLong stepCounter = new AtomicLong();

        Thread[] sorters = new Thread[SORTER_COUNT];

        for (int i = 0; i < SORTER_COUNT; i++) {
            sorters[i] = new Thread(
                    new Sorter(list, DELAY_MILLIS, stepCounter),
                    "Benchmark-Sorter-" + (i + 1)
            );
        }

        long startTime = System.currentTimeMillis();

        for (Thread sorter : sorters) {
            sorter.start();
        }

        Thread.sleep(TEST_TIME_MILLIS);

        for (Thread sorter : sorters) {
            sorter.interrupt();
        }

        for (Thread sorter : sorters) {
            sorter.join();
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println(
                list.getClass().getSimpleName()
                        + ": " + stepCounter.get()
                        + " steps in "
                        + elapsedTime
                        + " ms"
        );

        return stepCounter.get();
    }

    private void addTestValues(SortableList list) {
        String[] values = {
                "zul",
                "aha",
                "eco",
                "br",
                "hotel",
                "clie",
                "golf",
                "dta",
                "frot",
                "ba"
        };

        for (String value : values) {
            list.addFirst(value);
        }
    }
}