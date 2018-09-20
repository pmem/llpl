/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "lib_llpl_Transaction.h"
#include "persistent_heap.h"

JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeStartTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    int ret = pmemobj_tx_begin(pool, NULL, TX_PARAM_NONE);
    if (ret) {
        pmemobj_tx_end();
        printf("Error starting transaction\n");
        exit(-1);
    }
}

JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeCommitTransaction
  (JNIEnv *env, jobject obj)
{
    pmemobj_tx_commit();
}

JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeEndTransaction
  (JNIEnv *env, jobject obj)
{
    pmemobj_tx_end();
}

JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeAbortTransaction
  (JNIEnv *env, jobject obj)
{
    pmemobj_tx_abort(0);
}
