/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include <jni.h>
#include <libpmemobj.h>
#include <libpmem.h>

void throw_heap_exception(JNIEnv *env, const char* arg);
void throw_transaction_exception(JNIEnv *env, const char* arg);
