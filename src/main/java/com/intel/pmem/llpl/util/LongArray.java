/*
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.*;

public class LongArray {
    // TODO: add methods
    private static final int HEADER_SIZE = 8;
    AnyMemoryBlock arrayBlock;

    public static LongArray fromHandle(AnyHeap heap, long handle) {
        AnyMemoryBlock arrayBlock = heap.memoryBlockFromHandle(handle);
        return new LongArray(heap, arrayBlock);
    }

    public LongArray(AnyHeap heap, long size) {
        this.arrayBlock = heap.allocateMemoryBlock(HEADER_SIZE + Long.BYTES * size);
        this.arrayBlock.setLong(0, size);
    }

    private LongArray(AnyHeap heap, AnyMemoryBlock arrayBlock) {
        this.arrayBlock = arrayBlock;
    }

    public void set(long index, long value) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        arrayBlock.setLong(HEADER_SIZE + Long.BYTES * index, value);
    }

    public long get(long index) {
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
        arrayBlock.freeMemory();
    }

}

