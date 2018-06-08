/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

class MemoryBlockCollectionTest {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
        HashMap<MemoryBlock<?>, Integer> hm = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            hm.put(h.allocateMemoryBlock(Raw.class, 10), i);
        }
        assert(hm.size() == 10);
        for (int i = 0; i < 10; i++) {
            hm.put(h.allocateMemoryBlock(Transactional.class, 10), i);
        }
        assert(hm.size() == 20);
        for (int i = 0; i < 10; i++) {
            hm.put(h.allocateMemoryBlock(Flushable.class, 10), i);
        }
        assert(hm.size() == 30);
        for (Map.Entry<MemoryBlock<?>, Integer> e : hm.entrySet()) {
            // System.out.println(e.getKey().address() + ", " + e.getKey().getClass() + " --> " + e.getValue());
            h.freeMemoryBlock(e.getKey());
        }

        TreeMap<MemoryBlock<?>, Integer> tm = new TreeMap<>();
        for (int i = 0; i < 10; i++) {
            tm.put(h.allocateMemoryBlock(Raw.class, 10), i);
        }
        assert(tm.size() == 10);
        for (int i = 0; i < 10; i++) {
            tm.put(h.allocateMemoryBlock(Transactional.class, 10), i);
        }
        assert(tm.size() == 20);
        for (int i = 0; i < 10; i++) {
            tm.put(h.allocateMemoryBlock(Flushable.class, 10), i);
        }
        assert(tm.size() == 30);
        for (Map.Entry<MemoryBlock<?>, Integer> e : tm.entrySet()) {
            // System.out.println(e.getKey().address() + ", " + e.getKey().getClass() + " --> " + e.getValue());
            h.freeMemoryBlock(e.getKey());
        }
        System.out.println("=================================All MemoryBlockCollection tests passed=====================");
    }
}
