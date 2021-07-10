/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.AnyAccessor;
import com.intel.pmem.llpl.AnyHeap;
import com.intel.pmem.llpl.AnyMemoryBlock;
import com.intel.pmem.llpl.HeapException;
import java.lang.IllegalStateException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class implements a singly-linked list containing {@code long} values.
 * The list can be created using different heap types.
 * Given a persistent heap, the list will store values durably, and given
 * a transactional heap, it will store values transactionally.<br><br>
 * <b>This implementation is not thread-safe.</b> If multiple threads access a 
 * list, and one or more of them modifies the list, then it must be synchronized externally. 
 * Iterators returned by {@link LongLinkedList#iterator} will throw {@link java.util.ConcurrentModificationException}
 * if the list is structurally modified during iteration.
 * @since 1.2
 */

public class LongLinkedList {
    private AnyHeap heap;
    private AnyAccessor sentinel;
    private long handle;
    private static final long FIRST_OFFSET = 0;
    private static final long COUNT_OFFSET = 8;
    private static final long SIZE = 16;

    /**
     * Creates a list on the specified heap.
     * The semantics of this method depend on the type of {@code heap}.
     * Given a persistent heap, the list will store values durably, and given
     * a transactional heap will store values transactionally.
     * @param heap the heap on which to allocate the list
     * @throws HeapException if the list could not be created
     */
    public LongLinkedList(AnyHeap heap) {
        this.heap = heap;
        this.handle = heap.allocateCompactMemory(SIZE);
        this.sentinel = heap.createCompactAccessor();
        this.sentinel.handle(handle);
    }

    private LongLinkedList(AnyHeap heap, long handle) {
        this.heap = heap;
        this.handle = handle;
        this.sentinel = heap.createCompactAccessor();
        this.sentinel.handle(handle);
    }

    /**
     * Returns a previously created list that is associated with the specified
     * handle. The handle must be that of a {@code LongLinkedList} created on
     * the specified heap.
     * @param handle the handle of a previously-created list
     * @param heap the heap from which to retrieve the list
     * @return the list
     * @throws HeapException if the list could not be reaccessed
     */
    public static LongLinkedList fromHandle(AnyHeap heap, long handle) {
        return new LongLinkedList(heap, handle);
    }

    /**
     * Inserts {@code value} into the list at {@code index}.
     * Shifts all elements, with indices greater than or equal to {@code index}, 
     * one position to the right.
     * The semantics of this method depend on the heap supplied when constructed.
     * @param index the index at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access
     * of data outside of the list's bounds
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public void add(long index, long value) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException();
        }
        if (!sentinel.isValid()) throw new IllegalStateException();
        heap.execute(() -> {
            Node p = new Node(heap, value);
            if (index == 0) {
                p.setNext(first());
                setFirst(p);
            }
            else {
                Node n = findNode(index - 1);
                Node q = n.next();
                p.setNext(q);
                n.setNext(p);
            }
            incrementSize();
        });
    }

    /**
     * Inserts {@code value} as the first element in the list.
     * Shifts all elements one position to the right.
     * The semantics of this method depend on the heap supplied when constructed.
     * @param value the value to store
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public void addFirst(long value) {
        if (!sentinel.isValid()) throw new IllegalStateException();
        heap.execute(() -> {
            Node n = new Node(heap, value);
            Node p = first();
            if (p == null) {
                setFirst(n);
            }
            else {
                n.setNext(p);
                setFirst(n);
            }
            incrementSize();
        });
    }

    /**
     * Retrieves the value at {@code index}.
     * @param index the index at which to retrieve the value
     * @return the value stored at {@code index}
     * @throws IndexOutOfBoundsException if the operation would cause access of
     * data outside of the list's bounds
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public long get(long index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        Node n = findNode(index);
        return n.getValue();
    }

    /**
     * Retrieves the first element in the list.
     * @return the first element
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     * @throws NoSuchElementException if the list is empty
     */
    public long getFirst() {
        Node first = first();
        if (first == null) throw new NoSuchElementException();
        return first.getValue();
    }

    /**
     * Returns a handle to this list. This stable value can be stored and used
     * later to regain access to the list.
     * @return a handle to this list
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public long handle() {
        if (!sentinel.isValid()) throw new IllegalStateException();
        return sentinel.handle();
    }

    /**
     * Returns an {@code Iterator} to this list.
     * @return an {@code Iterator} to this list
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public Iterator<Long> iterator() {
        return new ListIterator(this);
    }

    /**
     * Stores {@code value} at {@code index} and returns the existing value.
     * The semantics of this method depend on the heap supplied when constructed.
     * @param index the index at which to store the value
     * @param value the value to store
     * @return the existing value at index
     * @throws IndexOutOfBoundsException if the operation would cause access of
     * data outside of the list's bounds
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public long set(long index, long value) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        Node n = findNode(index);
        long retValue = n.getValue();
        n.setValue(value);
        return retValue;
    }

    /**
     * Returns the number of elements in this list.
     * @return the number of elements in this list
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public long size() {
        return sentinel.getLong(COUNT_OFFSET);
    }

    /**
     * Removes and returns the value at {@code index}.
     * @param index the index from which to remove the value
     * @return the value stored at index
     * @throws IndexOutOfBoundsException if the operation would cause access of
     * data outside of the list's bounds
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public long remove(long index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        long data = heap.execute(() -> {
            Node n = null;
            Node p = null;
            long retValue = 0;
            if (index == 0) {
                p = first();
                setFirst(p.next());
            }
            else {
                n = findNode(index - 1);
                p = n.next();
                n.setNext(p.next());
            }
            retValue = p.getValue();
            p.free();
            decrementSize();
            return retValue;
        });
        return data;
    }

    /**
     * Removes and returns the first element in this list.
     * @return the first element
     * @throws NoSuchElementException if the list is empty
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public long removeFirst() {
        if (size() == 0) throw new NoSuchElementException();
        return remove(0);
    }

    /**
     * Removes all of the elements in this list.
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public void clear() {
        if (!sentinel.isValid()) throw new IllegalStateException();
        long firstHandle = sentinel.getLong(FIRST_OFFSET);
        if (firstHandle == 0) return;
        sentinel.handle(firstHandle);
        long next;
        while (sentinel.isValid()) {
            next = Node.nextHandle(sentinel);
            sentinel.freeMemory();
            if (next > 0) sentinel.handle(next);
            else sentinel.resetHandle();
        }
        sentinel.handle(handle);
        sentinel.setLong(FIRST_OFFSET, 0);
        sentinel.setLong(COUNT_OFFSET, 0);
    }

    /**
     * Clears the list then deallocates the memory referenced by this list.
     * The semantics of this method depend on the heap supplied when constructed.
     * @throws HeapException if the list could not be freed
     * @throws IllegalStateException if {@link LongLinkedList#free} has been called on this object
     */
    public void free() {
        clear();
        sentinel.freeMemory();
    }

    /**
     * Compares this list to the specified object.  The result is true if
     * and only if the argument is not null and is a {@code LongLinkedList} whose handle is
     * equal to the handle of this list.
     * @return true if the given object is equal
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LongLinkedList)) return false;
        AnyMemoryBlock thisBlock = heap.compactMemoryBlockFromHandle(handle);
        LongLinkedList otherList = (LongLinkedList)obj;
        AnyMemoryBlock otherBlock = otherList.heap.compactMemoryBlockFromHandle(otherList.handle());
        return otherBlock.equals(thisBlock);
    }

    /**
     * Returns a hash code for this list.  Note that this hash code is not computed based on the
     * elements in this list and is only stable for the lifetime of the Java process.
     * @return a hash code for this list
     */
    @Override
    public int hashCode() {
        return heap.compactMemoryBlockFromHandle(handle).hashCode();
    }

    private Node findNode(long index) {
        sentinel.handle(sentinel.getLong(FIRST_OFFSET));
        for(int i = 0; i < index; i++) { Node.advance(sentinel); }
        long val = sentinel.handle();
        sentinel.handle(handle);
        return Node.fromHandle(heap, val);
    }

    private void setFirst(Node first) {
        long firstHandle = (first == null) ? 0 : first.handle();
        sentinel.setLong(FIRST_OFFSET, firstHandle);
    }

    private Node first() {
        long firstHandle = sentinel.getLong(FIRST_OFFSET);
        return (firstHandle == 0) ? null : Node.fromHandle(heap, firstHandle);
    }

    private long firstHandle() {
        return sentinel.getLong(FIRST_OFFSET);
    }

    private void decrementSize() {
        sentinel.setLong(COUNT_OFFSET, size() - 1);
    }

    private void incrementSize() {
        sentinel.setLong(COUNT_OFFSET, size() + 1);
    }

    static class ListIterator implements Iterator<Long> {
        private long currentValue;
        private LongLinkedList l;
        private AnyAccessor acc;
        final long size;

        public ListIterator(LongLinkedList l) {
            this.l = l;
            size = l.size();
            acc = l.heap.createCompactAccessor();
            long handle;
            if ((handle = l.firstHandle()) != 0) acc.handle(handle);
        }

        public boolean hasNext() {
            return acc.isValid();
        }

        public Long next() {
            if (l.size() == 0) {
                throw new NoSuchElementException();
            }
            if (l.size() != size) {
                throw new ConcurrentModificationException();
            }
            currentValue = Node.getValue(acc);
            Node.advance(acc);
            return currentValue;
        }
    }

    static class Node {
        private static final long VALUE_OFFSET = 0;
        private static final long NEXT_OFFSET = 8;
        private static final int NODE_SIZE = 16;
        AnyMemoryBlock mb;
        private AnyHeap heap;

        public Node(AnyHeap heap, long value) {
            this.heap = heap;
            mb = heap.allocateCompactMemoryBlock(NODE_SIZE);
            mb.setLong(VALUE_OFFSET, value);
        }

        public static Node fromHandle(AnyHeap heap, long handle) {
            return new Node(heap, heap.compactMemoryBlockFromHandle(handle));
        }

        private Node (AnyHeap heap, AnyMemoryBlock mb) {
            this.mb = mb;
            this.heap = heap;
        }

        public long getValue() {
            return mb.getLong(VALUE_OFFSET);
        }

        public Node next() {
            long nextHandle = mb.getLong(NEXT_OFFSET);
            if (nextHandle != 0)
                return Node.fromHandle(heap, nextHandle);
            else
                return null;
        }

        public static long getValue(AnyAccessor acc) {
            return acc.getLong(VALUE_OFFSET);
        }

        static long nextHandle(AnyAccessor acc) {
            return acc.getLong(NEXT_OFFSET);
        }

        public static void advance(AnyAccessor acc) {
            long nextHandle = acc.getLong(NEXT_OFFSET);
            if (nextHandle != 0) acc.handle(nextHandle);
            else acc.resetHandle();
        }

        public void setValue(long value) {
            mb.setLong(VALUE_OFFSET, value);
        }

        public void setNext(Node next) {
            long nextHandle = (next == null) ? 0 : next.handle();
            mb.setLong(NEXT_OFFSET, nextHandle);
        }

        public long handle() {
            return mb.handle();
        }

        public void free() {
            mb.freeMemory();
        }
    }
}
