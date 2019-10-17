/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package lib.llpl;

import java.util.Map;
import java.util.HashMap;

class MemoryBlockCollectionTest {
    public static void main(String[] args) {
        Heap heap = Heap.getHeap("/mnt/mem/persistent_pool", 100_000_000L);
        PersistentHeap dHeap = PersistentHeap.getHeap("/mnt/mem/persistent_pool_durable", 100_000_000L);
        TransactionalHeap tHeap = TransactionalHeap.getHeap("/mnt/mem/persistent_pool_transactional", 100_000_000L);
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
        System.out.println("================================= All MemoryBlockCollection tests passed =======================");
    }
}
