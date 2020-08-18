/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

// Note: this code is EXPERIMENTAL 

#include "com_intel_pmem_llpl_MemoryPoolImpl.h"
#include <libpmem.h>

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeOpenPool
  (JNIEnv *env, jobject obj, jstring jpath, jlong size)
{
    const char* path = env->GetStringUTFChars(jpath, 0);

    size_t mappedSize = 0;
    int is_pmem = 0;
    int flags = PMEM_FILE_CREATE;
    long poolSize = (size_t)size; 
    void *poolAddress = pmem_map_file(path, poolSize, flags , 0666, &mappedSize, &is_pmem);
    if (poolAddress == 0) {
        printf("pmem_map_file failed, err = %s\n", pmem_errormsg());
    }
    env->ReleaseStringUTFChars(jpath, path);
    return (jlong)poolAddress;
}

JNIEXPORT int JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeClosePool
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong byteCount)
{
    int result = pmem_unmap((void *)poolAddress, (size_t)byteCount);
    if (result == -1) {
        printf("failed to close pool, err = %s\n", pmem_errormsg());
    }
    return result;
}

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeFlush
  (JNIEnv *env, jobject obj, jlong address, jlong byteCount)
{
    pmem_persist((const void*)address, byteCount);
}



