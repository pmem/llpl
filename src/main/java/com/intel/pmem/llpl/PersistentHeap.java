/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Manages a heap of memory.  
 * This class supports fixed-position memory accessor classes {@link PersistentMemoryBlock} and {@link PersistentCompactMemoryBlock}
 * and repositionable memory accessor classes {@link PersistentAccessor} and {@link PersistentCompactAccessor}.<br><br>
 * Use of this heap gives compile-time knowledge that all changes
 * to heap memory are done durably. Modifications to heap memory may optionally be done transactionally.
 *
 * See {@link AnyHeap} introduction for a description of different ways of controlling heap size.
 * 
 * @since 1.0
 *
 * @see com.intel.pmem.llpl.AnyHeap   
  */
public final class PersistentHeap extends AnyHeap {
    static final String HEAP_LAYOUT_ID = "llpl_persistent_heap";

    private PersistentHeap(String path, long size) {
        super(path, size);
    }

    private PersistentHeap(String path) {
        super(path);
    }

    /**
     * Creates a new heap. If {@code path} refers to a directory, a 
     * growable heap will be created.  If {@code path} refers to a DAX device, a heap over that 
     * entire device will be created.  
     * @param path a path to the new heap
     * @return the heap at the specified path
     * @throws IllegalArgumentException if {@code path} is {@code null}
     * @throws HeapException if the heap could not be created
     */
    public static synchronized PersistentHeap createHeap(String path) {
        if (path == null) throw new IllegalArgumentException("The provided path must not be null");
        
        String heapPath;
        // TODO test for Device Dax
        if (path.startsWith("/dev/dax")) {
            heapPath = path;
        }
        else { // Default case: growable heap with no limit
            File file = new File(path);
            if (!file.exists() || !file.isDirectory()) {
                throw new HeapException("The path \"" + path + "\" doesnt exist or is not a directory");
            }
            heapPath = (new File(file, AnyHeap.POOL_SET_FILE)).getAbsolutePath();
            try {
                AnyHeap.createPoolSetFile(file, 0);
            } 
            catch (IOException e) {
                throw new HeapException(e.getMessage());
            }
        }
        if (AnyHeap.getHeap(heapPath))
            throw new HeapException("Heap \"" + path + "\" already exists");

        PersistentHeap heap = new PersistentHeap(heapPath, 0);
        AnyHeap.putHeap(heapPath, heap);
        return heap;
    }

    /**
     * Creates a new heap. If {@code path} refers to a file, a fixed-size heap of {@code size} bytes will be created.
     * If {@code path} refers to a directory, a growable heap, limited to {@code size} bytes, will be created.
     * If {@code size} is {@code 0}, the path will be interpreted as an advanced "fused pool" descriptor file.
     * @param path the path to the heap
     * @param size the number of bytes to allocate for the heap
     * @return the heap at the specified path
     * @throws IllegalArgumentException if {@code path} is {@code null} or if {@code size} 
     * is less than {@code MINIMUM_HEAP_SIZE}
     * @throws HeapException if the heap could not be created
     */
    public static synchronized PersistentHeap createHeap(String path, long size) {
        if (path == null) throw new IllegalArgumentException("The provided path must not be null");
        if (path.startsWith("/dev/dax")) throw new IllegalArgumentException("The path is invalid for this method");
        if (size != 0L && size  < MINIMUM_HEAP_SIZE)
            throw new HeapException("The Heap size must be at least " + MINIMUM_HEAP_SIZE + " bytes");
        File file = new File(path);

        String heapPath;
        long heapSize;
        // Advanced fused case 
        // Size must be 0 and path is an existing poolSetFile
        if (size == 0) {
            if (file.exists() && file.isFile()) {
                heapPath = path;
                heapSize = size;
            } 
            else throw new HeapException("The path \"" + path + "\" does not exist or is not a file");
        } 
        else if (file.exists() && file.isDirectory()) { //growable with limit
            heapPath = (new File(file, AnyHeap.POOL_SET_FILE)).getAbsolutePath();
            try {
                AnyHeap.createPoolSetFile(file, size);
            } 
            catch (IOException e) {
                throw new HeapException(e.getMessage());
            }
            heapSize = 0L;
        } 
        else { //Fixed Heap
            if (file.exists()) {
                throw new HeapException("Heap \"" + path + "\" already exists");
            }
            heapPath = path;
            heapSize = size;
        }
        if (AnyHeap.getHeap(heapPath))
            throw new HeapException("Heap \"" + path + "\" already exists");

        PersistentHeap heap = new PersistentHeap(heapPath, heapSize);
        AnyHeap.putHeap(heapPath, heap);
        return heap;
    }

    /**
     * Opens an existing heap. Provides access to the heap associated with the specified {@code path}.
     * @param path the path to the heap
     * @return the heap at the specified path
     * @throws IllegalArgumentException if {@code path} is {@code null}
     * @throws HeapException if the heap could not be opened
     */
    public static synchronized PersistentHeap openHeap(String path) {
        if (path == null) throw new IllegalArgumentException("The provided path must not be null");
        PersistentHeap heap = (PersistentHeap)AnyHeap.getHeap(path, getHeapClass("PersistentHeap"));
        String heapPath = path;
        if (heap == null) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                heapPath = new File(file, AnyHeap.POOL_SET_FILE).getAbsolutePath();
                heap = (PersistentHeap)AnyHeap.getHeap(heapPath, getHeapClass("PersistentHeap"));
                if (heap != null) return heap;
            }
            heap = new PersistentHeap(heapPath);
            AnyHeap.putHeap(heapPath, heap);
        }
        return heap;
    }

    @Override
    public PersistentAccessor createAccessor() {
        return new PersistentAccessor(this);
    }

    @Override
    public PersistentCompactAccessor createCompactAccessor() {
        return new PersistentCompactAccessor(this);
    }

    /**
    * Allocates memory of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    * @param size the number of bytes to allocate
    * @param transactional if true, the allocation will be done transactionally
    * @return a handle to the allocated memory 
    * @throws HeapException if the memory could not be allocated
    */
    public long allocateMemory(long size, boolean transactional) {
        long allocationSize = size + PersistentMemoryBlock.METADATA_SIZE; 
        Supplier<Long> body = () -> {
            long handle =  transactional ? allocateTransactional(allocationSize) : allocateAtomic(allocationSize);
            if (handle == 0) throw new HeapException("Failed to allocate memory of size " + size);
            long address = poolHandle() + handle + AnyMemoryBlock.SIZE_OFFSET;
            AnyHeap.UNSAFE.putLong(address, size);
            if (!transactional) MemoryAccessor.nativeFlush(address, 8L);
            return handle;
        };
        long handle = transactional ? new Transaction(this).run(body) : body.get();
        return handle;
    }

    @Override
    public long allocateMemory(long size) {
        return allocateMemory(size, false);
    }

    /**
    * Allocates memory of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    * @param size the number of bytes to allocate
    * @param transactional if true, the allocation will be done transactionally
    * @return a handle to the allocated memory 
    * @throws HeapException if the memory could not be allocated
    */
    public long allocateCompactMemory(long size, boolean transactional) {
        long handle =  transactional ? Transaction.create(this, () -> allocateTransactional(size)) : allocateAtomic(size);
        if (handle == 0) throw new HeapException("Failed to allocate memory of size " + size);
        return handle;
    }

    @Override
    public long allocateCompactMemory(long size) {
        return allocateCompactMemory(size, false);
    }

    /**
    * Allocates a memory block of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    * @param size the size of the memory block in bytes
    * @param transactional if true, the allocation will be done transactionally
    * @return the allocated memory block 
    * @throws HeapException if the memory block could not be allocated
    */
    public PersistentMemoryBlock allocateMemoryBlock(long size, boolean transactional) {
        return new PersistentMemoryBlock(this, size, transactional);
    }

    @Override
    public PersistentMemoryBlock allocateMemoryBlock(long size) {
        return new PersistentMemoryBlock(this, size, false);
    }

    @Override
    public PersistentMemoryBlock allocateMemoryBlock(long size, Consumer<Range> initializer) {
        return allocateMemoryBlock(size, false, initializer);
    }

    /**
    * Allocates a memory block of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    * The supplied {@code initializer} function is executed, passing a Range object that can be used to 
    * write within the memory block's range of bytes.  Allocating a memory block with an initializer
    * function can be more efficient than separate allocation and initialization. 
    * @param size the size of the memory block in bytes
    * @param transactional if true, the allocation will be done transactionally
    * @param initializer a function to be run to initialize the new memory block
    * @return the allocated memory block 
    * @throws HeapException if the memory block could not be allocated
    */
    public PersistentMemoryBlock allocateMemoryBlock(long size, boolean transactional, Consumer<Range> initializer) {
        Supplier<PersistentMemoryBlock> body = () -> {
            PersistentMemoryBlock block = new PersistentMemoryBlock(this, size, transactional);
            Range range = block.range();
            if (initializer == null) throw new IllegalArgumentException("Initializer is null.");
            initializer.accept(range);
            if (!transactional) range.flush();
            range.markInvalid();
            return block;
        };
        return transactional ? Transaction.create(this, body) : body.get();
    }

    /**
    * Allocates a compact memory block of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    * @param size the size of the memory block in bytes
    * @param transactional if true, the allocation will be done transactionally
    * @return the allocated memory block 
    * @throws HeapException if the memory block could not be allocated
    */
    public PersistentCompactMemoryBlock allocateCompactMemoryBlock(long size, boolean transactional) {
        return new PersistentCompactMemoryBlock(this, size, transactional);
    }

    @Override
    public PersistentCompactMemoryBlock allocateCompactMemoryBlock(long size) {
        return new PersistentCompactMemoryBlock(this, size, false);
    }

    @Override
    public PersistentCompactMemoryBlock allocateCompactMemoryBlock(long size, Consumer<Range> initializer) {
        return allocateCompactMemoryBlock(size, false, initializer);
    }

    /**
    * Allocates a compact memory block of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    * The supplied {@code initializer} function is executed, passing a Range object that can be used to 
    * write within the memory block's range of bytes.  Allocating a memory block with an initializer
    * function can be more efficient than separate allocation and initialization. 
    * @param size the size of the memory block in bytes
    * @param transactional if true, the allocation will be done transactionally
    * @param initializer a function to be run to initialize the new memory block
    * @return the allocated memory block 
    */
    public PersistentCompactMemoryBlock allocateCompactMemoryBlock(long size, boolean transactional, Consumer<Range> initializer) {
        Supplier<PersistentCompactMemoryBlock> body = () -> {
            PersistentCompactMemoryBlock block = new PersistentCompactMemoryBlock(this, size, transactional);
            Range range = block.range(0, size);
            if (initializer == null) throw new IllegalArgumentException("Initializer is null.");
            initializer.accept(range);
            if (!transactional) range.flush();
            range.markInvalid();
            return block;
        };
        return transactional ? Transaction.create(this, body) : body.get();
    }

    @Override
    public PersistentMemoryBlock memoryBlockFromHandle(long handle) {
        checkBounds(handle, PersistentMemoryBlock.METADATA_SIZE);
        return new PersistentMemoryBlock(this, poolHandle(), handle);
    }

    @Override
    public PersistentCompactMemoryBlock compactMemoryBlockFromHandle(long handle) {
        checkBounds(handle);
        return new PersistentCompactMemoryBlock(this, poolHandle(), handle);
    }

    public void execute(Runnable op) {
        op.run();
    }

    public <T> T execute(Supplier<T> op) {
        return op.get();
    }


    @Override
    String getHeapLayoutID() {
        return HEAP_LAYOUT_ID;
    }

    @Override
    PersistentCompactMemoryBlock internalMemoryBlockFromHandle(long handle) {
        return new PersistentCompactMemoryBlock(this, poolHandle(), handle);
    }
}
