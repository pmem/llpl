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
 * The base class for all repositionable accessor classes.  An accessor references a previously-allocated block 
 * of memory on a heap. <br><br>
 * All read and write operations specify locations using zero-based offsets that refer
 * to the number of bytes from the beginning of an allocated block of memory. A reference from one block 
 * of memory to another block of memory can be made by storing a handle to the referenced block of memory,
 * available via the {@code handle()} method.  Given a handle, an accessor can be repositioned to
 * the handle's block of memory using the {@code handle(long handle)} method.<br><br>
 * Read and write operations using an invalid accessor (e.g. an accessor whose {@code freeMemory} method has 
 * been called) will throw an {@code IllegalStateException}.
 * The {@code isValid()} method can be used to check if this memory accessor
 * is in a valid state for use. 
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
     * to no memory and is not usable until it is assigned a handle using the {@code handle(long handle)} method.
     */
    public abstract void resetHandle(); 
}
