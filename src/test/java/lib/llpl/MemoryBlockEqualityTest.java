/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package lib.llpl;

import java.util.Map;
import java.util.HashMap;

class MemoryBlockEqualityTest {
    public static void main(String[] args) {
        HashMap<AnyMemoryBlock, Integer> map = new HashMap<>();

        {
            Heap heap = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
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
            heap.deleteHeap("/mnt/mem/persistent_pool");
        }

        {
            PersistentHeap heap = PersistentHeap.getHeap("/mnt/mem/persistent_pool_durable", 2147483648L);
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
            heap.deleteHeap("/mnt/mem/persistent_pool_durable");
        }

        {
            TransactionalHeap heap = TransactionalHeap.getHeap("/mnt/mem/persistent_pool_transactional", 2147483648L);
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
            heap.deleteHeap("/mnt/mem/persistent_pool_transactional");
        }
        
        System.out.println("================================= All MemoryBlockEquality tests passed =========================");
    }
}
