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

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeSetTransactionalByte
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jbyte value);

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeSetTransactionalShort
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jshort value);

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeSetTransactionalInt
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jint value);

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeSetTransactionalLong
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong value);

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeFlush
  (JNIEnv *env, jobject obj, jlong address, jlong size);

JNIEXPORT int JNICALL Java_lib_llpl_AnyMemoryBlock_nativeAddToTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size);

JNIEXPORT void JNICALL Java_lib_llpl_AnyMemoryBlock_nativeAddToTransactionNoCheck
  (JNIEnv *env, jobject obj, jlong address, jlong size);

JNIEXPORT int JNICALL Java_lib_llpl_AnyMemoryBlock_nativeAddRangeToTransaction
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size);

void throw_persistence_exception(JNIEnv *env, const char* arg);

#ifdef __cplusplus
}
#endif
#endif
