/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;

public final class Root extends Node {
    private static final long SIZE = Node.HEADER_SIZE + 8;  // 8 byte pointer to first node in tree
    private static final long CHILD_OFFSET = Node.HEADER_SIZE;

    Root(TransactionalHeap heap) {
        super(heap, SIZE);
        initType(Node.ROOT_TYPE);
    }

    Root(TransactionalHeap heap, TransactionalCompactMemoryBlock mb) {
        super(heap, mb);
    }

    boolean addChild(Node node) {
        mb.setLong(CHILD_OFFSET, node.handle());
        return true;
    }

    Node getChild() {
        return Node.rebuild(heap, mb.getLong(CHILD_OFFSET));
    }

    boolean isLeaf() { return false; }

    void destroy() {
        Node child = getChild();
        if (child != null) {
            child.free();
            mb.setLong(CHILD_OFFSET, 0L);
        }
    }
}
