/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import java.util.Comparator;

interface Sharded<K> {

    public Shardable<K> createShard(); 

    public Shardable<K> recreateShard(long handle); 

    public Comparator<K> getComparator();

}
