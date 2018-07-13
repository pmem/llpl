/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

public class ReallocateTest {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool2", 2147483648L);

        MemoryBlock<Raw> mb = h.allocateMemoryBlock(Raw.class, 500000000L);
        mb.setByte(1000000, (byte)0x50);
        mb = h.reallocateMemoryBlock(Raw.class, mb, 1000000000L);
        assert(mb.getByte(1000000) == (byte)0x50);

        boolean caught = false;
        try {
            h.reallocateMemoryBlock(Raw.class, mb, 3000000000L);
        } catch (PersistenceException e) {
            caught = true;
        }
        assert(caught);

        mb = h.reallocateMemoryBlock(Raw.class, mb, 500000000L);

        MemoryBlock<Raw> mb2 = h.allocateMemoryBlock(Raw.class, 1000000000L);
        caught = false;
        try {
            h.reallocateMemoryBlock(Raw.class, mb2, 1500000000L);
        } catch (PersistenceException e) {
            caught = true;
        }
        assert(caught);

        boolean didFree = Heap.freeHeap("/mnt/mem/persistent_pool2");
        assert(didFree);

        System.out.println("=================================All Reallocate tests passed=================================");
    }
}