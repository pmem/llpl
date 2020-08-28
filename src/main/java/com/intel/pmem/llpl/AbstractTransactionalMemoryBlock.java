/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.Consumer;

abstract class AbstractTransactionalMemoryBlock extends AnyMemoryBlock { 
    private static final long METADATA_SIZE = 8;

    AbstractTransactionalMemoryBlock(TransactionalHeap heap, long size, boolean bounded) {
        super(heap, size, bounded, true);
    }

    AbstractTransactionalMemoryBlock(TransactionalHeap heap, long poolHandle, long offset, boolean bounded) {
        super(heap, offset, bounded);
    }

    @Override
    public TransactionalHeap heap() {
        return (TransactionalHeap)super.heapInternal();
    }

    @Override
    abstract long metadataSize();

    /**
    * Deallocates this memory block.
    * @throws HeapException if the memory block could not be deallocated
    */
    public void free() {
        heap().freeMemoryBlock(this, true);
    }

    @Override
    public void freeMemory() {
        free();
    }

    @Override
    public void setByte(long offset, byte value) {
        super.transactionalSetByte(offset, value);
    }

    @Override
    public void setShort(long offset, short value) {
        super.transactionalSetShort(offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        super.transactionalSetInt(offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        super.transactionalSetLong(offset, value);
    }

    @Override
    public void copyFrom(MemoryAccessor srcAccessor, long srcOffset, long dstOffset, long length) {
        super.transactionalCopy(srcAccessor, srcOffset, dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcIndex, long dstOffset, int length) {
        super.transactionalCopyFromArray(srcArray, srcIndex, dstOffset, length);
    }

    @Override
    public void setMemory(byte value, long offset, long length) {
        super.transactionalSetMemory(value, offset, length);
    }

    @Override
    public <T> T withRange(long startOffset, long rangeLength, Function<Range, T> op) {
        return super.transactionalWithRange(startOffset, rangeLength, op);
    }

    @Override
    public void withRange(long startOffset, long rangeLength, Consumer<Range> op) {
        super.transactionalWithRange(startOffset, rangeLength, (Range r) -> {op.accept(r); return (Void)null;});
    }
}
