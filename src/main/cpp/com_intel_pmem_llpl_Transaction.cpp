/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "com_intel_pmem_llpl_Transaction.h"
#include "persistent_heap.h"
#include "util.h"

JNIEXPORT int JNICALL Java_com_intel_pmem_llpl_Transaction_nativeStartTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    int result = 0;
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    int ret = pmemobj_tx_begin(pool, NULL, TX_PARAM_NONE);
    if (ret) {
        pmemobj_tx_end();
        result = -1;
    }
    return result;
}

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_Transaction_nativeCommitTransaction
  (JNIEnv *env, jobject obj)
{
    if (pmemobj_tx_stage() == TX_STAGE_WORK) {
        pmemobj_tx_commit();
    }
    if (pmemobj_tx_stage() == TX_STAGE_ONCOMMIT | pmemobj_tx_stage() == TX_STAGE_ONABORT) {
        int ret = pmemobj_tx_end();
        if (ret != 0) throw_transaction_exception(env, "Failed to end transaction.");
    }
}

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_Transaction_nativeAbortTransaction
  (JNIEnv *env, jobject obj)
{
    if (pmemobj_tx_stage() == TX_STAGE_WORK) pmemobj_tx_abort(0);
    if (pmemobj_tx_stage() == TX_STAGE_ONABORT) {
        int ret = pmemobj_tx_end();
        // do not throw here on ret != 0
    }
}

JNIEXPORT int JNICALL Java_com_intel_pmem_llpl_Transaction_nativeTransactionState
  (JNIEnv *env, jobject obj)
{
    switch (pmemobj_tx_stage())
    {
        case TX_STAGE_NONE: return 1;
        case TX_STAGE_WORK: return 2;
        case TX_STAGE_ONCOMMIT: return 3;
        case TX_STAGE_ONABORT: return 4;
        case TX_STAGE_FINALLY: return 5;
        default: return 0;
    }
}

