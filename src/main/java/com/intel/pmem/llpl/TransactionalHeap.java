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
 * This class supports fixed-position memory accessor classes {@link TransactionalMemoryBlock} and {@link TransactionalCompactMemoryBlock}
 * and repositionable memory accessor classes {@link TransactionalAccessor} and {@link TransactionalCompactAccessor}.<br><br>
 * Use of this heap gives compile-time knowledge that all changes
 * to heap memory are done transactionally.<br><br>
 *
 * See {@link AnyHeap} introduction for a description of different ways of controlling heap size.
 * 
 * @since 1.0
 *
 * @see com.intel.pmem.llpl.AnyHeap   
 */
public final class TransactionalHeap extends AnyHeap {
    static final String HEAP_LAYOUT_ID = "llpl_transactional_heap";

    private TransactionalHeap(String path, long size) {
        super(path, size);
    }

    private TransactionalHeap(String path) {
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
    public static synchronized TransactionalHeap createHeap(String path) {
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

        TransactionalHeap heap = new TransactionalHeap(heapPath, 0);
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
    public static synchronized TransactionalHeap createHeap(String path, long size) {
        if (path == null) throw new IllegalArgumentException("The provided path must not be null");
        if (path.startsWith("/dev/dax")) throw new IllegalArgumentException("The path is invalid for this method");
        if (size != 0L && size < MINIMUM_HEAP_SIZE)
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
        TransactionalHeap heap = new TransactionalHeap(heapPath, heapSize);
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
    public static synchronized TransactionalHeap openHeap(String path) {
        if (path == null) throw new IllegalArgumentException("The provided path must not be null");
        TransactionalHeap heap = (TransactionalHeap)AnyHeap.getHeap(path, getHeapClass("TransactionalHeap"));
        String heapPath = path;
        if (heap == null) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                heapPath = new File(file, AnyHeap.POOL_SET_FILE).getAbsolutePath();
                heap = (TransactionalHeap)AnyHeap.getHeap(heapPath, getHeapClass("TransactionalHeap"));
                if (heap != null) return heap;
            }
            heap = new TransactionalHeap(heapPath);
            AnyHeap.putHeap(heapPath, heap);
        }
        return heap;
    }

    @Override
    public TransactionalAccessor createAccessor() {
        return new TransactionalAccessor(this);
    }

    @Override
    public TransactionalCompactAccessor createCompactAccessor() {
        return new TransactionalCompactAccessor(this);
    }

    @Override
    public long allocateMemory(long size) {
        long allocationSize = size + TransactionalMemoryBlock.METADATA_SIZE; 
        long hd = Transaction.create(this, () -> {
            long handle =  allocateTransactional(allocationSize);
            if (handle == 0) throw new HeapException("Failed to allocate memory of size " + size);
            AnyHeap.UNSAFE.putLong(poolHandle() + handle + AnyMemoryBlock.SIZE_OFFSET, size);
            return handle;
        });
        return hd;
    }

    @Override
    public long allocateCompactMemory(long size) {
        long handle =  Transaction.create(this, () -> allocateTransactional(size));
        if (handle == 0) throw new HeapException("Failed to allocate memory of size " + size);
        return handle;
    }

    @Override
    public TransactionalMemoryBlock allocateMemoryBlock(long size) {
        return new TransactionalMemoryBlock(this, size);
    }

    @Override
    public TransactionalMemoryBlock allocateMemoryBlock(long size, Consumer<Range> initializer) {
        return Transaction.create(this, () -> {
            TransactionalMemoryBlock block = new TransactionalMemoryBlock(this, size);
            Range range = block.range();
            if (initializer == null) throw new IllegalArgumentException("Initializer is null.");
            initializer.accept(range);
            range.markInvalid();
            return block;
        });
    }

    @Override
    public TransactionalCompactMemoryBlock allocateCompactMemoryBlock(long size) {
        return new TransactionalCompactMemoryBlock(this, size);
    }

    @Override
    public TransactionalCompactMemoryBlock allocateCompactMemoryBlock(long size, Consumer<Range> initializer) {
        return Transaction.create(this, () -> {
            TransactionalCompactMemoryBlock block = new TransactionalCompactMemoryBlock(this, size);
            Range range = block.range(0, size);
            if (initializer == null) throw new IllegalArgumentException("Initializer is null.");
            initializer.accept(range);
            range.markInvalid();
            return block;
        });
    }

    @Override
    public TransactionalMemoryBlock memoryBlockFromHandle(long handle) {
        checkBounds(handle, TransactionalMemoryBlock.METADATA_SIZE);
        return new TransactionalMemoryBlock(this, poolHandle(), handle);
    }

    @Override
    public void execute(Runnable op) {
        Transaction.create(this, op);
    }

    @Override
    public <T> T execute(Supplier<T> op) {
        return Transaction.create(this, op);
    }

    @Override
    String getHeapLayoutID() {
        return HEAP_LAYOUT_ID;
    }

    @Override
    TransactionalCompactMemoryBlock internalMemoryBlockFromHandle(long handle) {
        return new TransactionalCompactMemoryBlock(this, poolHandle(), handle);
    }

    @Override
    public TransactionalCompactMemoryBlock compactMemoryBlockFromHandle(long handle) {
        checkBounds(handle);
        return new TransactionalCompactMemoryBlock(this, poolHandle(), handle);
    }
}
