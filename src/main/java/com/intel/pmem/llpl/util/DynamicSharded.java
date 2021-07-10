/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

interface DynamicSharded<K> extends Sharded<K> {

    public DynamicShardable<K> createDynamicShard(); 

    public DynamicShardable<K> recreateDynamicShard(long handle); 
}
