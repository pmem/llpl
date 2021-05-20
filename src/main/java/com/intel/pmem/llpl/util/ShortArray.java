/*
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.*;

public class ShortArray {
    // TODO: add methods
    private static final int HEADER_SIZE = 8;
    AnyMemoryBlock arrayBlock;

    public static ShortArray fromHandle(AnyHeap heap, long handle) {
        AnyMemoryBlock arrayBlock = heap.memoryBlockFromHandle(handle);
        return new ShortArray(heap, arrayBlock);
    }

    public ShortArray(AnyHeap heap, long size) {
        this.arrayBlock = heap.allocateMemoryBlock(HEADER_SIZE + Short.BYTES * size);
        this.arrayBlock.setLong(0, size);
    }

    private ShortArray(AnyHeap heap, AnyMemoryBlock arrayBlock) {
        this.arrayBlock = arrayBlock;
    }

    public void set(long index, short value) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        arrayBlock.setShort(HEADER_SIZE + Short.BYTES * index, value);
    }

    public short get(long index) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return arrayBlock.getShort(HEADER_SIZE + Short.BYTES * index);
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

