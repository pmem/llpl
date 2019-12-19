/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "com_intel_pmem_llpl_AnyMemoryBlock.h"
#include "persistent_heap.h"
#include "util.h"
#include <libpmemobj.h>
#include <libpmem.h>

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeFlush
  (JNIEnv *env, jobject obj, jlong address, jlong size)
{
    pmem_persist((const void*)address, size);
}

// return 1 if started new and added to that one
// return 2 if added to existing
// return -1 on error
JNIEXPORT int JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeAddToTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size)
{
    int result = -1;    
    int stage = pmemobj_tx_stage();
    if (stage == TX_STAGE_WORK) {
        int err = pmemobj_tx_add_range_direct((const void *)address, (size_t)size);
        if (err != 0) throw_transaction_exception(env, "Internal error, failed to add to transaction.");
        result = 2;
    }
    return result;
}

JNIEXPORT int JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeAddRangeToTransaction
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

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeAddToTransactionNoCheck
  (JNIEnv *env, jobject obj, jlong address, jlong size)
{
    return pmemobj_tx_add_range_direct((const void *)address, (size_t)size);
}

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeHasAutoFlush
  (JNIEnv *env, jobject obj)
{
    return pmem_has_auto_flush();
}
