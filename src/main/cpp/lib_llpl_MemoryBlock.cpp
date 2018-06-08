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

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeMemoryBlockMemcpyRaw
  (JNIEnv *env, jobject obj, jlong src_block, jlong src_offset, jlong dest_block, jlong dest_offset, jlong length)
{
    PMEMoid src_oid = {get_uuid_lo(), (uint64_t)src_block};
    PMEMoid dest_oid = {get_uuid_lo(), (uint64_t)dest_block};

    void* src = (void*)((uint64_t)pmemobj_direct(src_oid)+(uint64_t)src_offset);
    void* dest = (void*)((uint64_t)pmemobj_direct(dest_oid)+(uint64_t)dest_offset);

    memcpy(dest, src, (uint64_t)length);
    return 0;
}

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeFromByteArrayMemcpyRaw
  (JNIEnv *env, jobject obj, jbyteArray src_array, jint src_offset, jlong dest_block, jlong dest_offset, jint length)
{
    PMEMoid dest_oid = {get_uuid_lo(), (uint64_t)dest_block};
    jbyte* dest = (jbyte*)((void*)((uint64_t)pmemobj_direct(dest_oid)+(uint64_t)dest_offset));

    jboolean is_copy;
    jbyte* bytes = env->GetByteArrayElements(src_array, &is_copy);

    memcpy((void*)dest, (void*)(bytes+src_offset), length);
    return 0;
}

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeMemoryBlockMemsetRaw
  (JNIEnv *env, jobject obj, jlong block, jlong offset, jint val, jlong length)
{
    PMEMoid block_oid = {get_uuid_lo(), (uint64_t)block};
    void* dest = (void*)((uint64_t)pmemobj_direct(block_oid)+(uint64_t)offset);
    memset(dest, val, (size_t)length);
    return 0;
}

JNIEXPORT jint JNICALL Java_lib_llpl_MemoryBlock_nativeSetSize
  (JNIEnv *env, jobject obj, jlong block, jlong offset, jlong size)
{
    PMEMoid oid = {get_uuid_lo(), (uint64_t)block};
    void* dest = (void*)((uint64_t)pmemobj_direct(oid)+(uint64_t)offset);
    void* src = &size;

    int ret = 0;
    TX_BEGIN(pool) {
        TX_MEMCPY(dest, src, 8);
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalByte
  (JNIEnv *env, jobject obj, jlong address, jbyte value)
{
    TX_BEGIN(pool) {
        char *ptr = (char*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write byte value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalShort
  (JNIEnv *env, jobject obj, jlong address, jshort value)
{
    TX_BEGIN(pool) {
        int16_t *ptr = (int16_t*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write int value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalInt
  (JNIEnv *env, jobject obj, jlong address, jint value)
{
    TX_BEGIN(pool) {
        int *ptr = (int*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write int value.");    
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalLong
  (JNIEnv *env, jobject obj, jlong address, jlong value)
{
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

