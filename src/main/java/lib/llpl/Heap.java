/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package lib.llpl;

/**
 * Manages memory blocks. Can be used for volatile and persistent applications.  
 */
public final class Heap extends AnyHeap {

    private Heap(String path, long size) {
        super(path, size);
    }
 
    /**
     * Provides access to the heap associated with the specified {@code path}. If the heap already exists, the {@code size}
     * parameter is ignored and the heap is retrieved.  If the heap does not already exist, it is created
     * with the supplied {@code size}. 
     * @param path the path to the heap
     * @param size the number of bytes to allocate for the heap
     * @return the heap at the specified path
     */
    public static synchronized Heap getHeap(String path, long size) {
        Heap heap = (Heap)AnyHeap.getHeap(path);
        if (heap == null) {
            heap = new Heap(path, size);
            AnyHeap.putHeap(path, heap);
        }
        return heap;
    }

    /**
     * Provides access to the heap associated with the specified {@code path}.
     * @param path the path to the heap
     * @return the heap at the specified path or {@code null} if the heap does not exist
     */
    public static synchronized Heap getHeap(String path) {
        return getHeap(path, 0);
    }

    @Override
    AnyHeap.Metadata initializeMetadata(long size) {
        MemoryBlock block = allocateMemoryBlock(AnyHeap.Metadata.METADATA_SIZE, true);
        return new AnyHeap.Metadata(this, block, size);
    }

    /**
    Allocates a memory block of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    @param size the size of the memory block in bytes
    @param transactional true if the allocation should be done transactionally
    @return the allocated memory block 
    */
    public MemoryBlock allocateMemoryBlock(long size, boolean transactional) {
        checkValid();
        return new MemoryBlock(this, size, transactional);
    }

    /**
    Allocates an unbounded memory block of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    @param size the size of the memory block in bytes
    @param transactional true if the allocation should be done transactionally
    @return the allocated memory block 
    */
    public UnboundedMemoryBlock allocateUnboundedMemoryBlock(long size, boolean transactional) {
        return new UnboundedMemoryBlock(this, size, transactional);
    }

    @Override
    public MemoryBlock memoryBlockFromHandle(long handle) {
        checkValid();
        checkBounds(handle);
        return new MemoryBlock(this, poolHandle, handle);
    }

    @Override
    MemoryBlock internalMemoryBlockFromHandle(long handle) {
        checkValid();
        return new MemoryBlock(this, poolHandle, handle);
    }

   public UnboundedMemoryBlock unboundedMemoryBlockFromHandle(long handle) {
        checkValid();
        checkBounds(handle);
        return new UnboundedMemoryBlock(this, poolHandle, handle);
    }

    void freeMemoryBlock(MemoryBlock block, boolean transactional) {
        super.freeMemoryBlock(block, transactional);
    }
}
