/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.Consumer;

abstract class AbstractTransactionalMemoryBlock extends AnyMemoryBlock { 
    private static final long METADATA_SIZE = 8;

    AbstractTransactionalMemoryBlock(TransactionalHeap heap, long size, boolean bounded) {
        super(heap, size, bounded, true);
    }

    AbstractTransactionalMemoryBlock(TransactionalHeap heap, long poolHandle, long offset, boolean bounded) {
        super(heap, offset, true);
    }

    /**
    * Returns the heap from which this memory block was allocated.
    * @return the {@code TransactionalHeap} from which this memory block was allocated
    */
    public TransactionalHeap heap() {
        return (TransactionalHeap)super.heap();
    }

    @Override
    abstract long baseOffset();

    /**
    * Deallocates this memory block.
    */
    public void free() {
        heap().freeMemoryBlock(this);
    }

    @Override
    public void setByte(long offset, byte value) {
        super.transactionalSetByte(offset, value);
    }

    @Override
    public void setShort(long offset, short value) {
        super.transactionalSetShort(offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        super.transactionalSetInt(offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        super.transactionalSetLong(offset, value);
    }

    @Override
    public void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        super.transactionalCopyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        super.transactionalCopyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    @Override
    public void setMemory(byte val, long offset, long length) {
        super.transactionalSetMemory(val, offset, length);
    }

    /**
     * Transactionally executes the suppied {@code Function}, passing in a {@code Range} object suitable for modifying bytes in 
     * the specified range of offsets within this memory block.  
     * @param startOffset the ranges startOffset
     * @param length the number of bytes in the range
     * @param op the function to execute
     * @param <T> the return type of the supplied fuction
     * @return the object returned from the supplied function
     */    
    public <T> T withRange(long startOffset, long length, Function<Range, T> op) {
        return super.transactionalWithRange(startOffset, length, op);
    }

    /**
     * Transactionally executes the suppied {@code Consumer} function, passing in a {@code Range} object suitable for modifying bytes in 
     * the specified range of offsets within this memory block.  
     * @param startOffset the ranges startOffset
     * @param length the number of bytes in the range
     * @param op the function to execute
     */    
    public void withRange(long startOffset, long length, Consumer<Range> op) {
        super.transactionalWithRange(startOffset, length, op);
    }
}

