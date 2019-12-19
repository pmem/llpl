/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Consumer;

/**
 * Implements a bounded read and write interface for access to a {@link com.intel.pmem.llpl.PersistentHeap}. 
 * Access through a {@code PersistentMemoryBlock} is bounds-checked to be within the block's allocated space. 
 * Using this memory block gives compile-time knowledge that all changes to persistent 
 * memory are done durably. Allocations and other modifications to persistent memory 
 * may optionally be done transactionally.  
 *  
 * @see com.intel.pmem.llpl.AnyMemoryBlock   
 */
public final class PersistentMemoryBlock extends AbstractPersistentMemoryBlock {
    static final long METADATA_SIZE = 8;

    PersistentMemoryBlock(PersistentHeap heap, long size, boolean transactional) {
        super(heap, size, true, transactional);
    }

    PersistentMemoryBlock(PersistentHeap heap, long poolHandle, long offset) {
        super(heap, poolHandle, offset, true);
    }

    @Override
    long metadataSize() { 
        return METADATA_SIZE; 
    }

    void checkBounds(long offset, long length) {
        super.checkBounds(offset, length);
    }

    /**
     * Executes the suppied {@code Consumer}, passing in a {@code Range} object suitable for durably modifying bytes 
     * within this memory block.  
     * @param op the function to execute
     */    
    public void withRange(Consumer<Range> op) {
        withRange(0, size(), op);
    }

    /**
     * Tansactionally executes the suppied {@code Consumer}, passing in a {@code Range} object suitable for modifying bytes 
     * within this memory block.  
     * @param op the function to execute
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public void transactionalWithRange(Consumer<Range> op) {
        transactionalWithRange(0, size(), op);
    }

    /**
     * Returns the allocated size, in bytes, of this memory block.  
     * @return the allocated size, in bytes, of this memory block
     */
    @Override
    public long size() { 
        return super.size(); 
    }

    Range range() {
        return range(0, size()); 
    }
}
