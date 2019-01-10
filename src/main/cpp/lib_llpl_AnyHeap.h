/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include <jni.h>

#ifndef _Included_lib_llpl_Heap
#define _Included_lib_llpl_Heap
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeAllocateTransactional
  (JNIEnv *, jobject, jlong, jlong, jint);

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeAllocateAtomic
  (JNIEnv *, jobject, jlong, jlong, jint);

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeOpenHeap
  (JNIEnv *, jobject, jstring, jlong, jlongArray);

JNIEXPORT void JNICALL Java_lib_llpl_AnyHeap_nativeCloseHeap
  (JNIEnv *env, jobject obj, jlong poolHandle);

JNIEXPORT jint JNICALL Java_lib_llpl_AnyHeap_nativeSetRoot
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT jint JNICALL Java_lib_llpl_AnyHeap_nativeRealloc
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeGetRoot
  (JNIEnv *, jobject, long);

JNIEXPORT jint JNICALL Java_lib_llpl_AnyHeap_nativeFree
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT jint JNICALL Java_lib_llpl_AnyHeap_nativeFreeAtomic
  (JNIEnv *env, jobject obj, jlong block_direct_address);

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeDirectAddress
  (JNIEnv *env, jobject obj, jlong poolId, jlong offset);

JNIEXPORT jint JNICALL Java_lib_llpl_AnyHeap_nativeRegisterAllocationClass
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong size);

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeHeapSize
  (JNIEnv *env, jobject obj, jstring path);
#ifdef __cplusplus
}
#endif
#endif
