/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include "persistent_heap.h"

const char *pool_layout_name = "llpl_persistent_heap";

PMEMobjpool* create_pool(const char* path, size_t size)
{
    return pmemobj_create(path, pool_layout_name, size, S_IRUSR | S_IWUSR);
}

PMEMobjpool* open_pool(const char* path)
{
    return pmemobj_open(path, pool_layout_name);
}

PMEMobjpool* get_or_create_pool(const char* path, size_t size)
{
    PMEMobjpool *pool = open_pool(path);
    if (pool == NULL) pool = create_pool(path, size);
    return pool;
}

void register_allocation_classes(JNIEnv *env, PMEMobjpool *pool, jlongArray alloc_classes)
{
    //jsize len = (env)->GetArrayLength(alloc_classes);
    jlong *j_arr = (env)->GetLongArrayElements(alloc_classes, 0);
    const int custom_index = 15;
    struct pobj_alloc_class_desc alloc_class;
    for (int i=0; i<custom_index-1; i++) {
        int size = (8*(1+i));
        alloc_class.header_type = POBJ_HEADER_NONE;
        alloc_class.unit_size = size;
        alloc_class.units_per_block = 5000;
        alloc_class.alignment = 0;

        int ret = pmemobj_ctl_set(pool, "heap.alloc_class.new.desc", &alloc_class);
        if (ret == 0) {
            j_arr[i] = alloc_class.class_id;
         //   printf("succeeded in registering alloc class of size %d, with id %d\n",size,alloc_class.class_id);
         //   fflush(stdout);
        }
        /*else {
            printf("failed to register alloc class of size %d\n",size);
            fflush(stdout);
        }*/
    }

    /*int ret = 0;
    char abuf[30];
    for (int i=0; i<255; i++) {
        struct pobj_alloc_class_desc alloc_class_query;
        sprintf(abuf, "heap.alloc_class.%d.desc",i);
        //printf("%s\n", abuf);
        ret = pmemobj_ctl_get(pool, abuf, &alloc_class_query);
        fflush(stdout);
        if (ret == 0) {
            printf("alloc class with id %d, has alloc size of %llu and  %llu units per block\n",i,alloc_class_query.unit_size, alloc_class_query.units_per_block);
            fflush(stdout);
        }

    }*/
    env->ReleaseLongArrayElements(alloc_classes, j_arr, 0);
}
