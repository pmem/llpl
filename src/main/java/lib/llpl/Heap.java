/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

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
    private static HashMap<String, Heap> heaps = new HashMap<>();
    private boolean open;
    private String path;
    private long poolAddress;

    private Heap(String path, long size) {
        if (open) return;
        this.path = path;
        this.open = true;
        poolAddress = nativeOpenHeap(path, size);
    }

    public synchronized static Heap getHeap(String path, long size) {
        Heap heap;
        if (heaps.get(path) == null) {
            heap = new Heap(path, size);
            heaps.put(path, heap);
        } else {
            heap = heaps.get(path);
        }
        return heap;
    }

    public static synchronized Heap getHeap(String path) {
        return heaps.get(path);
    }

    public static boolean exists(String path) {
        if (heaps.get(path) != null) return true;
        else return new File(path).exists();
    }

    long poolAddress() {
        return poolAddress;
    }

    @SuppressWarnings("unchecked")
    public <K extends MemoryBlock.Kind> MemoryBlock<K> allocateMemoryBlock(Class<K> kind, long size) {
        if (kind == Raw.class)
            return (MemoryBlock<K>)new RawMemoryBlock(this, size);
        else if (kind == Flushable.class)
            return (MemoryBlock<K>)new FlushableMemoryBlock(this, size);
        else if (kind == Transactional.class)
            return (MemoryBlock<K>)new TransactionalMemoryBlock(this, size);
        else throw new IllegalArgumentException("Unknown Kind:  " + kind);
    }

    @SuppressWarnings("unchecked")
    public <K extends MemoryBlock.Kind> MemoryBlock<K> memoryBlockFromAddress(Class<K> kind, long addr) {
        if (kind == Raw.class)
            return (MemoryBlock<K>)new RawMemoryBlock(poolAddress, addr);
        else if (kind == Flushable.class)
            return (MemoryBlock<K>)new FlushableMemoryBlock(poolAddress, addr);
        else if (kind == Transactional.class)
            return (MemoryBlock<K>)new TransactionalMemoryBlock(poolAddress, addr);
        else throw new IllegalArgumentException("Unknown Kind:  " + kind);
    }

    @SuppressWarnings("unchecked")
    public <K extends MemoryBlock.Kind> MemoryBlock<K> reallocateMemoryBlock(Class<K> kind, MemoryBlock<K> block, long newSize) {
        if (newSize == 0) {
            freeMemoryBlock(block);
            return null;
        }

        try {
            return Transaction.run(() -> {
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
        if (nativeFree(block.address()) < 0) {
            throw new PersistenceException("Failed to free block");
        }
        block.markInvalid();
    }

    public long getRoot() {
        return nativeGetRoot();
    }

    public void setRoot(long val) {
        if (nativeSetRoot(val) != 0) {
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

    //TODO: check for unneeded synchronized
    synchronized native long nativeAllocate(long size);
    private synchronized native long nativeOpenHeap(String path, long size);
    private synchronized native int nativeSetRoot(long val);
    private synchronized native int nativeRealloc(long offset, long newSize);
    private synchronized native int nativeFree(long addr);
    private native long nativeGetRoot();
}
