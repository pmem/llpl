/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Consumer;

/**
 * Implements a read and write interface for accessing a previously-allocated block of memory on 
 * a {@link com.intel.pmem.llpl.PersistentHeap}. 
 * Access through a {@code PersistentMemoryBlock} is bounds-checked to be within the block's allocated space. 
 * Using this memory block gives compile-time knowledge that all changes to persistent 
 * memory are done durably. 
 * Optionally, the "transaactional"-prefix versions of methods (e.g. {@code transactionalCopyFromArray}) 
 * can be used for stronger, transactional data consistency semantics.  
 * 
 * @since 1.0
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

    /**
     * Executes the supplied {@code Consumer}, passing in a {@link Range} object suitable for durably modifying bytes 
     * within this memory block.  
     * @param op the op to execute
     */    
    public void withRange(Consumer<Range> op) {
        withRange(0, size(), op);
    }

    /**
     * Tansactionally executes the supplied {@code Consumer}, passing in a {@link Range} object suitable for modifying bytes 
     * within this memory block.  
     * @param op the op to execute
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
