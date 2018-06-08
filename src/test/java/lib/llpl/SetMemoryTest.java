/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

public class SetMemoryTest {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);

        MemoryBlock<Raw> rmb = h.allocateMemoryBlock(Raw.class, 120);
        MemoryBlock<Flushable> fmb = h.allocateMemoryBlock(Flushable.class, 120);
        MemoryBlock<Transactional> tmb = h.allocateMemoryBlock(Transactional.class, 120);

        h.setMemory(rmb, (byte)0x44, 10, 50);
        for (int i = 0; i < 50; i++) {
            assert(rmb.getByte(10 + i) == (byte)0x44);
        }

        assert(fmb.isFlushed() == true);
        h.setMemory(fmb, (byte)0x88, 30, 50);
        assert(fmb.isFlushed() == false);
        for (int i = 0; i < 50; i++) {
            assert(fmb.getByte(30 + i) == (byte)0x88);
        }

        h.setMemory(tmb, (byte)0xcc, 50, 50);
        for (int i = 0; i < 50; i++) {
            assert(tmb.getByte(50 + i) == (byte)0xcc);
        }
        System.out.println("=================================All SetMemory tests passed==================================");
    }
}