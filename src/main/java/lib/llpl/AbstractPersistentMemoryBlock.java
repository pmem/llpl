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

abstract class AbstractPersistentMemoryBlock extends AnyMemoryBlock { 
    private static final long METADATA_SIZE = 8;

    AbstractPersistentMemoryBlock(PersistentHeap heap, long size, boolean bounded, boolean transactional) {
        super(heap, size, bounded, true);
    }

    AbstractPersistentMemoryBlock(PersistentHeap heap, long poolHandle, long offset, boolean bounded) {
        super(heap, offset, true);
    }

    /**
    * Returns the heap from which this memory block was allocated.
    * @return the {@code PersistentHeap} from which this memory block was allocated
    */
    public PersistentHeap heap() {
        return (PersistentHeap)super.heap();
    }

    @Override
    abstract long baseOffset();

    /**
    * Deallocates this memory block.
    * @param transactional whether to make the deallocation operation transactional
    */
    public void free(boolean transactional) {
        heap().freeMemoryBlock(this, transactional);
    }

    @Override
    public void setByte(long offset, byte value) {
        super.durableSetByte(offset, value);
    }

    @Override
    public void setShort(long offset, short value) {
        super.durableSetShort(offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        super.durableSetInt(offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        super.durableSetLong(offset, value);
    }

    @Override
    public void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        super.durableCopyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        super.durableCopyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    @Override
    public void setMemory(byte val, long offset, long length) {
        super.durableSetMemory(val, offset, length);
    }

    /**
     * Executes the suppied {@code Function} passing in a {@code Range} object suitable for durably modifying bytes in 
     * the specified range of offsets within this memory block.  
     * @param startOffset the ranges startOffset
     * @param length the number of bytes in the range
     * @param op the function to execute
     * @param <T> the return type of the supplied fuction
     * @return the object returned from the supplied function
     */    
    public <T> T withRange(long startOffset, long length, Function<Range, T> op) {
        return super.durableWithRange(startOffset, length, op);
    }

    /**
     * Executes the suppied {@code Consumer} function passing in a {@code Range} object suitable for durably modifying bytes in 
     * the specified range of offsets within this memory block.  
     * @param startOffset the ranges startOffset
     * @param length the number of bytes in the range
     * @param op the function to execute
     */    
    public void withRange(long startOffset, long length, Consumer<Range> op) {
        super.durableWithRange(startOffset, length, op);
    }

    /**
     * Transactionally stores the supplied {@code byte} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     */
     public void transactionalSetByte(long offset, byte value) {
        super.transactionalSetByte(offset, value);
    }

    /**
     * Transactionally stores the supplied {@code short} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     */
    public void transactionalSetShort(long offset, short value) {
        super.transactionalSetShort(offset, value);
    }

    /**
     * Transactionally stores the supplied {@code int} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     */
     public void transactionalSetInt(long offset, int value) {
        super.transactionalSetInt(offset, value);
    }

    /**
     * Transactionally stores the supplied {@code long} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     */
    public void transactionalSetLong(long offset, long value) {
        super.transactionalSetLong(offset, value);
    }

    /**
     * Transactionally copies {@code length} bytes from the supplied source memory block, starting at {@code srcOffset}, to  
     * this memory block starting at {@code dstOffset}.  
     * @param srcBlock the memory block to copy bytes from
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     */
    public void transactionalCopyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        super.transactionalCopyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
    }

    /**
     * Transactionally copies {@code length} bytes from the supplied byte array, starting at {@code srcOffset}, to  
     * this memory block starting at {@code dstOffset}.  
     * @param srcArray the memory block to copy bytes from
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     */
    public void transactionalCopyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        super.transactionalCopyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    /**
     * Transactionally sets {@code length} bytes in this memory block, starting at {@code offset}, to the supplied {@code byte}  
     * value.  
     * @param value the value to set
     * @param offset the starting offset in this memory block
     * @param length the number of bytes to set
     */    
    public void transactionalSetMemory(byte value, long offset, long length) {
        super.transactionalSetMemory(value, offset, length);
    }    

    /**
     * Transactionally executes the suppied {@code Function}, passing in a {@code Range} object suitable for modifying bytes in 
     * the specified range of offsets within this memory block.  
     * @param startOffset the ranges startOffset
     * @param length the number of bytes in the range
     * @param op the function to execute
     * @return the object returned from the supplied function
     */    
    public <T> T transactionalWithRange(long startOffset, long length, Function<Range, T> op) {
        return super.transactionalWithRange(startOffset, length, op);
    }

    /**
     * Transactionally executes the suppied {@code Consumer}, function passing in a {@code Range} object suitable for modifying bytes in 
     * the specified range of offsets within this memory block.  
     * @param startOffset the ranges startOffset
     * @param length the number of bytes in the range
     * @param op the function to execute
     */    
    public void transactionalWithRange(long startOffset, long length, Consumer<Range> op) {
        super.transactionalWithRange(startOffset, length, op);
    }
}
