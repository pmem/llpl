/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "persistent_heap.h"

PMEMobjpool* get_or_create_pool(const char* path, size_t size)
{
    const char *pool_layout_name = "llpl_persistent_heap";
    PMEMobjpool *pool = pmemobj_open(path, pool_layout_name);
    if (pool == NULL) {
        pool = pmemobj_create(path, pool_layout_name, size, S_IRUSR | S_IWUSR);
    }
    if (pool == NULL) {
        printf("Failed to open pool %s\n", pmemobj_errormsg());
        fflush(stdout);
        exit(-1);
    }
    return pool;
}
