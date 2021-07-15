/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.intarray;

import com.intel.pmem.llpl.*;

public class IntArray {
    private static final int HEADER_SIZE = 8;
    TransactionalMemoryBlock arrayBlock;

    public static IntArray fromHandle(TransactionalHeap heap, long handle) {
        TransactionalMemoryBlock arrayBlock = heap.memoryBlockFromHandle(handle);
        return new IntArray(heap, arrayBlock);
    }

    public IntArray(TransactionalHeap heap, long size) {
        this.arrayBlock = heap.allocateMemoryBlock(HEADER_SIZE + Integer.BYTES * size);
        this.arrayBlock.setLong(0, size);
    }

    private IntArray(TransactionalHeap heap, TransactionalMemoryBlock arrayBlock) {
        this.arrayBlock = arrayBlock;
    }

    public void set(int index, int value) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        arrayBlock.setInt(HEADER_SIZE + Integer.BYTES * index, value);
    }

    public int get(int index) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return arrayBlock.getInt(HEADER_SIZE + Integer.BYTES * index);
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
