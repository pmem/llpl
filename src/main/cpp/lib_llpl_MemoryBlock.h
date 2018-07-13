/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include <jni.h>

#ifndef _Included_lib_llpl_MemoryBlock
#define _Included_lib_llpl_MemoryBlock
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeCopyBlockToBlock
  (JNIEnv *, jobject, jlong, jlong, jlong, jlong, jlong);

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeCopyFromByteArray
  (JNIEnv *, jobject, jbyteArray, jint, jlong, jlong, jint);

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeSetMemory
  (JNIEnv *, jobject, jlong, jlong, jint, jlong);

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalByte
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong address, jbyte value);

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalShort
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong address, jshort value);

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalInt
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong address, jint value);

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalLong
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong address, jlong value);

JNIEXPORT jlong JNICALL Java_lib_llpl_MemoryBlock_nativeFlush
  (JNIEnv *env, jobject obj, jlong address, jlong size);

JNIEXPORT jlong JNICALL Java_lib_llpl_MemoryBlock_addToTransaction
  (JNIEnv *env, jobject obj, jlong address, jlong size);

void throw_persistence_exception(JNIEnv *env, const char* arg);

#ifdef __cplusplus
}
#endif
#endif
