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

abstract class AbstractMemoryBlock extends AnyMemoryBlock { 

    AbstractMemoryBlock(Heap heap, long size, boolean bounded, boolean transactional) {
        super(heap, size, bounded, transactional);
    }

    AbstractMemoryBlock(Heap heap, long poolHandle, long offset, boolean bounded) {
        super(heap, offset, bounded);
    }

    /**
    * Returns the heap from which this memory block was allocated.
    * @return the {@code Heap} from which this memory block was allocated
    */
    public Heap heap() {
        return (Heap)super.heap();
    }

    @Override
    abstract long baseOffset();

    @Override
    public void setByte(long offset, byte value) {
        super.rawSetByte(offset, value);
    }

     @Override
    public void setShort(long offset, short value) {
        super.rawSetShort(offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        super.rawSetInt(offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        super.rawSetLong(offset, value);
    }

    @Override
    public void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        super.rawCopyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        super.rawCopyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    @Override
    public void setMemory(byte val, long offset, long length) {
        super.rawSetMemory(val, offset, length);
    }

    // //@Override
    // public <T> T withRange(long startOffset, long length, Function<Range, T> op) {
    //     Range range = range(startOffset, length);
    //     T result = op.apply(range);
    //     return result;
    // }

    // //@Override
    // public void withRange(long startOffset, long length, Consumer<Range> op) {
    //     Range range = range(startOffset, length);
    //     op.accept(range);
    // }

    /**
    * Ensures that any modifications made within the supplied range within this memory 
    * block are written to persistent memory media.
    */
     public void flush(long offset, long length) {
        super.flush(offset, length);
    }

    /**
    * Adds the specified range of of bytes within this memory block to the current transaction.
    * Any modifications to this range of bytes will be committed on successful completion of the current
    * transaction or rolled-back on abort of the current transaction
    */
    public void addToTransaction(long offset, long length) {
        super.addToTransaction(offset, length);
    }

    /**
    * Deallocates this memory block.
    * @param transactional whether to make the deallocation operation transactional
    */
    public void free(boolean transactional) {
        heap().freeMemoryBlock(this, transactional);
    }
}
