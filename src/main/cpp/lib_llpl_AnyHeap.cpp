/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include "lib_llpl_AnyHeap.h"
#include "persistent_heap.h"

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeOpenHeap
  (JNIEnv *env, jobject obj, jstring path, jlong size, jlongArray alloc_classes)
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

JNIEXPORT void JNICALL Java_lib_llpl_AnyHeap_nativeCloseHeap
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    pmemobj_close(pool);
}

JNIEXPORT jint JNICALL Java_lib_llpl_AnyHeap_nativeSetRoot
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

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeGetRoot
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    return (jlong)*(long *)pmemobj_direct(pmemobj_root(pool, 8));
}

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeAllocateTransactional
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
    return ret;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeAllocateAtomic
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong size, jint class_index)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;

    jlong ret = 0;
    if (class_index == 0) {
        TOID(char) bytes = TOID_NULL(char);
        POBJ_ZALLOC(pool, &bytes, char, (size_t)size);
        ret = bytes.oid.off;
    }
    else {
        PMEMoid bytes = OID_NULL;
        pmemobj_xalloc(pool, &bytes, (size_t)size, 0, POBJ_XALLOC_ZERO | POBJ_CLASS_ID(class_index), NULL, NULL);
        ret = bytes.off;
    }
   return ret;
}

JNIEXPORT jint JNICALL Java_lib_llpl_AnyHeap_nativeFree
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
        printf("nativeFree error: %s\n", pmemobj_errormsg());
        ret = -1;
    } TX_END
    return ret;
}

JNIEXPORT jint JNICALL Java_lib_llpl_AnyHeap_nativeFreeAtomic
  (JNIEnv *env, jobject obj, jlong block_direct_address)
{
    PMEMoid oid = pmemobj_oid((const void*)block_direct_address);
    TOID(char) bytes;
    TOID_ASSIGN(bytes, oid);
    POBJ_FREE(&bytes);
    return 0;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeDirectAddress
  (JNIEnv *env, jobject obj, jlong poolId, jlong offset)
{
    PMEMoid oid = {(uint64_t)poolId, (uint64_t)offset};
    return (long)pmemobj_direct(oid);
}

JNIEXPORT jint JNICALL Java_lib_llpl_AnyHeap_nativeRegisterAllocationClass
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong size)
{
    struct pobj_alloc_class_desc custom_alloc_class;

    custom_alloc_class.header_type = POBJ_HEADER_NONE;
    custom_alloc_class.unit_size = size;
    custom_alloc_class.units_per_block = 5000;
    custom_alloc_class.alignment = 0;

    int ret = pmemobj_ctl_set((PMEMobjpool*)poolHandle, "heap.alloc_class.new.desc", &custom_alloc_class);
    if (ret == 0) {
        return custom_alloc_class.class_id;
    } else 
        return -1;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_AnyHeap_nativeHeapSize
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
