/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include <jni.h>

#ifndef _Included_com_intel_pmem_llpl_Transaction
#define _Included_com_intel_pmem_llpl_Transaction
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT int JNICALL Java_com_intel_pmem_llpl_Transaction_nativeStartTransaction
  (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_Transaction_nativeCommitTransaction
  (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_Transaction_nativeAbortTransaction
  (JNIEnv *, jobject);

JNIEXPORT int JNICALL Java_com_intel_pmem_llpl_Transaction_nativeTransactionState
  (JNIEnv *env, jobject obj);

#ifdef __cplusplus
}
#endif
#endif
