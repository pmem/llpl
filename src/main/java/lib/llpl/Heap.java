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
    private String path;
    private long poolAddress;
    private long size;

    private Heap(String path, long size) {
        this.path = path;
        this.size = size;
        this.open = true;
        poolAddress = nativeOpenHeap(path, size);
    }

    public static synchronized Heap getHeap(String path, long size) {
        Heap heap = heaps.get(path);
        if (heap == null) {
            heap = new Heap(path, size);
            heaps.put(path, heap);
        } 
        return heap;
    }

    public static synchronized Heap getHeap(String path) {
        return heaps.get(path);
    }

    public static boolean freeHeap(String path) {
        boolean result = false;
        if (exists(path)) {
            result = new File(path).delete();
        }
        return result;
    }

    public static boolean exists(String path) {
        return heaps.get(path) != null || new File(path).exists();
    }

    public long size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public <K extends MemoryBlock.Kind> MemoryBlock<K> allocateMemoryBlock(Class<K> kind, long size) {
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
        if (kind == Unbounded.class)
            return (MemoryBlock<K>)new UnboundedMemoryBlock(this, poolAddress, address);
        if (kind == Raw.class)
            return (MemoryBlock<K>)new RawMemoryBlock(this, poolAddress, address);
        else if (kind == Flushable.class)
            return (MemoryBlock<K>)new FlushableMemoryBlock(this, poolAddress, address);
        else if (kind == Transactional.class)
            return (MemoryBlock<K>)new TransactionalMemoryBlock(this, poolAddress, address);
        else if (kind == Unbounded.class)
            return (MemoryBlock<K>)new UnboundedMemoryBlock(this, poolAddress, address);
        else throw new IllegalArgumentException("Unsupported Kind:  " + kind);
    }

    @SuppressWarnings("unchecked")
    public <K extends MemoryBlock.Kind> MemoryBlock<K> reallocateMemoryBlock(Class<K> kind, MemoryBlock<K> block, long newSize) {
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
            throw new PersistenceException("Failed to reallocate MemoryBlock of size " + newSize);
        }
    }

    public void freeMemoryBlock(MemoryBlock<?> block) {
        if (nativeFree(poolAddress, block.directAddress()) < 0) {
            throw new PersistenceException("Failed to free block");
        }
        block.markInvalid();
    }

    public long getRoot() {
        return nativeGetRoot(poolAddress);
    }

    public void setRoot(long val) {
        if (nativeSetRoot(poolAddress, val) != 0) {
            throw new PersistenceException("Failed to set root to " + val);
        }
    }

    public void copyMemory(MemoryBlock<?> srcBlock, long srcOffset, MemoryBlock<?> dstBlock, long dstOffset, long length) {
        dstBlock.copyFromMemory(srcBlock, srcOffset, dstOffset, length);
    }

    public void copyToArray(MemoryBlock<?> srcBlock, long srcOffset, byte[] dstArray, int dstOffset, int length) {
        long srcAddress = srcBlock.directAddress() + srcBlock.baseOffset() + srcOffset;
        long dstAddressOffset = UNSAFE.ARRAY_BYTE_BASE_OFFSET + UNSAFE.ARRAY_BYTE_INDEX_SCALE * dstOffset;
        UNSAFE.copyMemory(null, srcAddress, dstArray, dstAddressOffset, length);
    }

    public void copyFromArray(byte[] srcArray, int srcOffset, MemoryBlock<?> dstBlock, long dstOffset, int length) {
        dstBlock.copyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    public void setMemory(MemoryBlock<?> block, byte val, long offset, long length) {
        block.setMemory(val, offset, length);
    }

    long poolAddress() {
        return poolAddress;
    }

    long allocate(long size) {
        return nativeAllocate(poolAddress, size);
    }

    //TODO: check for unneeded synchronized
    private static synchronized native long nativeAllocate(long poolAddress, long size); // looks thread-safe
    private static synchronized native long nativeOpenHeap(String path, long size);      // keep synchronized
    private static synchronized native int nativeSetRoot(long poolAddress, long val);    // keep synchronized
    private static synchronized native int nativeFree(long poolAddress, long addr);      // looks thread-safe
    private static native long nativeGetRoot(long poolAddress);                          // keep synchronized
}
