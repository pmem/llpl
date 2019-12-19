/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Consumer;

/**
 * Implements a read and write interface for transactional access to a {@link com.intel.pmem.llpl.TransactionalHeap}.
 * Access through a {@code TransactionalMemoryBlock} is bounds-checked to be within the block's allocated space. 
 * Using this memory block gives compile-time knowledge that all changes to persistent 
 * memory are done transactionally. 
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

    void checkBounds(long offset, long length) {
        super.checkBounds(offset, length);
    }

    /**
     * Tansactionally executes the supplied {@code Consumer} function, passing in a {@code Range} object suitable for modifying bytes 
     * within this memory block.  
     * @param body the function to execute
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public void withRange(Consumer<Range> body) {
        withRange(0, size(), body);
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
