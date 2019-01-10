/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

import java.util.function.Consumer;

/**
 * Implements a bounded read and write interface for access to a {@code PersistentHeap}. 
 * Access through a {@code TransactionalMemoryBlock} is bounds-checked to be within the blocks allocated space. 
 * Using this memory block gives compile-time knowledge that all changes to persistent 
 * memory are done durably. Allocations and other modifications to persistent memory 
 * may optionally be done transactionally.  
 */
public final class PersistentMemoryBlock extends AbstractPersistentMemoryBlock {
    private static final long METADATA_SIZE = 8;

    PersistentMemoryBlock(PersistentHeap heap, long size, boolean transactional) {
        super(heap, size, true, transactional);
    }

    PersistentMemoryBlock(PersistentHeap heap, long poolHandle, long offset) {
        super(heap, poolHandle, offset, true);
    }

    @Override
    long baseOffset() { 
        return METADATA_SIZE; 
    }

    /**
     * Checks that the range of bytes from {@code offset} (inclusive) to {@code offset} + length (exclusive) 
     * is within the bounds of this memory block. 
     * @param offset The start if the range to check
     * @param length The number of bytes in the range to check
     * @throws IndexOutOfBoundsException if the range is not within this memory block's bounds
     */
    public void checkBounds(long offset, long length) {
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
     * Tansactionally executes the suppied {@code Consumer} function, passing in a {@code Range} object suitable for modifying bytes 
     * within this memory block.  
     * @param op the function to execute
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

