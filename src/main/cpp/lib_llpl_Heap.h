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
  (JNIEnv *, jobject, jlong);

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeOpenHeap
  (JNIEnv *, jobject, jstring, jlong);

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeSetRoot
  (JNIEnv *, jobject, jlong);

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeRealloc
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeGetRoot
  (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeFree
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
