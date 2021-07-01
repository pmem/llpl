/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

// Note: this code is EXPERIMENTAL 

#include <jni.h>

#ifndef _Included_com_intel_pmem_llpl_MemoryPoolImpl
#define _Included_com_intel_pmem_llpl_MemoryPoolImpl
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeOpenPool
  (JNIEnv *env, jobject obj, jstring path, jlong size);

JNIEXPORT int JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeClosePool
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong byteCount);

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeFlush
  (JNIEnv *env, jobject obj, jlong offset, jlong byteCount);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativePoolSize
  (JNIEnv *env, jobject obj, jstring path);

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeCopyFromByteArrayNT
  (JNIEnv *env, jobject obj, jbyteArray srcArray, jint srcIndex, jlong dst, jint byteCount);

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeSetMemoryNT
  (JNIEnv *env, jobject obj, jlong offset, jlong length, jbyte value);

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeCopyMemoryNT
  (JNIEnv *env, jobject obj, jlong src, jlong dst, jlong byteCount);
#ifdef __cplusplus
}
#endif
#endif
