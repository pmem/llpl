/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import java.util.Comparator;

abstract class AbstractSharded<K> {

    abstract Shardable<K> createShard(); 

    abstract Shardable<K> recreateShard(long handle); 

    abstract Comparator<K> getComparator();

    abstract DynamicShardable<K> createDynamicShard(); 

    abstract DynamicShardable<K> recreateDynamicShard(long handle); 
}
