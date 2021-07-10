/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

interface Shardable<K> {

    public long handle(); 

    public long size(); 

    public void free();
}
