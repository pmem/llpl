/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.linkedlist;

import com.intel.pmem.llpl.*;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

public class LinkedList {
    private TransactionalHeap heap;
    private TransactionalMemoryBlock sentinel;
    private static final long FIRST_OFFSET = 0;
    private static final long COUNT_OFFSET = 8;
    private static final long SIZE = 16;

    public LinkedList(TransactionalHeap heap) {
        this.heap = heap;
        sentinel = heap.allocateMemoryBlock(SIZE);
    }
    
    private LinkedList(TransactionalHeap heap, long handle) {
        this.heap = heap;
        this.sentinel = heap.memoryBlockFromHandle(handle);
    }

    public static LinkedList fromHandle(TransactionalHeap heap, long handle) {
        return new LinkedList(heap, handle);
    }

    public void insert(int index, long t) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        Transaction.create(heap, ()-> {
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
        Transaction.create(heap, ()-> {
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
        Transaction.create(heap, ()-> {
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
        Node n = first();
        if (n == null) return;
        Node curr;
        while ((curr = n.next()) != null) {
            n.free();
            n = curr;
        }
        n.free();
        setFirst(null);
        sentinel.setLong(COUNT_OFFSET, 0);
    }

    public void free() {
        clear();
        sentinel.free();
    }

    private Node findNode(int index) {
        Node curr = null;
        int count = 0;
        Iterator it = getIterator();
        while (it.hasNext()) {
            curr = it.nextNode();
            if (count == index) break;
            count++; 
        }
        return curr;
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
        private Node cursor;
        private Node next;
        private LinkedList l;
        final long size;
        boolean hasNext = false;

        public Iterator(LinkedList l) {
            this.l = l;
            next = l.first();
            size = l.size(); 
        } 
    
        public boolean hasNext() {
            return next != null;
        }

        public long getNext() {
            if (l.size() != size) {
                throw new ConcurrentModificationException();
            }
            cursor = next;
            next = cursor.next();
            return cursor.getValue();
        }

        Node nextNode() {
            if (l.size() != size) {
                throw new ConcurrentModificationException();
            }
            cursor = next;
            next = cursor.next();
            return cursor;
        }   
    }

    static class Node {
        private static final long VALUE_OFFSET = 0;
        private static final long NEXT_OFFSET = 8;
        private static final int NODE_SIZE = 16;
        private TransactionalMemoryBlock mb; 
        private TransactionalHeap heap;

        public Node(TransactionalHeap heap, long value) {
            this.heap = heap;
            mb = heap.allocateMemoryBlock(NODE_SIZE);        
            mb.setLong(VALUE_OFFSET, value);
        }
        
        public static Node fromHandle(TransactionalHeap heap, long handle) {
            return new Node(heap, heap.memoryBlockFromHandle(handle));
        }

        private Node (TransactionalHeap heap, TransactionalMemoryBlock mb) {
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
            mb.free();
        }
    }
}
