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

interface Sharded<K> {

    public Shardable<K> createShard(); 

    public Shardable<K> recreateShard(long handle); 

    public Comparator<K> getComparator();

    //abstract class ShardIterator<E> implements Iterator<E>, AutoCloseable{}

}
