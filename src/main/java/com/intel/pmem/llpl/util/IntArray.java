/*
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.*;

public class IntArray {
    // TODO: add methods
    private static final int HEADER_SIZE = 8;
    AnyMemoryBlock arrayBlock;

    public static IntArray fromHandle(AnyHeap heap, long handle) {
        AnyMemoryBlock arrayBlock = heap.memoryBlockFromHandle(handle);
        return new IntArray(heap, arrayBlock);
    }

    public IntArray(AnyHeap heap, long size) {
        this.arrayBlock = heap.allocateMemoryBlock(HEADER_SIZE + Integer.BYTES * size);
        this.arrayBlock.setLong(0, size);
    }

    private IntArray(AnyHeap heap, AnyMemoryBlock arrayBlock) {
        this.arrayBlock = arrayBlock;
    }

    public void set(long index, int value) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        arrayBlock.setInt(HEADER_SIZE + Integer.BYTES * index, value);
    }

    public int get(long index) {
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
        // boolean transactionalFree = false;
        arrayBlock.freeMemory();
    }

}

