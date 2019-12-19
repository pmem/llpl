/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;
import java.nio.ByteBuffer;
import java.util.function.*;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Optional;

public class ARTree {
    final TransactionalHeap heap;
    private Root root;
    private int maxKeyLen;

    public ARTree(TransactionalHeap heap) {
        this.heap = heap;   
        this.root = new Root(heap);
    }

    @SuppressWarnings("unchecked")
    public ARTree(TransactionalHeap heap, long handle) {
        this.heap = heap;
        this.root = (Root)Node.rebuild(heap, handle);
    }

    public long handle() {
        return root.handle();
    }

    public void apply(byte[] radixKey, long value) {
        if (radixKey.length > maxKeyLen) maxKeyLen = radixKey.length;
        Transaction.create(heap, () -> {
            insert(root, root.getChild(), radixKey, value, 0, 0);
        });
    }

    @SuppressWarnings("unchecked")
    private void insert(Node parent, Node node, byte[] key, long value, int depth, int replaceIndex) {
        if (node == null) {    // empty tree
            long leafValue = value;

            Root rt = (Root)parent;    // if tree is empty, parent is guaranteed to be root
            Node leaf = SimpleLeaf.create(this.heap, key, 0, key.length, leafValue);
            rt.addChild(leaf);
            return;
        }

        byte[] newPrefix = new byte[8];
        byte[] prefix = node.getPrefix();

        if (node.isLeaf()) {
            int matchedLength = node.checkPrefix(key, depth);
            if (matchedLength == node.getPrefixLength() && matchedLength + depth == key.length) {
                //replacement
                ((Leaf)node).setValue(value);
                return;
            }
            InternalNode newNode;
            int i = 0;
            for (; i < (key.length-depth) && i < prefix.length && key[i+depth] == prefix[i]; i++) {
                newPrefix[i] = key[i+depth];
            }
            depth += i;
            node.updatePrefix(prefix, i + 1, node.getPrefixLength() - i - 1);
            int prefixLength = key.length - depth - 1;
            Node newChild = SimpleLeaf.create(this.heap, key, depth + 1, prefixLength, value);
            if (depth == key.length) newNode = new Node4(this.heap, newPrefix, i, true, newChild, (byte)0, node, prefix[i]);
            else if (i == prefix.length) newNode = new Node4(this.heap, newPrefix, i, true, (Leaf)node, (byte)0, newChild, key[depth]);
            else newNode = new Node4(this.heap, newPrefix, i, false, newChild, key[depth], node, prefix[i]);
            if (parent == root) { ((Root)parent).addChild(newNode); }
            else ((InternalNode)parent).putChildAtIndex(replaceIndex, newNode);
            return;
        }

        InternalNode intNode = (InternalNode)node;
        int matchedLength = intNode.checkPrefix(key, depth);
        if (matchedLength != intNode.getPrefixLength()) {
            InternalNode newNode;
            int i = 0;
            for (; i < matchedLength; i++) {
                newPrefix[i] = prefix[i];
            }
            intNode.updatePrefix(prefix, i + 1, intNode.getPrefixLength() - i - 1);
            int prefixLength = key.length - depth - i - 1;
            Node newChild = SimpleLeaf.create(this.heap, key, depth + i + 1, prefixLength, value);
            if (depth + i == key.length) newNode = new Node4(this.heap, newPrefix, matchedLength, true, newChild, (byte)0, node, prefix[i]);
            else newNode = new Node4(this.heap, newPrefix, matchedLength, false, newChild, key[depth + i], node, prefix[i]);
            if (parent == root) { ((Root)parent).addChild(newNode); }
            else ((InternalNode)parent).putChildAtIndex(replaceIndex, newNode);
            return;
        }

        depth += intNode.getPrefixLength();
        if (depth == key.length) {
            //this insertion will be a blankradix child to this internal node
            if (intNode.hasBlankRadixChild()) {
                Leaf child = intNode.findBlankRadixChild();
                child.setValue(value);
            }
            else {
                SimpleLeaf leaf = new SimpleLeaf(this.heap, value);
                if (!intNode.addBlankRadixChild(leaf)) {
                    InternalNode newNode = intNode.grow(leaf, Optional.empty());
                    ((InternalNode)parent).putChildAtIndex(replaceIndex, newNode);
                    intNode.free();
                }
            }
            // no need to update prefix for a blank radix child - it has no prefix
            return;
        }
        //descending find next node with matching radix
        int childIndex = intNode.findChildIndex(key[depth]);
        Node next = intNode.getChildAtIndex(childIndex);
        if (next != null) {
            insert(node, next, key, value, depth + 1, childIndex);
        } 
        else {
            // found insertion point. insert leaf
            int prefixLength = key.length - depth - 1;
            Node newChild = SimpleLeaf.create(this.heap, key, depth + 1, prefixLength, value);
            if (!intNode.addChild(key[depth], newChild)) {
                InternalNode newNode = intNode.grow(newChild, Optional.of(key[depth]));
                if (parent == root) { ((Root)parent).addChild(newNode); }
                else ((InternalNode)parent).putChildAtIndex(replaceIndex, newNode);
                intNode.free();
            }
        }
    }

    public long get(byte[] radixKey) {
        Node node;
        if ((node = root.getChild()) != null) {
                return search(root.getChild(), radixKey, 0, null);
        }
        return 0;
    }

    @FunctionalInterface
    public interface SearchHelper {
        void apply(InternalNode parent, Node child, Byte radix);
    }

    @SuppressWarnings("unchecked")
    private long search(Node node, byte[] key, int depth, SearchHelper helper) {
        if (node == null) {
            return 0;
        }
        Node next;
        int matchedLength = node.checkPrefix(key, depth);
        if (matchedLength != node.getPrefixLength()) {
            return 0;
        }
        if (node.isLeaf())
            return ((SimpleLeaf)node).getValue();
        else {
            InternalNode intNode = (InternalNode)node;
            depth += matchedLength;
            boolean blank = (depth == key.length);
            next = blank ? intNode.findBlankRadixChild() : intNode.findChild(key[depth]);
            long l = search(next, key, depth + 1, helper);
            if (helper != null) {
                helper.apply(intNode, next, (blank ? null : key[Math.min(depth,key.length-1)]));
            }
            return l;
        }
    }

    public void print() {
        if (root.getChild() != null)
            root.getChild().print(0);
        System.out.println("");
    }

    public void clear() {
        root.destroy();
    }

    void deleteNodes(Node parent, Node child, Byte radix) {
        Transaction.create(heap, ()-> {
            if (child.isLeaf()) {
                heap.memoryBlockFromHandle(((SimpleLeaf)child).getValue()).free();
                child.free();
                ((InternalNode)parent).deleteChild(radix);
            }
            else if (((InternalNode)child).getChildrenCount() == 0) {
                child.free();
                ((InternalNode)parent).deleteChild(radix);
            }
        });
    }

    public void delete(byte[] key) {
        search(root.getChild(), key, 0, this::deleteNodes);
    }

    public EntryIterator getEntryIterator() {
        return new EntryIterator();
    }

    class StackItem {
        NodeEntry[] entries;
        int index = 0;
        int prefixLen = 0;
        private final boolean hasBlank;

        public StackItem(NodeEntry[] entries, int prefixLen, boolean hasBlank) {
            this.entries = entries;
            this.prefixLen = prefixLen;
            this.hasBlank = hasBlank;
        }

        public int prefixLen() {
            return prefixLen;
        }

        public NodeEntry[] entries() {
            return entries;
        }

        public void saveIndex(int idx) {
            index = idx;
        }

        public int getIndex() {
            return index;
        }

        public boolean hasBlank() {
            return hasBlank;
        }

        public int length() {
            return entries.length;
        }

        public NodeEntry entryAt(int index) {
            return entries[index];
        }
    }

    public class Entry {
        byte[] key;
        long value;

        public Entry(byte[] key, long value) {
            this.key = key;
            this.value = value;
        }

        public byte[] getKey() {
            return key;
        }

        public long getValue() {
            return value;
        }
    }

    public class EntryIterator {
        StackItem cursor;
        int index;
        byte[] lastKey = null;
        ByteBuffer keyBuf;
        Deque<StackItem> cache;
        Entry prev;
        Entry next;

        public EntryIterator() {
            cache = new ArrayDeque<>();
            Node first = root.getChild();
            if (first != null)
            {
                if (first.isLeaf()) {
                    SimpleLeaf leaf = (SimpleLeaf)first;
                    next = new Entry(first.getPrefix(), leaf.getValue());
                }
                else {
                    keyBuf = ByteBuffer.allocate(100);
                    iterate(first);
                    cursor = cache.getFirst();
                    next();
                }
            }
        }

        public boolean hasNext() {
            return (next != null && !Arrays.equals(lastKey, next.getKey()));
        }

        public ARTree.Entry next() {
            prev = next;
            if (cursor == null) {
                next = null;
                return prev;
            }
            while (index >= cursor.length()) {
                if (cache.size() == 0) {
                    next = null;
                    return prev;
                }
                keyBuf.reset().position(Math.max(0, keyBuf.position() - (1 + cursor.prefixLen()))).mark();
                cache.pop();
                cursor = cache.peekFirst();
                if (cursor == null) {
                    next = null;
                    return prev;
                }
                index = cursor.getIndex() + 1;
                keyBuf.reset();
            }
            if (!cursor.entryAt(index).child.isLeaf()) {
                cursor.saveIndex(index);
                NodeEntry ne = cursor.entryAt(index);
                if (!cursor.hasBlank() || (index != 0)) keyBuf.put(ne.radix);
                iterate(cursor.entryAt(index).child);
                cursor = cache.getFirst();
                index=0;
            }
            NodeEntry ne = cursor.entryAt(index++);
            SimpleLeaf leaf = (SimpleLeaf)ne.child;
            keyBuf.mark();
            if (!cursor.hasBlank() || (index != 1)) keyBuf.put(ne.radix);
            if (leaf.getPrefixLength() > 0) keyBuf.put(leaf.getPrefix());
            next = new Entry(Arrays.copyOf(keyBuf.array(),keyBuf.position()),leaf.getValue());
            keyBuf.reset();
            return prev;
        }

        void iterateChildren(InternalNode current) {
            NodeEntry[] entries = current.getEntries();
            boolean blank;
            cache.push(new StackItem(entries, current.getPrefixLength(), blank = current.hasBlankRadixChild()));
            if (current.getPrefixLength() > 0) keyBuf.put(current.getPrefix());
            if (!blank && !entries[0].child.isLeaf()) keyBuf.put(entries[0].radix);
            iterate(entries[0].child);
        }

        void iterate(Node current) {
            if (!current.isLeaf()) {
                iterateChildren((InternalNode)current);
            } 
            else {
                return;
            }
        }
    }
}
