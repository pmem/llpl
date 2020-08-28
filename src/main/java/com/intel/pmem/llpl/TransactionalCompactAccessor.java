/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implements a read and write interface for accessing a previously-allocated compact block of memory on 
 * a {@link com.intel.pmem.llpl.TransactionalHeap}. A {@code TransactionalCompactAccessor} can be repositioned 
 * to refer to other previously-allocated compact blocks of memory. 
 * Access through an instance of this class is bounds-checked to be within the 
 * heap from which it was allocated but not checked to be within the associated allocated 
 * space in that heap. 
 * Using this accessor gives compile-time knowledge that all changes to persistent 
 * memory are done transactionally.   
 * 
 * @since 1.1
 *  
 * @see com.intel.pmem.llpl.AnyAccessor   
 */

public final class TransactionalCompactAccessor extends AnyAccessor {

    static final long METADATA_SIZE = 0;

    TransactionalCompactAccessor(TransactionalHeap heap) {
        super(heap);
    }

    @Override
    public TransactionalHeap heap() {
        return (TransactionalHeap)super.heapInternal();
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
    public void copyFrom(MemoryAccessor srcBlock, long srcOffset, long dstOffset, long length) {
        super.transactionalCopy(srcBlock, srcOffset, dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        super.transactionalCopyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    @Override
    public void setMemory(byte value, long offset, long length) {
        super.transactionalSetMemory(value, offset, length);
    }

    @Override
    public void freeMemory() {
        checkValid();
        heap().freeMemory(directAddress(), true);
        super.reset();
    }

    void checkBounds(long offset, long length) {
        if (offset < 0 || heap().outOfBounds(offset + length + uncheckedGetHandle())) throw new IndexOutOfBoundsException(MemoryAccessor.outOfBoundsMessage(offset, length));
    }

    void checkBoundsAndLength(long offset, long length) {
        if (offset < 0 || length <= 0 || heap().outOfBounds(offset + length + uncheckedGetHandle())) throw new IndexOutOfBoundsException(MemoryAccessor.outOfBoundsMessage(offset, length));
    }

    @Override
    long metadataSize() { 
        return METADATA_SIZE; 
    }

    /**
     * Sets this accessor's handle thereby changing the memory that this accessor references.
     * The supplied handle must be associated with a compact live allocation on this accessor's heap.   
     * @param handle The handle to use
     * @throws IllegalArgumentException if {@code handle} is not valid
     * @throws HeapException if the accessor could not be updated
     */
    @Override
    public void handle(long handle) {
        heap().checkBounds(handle, METADATA_SIZE);
        super.handle(handle, false);
    }

    @Override
    public void resetHandle() {
        super.reset();
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
