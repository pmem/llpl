/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

/**
 * Implements a read and write interface for accessing a {@link com.intel.pmem.llpl.PersistentHeap}.
 * Access through a {@code PersistentCompactMemoryBlock} is bounds-checked to be within the {@code PersistentHeap} from which it was allocated
 * but not checked to be within its allocated space in that heap. {@code PersistentCompactMemoryBlock}s have a smaller footprint than 
 * {@code PersistentMemoryBlock}s.  
 * Using this memory block gives compile-time knowledge that all changes to persistent 
 * memory are done durably. Allocations and other modifications to persistent memory 
 * may optionally be done transactionally. 
 *  
 * @see com.intel.pmem.llpl.AnyMemoryBlock   
 */
public final class PersistentCompactMemoryBlock extends AbstractPersistentMemoryBlock {
    PersistentCompactMemoryBlock(PersistentHeap heap, long size, boolean transactional) {
        super(heap, size, false, transactional);
    }

    PersistentCompactMemoryBlock(PersistentHeap heap, long poolHandle, long offset) {
        super(heap, poolHandle, offset, false);
    }

    @Override
    long metadataSize() { 
        return 0; 
    }

    /**
     * Checks that the range of bytes from {@code offset} (inclusive) to {@code offset} + length (exclusive) 
     * is within the bounds of this memory block's heap. 
     * @param offset The start if the range to check
     * @param length The number of bytes in the range to check
     * @throws IndexOutOfBoundsException if the range is not within this memory block's heap bounds
     */
    @Override
    void checkBounds(long offset, long length) {
        if (offset < 0 || heap().outOfBounds(offset + length + handle())) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(offset, length));
    }

    @Override
    void checkBoundsAndLength(long offset, long length) {
        if (offset < 0 || length <= 0 || heap().outOfBounds(offset + length + handle())) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(offset, length));
    }
}
