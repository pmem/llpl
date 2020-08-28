/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

/**
 * Implements a read and write interface for accessing a previously-allocated compact 
 * block of memory on a {@link com.intel.pmem.llpl.PersistentHeap}.
 * Access through a {@code PersistentCompactMemoryBlock} is bounds-checked to be within the {@code PersistentHeap} from which it was allocated
 * but not checked to be within its allocated space in that heap. {@code PersistentCompactMemoryBlock}s have a smaller footprint than 
 * {@code PersistentMemoryBlock}s.  
 * Using this memory block gives compile-time knowledge that all changes to persistent 
 * memory are done durably. 
 * Optionally, the "transacational"-prefix versions of methods (e.g. {@code transactionalCopyFromArray}) 
 * can be used for stronger, transactional data consistency semantics.  
 * 
 * @since 1.0
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

    @Override
    void checkBounds(long offset, long length) {
        if (offset < 0 || heap().outOfBounds(offset + length + uncheckedGetHandle())) throw new IndexOutOfBoundsException(MemoryAccessor.outOfBoundsMessage(offset, length));
    }

    @Override
    void checkBoundsAndLength(long offset, long length) {
        if (offset < 0 || length <= 0 || heap().outOfBounds(offset + length + uncheckedGetHandle())) throw new IndexOutOfBoundsException(MemoryAccessor.outOfBoundsMessage(offset, length));
    }
}
