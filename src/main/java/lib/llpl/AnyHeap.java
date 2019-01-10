/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package lib.llpl;

import java.util.Map;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Arrays;
import java.io.File;
import sun.misc.Unsafe;

/**
 * The base class for all heap classes.  A heap contains bytes associated with physical memory.
 * These bytes are allocated for use in block units.
 */
public abstract class AnyHeap {
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

    private static final int TOTAL_ALLOCATION_CLASSES = 40;
    private static final int USER_CLASS_INDEX = 15;
    private static final int MAX_USER_CLASSES = (TOTAL_ALLOCATION_CLASSES - USER_CLASS_INDEX) / 2;
    static Unsafe UNSAFE;
    private static final Map<String, AnyHeap> heaps = new HashMap<>();
    private boolean open;
    private final String path;
    private boolean valid;
    final long poolHandle;
    private long size;
    private SortedMap<Long, Integer> userSizes;
    private long[] allocationClasses;
    private Metadata metadata;

    AnyHeap(String path, long size) {
        this.path = path;
        userSizes = new TreeMap<Long, Integer>();
        allocationClasses = new long[TOTAL_ALLOCATION_CLASSES];
        poolHandle = nativeOpenHeap(path, size, allocationClasses);
        if (poolHandle == 0) throw new RuntimeException("Failed to open heap.");
        valid = true;
        if (size == 0) this.size = nativeHeapSize(path);
        else this.size = size;
        metadata = initializeMetadata(size);
        this.size = metadata.getHeapSize();
        open = true;
    }

    static class Metadata {
        static final long METADATA_SIZE = 16;
        private static final long USER_ROOT_OFFSET = 0;
        private static final long HEAP_SIZE_OFFSET = 8;
        private AnyMemoryBlock metaBlock;

        public Metadata(AnyHeap heap, AnyMemoryBlock block, long size) {
            long metadataHandle = nativeGetRoot(heap.poolHandle());
            if (metadataHandle == 0) {
                this.metaBlock = block;
                metaBlock.setLong(HEAP_SIZE_OFFSET, size);
                int result = nativeSetRoot(heap.poolHandle(), metaBlock.handle());
                if (result != 0) throw new HeapException("Failed to initialize heap root");
            }
            else {
                this.metaBlock = heap.internalMemoryBlockFromHandle(metadataHandle);
            }
        }

        public long getUserRoot() {return metaBlock.getLong(USER_ROOT_OFFSET);}
        public void setUserRoot(long value) {metaBlock.setLong(USER_ROOT_OFFSET, value);}
        public long getHeapSize() {return metaBlock.getLong(HEAP_SIZE_OFFSET);}
    }

   static synchronized AnyHeap getHeap(String path) {
        return heaps.get(path);
    }

    static synchronized void putHeap(String path, AnyHeap heap) {
        heaps.put(path, heap);
    }

    /**
     * Closes this heap.  
     */
    public void close() {
        checkValid();
        nativeCloseHeap(poolHandle);
        heaps.remove(path);
        markInvalid(this);
    }

    /**
     * [EXPERIMENTAL] Registers a specific memory block size for optimized allocation of blocks of that size.  Use this for very
     * common block sizes to optimize allocation speed and minimize footprint of memory blocks on the heap.<br> 
     * Separate registrations are required for bounded and unbounded memory blocks of a given size. Each heap 
     * instance maintains a separate set of registered sizes.  Size registration is itself not persistent so 
     * sizes must be registered each time a heap is opened.
     * @param size the required size of an allocated memory block
     * @param bounded false if {@code size} is associated with a unbounded memory block
     */
    public synchronized void registerAllocationSize(long size, boolean bounded) {
        if (userSizes.size() == MAX_USER_CLASSES) throw new HeapException("Max allocation size count reached.");
        long effectiveSize = 0L;
        if (!userSizes.containsKey(effectiveSize = (size + (bounded ? Long.BYTES : 0L)))) {
            int id = nativeRegisterAllocationClass(poolHandle, effectiveSize);
            if (id != -1) {
                int i = USER_CLASS_INDEX + (userSizes.size() * 2); 
                userSizes.put(effectiveSize, id); 
                allocationClasses[i++] = effectiveSize; 
                allocationClasses[i] = id; 
            }
        }
    }

    synchronized void deregisterAllocationSize(long size, boolean bounded) {
        userSizes.remove(size + (bounded ? Long.BYTES : 0));
        //loadAllocationClasses();
    }

    /**
    Deletes the heap associated with the suppplied path.
    @param path the path to the heap
    @return true if the heap was deleted
     */
    public static synchronized boolean deleteHeap(String path) {
        boolean result = false;
        if (exists(path)) {
            AnyHeap heap = heaps.get(path);
            if (heap != null) markInvalid(heap);
            heaps.remove(path);
            result = new File(path).delete();
        }
        return result;
    }

    static synchronized void markInvalid(AnyHeap heap) {
        heap.valid = false;
    }

    /**
     * Checks that this heap is in a valid state for use, for example it has not been deleted and is open. 
     * @throws IllegalStateException if the heap is not in a valid state for use
     */
    public void checkValid() {
        if (!valid) throw new IllegalStateException("Heap is not valid");
    }

    /**
    * Tests whether a heap exists.
    * @param path the path to the heap
    * @return true if the heap exists
    */
    public static synchronized boolean exists(String path) {
        return new File(path).exists();
    }

    /**
     * Returns the size, in bytes, of this heap.  
     * @return the size, in bytes, of this heap
     */
    public long size() {
        checkValid();
        return size;
    }

    /**
     * Checks that {@code offset} is within the bounds of this heap. 
     * @param offset The offset to check
     * @throws IndexOutOfBoundsException if the offset is not within this memory block's bounds
     */
    public void checkBounds(long offset) {
        // System.out.format("AnyHeap.checkBounds(%d <= 0 || %d >= %d)\n", offset, offset, size);
        checkValid();
        if (offset <= 0 || offset >= size) {
            throw new IllegalArgumentException("Offset is outside of heap bounds.");
        }
    }

    /**
     * Returns the {@code long} value stored at this heap's root location.  The root location can be
     * used to store any {@code long} value but typically holds the handle of an allocated memory block.
     * Initially, zero is stored in the root location.  
     @return the value of the heap's root location
     */
    public long getRoot() {
        checkValid();
        return metadata.getUserRoot();
    }

    /**
     * Stores a {@code long} value in this heap's root location.  The root location can be
     * used to store any {@code long} value but typically holds the handle of an allocated memory block.  
     * @param value the value to be stored
     */
    public void setRoot(long value) {
        checkValid();
        metadata.setUserRoot(value);
    }

    /**
    * Returns a previously-allocated memory block associated with the given handle.
    * @param handle the handle of a previously-allocated memory block
    * @return the memory block associated with the given handle 
    */
    abstract AnyMemoryBlock memoryBlockFromHandle(long handle); 
    
    /**
    * Returns a previously-allocated unmbounded memory block associated with the given handle.
    * @param handle the handle of a previously-allocated memory block
    * @return the unbounded memory block associated with the given handle 
    */
    abstract AnyMemoryBlock unboundedMemoryBlockFromHandle(long handle); 

    abstract AnyMemoryBlock internalMemoryBlockFromHandle(long handle); 
    abstract Metadata initializeMetadata(long size);

    void freeMemoryBlock(AnyMemoryBlock block) {
        freeMemoryBlock(block, true);
    }

    void freeMemoryBlock(AnyMemoryBlock block, boolean transactional) {
        checkValid();
        int result = transactional ? nativeFree(poolHandle, block.directAddress()) : nativeFreeAtomic(block.directAddress());
        if (result < 0) {
            throw new HeapException("Failed to free block.");
        }
        block.markInvalid();
    }

    long poolHandle() {
        return poolHandle;
    }

    long allocateTransactional(long size) {
        return nativeAllocateTransactional(poolHandle, size, getAllocationClassIndex(size));
    }

    long allocateAtomic(long size) {
        return nativeAllocateAtomic(poolHandle, size, getAllocationClassIndex(size));
    }

    int getAllocationClassIndex(long size) {
        // first check custom classes starting
        long custom_unit_size = 0;
        int custom_id = 0;
        //for (int i = USER_CLASS_INDEX; i < allocationClasses.length - 1; i += 2) {
        for (Map.Entry<Long, Integer> e : userSizes.entrySet()) {
            custom_unit_size = e.getKey(); //allocationClasses[i];
            if (custom_unit_size == 0) break;
            custom_id = e.getValue(); //(int)allocationClasses[i + 1];
            if (custom_unit_size == size) {
                return custom_id;
            }
        //assumes a sorted list of custom sizes
            if (custom_unit_size > size)
                break;
        }

        // if no specific allocation class exists for requested size and size > 128,
        // let pmdk take care of it

        if (size >= 128) return 0;

        // find closest built-in allocation class
        //  starting from the largest less than 128
        int closest_builtin_size = 128, builtin_id = 0;
        for (int j = USER_CLASS_INDEX - 1; j >= 0; j--) {
            if (allocationClasses[j] == 0) continue;
            if ((8 * (j + 1)) < size)
                break;
            closest_builtin_size = (8 * (j + 1));
            builtin_id = (int)allocationClasses[j];
            if (closest_builtin_size == size) {
                return builtin_id;
            }
        }

        if (closest_builtin_size > custom_unit_size) {
            return custom_id;
        }
        else {
            return builtin_id;
        }
    }

    long directAddress(long offset) {
        return poolHandle + offset; 
    }

    private static native long nativeAllocateTransactional(long poolHandle, long size, int class_index);
    private static native long nativeAllocateAtomic(long poolHandle, long size, int class_index);
    private static native int nativeFree(long poolHandle, long addr);
    private static native int nativeFreeAtomic(long addr);
    private static synchronized native long nativeOpenHeap(String path, long size, long[] allocationClasses);
    private static synchronized native int nativeRegisterAllocationClass(long poolHandle, long size);
    private static synchronized native void nativeCloseHeap(long poolHandle);
    private static synchronized native int nativeSetRoot(long poolHandle, long val);
    private static synchronized native long nativeGetRoot(long poolHandle);
    private static native long nativeDirectAddress(long poolId, long offset);
    private static native long nativeHeapSize(String path);
}
