/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Consumer;

/**
 * Implements a read and write interface for accessing a previously-allocated block of memory
 * on a {@link com.intel.pmem.llpl.TransactionalHeap}.
 * Access through a {@code TransactionalMemoryBlock} is bounds-checked to be within the block's allocated space. 
 * Using this memory block gives compile-time knowledge that all changes to persistent memory are done transactionally. 
 * 
 * @since 1.0
 *  
 * @see com.intel.pmem.llpl.AnyMemoryBlock   
 */
public final class TransactionalMemoryBlock extends AbstractTransactionalMemoryBlock {
    static final long METADATA_SIZE = 8;

    TransactionalMemoryBlock(TransactionalHeap heap, long size) {
        super(heap, size, true);
    }

    TransactionalMemoryBlock(TransactionalHeap heap, long poolHandle, long offset) {
        super(heap, poolHandle, offset, true);
    }

    @Override
    long metadataSize() { 
        return METADATA_SIZE; 
    }

    /**
     * Tansactionally executes the supplied {@code Consumer}, passing in a {@link Range} object suitable for modifying bytes 
     * within this memory block.  
     * @param op the op to execute
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public void withRange(Consumer<Range> op) {
        withRange(0, size(), op);
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
