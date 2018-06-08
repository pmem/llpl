/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "lib_llpl_Heap.h"
#include "persistent_heap.h"

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeAllocate
  (JNIEnv *env, jobject obj, jlong size)
{
    TOID(char) bytes = TOID_NULL(char);

    jlong ret = 0;
    TX_BEGIN(pool) {
        bytes = TX_ZALLOC(char, (size_t)size);
        ret = bytes.oid.off;
    } TX_END

    return ret;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeOpenHeap
  (JNIEnv *env, jobject obj, jstring path, jlong size)
{
    const char* native_string = env->GetStringUTFChars(path, 0);
    long poolAddress = (long)get_or_create_pool(native_string, (size_t)size);
    env->ReleaseStringUTFChars(path, native_string);
    return poolAddress;
}

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeSetRoot
  (JNIEnv *env, jobject obj, jlong val)
{
    int ret = 0;
    TX_BEGIN(pool) {
        D_RW(root)->root_val = (uint64_t)val;
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeRealloc
  (JNIEnv *env, jobject obj, jlong offset, jlong new_size)
{
    PMEMoid oid = {get_uuid_lo(), (uint64_t)offset};
    TOID(char) bytes;
    TOID_ASSIGN(bytes, oid);

    int ret = 0;
    TX_BEGIN(pool) {
        TX_REALLOC(bytes, new_size);
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}


JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeGetRoot
  (JNIEnv *env, jobject obj)
{
    return D_RO(root)->root_val;
}

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeFree
  (JNIEnv *env, jobject obj, jlong block_offset)
{
    PMEMoid oid = {get_uuid_lo(), (uint64_t)block_offset};
    TOID(char) bytes;
    TOID_ASSIGN(bytes, oid);

    int ret = 0;
    TX_BEGIN(pool) {
        TX_FREE(bytes);
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}
