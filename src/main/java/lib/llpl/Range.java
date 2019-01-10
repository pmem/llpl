/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

/**
 * Implements write methods for a range of locations within an associated memory block.  
 */
public final class Range {
    private final AnyMemoryBlock block;
    private long startOffset;
    private final long endOffset;
    private final long rangeLength;

    Range(AnyMemoryBlock block, long startOffset, long length) {
        block.checkBounds(startOffset, length);
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

    String debugString() {
    	return String.format("Range(%d, %d)", startOffset, rangeLength);
    }

    /**
     * Checks that the range of bytes from {@code offset} (inclusive) to {@code offset} + length (exclusive) 
     * is within the bounds of this {@code Range} object. 
     * @param start The start of the range to check
     * @param length The number of bytes in the range to check
     * @throws IndexOutOfBoundsException if the range is not within this Range's bounds
     */
    public void checkBounds(long start, long length) {
    	// System.out.format("Range.checkBounds(%d < %d || %d + %d > %d)\n", start, startOffset, start, length, endOffset);
        if (start < startOffset || start + length > endOffset) throw new IndexOutOfBoundsException();
    }

    // public byte getByte(long offset) {
    // 	checkValid();
    //     checkBounds(offset, 1);
    //     return AnyHeap.UNSAFE.getByte(block.payloadAddress(offset));
    // }

    // public short getShort(long offset) {
    // 	checkValid();
    //     checkBounds(offset, 2);
    //     return AnyHeap.UNSAFE.getShort(block.payloadAddress(offset));
    // }

    // public int getInt(long offset) {
    // 	checkValid();
    //     checkBounds(offset, 4);
    //     return AnyHeap.UNSAFE.getInt(block.payloadAddress(offset));
    // }

    // public long getLong(long offset) {
    // 	checkValid();
    //     checkBounds(offset, 8);
    //     return AnyHeap.UNSAFE.getLong(block.payloadAddress(offset));
    // }

    // public void copyToArray(long srcOffset, byte[] dstArray, int dstOffset, int length) {
    // 	checkValid();
    //     checkBounds(srcOffset, length);
    //     if (dstOffset < 0 || dstOffset + length >= dstArray.length) throw new IndexOutOfBoundsException("array index out of bounds.");
    //     block.uncheckedCopyToArray(block.directAddress() + block.baseOffset() + srcOffset, dstArray, dstOffset, length);
    // }

    /**
     * Stores the supplied {@code byte} value at {@code offset} within the memory block associated
     * with this range.  
     * @param offset the location at which to store the value
     * @param value the value to store
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
     */
    public void setLong(long offset, long value) {
    	checkValid();
        checkBounds(offset, 8);
        block.setRawLong(offset, value);
    }

    /**
     * Copies {@code length} bytes from the supplied source memory block, starting at {@code srcOffset}, to  
     * the memory block associated with this range, starting at {@code dstOffset}.  
     * @param srcBlock the memory block to copy bytes from
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     */
    public void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
    	checkValid();
    	srcBlock.checkValid();
        block.checkBounds(srcOffset, length);
        checkBounds(dstOffset, length);
        AnyMemoryBlock.uncheckedCopyBlockToBlock(srcBlock.directAddress() + srcBlock.baseOffset() + srcOffset, block.directAddress() + block.baseOffset() + dstOffset, length);
    }

    /**
     * Copies {@code length} bytes from the supplied byte array, starting at {@code srcOffset}, to  
     * the memory block associated with this range, starting at {@code dstOffset}.  
     * @param srcArray the array to copy bytes from
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     */
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
    	checkValid();
        if (srcOffset < 0 || srcOffset + length > srcArray.length) throw new IndexOutOfBoundsException();
        checkBounds(dstOffset, length);
        AnyMemoryBlock.uncheckedCopyFromArray(srcArray, srcOffset, block.directAddress() + block.baseOffset() + dstOffset, length);
    }

    /**
     * Sets {@code length} bytes in the memory block associted with this range, starting at {@code offset}, to the supplied {@code byte}  
     * value.  
     * @param value the value to set
     * @param offset the starting offset in the memory block associated with this range
     * @param length the number of bytes to set
     */    
    public void setMemory(byte value, long offset, long length) {
    	checkValid();
        checkBounds(offset, length);
        AnyMemoryBlock.uncheckedSetMemory(block.directAddress() + block.baseOffset() + offset, value, length);
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
        AnyMemoryBlock.nativeAddToTransactionNoCheck(block.payloadAddress(startOffset), rangeLength);
    }
}
