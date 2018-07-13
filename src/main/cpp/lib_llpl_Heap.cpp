/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "lib_llpl_Heap.h"
#include "persistent_heap.h"

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeAllocate
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong size)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
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
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong val)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
    int ret = 0;
    TX_BEGIN(pool) {
        long *root_address = (long *)pmemobj_direct(pmemobj_root(pool, 0));
        pmemobj_tx_add_range_direct((const void *)root_address, 8);
        *root_address = val;
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeGetRoot
  (JNIEnv *env, jobject obj, jlong poolAddress)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
    return (jlong)(pmemobj_root(pool, 0).off); 
}

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeFree
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong block_direct_address)
{
    PMEMoid oid = pmemobj_oid((const void*)block_direct_address);
    TOID(char) bytes;
    TOID_ASSIGN(bytes, oid);
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;

    int ret = 0;
    TX_BEGIN(pool) {
        TX_FREE(bytes);
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}
