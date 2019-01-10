/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "lib_llpl_AnyMemoryBlock.h"
#include "persistent_heap.h"
#include <libpmemobj.h>
#include <libpmem.h>

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeSetTransactionalByte
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jbyte value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    TX_BEGIN(pool) {
        pmemobj_tx_add_range_direct((const void *)address, (size_t)1);
        char *ptr = (char*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write byte value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeSetTransactionalShort
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jshort value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    TX_BEGIN(pool) {
        pmemobj_tx_add_range_direct((const void *)address, (size_t)2);
        int16_t *ptr = (int16_t*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write int value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeSetTransactionalInt
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jint value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    TX_BEGIN(pool) {
        pmemobj_tx_add_range_direct((const void *)address, (size_t)4);
        int *ptr = (int*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write int value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeSetTransactionalLong
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    TX_BEGIN(pool) {
        pmemobj_tx_add_range_direct((const void *)address, (size_t)8);
        long *ptr = (long*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write long value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeFlush
  (JNIEnv *env, jobject obj, jlong address, jlong size)
{
    pmem_persist((const void*)address, size);
}

// return 1 if added to existing
// return 2 if started new and added to that one
// return -1 on error
JNIEXPORT int JNICALL Java_lib_llpl_AnyMemoryBlock_nativeAddToTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size)
{
    int result = -1;    
    int stage = pmemobj_tx_stage();
    if (stage == TX_STAGE_WORK) {
        pmemobj_tx_add_range_direct((const void *)address, (size_t)size);
        result = 2;
    }
    return result;
}

JNIEXPORT int JNICALL Java_lib_llpl_AnyMemoryBlock_nativeAddRangeToTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size)
{
    int result = -1;    
    int stage = pmemobj_tx_stage();
    if (stage == TX_STAGE_NONE) {
        PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
        int begin_result = pmemobj_tx_begin(pool, NULL, TX_PARAM_NONE);
        if (begin_result) {
            pmemobj_tx_end();
        }
        else {
            pmemobj_tx_add_range_direct((const void *)address, (size_t)size);
            result = 1;
        }
    }
    else if (stage == TX_STAGE_WORK) {
        pmemobj_tx_add_range_direct((const void *)address, (size_t)size);
        result = 2;
    }
    return result;
}

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeAddToTransactionNoCheck
  (JNIEnv *env, jobject obj, jlong address, jlong size)
{
    int result = pmemobj_tx_add_range_direct((const void *)address, (size_t)size);
    if (result) printf("pmemobj_tx_add_range_direct error: %s\n", pmemobj_errormsg());
}

void throw_persistence_exception(JNIEnv *env, const char* arg)
{
    char className[50] = "lib/llpl/HeapException";
    jclass exClass = env->FindClass(className);

    char errmsg[250];
    strcpy(errmsg, arg);
    strcat(errmsg, pmemobj_errormsg());
    env->ThrowNew(exClass, errmsg);
}

