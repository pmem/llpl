/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include <jni.h>

#ifndef _Included_com_intel_pmem_llpl_Heap
#define _Included_com_intel_pmem_llpl_Heap
#ifdef __cplusplus
extern "C" {
#endif

const size_t MAX_HEAP_SIZE = 12 * 1024 * 1024 * 1024L * 1024L;

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeAllocateTransactional
  (JNIEnv *, jobject, jlong, jlong, jint);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeAllocateAtomic
  (JNIEnv *, jobject, jlong, jlong, jint);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeCreateHeap
  (JNIEnv *, jobject, jstring, jlong, jlongArray, jstring);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeOpenHeap
  (JNIEnv *, jobject, jstring, jlongArray, jstring);

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeCloseHeap
  (JNIEnv *env, jobject obj, jlong poolHandle);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeRealloc
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeGetRoot
  (JNIEnv *, jobject, long);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeFree
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeFreeAtomic
  (JNIEnv *env, jobject obj, jlong block_direct_address);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeUsableSize
  (JNIEnv *env, jobject obj, jlong addr);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeDirectAddress
  (JNIEnv *env, jobject obj, jlong poolId, jlong offset);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeRegisterAllocationClass
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong size);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeHeapSize
  (JNIEnv *env, jobject obj, jstring path);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeProbeHeapSize
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong currentSize);

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeMinHeapSize
  (JNIEnv *env, jobject obj);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeRemovePool
  (JNIEnv *env, jobject obj, jstring path);

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeHeapExists
  (JNIEnv *env, jobject obj, jstring path);

#ifdef __cplusplus
}
#endif
#endif
