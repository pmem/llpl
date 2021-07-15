/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;

public class SimpleLeaf extends Leaf {
    protected static final long SIZE = Node.HEADER_SIZE + 8L;
    private static final long VALUE_OFFSET = Node.HEADER_SIZE;

    SimpleLeaf(TransactionalHeap heap) {
        super(heap, SIZE);
        initType(Node.SIMPLE_LEAF_TYPE);
    }

    SimpleLeaf(TransactionalHeap heap, long value) {
        this(heap, new byte[]{0}, 0, 0, value);
    }

    SimpleLeaf(TransactionalHeap heap, byte[] prefix, int start, int length, long value) {
        super(heap, heap.allocateCompactMemoryBlock(SIZE, (Range range) -> {
            range.setByte(Node.NODE_TYPE_OFFSET, Node.SIMPLE_LEAF_TYPE);
            if (length > 0) {
                range.setInt(Node.PREFIX_LENGTH_OFFSET, length);
                range.copyFromArray(prefix, start, Node.COMPRESSED_PATH_OFFSET, length);
            }
            range.setLong(VALUE_OFFSET, value);
        }));
    }

    static Node create(TransactionalHeap heap, byte[] prefix, int start, int length, long value) {
        if (length > Node.MAX_PREFIX_LENGTH) {
            return Leaf.prependNodes(heap, prefix, start, length, value);
        }
        else return new SimpleLeaf(heap, prefix, start, length, value);
    }

    SimpleLeaf(TransactionalHeap heap, TransactionalCompactMemoryBlock mb) {
        super(heap, mb);
    }

    long getValue() {
        return mb.getLong(VALUE_OFFSET);
    }

    @Override
    void setValue(long value) {
        mb.setLong(VALUE_OFFSET, value);
    }
}
