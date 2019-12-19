/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

class MemoryBlockCollectionTest {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/persistent_pool";
        String dHeapName = "/mnt/mem/persistent_pool_durable";
        String tHeapName = "/mnt/mem/persistent_pool_transactional";
        Heap heap = Heap.exists(heapName) ? Heap.openHeap(heapName) : Heap.createHeap(heapName, 2147483648L);
        PersistentHeap dHeap = PersistentHeap.exists(dHeapName) ? PersistentHeap.openHeap(dHeapName) : PersistentHeap.createHeap(dHeapName, 100000000L);
        TransactionalHeap tHeap = TransactionalHeap.exists(tHeapName) ? TransactionalHeap.openHeap(tHeapName) : TransactionalHeap.createHeap(tHeapName, 100000000L);
        HashMap<AnyMemoryBlock, Integer> hm = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            hm.put(heap.allocateMemoryBlock(10, true), i);
        }
        assert(hm.size() == 10);
        for (int i = 0; i < 10; i++) {
            hm.put(dHeap.allocateMemoryBlock(10, true), i);
        }
        assert(hm.size() == 20);
        for (int i = 0; i < 10; i++) {
            hm.put(tHeap.allocateMemoryBlock(10), i);
        }
        assert(hm.size() == 30);
        for (Map.Entry<AnyMemoryBlock, Integer> e : hm.entrySet()) {
            // e.getKey().free(true);
        }
        new File("/mnt/mem/persistent_pool").delete();
        new File("/mnt/mem/persistent_pool_durable").delete();
        new File("/mnt/mem/persistent_pool_transactional").delete();
        System.out.println("================================= All MemoryBlockCollection tests passed =======================");
    }
}
