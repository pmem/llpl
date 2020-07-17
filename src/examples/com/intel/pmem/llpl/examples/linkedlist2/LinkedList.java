/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.linkedlist2;

import com.intel.pmem.llpl.*;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

public class LinkedList {
    public Heap heap;
    private MemoryBlock sentinel;
    private static final long FIRST_OFFSET = 0;
    private static final long COUNT_OFFSET = 8;
    private static final long SIZE = 16;
    private Accessor accessor;

    public LinkedList(Heap heap) {
        this.heap = heap;
        sentinel = heap.allocateMemoryBlock(SIZE, false);
        this.accessor = heap.createAccessor();
    }
    
    private LinkedList(Heap heap, long handle) {
        this.heap = heap;
        this.sentinel = heap.memoryBlockFromHandle(handle);
        this.accessor = heap.createAccessor();
    }

    public static LinkedList fromHandle(Heap heap, long handle) {
        return new LinkedList(heap, handle);
    }

    public void insert(int index, long t) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        //Transaction.create(heap, ()-> {
            Node p = new Node(heap, t);
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
        //});
    }
    
    public void insertFirst(long t) {
        //Transaction.create(heap, ()-> {
            Node n = new Node(heap, t);
            Node p = first();
            if (p == null) {
                setFirst(n);
            } 
            else {
                n.setNext(p);
                setFirst(n);
            }
            incrementSize();
        //});
    }

    public long get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        Node n = findNode(index);
        if (n == null) throw new NoSuchElementException();
        else return n.getValue();
    }

    public long getFirst() { 
        Node first = first();
        if (first == null) throw new NoSuchElementException();
        return first.getValue();
    }

    public long handle() {
        return sentinel.handle();
    }

    public Iterator getIterator() {
        return new Iterator(this);
    }

    public long size() {
        return sentinel.getLong(COUNT_OFFSET);
    }

    public void remove(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        //Transaction.create(heap, ()-> {
            Node n = null;
            Node p = null;
            if (index == 0) { 
                p = first();
                setFirst(p.next());
            }
            else {
                n = findNode(index - 1);
                p = n.next();
                n.setNext(p.next());
            }
            p.free();
            decrementSize();
        //});
    }

    public void clear() {
        accessor.handle(sentinel.getLong(FIRST_OFFSET));
        if (!accessor.isValid()) return;
        long prev;
        while (accessor.isValid()) {
            prev = accessor.handle();
            Node.advance(accessor);
            accessor.freeMemory(false);
        } 
        setFirst(null);
        sentinel.setLong(COUNT_OFFSET, 0);
    }

    public void free() {
        clear();
        sentinel.free(false);
    }

    private Node findNode(int index) {
        accessor.handle(sentinel.getLong(FIRST_OFFSET));
        for(int i = 0; i < index; i++) { Node.advance(accessor); }
        if (accessor.handle() == 0) throw new RuntimeException();
        return Node.fromHandle(heap, accessor.handle());
    }

    private void setFirst(Node first) {
        long firstHandle = (first == null) ? 0 : first.handle();
        sentinel.setLong(FIRST_OFFSET, firstHandle);
    }

    private Node first() {
        long firstHandle = sentinel.getLong(FIRST_OFFSET);
        return (firstHandle == 0) ? null : Node.fromHandle(heap, firstHandle);
    }

    private void decrementSize() {
        sentinel.setLong(COUNT_OFFSET, size() - 1);
    }
    
    private void incrementSize() {
        sentinel.setLong(COUNT_OFFSET, size() + 1);
    }
    
    public static class Iterator {
        private long currentValue;
        private LinkedList l;
        private Accessor acc;
        final long size;

        public Iterator(LinkedList l) {
            this.l = l;
            size = l.size(); 
            acc = l.heap.createAccessor();
            acc.handle(l.first().handle());
        } 
    
        public boolean hasNext() {
            return acc.isValid();
        }

        public long getNext() {
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
        MemoryBlock mb; 
        private Heap heap;

        public Node(Heap heap, long value) {
            this.heap = heap;
            mb = heap.allocateMemoryBlock(NODE_SIZE, false);        
            mb.setLong(VALUE_OFFSET, value);
        }
        
        public static Node fromHandle(Heap heap, long handle) {
            return new Node(heap, heap.memoryBlockFromHandle(handle));
        }

        private Node (Heap heap, MemoryBlock mb) {
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

        public static long getValue(Accessor acc) {
            return acc.getLong(VALUE_OFFSET);
        }

        public static void advance(Accessor acc) {
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
            mb.free(false);
        }
    }
}
