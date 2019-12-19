/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.handlearray;

import com.intel.pmem.llpl.*;

public class HandleArray {
    private static final int HEADER_SIZE = 8;
    TransactionalMemoryBlock arrayBlock;

    public static HandleArray fromHandle(TransactionalHeap heap, long handle) {
        TransactionalMemoryBlock arrayBlock = heap.memoryBlockFromHandle(handle);
        return new HandleArray(heap, arrayBlock);
    }

    public HandleArray(TransactionalHeap heap, long size) {
        this.arrayBlock = heap.allocateMemoryBlock(HEADER_SIZE + Long.BYTES * size);
        this.arrayBlock.setLong(0, size);
    }

    private HandleArray(TransactionalHeap heap, TransactionalMemoryBlock arrayBlock) {
        this.arrayBlock = arrayBlock;
    }

    public void set(int index, long handle) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        arrayBlock.setLong(HEADER_SIZE + Long.BYTES * index, handle);
    }

    public long get(int index) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return arrayBlock.getLong(HEADER_SIZE + Long.BYTES * index);
    }

    public long size() {
        return arrayBlock.getLong(0);
    }

    public long handle() {
        return arrayBlock.handle();
    }

    public void free() {
        arrayBlock.free();
    }
}
