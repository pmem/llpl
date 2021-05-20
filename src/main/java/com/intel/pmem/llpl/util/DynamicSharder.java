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
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.NoSuchElementException;
import static java.util.concurrent.ConcurrentMap.Entry;
import static java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import com.intel.pmem.llpl.util.LongArray;
import java.util.Map;
import com.intel.pmem.llpl.Transaction;
import com.intel.pmem.llpl.AnyHeap;
import com.intel.pmem.llpl.AnyMemoryBlock;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

class DynamicSharder<K> implements Sharder<K> {
	private int nShards;
	private int maxShards;
    private ConcurrentSkipListMap<KeyRange<K>, DynamicShardable<K>> rangeToShardMap;
	private AnyHeap heap;
    private Sharded<K> sharded;
    private final long SPLIT_THRESHOLD = 100_000L;
    private long  handle;
    private LongArray shardArray;
    final String CLASSNAME = "com.intel.pmem.llpl.util.DynamicSharder"; 
    private final int CLASSNAME_LENGTH_OFFSET = 0;
    private final int CLASSNAME_OFFSET = 4;
    private final long ROOT_SHARD_OFFSET = CLASSNAME_OFFSET + CLASSNAME.length();
    private final long ROOT_BLOCK_SIZE = ROOT_SHARD_OFFSET + 8;
    private final Comparator<K> comparator;

    long encodeRootBlock(AnyHeap heap, LongArray shardArray) {
        AnyMemoryBlock rootBlock = heap.allocateMemoryBlock(ROOT_BLOCK_SIZE);
        rootBlock.setInt(CLASSNAME_LENGTH_OFFSET, CLASSNAME.length());
        rootBlock.copyFromArray(CLASSNAME.getBytes(), 0, CLASSNAME_OFFSET, CLASSNAME.length());
        rootBlock.setLong(ROOT_SHARD_OFFSET, shardArray.handle());
        return rootBlock.handle();
    }

	@SuppressWarnings("unchecked")
    public DynamicSharder(AnyHeap heap, long handle, DynamicSharded sharded) {
        AnyMemoryBlock block = heap.memoryBlockFromHandle(handle);
        long shardArrayHandle = block.getLong(ROOT_SHARD_OFFSET);
        this.shardArray = LongArray.fromHandle(heap, shardArrayHandle);
        this.maxShards = (int)shardArray.size();
        this.comparator = sharded.getComparator();
        this.rangeToShardMap = new ConcurrentSkipListMap<>();
        DynamicShardable<K> shard;
        for (int i = 0; i < maxShards; i++) {
            long l = shardArray.get(i);
            if (l == 0) continue;
            shard = sharded.recreateDynamicShard(l);
            KeyRange<K> range = (shard.size() == 0) ? new KeyRange(this.comparator) : new KeyRange(shard.lastKey(), this.comparator);
            rangeToShardMap.put(range, shard); 
        } 
        Map.Entry<KeyRange<K>, DynamicShardable<K>> last2 = rangeToShardMap.pollLastEntry();
        rangeToShardMap.put(new KeyRange(this.comparator), last2.getValue());
        printRangeMap();
		// System.out.println("   nShards = " + rangeToShardMap.size());
		// debug();
		this.nShards = rangeToShardMap.size();
		this.heap = heap;
        this.handle = handle;
        this.sharded = sharded;
    }

	@SuppressWarnings("unchecked")
	public DynamicSharder(AnyHeap heap, int maxShards, DynamicSharded sharded) {
        LongArray shardArray = new LongArray(heap, maxShards);
		rangeToShardMap = new ConcurrentSkipListMap<>();
        DynamicShardable<K> shard = sharded.createDynamicShard();
        shardArray.set(0, shard.handle());
        this.comparator = sharded.getComparator();
		KeyRange range = new KeyRange(this.comparator);
		rangeToShardMap.put(range, shard);
		nShards = 1;
		// System.out.println("   nShards = " + rangeToShardMap.size());
		this.maxShards = maxShards;
		this.heap = heap;
        this.shardArray = shardArray;
        this.handle = encodeRootBlock(heap, shardArray);
        this.sharded = sharded;
	}

    @Override
    public long handle() {
        return handle;
    }

    @Override //should lock?
    public long totalEntries() {
        long size = 0;
        for (DynamicShardable<K> shard : rangeToShardMap.values()) {
           size += shard.size();
        }
        return size;
    }

    // remove
    private static String format(byte[] ba) {
        StringBuffer sb = new StringBuffer("[ ");
        for (int i = 0; i< ba.length; i++) {
            sb.append(Byte.toUnsignedInt(ba[i])+ " ");
            //sb.append(ba[i]+ " ");
        }
        sb.append("]");
        return sb.toString();
    }

    public Shardable<K> splitKeyRange(Map.Entry<KeyRange<K>, DynamicShardable<K>> entry, K bytes) {
        KeyRange<K> oldRange = entry.getKey();
        KeyRange<K> left;
        DynamicShardable<K> oldShard = entry.getValue();
        DynamicShardable<K> newShard;
        
        synchronized(shardArray) {
            oldShard.lock();
            try {
                if (nShards == maxShards || entry.getValue().size() < SPLIT_THRESHOLD) return entry.getValue();
                KeyRange<K> right = oldRange; 
                //persistent stuff
                newShard = Transaction.create(heap, ()-> {
                    DynamicShardable<K> tempShard = oldShard.split();
                    shardArray.set(nShards, tempShard.handle());
                    return tempShard;
                });
                //volatile stuff
                nShards++;
                left = createKeyRange(oldShard.lastKey());
                rangeToShardMap.put(left, oldShard);
                rangeToShardMap.put(right, newShard);
            } finally {
                oldShard.unlock();
            }
        }
        if (left.contains(bytes)) return oldShard;
        else return newShard;
    }
    
    private KeyRange<K> createKeyRange(K high) {
        return new KeyRange<K>(high, comparator);
    }

	static class KeyRange<K> implements Comparable<KeyRange<K>> {
		private K high;
        public static final Object END_RANGE = new Object();
        private final Comparator<K> c;
    
	    @SuppressWarnings("unchecked")
		public KeyRange(Comparator<K> c) {
            this.high = (K)END_RANGE;
            this.c = c;
        }

		public KeyRange(K high, Comparator<K> c) {
			this.high = high;
            this.c = c;
		}

		public K high() {return high;}

		public boolean contains(K key) {
            if (high == END_RANGE) return true;
			boolean b = compare(c, key, high) <= 0;
            //if (!b) System.out.println(this + " contains " + Arrays.toString(bytes));
            return b;
		}

        @Override
	    public int compareTo(KeyRange<K> b) {
            if (high.equals(b.high)) return 0;
            if (b.high.equals(KeyRange.END_RANGE)) return -1;
            if (high.equals(KeyRange.END_RANGE)) return 1;
            return compare(c, high, b.high);
        }

	    @SuppressWarnings("unchecked")
	    static <K> int compare(Comparator<K> c, K a, K b) {
            return (c != null) ? c.compare(a, b) : ((Comparable)a).compareTo(b); 
        }

    	public K asKey() {
    		return high;
    	}

    	public int hashCode() {
    		return high.hashCode();
    	}

	    @SuppressWarnings("unchecked")
    	public boolean equals(Object obj) {
    		if (!(obj instanceof KeyRange)) return false;
    		KeyRange<K> other = (KeyRange<K>)obj;
    		return (c != null) ? c.compare(this.high, other.high) == 0 : this.high.equals(other.high);
    	}

		@Override
		public String toString() {
            if (high == END_RANGE) return "END_RANGE";
			//return Arrays.toString((byte[])high);
			return DynamicSharder.format((byte[])high);
		} 
	}

    @Override
    public void shardAndExecute(K key, Consumer<Shardable<K>> c) {
        Shardable<K> shard; 
        KeyRange<K> range;
        ConcurrentMap.Entry<KeyRange<K>, DynamicShardable<K>> celEntry = rangeToShardMap.ceilingEntry(new KeyRange<K>(key, comparator));
        shard = maybeSplit(celEntry, key); 
        shard.lock();
        // recheck condition
        while(!rangeToShardMap.get(celEntry.getKey()).equals(shard)) {
            shard.unlock();
            celEntry = rangeToShardMap.ceilingEntry(new KeyRange<K>(key, comparator));
            shard = celEntry.getValue(); shard.lock();
        }
        try {
            c.accept(shard);
        }
        finally { shard.unlock(); }
    }

    @Override
    public Object shardAndExecute(K key, Function<Shardable<K>, Object> f) {
        Shardable<K> shard;
        KeyRange<K> range;
        Object ret = null;
        ConcurrentMap.Entry<KeyRange<K>, DynamicShardable<K>> celEntry = rangeToShardMap.ceilingEntry(new KeyRange<K>(key, comparator));
        shard = celEntry.getValue(); 
        shard.lock();
        // recheck condition
        while(!rangeToShardMap.get(celEntry.getKey()).equals(shard)) {
            shard.unlock();
            celEntry = rangeToShardMap.ceilingEntry(new KeyRange<K>(key, comparator));
            shard = celEntry.getValue(); shard.lock();
        }
        try {
            ret = f.apply(shard);
        }
        finally { shard.unlock(); }
        return ret;
    }
    
    public void debug() {
        int i = 0;
        Iterator<Map.Entry<KeyRange<K>, DynamicShardable<K>>> it = rangeToShardMap.entrySet().iterator();
        Map.Entry<KeyRange<K>, DynamicShardable<K>> e;
        while (it.hasNext()) {
            e = it.next();
            DynamicShardable<K> shard =e.getValue();
            // System.out.println("Shard["+i+"]: handle = "+shard.handle()+" HighKey is "+e.getKey()+" firstKey is"+format(((LongART)shard).firstKey())+" size is "+shard.size()); 
            // if (i == 0) ((LongART)shard).statsPrint();
            i++;
        }
    }

    @Override
    public <E> SequentialShardIterator<E> shardsAndExecute(K fromKey, K toKey, Function<Shardable<K>, Iterator<E>> f) {
        Iterator<DynamicShardable<K>> it;
        Map.Entry<KeyRange<K>, DynamicShardable<K>> entry = null;
        if ((fromKey == null) && (toKey == null)) {
            it = rangeToShardMap.values().iterator();
        }
        else if (fromKey == null) { // Headiterator
            it = rangeToShardMap.headMap(new KeyRange<K>(toKey, comparator)).values().iterator();
        }
        else if (toKey == null) { // TailIterator
            it = rangeToShardMap.tailMap(new KeyRange<K>(fromKey, comparator)).values().iterator(); 
        }
        //else if (toKey.equals(fromKey) || (entry = rangeToShardMap.ceilingEntry(new KeyRange(toKey, comparator))).getKey().contains(fromKey)) {
        else if ((entry = rangeToShardMap.ceilingEntry(new KeyRange<K>(fromKey, comparator))).getKey().contains(toKey)) {
            return new SequentialShardIterator<E>(entry.getValue(), f);
        }
        else it = rangeToShardMap.subMap(new KeyRange<K>(fromKey, comparator), new KeyRange<K>(toKey, comparator)).values().iterator();
        return new SequentialShardIterator<E>(it, f);
    }

    @Override
	public int shardCount() {
		return nShards;
	}

    @Override
    public void forEach(Consumer<Shardable<K>> c) {
        rangeToShardMap.values().parallelStream().forEach((DynamicShardable<K> shard) -> {    
            shard.lock();
            try {
                c.accept(shard);
            }
            finally { shard.unlock(); }
        });
    }
    
    @Override
    public void free() {
        rangeToShardMap.values().parallelStream().forEach((DynamicShardable<K> shard) -> {    
            shard.lock();
            try {
                shard.free();
            }
            finally { shard.unlock(); }
        });
        heap.memoryBlockFromHandle(handle).freeMemory();
        shardArray.free();
    /*    rangeToShardMap.clear(); 
        for (int i = 0; i < shardArray.size(); i++) {
            sharded.recreateShard(shardArray.get(i)).free(); 
        }
        heap.memoryBlockFromHandle(handle).freeMemory();
    */}

	public Shardable<K> shard(K key) {
        /* splitLock_r.lock();
        try {
            ConcurrentMap.Entry<KeyRange<K>, Integer> celEntry = rangeMap.ceilingEntry(new KeyRange((byte[])key, comparator));
            return maybeSplit(celEntry, key);
        }
        finally {
            if (splitLock.isWriteLockedByCurrentThread()) splitLock_w.unlock();
            else splitLock_r.unlock();
        }*/
        return null;
	}

    public void printRangeMap() {
		for (ConcurrentMap.Entry<KeyRange<K>, DynamicShardable<K>> entry : rangeToShardMap.entrySet()) {
            System.out.println(entry.getKey() +" size "+entry.getValue().size());
        }
    }

    private Shardable<K> maybeSplit(Map.Entry<KeyRange<K>, DynamicShardable<K>> entry, K key){
        if (nShards == maxShards || entry.getValue().size() < SPLIT_THRESHOLD) return entry.getValue();
        else return splitKeyRange(entry, key);
    }

    public class SequentialShardIterator<E> implements AutoCloseableIterator<E> { 
        Iterator<DynamicShardable<K>> shardIterator;
        Function<Shardable<K>, Iterator<E>> f;
        Iterator<E> shardEntryIter;
        Shardable<K> currentShard;
        E currentEntry;
        E nextEntry;

        public SequentialShardIterator(DynamicShardable<K> shard, Function<Shardable<K>, Iterator<E>> f){
            this.shardIterator = null;
            this.f = f;
            currentShard = shard;
            currentShard.lock();
            shardEntryIter = f.apply(currentShard);
            nextEntry = (shardEntryIter != null && shardEntryIter.hasNext()) ? shardEntryIter.next() :  null;
            hasNext();
        }

        public SequentialShardIterator(Iterator<DynamicShardable<K>> shardIterator, Function<Shardable<K>, Iterator<E>> f){
            this.shardIterator = shardIterator;
            this.f = f;
            if (shardIterator.hasNext()) {
                currentShard = shardIterator.next();
                currentShard.lock();
                shardEntryIter = f.apply(currentShard);
                nextEntry = (shardEntryIter != null && shardEntryIter.hasNext()) ? shardEntryIter.next() : null;
            }
            hasNext();
        }

        public E next() {
            currentEntry = nextEntry;
            if (currentEntry == null) throw new NoSuchElementException("Null");
            nextEntry = (shardEntryIter != null && shardEntryIter.hasNext()) ? shardEntryIter.next() : null;
            return currentEntry;
        }

        public boolean hasNext() {
            if (nextEntry != null) return true;
            if (shardIterator != null && shardIterator.hasNext()) {
                currentShard.unlock();
                currentShard = shardIterator.next();
                currentShard.lock();
                shardEntryIter = f.apply(currentShard);
                nextEntry = shardEntryIter.hasNext() ? shardEntryIter.next() : null;
                if (nextEntry != null) return true;
            }
            if (currentShard != null && currentShard.isLocked()) currentShard.unlock();
            return false;
        }
    
        @Override
        public void close() {
            if (currentShard != null && currentShard.isLocked()) currentShard.unlock();
        }
    }
}
