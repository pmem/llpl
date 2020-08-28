/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Function;
import java.util.function.Consumer;

/**
 * Implements a read and write interface for accessing a previously-allocated block of memory on a {@link com.intel.pmem.llpl.Heap}. 
 * An {@code Accessor} can be repositioned to refer to other previously-allocated blocks of memory. Access through an 
 * {@code Accessor} is bounds-checked to be within the block of memory referenced by the accessor's current {@code handle} value.
 *  
 * @since 1.1
 */

public final class Accessor extends AnyAccessor {

    static final long METADATA_SIZE = 8;

    Accessor(Heap heap) {
        super(heap);
    }

    @Override
    public Heap heap() {
        return (Heap)super.heapInternal();
    }

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
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        super.rawCopyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    @Override
    public void setMemory(byte value, long offset, long length) {
        super.rawSetMemory(value, offset, length);
    }

    /**
    * Ensures that the supplied range of bytes within this accessor's memory are written to persistent memory media.
    * @param offset the location from which to flush bytes
    * @param length the number of bytes to flush
    * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor bounds 
    * @throws IllegalStateException if the accessor is not in a valid state for use
    */
     public void flush(long offset, long length) {
        super.flush(offset, length);
    }

    /**
    * Ensures that the bytes referred to by this accessor are written to persistent memory media.
    */
    public void flush() {
        super.flush(0, size());
    }

    /**
    * Adds the specified range of of bytes within this accessor's memory to the current transaction.
    * Any modifications to this range of bytes will be committed on successful completion of the current
    * transaction or rolled-back on abort of the current transaction
    * @param offset the start of the range of bytes to add
    * @param length the number of bytes to add
    * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor bounds 
    * @throws IllegalStateException if the accessor is not in a valid state for use
    */
    public void addToTransaction(long offset, long length) {
        super.addToTransaction(offset, length);
    }

    @Override   
    public <T> T withRange(long startOffset, long rangeLength, Function<Range, T> op) {
        return super.rawWithRange(startOffset, rangeLength, op);
    }

    @Override
    public void withRange(long startOffset, long rangeLength, Consumer<Range> op) {
        super.rawWithRange(startOffset, rangeLength, (Range r) -> {op.accept(r); return (Void)null;});
    }

    /**
     * Executes the supplied {@code Consumer}, passing in a {@link Range} object suitable for modifying bytes 
     * within this accessor's memory.  
     * @param op the op to execute
     */    
    public void withRange(Consumer<Range> op) {
        withRange(0, size(), op);
    }

    /**
    * Deallocates the memory that this accessor references.
    * @param transactional if true, the deallocation operation will be done transactionally
    * @throws HeapException if the memory could not be deallocated
    */
    public void freeMemory(boolean transactional) {
        checkValid();
        heap().freeMemory(directAddress(), transactional);
        super.reset();
    }

    @Override
    public void freeMemory() {
        freeMemory(false);
    }

    @Override
    long metadataSize() { 
        return METADATA_SIZE; 
    }

    /**
    * Adds this accessor's range of bytes to the current transaction.
    * Any modifications to this range of bytes will be committed upon successful completion of the current
    * transaction or rolled-back on an abort of the current transaction
    * @throws IllegalStateException if the accessor is not in a valid state for use
    */
    public void addToTransaction() {
        super.addToTransaction(0, size());
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
}
