/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

/**
 * Implements a read and write interface for accessing a {@code Heap}.
 * Access through an {@code UnboundedMemoryBlock} is bounds-checked to be within the {@code Heap} from which it was allocated
 * but not checked to be within it's allocated space in that heap. {@code UnboundedMemoryBlock}s have a smaller footprint than 
 * {@code MemoryBlock}s  
 */
public final class UnboundedMemoryBlock extends AbstractMemoryBlock {
    UnboundedMemoryBlock(Heap heap, long size, boolean transactional) {
        super(heap, size, false, transactional);
    }

    UnboundedMemoryBlock(Heap heap, long poolHandle, long offset) {
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
