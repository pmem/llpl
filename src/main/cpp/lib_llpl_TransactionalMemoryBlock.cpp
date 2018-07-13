/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "lib_llpl_TransactionalMemoryBlock.h"
#include "persistent_heap.h"

JNIEXPORT jint JNICALL Java_lib_llpl_TransactionalMemoryBlock_nativeCopyBlockToBlock
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong src_block_direct_address, jlong src_offset, jlong dest_block_direct_address, jlong dest_offset, jlong length)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
    int ret = 0;
    TX_BEGIN(pool) {
        TX_MEMCPY((void*)(dest_block_direct_address + dest_offset), (void*)(src_block_direct_address + src_offset), (uint64_t)length);
    } TX_ONABORT {
        ret = -1;
    } TX_END
    return ret;
}

JNIEXPORT jint JNICALL Java_lib_llpl_TransactionalMemoryBlock_nativeCopyFromByteArray
  (JNIEnv *env, jobject obj, jlong poolAddress, jbyteArray src_array, jint src_offset, jlong dest_block_direct_address, jlong dest_offset, jint length)
{
    jboolean is_copy;
    jbyte* bytes = env->GetByteArrayElements(src_array, &is_copy);
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
    int ret = 0;
    TX_BEGIN(pool) {
        TX_MEMCPY((void*)(dest_block_direct_address + dest_offset), (void*)(bytes+src_offset), length);
    } TX_ONABORT {
        ret = -1;
    } TX_FINALLY {
        if (is_copy) env->ReleaseByteArrayElements(src_array, bytes, 0);
    } TX_END
    return ret;
}

JNIEXPORT jint JNICALL Java_lib_llpl_TransactionalMemoryBlock_nativeSetMemory
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong block_direct_address, jlong offset, jint val, jlong length)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
    int ret = 0;
    TX_BEGIN(pool) {
        TX_MEMSET((void*)(block_direct_address + offset), val, (size_t)length);
    } TX_ONABORT {
        ret = -1;
    } TX_END
    return ret;
}

