/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Consumer;

/**
 * Implements a read and write interface for accessing a previously-allocated block of 
 * memory on a {@link com.intel.pmem.llpl.Heap}. Access through a 
 * {@code MemoryBlock} is bounds-checked to be within the block's allocated space.
 * 
 * @since 1.0
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
    public void addToTransaction() {
        super.addToTransaction(0, size());
    }

    /**
     * Returns the allocated size, in bytes, of the memory of this memory block.  
     * @return the allocated size, in bytes, of the memory of this memory block 
     */
    public long size() { 
        return super.size(); 
    }

    /**
    * Ensures that any modifications made to this memory block are written to persistent memory media.
    */
    public void flush() {
        flush(0, size());
    }

    /**
     * Executes the supplied {@code Consumer}, passing in a {@link Range} object
     * suitable for modifying bytes within this memory block.
     * @param op the op to execute

     * @since 1.1
     */    

    public void withRange(Consumer<Range> op) {
        withRange(0, size(), op);
    }

    Range range() {
        return range(0, size()); 
    }
}

