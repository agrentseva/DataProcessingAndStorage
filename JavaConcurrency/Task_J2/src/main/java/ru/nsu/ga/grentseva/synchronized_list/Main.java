package ru.nsu.ga.grentseva.synchronized_list;

import ru.nsu.ga.grentseva.synchronized_list.custom.CustomLinkedList;
import ru.nsu.ga.grentseva.synchronized_list.standard.StandardList;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose list implementation:");
        System.out.println("1 - CustomLinkedList");
        System.out.println("2 - StandardList");
        System.out.print("Your choice: ");

        int implementation = scanner.nextInt();

        System.out.print("Number of sorter threads: ");
        int sorterCount = scanner.nextInt();

        System.out.print("Delay between steps (ms): ");
        long delayMillis = scanner.nextLong();

        scanner.nextLine();

        SortableList list;

        if (implementation == 1) {
            list = new CustomLinkedList();
        } else if (implementation == 2) {
            list = new StandardList();
        } else {
            System.out.println("Invalid choice.");
            scanner.close();
            return;
        }

        AtomicLong stepCounter = new AtomicLong();

        Thread[] sorters = new Thread[sorterCount];

        for (int i = 0; i < sorterCount; i++) {
            sorters[i] = new Thread(
                    new Sorter(list, delayMillis, stepCounter),
                    "Sorter-" + (i + 1)
            );

            sorters[i].start();
        }

        System.out.println();
        System.out.println("Enter strings.");
        System.out.println("Enter an empty line to print the current list.");
        System.out.println("Use Ctrl+D to terminate the program.");
        System.out.println();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.isEmpty()) {
                ListPrinter.print(list);

                System.out.println(
                        "Number of steps: " + stepCounter.get()
                );
            } else {
                addStringParts(list, line);
            }
        }

        for (Thread sorter : sorters) {
            sorter.interrupt();
        }

        scanner.close();
    }

    private static void addStringParts(
            SortableList list,
            String value
    ) {
        final int maxLength = 80;

        for (int end = value.length(); end > 0; end -= maxLength) {
            int start = Math.max(0, end - maxLength);

            list.addFirst(value.substring(start, end));
        }
    }
}