/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include <jni.h>

#ifndef _Included_lib_llpl_TransactionalMemoryBlock
#define _Included_lib_llpl_TransactionalMemoryBlock
#ifdef __cplusplus
extern "C" {
#endif
	
JNIEXPORT jint JNICALL Java_lib_llpl_TransactionalMemoryBlock_nativeMemoryBlockMemcpyTransactional
  (JNIEnv *, jobject, jlong, jlong, jlong, jlong, jlong);

JNIEXPORT jint JNICALL Java_lib_llpl_TransactionalMemoryBlock_nativeFromByteArrayMemcpyTransactional
  (JNIEnv *, jobject, jbyteArray, jint, jlong, jlong, jint);

JNIEXPORT jint JNICALL Java_lib_llpl_TransactionalMemoryBlock_nativeMemoryBlockMemsetTransactional
  (JNIEnv *, jobject, jlong, jlong, jint, jlong);

#ifdef __cplusplus
}
#endif
#endif
