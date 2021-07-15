/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.AnyHeap;
import com.intel.pmem.llpl.AnyMemoryBlock;
import com.intel.pmem.llpl.HeapException;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A concurrent implementation of an Adaptive Radix Tree that uses {@code byte[]} for keys and {@code long} for values.
 * The radix tree can be created using different heap types.
 * Given a persistent heap, the radix tree will store values durably, and given
 * a transactional heap, it will store values transactionally.
 * @since 1.2
 */

public class ConcurrentLongART extends AbstractSharded<byte[]> {
    private final AnyHeap heap;
    private final Sharder<byte[]> sharder;
    final Comparator<byte[]> comparator = ConcurrentLongART::compare;

    enum Mode {
        Static, 
        Dynamic
    }

    /**
     * Creates a new radix tree.
     * The semantics of this method depend on the heap supplied.
     * Given a persistent heap, the radix tree will store values durably, and given
     * a transactional heap will store values transactionally. To reaccess this radix tree, for
     * example after a restart, call {@link LongART#fromHandle(AnyHeap, long)}
     * @param heap the heap on which to allocate the radix tree
     * @param concurrencyLevel the estimated number of concurrently accessing threads. This value may
     * be used as a sizing hint
     * @throws HeapException if the radix tree could not be created
     */
    public ConcurrentLongART(AnyHeap heap, int concurrencyLevel) {
        this.heap = heap;
        this.sharder = new DynamicSharder<byte[]>(heap, concurrencyLevel, this);
    }

    private ConcurrentLongART(AnyHeap heap, long handle) {
        this.heap = heap;
        this.sharder = Sharder.rebuild(heap, handle, this);
        if (this.sharder == null) throw new RuntimeException();
    }

    /**
     * Returns a previously created radix tree that is associated with the supplied handle.
     * The {@code handle} must be that of a radix tree created on the supplied heap.
     * @param handle the handle of a previously-created radix tree
     * @param heap the heap from which to retrieve the radix tree 
     * @return the radix tree
     * @throws HeapException if the radix tree could not be reaccessed
     */
    public static ConcurrentLongART fromHandle(AnyHeap heap, long handle) {
        return new ConcurrentLongART(heap, handle);
    }

    Comparator<byte[]> getComparator(){
        return comparator;
    }

    @Override
    LongART createDynamicShard() {
        return new LongART(heap);
    }

    @Override
    LongART recreateDynamicShard(long handle) {
        return LongART.fromHandle(heap, handle);
    }
    
    @Override
    LongART createShard() {
        return new LongART(heap);
    }

    @Override
    LongART recreateShard(long handle) {
        return LongART.fromHandle(heap, handle);
    }
    
    static int compare(byte[] a, byte[] b) {
	    byte[] shorter = a.length < b.length ? a : b;
	    int compare = 0;
	    for (int i = 0; i < shorter.length; i++) {
	        if ((compare = Integer.compareUnsigned(Byte.toUnsignedInt(a[i]), Byte.toUnsignedInt(b[i]))) != 0) {
	            break;
	        }
	    }
	    return compare;
    }

    /**
     * Retrieves the lowest key in this radix tree.
     * @return the lowest key 
     * @throws NoSuchElementException if the radix tree is empty 
     */
    public byte[] firstKey() {
        return sharder.lowestKey((Shardable<byte[]> s) -> {
            return ((LongART)s).firstKey();
        });
    }

    /**
     * Retrieves the highest key in this radix tree.
     * @return the highest key 
     * @throws NoSuchElementException if the radix tree is empty 
     */
    public byte[] lastKey() {
        return sharder.highestKey((Shardable<byte[]> s) -> {
            return ((LongART)s).lastKey();
        });
    }

    /**
     * Maps the specified key to the specified value.
     * If a mapping already exists for the specified key, the value is replaced.
     * @param key the key to which the specified value is to be mapped
     * @param value the value to be mapped to the specified key
     * @return the previous {@code long} value mapped to the specified key, or zero
     * if there is no previous mapping
     * @throws IllegalArgumentException if the supplied key has zero length
     */    
    public long put(byte[] key, long value) {
        if (key == null || key.length == 0) throw new IllegalArgumentException("Invalid key");
        return (long)sharder.shardAndPut(key, (Shardable<byte[]> s) -> {
            return ((LongART)s).put(key, value, (Object v, Long old) -> {return (Long)v;});
        });
    }
    
    /**
     * Maps the specified key to the specified value.
     * If a mapping already exists for the specified key, the value is replaced.
     * The supplied merge function will be called with the {@code newValue} and current 
     * value. The value returned by the merge function will be stored.
     * @param key the key to which the specified value is to be mapped
     * @param newValue the new value to be passed to the merge function
     * @param mergeFunction the merge function
     * @return the previous {@code long} value mapped to the specified key, or zero 
     * if there is no previous mapping
     * @throws IllegalArgumentException if the supplied value is null or the supplied key 
     * has zero length 
     */
    public long put(byte[] key, Object newValue, BiFunction<Object, Long, Long> mergeFunction) {
        if (key == null || key.length == 0) throw new IllegalArgumentException("Invalid key");
        if (newValue == null) throw new IllegalArgumentException("newValue cannot be null");
        return (long)sharder.shardAndPut(key, (Shardable<byte[]> s) -> {
            return ((LongART)s).put(key, newValue, mergeFunction);
        });
    }

    /**
     * Retrieves the {@code long} value mapped to the supplied key.
     * @param key the key whose mapped value is to be returned 
     * @return the {@code long} value mapped to the supplied key
     * @throws IllegalStateException if {@link ConcurrentLongART#free} has been called on this object
     */
    public long get(byte[] key) {
        if (key == null || key.length == 0) throw new IllegalArgumentException("Invalid key");
        return (long)sharder.shardAndGet(key, (Shardable<byte[]> s) -> {
            return ((LongART)s).get(key);
        });
    }

    /**
     * Returns the number of entries in this radix tree.
     * @return the number of entries
     * @throws IllegalStateException if {@link ConcurrentLongART#free} has been called on this object
     */
    public long size() {
        return sharder.totalEntries();
    }

    /**
     * Returns an ascending-order iterator over entries in this radix tree;
     * the iterator will include entries whose keys range from {@code firstKey} to {@code lastKey}.
     * The returned iterator is designed to be used solely by the thread that calls this method. This 
     * iterator implementation acquires resources which must be released, either by iterating through 
     * all entries, or by calling {@link AutoCloseable#close} on the iterator. 
     * @param firstKey low endpoint of the keys in the returned iterator
     * @param firstInclusive true if the lowest key is to be included in the returned iterator
     * @param lastKey high endpoint of the keys in the returned iterator
     * @param lastInclusive true if the highest key is to be included in the returned iterator
     * @return the iterator 
     * @throws IllegalArgumentException if firstKey or lastKey has zero length
     */
    public AutoCloseableIterator<LongART.Entry> getEntryIterator(byte[] firstKey, boolean firstInclusive, byte[] lastKey, boolean lastInclusive) {
        if (firstKey == null || firstKey.length == 0 || lastKey == null || lastKey.length == 0) throw new IllegalArgumentException();
        return sharder.shardsAndExecute(firstKey, lastKey, (Shardable<byte[]> s) -> {
            return ((LongART)s).getEntryIterator(firstKey, firstInclusive, lastKey, lastInclusive);
        }, false);
    }

    /**
     * Returns an ascending-order iterator over the entries in this radix tree.
     * The returned iterator is designed to be used solely by the thread that calls this method. This 
     * iterator implementation acquires resources which must be released, either by iterating through 
     * all entries, or by calling {@link AutoCloseable#close} on the iterator. 
     * @return the iterator
     */
    public AutoCloseableIterator<LongART.Entry> getEntryIterator() {
        return sharder.shardsAndExecute(null, null, (Shardable<byte[]> s) -> {
            return ((LongART)s).getEntryIterator();
        }, false);
    }

    /**
     * Returns a descending-order iterator over entries in this radix tree;
     * the iterator will include entries whose keys range from {@code firstKey} to {@code lastKey}.
     * The returned iterator is designed to be used solely by the thread that calls this method. This 
     * iterator implementation acquires resources which must be released, either by iterating through 
     * all entries, or by calling {@link AutoCloseable#close} on the iterator. 
     * @param firstKey low endpoint of the keys in the returned iterator
     * @param firstInclusive true if the lowest key is to be included in the returned iterator
     * @param lastKey high endpoint of the keys in the returned iterator
     * @param lastInclusive true if the highest key is to be included in the returned iterator
     * @return the iterator 
     * @throws IllegalArgumentException if firstKey or lastKey has zero length
     */
    public AutoCloseableIterator<LongART.Entry> getReverseEntryIterator(byte[] firstKey, boolean firstInclusive, byte[] lastKey, boolean lastInclusive) {
        if (firstKey == null || firstKey.length == 0 || lastKey == null || lastKey.length == 0) throw new IllegalArgumentException();
        return sharder.shardsAndExecute(firstKey, lastKey, (Shardable<byte[]> s) -> {
            return ((LongART)s).getReverseEntryIterator(firstKey, firstInclusive, lastKey, lastInclusive);
        }, true);
    }

    /**
     * Returns a descending-order iterator over the entries in this radix tree.
     * The returned iterator is designed to be used solely by the thread that calls this method. This 
     * iterator implementation acquires resources which must be released, either by iterating through 
     * all entries, or by calling {@link AutoCloseable#close} on the iterator. 
     * @return the iterator
     */
    public AutoCloseableIterator<LongART.Entry> getReverseEntryIterator() {
        return sharder.shardsAndExecute(null, null, (Shardable<byte[]> s) -> {
            return ((LongART)s).getReverseEntryIterator();
        }, true);
    }

    /**
     * Returns an ascending-order iterator over entries in this radix tree;
     * the iterator will include entries whose keys are lower than (or equal to, 
     * if {@code lastInclusive} is true) {@code lastKey}.
     * The returned iterator is designed to be used solely by the thread that calls this method. This 
     * iterator implementation acquires resources which must be released, either by iterating through 
     * all entries, or by calling {@link AutoCloseable#close} on the iterator. 
     * @param lastKey high endpoint of the keys in the returned iterator
     * @param lastInclusive true if the highest key is to be included in the returned iterator
     * @return the iterator 
     * @throws IllegalArgumentException if lastKey has zero length
     */
    public AutoCloseableIterator<LongART.Entry> getHeadEntryIterator(byte[] lastKey, boolean lastInclusive) {
        if (lastKey == null || lastKey.length == 0) throw new IllegalArgumentException();
        return sharder.shardsAndExecute(null, lastKey, (Shardable<byte[]> s) -> {
            return ((LongART)s).getHeadEntryIterator(lastKey, lastInclusive);
        }, false);
    }

    /**
     * Returns an ascending-order iterator over entries in this radix tree;
     * the iterator will include entries whose keys are higher than (or equal to, 
     * if {@code firstInclusive} is true) {@code firstKey}.
     * The returned iterator is designed to be used solely by the thread that calls this method. This 
     * iterator implementation acquires resources which must be released, either by iterating through 
     * all entries, or by calling {@link AutoCloseable#close} on the iterator. 
     * @param firstKey low endpoint of the keys in the returned iterator
     * @param firstInclusive true if the lowest key is to be included in the returned iterator
     * @return the iterator
     * @throws IllegalArgumentException if lastKey has zero length
     */
    public AutoCloseableIterator<LongART.Entry> getTailEntryIterator(byte[] firstKey, boolean firstInclusive) {
        if (firstKey == null || firstKey.length == 0) throw new IllegalArgumentException();
        return sharder.shardsAndExecute(firstKey, null, (Shardable<byte[]> s) -> {
            return ((LongART)s).getTailEntryIterator(firstKey, firstInclusive);
        }, false);
    }

    /**
     * Removes the mapping for the specified key from this radix tree if present.
     * The semantics of this method depend on the heap supplied when constructed.
     * @param key the key whose mapping is to be removed.
     * @param cleanerFunction this function will be called once for each entry, passing the value of the 
     * entry being removed. This may be particularly useful for performing additional cleanup, in 
     * the case where the values stored in this radix tree are handles
     * @return the removed value or zero if not found
     * @throws IllegalArgumentException if the supplied key has zero length
     */
    public long remove(byte[] key, Consumer<Long> cleanerFunction) {
        if (cleanerFunction == null) throw new NullPointerException("cleaner function cannot be null");
        if (key.length == 0) throw new IllegalArgumentException("Invalid key");
        return (long)sharder.shardAndGet(key, (Shardable<byte[]> s) -> {
            return ((LongART)s).remove(key, cleanerFunction);
        });
    }

    /**
     * Removes all of the entries in this radix tree.
     * The semantics of this method depend on the heap supplied when constructed.
     * @param cleanerFunction this function will be called once for each entry, passing the value of the 
     * entry being removed. This may be particularly useful for performing additional cleanup, in 
     * the case where the values stored in this radix tree are handles
     * @throws IllegalStateException if {@link ConcurrentLongART#free} has been called on this object
     */
    public void clear(Consumer<Long> cleanerFunction) {
        if (cleanerFunction == null) throw new IllegalArgumentException("cleaner function cannot be null");
        sharder.forEach((Shardable<byte[]> s) -> { ((LongART)s).clear(cleanerFunction); });
    }

    /**
     * Deallocates the memory used by this radix tree.
     * The semantics of this method depend on the heap supplied when the radix tree was constructed.
     * @throws HeapException if the radix tree could not be freed
     * @throws IllegalStateException if {@link ConcurrentLongART#free} has been called on this object
     */
    public void free() {
        sharder.free();
    }

    /**
     * Returns a handle to this radix tree. This stable value can be stored and used later to regain
     * access to the radix tree.
     * @return a handle to this radix tree
     * @throws IllegalStateException if {@link ConcurrentLongART#free} has been called on this object
     */
    public long handle() {
        long ret = sharder.handle();
        if (ret == 0) throw new IllegalStateException();
        return ret;
    }

    /**
     * Returns a hash code for this radix tree.  Note that this hash code is not computed based on the 
     * entries in this radix tree and is only stable for the lifetime of the Java process.   
     * @return a hash code for this radix tree 
     */
    @Override
    public int hashCode() {
        AnyMemoryBlock rootBlock = heap.memoryBlockFromHandle(handle());
        return rootBlock.hashCode();
    }

    /**
     * Compares this radix tree to the specified object.  The result is true if and only if the argument is not 
     * null and is a radix tree whose handle is equal to the handle of this radix tree. 
     * @return true if the given object is equal 
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConcurrentLongART)) return false;
        AnyMemoryBlock thisRootBlock = heap.memoryBlockFromHandle(handle());
        AnyMemoryBlock otherRootBlock = heap.memoryBlockFromHandle(((ConcurrentLongART)obj).handle());
        return otherRootBlock.equals(thisRootBlock);
    }
}
