/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

public class SetMemoryTest {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/persistent_pool";
        String tHeapName = "/mnt/mem/persistent_pool_tranactional";
        Heap heap = Heap.getHeap(heapName, 2_147_483_648L);
        TransactionalHeap tHeap = TransactionalHeap.getHeap(tHeapName, 100_000_000L);

        MemoryBlock rmb = heap.allocateMemoryBlock(120, true);
        UnboundedMemoryBlock umb = heap.allocateUnboundedMemoryBlock(120, true);
        TransactionalMemoryBlock tmb = tHeap.allocateMemoryBlock(120);

        rmb.setMemory((byte)0x44, 10, 50);
        for (int i = 0; i < 50; i++) {
            assert(rmb.getByte(10 + i) == (byte)0x44);
        }

        umb.setMemory((byte)0x44, 10, 50);
        for (int i = 0; i < 50; i++) {
            assert(umb.getByte(10 + i) == (byte)0x44);
        }

        tmb.setMemory((byte)0xcc, 50, 50);
        for (int i = 0; i < 50; i++) {
            assert(tmb.getByte(50 + i) == (byte)0xcc);
        }
        Heap.deleteHeap(heapName);
        TransactionalHeap.deleteHeap(heapName);
        System.out.println("================================= All SetMemory tests passed ===================================");
    }
}