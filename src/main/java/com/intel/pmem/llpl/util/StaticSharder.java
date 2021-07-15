/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.AnyHeap;
import com.intel.pmem.llpl.AnyMemoryBlock;
import com.intel.pmem.llpl.Transaction;
import com.intel.pmem.llpl.util.LongArray;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

class StaticSharder<K> implements Sharder<K>{
	private int maxShards;
	private Shard<K>[] shards;
	private AnyHeap heap;
    private final long handle;
    final String CLASSNAME = "com.intel.pmem.llpl.util.StaticSharder"; 
    private final short VERSION = 100;
    private final long VERSION_OFFSET = 0;
    private final long ROOT_SHARD_OFFSET = VERSION_OFFSET + 8;
    private final long CLASSNAME_LENGTH_OFFSET = ROOT_SHARD_OFFSET + 8;
    private final long CLASSNAME_OFFSET = CLASSNAME_LENGTH_OFFSET + 4;
    private final long ROOT_BLOCK_SIZE = CLASSNAME_OFFSET + CLASSNAME.length();

	private StaticSharder(LongArray shardArray, int maxShards, AnyHeap heap, Shard<K>[] shards) {
		this.maxShards = maxShards;
		this.heap = heap;
		this.shards = shards;
        this.handle = encodeRootBlock(heap, shardArray);
	} 

	@SuppressWarnings("unchecked")
    public StaticSharder(AnyHeap heap, long handle, Sharded<K> sharded) {
		this.heap = heap;
        this.handle = handle;
        AnyMemoryBlock block = heap.memoryBlockFromHandle(handle);
        long shardArrayHandle = block.getLong(ROOT_SHARD_OFFSET);
        LongArray shardArray = LongArray.fromHandle(heap, shardArrayHandle); 
        this.maxShards = (int)shardArray.size();
        this.shards = (Shard<K>[])new Shard[maxShards];
        for (int i = 0; i < maxShards; i++) {
            shards[i] = new Shard<K>(sharded.recreateShard(shardArray.get(i)));
        } 
    }

    public long encodeRootBlock(AnyHeap heap, LongArray shardArray) {
        AnyMemoryBlock rootBlock = heap.allocateMemoryBlock(ROOT_BLOCK_SIZE);
        rootBlock.setInt(CLASSNAME_LENGTH_OFFSET, CLASSNAME.length());
        rootBlock.copyFromArray(CLASSNAME.getBytes(), 0, CLASSNAME_OFFSET, CLASSNAME.length());
        rootBlock.setLong(ROOT_SHARD_OFFSET, shardArray.handle());
        rootBlock.setShort(VERSION_OFFSET, VERSION);
        return rootBlock.handle();
    }

	@SuppressWarnings("unchecked")
	public StaticSharder(AnyHeap heap, int maxShards, Sharded<K> sharded) {
        LongArray shardArray = new LongArray(heap, maxShards); 
		Shard<K>[] shards = new Shard[maxShards];
		for (int i = 0; i < maxShards; i++) {
            shards[i] = new Shard<K>(sharded.createShard());
            shardArray.set(i, shards[i].handle());
		}
		this.maxShards = maxShards;
		this.heap = heap;
		this.shards = shards;
        this.handle = encodeRootBlock(heap, shardArray);
    }

    @Override //should lock?
    public long totalEntries() {
        long size = 0;
        for (int i = 0; i < shards.length; i++) {
           size += shards[i].size();
        }
        return size;
    }

	Shard<K> shard(K key) {
        return shards[Math.abs(key.hashCode()) % maxShards];
	}

    @Override
    //public void shardAndExecute(K key, Consumer<Shardable<K>> c){
    public Object shardAndPut(K key, Function<Shardable<K>, Object> f) {
        return shardAndGet(key, f);
    }

    @Override
    public Object shardAndGet(K key, Function<Shardable<K>, Object> f) {
        Shard<K> shard = shard(key);
        Object ret;
        shard.lock();
        try {
            ret = f.apply(shard.shard());
        }
        finally { shard.unlock(); }
        return ret;
    }
    
    @Override
    public void forEach(Consumer<Shardable<K>> c) {
        Arrays.stream(shards).parallel().forEach((Shard<K> shard) -> {
            shard.lock();
            try {
                c.accept(shard.shard());
            }
            finally { shard.unlock(); }
        });
    }

    @Override
    public void free() {
        Arrays.stream(shards).parallel().forEach((Shard<K> shard) -> {
            shard.lock();
            try {
                shard.free();
            }
            finally { shard.unlock(); }
        });
        heap.memoryBlockFromHandle(handle).freeMemory();
    }
    
    @Override
    public <E> AutoCloseableIterator<E> shardsAndExecute(K fromKey, K toKey, Function<Shardable<K>, Iterator<E>> f, boolean reversed) {
        return null;
    }

    @Override
    public long handle() {
        return handle;
    }

    @Override
    public K lowestKey(Function<Shardable<K>, K> f) {
        ConcurrentSkipListSet<K> set = new ConcurrentSkipListSet<K>();
        Arrays.stream(shards).parallel().forEach((Shard<K> shard) -> {
            shard.lock();
            try {
                set.add(f.apply(shard.shard()));
            }
            finally { shard.unlock(); }
        });
        return set.first();
    }

    @Override
    public K highestKey(Function<Shardable<K>, K> f) {
        ConcurrentSkipListSet<K> set = new ConcurrentSkipListSet<K>();
        Arrays.stream(shards).parallel().forEach((Shard<K> shard) -> {
            shard.lock();
            try {
                set.add(f.apply(shard.shard()));
            }
            finally { shard.unlock(); }
        });
        return set.last();
    }

    class Shard<K> {
        Shardable<K> shard;
        ReentrantLock lock;
        
        Shard (Shardable<K> shard) {
            this.shard = shard;
            lock = new ReentrantLock(false);
        }

        public Shardable<K> shard() { return shard; }
        public long handle() { return shard.handle(); }
        public long size() { return shard.size(); }
        public void free() { shard.free(); }
        public void lock() { this.lock.lock(); }
        public void unlock() { this.lock.unlock(); }
        public boolean isLocked() { return lock.isLocked(); }
    }
}
