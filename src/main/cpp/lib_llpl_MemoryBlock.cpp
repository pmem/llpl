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

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalByte
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jbyte value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    TX_BEGIN(pool) {
    TX_ADD_DIRECT((char*)address);
        char *ptr = (char*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write byte value.");
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalShort
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jshort value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    TX_BEGIN(pool) {
    TX_ADD_DIRECT((int16_t*)address);
        int16_t *ptr = (int16_t*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write int value.");
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalInt
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jint value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    TX_BEGIN(pool) {
    TX_ADD_DIRECT((int*)address);
        int *ptr = (int*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write int value.");
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeSetTransactionalLong
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong value)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    TX_BEGIN(pool) {
    TX_ADD_DIRECT((long*)address);
        long *ptr = (long*)address;
        *ptr = value;
    } TX_ONABORT {
        throw_persistence_exception(env, "Failed to write long value.");
    } TX_END
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeFlush
  (JNIEnv *env, jobject obj, jlong address, jlong size)
{
    pmem_persist((const void*)address, size);
}

JNIEXPORT void JNICALL Java_lib_llpl_MemoryBlock_nativeAddToTransaction
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

