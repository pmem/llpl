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
 * 
 * @since 1.0
 */
public abstract class AnyMemoryBlock extends MemoryAccessor {
    // Constructor
    AnyMemoryBlock(AnyHeap heap, long size, boolean bounded, boolean transactional) {
        super(heap, size, bounded, transactional);
    }

    // Reconstructor
    AnyMemoryBlock(AnyHeap heap, long offset, boolean bounded) {
        super(heap, offset, bounded);
    }

    /**
     * Stores the supplied {@code byte} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    //public abstract void setByte(long offset, byte value);

    /**
     * Stores the supplied {@code short} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
   // public abstract void setShort(long offset, short value);

    /**
     * Stores the supplied {@code int} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    //public abstract void setInt(long offset, int value);

    /**
     * Stores the supplied {@code long} value at {@code offset} within this memory block.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    //public abstract void setLong(long offset, long value);

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
    public void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length){
        super.copyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
    }
    
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
    //public abstract void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length);

    /**
     * Sets {@code length} bytes in this memory block, starting at {@code offset}, to the supplied {@code byte}  
     * value.  
     * @param value the value to set
     * @param offset the starting offset in this memory block
     * @param length the number of bytes to set
     * @throws IndexOutOfBoundsException if setting would cause access of data outside of memory block bounds 
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */    
    //public abstract void setMemory(byte value, long offset, long length);

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

    @Override
    public abstract void copyFrom(MemoryAccessor src, long srcOffset, long dstOffset, long length); 
}
