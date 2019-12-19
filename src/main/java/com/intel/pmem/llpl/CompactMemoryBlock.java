/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

/**
 * Implements a read and write interface for accessing a {@link com.intel.pmem.llpl.Heap}.
 * Access through an {@code CompactMemoryBlock} is bounds-checked to be within the {@code Heap} from which it was allocated
 * but not checked to be within its allocated space in that heap. {@code CompactMemoryBlock}s have a smaller footprint than 
 * {@code MemoryBlock}s.  
 *  
 * @see com.intel.pmem.llpl.AnyMemoryBlock   
 */
public final class CompactMemoryBlock extends AbstractMemoryBlock {
    CompactMemoryBlock(Heap heap, long size, boolean transactional) {
        super(heap, size, false, transactional);
    }

    CompactMemoryBlock(Heap heap, long poolHandle, long offset) {
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
