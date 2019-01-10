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
 * Manages transactional memory blocks. Use of this heap gives compile-time knowledge that all changes
 * to heap memory are done transactionally.
 */
public final class TransactionalHeap extends AnyHeap {

    private TransactionalHeap(String path, long size) {
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
    public static synchronized TransactionalHeap getHeap(String path, long size) {
        TransactionalHeap heap = (TransactionalHeap)AnyHeap.getHeap(path);
        if (heap == null) {
            heap = new TransactionalHeap(path, size);
            AnyHeap.putHeap(path, heap);
        }
        return heap;
    }

    /**
     * Provides access to the heap associated with the specified {@code path}.
     * @param path the path to the heap
     * @return the heap at the specified path or {@code null} if the heap does not exist
     */
    public static synchronized TransactionalHeap getHeap(String path) {
        return getHeap(path, 0);
    }

    @Override
    AnyHeap.Metadata initializeMetadata(long size) {
        TransactionalMemoryBlock block = allocateMemoryBlock(AnyHeap.Metadata.METADATA_SIZE);
        return new AnyHeap.Metadata(this, block, size);
    }

    /**
    * Allocates a memory block of {@code size} bytes. The allocation is done transactionally.
    * @param size the size of the memory block in bytes
    * @return the allocated memory block 
    */
    public TransactionalMemoryBlock allocateMemoryBlock(long size) {
        checkValid();
        return new TransactionalMemoryBlock(this, size);
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
    public TransactionalMemoryBlock allocateMemoryBlock(long size, Consumer<Range> initializer) {
        return Transaction.run(this, () -> {
            checkValid();
            TransactionalMemoryBlock block = new TransactionalMemoryBlock(this, size);
            Range range = block.range();
            initializer.accept(range);
            range.markInvalid();
            return block;
        });
    }

    /**
    Allocates an unbounded memory block of {@code size} bytes. The allocation is done transactionally.
    @param size the size of the memory block in bytes
    @return the allocated memory block 
    */
    public TransactionalUnboundedMemoryBlock allocateUnboundedMemoryBlock(long size) {
        checkValid();
        return new TransactionalUnboundedMemoryBlock(this, size);
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
    public TransactionalUnboundedMemoryBlock allocateUnboundedMemoryBlock(long size, Consumer<Range> initializer) {
        return Transaction.run(this, () -> {
            checkValid();
            TransactionalUnboundedMemoryBlock block = new TransactionalUnboundedMemoryBlock(this, size);
            Range range = block.range(0, size);
            initializer.accept(range);
            range.markInvalid();
            return block;
        });
    }

    @Override
    public TransactionalMemoryBlock memoryBlockFromHandle(long handle) {
        checkValid();
        checkBounds(handle);
        return new TransactionalMemoryBlock(this, poolHandle, handle);
    }

    @Override
    TransactionalMemoryBlock internalMemoryBlockFromHandle(long handle) {
        checkValid();
        return new TransactionalMemoryBlock(this, poolHandle, handle);
    }

    public TransactionalUnboundedMemoryBlock unboundedMemoryBlockFromHandle(long handle) {
        checkValid();
        checkBounds(handle);
        return new TransactionalUnboundedMemoryBlock(this, poolHandle, handle);
    }
}
