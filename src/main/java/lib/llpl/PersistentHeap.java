/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package lib.llpl;

import java.util.function.Function;
import java.util.function.Consumer;

/**
 * Manages persistent memory blocks. Use of this heap gives compile-time knowledge that all changes
 * to heap memory are done durably. Modification to heap memory may optionally be done transactionally.
 */
public final class PersistentHeap extends AnyHeap {

    private PersistentHeap(String path, long size) {
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
    public static synchronized PersistentHeap getHeap(String path, long size) {
        PersistentHeap heap = (PersistentHeap)AnyHeap.getHeap(path);
        if (heap == null) {
            heap = new PersistentHeap(path, size);
            AnyHeap.putHeap(path, heap);
        }
        return heap;
    }

    /**
     * Provides access to the heap associated with the specified {@code path}.
     * @param path the path to the heap
     * @return the heap at the specified path or {@code null} if the heap does not exist
     */
    public static synchronized PersistentHeap getHeap(String path) {
        return getHeap(path, 0);
    }

    @Override
    AnyHeap.Metadata initializeMetadata(long size) {
        PersistentMemoryBlock block = allocateMemoryBlock(AnyHeap.Metadata.METADATA_SIZE, true);
        return new AnyHeap.Metadata(this, block, size);
    }

    /**
    Allocates a memory block of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    @param size the size of the memory block in bytes
    @param transactional true if the allocation should be done transactionally
    @return the allocated memory block 
    */
    public PersistentMemoryBlock allocateMemoryBlock(long size, boolean transactional) {
        checkValid();
        return new PersistentMemoryBlock(this, size, transactional);
    }

    /**
    * Allocates a memory block of {@code size} bytes. The allocation is done transactionally.
    * The supplied initializer function is executed, passing a Range object that can be used to 
    * write within the memory block's range of bytes.  Allocating a memory block with an initializer
    * function can be more efficient that separate allocation and initialization. 
    * @param size the size of the memory block in bytes
    * @param initializer a function to be run to initialize the new memory block
    * @return the allocated memory block 
    */
    public PersistentMemoryBlock allocateMemoryBlock(long size, Consumer<Range> initializer) {
        return Transaction.run(this, () -> {
            checkValid();
            PersistentMemoryBlock block = new PersistentMemoryBlock(this, size, true);
            Range range = block.range();
            initializer.accept(range);
            range.markInvalid();
            return block;
        });
    }

    /**
    Allocates an unbounded memory block of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    @param size the size of the memory block in bytes
    @param transactional true if the allocation should be done transactionally
    @return the allocated memory block 
    */
    public PersistentUnboundedMemoryBlock allocateUnboundedMemoryBlock(long size, boolean transactional) {
        checkValid();
        return new PersistentUnboundedMemoryBlock(this, size, transactional);
    }

    /**
    * Allocates an unbounded memory block of {@code size} bytes. The allocation is done transactionally.
    * The supplied initializer function is executed, passing a Range object that can be used to 
    * write within the memory block's range of bytes.  Allocating a memory block with an initializer
    * function can be more efficient that separate allocation and initialization. 
    * @param size the size of the memory block in bytes
    * @param initializer a function to be run to initialize the new memory block
    * @return the allocated memory block 
    */
    public PersistentUnboundedMemoryBlock allocateUnboundedMemoryBlock(long size, Consumer<Range> initializer) {
        return Transaction.run(this, () -> {
            checkValid();
            PersistentUnboundedMemoryBlock block = new PersistentUnboundedMemoryBlock(this, size, true);
            Range range = block.range(0, size);
            initializer.accept(range);
            range.markInvalid();
            return block;
        });
    }

    @Override
    public PersistentMemoryBlock memoryBlockFromHandle(long handle) {
        checkValid();
        checkBounds(handle);
        return new PersistentMemoryBlock(this, poolHandle, handle);
    }

    @Override
    PersistentMemoryBlock internalMemoryBlockFromHandle(long handle) {
        checkValid();
        return new PersistentMemoryBlock(this, poolHandle, handle);
    }

    public PersistentUnboundedMemoryBlock unboundedMemoryBlockFromHandle(long handle) {
        checkValid();
        checkBounds(handle);
        return new PersistentUnboundedMemoryBlock(this, poolHandle, handle);
    }
}
