/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include <string.h>
#include "util.h"
#include "persistent_heap.h"

void throw_exception(JNIEnv *env, jclass cls, const char* arg) {
    /*int MAX_LEN = 250;
    char errmsg[MAX_LEN];
    strncpy(errmsg, arg, sizeof(errmsg) - 1);
    int n = strlen(errmsg);
    if (n < MAX_LEN) {
        strncat(errmsg, pmemobj_errormsg(), (size_t)(MAX_LEN - n));
    env->ThrowNew(cls, errmsg);
    }*/
    env->ThrowNew(cls, arg);
}

void throw_heap_exception(JNIEnv *env, const char* arg)
{
    jclass cls = env->FindClass("com/intel/pmem/llpl/HeapException");
    throw_exception(env, cls, arg);
}

void throw_transaction_exception(JNIEnv *env, const char* arg)
{
    jclass cls = env->FindClass("com/intel/pmem/llpl/TransactionException");
    throw_exception(env, cls, arg);
}
