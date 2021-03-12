/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import java.io.File;
import java.util.function.Supplier;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Manages a heap of memory.  
 * Usable for both volatile and persistent applications.  
 * This class supports fixed-position memory accessor classes {@link MemoryBlock} and {@link CompactMemoryBlock}
 * and repositionable memory accessor classes {@link Accessor} and {@link CompactAccessor}.<br><br>
 * These classes offer full manual control over if and when modifications to memory are flushed for persistence,
 * as well as if and when intended modifications are added to a transaction.  This manual control makes it
 * possible to impelment almost any kind of data consistency policy, at the cost of additional care, on the 
 * part of the developer, to make sure these steps are done where needed. 
 *  
 <br><br>  
 *
 * See {@link AnyHeap} introduction for a description of different ways of controlling heap size.
 * 
 * @since 1.0
 *
 * @see com.intel.pmem.llpl.AnyHeap   
 */
public final class Heap extends AnyHeap {
    static final String HEAP_LAYOUT_ID = "llpl_heap";

    private Heap(String path, long size) {
        super(path, size);
    }
 
    private Heap(String path) {
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
    public static synchronized Heap createHeap(String path) {
        if (path == null) throw new IllegalArgumentException("The provided path must not be null");

        String heapPath;
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
            throw new HeapException("Heap \"" + path + "\" already exists.");

        Heap heap = new Heap(heapPath, 0);
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
    public static synchronized Heap createHeap(String path, long size) {
        if (path == null) throw new IllegalArgumentException("The provided path must not be null.");
        if (path.startsWith("/dev/dax")) throw new IllegalArgumentException("The path is invalid for this method");
        if (size != 0L && size < MINIMUM_HEAP_SIZE)
            throw new HeapException("The Heap size must be at least " + MINIMUM_HEAP_SIZE + " bytes.");
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
            else throw new HeapException("The path \"" + path + "\" does not exist or is not a file.");
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
                throw new HeapException("Heap \"" + path + "\" already exists.");
            }
            heapPath = path;
            heapSize = size;
        }
        if (AnyHeap.getHeap(heapPath))
            throw new HeapException("Heap \"" + path + "\" already exists.");

        Heap heap = new Heap(heapPath, heapSize);
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
    public static synchronized Heap openHeap(String path) {
        if (path == null) throw new IllegalArgumentException("The provided path must not be null.");
        Heap heap = (Heap)AnyHeap.getHeap(path, getHeapClass("Heap"));
        String heapPath = path;
        if (heap == null) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                heapPath = new File(file, AnyHeap.POOL_SET_FILE).getAbsolutePath();
                heap = (Heap)AnyHeap.getHeap(heapPath, getHeapClass("Heap"));
                if (heap != null) return heap;
            }
            heap = new Heap(heapPath);
            AnyHeap.putHeap(heapPath, heap);
        }
        return heap;
    }

    @Override
    public Accessor createAccessor() {
        return new Accessor(this);
    }

    @Override
    public CompactAccessor createCompactAccessor() {
        return new CompactAccessor(this);
    }

    /**
    * Allocates memory of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    * @param size the number of bytes to allocate
    * @param transactional true if the allocation should be done transactionally
    * @return a handle to the allocated memory 
    * @throws HeapException if the memory could not be allocated
    */
    public long allocateMemory(long size, boolean transactional) {
        long allocationSize = size + MemoryBlock.METADATA_SIZE; 
        Supplier<Long> body = () -> {
            long handle =  transactional ? allocateTransactional(allocationSize) : allocateAtomic(allocationSize);
            if (handle == 0) throw new HeapException("Failed to allocate memory of size " + size);
            long address = poolHandle() + handle +AnyMemoryBlock.SIZE_OFFSET;
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
    * @param transactional true if the allocation should be done transactionally
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
    * @param transactional true if the allocation should be done transactionally
    * @return the allocated memory block 
    * @throws HeapException if the memory block could not be allocated
    */
    public MemoryBlock allocateMemoryBlock(long size, boolean transactional) {
        return new MemoryBlock(this, size, transactional);
    }

    @Override
    public MemoryBlock allocateMemoryBlock(long size) {
        return new MemoryBlock(this, size, false);
    }

    @Override
    public MemoryBlock allocateMemoryBlock(long size, Consumer<Range> initializer) {
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
    public MemoryBlock allocateMemoryBlock(long size, boolean transactional, Consumer<Range> initializer) {
        MemoryBlock block = new MemoryBlock(this, size, transactional);
        Range range = block.range();
        initializer.accept(range);
        range.markInvalid();
        return block;
    }

    @Override
    public CompactMemoryBlock allocateCompactMemoryBlock(long size, Consumer<Range> initializer) {
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
    * @throws HeapException if the memory block could not be allocated
    */
    public CompactMemoryBlock allocateCompactMemoryBlock(long size, boolean transactional, Consumer<Range> initializer) {
        CompactMemoryBlock block = new CompactMemoryBlock(this, size, transactional);
        Range range = block.range(0, size);
        initializer.accept(range);
        range.markInvalid();
        return block;
    }

    /**
    * Allocates a compact memory block of {@code size} bytes. The allocation may be done transactionally or non-transactionally.
    * @param size the size of the memory block in bytes
    * @param transactional if true, the allocation will be done transactionally
    * @return the allocated memory block 
    * @throws HeapException if the memory block could not be allocated
    */
    public CompactMemoryBlock allocateCompactMemoryBlock(long size, boolean transactional) {
        return new CompactMemoryBlock(this, size, transactional);
    }

    @Override
    public CompactMemoryBlock allocateCompactMemoryBlock(long size) {
        return new CompactMemoryBlock(this, size, false);
    }

    @Override
    public MemoryBlock memoryBlockFromHandle(long handle) {
        checkBounds(handle, MemoryBlock.METADATA_SIZE);
        return new MemoryBlock(this, poolHandle(), handle);
    }

    @Override
    public void execute(Runnable op) {
        op.run();
    }

    @Override
    public <T> T execute(Supplier<T> op) {
        return op.get();
    }

    @Override
    CompactMemoryBlock internalMemoryBlockFromHandle(long handle) {
        return new CompactMemoryBlock(this, poolHandle(), handle);
    }

    @Override
    String getHeapLayoutID() {
        return HEAP_LAYOUT_ID;
    }

    @Override	
    public CompactMemoryBlock compactMemoryBlockFromHandle(long handle) {
        checkBounds(handle);
        return new CompactMemoryBlock(this, poolHandle(), handle);
    }
}
