/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

/**
 * Implements write methods for a range of locations within an associated memory block. Instances of this class
 * are provided as an argument to user functions associated with ranged operations such as 
 * {@link com.intel.pmem.llpl.TransactionalMemoryBlock#withRange(Consumer) withRange}
 */
public final class Range {
    private final AnyMemoryBlock block;
    private long startOffset;
    private final long endOffset;
    private final long rangeLength;

    Range(AnyMemoryBlock block, long startOffset, long length) {
        block.checkBoundsAndLength(startOffset, length);
        this.block = block;
        this.startOffset = startOffset;
        this.endOffset = startOffset + length;
        this.rangeLength = length;
    }

    void markInvalid() {
    	startOffset = -1;
    }

    void checkValid() {
        if (startOffset < 0) throw new IllegalStateException("Invalid Range object");
        block.checkValid();
    }

    /**
     * Checks whether this range object is in a valid state for use. A range is marked invalid, for example, after the function to which it is supplied completes execution. 
     * @return true if this range is valid for use
     */
    public boolean isValid() {
        return startOffset > -1;
    }

    /**
     * Returns the ranges's start offset within it's memory block. 
     * @return the start offset
     */
    public long startOffset () {
        return startOffset;
    }

    /**
     * Returns the ranges's length in bytes. 
     * @return the range's length
     */
     public long rangeLength() {
        return rangeLength;        
    }

    void checkBounds(long start, long length) {
        if (start < startOffset || start + length > endOffset) throw new IndexOutOfBoundsException("range start or length is out of bounds");
    }

    void checkBoundsAndLength(long start, long length) {
        if (start < startOffset || length <= 0 || start + length > endOffset) throw new IndexOutOfBoundsException("range start or length is out of bounds");
    }

    /**
     * Stores the supplied {@code byte} value at {@code offset} within the memory block associated
     * with this range.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of the range bounds
     * @throws IllegalStateException if the range is not in a valid state for use
     */
    public void setByte(long offset, byte value) {
    	checkValid();
        checkBounds(offset, 1);
        block.setRawByte(offset, value);
    }

    /**
     * Stores the supplied {@code short} value at {@code offset} within the memory block associated
     * with this range.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of the range bounds
     * @throws IllegalStateException if the range is not in a valid state for use
     */
    public void setShort(long offset, short value) {
    	checkValid();
        checkBounds(offset, 2);
        block.setRawShort(offset, value);
    }

    /**
     * Stores the supplied {@code int} value at {@code offset} within the memory block associated
     * with this range.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of the range bounds
     * @throws IllegalStateException if the range is not in a valid state for use
     */
    public void setInt(long offset, int value) {
    	checkValid();
        checkBounds(offset, 4);
        block.setRawInt(offset, value);
    }

    /**
     * Stores the supplied {@code long} value at {@code offset} within the memory block associated
     * with this range.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of the range bounds
     * @throws IllegalStateException if the range is not in a valid state for use
     */
    public void setLong(long offset, long value) {
    	checkValid();
        checkBounds(offset, 8);
        block.setRawLong(offset, value);
    }

    /**
     * Copies {@code length} bytes from the supplied source memory block, starting at {@code srcOffset}, to  
     * the memory block associated with this range, starting at {@code dstOffset}.  
     * @param srcBlock the memory block from which to copy bytes
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of range bounds 
     * @throws IllegalStateException if the range is not in a valid state for use
     */
    public void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
    	checkValid();
    	srcBlock.checkValid();
        srcBlock.checkBoundsAndLength(srcOffset, length);
        checkBoundsAndLength(dstOffset, length);
        AnyMemoryBlock.uncheckedCopyBlockToBlock(srcBlock.directAddress() + srcBlock.metadataSize() + srcOffset, block.directAddress() + block.metadataSize() + dstOffset, length);
    }

    /**
     * Copies {@code length} bytes from the supplied byte array, starting at {@code srcOffset}, to  
     * the memory block associated with this range, starting at {@code dstOffset}.  
     * @param srcArray the array to from which to copy bytes
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of range bounds 
     * @throws IllegalStateException if the range is not in a valid state for use
     */
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
    	checkValid();
        if (srcOffset < 0 || srcOffset + length > srcArray.length) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(srcOffset, length));
        checkBoundsAndLength(dstOffset, length);
        AnyMemoryBlock.uncheckedCopyFromArray(srcArray, srcOffset, block.directAddress() + block.metadataSize() + dstOffset, length);
    }

    /**
     * Sets {@code length} bytes in the memory block associted with this range, starting at {@code offset}, to the supplied {@code byte}  
     * value.  
     * @param value the value to set
     * @param offset the starting offset in the memory block associated with this range
     * @param length the number of bytes to set
     * @throws IndexOutOfBoundsException if setting would cause access of data outside of range bounds 
     * @throws IllegalStateException if the range is not in a valid state for use
     */    
    public void setMemory(byte value, long offset, long length) {
    	checkValid();
        checkBoundsAndLength(offset, length);
        AnyMemoryBlock.uncheckedSetMemory(block.directAddress() + block.metadataSize() + offset, value, length);
    }

    void flush() {
    	checkValid();
        block.internalFlush(startOffset, rangeLength);
    }

    int addToTransaction() {
    	checkValid();
        return AnyMemoryBlock.nativeAddRangeToTransaction(block.heap().poolHandle(), block.payloadAddress(startOffset), rangeLength);
    }

    void addToTransactionNoCheck() {
    	checkValid();
        int res = AnyMemoryBlock.nativeAddToTransactionNoCheck(block.payloadAddress(startOffset), rangeLength);
        if (res != 0) throw new TransactionException("Failed to add range to transaction.");
    }
}
