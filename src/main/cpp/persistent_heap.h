/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include <libpmemobj.h>
#include <libpmem.h>
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <jni.h>

#define CHAR_TYPE_OFFSET 1017
TOID_DECLARE(char, CHAR_TYPE_OFFSET);

PMEMobjpool *create_pool(JNIEnv *env, const char* path, size_t size, const char* pool_layout_name);
PMEMobjpool *open_pool(JNIEnv *env, const char* path, const char* pool_layout_name);
void register_allocation_classes(JNIEnv *env, PMEMobjpool* pool, jlongArray alloc_classes);
