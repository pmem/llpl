/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include <jni.h>

#ifndef _Included_lib_llpl_Transaction
#define _Included_lib_llpl_Transaction
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeStartTransaction
  (JNIEnv *, jobject, jlong);


JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeEndTransaction
  (JNIEnv *, jobject);


JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeCommitTransaction
  (JNIEnv *, jobject);


JNIEXPORT void JNICALL Java_lib_llpl_Transaction_nativeAbortTransaction
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
