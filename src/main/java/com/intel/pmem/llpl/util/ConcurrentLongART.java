/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import static java.util.Map.Entry;
import java.util.Arrays;
import java.util.Map;
import com.intel.pmem.llpl.*;
import com.intel.pmem.llpl.util.LongArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Comparator;
import java.util.Iterator;

public class ConcurrentLongART implements DynamicSharded<byte[]> {
    private final AnyHeap heap;
    private final Sharder<byte[]> sharder;
    private final long handle;
    private AnyMemoryBlock rootBlock;
    private final long ROOT_BLOCK_SIZE = 18;
    private final long ROOT_BLOCK_SHARD_OFFSET = 0;
    private final long ROOT_BLOCK_RANGE_OFFSET = 8;
    private final long ROOT_BLOCK_POLICY_OFFSET = 16;
    final Comparator<byte[]> comparator;// = ConcurrentLongART::compare;

    public enum Mode {
        Static, 
        Dynamic
    }

    public ConcurrentLongART(AnyHeap heap) {
        this.heap = heap;
        comparator = ConcurrentLongART::compare;
        this.sharder = new StaticSharder<byte[]>(heap, 16, this);
        this.handle = this.sharder.handle(); //encodeRootBlock(heap, this.policy, shardArray);
        LongART.registerAllocationClasses(heap);
        // System.out.println("New Tree. policy is static. handle is "+this.handle);
    }

    public ConcurrentLongART(AnyHeap heap, Mode mode, int maxShards) {
        this.heap = heap;
        comparator = ConcurrentLongART::compare;
        if (mode == Mode.Static) this.sharder = new StaticSharder<byte[]>(heap, maxShards, this);
        else this.sharder = new DynamicSharder<byte[]>(heap, maxShards, this);
        this.handle = this.sharder.handle(); //encodeRootBlock(heap, this.policy, shardArray);
        LongART.registerAllocationClasses(heap);
        // System.out.println("New Tree. policy is " + type + " handle is "+this.handle);
    }

    public ConcurrentLongART(AnyHeap heap, long handle) {
        this.heap = heap;
        this.handle = handle;
        this.comparator = ConcurrentLongART::compare;
        this.sharder = Sharder.rebuild(heap, handle, this);
        if (this.sharder == null) throw new RuntimeException();
        LongART.registerAllocationClasses(heap);
    }

    public Comparator<byte[]> getComparator(){
        return comparator;
    }

    public Function<Object, byte[]> getKeyWriter() {
        return (Object o) -> { return (byte[])o; };
    }

    public Function<byte[], Object> getKeyReader() {
        return (byte[] b) -> { return b; };
    }

    @Override
    public DynamicShardable<byte[]> createDynamicShard() {
        return new LongART(heap);
    }

    @Override
    public DynamicShardable<byte[]> recreateDynamicShard(long handle) {
        return new LongART(heap, handle);
    }
    
    @Override
    public Shardable<byte[]> createShard() {
        return new LongART(heap);
    }

    @Override
    public Shardable<byte[]> recreateShard(long handle) {
        return new LongART(heap, handle);
    }
    
    public static int compare(byte[] a, byte[] b) {
	    byte[] shorter = a.length < b.length ? a : b;
	    int compare = 0;
	    for (int i = 0; i < shorter.length; i++) {
	        if ((compare = Integer.compareUnsigned(Byte.toUnsignedInt(a[i]), Byte.toUnsignedInt(b[i]))) != 0) {
	            break;
	        }
	    }
	    return compare;
    }

    public void put(byte[] radixKey, long value) {
        sharder.shardAndExecute(radixKey, (Shardable<byte[]> s) -> {
            ((LongART)s).put(radixKey, value, (Object v, Long old) -> {return (Long)v;});
        });
    }

    public void put(byte[] radixKey, Object value, BiFunction<Object, Long, Long> biFunc) {
        sharder.shardAndExecute(radixKey, (Shardable<byte[]> s) -> {
            ((LongART)s).put(radixKey, value, biFunc);
        });
    }

    public long get(byte[] key) {
        long ret = (long)sharder.shardAndExecute(key, (Shardable<byte[]> s) -> {
            return ((LongART)s).get(key);
        });
        return ret;
    }

    public Object get(byte[] key, Function<Long, Object> operation) {
        Object ret = sharder.shardAndExecute(key, (Shardable<byte[]> s) -> {
            Long value = ((LongART)s).get(key);
            return operation.apply(value);
        });
        return ret;
    }

    public long size() {
        return sharder.totalEntries();
    }

    public AutoCloseableIterator<LongART.Entry> getEntryIterator(byte[] firstKey, boolean firstInclusive, byte[] lastKey, boolean lastInclusive) {
        return sharder.shardsAndExecute(firstKey, lastKey, (Shardable<byte[]> s) -> {
            return ((LongART)s).getEntryIterator(firstKey, firstInclusive, lastKey, lastInclusive);
        });
    }

    public AutoCloseableIterator<LongART.Entry> getEntryIterator() {
        return sharder.shardsAndExecute(null, null, (Shardable<byte[]> s) -> {
            return ((LongART)s).getEntryIterator();
        });
    }

    public AutoCloseableIterator<LongART.Entry> getHeadEntryIterator(byte[] lastKey, boolean lastInclusive) {
        return sharder.shardsAndExecute(null, lastKey, (Shardable<byte[]> s) -> {
            return ((LongART)s).getHeadEntryIterator(lastKey, lastInclusive);
        });
    }

    public AutoCloseableIterator<LongART.Entry> getTailEntryIterator(byte[] firstKey, boolean firstInclusive) {
        return sharder.shardsAndExecute(firstKey, null, (Shardable<byte[]> s) -> {
            return ((LongART)s).getTailEntryIterator(firstKey, firstInclusive);
        });
    }

    public void delete(byte[] radixKey, Consumer<Long> cleaner) {
        sharder.shardAndExecute(radixKey, (Shardable<byte[]> s) -> {
            ((LongART)s).delete(radixKey, cleaner);
        });
    }

    public void clear(Consumer<Long> cleaner) {
        sharder.forEach((Shardable<byte[]> s) -> { ((LongART)s).clear(cleaner); });
    }

    public void free() {
        sharder.free();
    }

    public void print() {
        // ((DynamicSharder)sharder).printRangeMap();
        ((DynamicSharder)sharder).debug();
    }
    
    public long handle() {
        return handle; 
    }
}
