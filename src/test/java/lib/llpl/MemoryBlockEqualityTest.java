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
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
        MemoryBlock<?> mb = h.allocateMemoryBlock(Raw.class, 10);
        assert(mb.address() != 0);
        MemoryBlock<?> mb2 = h.memoryBlockFromAddress(Raw.class, mb.address());
        assert(mb.address() == mb2.address());
        assert(mb.equals(mb2));

        HashMap<MemoryBlock<?>, Integer> hm = new HashMap<>();
        assert(hm.size() == 0);
        hm.put(mb, 1);
        hm.put(mb2, 2);
        assert(hm.size() == 1);
        assert(hm.get(mb) == 2);

        h.freeMemoryBlock(mb);

        mb = h.allocateMemoryBlock(Flushable.class, 10);
        assert(mb.address() != 0);
        mb2 = h.memoryBlockFromAddress(Flushable.class, mb.address());
        assert(mb.address() == mb2.address());
        assert(mb.equals(mb2));

        hm = new HashMap<>();
        assert(hm.size() == 0);
        hm.put(mb, 1);
        hm.put(mb2, 2);
        assert(hm.size() == 1);
        assert(hm.get(mb) == 2);

        h.freeMemoryBlock(mb);

        mb = h.allocateMemoryBlock(Transactional.class, 10);
        assert(mb.address() != 0);
        mb2 = h.memoryBlockFromAddress(Transactional.class, mb.address());
        assert(mb.address() == mb2.address());
        assert(mb.equals(mb2));

        hm = new HashMap<>();
        assert(hm.size() == 0);
        hm.put(mb, 1);
        hm.put(mb2, 2);
        assert(hm.size() == 1);
        assert(hm.get(mb) == 2);

        h.freeMemoryBlock(mb);

        System.out.println("=================================All MemoryBlockEquality tests passed=======================");
    }
}
