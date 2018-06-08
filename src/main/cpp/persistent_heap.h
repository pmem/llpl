/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include <libpmemobj.h>
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <jni.h>

extern PMEMobjpool* pool;
extern TOID(struct root_struct) root;

struct root_struct {
    uint64_t root_val;
};

POBJ_LAYOUT_BEGIN(persistent_heap);
POBJ_LAYOUT_ROOT(persistent_heap, struct root_struct);
POBJ_LAYOUT_END(persistent_heap);

#define CHAR_TYPE_OFFSET 1017
TOID_DECLARE(char, CHAR_TYPE_OFFSET);

PMEMobjpool *get_or_create_pool(const char* path, size_t size);
uint64_t get_uuid_lo();
