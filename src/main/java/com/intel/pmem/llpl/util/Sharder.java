/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.AnyHeap;
import com.intel.pmem.llpl.AnyMemoryBlock;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

interface Sharder<K> {

	public long handle();

	public K lowestKey(Function<Shardable<K>, K> f);

	public K highestKey(Function<Shardable<K>, K> f);

    //public void shardAndExecute(K key, Consumer<Shardable<K>> c);

    public Object shardAndPut(K key, Function<Shardable<K>, Object> f);

    public Object shardAndGet(K key, Function<Shardable<K>, Object> f);

    public <E> AutoCloseableIterator<E> shardsAndExecute(K fromKey, K toKey, Function<Shardable<K>, Iterator<E>> f, boolean reversed);

    public void forEach(Consumer<Shardable<K>> c);

    public void free();

    public long totalEntries();

    @SuppressWarnings("unchecked")
    public static <K> Sharder<K> rebuild(AnyHeap heap, long handle, AbstractSharded<K> sharded) {
        AnyMemoryBlock block = heap.memoryBlockFromHandle(handle);
        byte[] arr = new byte[block.getInt(16)];
        block.copyToArray(20, arr, 0, arr.length);
        String className = new String(arr);
        Constructor ctor;
        Sharder<K> ret = null;
        String classArg = className.equals("com.intel.pmem.llpl.util.DynamicSharder")
                        ? "com.intel.pmem.llpl.util.AbstractSharded" 
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
