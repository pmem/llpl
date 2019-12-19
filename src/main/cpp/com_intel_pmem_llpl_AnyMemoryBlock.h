/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include <jni.h>

#ifndef _Included_com_intel_pmem_llpl_MemoryBlock
#define _Included_com_intel_pmem_llpl_MemoryBlock
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeFlush
  (JNIEnv *env, jobject obj, jlong address, jlong size);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeAddToTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeAddToTransactionNoCheck
  (JNIEnv *env, jobject obj, jlong address, jlong size);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeAddRangeToTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyMemoryBlock_nativeHasAutoFlush
  (JNIEnv *env, jobject obj);
#ifdef __cplusplus
}
#endif
#endif
