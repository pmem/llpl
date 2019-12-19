/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.Consumer;

/**
 * The base class for all memory block classes.  A memory block represents an allocated portion
 * of a heap. <br><br>
 * All read and write operations specify locations using zero-based offsets that refer
 * to the number of bytes from the beginning of the memory block. A reference within one memory block 
 * to another memory block can be made by storing the referenced memory block's handle, available using 
 * the memory block {@link com.intel.pmem.llpl.AnyMemoryBlock#handle} method.  Given a handle, a
 * corresponding memory block can be retrieved using the heap's {@code memoryBlockFromHandle(long handle)}
 * or {@code compactMemoryBlockFromHandle(long handle)} method.<br><br>
 * Read and write operations using an invalid memory block (e.g. if the MemoryBlock's {@code free()} method
 * has been called)a memory block that refers to freed memory) will throw an {@code IllegalStateException}.
 * The {@link com.intel.pmem.llpl.AnyMemoryBlock#isValid()} method can be used to check if a specific 
 * memory block object is valid for use.
 */
public abstract class AnyMemoryBlock {
    private static boolean ELIDE_FLUSHES;
     
    static {
        // ELIDE_FLUSHES = (nativeHasAutoFlush() == 1);
        ELIDE_FLUSHES = false;
    }

    private static final long SIZE_OFFSET = 0; 
    private final AnyHeap heap;
    private long size;
    private long address;       
    private long directAddress; 

    static {
        System.loadLibrary("llpl");
    }

    // Constructor
    AnyMemoryBlock(AnyHeap heap, long size, boolean bounded, boolean transactional) {
        if (size <= 0) throw new HeapException("Failed to allocate memory block of size " + size);
        this.heap = heap;
        long allocSize = size + metadataSize();
        Runnable body = () -> {
            this.address = transactional ? heap.allocateTransactional(allocSize) : heap.allocateAtomic(allocSize);
            if (address == 0) throw new HeapException("Failed to allocate memory block of size " + size);
            this.directAddress = directAddress(heap, address);
            if (bounded) setPersistentSize(size);
            else this.size = -1;
        };        
        if (transactional) new Transaction(heap).run(body);
        else body.run();
        // Stats.current.allocStats.update(getClass().getName(), allocSize, 0, 1);   // uncomment for allocation stats
    }

    // Reconstructor
    AnyMemoryBlock(AnyHeap heap, long offset, boolean bounded) {
        this.heap = heap;
        this.address = offset;
        this.directAddress = heap.poolHandle() + address;
        this.size = bounded ? getPersistentSize() : -1;
        if (bounded) {
            if (this.size <= 0 || !this.heap.isInBounds(offset + this.size, 0)) {
                throw new HeapException("Failed to reconstruct memory block from supplied handle");
            }
        }
    }

    long directAddress(AnyHeap heap, long offset) {
        return heap.poolHandle() + offset; 
    }

    AnyHeap heap() {
        return heap;
    }

    abstract long metadataSize();

    long address() {
        return handle();
    }

    /**
     * Returns a handle to this memory block.  This stable value can be stored and used later 
     * in heap methods {@code memoryBlockFromHandle} and {@code compactMemoryBlockFromHandle} 
     * to regain access to the memory block.
     * @return a handle to the memory block
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    public long handle() {
        checkValid();
        return this.address;
    }

    /**
     * Checks whether this memory block is in a valid state for use, for example, that it's {@code free()}
     * method has not been called. 
     * @return true if this memory block is valid for use
     */
    public boolean isValid() {
        return directAddress != 0;
    }

    /**
     * Checks that this memory block is in a valid state for use, for example it has not been freed. 
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    void checkValid() {
        heap.checkValid();
        if (directAddress != 0) return;
        throw new IllegalStateException("Invalid memory block");
    }
    
    // 5 public read methods

    /**
     * Retrieves the {@code byte} value at {@code offset} within this memory block.  
     * @param offset the location from which to retrieve data
     * @return the {@code byte} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
     public byte getByte(long offset) {
        checkValid();
        checkBounds(offset, 1);
        return AnyHeap.UNSAFE.getByte(payloadAddress(offset));
    }

    /**
     * Retrieves the {@code short} value at {@code offset} within this memory block.  
     * @param offset the location from which to retrieve data
     * @return the {@code short} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    public short getShort(long offset) {
        checkValid();
        checkBounds(offset, 2);
        return AnyHeap.UNSAFE.getShort(payloadAddress(offset));
    }

    /**
     * Retrieves the {@code int} value at {@code offset} within this memory block.  
     * @param offset the location from which to retrieve data
     * @return the {@code int} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
          * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    public int getInt(long offset) {
        checkValid();
        checkBounds(offset, 4);
        return AnyHeap.UNSAFE.getInt(payloadAddress(offset));
    }

    /**
     * Retrieves the {@code long} value at {@code offset} within this memory block.  
     * @param offset the location from which to retrieve data
     * @return the {@code long} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    public long getLong(long offset) {
        checkValid();
        checkBounds(offset, 8);
        return AnyHeap.UNSAFE.getLong(payloadAddress(offset));
    }

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
    public void copyToArray(long srcOffset, byte[] dstArray, int dstOffset, int length) {
        checkValid();
        checkBoundsAndLength(srcOffset, length);
        if (dstOffset < 0 || dstOffset + length > dstArray.length) throw new IndexOutOfBoundsException("array index out of bounds.");
        uncheckedCopyToArray(directAddress() + metadataSize() + srcOffset, dstArray, dstOffset, length);
    }

    /**
     * Stores the supplied {@code byte} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    public abstract void setByte(long offset, byte value);

    /**
     * Stores the supplied {@code short} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    public abstract void setShort(long offset, short value);

    /**
     * Stores the supplied {@code int} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    public abstract void setInt(long offset, int value);

    /**
     * Stores the supplied {@code long} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    public abstract void setLong(long offset, long value);

    /**
     * Copies {@code length} bytes from the {@code srcBlock} memory block, starting at {@code srcOffset}, to  
     * this memory block starting at {@code dstOffset}.  
     * @param srcBlock the memory block from which to copy bytes
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of memory block bounds 
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    abstract void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length);
    
    /**
     * Copies {@code length} bytes from the {@code srcArray} byte array, starting at {@code srcOffset}, to  
     * this memory block starting at {@code dstOffset}.  
     * @param srcArray the array from which to copy bytes
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of array or memory block bounds 
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    public abstract void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length);

    /**
     * Sets {@code length} bytes in this memory block, starting at {@code offset}, to the supplied {@code byte}  
     * value.  
     * @param value the value to set
     * @param offset the starting offset in this memory block
     * @param length the number of bytes to set
     * @throws IndexOutOfBoundsException if setting would cause access of data outside of memory block bounds 
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */    
    public abstract void setMemory(byte value, long offset, long length);

    /**
    * Returns a hash code for this memory block.  Note that memory block hash codes are not computed based on the 
    * presistent memory they reference and are only stable for the life of the memory block object itself.   
    * @return a hash code for this memory block
    */
    @Override
    public int hashCode() {
        return Objects.hash(address(), heap.poolHandle()); 
    }

    /**
    * Compares this memory block to the specified object.  The result is true if and only if the argument is not 
    * null and is a memory block that refers to the same range of memory as this object. 
    * @return true if the given object is a memory block that refers to the same range of memory as this object
    */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AnyMemoryBlock)) return false;
        AnyMemoryBlock other = (AnyMemoryBlock)obj;
        return this.address() == other.address() && heap.poolHandle() == other.heap.poolHandle();
    }

    // Raw

    void rawSetByte(long offset, byte value) {
        checkValid();
        checkBounds(offset, 1);
        setRawByte(offset, value);
    }

    void rawSetShort(long offset, short value) {
        checkValid();
        checkBounds(offset, 2);
        setRawShort(offset, value);
    }

    void rawSetInt(long offset, int value) {
        checkValid();
        checkBounds(offset, 4);
        setRawInt(offset, value);
    }

    void rawSetLong(long offset, long value) {
        checkValid();
        checkBounds(offset, 8);
        setRawLong(offset, value);
    }

    void rawCopyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        checkValid();
        srcBlock.checkValid();
        srcBlock.checkBoundsAndLength(srcOffset, length);
        checkBoundsAndLength(dstOffset, length);
        AnyMemoryBlock.uncheckedCopyBlockToBlock(srcBlock.directAddress() + srcBlock.metadataSize() + srcOffset, directAddress() + metadataSize() + dstOffset, length);
    }

    void rawCopyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        if (srcOffset < 0 || srcOffset + length > srcArray.length) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(srcOffset, length));
        checkValid();
        checkBoundsAndLength(dstOffset, length);
        AnyMemoryBlock.uncheckedCopyFromArray(srcArray, srcOffset, directAddress() + metadataSize() + dstOffset, length);
    }

    void rawSetMemory(byte val, long offset, long length) {
        checkValid();
        checkBoundsAndLength(offset, length);
        AnyMemoryBlock.uncheckedSetMemory(directAddress() + metadataSize() + offset, val, length);
    }

    // Durable 

    void durableSetByte(long offset, byte value) {
        rawSetByte(offset, value);
        internalFlush(offset, 1);
    }

    void durableSetShort(long offset, short value) {
        rawSetShort(offset, value);
        internalFlush(offset, 2);
    }

    void durableSetInt(long offset, int value) {
        rawSetInt(offset, value);
        internalFlush(offset, 4);
    }

    void durableSetLong(long offset, long value) {
        rawSetLong(offset, value);
        internalFlush(offset, 8);
    }

    void durableCopyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        durableWithRange(dstOffset, length, (Range range) -> {
            range.copyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
        });
    }

    void durableCopyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        durableWithRange(dstOffset, length, (Range range) -> {
            range.copyFromArray(srcArray, srcOffset, dstOffset, length);
        });
    }

    void durableSetMemory(byte val, long offset, long length) {
        durableWithRange(offset, length, (Range range) -> {
            range.setMemory(val, offset, length);
        });
    }

    <T> T durableWithRange(long startOffset, long length, Function<Range, T> op) {
        Range range = range(startOffset, length);
        T result = op.apply(range);
        range.flush();
        range.markInvalid();
        return result;
    }

    void durableWithRange(long startOffset, long length, Consumer<Range> op) {
        Range range = range(startOffset, length);
        op.accept(range);
        range.flush();
        range.markInvalid();
    }

    // Transactional

    void transactionalSetByte(long offset, byte value) {
        transactionalWithRange(offset, 1, (Range range) -> {range.setByte(offset, value);});
    }

    void transactionalSetShort(long offset, short value) {
        transactionalWithRange(offset, 2, (Range range) -> {range.setShort(offset, value);});
    }

    void transactionalSetInt(long offset, int value) {
        transactionalWithRange(offset, 4, (Range range) -> {range.setInt(offset, value);});
    }

    void transactionalSetLong(long offset, long value) {
        transactionalWithRange(offset, 8, (Range range) -> {range.setLong(offset, value);});
    }

    void transactionalCopyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        transactionalWithRange(dstOffset, length, (Range range) -> {
            range.copyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
        });
    }

    void transactionalCopyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        transactionalWithRange(dstOffset, length, (Range range) -> {
            range.copyFromArray(srcArray, srcOffset, dstOffset, length);
        });
    }

    void transactionalSetMemory(byte val, long offset, long length) {
        transactionalWithRange(offset, length, (Range range) -> {
            range.setMemory(val, offset, length);
        });
    }

    <T> T transactionalWithRange(long startOffset, long length, Function<Range, T> op) {
        Range range = range(startOffset, length);
        int result = range.addToTransaction();
        T ans = null;
        if (result == 2) ans = op.apply(range);
        else if (result == 1) ans = new Transaction(heap(), false).run(range, op);
        else throw new TransactionException("No active transaction and unable to create transaction.");
        range.markInvalid();
        return ans;
    }

    void transactionalWithRange(long startOffset, long length, Consumer<Range> op) {
        transactionalWithRange(startOffset, length, (Range r) -> {op.accept(r); return (Void)null;});
    }

    void transactionalWithRange(Consumer<Range> op) {
        transactionalWithRange(0, size, (Range r) -> {op.accept(r); return (Void)null;});
    }

    long size() {
        return this.size;
    }

    Range range(long startOffset, long length) {
        return new Range(this, startOffset, length);
    }

    void flush() {
        flush(0, size());
    }

    void flush(long offset, long length) {
        checkValid();
        checkBoundsAndLength(offset, length);
        internalFlush(offset, length);
    }

    void internalFlush(long offset, long size) {
        if (!ELIDE_FLUSHES) nativeFlush(payloadAddress(offset), size);
    }

    void addToTransaction() {
        addToTransaction(0, size());
    }

    void addToTransaction(long offset, long size) {
        checkValid();
        checkBoundsAndLength(offset, size);
        int result = nativeAddToTransaction(heap().poolHandle(), payloadAddress(offset), size);
        if (result != 2) throw new IllegalStateException("No transaction active.");
    }

    void setPersistentSize(long size) {
        long address = directAddress + SIZE_OFFSET;
        nativeAddToTransaction(heap().poolHandle(), address, 8);
        setAbsoluteLong(address, size);
        this.size = size;     
    }

    long getPersistentSize() {
        return getAbsoluteLong(directAddress + SIZE_OFFSET);
    }

    long payloadAddress(long payloadOffset) {
        return directAddress + metadataSize() + payloadOffset;
    }

    long directAddress() {
        return directAddress;
    }

    void markInvalid() {
        directAddress = 0;
    }

    void checkBoundsAndLength(long offset, long length) {
        if (offset < 0 || length <= 0 || offset + length > size) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(offset, length));
    }

    void checkBounds(long offset, long length) {
        if (offset < 0 || offset + length > size) throw new IndexOutOfBoundsException(AnyMemoryBlock.outOfBoundsMessage(offset, length));
    }

    void setAbsoluteByte(long address, byte value) {
        AnyHeap.UNSAFE.putByte(address, value);
    }

    void setAbsoluteShort(long address, short value) {
        AnyHeap.UNSAFE.putShort(address, value);
    }

    void setAbsoluteInt(long address, int value) {
        AnyHeap.UNSAFE.putInt(address, value);
    }

    void setAbsoluteLong(long address, long value) {
        AnyHeap.UNSAFE.putLong(address, value);
    }

    byte getAbsoluteByte(long address) {
        return AnyHeap.UNSAFE.getByte(address);
    }

    short getAbsoluteShort(long address) {
        return AnyHeap.UNSAFE.getShort(address);
    }

    int getAbsoluteInt(long address) {
        return AnyHeap.UNSAFE.getInt(address);
    }

    long getAbsoluteLong(long address) {
        return AnyHeap.UNSAFE.getLong(address);
    }

    void setRawByte(long offset, byte value) {
        AnyHeap.UNSAFE.putByte(payloadAddress(offset), value);
    }

    void setRawShort(long offset, short value) {
        AnyHeap.UNSAFE.putShort(payloadAddress(offset), value);
    }

    void setRawInt(long offset, int value) {
        AnyHeap.UNSAFE.putInt(payloadAddress(offset), value);
    }

    void setRawLong(long offset, long value) {
        AnyHeap.UNSAFE.putLong(payloadAddress(offset), value);
    }

    static void uncheckedCopyToArray(long srcAddress, byte[] dstArray, int dstOffset, int length) {
        long dstAddress = AnyHeap.UNSAFE.ARRAY_BYTE_BASE_OFFSET + AnyHeap.UNSAFE.ARRAY_BYTE_INDEX_SCALE * dstOffset;
        AnyHeap.UNSAFE.copyMemory(null, srcAddress, dstArray, dstAddress, length);
    }

    static void uncheckedCopyBlockToBlock(long srcAddress, long dstAddress, long length) {
        AnyHeap.UNSAFE.copyMemory(srcAddress, dstAddress, length);
    } 

    static void uncheckedCopyFromArray(byte[] srcArray, int srcOffset, long dstAddress, int length) {
        long srcAddress = AnyHeap.UNSAFE.ARRAY_BYTE_BASE_OFFSET + AnyHeap.UNSAFE.ARRAY_BYTE_INDEX_SCALE * srcOffset;
        AnyHeap.UNSAFE.copyMemory(srcArray, srcAddress, null, dstAddress, length);
    }

    static void uncheckedSetMemory(long dstAddress, byte val, long length) {
        AnyHeap.UNSAFE.setMemory(dstAddress, length, val); 
    }

    static String outOfBoundsMessage(long offset, long length)
    {
        if (offset < 0) return "negative offset: " + offset;
        if (length < 0) return "negative length: " + length;
        return String.format("offset + length is out of bounds: %s + %s", offset, length);
    }

    private native static void nativeFlush(long address, long size);
    private native static int nativeAddToTransaction(long poolHandle, long address, long size);
    private native static int nativeHasAutoFlush();
    native static int nativeAddToTransactionNoCheck(long address, long size);
    native static int nativeAddRangeToTransaction(long poolHandle, long address, long size);
}
