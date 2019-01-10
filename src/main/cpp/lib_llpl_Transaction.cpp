/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "lib_llpl_Transaction.h"
#include "persistent_heap.h"

JNIEXPORT int JNICALL Java_lib_llpl_Transaction_nativeStartTransaction
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

JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeCommitTransaction
  (JNIEnv *env, jobject obj)
{
    if (pmemobj_tx_stage() == TX_STAGE_WORK) {
        pmemobj_tx_commit();
    }
    if (pmemobj_tx_stage() == TX_STAGE_ONCOMMIT | pmemobj_tx_stage() == TX_STAGE_ONABORT) {
        pmemobj_tx_end();
    }
}

JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeEndTransaction
  (JNIEnv *env, jobject obj)
{
    pmemobj_tx_end();
}

JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeAbortTransaction
  (JNIEnv *env, jobject obj)
{
    if (pmemobj_tx_stage() == TX_STAGE_WORK) pmemobj_tx_abort(0);
    if (pmemobj_tx_stage() == TX_STAGE_ONABORT) pmemobj_tx_end();
}

JNIEXPORT int JNICALL Java_lib_llpl_Transaction_nativeTransactionState
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
