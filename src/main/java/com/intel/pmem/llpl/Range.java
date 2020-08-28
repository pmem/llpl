/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

/**
 * Implements methods suitable for writing within a contiguous range of locations in an associated block of memory. An instance of this class
 * is provided as an argument to user-supplied functions associated with ranged operations, such as 
 * {@link com.intel.pmem.llpl.TransactionalMemoryBlock#withRange(Consumer)}
 * 
 * @since 1.0
 */
public final class Range {
    private final MemoryAccessor accessor;
    private long startOffset;
    private final long endOffset;
    private final long rangeLength;

    Range(MemoryAccessor accessor, long startOffset, long length) {
        accessor.checkBoundsAndLength(startOffset, length);
        this.accessor = accessor;
        this.startOffset = startOffset;
        this.endOffset = startOffset + length;
        this.rangeLength = length;
    }

    void markInvalid() {
        startOffset = -1;
    }

    void checkValid() {
        if (startOffset < 0) throw new IllegalStateException("Invalid Range object");
        accessor.checkValid();
    }

    /**
     * Checks whether this range object is in a valid state for use. A range is marked invalid, 
     for example, after the function to which it is supplied completes execution. 
     * @return true if this range is valid for use
     */
    public boolean isValid() {
        return startOffset > -1;
    }

    /**
     * Returns this ranges's start offset within it's associated memory. 
     * @return the start offset
     */
    public long startOffset () {
        return startOffset;
    }

    /**
     * Returns this ranges's length in bytes. 
     * @return this range's length
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
     * Stores the supplied {@code byte} value at {@code offset} within the block of memory associated
     * with this range.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of this range's bounds
     * @throws IllegalStateException if this range is not in a valid state for use
     */
    public void setByte(long offset, byte value) {
        checkValid();
        checkBounds(offset, 1);
        accessor.setRawByte(offset, value);
    }

    /**
     * Stores the supplied {@code short} value at {@code offset} within the block of memory associated
     * with this range.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of this range's bounds
     * @throws IllegalStateException if this range is not in a valid state for use
     */
    public void setShort(long offset, short value) {
        checkValid();
        checkBounds(offset, 2);
        accessor.setRawShort(offset, value);
    }

    /**
     * Stores the supplied {@code int} value at {@code offset} within the block of memory associated
     * with this range.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of this range's bounds
     * @throws IllegalStateException if this range is not in a valid state for use
     */
    public void setInt(long offset, int value) {
        checkValid();
        checkBounds(offset, 4);
        accessor.setRawInt(offset, value);
    }

    /**
     * Stores the supplied {@code long} value at {@code offset} within the block of memory associated
     * with this range.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of this range's bounds
     * @throws IllegalStateException if this range is not in a valid state for use
     */
    public void setLong(long offset, long value) {
        checkValid();
        checkBounds(offset, 8);
        accessor.setRawLong(offset, value);
    }

    /**
     * Copies {@code length} bytes from {@code srcAccessor}, starting at {@code srcOffset}, to  
     * the block of memory associated with this range, starting at {@code dstOffset}.  
     * @param srcAccessor the memory block from which to copy bytes
     * @param srcOffset the starting offset in the source accessor's memory
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of this range's bounds 
     * @throws IllegalStateException if this range is not in a valid state for use
     */
    public void copyFrom(MemoryAccessor srcAccessor, long srcOffset, long dstOffset, long length) {
        checkValid();
        srcAccessor.checkValid();
        srcAccessor.checkBoundsAndLength(srcOffset, length);
        checkBoundsAndLength(dstOffset, length);
        MemoryAccessor.uncheckedCopyBlockToBlock(srcAccessor.directAddress() + srcAccessor.metadataSize() + srcOffset, accessor.directAddress() + accessor.metadataSize() + dstOffset, length);
    }

     /**
     * Copies {@code length} bytes from {@code srcBlock}, starting at {@code srcOffset}, to  
     * the block of memory associated with this range starting at {@code dstOffset}.  
     * @param srcBlock the accessor from whose memory to copy bytes
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of this range's bounds 
     * @throws IllegalStateException if this range is not in a valid state for use
     */
    public void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        copyFrom(srcBlock, srcOffset, dstOffset, length);
    }

    /**
     * Copies {@code length} bytes from {@code srcArray}, starting at {@code srcOffset}, to  
     * the block of memory associated with this range, starting at {@code dstOffset}.  
     * @param srcArray the array to from which to copy bytes
     * @param srcIndex the starting index in the source array
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of this range's bounds 
     * @throws IllegalStateException if this range is not in a valid state for use
     */
    public void copyFromArray(byte[] srcArray, int srcIndex, long dstOffset, int length) {
        checkValid();
        if (srcIndex < 0 || srcIndex + length > srcArray.length) throw new IndexOutOfBoundsException(MemoryAccessor.outOfBoundsMessage(srcIndex, length));
        checkBoundsAndLength(dstOffset, length);
        MemoryAccessor.uncheckedCopyFromArray(srcArray, srcIndex, accessor.directAddress() + accessor.metadataSize() + dstOffset, length);
    }

    /**
     * Sets {@code length} bytes in the block of memory associted with this range, starting at {@code offset}, to the supplied {@code byte}  
     * value.  
     * @param value the value to set
     * @param offset the starting offset in the block of memory associated with this range
     * @param length the number of bytes to set
     * @throws IndexOutOfBoundsException if setting would cause access of data outside of this range's bounds 
     * @throws IllegalStateException if this range is not in a valid state for use
     */    
    public void setMemory(byte value, long offset, long length) {
        checkValid();
        checkBoundsAndLength(offset, length);
        MemoryAccessor.uncheckedSetMemory(accessor.directAddress() + accessor.metadataSize() + offset, value, length);
    }

    void flush() {
        checkValid();
        accessor.internalFlush(startOffset, rangeLength);
    }

    int addToTransaction() {
        checkValid();
        return MemoryAccessor.nativeAddRangeToTransaction(accessor.heap().poolHandle(), accessor.payloadAddress(startOffset), rangeLength);
    }

    void addToTransactionNoCheck() {
        checkValid();
        int res = MemoryAccessor.nativeAddToTransactionNoCheck(accessor.payloadAddress(startOffset), rangeLength);
        if (res != 0) throw new TransactionException("Failed to add range to transaction.");
    }
}
