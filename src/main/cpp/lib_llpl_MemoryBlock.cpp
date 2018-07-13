/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

#include "lib_llpl_MemoryBlock.h"
#include "persistent_heap.h"
#include <libpmemobj.h>
#include <libpmem.h>

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeCopyBlockToBlock
  (JNIEnv *env, jobject obj, jlong src_block_direct_address, jlong src_offset, jlong dest_block_direct_address, jlong dest_offset, jlong length)
{
    memcpy((void*)(dest_block_direct_address + dest_offset), (void*)(src_block_direct_address + src_offset), (uint64_t)length);
    return 0;
}

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeCopyFromByteArray
  (JNIEnv *env, jobject obj, jbyteArray src_array, jint src_offset, jlong dest_block_direct_address, jlong dest_offset, jint length)
{
    jboolean is_copy;
    jbyte* bytes = env->GetByteArrayElements(src_array, &is_copy);
    memcpy((void*)(dest_block_direct_address + dest_offset), (void*)(bytes + src_offset), length);
    return 0;
}

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeSetMemory
  (JNIEnv *env, jobject obj, jlong block_direct_address, jlong offset, jint val, jlong length)
{
    memset((void*)(block_direct_address + offset), val, (size_t)length);
    return 0;
}


JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalByte
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong address, jbyte value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
    TX_BEGIN(pool) {
        char *ptr = (char*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write byte value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalShort
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong address, jshort value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
    TX_BEGIN(pool) {
        int16_t *ptr = (int16_t*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write int value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalInt
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong address, jint value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
    TX_BEGIN(pool) {
        int *ptr = (int*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write int value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalLong
  (JNIEnv *env, jobject obj, jlong poolAddress, jlong address, jlong value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolAddress;
    TX_BEGIN(pool) {
        long *ptr = (long*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write long value.");    
    } TX_END
}

JNIEXPORT jlong JNICALL Java_lib_llpl_MemoryBlock_nativeFlush
  (JNIEnv *env, jobject obj, jlong address, jlong size)
{
    pmem_persist((const void*)address, size);
}

JNIEXPORT jlong JNICALL Java_lib_llpl_MemoryBlock_addToTransaction
  (JNIEnv *env, jobject obj, jlong address, jlong size)
{
    pmemobj_tx_add_range_direct((const void *)address, (size_t)size);
}

void throw_persistence_exception(JNIEnv *env, const char* arg)
{
    char className[50] = "lib/llpl/PersistenceException";
    jclass exClass = env->FindClass(className);

    char errmsg[250];
    strcpy(errmsg, arg);
    strcat(errmsg, pmemobj_errormsg());
    env->ThrowNew(exClass, errmsg);
}

