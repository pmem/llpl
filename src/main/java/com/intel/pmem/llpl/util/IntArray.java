/*
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.AnyHeap;
import com.intel.pmem.llpl.AnyMemoryBlock;
import com.intel.pmem.llpl.HeapException;

/**
 * A sequence of {@code int} values.
 * The array can be created using different heap types.
 * Given a persistent heap, the array will store values durably, and given
 * a transactional heap, it will store values transactionally.
 * @since 1.2
 */

public class IntArray {
    private static final int SHIFT_BITS = 2;
    private final AnyMemoryBlock arrayBlock;
    private static final long VERSION_OFFSET = 0;
    private static final long DATA_OFFSET = 8;
    private static final short VERSION = 100;

    /**
     * Returns a previously created array that is associated with the given handle.
     * The supplied handle must be associated with an array of the same type.
     * @param handle the handle of a previously-created array
     * @param heap the heap from which to retrieve the array
     * @return the array associated with the given handle
     * @throws HeapException if the array could not be reaccessed
     */
    public static IntArray fromHandle(AnyHeap heap, long handle) {
        AnyMemoryBlock arrayBlock = heap.memoryBlockFromHandle(handle);
        return new IntArray(heap, arrayBlock);
    }

    /**
     * Creates an array of the given size.
     * The semantics of this method depend on the heap supplied.
     * Given a persistent heap, the array will store values durably, and given
     * a transactional heap will store values transactionally.
     * @param heap the heap on which to allocate the array
     * @param size the number of elements in the array
     * @throws HeapException if the array could not be created
     */
    public IntArray(AnyHeap heap, long size) {
        this.arrayBlock = heap.allocateMemoryBlock(Integer.BYTES * size + DATA_OFFSET);
        arrayBlock.setShort(VERSION_OFFSET, VERSION);
    }

    private IntArray(AnyHeap heap, AnyMemoryBlock arrayBlock) {
        this.arrayBlock = arrayBlock;
    }

    private long elementOffset(long index){
        return DATA_OFFSET + Integer.BYTES * index;
    }

    /**
     * Stores the supplied {@code int} value at {@code index}.
     * The semantics of this method depend on the heap supplied when constructed.
     * @param index the index at which to store the value
     * @param value the value to store
     * @throws ArrayIndexOutOfBoundsException if the operation would cause access of data outside of
     * the array's bounds
     * @throws IllegalStateException if the array has been freed
     */
    public void set(long index, int value) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        arrayBlock.setInt(elementOffset(index), value);
    }

    /**
     * Retrieves the {@code int} value at {@code index}.
     * @param index the index from which to retrieve the value
     * @return the {@code int} value stored at {@code index}
     * @throws ArrayIndexOutOfBoundsException if the operation would cause access of data outside
     * of the array's bounds
     * @throws IllegalStateException if the array has been freed
     */
    public int get(long index) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return arrayBlock.getInt(elementOffset(index));
    }

    /**
     * Returns the number of elements in this array.
     * @return the number of elements in this array
     * @throws IllegalStateException if the array has been freed
     */
    public long size() {
        return (arrayBlock.size() - DATA_OFFSET) >> SHIFT_BITS;
    }

    /**
     * Returns a handle to this array. This stable value can be stored and used later to regain
     * access to the array.
     * @return a handle to this array
     * @throws IllegalStateException if the array has been freed
     */
    public long handle() {
        return arrayBlock.handle();
    }

    /**
     * Deallocates the memory referenced by this array.
     * The semantics of this method depend on the heap supplied when constructed.
     * @throws HeapException if the array could not be freed
     * @throws IllegalStateException if the array has already been freed
     */
    public void free() {
        arrayBlock.freeMemory();
    }

    /**
     * Compares this array to the specified object.  The result is true if
     * and only if the argument is not null and is a {@code IntArray} whose handle is
     * equal to the handle of this array.
     * @return true if the given object is equal
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntArray && ((IntArray)obj).arrayBlock.equals(this.arrayBlock);
    }

    /**
     * Returns a hash code for this array.  Note that this hash code is not computed based on the
     * elements in this array and is only stable for the lifetime of the Java process.
     * @return a hash code for this array
     */
    @Override
    public int hashCode() {
        return arrayBlock.hashCode();
    }
}
