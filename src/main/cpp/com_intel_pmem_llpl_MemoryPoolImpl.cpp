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

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativePoolSize
  (JNIEnv *env, jobject obj, jstring path)
{
    size_t heapSize;
    int is_pmemp;
    const char* native_string = env->GetStringUTFChars(path, 0);
    void *file = pmem_map_file(native_string, 0, 0, 0, &heapSize, &is_pmemp);
    pmem_unmap(file, heapSize);
    env->ReleaseStringUTFChars(path, native_string);
    return heapSize;
}

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeCopyFromByteArrayNT
  (JNIEnv *env, jobject obj, jbyteArray srcArray, jint srcIndex, jlong dst, jint byteCount)
{
    jbyte* src = env->GetByteArrayElements(srcArray, (jboolean *)0);
    void *addr = pmem_memcpy((void *)dst, src + srcIndex, byteCount, PMEM_F_MEM_NONTEMPORAL);
    env->ReleaseByteArrayElements(srcArray, src, 0);
}

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeSetMemoryNT
  (JNIEnv *env, jobject obj, jlong offset, jlong length, jbyte value)
{
    void *addr = pmem_memset((void *)offset, (int)value, length, PMEM_F_MEM_NONTEMPORAL);
}

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_MemoryPoolImpl_nativeCopyMemoryNT
  (JNIEnv *env, jobject obj, jlong src, jlong dst, jlong byteCount)
{
    void *addr = pmem_memcpy((void *)dst, (void *)src, byteCount, PMEM_F_MEM_NONTEMPORAL);
}

