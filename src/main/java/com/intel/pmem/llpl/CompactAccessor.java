/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Consumer;

/**
 * Implements a read and write interface for accessing a {@link com.intel.pmem.llpl.Heap}. Access through a 
 * {@code MemoryBlock} is bounds-checked to be within the block's allocated space.
 *  
 * @see com.intel.pmem.llpl.AnyMemoryBlock   
 */

/*TODO: Should not be a subclass of AnyMemoryBlock as it violates Liskov's substitution principle*/
public final class CompactAccessor extends AnyMemoryBlock {

    static final long METADATA_SIZE = 0;

    CompactAccessor(Heap heap) {
        super(heap);
    }

    /**
    * Returns the heap from which this memory block was allocated.
    * @return the {@code Heap} from which this memory block was allocated
    */
    public Heap heap() {
        return (Heap)super.heap();
    }

    /**
     * Retrieves the {@code byte} value at {@code offset} within this memory block.  
     * @param offset the location from which to retrieve data
     * @return the {@code byte} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
     /*public byte getByte(long offset) {
        checkValid();
        checkBounds(offset, 1);
        return AnyHeap.UNSAFE.getByte(payloadAddress(offset));
    }
*/
    /**
     * Retrieves the {@code short} value at {@code offset} within this memory block.  
     * @param offset the location from which to retrieve data
     * @return the {@code short} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
 /*   public short getShort(long offset) {
        checkValid();
        checkBounds(offset, 2);
        return AnyHeap.UNSAFE.getShort(payloadAddress(offset));
    }
*/
    /**
     * Retrieves the {@code int} value at {@code offset} within this memory block.  
     * @param offset the location from which to retrieve data
     * @return the {@code int} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
          * @throws IllegalStateException if the memory block is not in a valid state for use
     */
 /*   public int getInt(long offset) {
        checkValid();
        checkBounds(offset, 4);
        return AnyHeap.UNSAFE.getInt(payloadAddress(offset));
    }
*/
    /**
     * Retrieves the {@code long} value at {@code offset} within this memory block.  
     * @param offset the location from which to retrieve data
     * @return the {@code long} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
 /*   public long getLong(long offset) {
        checkValid();
        checkBounds(offset, 8);
        return AnyHeap.UNSAFE.getLong(payloadAddress(offset));
    }
*/
    /**
     * Copies {@code length} bytes from this memory block, starting at {@code srcOffset}, to the 
     * {@code dstArray} byte array starting at array index {@code dstOffset}.  
     * @param srcOffset the starting offset in this memory block
     * @param dstArray the destination byte array
     * @param dstOffset the starting offset in the destination array
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of array or memory block bounds 
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
 /*   public void copyToArray(long srcOffset, byte[] dstArray, int dstOffset, int length) {
        checkValid();
        checkBoundsAndLength(srcOffset, length);
        if (dstOffset < 0 || dstOffset + length > dstArray.length) throw new IndexOutOfBoundsException("array index out of bounds.");
        uncheckedCopyToArray(directAddress() + metadataSize() + srcOffset, dstArray, dstOffset, length);
    }
*/
    /**
     * {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void setByte(long offset, byte value) {
        super.rawSetByte(offset, value);
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
        super.rawSetShort(offset, value);
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
        super.rawSetInt(offset, value);
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
        super.rawSetLong(offset, value);
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
        super.rawCopyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
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
        super.rawCopyFromArray(srcArray, srcOffset, dstOffset, length);
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
        super.rawSetMemory(value, offset, length);
    }

    /**
    * Ensures that any modifications made within the supplied range within this memory 
    * block are written to persistent memory media.
    * @param offset the location from which to flush bytes
    * @param length the number of bytes to flush
    * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds 
    * @throws IllegalStateException if the memory block is not in a valid state for use
    */
     public void flush(long offset, long length) {
        super.flush(offset, length);
    }

    /**
    * Adds the specified range of of bytes within this memory block to the current transaction.
    * Any modifications to this range of bytes will be committed on successful completion of the current
    * transaction or rolled-back on abort of the current transaction
    * @param offset the start of the range of bytes to add
    * @param length the number of bytes to add
    * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block bounds 
    * @throws IllegalStateException if the memory block is not in a valid state for use
    */
    public void addToTransaction(long offset, long length) {
        super.addToTransaction(offset, length);
    }

    /**
    * Deallocates this memory block.
    * @param transactional if true, the deallocation operation will be done transactionally
    * @throws HeapException if the memory block could not be deallocated
    */
    public void freeMemory(boolean transactional) {
        heap().freeMemoryBlock(this, transactional);
    }
    /**
     * Checks that the range of bytes from {@code offset} (inclusive) to {@code offset} + length (exclusive) 
     * is within the bounds of this memory block. 
     * @param offset The start if the range to check
     * @param length The number of bytes in the range to check
     * @throws IndexOutOfBoundsException if the range is not within this memory block's bounds
     */
    /*void checkBounds(long offset, long length) {
        super.checkBounds(offset, length);
    }*/

    void checkBounds(long offset, long length) {
        if (offset < 0 || heap().outOfBounds(offset + length + handle())) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(offset, length));
    }

    void checkBoundsAndLength(long offset, long length) {
        if (offset < 0 || length <= 0 || heap().outOfBounds(offset + length + handle())) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(offset, length));
    }

    @Override
    long metadataSize() { 
        return METADATA_SIZE; 
    }

    /**
    * Adds this memory block's range of bytes to the current transaction.
    * Any modifications to this range of bytes will be committed upon successful completion of the current
    * transaction or rolled-back on an abort of the current transaction
    * @throws IllegalStateException if the memory block is not in a valid state for use
    */
    @Override
    public void addToTransaction() {
        super.addToTransaction(0, size());
    }

    public void handle(long handle) {
        super.handle(handle, false);
    }

    public void resetHandle() {
        super.reset();
    }
}
