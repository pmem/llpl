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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

class DynamicSharder<K> implements Sharder<K> {
	private int nShards;
	private int maxShards;
    private ConcurrentSkipListMap<KeyRange<K>, Shard<K>> rangeToShardMap;
	private AnyHeap heap;
    private AbstractSharded<K> sharded;
    private final long SPLIT_THRESHOLD = 1000L;
    private long  handle;
    private LongArray shardArray;
    final String CLASSNAME = "com.intel.pmem.llpl.util.DynamicSharder"; 
    private final short VERSION = 100;
    private final long VERSION_OFFSET = 0;
    private final long ROOT_SHARD_OFFSET = VERSION_OFFSET + 8;
    private final long CLASSNAME_LENGTH_OFFSET = ROOT_SHARD_OFFSET + 8; //16
    private final long CLASSNAME_OFFSET = CLASSNAME_LENGTH_OFFSET + 4; //20
    private final long ROOT_BLOCK_SIZE = CLASSNAME_OFFSET + CLASSNAME.length();

    private final Comparator<K> comparator;

    long encodeRootBlock(AnyHeap heap, LongArray shardArray) {
        AnyMemoryBlock rootBlock = heap.allocateMemoryBlock(ROOT_BLOCK_SIZE);
        rootBlock.setInt(CLASSNAME_LENGTH_OFFSET, CLASSNAME.length());
        rootBlock.copyFromArray(CLASSNAME.getBytes(), 0, CLASSNAME_OFFSET, CLASSNAME.length());
        rootBlock.setLong(ROOT_SHARD_OFFSET, shardArray.handle());
        rootBlock.setShort(VERSION_OFFSET, VERSION);
        return rootBlock.handle();
    }

	@SuppressWarnings("unchecked")
    public DynamicSharder(AnyHeap heap, long handle, AbstractSharded sharded) {
        AnyMemoryBlock block = heap.memoryBlockFromHandle(handle);
        long shardArrayHandle = block.getLong(ROOT_SHARD_OFFSET);
        this.shardArray = LongArray.fromHandle(heap, shardArrayHandle);
        this.maxShards = (int)shardArray.size();
        this.comparator = sharded.getComparator();
        this.rangeToShardMap = new ConcurrentSkipListMap<>();
        Shard<K> shard;
        for (int i = 0; i < maxShards; i++) {
            long l = shardArray.get(i);
            if (l == 0) continue;
            shard = new Shard(sharded.recreateDynamicShard(l));
            KeyRange<K> range = (shard.shard().size() == 0) ? new KeyRange(this.comparator) : new KeyRange(shard.shard().lastKey(), this.comparator);
            rangeToShardMap.put(range, shard); 
        } 
        Map.Entry<KeyRange<K>, Shard<K>> last2 = rangeToShardMap.pollLastEntry();
        rangeToShardMap.put(new KeyRange(this.comparator), last2.getValue());
		this.nShards = rangeToShardMap.size();
		this.heap = heap;
        this.handle = handle;
        this.sharded = sharded;
    }

	@SuppressWarnings("unchecked")
	public DynamicSharder(AnyHeap heap, int maxShards, AbstractSharded sharded) {
        LongArray shardArray = new LongArray(heap, maxShards);
		rangeToShardMap = new ConcurrentSkipListMap<>();
        Shard<K> shard = new Shard(sharded.createDynamicShard());
        shardArray.set(0, shard.shard().handle());
        this.comparator = sharded.getComparator();
		KeyRange range = new KeyRange(this.comparator);
		rangeToShardMap.put(range, shard);
		nShards = 1;
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
        for (Shard<K> shard : rangeToShardMap.values()) {
           size += shard.shard().size();
        }
        return size;
    }

    @Override
    public K lowestKey(Function<Shardable<K>, K> f) {
        Shard<K> shard;
        KeyRange<K> range;
        K ret;
        ConcurrentMap.Entry<KeyRange<K>, Shard<K>> celEntry = rangeToShardMap.firstEntry();
        shard = celEntry.getValue(); 
        shard.lock();
        // recheck condition
        Shard<K> newShard;
        while((newShard = rangeToShardMap.get(celEntry.getKey())) != null && !newShard.equals(shard)) {
            shard.unlock();
            celEntry = rangeToShardMap.firstEntry();
            shard = celEntry.getValue(); shard.lock();
        }
        try {
            ret = f.apply(shard.shard());
        }
        finally { shard.unlock(); }
        return ret;
    }

    @Override
    public K highestKey(Function<Shardable<K>, K> f) {
        Shard<K> shard;
        KeyRange<K> range;
        K ret;
        ConcurrentMap.Entry<KeyRange<K>, Shard<K>> celEntry = rangeToShardMap.lastEntry();
        shard = celEntry.getValue(); 
        shard.lock();
        // recheck condition
        Shard<K> newShard;
        while((newShard = rangeToShardMap.get(celEntry.getKey())) != null && !newShard.equals(shard)) {
            shard.unlock();
            celEntry = rangeToShardMap.lastEntry();
            shard = celEntry.getValue(); shard.lock();
        }
        try {
            ret = f.apply(shard.shard());
        }
        finally { shard.unlock(); }
        return ret;
    }

    @Override
    public Object shardAndPut(K key, Function<Shardable<K>, Object> f) {
        Shard<K> shard; 
        KeyRange<K> range;
        Object ret;
        ConcurrentMap.Entry<KeyRange<K>, Shard<K>> celEntry = rangeToShardMap.ceilingEntry(new KeyRange<K>(key, comparator));
        shard = maybeSplit(celEntry, key); 
        shard.lock();
        // recheck condition
        Shard<K> newShard;
        while((newShard = rangeToShardMap.get(celEntry.getKey())) != null && !newShard.equals(shard)) {
            shard.unlock();
            celEntry = rangeToShardMap.ceilingEntry(new KeyRange<K>(key, comparator));
            shard = celEntry.getValue(); shard.lock();
        }
        try {
            ret = f.apply(shard.shard());
        }
        finally { shard.unlock(); }
        return ret;
    }

    @Override
    public Object shardAndGet(K key, Function<Shardable<K>, Object> f) {
        Shard<K> shard;
        KeyRange<K> range;
        Object ret;
        ConcurrentMap.Entry<KeyRange<K>, Shard<K>> celEntry = rangeToShardMap.ceilingEntry(new KeyRange<K>(key, comparator));
        shard = celEntry.getValue(); 
        shard.lock();
        // recheck condition
        Shard<K> newShard;
        while((newShard = rangeToShardMap.get(celEntry.getKey())) != null && !newShard.equals(shard)) {
            shard.unlock();
            celEntry = rangeToShardMap.ceilingEntry(new KeyRange<K>(key, comparator));
            shard = celEntry.getValue(); shard.lock();
        }
        try {
            ret = f.apply(shard.shard());
        }
        finally { shard.unlock(); }
        return ret;
    }

    @Override
    public <E> SequentialShardIterator<E> shardsAndExecute(K fromKey, K toKey, Function<Shardable<K>, Iterator<E>> f, boolean reversed) {
        Iterator<Shard<K>> it;
        Map.Entry<KeyRange<K>, Shard<K>> entry;
        KeyRange<K> toKeyRange = null;
        KeyRange<K> fromKeyRange = null;
        if (toKey != null) toKeyRange = rangeToShardMap.ceilingKey(new KeyRange<K>(toKey, comparator));
        if (fromKey != null) fromKeyRange = rangeToShardMap.ceilingKey(new KeyRange<K>(fromKey, comparator));
        if ((fromKey == null) && (toKey == null)) {
            if (reversed) it = rangeToShardMap.descendingMap().values().iterator();
            else it = rangeToShardMap.values().iterator();
        }
        else if (fromKey == null) { // Headiterator
            if (reversed) it = rangeToShardMap.headMap(toKeyRange, true).descendingMap().values().iterator();
            else it = rangeToShardMap.headMap(toKeyRange, true).values().iterator();
        }
        else if (toKey == null) { // TailIterator
            if (reversed) it = rangeToShardMap.tailMap(fromKeyRange, true).descendingMap().values().iterator(); 
            else it = rangeToShardMap.tailMap(fromKeyRange, true).values().iterator(); 
        }
        else if ((entry = rangeToShardMap.ceilingEntry(new KeyRange<K>(fromKey, comparator))).getKey().contains(toKey)) {
            return new SequentialShardIterator<E>(entry.getValue(), f);
        }
        else {
            if (reversed) it = rangeToShardMap.subMap(fromKeyRange, true, toKeyRange, true).descendingMap().values().iterator();
            else it = rangeToShardMap.subMap(fromKeyRange, true, toKeyRange, true).values().iterator();
        }
        return new SequentialShardIterator<E>(it, f);
    }

    @Override
    public void forEach(Consumer<Shardable<K>> c) {
        rangeToShardMap.values().parallelStream().forEach((Shard<K> shard) -> {    
            shard.lock();
            try {
                c.accept(shard.shard());
            }
            finally { shard.unlock(); }
        });
    }
    
    @Override
    public void free() {
        rangeToShardMap.values().parallelStream().forEach((Shard<K> shard) -> {    
            shard.lock();
            try {
                shard.shard().free();
            }
            finally { shard.unlock(); }
        });
        shardArray.free();
        heap.memoryBlockFromHandle(handle).freeMemory();
        handle = 0;
    /*    rangeToShardMap.clear(); 
        for (int i = 0; i < shardArray.size(); i++) {
            sharded.recreateShard(shardArray.get(i)).free(); 
        }
        heap.memoryBlockFromHandle(handle).freeMemory();
    */}
    // remove
    private static String format(byte[] ba) {
        StringBuffer sb = new StringBuffer("[ ");
        for (int i = 0; i< ba.length; i++) {
            sb.append(Byte.toUnsignedInt(ba[i])+ " ");
        }
        sb.append("]");
        return sb.toString();
    }

    private Shard<K> splitKeyRange(Map.Entry<KeyRange<K>, Shard<K>> entry, K bytes) {
        KeyRange<K> oldRange = entry.getKey();
        KeyRange<K> left;
        Shard<K> oldShard = entry.getValue();
        Shard<K> newShard;
        
        synchronized(shardArray) {
            oldShard.lock();
            try {
                if (nShards == maxShards || entry.getValue().shard().size() < SPLIT_THRESHOLD) return entry.getValue();
                KeyRange<K> right = oldRange; 
                //persistent stuff
                newShard = Transaction.create(heap, ()-> {
                    Shard<K> tempShard = new Shard<K>(oldShard.shard().split());
                    shardArray.set(nShards, tempShard.shard().handle());
                    return tempShard;
                });
                //volatile stuff
                nShards++;
                left = createKeyRange(oldShard.shard().lastKey());
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
    
	/*public Shardable<K> shard(K key) {
        splitLock_r.lock();
        try {
            ConcurrentMap.Entry<KeyRange<K>, Integer> celEntry = rangeMap.ceilingEntry(new KeyRange((byte[])key, comparator));
            return maybeSplit(celEntry, key);
        }
        finally {
            if (splitLock.isWriteLockedByCurrentThread()) splitLock_w.unlock();
            else splitLock_r.unlock();
        }
        return null;
	}*/

    void printRangeMap() {
		for (ConcurrentMap.Entry<KeyRange<K>, Shard<K>> entry : rangeToShardMap.entrySet()) {
            System.out.println(entry.getKey() +" size "+entry.getValue().shard().size());
        }
    }

    private Shard<K> maybeSplit(Map.Entry<KeyRange<K>, Shard<K>> entry, K key){
        if (nShards == maxShards || entry.getValue().shard().size() < SPLIT_THRESHOLD) return entry.getValue();
        else return splitKeyRange(entry, key);
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

    class Shard<K> {
        DynamicShardable<K> shard;
        ReentrantLock lock;
        
        Shard (DynamicShardable<K> shard) {
            this.shard = shard;
            lock = new ReentrantLock(false);
        }

        public DynamicShardable<K> shard() { return shard; }
        public void lock() { this.lock.lock(); }
        public void unlock() { this.lock.unlock(); }
        public boolean isLocked() { return lock.isLocked(); }
    }

    public class SequentialShardIterator<E> implements AutoCloseableIterator<E> { 
        Iterator<Shard<K>> shardIterator;
        Function<Shardable<K>, Iterator<E>> f;
        Iterator<E> shardEntryIter;
        Shard<K> currentShard;
        E currentEntry;
        E nextEntry;
        boolean reversed;

        public SequentialShardIterator(Shard<K> shard, Function<Shardable<K>, Iterator<E>> f){
            this.shardIterator = null;
            this.f = f;
            currentShard = shard;
            currentShard.lock();
            shardEntryIter = f.apply(currentShard.shard());
            nextEntry = (shardEntryIter != null && shardEntryIter.hasNext()) ? shardEntryIter.next() :  null;
            if (nextEntry == null) currentShard.unlock();
        }

        public SequentialShardIterator(Iterator<Shard<K>> shardIterator, Function<Shardable<K>, Iterator<E>> f){
            this.shardIterator = shardIterator;
            this.f = f;
            if (shardIterator.hasNext()) {
                currentShard = shardIterator.next();
                currentShard.lock();
                shardEntryIter = f.apply(currentShard.shard());
                nextEntry = (shardEntryIter != null && shardEntryIter.hasNext()) ? shardEntryIter.next() : null;
                if (nextEntry == null && currentShard != null && currentShard.isLocked()) currentShard.unlock();
            }
        }

        public E next() {
            currentEntry = nextEntry;
            if (currentEntry == null) throw new NoSuchElementException("Null");
            nextEntry = (shardEntryIter != null && shardEntryIter.hasNext()) ? shardEntryIter.next() : null;
            if (nextEntry == null && shardIterator != null && shardIterator.hasNext()) {
                currentShard.unlock();
                currentShard = shardIterator.next();
                currentShard.lock();
                shardEntryIter = f.apply(currentShard.shard());
                nextEntry = shardEntryIter.hasNext() ? shardEntryIter.next() : null;
                if (nextEntry == null && currentShard != null && currentShard.isLocked()) currentShard.unlock();
            }
            return currentEntry;
        }

        public boolean hasNext() {
            return (nextEntry != null);
        }
    
        @Override
        public void close() {
            if (currentShard != null && currentShard.isLocked()) currentShard.unlock();
        }
    }
}
