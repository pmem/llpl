/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include <libpmem.h>
#include "lib_llpl_Heap.h"
#include "persistent_heap.h"

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeOpenHeap
  (JNIEnv *env, jobject obj, jstring path, jlong size, jintArray alloc_classes)
{
    const char* native_string = env->GetStringUTFChars(path, 0);
    long poolHandle = (long)get_or_create_pool(native_string, (size_t)size);
    register_allocation_classes(env, (PMEMobjpool*)poolHandle, alloc_classes);
    env->ReleaseStringUTFChars(path, native_string);

    /*int enabled = 1;
    int ret = 0;
    if (ret = pmemobj_ctl_set((PMEMobjpool*)poolHandle, "stats.enabled", &enabled) != 0) {
        printf("unable to collect heap stats. error code %d\n", ret);
        fflush(stdout);
    }*/
    return poolHandle;
}

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeSetRoot
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong val)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    int ret = 0;
    TX_BEGIN(pool) {
        long *root_address = (long *)pmemobj_direct(pmemobj_root(pool, 8));
        pmemobj_tx_add_range_direct((const void *)root_address, 8);
        *root_address = val;
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeGetRoot
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    return (jlong)*(long *)pmemobj_direct(pmemobj_root(pool, 8));
}

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeAllocate
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong size, jint class_index)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    TOID(char) bytes = TOID_NULL(char);

    jlong ret = 0;
    TX_BEGIN(pool) {
        if (class_index == 0)
            bytes = TX_ZALLOC(char, (size_t)size);
        else {
            bytes = TX_XALLOC(char, (size_t)size, POBJ_XALLOC_ZERO | POBJ_CLASS_ID(class_index));
            }
        ret = bytes.oid.off;
    } TX_END
    // TODO: remove after debugging
    //printf("pmem alloc: requested = %llu, class_index = %d, usable = %llu\n", size, class_index, pmemobj_alloc_usable_size(bytes.oid));
    //fflush(stdout);
    return ret;
}

JNIEXPORT jint JNICALL Java_lib_llpl_Heap_nativeFree
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong block_direct_address)
{
    PMEMoid oid = pmemobj_oid((const void*)block_direct_address);
    TOID(char) bytes;
    TOID_ASSIGN(bytes, oid);
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;

    int ret = 0;
    TX_BEGIN(pool) {
        TX_FREE(bytes);
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativePoolId
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    PMEMoid test_oid;
    pmemobj_alloc(pool, &test_oid,  64, 0, NULL, 0);
    jlong poolId = (jlong)test_oid.pool_uuid_lo;
    pmemobj_free(&test_oid);
    return poolId;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativeDirectAddress
  (JNIEnv *env, jobject obj, jlong poolId, jlong offset)
{
    PMEMoid oid = {(uint64_t)poolId, (uint64_t)offset};
    return (long)pmemobj_direct(oid);
}

JNIEXPORT jlong JNICALL Java_lib_llpl_Heap_nativePoolSize
  (JNIEnv *env, jobject obj, jstring path)
{
    size_t poolSize;
    int is_pmemp;
    const char* native_string = env->GetStringUTFChars(path, 0);
    void *file = pmem_map_file(native_string, 0, 0, 0, &poolSize, &is_pmemp);
    pmem_unmap(file, poolSize);
    env->ReleaseStringUTFChars(path, native_string);
    return poolSize;
}
