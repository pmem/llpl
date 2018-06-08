/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "persistent_heap.h"

PMEMobjpool *pool = NULL;
TOID(struct root_struct) root;
static uint64_t uuid_lo;

PMEMobjpool* get_or_create_pool(const char* path, size_t size)
{
    if (pool != NULL) {
        return pool;
    }

    pool = pmemobj_open(path, POBJ_LAYOUT_NAME(persistent_heap));
    if (pool == NULL) {
        pool = pmemobj_create(path, POBJ_LAYOUT_NAME(persistent_heap), size, S_IRUSR | S_IWUSR);
    }

    if (pool == NULL) {
        printf("Failed to open pool %s\n", pmemobj_errormsg());
        fflush(stdout);
        exit(-1);
    }

    TOID(char) arr;
    TX_BEGIN (pool) {
        arr = TX_ALLOC(char, 1);
        uuid_lo = arr.oid.pool_uuid_lo;
        fflush(stdout);
        TX_MEMSET(pmemobj_direct(arr.oid), 0, 1);
    } TX_ONABORT {
        printf("Encountered error opening pool\n");
        fflush(stdout);
        exit(-1);
    } TX_END

    root = POBJ_ROOT(pool, struct root_struct);
    return pool;
}

uint64_t get_uuid_lo()
{
    return uuid_lo;
}
