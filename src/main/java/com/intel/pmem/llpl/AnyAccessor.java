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
 * The base class for all repositionable accessor classes.  An accessor refrences a previously allocated block 
 * of memory on a heap. <br><br>
 * All read and write operations specify locations using zero-based offsets that refer
 * to the number of bytes from the beginning of an allocated block of memory. A reference from one block 
 * of memory to another block of memory can be made by storing a handle to the referenced block of memory,
 * available via the {@link com.intel.pmem.llpl.MemoryAccessor#handle()} method.  Given a handle, an accessor can be repositioned to
 * the handle's block of memory using the {@link com.intel.pmem.llpl.AnyAccessor#handle()} method.<br><br>
 * Read and write operations using an invalid accessor (e.g. an accessor that refers to deallocated memory) will throw an {@code IllegalStateException}.
 * The {@link com.intel.pmem.llpl.AnyAccessor#isValid()} method can be used to check if the memory the accessor
 * currently refers to has been freed using the accessor 
 *
 * @since 1.1
 *
 */
public abstract class AnyAccessor extends MemoryAccessor {
    AnyAccessor(AnyHeap heap) {
        super(heap);
    }

    /**
     * Sets this accessor's handle thereby changing the memory that this accessor references.   
     * @param handle The handle to use
     * @throws IllegalArgumentException if {@code handle} is not valid
     * @throws HeapException if the accessor could not be updated
     */
    public abstract void handle(long handle); 

    /**
     * Resets this accessor to its initial state. In its initial state the accessor refers
     * to no memory and is not usable until it is assigned a handle using {@link com.intel.pmem.llpl.AnyAccessor#handle(long)}
     */
    public abstract void resetHandle(); 

    /**
     * Stores the supplied {@code byte} value at {@code offset} within this accessor's block of memory.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    // public abstract void setByte(long offset, byte value);

    /**
     * Stores the supplied {@code short} value at {@code offset} within this accessor's block of memory.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of the accessor's
     * bounds or, for compact accessors, outside of heap bounds
     * @throws IllegalStateException if the accessor is not in a valid state for use
     */
    // public abstract void setShort(long offset, short value);

    /**
     * Stores the supplied {@code int} value at {@code offset} within this accessor's block of memory.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    // public abstract void setInt(long offset, int value);

    /**
     * Stores the supplied {@code long} value at {@code offset} within this accessor's block of memory.  
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of memory block
     * bounds or, for compact memory blocks, outside of heap bounds
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */
    // public abstract void setLong(long offset, long value);

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
    // public abstract void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length);

    /**
     * Sets {@code length} bytes in this memory block, starting at {@code offset}, to the supplied {@code byte}  
     * value.  
     * @param value the value to set
     * @param offset the starting offset in this memory block
     * @param length the number of bytes to set
     * @throws IndexOutOfBoundsException if setting would cause access of data outside of memory block bounds 
     * @throws IllegalStateException if the memory block is not in a valid state for use
     */    
    // public abstract void setMemory(byte value, long offset, long length);
}
