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

abstract class AbstractMemoryBlock extends AnyMemoryBlock { 
    AbstractMemoryBlock(Heap heap, long size, boolean bounded, boolean transactional) {
        super(heap, size, bounded, transactional);
    }

    AbstractMemoryBlock(Heap heap, long poolHandle, long offset, boolean bounded) {
        super(heap, offset, bounded);
    }

    @Override
    public Heap heap() {
        return (Heap)super.heapInternal();
    }

    @Override
    abstract long metadataSize();

    @Override
    public void setByte(long offset, byte value) {
        super.rawSetByte(offset, value);
    }

    @Override
    public void setShort(long offset, short value) {
        super.rawSetShort(offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        super.rawSetInt(offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        super.rawSetLong(offset, value);
    }

    @Override
    public void copyFrom(MemoryAccessor srcAccessor, long srcOffset, long dstOffset, long length) {
        super.rawCopy(srcAccessor, srcOffset, dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcIndex, long dstOffset, int length) {
        super.rawCopyFromArray(srcArray, srcIndex, dstOffset, length);
    }

    @Override
    public void setMemory(byte value, long offset, long length) {
        super.rawSetMemory(value, offset, length);
    }

    public <T> T withRange(long startOffset, long rangeLength, Function<Range, T> op) {
        return super.rawWithRange(startOffset, rangeLength, op);
    }

    public void withRange(long startOffset, long rangeLength, Consumer<Range> op) {
        super.rawWithRange(startOffset, rangeLength, (Range r) -> {op.accept(r); return (Void)null;});
    }

    /**
    * Ensures that the supplied range of bytes within this memory block are written to persistent memory media.
    * @param offset the location from which to flush bytes
    * @param length the number of bytes to flush
    * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds 
    * @throws IllegalStateException if the memory block is not in a valid state for use
    */
     public void flush(long offset, long length) {
        super.flush(offset, length);
    }

    /**
    * Adds the specified range of of bytes within this memory block to the current transaction.
    * Any modifications to this range of bytes will be committed on successful completion of the current
    * transaction or rolled-back on abort of the current transaction
    * @param offset the start of the range of bytes to add
    * @param length the number of bytes to add
    * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds 
    * @throws IllegalStateException if the memory block is not in a valid state for use
    */
    public void addToTransaction(long offset, long length) {
        super.addToTransaction(offset, length);
    }

    /**
    * Deallocates this memory block.
    * @param transactional if true, the deallocation operation will be done transactionally
    * @throws HeapException if the memory block could not be deallocated
    */
    public void free(boolean transactional) {
        heap().freeMemoryBlock(this, transactional);
    }

    @Override
    public void freeMemory() {
        heap().freeMemoryBlock(this, false);
    }
}
