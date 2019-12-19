/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.Consumer;

abstract class AbstractPersistentMemoryBlock extends AnyMemoryBlock { 
    private static final long METADATA_SIZE = 8;

    AbstractPersistentMemoryBlock(PersistentHeap heap, long size, boolean bounded, boolean transactional) {
        super(heap, size, bounded, transactional);
    }

    AbstractPersistentMemoryBlock(PersistentHeap heap, long poolHandle, long offset, boolean bounded) {
        super(heap, offset, bounded);
    }

    /**
    * Returns the heap from which this memory block was allocated.
    * @return the {@code PersistentHeap} from which this memory block was allocated
    */
    public PersistentHeap heap() {
        return (PersistentHeap)super.heap();
    }

    @Override
    abstract long metadataSize();

    /**
    * Deallocates this memory block.
    * @param transactional if true, the deallocation operation will be done transactionally
    * @throws HeapException if the memory block could not be deallocated
    */
    public void free(boolean transactional) {
        heap().freeMemoryBlock(this, transactional);
    }

    /**
     * {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void setByte(long offset, byte value) {
        super.durableSetByte(offset, value);
    }

    /**
     * {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void setShort(long offset, short value) {
        super.durableSetShort(offset, value);
    }

    /**
     * {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void setInt(long offset, int value) {
        super.durableSetInt(offset, value);
    }

    /**
     * {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void setLong(long offset, long value) {
        super.durableSetLong(offset, value);
    }

    /**
     * {@inheritDoc}  
     * @param srcBlock {@inheritDoc}
     * @param srcOffset {@inheritDoc}
     * @param dstOffset {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc} 
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        super.durableCopyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
    }

    /**
     * {@inheritDoc} 
     * @param srcArray {@inheritDoc}
     * @param srcOffset {@inheritDoc}
     * @param dstOffset {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        super.durableCopyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    /**
     *{@inheritDoc}
     * @param value {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc} 
     * @throws IllegalStateException {@inheritDoc}
     */    
    @Override
    public void setMemory(byte value, long offset, long length) {
        super.durableSetMemory(value, offset, length);
    }

    /**
     * Executes the supplied {@code Function} passing in a {@code Range} object suitable for durably modifying bytes in 
     * the specified range of offsets within this memory block.  
     * @param startOffset the starting offset of the range
     * @param rangeLength the number of bytes in the range
     * @param op the function to execute
     * @param <T> the return type of the supplied fuction
     * @return the object returned from the supplied function
     * @throws IndexOutOfBoundsException if the the specified range of bytes is not within this memory block's bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public <T> T withRange(long startOffset, long rangeLength, Function<Range, T> op) {
        return super.durableWithRange(startOffset, rangeLength, op);
    }

    /**
     * Executes the supplied {@code Consumer} function passing in a {@code Range} object suitable for durably modifying bytes in 
     * the specified range of offsets within this memory block.  
     * @param startOffset the starting offset of the range
     * @param rangeLength the number of bytes in the range
     * @param op the function to execute
     * @throws IndexOutOfBoundsException if the the specified range of bytes is not within this memory block's bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public void withRange(long startOffset, long rangeLength, Consumer<Range> op) {
        super.durableWithRange(startOffset, rangeLength, op);
    }

    /**
     * Transactionally stores the supplied {@code byte} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the the specified range of bytes is not within this memory block's bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
     public void transactionalSetByte(long offset, byte value) {
        super.transactionalSetByte(offset, value);
    }

    /**
     * Transactionally stores the supplied {@code short} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
    public void transactionalSetShort(long offset, short value) {
        super.transactionalSetShort(offset, value);
    }

    /**
     * Transactionally stores the supplied {@code int} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
     public void transactionalSetInt(long offset, int value) {
        super.transactionalSetInt(offset, value);
    }

    /**
     * Transactionally stores the supplied {@code long} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
    public void transactionalSetLong(long offset, long value) {
        super.transactionalSetLong(offset, value);
    }

    /**
     * Transactionally copies {@code length} bytes from the {@code srcBlock} memory block, starting at {@code srcOffset}, to  
     * this memory block starting at {@code dstOffset}.  
     * @param srcBlock the memory block from which to copy bytes
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
    public void transactionalCopyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        super.transactionalCopyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
    }

    /**
     * Transactionally copies {@code length} bytes from the {@code srcArray} byte array, starting at {@code srcOffset}, to  
     * this memory block starting at {@code dstOffset}.  
     * @param srcArray the memory block from which to copy bytes
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
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
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public void transactionalSetMemory(byte value, long offset, long length) {
        super.transactionalSetMemory(value, offset, length);
    }    

    /**
     * Transactionally executes the suppied {@code Function}, passing in a {@code Range} object suitable for modifying bytes in 
     * the specified bytes within this memory block.  
     * @param startOffset the starting offset of the range
     * @param rangeLength the number of bytes in the range
     * @param op the function to execute
     * @return the object returned from the supplied {@code Function}
     * @throws IndexOutOfBoundsException if the the specified range of bytes is not within this memory block's bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public <T> T transactionalWithRange(long startOffset, long rangeLength, Function<Range, T> op) {
        return super.transactionalWithRange(startOffset, rangeLength, op);
    }

    /**
     * Transactionally executes the suppied {@code Consumer}, passing in a {@code Range} object suitable for modifying bytes in 
     * the specified bytes within this memory block.  
     * @param startOffset the starting offset of the range
     * @param rangeLength the number of bytes in the range
     * @param op the function to execute
     * @throws IndexOutOfBoundsException if the the specified range of bytes is not within this memory block's bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public void transactionalWithRange(long startOffset, long rangeLength, Consumer<Range> op) {
        super.transactionalWithRange(startOffset, rangeLength, op);
    }
}
