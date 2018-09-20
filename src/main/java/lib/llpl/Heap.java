/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package lib.llpl;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import sun.misc.Unsafe;

public class Heap {
    static {
        System.loadLibrary("llpl");
        try {
            java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe)f.get(null);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to initialize UNSAFE.");
        }
    }

    static Unsafe UNSAFE;
    private static final Map<String, Heap> heaps = new HashMap<>();
    private boolean open;
    private final String path;
    private boolean valid;
    private final long poolHandle;
    private final long poolId;
    private final long size;
    private static int[] allocationClasses;
    private static final int CUSTOM_INDEX = 16;

    private Heap(String path, long size) {
        this.path = path;
        this.open = true;
        loadAllocationClasses();
        this.poolHandle = nativeOpenHeap(path, size, allocationClasses);
        if (size == 0) this.size = nativePoolSize(path);
        else this.size = size;
        this.poolId = nativePoolId(poolHandle);
        this.valid = true;
    }

    public static synchronized Heap getHeap(String path, long size) {
        Heap heap = heaps.get(path);
        if (heap == null) {
            heap = new Heap(path, size);
            heaps.put(path, heap);
        }
        return heap;
    }

    private static void loadAllocationClasses() {
        allocationClasses = new int[30];
        //Load User defined allocatoin classes

        int[] userClasses = new int[]{32,60,168,664,2080};
        for (int i = 0; i<userClasses.length; i++) {
            allocationClasses[CUSTOM_INDEX + (i * 2)] = userClasses[i];
        }
    }

    public static synchronized Heap getHeap(String path) {
        return heaps.get(path);
    }

    // TODO: add heap close() method

    public static synchronized boolean freeHeap(String path) {
        boolean result = false;
        if (exists(path)) {
            Heap heap = heaps.get(path);
            if (heap != null) markInvalid(heap);
            heaps.remove(path);
            result = new File(path).delete();
        }
        return result;
    }

    private static synchronized void markInvalid(Heap heap) {
        heap.valid = false;
    }

    private void checkValid() {
        if (!valid) throw new IllegalStateException("Heap is not valid");
    }

    public static synchronized boolean exists(String path) {
        return heaps.get(path) != null && new File(path).exists();
    }

    public long size() {
        checkValid();
        return size;
    }

    public void checkBounds(long address) {
        checkValid();
        if (address <= 0 || address >= size) {
            throw new IllegalArgumentException("Address is outside of heap bounds.");
        }
    }

    @SuppressWarnings("unchecked")
    public <K extends MemoryBlock.Kind> MemoryBlock<K> allocateMemoryBlock(Class<K> kind, long size) {
        if (kind == Raw.class)
        checkValid();
        if (kind == Unbounded.class)
            return (MemoryBlock<K>)new UnboundedMemoryBlock(this, size);
        else if (kind == Raw.class)
            return (MemoryBlock<K>)new RawMemoryBlock(this, size);
        else if (kind == Flushable.class)
            return (MemoryBlock<K>)new FlushableMemoryBlock(this, size);
        else if (kind == Transactional.class)
            return (MemoryBlock<K>)new TransactionalMemoryBlock(this, size);
        else throw new IllegalArgumentException("Unsupported Kind:  " + kind);
    }

    @SuppressWarnings("unchecked")
    public <K extends MemoryBlock.Kind> MemoryBlock<K> memoryBlockFromAddress(Class<K> kind, long address) {
        checkValid();
        checkBounds(address);
        if (kind == Unbounded.class)
            return (MemoryBlock<K>)new UnboundedMemoryBlock(this, poolHandle, address);
        if (kind == Raw.class)
            return (MemoryBlock<K>)new RawMemoryBlock(this, poolHandle, address);
        else if (kind == Flushable.class)
            return (MemoryBlock<K>)new FlushableMemoryBlock(this, poolHandle, address);
        else if (kind == Transactional.class)
            return (MemoryBlock<K>)new TransactionalMemoryBlock(this, poolHandle, address);
        else if (kind == Unbounded.class)
            return (MemoryBlock<K>)new UnboundedMemoryBlock(this, poolHandle, address);
        else throw new IllegalArgumentException("Unsupported Kind:  " + kind);
    }

    @SuppressWarnings("unchecked")
    public <K extends MemoryBlock.Kind> MemoryBlock<K> reallocateMemoryBlock(Class<K> kind, MemoryBlock<K> block, long newSize) {
        checkValid();
        if (newSize == 0) {
            freeMemoryBlock(block);
            return null;
        }

        try {
            return Transaction.run(this, () -> {
                MemoryBlock<K> result = allocateMemoryBlock(kind, newSize);
                result.copyFromMemory(block, 0, 0, Math.min(newSize, block.size()));
                if (block != null) freeMemoryBlock(block);
                return result;
            });
        }
        catch (Exception e) {
            throw new PersistenceException("Failed to reaellocate MemoryBlock of size " + newSize, e);
        }
    }

    public void freeMemoryBlock(MemoryBlock<?> block) {
        checkValid();
        if (nativeFree(poolHandle, block.directAddress()) < 0) {
            throw new PersistenceException("Failed to free block.");
        }
        block.markInvalid();
    }

    public long getRoot() {
        checkValid();
        return nativeGetRoot(poolHandle);
    }

    public void setRoot(long val) {
        checkValid();
        if (nativeSetRoot(poolHandle, val) != 0) {
            throw new PersistenceException("Failed to set root to " + val);
        }
    }

    // TODO: redundant with block function, consider removing
    public void copyMemory(MemoryBlock<?> srcBlock, long srcOffset, MemoryBlock<?> dstBlock, long dstOffset, long length) {
        checkValid();
        dstBlock.copyFromMemory(srcBlock, srcOffset, dstOffset, length);
    }

    // available on MemoryBlock, consider removing
    public void copyToArray(MemoryBlock<?> srcBlock, long srcOffset, byte[] dstArray, int dstOffset, int length) {
        checkValid();
        srcBlock.checkRange(srcOffset, length);
        if (dstOffset < 0 || dstOffset + length > dstArray.length) throw new IndexOutOfBoundsException("array index out of bounds.");
        MemoryBlock.rawCopyToArray(srcBlock.directAddress() + srcBlock.baseOffset() + srcOffset, dstArray, dstOffset, length);
    }

    // TODO: redundant with block function, consider removing
    public void copyFromArray(byte[] srcArray, int srcOffset, MemoryBlock<?> dstBlock, long dstOffset, int length) {
        checkValid();
        dstBlock.copyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    // TODO: redundant with block function, consider removing
    public void setMemory(MemoryBlock<?> block, byte val, long offset, long length) {
        checkValid();
        block.setMemory(val, offset, length);
    }

    long poolHandle() {
        return poolHandle;
    }

    long[] allocate(long size) {
        long[] addr_size_pair = getAllocationClassIndex(size);
        addr_size_pair[0] = nativeAllocate(poolHandle, size, (int) addr_size_pair[0]);
        return addr_size_pair;
        //return nativeAllocate(poolHandle, size, 0);
    }

    long[] getAllocationClassIndex(long size) {
        // first check custom classes starting
        int custom_unit_size=0;
        int custom_id=0;
        long[] index_size_pair = new long[2];
        for (int i = CUSTOM_INDEX; i < allocationClasses.length; i+=2) {
            custom_unit_size = allocationClasses[i];
            custom_id = allocationClasses[i + 1];
            if (custom_unit_size == size) {
                index_size_pair[0] = custom_id;
                index_size_pair[1] = custom_unit_size;
                return index_size_pair;
            }
            if (custom_unit_size > size)
                break;
        }


        // if no specific allocation class exists for requested size and size > 128,
        // let pmdk take care of it

        if (size >= 128) {
            index_size_pair[1] = size;
            return index_size_pair;
        }

        // find closest built-in allocation class
        //  starting from the largest less than 128
        int closest_builtin_size=128, builtin_id=0;
        for (int j = CUSTOM_INDEX - 1; j >= 0; j--) {
            if (allocationClasses[j] == 0) continue;
            if ((8 * (j + 1)) < size)
                break;
            closest_builtin_size = (8 * (j + 1));
            builtin_id = allocationClasses[j];
            if (closest_builtin_size == size) {
                index_size_pair[0] = builtin_id;
                index_size_pair[1] = size;
                return index_size_pair;
            }
        }

        if (closest_builtin_size > custom_unit_size) {
            index_size_pair[0] = custom_id;
            index_size_pair[1] = custom_unit_size;
            return index_size_pair;
        }
        else {
            index_size_pair[0] = builtin_id;
            index_size_pair[1] = closest_builtin_size;
            return index_size_pair;
        }
    }

    long directAddress(long offset) {
        //return poolHandle + offset; TODO: validate use of poolHandle this way as optimization
        return nativeDirectAddress(poolId, offset);
    }

    private static native long nativeAllocate(long poolHandle, long size, int class_index);
    private static native int nativeFree(long poolHandle, long addr);
    private static synchronized native long nativeOpenHeap(String path, long size, int[] allocationClasses);
    private static synchronized native int nativeSetRoot(long poolHandle, long val);
    private static synchronized native long nativeGetRoot(long poolHandle);
    private static native long nativePoolId(long poolHandle);
    private static native long nativeDirectAddress(long poolId, long offset);
    private static native long nativeHeapSize(long poolHandle);
    private static native long nativePoolSize(String path);
}
