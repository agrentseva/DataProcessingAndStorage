package ru.nsu.ga.grentseva.synchronized_list.standard;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardListTest {

    @Test
    void shouldAddElementsToBeginning() {
        StandardList list = new StandardList();

        list.addFirst("a");
        list.addFirst("b");
        list.addFirst("c");

        assertEquals(
                List.of("c", "b", "a"),
                toList(list)
        );
    }

    @Test
    void shouldHandleEmptyList() {
        StandardList list = new StandardList();

        assertEquals(0, list.size());
        assertTrue(toList(list).isEmpty());
    }

    @Test
    void shouldHandleSingleElement() {
        StandardList list = new StandardList();

        list.addFirst("hello");

        assertEquals(1, list.size());
        assertEquals(
                List.of("hello"),
                toList(list)
        );
    }

    @Test
    void shouldSwapAdjacentElements() {
        StandardList list = new StandardList();

        list.addFirst("c");
        list.addFirst("a");
        list.addFirst("b");

        // b -> a -> c
        assertTrue(list.compareAndSwap(0));

        // a -> b -> c
        assertEquals(
                List.of("a", "b", "c"),
                toList(list)
        );
    }

    @Test
    void shouldSwapLastPair() {
        StandardList list = new StandardList();

        list.addFirst("b");
        list.addFirst("c");
        list.addFirst("a");

        // a -> c -> b
        assertTrue(list.compareAndSwap(1));

        // a -> b -> c
        assertEquals(
                List.of("a", "b", "c"),
                toList(list)
        );
    }

    @Test
    void shouldNotSwapAlreadySortedElements() {
        StandardList list = new StandardList();

        list.addFirst("b");
        list.addFirst("a");

        // a -> b
        assertFalse(list.compareAndSwap(0));

        assertEquals(
                List.of("a", "b"),
                toList(list)
        );
    }

    @Test
    void shouldNotSwapInvalidIndex() {
        StandardList list = new StandardList();

        list.addFirst("b");
        list.addFirst("a");

        assertFalse(list.compareAndSwap(-1));
        assertFalse(list.compareAndSwap(2));
        assertFalse(list.compareAndSwap(100));

        assertEquals(
                List.of("a", "b"),
                toList(list)
        );
    }

    @Test
    void shouldReturnCorrectSize() {
        StandardList list = new StandardList();

        assertEquals(0, list.size());

        list.addFirst("a");
        list.addFirst("b");
        list.addFirst("c");

        assertEquals(3, list.size());
    }

    @Test
    void shouldCreateCorrectSnapshot() {
        StandardList list = new StandardList();

        list.addFirst("c");
        list.addFirst("b");
        list.addFirst("a");

        assertEquals(
                List.of("a", "b", "c"),
                toList(list.snapshot())
        );
    }

    @Test
    void shouldWorkWithForEach() {
        StandardList list = new StandardList();

        list.addFirst("c");
        list.addFirst("b");
        list.addFirst("a");

        List<String> result = new ArrayList<>();

        for (String value : list) {
            result.add(value);
        }

        assertEquals(
                List.of("a", "b", "c"),
                result
        );
    }

    private List<String> toList(Iterable<String> iterable) {
        List<String> result = new ArrayList<>();

        for (String value : iterable) {
            result.add(value);
        }

        return result;
    }
}