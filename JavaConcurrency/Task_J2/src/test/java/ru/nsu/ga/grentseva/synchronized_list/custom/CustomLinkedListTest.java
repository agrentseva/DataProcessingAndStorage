package ru.nsu.ga.grentseva.synchronized_list.custom;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomLinkedListTest {

    @Test
    void shouldAddElementsToBeginning() {
        CustomLinkedList list = new CustomLinkedList();

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
        CustomLinkedList list = new CustomLinkedList();

        assertEquals(0, list.size());
        assertTrue(toList(list).isEmpty());
        assertNull(list.getHead());
        assertNull(list.getTail());
    }

    @Test
    void shouldHandleSingleElement() {
        CustomLinkedList list = new CustomLinkedList();

        list.addFirst("hello");

        assertEquals(1, list.size());
        assertEquals(List.of("hello"), toList(list));
        assertEquals(list.getHead(), list.getTail());
    }

    @Test
    void shouldSwapAdjacentNodes() {
        CustomLinkedList list = new CustomLinkedList();

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
        CustomLinkedList list = new CustomLinkedList();

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
        CustomLinkedList list = new CustomLinkedList();

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
        CustomLinkedList list = new CustomLinkedList();

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
        CustomLinkedList list = new CustomLinkedList();

        assertEquals(0, list.size());

        list.addFirst("a");
        list.addFirst("b");
        list.addFirst("c");

        assertEquals(3, list.size());
    }

    @Test
    void shouldKeepLinksCorrectAfterSwap() {
        CustomLinkedList list = new CustomLinkedList();

        list.addFirst("c");
        list.addFirst("a");
        list.addFirst("b");

        // b -> a -> c
        assertTrue(list.compareAndSwap(0));

        // a -> b -> c
        Node first = list.getHead();
        Node second = first.getNext();
        Node third = second.getNext();

        assertEquals("a", first.getValue());
        assertEquals("b", second.getValue());
        assertEquals("c", third.getValue());

        assertNull(first.getPrev());
        assertEquals(first, second.getPrev());
        assertEquals(second, third.getPrev());

        assertEquals(second, first.getNext());
        assertEquals(third, second.getNext());
        assertNull(third.getNext());
    }

    @Test
    void shouldUpdateTailAfterSwap() {
        CustomLinkedList list = new CustomLinkedList();

        list.addFirst("a");
        list.addFirst("b");

        // b -> a
        assertTrue(list.compareAndSwap(0));

        // a -> b
        assertEquals("a", list.getHead().getValue());
        assertEquals("b", list.getTail().getValue());

        assertEquals(
                List.of("a", "b"),
                toList(list)
        );
    }

    @Test
    void shouldCreateCorrectSnapshot() {
        CustomLinkedList list = new CustomLinkedList();

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
        CustomLinkedList list = new CustomLinkedList();

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