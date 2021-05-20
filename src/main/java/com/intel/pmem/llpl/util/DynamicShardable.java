/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

interface DynamicShardable<K> extends Shardable<K> {

    //public DynamicShardable<K> split(K splitkey);

    public DynamicShardable<K> split();

    //public K splitKey();
    
    public K lastKey();
}
