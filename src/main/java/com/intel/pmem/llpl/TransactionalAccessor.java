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
 * Implements a read and write interface for accessing a previously-allocated block of memory 
 * on a {@link com.intel.pmem.llpl.TransactionalHeap}. 
 * A {@code TransactionalAccessor} can be repositioned to refer to other previously-allocated blocks of memory. 
 * Access through a {@code TransactionalAccessor} is bounds-checked to be within the block of 
 * memory referenced by the accessor's current {@code handle} value.
 * Using this accessor gives compile-time knowledge that all changes to persistent 
 * memory are done transactionally.   
 * 
 * @since 1.1  
 */

public final class TransactionalAccessor extends AnyAccessor {

    static final long METADATA_SIZE = 8;

    TransactionalAccessor(TransactionalHeap heap) {
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
    public void copyFrom(MemoryAccessor src, long srcOffset, long dstOffset, long length) {
        super.transactionalCopy(src, srcOffset, dstOffset, length);
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
        super.checkBounds(offset, length);
    }

    @Override
    long metadataSize() { 
        return METADATA_SIZE; 
    }

    /**
     * Returns the allocated size, in bytes, of the memory referenced by this accessor.  
     * @return the allocated size, in bytes, of the memory referenced by this accessor 
     */
    @Override
    public long size() { 
        return super.size(); 
    }

    /**
     * Sets this accessor's handle thereby changing the memory that this accessor references.
     * The supplied handle must be associated with a non-compact live allocation on this accessor's heap.   
     * @param handle The handle to use
     * @throws IllegalArgumentException if {@code handle} is not valid
     * @throws HeapException if the accessor could not be updated
     */
    @Override
    public void handle(long handle) {
        heap().checkBounds(handle, METADATA_SIZE);
        super.handle(handle, true);
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

    /**
     * Tansactionally executes the supplied {@code Consumer}, passing in a {@link Range} object suitable for modifying bytes 
     * within this accessor's memory.  
     * Writing using the Range object in ranged operations such as this can be more efficient than
     * calling separate write methods on the accessor. 
     * @param op the op to execute
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
     public void withRange(Consumer<Range> op) {
         withRange(0, size(), op); 
    }
}
