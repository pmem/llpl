/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.concurrent.ConcurrentMap;
import static java.util.concurrent.ConcurrentMap.Entry;
import static java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.intel.pmem.llpl.util.LongArray;
import java.util.Map;
import com.intel.pmem.llpl.Transaction;
import com.intel.pmem.llpl.AnyHeap;
import com.intel.pmem.llpl.AnyMemoryBlock;
import com.intel.pmem.llpl.util.LongArray;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Random;
import java.util.Iterator;

class StaticSharder<K> implements Sharder<K>{
	private int maxShards;
	private Shardable<K>[] shards;
	private ReentrantLock[] locks;
	private AnyHeap heap;
    private final long handle;
    final String CLASSNAME = "com.intel.pmem.llpl.util.StaticSharder"; 
    private final int CLASSNAME_LENGTH_OFFSET = 0;
    private final int CLASSNAME_OFFSET = 4;
    private final long ROOT_SHARD_OFFSET = CLASSNAME_OFFSET + CLASSNAME.length();
    private final long ROOT_BLOCK_SIZE = ROOT_SHARD_OFFSET + 8;

	private StaticSharder(LongArray shardArray, int maxShards, AnyHeap heap, Shardable<K>[] shards, ReentrantLock[] locks) {
		this.maxShards = maxShards;
		this.heap = heap;
		this.shards = shards;
        this.handle = encodeRootBlock(heap, shardArray);
        this.locks = locks;
	} 

	@SuppressWarnings("unchecked")
    public StaticSharder(AnyHeap heap, long handle, Sharded<K> sharded) {
		this.heap = heap;
        this.handle = handle;
        AnyMemoryBlock block = heap.memoryBlockFromHandle(handle);
        long shardArrayHandle = block.getLong(ROOT_SHARD_OFFSET);
        LongArray shardArray = LongArray.fromHandle(heap, shardArrayHandle); 
        this.maxShards = (int)shardArray.size();
        this.shards = (Shardable<K>[])new Shardable[maxShards];
        for (int i = 0; i < maxShards; i++) {
            shards[i] = sharded.recreateShard(shardArray.get(i));
        } 
		System.out.println("   nShards = " + maxShards);
    }

    public long encodeRootBlock(AnyHeap heap, LongArray shardArray) {
        AnyMemoryBlock rootBlock = heap.allocateMemoryBlock(ROOT_BLOCK_SIZE);
        rootBlock.setInt(CLASSNAME_LENGTH_OFFSET, CLASSNAME.length());
        rootBlock.copyFromArray(CLASSNAME.getBytes(), 0, CLASSNAME_OFFSET, CLASSNAME.length());
        rootBlock.setLong(ROOT_SHARD_OFFSET, shardArray.handle());
        return rootBlock.handle();
    }

	@SuppressWarnings("unchecked")
	public StaticSharder(AnyHeap heap, int maxShards, Sharded<K> sharded) {
        LongArray shardArray = new LongArray(heap, maxShards); 
		Shardable<K>[] shards = new Shardable[maxShards];
        ReentrantLock[] locks = new ReentrantLock[maxShards];
		for (int i = 0; i < maxShards; i++) {
            shards[i] = sharded.createShard();
            locks[i] = new ReentrantLock(true);
            shardArray.set(i, shards[i].handle());
		}
		this.maxShards = maxShards;
		this.heap = heap;
		this.shards = shards;
        this.handle = encodeRootBlock(heap, shardArray);
        this.locks = locks;
    }

    /*@Override
	public Shardable<K>[] shards() {
		return shards;
	}*/

    @Override //should lock?
    public long totalEntries() {
        long size = 0;
        for (int i = 0; i < shards.length; i++) {
           size += shards[i].size();
        }
        return size;
    }

    @Override
	public int shardCount() {
		return shards.length;
	}

    @Override
	public Shardable<K> shard(K key) {
        return shards[Math.abs(key.hashCode()) % maxShards];
	}

    @Override
    public void shardAndExecute(K key, Consumer<Shardable<K>> c){
        Shardable<K> shard = shard(key);
        shard.lock();
        try {
            c.accept(shard);
        }
        finally { shard.unlock(); }
    }

    @Override
    public Object shardAndExecute(K key, Function<Shardable<K>, Object> f) {
        Shardable<K> shard = shard(key);
        Object ret;
        shard.lock();
        try {
            ret = f.apply(shard);
        }
        finally { shard.unlock(); }
        return ret;
    }
    
    @Override
    public void forEach(Consumer<Shardable<K>> c) {
        Arrays.stream(shards).parallel().forEach((Shardable<K> shard) -> {
            shard.lock();
            try {
                c.accept(shard);
            }
            finally { shard.unlock(); }
        });
    }

    @Override
    public void free() {
        Arrays.stream(shards).parallel().forEach((Shardable<K> shard) -> {
            shard.lock();
            try {
                shard.free();
            }
            finally { shard.unlock(); }
        });
        heap.memoryBlockFromHandle(handle).freeMemory();
    }
    
    @Override
    public <E> AutoCloseableIterator<E> shardsAndExecute(K fromKey, K toKey, Function<Shardable<K>, Iterator<E>> f) {
        return null;
    }

    @Override
    public long handle() {
        return handle;
    }
}
