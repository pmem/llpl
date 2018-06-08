/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "lib_llpl_TransactionalMemoryBlock.h"
#include "persistent_heap.h"

JNIEXPORT jint JNICALL Java_lib_llpl_TransactionalMemoryBlock_nativeMemoryBlockMemcpyTransactional
  (JNIEnv *env, jobject obj, jlong src_block, jlong src_offset, jlong dest_block, jlong dest_offset, jlong length)
{
    PMEMoid src_oid = {get_uuid_lo(), (uint64_t)src_block};
    PMEMoid dest_oid = {get_uuid_lo(), (uint64_t)dest_block};

    void* src = (void*)((uint64_t)pmemobj_direct(src_oid)+(uint64_t)src_offset);
    void* dest = (void*)((uint64_t)pmemobj_direct(dest_oid)+(uint64_t)dest_offset);

    int ret = 0;
    TX_BEGIN(pool) {
        TX_MEMCPY(dest, src, (uint64_t)length);
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}

JNIEXPORT jint JNICALL Java_lib_llpl_TransactionalMemoryBlock_nativeFromByteArrayMemcpyTransactional
  (JNIEnv *env, jobject obj, jbyteArray src_array, jint src_offset, jlong dest_block, jlong dest_offset, jint length)
{
    PMEMoid dest_oid = {get_uuid_lo(), (uint64_t)dest_block};
    jbyte* dest = (jbyte*)((void*)((uint64_t)pmemobj_direct(dest_oid)+(uint64_t)dest_offset));

    jboolean is_copy;
    jbyte* bytes = env->GetByteArrayElements(src_array, &is_copy);

    int ret = 0;
    TX_BEGIN(pool) {
        TX_MEMCPY((void*)dest, (void*)(bytes+src_offset), length);
    } TX_ONABORT {
        ret = -1;
    } TX_FINALLY {
        if (is_copy) env->ReleaseByteArrayElements(src_array, bytes, 0);
    } TX_END

    return ret;
}
JNIEXPORT jint JNICALL Java_lib_llpl_TransactionalMemoryBlock_nativeMemoryBlockMemsetTransactional
  (JNIEnv *env, jobject obj, jlong block, jlong offset, jint val, jlong length)
{
    PMEMoid block_oid = {get_uuid_lo(), (uint64_t)block};
    void* dest = (void*)((uint64_t)pmemobj_direct(block_oid)+(uint64_t)offset);

    int ret = 0;
    TX_BEGIN(pool) {
        TX_MEMSET(dest, val, (size_t)length);
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}
