/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;
import com.intel.pmem.llpl.util.LongArray;

import java.util.Map;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import com.intel.pmem.llpl.Transaction;
import com.intel.pmem.llpl.AnyHeap;
import com.intel.pmem.llpl.AnyMemoryBlock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Iterator;
import java.lang.invoke.MethodHandle;

interface Sharder<K> {

	public long handle();

	public int shardCount(); 

	public Shardable<K> shard(K key); 

    public void shardAndExecute(K key, Consumer<Shardable<K>> c);

    public Object shardAndExecute(K key, Function<Shardable<K>, Object> f);

    public <E> AutoCloseableIterator<E> shardsAndExecute(K fromKey, K toKey, Function<Shardable<K>, Iterator<E>> f);

    public void forEach(Consumer<Shardable<K>> c);

    public void free();

    public long totalEntries();

    @SuppressWarnings("unchecked")
    public static <K> Sharder<K> rebuild(AnyHeap heap, long handle, Sharded<K> sharded) {
        AnyMemoryBlock block = heap.memoryBlockFromHandle(handle);
        byte[] arr = new byte[block.getInt(0)];
        block.copyToArray(4, arr, 0, arr.length);
        String className = new String(arr);
        Constructor ctor = null;
        Sharder<K> ret = null;
        String classArg = className.equals("com.intel.pmem.llpl.util.DynamicSharder")
                        ? "com.intel.pmem.llpl.util.DynamicSharded" 
                        : "com.intel.pmem.llpl.util.Sharded";
        try {
            Class cls = Class.forName(className);
            ctor = cls.getDeclaredConstructor(AnyHeap.class, Long.TYPE, Class.forName(classArg));
            ret = (Sharder)ctor.newInstance(heap, handle, sharded);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
