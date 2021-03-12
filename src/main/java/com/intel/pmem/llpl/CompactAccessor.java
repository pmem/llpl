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
 * Implements a read and write interface for accessing a previously-allocated compact 
 * block of memory on a {@link com.intel.pmem.llpl.Heap}. 
 * A {@code CompactAccessor} can be repositioned to refer to other previously-allocated compact blocks of memory. 
 * Access through an instance of this class is bounds-checked to be within the 
 * heap from which it was allocated but not checked to be within the associated allocated 
 * space in that heap. 
 * 
 * @since 1.1
 * 
 * @see com.intel.pmem.llpl.AnyAccessor   
 */

public final class CompactAccessor extends AnyAccessor {

    static final long METADATA_SIZE = 0;

    CompactAccessor(Heap heap) {
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
    * Deallocates the memory this accessor references.
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

    @Override
    public long size() {
        throw new UnsupportedOperationException("Size method is not supported for compact accessors");
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
}
