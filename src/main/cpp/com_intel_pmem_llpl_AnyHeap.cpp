/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include "com_intel_pmem_llpl_AnyHeap.h"
#include "persistent_heap.h"
#include "libpmemobj/atomic_base.h"
#include <libpmempool.h>

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeCreateHeap
  (JNIEnv *env, jobject obj, jstring path, jlong size, jlongArray alloc_classes, jstring layout)
{
    const char* native_string = env->GetStringUTFChars(path, 0);
    const char* pool_layout_name = env->GetStringUTFChars(layout, 0);
    long poolHandle = (long)create_pool(env, native_string, (size_t)size, pool_layout_name);
    jboolean exceptionFlag = env->ExceptionCheck();
    if (exceptionFlag) return poolHandle;
    register_allocation_classes(env, (PMEMobjpool*)poolHandle, alloc_classes);
    env->ReleaseStringUTFChars(path, native_string);
    env->ReleaseStringUTFChars(layout, pool_layout_name);
    return poolHandle;
}

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeOpenHeap
  (JNIEnv *env, jobject obj, jstring path, jlongArray alloc_classes, jstring layout)
{
    const char* native_string = env->GetStringUTFChars(path, 0);
    const char* pool_layout_name = env->GetStringUTFChars(layout, 0);
    long poolHandle = (long)open_pool(env, native_string, pool_layout_name);
    jboolean exceptionFlag = env->ExceptionCheck();
    if (exceptionFlag) return poolHandle;
    register_allocation_classes(env, (PMEMobjpool*)poolHandle, alloc_classes);
    env->ReleaseStringUTFChars(path, native_string);
    env->ReleaseStringUTFChars(layout, pool_layout_name);
    return poolHandle;
}

JNIEXPORT void JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeCloseHeap
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    pmemobj_close(pool);
}

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeGetRoot
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    return pmemobj_root(pool, 64).off;
}

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeAllocateTransactional
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

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeAllocateAtomic
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

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeFree
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

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeFreeAtomic
  (JNIEnv *env, jobject obj, jlong block_direct_address)
{
    PMEMoid oid = pmemobj_oid((const void*)block_direct_address);
    TOID(char) bytes;
    TOID_ASSIGN(bytes, oid);
    POBJ_FREE(&bytes);
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeDirectAddress
  (JNIEnv *env, jobject obj, jlong poolId, jlong offset)
{
    PMEMoid oid = {(uint64_t)poolId, (uint64_t)offset};
    return (long)pmemobj_direct(oid);
}

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeUsableSize
  (JNIEnv *env, jobject obj, jlong addr)
{
    PMEMoid oid = pmemobj_oid((const void*)addr);
    return (long)pmemobj_alloc_usable_size(oid);
}

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeRegisterAllocationClass
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

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeHeapSize
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

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeHeapExists
  (JNIEnv *env, jobject obj, jstring path)
{
    enum pmempool_feature feature = PMEMPOOL_FEAT_CKSUM_2K;
    const char* native_string = env->GetStringUTFChars(path, 0);
    int ret = pmempool_feature_query(native_string, feature, 0);
    env->ReleaseStringUTFChars(path, native_string);
	return ret;
}

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeProbeHeapSize
  (JNIEnv *env, jobject obj, jlong poolId, jlong currentSize)
{
    // find maxHeap value
    long maxHeap = MAX_HEAP_SIZE;
    if (poolId + maxHeap < 0) {
        do {
            maxHeap = maxHeap / 2; 
        } while (poolId + maxHeap < 0);
        long delta = maxHeap;
        do {
            delta = delta / 2;
            maxHeap = maxHeap + delta;    
        } while (poolId + maxHeap > 0);
        maxHeap = maxHeap - delta;
    }
    long min = poolId + currentSize;
    long max = poolId + maxHeap;
    PMEMobjpool *pool = (PMEMobjpool*)poolId;
    for (long i = max; min < i; i = (max + min) / 2) {
        PMEMoid oid = pmemobj_oid((const void*)i);
        if (!OID_IS_NULL(oid) && pool == pmemobj_pool_by_oid(oid)) {
            min = i;
        }
        else {
            max = i;
        }
    }
    return min - poolId;
}

JNIEXPORT jlong JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeMinHeapSize
  (JNIEnv *env, jobject obj)
{
    return PMEMOBJ_MIN_POOL;
}

JNIEXPORT jint JNICALL Java_com_intel_pmem_llpl_AnyHeap_nativeRemovePool
  (JNIEnv *env, jobject obj, jstring path)
{
    const char* native_string = env->GetStringUTFChars(path, 0);
    jint res = pmempool_rm(native_string, PMEMPOOL_RM_FORCE);
    env->ReleaseStringUTFChars(path, native_string);
    return res;
}
