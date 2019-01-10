/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

/**
 * Implements read and write interface for accessing a {@link lib.llpl.PersistentHeap}.
 * Access through a {@code PersistentUnboundedMemoryBlock} is bounds-checked to be within the {@code PersistentHeap} from which it was allocated
 * but not checked to be within it's allocated space in that heap. {@code PersistentUnboundedMemoryBlock}s have a smaller footprint than 
 * {@code PersistentMemoryBlock}s  
 * Using this memory block gives compile-time knowledge that all changes to persistent 
 * memory are done durably. Allocations and other modifications to persistent memory 
 * may optionally be done transactionally. */
public final class PersistentUnboundedMemoryBlock extends AbstractPersistentMemoryBlock {
    PersistentUnboundedMemoryBlock(PersistentHeap heap, long size, boolean transactional) {
        super(heap, size, false, transactional);
    }

    PersistentUnboundedMemoryBlock(PersistentHeap heap, long poolHandle, long offset) {
        super(heap, poolHandle, offset, false);
    }

    @Override
    long baseOffset() { 
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
    public void checkBounds(long offset, long length) {
        if (offset < 0 || offset + length > heap().size()) throw new IndexOutOfBoundsException();
    }
}
