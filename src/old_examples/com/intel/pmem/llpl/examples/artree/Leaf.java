/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;

public abstract class Leaf extends Node {
    Leaf(TransactionalHeap heap, TransactionalCompactMemoryBlock mb) {
        super(heap, mb);
    }

    Leaf(TransactionalHeap heap, long size) {
        super(heap, size);
    }

    abstract void setValue(long value);
    abstract long getValue();

    @Override
    boolean isLeaf() {
        return true;
    }
    
    static Node prependNodes(TransactionalHeap heap, byte[] key, int start, int length, long value) {
        int curStart = key.length - 8;
        int curLength = length;

        Node child = new SimpleLeaf(heap, key, curStart, Node.MAX_PREFIX_LENGTH, value); 
        curLength -= (Node.MAX_PREFIX_LENGTH + 1);
        curStart -= (Node.MAX_PREFIX_LENGTH + 1);

        while (curLength > Node.MAX_PREFIX_LENGTH) {
            InternalNode parent = new Node4(heap, key, curStart, Node.MAX_PREFIX_LENGTH, child, key[curStart + Node.MAX_PREFIX_LENGTH]);
            child = parent;
            curStart -= (Node.MAX_PREFIX_LENGTH + 1);
            curLength -= (Node.MAX_PREFIX_LENGTH + 1);
        }

        if (curLength >= 0) {
            InternalNode parent = new Node4(heap, key, start, curLength, child, key[curStart + Node.MAX_PREFIX_LENGTH]);
            child = parent;
        }
        return child;
    }
}
