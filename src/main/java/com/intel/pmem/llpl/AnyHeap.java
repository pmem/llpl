/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.function.Supplier;
import java.util.function.Consumer;
import sun.misc.Unsafe;

/**
 * The base class for all heap classes.  A heap contains memory that can be allocated, then accessed, and deallocated if no longer needed.<br><br>  
 * Heap {@code createHeap()} factory methods accept a {@code String} path argument that specifies the identity of the heap and an optional
 * {@code long} size argument. There are 5 ways to configure the size of a heap:<br><br>
 *
 * 1. fixed size -- the path argument is a file path and a supplied size arugument sets both the minimum and 
 * maximum size of the heap.<br>
 * 2. growable -- the path argument is a file path and the heap size starts with size {@code MINIMUM_HEAP_SIZE}, growing 
 * in size as needed up to the available memory.<br>
 * 3. growable with limit -- the path argument is a file path and the heap size starts with size {@code MINIMUM_HEAP_SIZE},
 * growing in size as needed up to a maximum size set by the supplied size argument.<br>
 * 4. DAX device -- the path argument is DAX device name and the size of the dax device sets both the minimum and
 * maximum size of the heap.<br>
 * 5. fused memory pool -- the path argument points to a memory pool configuration file that describes DAX
 * devices [EXPERIMENTAL] or file systems to be fused for use with a single heap.  The combined memory sizes
 * of devices or file systems sets both the minimum and maximum size of the heap.<br><br>  
 * 
 * A previously created heap can be re-opened after a restart using the {@code openHeap()} method which accepts 
 * the {@code String} path argument that was used when the heap was created.<br><br>
 * 
 * For allocating memory, there is a choice of regular allocations which store 
 * the allocated size, or compact allocations which do not store the allocated size, thus saving space. 
 * Some memory allocation methods return a memory block object, which may be used directly to access the allocated memory.
 * An instance of a memory block object always refers to the same allocated memory.  Other memory allocation
 * methods return a {@code long} handle to memory.  This handle can be bound to a memory block object as a separate 
 * step using one of the {@code memoryBlockFromHandle} methods. Alternately, a handle can be bound to a repositionable 
 * accessor object (created with the {@code createAccessor} or {@code createCompactAccessor} method) by calling the 
 * accessor's {@code handle(long handle)} method.  You can retrieve any memory accessor's handle by calling
 * its {@code handle()} method.  
 * 
 * @since 1.0
 *
 * @see com.intel.pmem.llpl.Heap   
 * @see com.intel.pmem.llpl.PersistentHeap   
 * @see com.intel.pmem.llpl.TransactionalHeap   
 */
public abstract class AnyHeap {
    private static final int TOTAL_ALLOCATION_CLASSES = 40;
    private static final int USER_CLASS_INDEX = 15;
    private static final int MAX_USER_CLASSES = (TOTAL_ALLOCATION_CLASSES - USER_CLASS_INDEX) / 2;
    static Unsafe UNSAFE;
    private static final Map<String, AnyHeap> heaps = new ConcurrentHashMap<>();
    private static final long HEAP_VERSION = 1100;
    private static final long MIN_HEAP_VERSION = 900;

    /**
    * The minimum size for a heap, in bytes. Attempting to create a heap with a size smaller that this will throw an 
    * {@code IllegalArgumentException}.
    */
    public static final long MINIMUM_HEAP_SIZE;
    static {
        Util.loadLibrary();
        try {
            java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe)f.get(null);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to initialize UNSAFE.");
        }
        MINIMUM_HEAP_SIZE = nativeMinHeapSize();
    }

    private boolean open;
    private final String path;
    private boolean valid;
    private final long poolHandle;
    private long size;
    private SortedMap<Long, Integer> userSizes;
    private long[] allocationClasses;
    private Metadata metadata;
    static final String POOL_SET_FILE = "myobjpool.set";

    AnyHeap(String path, long requestedSize) {
        this.path = path;
        userSizes = new TreeMap<Long, Integer>();
        allocationClasses = new long[TOTAL_ALLOCATION_CLASSES];
        poolHandle = nativeCreateHeap(path, requestedSize, allocationClasses, this.getHeapLayoutID());
        if (poolHandle == 0) throw new HeapException("Failed to create heap.");
        valid = true;
        this.size = nativeProbeHeapSize(poolHandle, this.size);
        metadata = Metadata.create(this);
        open = true;
    }

    AnyHeap(String path) {
        this.path = path;
        userSizes = new TreeMap<Long, Integer>();
        allocationClasses = new long[TOTAL_ALLOCATION_CLASSES];
        poolHandle = nativeOpenHeap(path, allocationClasses, this.getHeapLayoutID());
        if (poolHandle == 0) throw new HeapException("Failed to open heap.");
        valid = true;
        this.size = nativeProbeHeapSize(poolHandle, this.size);
        metadata = Metadata.open(this);
        if (MIN_HEAP_VERSION > metadata.getVersion()) throw new HeapException("Failed to open heap. Incompatible heap version."); 
        open = true;
    }

    static class Metadata {
        static final long METADATA_SIZE = 64;
        private static final long USER_ROOT_OFFSET = 0;
        private static final long HEAP_VERSION_OFFSET = 8;
        private AnyMemoryBlock metaBlock;

        private Metadata(AnyHeap heap) {
            long metadataHandle = nativeGetRoot(heap.poolHandle());
            this.metaBlock = heap.internalMemoryBlockFromHandle(metadataHandle);
        }

        static Metadata create(AnyHeap heap) {
            Metadata m = new Metadata(heap);
            m.metaBlock.transactionalSetLong(HEAP_VERSION_OFFSET, AnyHeap.HEAP_VERSION);
            return m;
        }

        static Metadata open(AnyHeap heap) {
            return new Metadata(heap);
        }

        public long getUserRoot() {return metaBlock.getLong(USER_ROOT_OFFSET);}
        public void setUserRoot(long value) {metaBlock.transactionalSetLong(USER_ROOT_OFFSET, value);}
        public long getVersion() {return metaBlock.getLong(HEAP_VERSION_OFFSET);}
    }
    
    static boolean getHeap(String path) {
	   return heaps.containsKey(path);
    }

    static AnyHeap getHeap(String path, Class cls) {
    	AnyHeap h = heaps.get(path);
    	try {
    	    cls.cast(h);	
    	} 
	    catch (ClassCastException e){
    	    throw new HeapException("Failed to open heap. wrong heap type (wrong layout)");
    	}
	   return h;
    }

    static Class getHeapClass(String heapName) {
    	Class cls = null;
    	if (heapName.equals("Heap")) cls = Heap.class;
    	else if (heapName.equals("PersistentHeap")) cls = PersistentHeap.class;
    	else if (heapName.equals("TransactionalHeap")) cls = TransactionalHeap.class;
    	return cls;
    }

    static void putHeap(String path, AnyHeap heap) {
        heaps.put(path, heap);
    }

    void close() {
        nativeCloseHeap(poolHandle);
        heaps.remove(path);
        markInvalid(this);
    }

    /**
     * [EXPERIMENTAL] Registers a specific size for optimized allocation of blocks of memory of that size.  Use this for very
     * common allocation sizes to optimize allocation speed and minimize footprint of allocations on the heap.<br> 
     * Separate registrations are required for regular and compact (low overhead) allocations of a given size. Each heap 
     * instance maintains a separate set of registered sizes.  Size registration is itself not persistent so 
     * sizes must be registered each time a heap is opened.
     * @param size the required size of an allocation
     * @param compact true if {@code size} is associated with a compact allocation
     * @return true if size was successfully registered
     * @throws HeapException if the allocation size could not be registered
     */
    public synchronized boolean registerAllocationSize(long size, boolean compact) {
        if (userSizes.size() == MAX_USER_CLASSES) throw new HeapException("Max number of allocation sizes reached.");
        long effectiveSize;
        if (!userSizes.containsKey(effectiveSize = (size + (compact ? 0L : Long.BYTES)))) {
            int id = nativeRegisterAllocationClass(poolHandle, effectiveSize);
            if (id != -1) {
                int i = USER_CLASS_INDEX + (userSizes.size() * 2);
                userSizes.put(effectiveSize, id);
                allocationClasses[i++] = effectiveSize;
                allocationClasses[i] = id;
                return true;
            }
            return false;
        }
        return true;
    }

    static synchronized boolean deleteHeap(String path) {
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
    * Tests for the existence of a heap associated with the given path.
    * @param path the path to the heap
    * @return true if the heap exists
    * @throws IllegalArgumentException if {@code path} is null
    */
    public static synchronized boolean exists(String path) {
        if (path == null) throw new IllegalArgumentException("The provided path must not be null");
        if (getHeap(path)) return true;
        if (path.startsWith("/dev/dax")) {
            int flag = nativeHeapExists(path);
            return (flag == 1) ? true : false;
        }
        File file = new File(path);
        if (file.exists() && file.isFile()) { 
            return true;
        }
        else
            return (file.exists() && file.isDirectory() && new File(path, POOL_SET_FILE).exists());
    }

    /**
     * Returns the size of this heap, in bytes.
     * @return the size of this heap, in bytes
     */
    public long size() {
        return size;
    }

    /**
     * Executes the supplied operation with semantics of the implementing heap subclass.  A 
     * {@code TransactionalHeap} will execute the operation in the context of a transaction.
     * Other heaps currently just execute the operation normally.
     * This method is primarily useful in writing code that will operate correctly with 
     * different kinds of heaps.
     * @param op the operation to execute
     */
    public abstract void execute(Runnable op);

    /**
     * Executes the supplied operation with semantics of the implementing heap subclass. A 
     * {@code TransactionalHeap} will execute the operation in the context of a transaction.
     * Other heaps currently just execute the operation normally.
     * This method is primarily useful in writing code that will operate correctly with 
     * different kinds of heaps.
     * @param <T> the return type of the supplied operation
     * @param op the operation to execute
     * @return the object returned by the operation
     */
    public abstract <T> T execute(Supplier<T> op);

    void checkBounds(long handle) {
        checkBounds(handle, 0);
    }

    void checkBounds(long handle, long length) {
        if (handle <= 0 || outOfBounds(handle + length)) {
            throw new IllegalArgumentException("Handle is invalid for this heap");
        }
    }

    boolean isInBounds(long handle, long length) {
        if (handle <= 0 || outOfBounds(handle + length)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the {@code long} value stored in this heap's root location.  The root location can be
     * used to store any {@code long} value but typically holds the handle of an allocated block of memory.
     * Initially, zero is stored in the root location.
     @return the value of the heap's root location
     */
    public long getRoot() {
        return metadata.getUserRoot();
    }

    /**
     * Stores a {@code long} value in this heap's root location.  The root location can be
     * used to store any {@code long} value but typically holds the handle of an allocated block of memory.
     * @param value the value to be stored
     */
    public void setRoot(long value) {
        metadata.setUserRoot(value);
    }

    /**
    * Allocates memory of {@code size} bytes. For {@code TransactionalHeap}s, the allocation will be done transactionally.
    * @param size the number of bytes to allocate
    * @return a handle to the allocated memory 
    * @throws HeapException if the memory could not be allocated
    */
    public abstract long allocateMemory(long size);

    /**
    * Allocates memory of {@code size} bytes. For {@code TransactionalHeap}s, the allocation will be done transactionally.
    * @param size the number of bytes to allocate
    * @return a handle to the allocated memory 
    * @throws HeapException if the memory could not be allocated
    */
    public abstract long allocateCompactMemory(long size);

   /**
    * Allocates a memory block of {@code size} bytes. For {@code TransactionalHeap}s, the allocation will be done transactionally.
    * @param size the size of the memory block in bytes
    * @return the allocated memory block 
    * @throws HeapException if the memory block could not be allocated
    */
    public abstract AnyMemoryBlock allocateMemoryBlock(long size);

    /**
    * Allocates a compact memory block of {@code size} bytes. For {@code TransactionalHeap}s, the allocation will be done transactionally.
    * @param size the size of the memory block in bytes
    * @return the allocated memory block 
    */
    public abstract AnyMemoryBlock allocateCompactMemoryBlock(long size);

    /**
    * Allocates a memory block of {@code size} bytes. For {@code TransactionalHeap}s, the allocation will be done transactionally.
    * The supplied {@code initializer} function is executed, passing a Range object that can be used to 
    * write within the memory block's range of bytes.  Allocating a memory block with an initializer
    * function can be more efficient than separate allocation and initialization. 
    * @param size the size of the memory block in bytes
    * @param initializer a function to be run to initialize the new memory block
    * @return the allocated memory block 
    * @throws HeapException if the memory block could not be allocated
    */
    public abstract AnyMemoryBlock allocateMemoryBlock(long size, Consumer<Range> initializer);

    /**
    * Allocates a compact (low overhead) memory block of {@code size} bytes. For {@code TransactionalHeap}s,
    * the allocation will be done transactionally.
    * The supplied {@code initializer} function is executed, passing a Range object that can be used to 
    * write within the memory block's range of bytes.  Allocating a memory block with an initializer
    * function can be more efficient than separate allocation and initialization. 
    * @param size the size of the memory block in bytes
    * @param initializer a function to be run to initialize the new memory block
    * @return the allocated memory block 
    * @throws HeapException if the memory block could not be allocated
    */
    public abstract AnyMemoryBlock allocateCompactMemoryBlock(long size, Consumer<Range> initializer);

     /**
     * Creates a new accessor object. In its initial state the accessor refers
     * to no memory and is not usable until it is assigned a handle using the {@code handle(long handle)} method.
     * @return the new accessor object 
     */
    public abstract AnyAccessor createAccessor();

    /**
     * Creates a new accessor object usable with compact allocations. In its initial state the accessor refers
     * to no memory and is not usable until it is assigned a handle using the {@code handle(long handle)} method.
     * @return the new accessor object 
     */
    public abstract AnyAccessor createCompactAccessor();

    /**
    * Returns a memory block that refers to a previous allocation specified by the given handle.
    * The supplied handle must be associated non-compact live allocation this heap.
    * @param handle the handle of a previously-allocated memory
    * @return the memory block associated with the given handle
    * @throws HeapException if the memory block could not be created
    */
    public abstract AnyMemoryBlock memoryBlockFromHandle(long handle);

    /**
    * Returns a memory block that refers to a previous compact allocation specified by the given handle.
    * The supplied handle must be associated compact live allocation this heap.
    * @param handle the handle of a previously-allocated memory
    * @return the compact memory block associated with the given handle
    * @throws HeapException if the memory block could not be created
    */
    public abstract AnyMemoryBlock compactMemoryBlockFromHandle(long handle);

    abstract String getHeapLayoutID();

    abstract AnyMemoryBlock internalMemoryBlockFromHandle(long handle);

    void freeMemory(long directAddress, boolean transactional) {
        int result = transactional ? nativeFree(poolHandle, directAddress) : nativeFreeAtomic(directAddress);
        if (result < 0) {
            throw new HeapException("Failed to free memory.");
        }
    }

    void freeMemoryBlock(AnyMemoryBlock block, boolean transactional) {
        block.checkValid();
        freeMemory(block.directAddress(), transactional);
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
        for (Map.Entry<Long, Integer> e : userSizes.entrySet()) {
            custom_unit_size = e.getKey(); 
            if (custom_unit_size == 0) break;
            custom_id = e.getValue(); 
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

    static void createPoolSetFile(File file, long size) throws IOException {
        File poolFile = new File(file, POOL_SET_FILE);
        long capacity = (size == 0L) ? file.getTotalSpace() : size;
        if (capacity < MINIMUM_HEAP_SIZE)
            throw new HeapException("The partition \"" + file.getAbsolutePath() + "\" must have at least " + MINIMUM_HEAP_SIZE + " bytes");

        Charset charset = Charset.forName("US-ASCII");
        StringBuffer sb = new StringBuffer(); 
        sb.append( "PMEMPOOLSET\n" 
                  + "OPTION SINGLEHDR\n"
                  + capacity + " " + file.getAbsolutePath() + "\n");
        if (poolFile.createNewFile()) {
            BufferedWriter writer = Files.newBufferedWriter(poolFile.toPath(), charset);
            writer.write(sb.toString(), 0, sb.toString().length()); 
            writer.close();
        }
        else throw new HeapException("Heap \"" + file.getAbsolutePath() + "\" already exists");
    }

    boolean outOfBounds(long offset) {
        if (offset < 0) return true;
        if (offset >= size) {
            size = nativeProbeHeapSize(poolHandle, size);
            if (offset >= size) return true;
        } 
        return false;
    }
    
    static long getUsableSize(MemoryAccessor mb) {
        return nativeUsableSize(mb.directAddress());
    }

    static int removePool(String path) {
	   return nativeRemovePool(path);
    }

    private static native long nativeAllocateTransactional(long poolHandle, long size, int class_index);
    private static native long nativeAllocateAtomic(long poolHandle, long size, int class_index);
    private static native int nativeFree(long poolHandle, long addr);
    private static native int nativeFreeAtomic(long addr);
    private static synchronized native long nativeCreateHeap(String path, long size, long[] allocationClasses, String layout);
    private static synchronized native long nativeOpenHeap(String path, long[] allocationClasses, String layout);
    private static synchronized native int nativeRegisterAllocationClass(long poolHandle, long size);
    private static synchronized native void nativeCloseHeap(long poolHandle);
    private static synchronized native long nativeGetRoot(long poolHandle);
    private static native long nativeUsableSize(long addr);
    private static native long nativeDirectAddress(long poolId, long offset);
    static native int nativeHeapExists(String path);
    private static native long nativeHeapSize(String path);
    private static native int nativeRemovePool(String path);
    private static native long nativeProbeHeapSize(long poolId, long currentSize);
    private static native long nativeMinHeapSize();
}
