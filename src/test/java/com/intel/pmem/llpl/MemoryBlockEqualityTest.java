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

class MemoryBlockEqualityTest {
    public static void main(String[] args) {
        HashMap<AnyMemoryBlock, Integer> map = new HashMap<>();
        {
            String heapName = "/mnt/mem/persistent_pool";
            Heap heap = Heap.exists(heapName) ? Heap.openHeap(heapName) : Heap.createHeap(heapName, 2147483648L);
            MemoryBlock mb = heap.allocateMemoryBlock(10, false);
            assert(mb.handle() != 0);
            MemoryBlock mb2 = heap.memoryBlockFromHandle(mb.handle());
            assert(mb.handle() == mb2.handle());
            assert(mb.equals(mb2));

            assert(map.size() == 0);
            map.put(mb, 1);
            map.put(mb2, 2);
            assert(map.size() == 1);
            assert(map.get(mb) == 2);

            mb.free(false);
            new File(heapName).delete();
        }

        {
            String heapName = "/mnt/mem/persistent_pool_durable";
            PersistentHeap heap = PersistentHeap.exists(heapName) ? PersistentHeap.openHeap(heapName) : PersistentHeap.createHeap(heapName, 2147483648L);
            PersistentMemoryBlock mb = heap.allocateMemoryBlock(10, false);
            assert(mb.handle() != 0);
            PersistentMemoryBlock mb2 = heap.memoryBlockFromHandle(mb.handle());
            assert(mb.handle() == mb2.handle());
            assert(mb.equals(mb2));

            map = new HashMap<>();
            assert(map.size() == 0);
            map.put(mb, 1);
            map.put(mb2, 2);
            assert(map.size() == 1);
            assert(map.get(mb) == 2);

            mb.free(false);
            new File(heapName).delete();
        }

        {
            String heapName = "/mnt/mem/persistent_pool_transactional";
            TransactionalHeap heap = TransactionalHeap.exists(heapName) ? TransactionalHeap.openHeap(heapName) : TransactionalHeap.createHeap(heapName, 2147483648L);
            TransactionalMemoryBlock mb = heap.allocateMemoryBlock(10);
            assert(mb.handle() != 0);
            TransactionalMemoryBlock mb2 = heap.memoryBlockFromHandle(mb.handle());
            assert(mb.handle() == mb2.handle());
            assert(mb.equals(mb2));

            map = new HashMap<>();
            assert(map.size() == 0);
            map.put(mb, 1);
            map.put(mb2, 2);
            assert(map.size() == 1);
            assert(map.get(mb) == 2);

            mb.free();
            new File(heapName).delete();
        }
        
        System.out.println("================================= All MemoryBlockEquality tests passed =========================");
    }
}
