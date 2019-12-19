/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

/**
 * Implements a read and write interface for transactional access to a {@link com.intel.pmem.llpl.TransactionalHeap}.
 * Access through a {@code TransactionalCompactMemoryBlock} is bounds-checked to be within the 
 * {@code TransactionalHeap} from which it was allocated but not checked to be within its allocated 
 * space in that heap. {@code TransactionalCompactMemoryBlock}s have a smaller footprint than 
 * {@code TransactionalMemoryBlock}s.  
 * Using this memory block gives compile-time knowledge that all changes to persistent 
 * memory are done transactionally. 
 *  
 * @see com.intel.pmem.llpl.AnyMemoryBlock   
 */
public final class TransactionalCompactMemoryBlock extends AbstractTransactionalMemoryBlock {
    TransactionalCompactMemoryBlock(TransactionalHeap heap, long size) {
        super(heap, size, false);
    }

    TransactionalCompactMemoryBlock(TransactionalHeap heap, long poolHandle, long offset) {
        super(heap, poolHandle, offset, false);
    }

    @Override
    long metadataSize() { 
        return 0; 
    }

    @Override
    void checkBounds(long offset, long length) {
        if (offset < 0 || heap().outOfBounds(offset + length + handle())) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(offset, length));
    }

    @Override
    void checkBoundsAndLength(long offset, long length) {
        if (offset < 0 || length <= 0 || heap().outOfBounds(offset + length + handle())) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(offset, length));
    }
}
