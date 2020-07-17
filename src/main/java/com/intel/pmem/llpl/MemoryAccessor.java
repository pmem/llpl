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
 * The base class for all memory accessor classes, inlcuding derivatives of {@code AnyMemoryBlock} and {@code AnyAccessor}. 
 * A memory accessor refers to an allocated portion of a heap. <br><br>
 * All read and write operations specify locations using zero-based offsets that refer
 * to the number of bytes from the beginning of an allocated block of memory. A reference within one block 
 * of memory to another block of memory can be made by storing a handle to the referenced memory, available 
 * using the {@link com.intel.pmem.llpl.MemoryAccessor#handle} method.  Given a handle, access to
 * the corresponding memory can be achieved be either aquiring a memory block object using the heap's 
 * {@code memoryBlockFromHandle(long handle)} method or by setting an existing Accessor object's handle using its 
 * {@code handle(long handle)} method.<br><br>
 * Read and write operations using an invalid accessor (e.g. if the accessor's {@code free()} method
 * has been called) will throw an {@code IllegalStateException}.
 * The {@link com.intel.pmem.llpl.MemoryAccessor#isValid()} method can be used to check if a specific 
 * accessor is valid for use.
 * 
 * @since 1.1
 */
public abstract class MemoryAccessor {
    private static boolean ELIDE_FLUSHES;
     
    static {
        // ELIDE_FLUSHES = (nativeHasAutoFlush() == 1);
        ELIDE_FLUSHES = false;
    }

    static final long SIZE_OFFSET = 0; 
    final AnyHeap heap;
    private long size;
    private long address;       
    private long directAddress; 

    static {
        System.loadLibrary("llpl");
    }

    // Constructor
    MemoryAccessor(AnyHeap heap, long size, boolean bounded, boolean transactional) {
        if (size <= 0) throw new HeapException("Failed to allocate memory of size " + size);
        this.heap = heap;
        long allocSize = size + metadataSize();
        Runnable body = () -> {
            this.address = transactional ? heap.allocateTransactional(allocSize) : heap.allocateAtomic(allocSize);
            if (address == 0) throw new HeapException("Failed to allocate memory of size " + size);
            this.directAddress = directAddress(heap, address);
            if (bounded) setPersistentSize(size);
            else this.size = -1;
        };        
        if (transactional) new Transaction(heap).run(body);
        else body.run();
        // Stats.current.allocStats.update(getClass().getName(), allocSize, 0, 1);   // uncomment for allocation stats
    }

    // Reconstructor
    MemoryAccessor(AnyHeap heap, long offset, boolean bounded) {
        this.heap = heap;
        handle(offset, bounded);
    }

    MemoryAccessor(AnyHeap heap) {
        this.heap = heap;
        this.address = 0;
        this.directAddress = 0;
    }

    void reset() {
        address = 0;
        directAddress = 0;
    }

    void handle(long offset, boolean bounded) {
        this.address = offset;
        this.directAddress = heap.poolHandle() + address;
        this.size = bounded ? getPersistentSize() : -1;
        if (bounded) {
            if (this.size <= 0 || !this.heap.isInBounds(offset + this.size, 0)) {
                throw new HeapException("Failed to update accessor with supplied handle");
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
     * Returns a handle to this accessor's memory.  This stable value can be stored and used later 
     * to regain access to the memory.
     * @return a handle to the memory
     * @throws IllegalStateException if this accessor is not in a valid state for use
     */
    public long handle() {
        checkValid();
        return this.address;
    }

    /**
     * Checks whether this accessor is in a valid state for use, for example, that it's {@code free()}
     * method has not been called. 
     * @return true if this accessor is valid for use
     */
    public boolean isValid() {
        return directAddress != 0;
    }

    void checkValid() {
        if (directAddress != 0) return;
        throw new IllegalStateException("Accessor references invalid memory");
    }
    
    // 5 public read methods

    /**
     * Retrieves the {@code byte} value at {@code offset} within this accessor's memory.  
     * @param offset the location from which to retrieve data
     * @return the {@code byte} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor
     * bounds or, for compact allocations, outside of heap bounds
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
     public byte getByte(long offset) {
        checkValid();
        checkBounds(offset, 1);
        return AnyHeap.UNSAFE.getByte(payloadAddress(offset));
    }

    /**
     * Retrieves the {@code short} value at {@code offset} within this accessor's memory.  
     * @param offset the location from which to retrieve data
     * @return the {@code short} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor
     * bounds or, for compact allocations, outside of heap bounds
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    public short getShort(long offset) {
        checkValid();
        checkBounds(offset, 2);
        return AnyHeap.UNSAFE.getShort(payloadAddress(offset));
    }

    /**
     * Retrieves the {@code int} value at {@code offset} within this accessor's memory.  
     * @param offset the location from which to retrieve data
     * @return the {@code int} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor
     * bounds or, for compact allocations, outside of heap bounds
          * @throws IllegalStateException if theaccessor is not in a valid state for use
     */
    public int getInt(long offset) {
        checkValid();
        checkBounds(offset, 4);
        return AnyHeap.UNSAFE.getInt(payloadAddress(offset));
    }

    /**
     * Retrieves the {@code long} value at {@code offset} within this accessor's memory.  
     * @param offset the location from which to retrieve data
     * @return the {@code long} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor
     * bounds or, for compact allocations, outside of heap bounds
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    public long getLong(long offset) {
        checkValid();
        checkBounds(offset, 8);
        return AnyHeap.UNSAFE.getLong(payloadAddress(offset));
    }

    /**
     * Copies {@code length} bytes from this accessor's memory, starting at {@code srcOffset}, to the 
     * {@code dstArray} byte array starting at array index {@code dstOffset}.  
     * @param srcOffset the starting offset in this accessor's memory
     * @param dstArray the destination byte array
     * @param dstOffset the starting offset in the destination array
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of array or accessor bounds 
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    public void copyToArray(long srcOffset, byte[] dstArray, int dstOffset, int length) {
        checkValid();
        checkBoundsAndLength(srcOffset, length);
        if (dstOffset < 0 || dstOffset + length > dstArray.length) throw new IndexOutOfBoundsException("array index out of bounds.");
        uncheckedCopyToArray(directAddress() + metadataSize() + srcOffset, dstArray, dstOffset, length);
    }

    /**
     * Stores the supplied {@code byte} value at {@code offset} within this accessor's memory.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor bounds
     * bounds or, for compact allocations, outside of heap bounds
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    public abstract void setByte(long offset, byte value);

    /**
     * Stores the supplied {@code short} value at {@code offset} within this accessor's memory.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor
     * bounds or, for compact allocations, outside of heap bounds
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    public abstract void setShort(long offset, short value);

    /**
     * Stores the supplied {@code int} value at {@code offset} within this accessor's memory.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor
     * bounds or, for compact allocations, outside of heap bounds
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    public abstract void setInt(long offset, int value);

    /**
     * Stores the supplied {@code long} value at {@code offset} within this accessor's memory.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of accessor
     * bounds or, for compact allocations, outside of heap bounds
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    public abstract void setLong(long offset, long value);

    /**
     * Copies {@code length} bytes from the {@code src} accessor, starting at {@code srcOffset}, to  
     * this accessor's memory starting at {@code dstOffset}.  
     * @param src the accessor from whose memory to copy bytes
     * @param srcOffset the starting offset in the source accessor's memory
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of accessor bounds 
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    public abstract void copyFrom(MemoryAccessor src, long srcOffset, long dstOffset, long length);

     /**
     * Copies {@code length} bytes from the {@code srcBlock} memory block, starting at {@code srcOffset}, to  
     * this memory block starting at {@code dstOffset}.  
     * @param srcBlock the accessor from whose memory to copy bytes
     * @param srcOffset the starting offset in the source memory block
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of accessor bounds 
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        copyFrom(srcBlock, srcOffset, dstOffset, length);
    }
   
    /**
     * Copies {@code length} bytes from the {@code srcArray} byte array, starting at {@code srcOffset}, to  
     * this accessor's memory starting at {@code dstOffset}.  
     * @param srcArray the array from which to copy bytes
     * @param srcOffset the starting offset in the source accessor's memory
     * @param dstOffset the starting offset to which byte are to be copied
     * @param length the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of array or accessor bounds 
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    public abstract void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length);

    /**
     * Sets {@code length} bytes in this accessor's memory, starting at {@code offset}, to the supplied {@code byte}  
     * value.  
     * @param value the value to set
     * @param offset the starting offset in this accessor's memory
     * @param length the number of bytes to set
     * @throws IndexOutOfBoundsException if setting would cause access of data outside of accessor bounds 
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */    
    public abstract void setMemory(byte value, long offset, long length);

    /**
     * Executes the supplied {@code Function}, passing in a {@code Range} object suitable for modifying bytes in 
     * the specified range of offsets within this accessor's memory.  The semantics of the execution will depend on the
     * specific subclass implementing this method.  
     * @param startOffset the starting offset of the range
     * @param rangeLength the number of bytes in the range
     * @param op the function to execute
     * @param <T> the return type of the supplied function
     * @return the object returned from the supplied function
     * @throws IndexOutOfBoundsException if the the specified range of bytes is not within this accessor's heap bounds
     */    
    public abstract <T> T withRange(long startOffset, long rangeLength, Function<Range, T> op);

    /**
     * Executes the supplied {@code Consumer} function, passing in a {@code Range} object
     * suitable for modifying bytes in the specified range of offsets within this memory block. The semantics of the 
     * execution will depend on the specific subclass implementing this method. 
     * @param startOffset the starting offset of the range
     * @param rangeLength the number of bytes in the range
     * @param op the function to execute
     * @throws IndexOutOfBoundsException if the the specified range of bytes is not within this accessors's heap bounds
     */    
    public abstract void withRange(long startOffset, long rangeLength, Consumer<Range> op);

    /**
    * Deallocates the memory referenced by this memory accessor.  The deallocation will be done transactionally
    * for transactional memory accessor instances.
    * @throws HeapException if the memory could not be deallocated
    */
    public abstract void freeMemory();

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

    void rawCopy(MemoryAccessor srcAccessor, long srcOffset, long dstOffset, long length) {
        checkValid();
        srcAccessor.checkValid();
        srcAccessor.checkBoundsAndLength(srcOffset, length);
        checkBoundsAndLength(dstOffset, length);
        MemoryAccessor.uncheckedCopyBlockToBlock(srcAccessor.directAddress() + srcAccessor.metadataSize() + srcOffset, directAddress() + metadataSize() + dstOffset, length);
    }

    void rawCopyFromMemoryBlock(MemoryAccessor srcBlock, long srcOffset, long dstOffset, long length) {
        rawCopy(srcBlock, srcOffset, dstOffset, length);
    }

    void rawCopyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        if (srcOffset < 0 || srcOffset + length > srcArray.length) throw new IndexOutOfBoundsException(MemoryAccessor.outOfBoundsMessage(srcOffset, length));
        checkValid();
        checkBoundsAndLength(dstOffset, length);
        MemoryAccessor.uncheckedCopyFromArray(srcArray, srcOffset, directAddress() + metadataSize() + dstOffset, length);
    }

    void rawSetMemory(byte val, long offset, long length) {
        checkValid();
        checkBoundsAndLength(offset, length);
        MemoryAccessor.uncheckedSetMemory(directAddress() + metadataSize() + offset, val, length);
    }

    <T> T rawWithRange(long startOffset, long length, Function<Range, T> op) {
        Range range = range(startOffset, length);
        T result = op.apply(range);
        range.markInvalid();
        return result;
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

    void durableCopy(MemoryAccessor src, long srcOffset, long dstOffset, long length) {
        durableWithRange(dstOffset, length, (Range range) -> {
            range.copy(src, srcOffset, dstOffset, length);
            return (Void)null;
        });
    }

    void durableCopyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        durableWithRange(dstOffset, length, (Range range) -> {
            range.copyFromArray(srcArray, srcOffset, dstOffset, length);
            return (Void)null;
        });
    }

    void durableSetMemory(byte val, long offset, long length) {
        durableWithRange(offset, length, (Range range) -> {
            range.setMemory(val, offset, length);
            return (Void)null;
        });
    }

    <T> T durableWithRange(long startOffset, long length, Function<Range, T> op) {
        Range range = range(startOffset, length);
        T result = op.apply(range);
        range.flush();
        range.markInvalid();
        return result;
    }

    // Transactional

    void transactionalSetByte(long offset, byte value) {
        transactionalWithRange(offset, 1, (Range range) -> {range.setByte(offset, value); return (Void)null;});
    }

    void transactionalSetShort(long offset, short value) {
        transactionalWithRange(offset, 2, (Range range) -> {range.setShort(offset, value); return (Void)null;});
    }

    void transactionalSetInt(long offset, int value) {
        transactionalWithRange(offset, 4, (Range range) -> {range.setInt(offset, value); return (Void)null;});
    }

    void transactionalSetLong(long offset, long value) {
        transactionalWithRange(offset, 8, (Range range) -> {range.setLong(offset, value); return (Void)null;});
    }

    void transactionalCopy(MemoryAccessor src, long srcOffset, long dstOffset, long length) {
        transactionalWithRange(dstOffset, length, (Range range) -> {
            range.copy(src, srcOffset, dstOffset, length);
            return (Void)null;
        });
    }

    void transactionalCopyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        transactionalWithRange(dstOffset, length, (Range range) -> {
            range.copyFromArray(srcArray, srcOffset, dstOffset, length);
            return (Void)null;
        });
    }

    void transactionalSetMemory(byte val, long offset, long length) {
        transactionalWithRange(offset, length, (Range range) -> {
            range.setMemory(val, offset, length);
            return (Void)null;
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

    long size() {
        return this.size;
    }

    Range range(long startOffset, long length) {
        return new Range(this, startOffset, length);
    }

    void flush(long offset, long length) {
        checkValid();
        checkBoundsAndLength(offset, length);
        internalFlush(offset, length);
    }

    void internalFlush(long offset, long size) {
        if (!ELIDE_FLUSHES) nativeFlush(payloadAddress(offset), size);
    }

    void addToTransaction(long offset, long size) {
        checkValid();
        checkBoundsAndLength(offset, size);
        int result = nativeAddToTransaction(heap().poolHandle(), payloadAddress(offset), size);
        if (result != 2) throw new IllegalStateException("No transaction active.");
    }

    void setPersistentSize(long size) {
        long address = directAddress + SIZE_OFFSET;
        AnyHeap.UNSAFE.putLong(address, size);
        this.size = size;     
    }

    long getPersistentSize() {
        return AnyHeap.UNSAFE.getLong(directAddress + SIZE_OFFSET);
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
        if (offset < 0 || length <= 0 || offset + length > size) throw new IndexOutOfBoundsException(MemoryAccessor.outOfBoundsMessage(offset, length));
    }

    void checkBounds(long offset, long length) {
        if (offset < 0 || offset + length > size) throw new IndexOutOfBoundsException(MemoryAccessor.outOfBoundsMessage(offset, length));
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
