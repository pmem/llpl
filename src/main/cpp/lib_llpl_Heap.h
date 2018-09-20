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

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeAllocate
  (JNIEnv *, jobject, jlong, jlong, jint);

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeOpenHeap
  (JNIEnv *, jobject, jstring, jlong, jintArray);

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeSetRoot
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeRealloc
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeGetRoot
  (JNIEnv *, jobject, long);

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeFree
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativePoolId
  (JNIEnv *env, jobject obj, jlong poolHandle);

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeDirectAddress
  (JNIEnv *env, jobject obj, jlong poolId, jlong offset);

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativePoolSize
  (JNIEnv *env, jobject obj, jstring path);
#ifdef __cplusplus
}
#endif
#endif
