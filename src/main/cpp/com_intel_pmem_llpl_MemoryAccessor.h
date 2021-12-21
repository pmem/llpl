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

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_MemoryAccessor_nativeFlush
  (JNIEnv *env, jobject obj, jlong address, jlong size);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_MemoryAccessor_nativeAddToTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_MemoryAccessor_nativeAddToTransactionNoCheck
  (JNIEnv *env, jobject obj, jlong address, jlong size);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_MemoryAccessor_nativeAddRangeToTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_MemoryAccessor_nativeHasAutoFlush
  (JNIEnv *env, jobject obj);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_MemoryAccessor_nativeGetDirectByteBufferAddress
  (JNIEnv *env, jobject obj, jobject buf);
#ifdef __cplusplus
}
#endif
#endif
