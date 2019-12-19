/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Consumer;

/**
 * Implements a read and write interface for accessing a {@link com.intel.pmem.llpl.Heap}. Access through a 
 * {@code MemoryBlock} is bounds-checked to be within the block's allocated space.
 *  
 * @see com.intel.pmem.llpl.AnyMemoryBlock   
 */
public final class MemoryBlock extends AbstractMemoryBlock {
    static final long METADATA_SIZE = 8;

    MemoryBlock(Heap heap, long size, boolean transactional) {
        super(heap, size, true, transactional);
    }

    MemoryBlock(Heap heap, long poolHandle, long offset) {
        super(heap, poolHandle, offset, true);
    }

    /**
     * Checks that the range of bytes from {@code offset} (inclusive) to {@code offset} + length (exclusive) 
     * is within the bounds of this memory block. 
     * @param offset The start if the range to check
     * @param length The number of bytes in the range to check
     * @throws IndexOutOfBoundsException if the range is not within this memory block's bounds
     */
    void checkBounds(long offset, long length) {
        super.checkBounds(offset, length);
    }

    @Override
    long metadataSize() { 
        return METADATA_SIZE; 
    }

    /**
    * Adds this memory block's range of bytes to the current transaction.
    * Any modifications to this range of bytes will be committed upon successful completion of the current
    * transaction or rolled-back on an abort of the current transaction
    * @throws IllegalStateException if the memory block is not in a valid state for use
    */
    @Override
    public void addToTransaction() {
        super.addToTransaction(0, size());
    }

    /**
     * Returns the allocated size, in bytes, of this memory block.  
     * @return the allocated size, in bytes, of this memory block
     */
    @Override
    public long size() { 
        return super.size(); 
    }

    /**
    * Ensures that any modifications made to this memory block are written to persistent memory media.
    */
    @Override
    public void flush() {
        flush(0, size());
    }
}

