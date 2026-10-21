package ru.nsu.ga.grentseva.synchronized_list;

public class ListPrinter {

    public static void print(SortableList list) {
        StringBuilder result = new StringBuilder();

        for (String value : list) {
            if (result.length() > 0) {
                result.append(" -> ");
            }

            result.append(value);
        }

        System.out.println(result);
    }
}