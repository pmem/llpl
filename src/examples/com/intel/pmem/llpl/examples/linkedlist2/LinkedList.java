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
    public AnyHeap heap;
    private AnyAccessor sentinel;
    private long handle;
    private static final long FIRST_OFFSET = 0;
    private static final long COUNT_OFFSET = 8;
    private static final long SIZE = 16;

    public LinkedList(AnyHeap heap) {
        this.heap = heap;
        this.handle = heap.allocateMemory(SIZE);
        this.sentinel = heap.createAccessor();
        this.sentinel.handle(handle);
    }
    
    private LinkedList(AnyHeap heap, long handle) {
        this.heap = heap;
        this.handle = handle;
        this.sentinel = heap.createAccessor();
        this.sentinel.handle(handle);
    }

    public static LinkedList fromHandle(AnyHeap heap, long handle) {
        return new LinkedList(heap, handle);
    }

    public void insert(int index, long t) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        heap.execute(() -> {
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
        });
    }
    
    public void insertFirst(long t) {
        heap.execute(() -> {
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
        });
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
        heap.execute(() -> {
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
        });
    }

    public void clear() {
        sentinel.handle(sentinel.getLong(FIRST_OFFSET));
        if (!sentinel.isValid()) return;
        long prev;
        while (sentinel.isValid()) {
            prev = sentinel.handle();
            Node.advance(sentinel);
            sentinel.freeMemory();
        } 
        setFirst(null);
        sentinel.handle(handle);
        sentinel.setLong(COUNT_OFFSET, 0);
    }

    public void free() {
        clear();
        sentinel.freeMemory();
    }

    private Node findNode(int index) {
        sentinel.handle(sentinel.getLong(FIRST_OFFSET));
        for(int i = 0; i < index; i++) { Node.advance(sentinel); }
        if (sentinel.handle() == 0) throw new RuntimeException();
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

    private void decrementSize() {
        sentinel.setLong(COUNT_OFFSET, size() - 1);
    }
    
    private void incrementSize() {
        sentinel.setLong(COUNT_OFFSET, size() + 1);
    }
    
    public static class Iterator implements java.util.Iterator {
        private long currentValue;
        private LinkedList l;
        private AnyAccessor acc;
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

        public Long next() {
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
            mb = heap.allocateMemoryBlock(NODE_SIZE);        
            mb.setLong(VALUE_OFFSET, value);
        }
        
        public static Node fromHandle(AnyHeap heap, long handle) {
            return new Node(heap, heap.memoryBlockFromHandle(handle));
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
