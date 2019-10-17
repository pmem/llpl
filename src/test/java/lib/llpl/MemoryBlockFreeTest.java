/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class MemoryBlockFreeTest {
    public static void main(String[] args) {
        Heap heap = Heap.getHeap("/mnt/mem/persistent_pool_base", 100_000_000L);
        MemoryBlock mb = heap.allocateMemoryBlock(10, true);
        assert(mb.isValid());
        heap.freeMemoryBlock(mb);
        assert(!mb.isValid());
        boolean caught = false;
        try {
            mb.checkValid();
        } catch (Exception e) {
            caught = true;
        }
        assert(caught);

        mb = heap.allocateMemoryBlock(10, false);
        assert(mb.isValid());
        mb.free(true);
        assert(!mb.isValid());
        caught = false;
        try {
            mb.checkValid();
        } catch (Exception e) {
            caught = true;
        }
        assert(caught);

        PersistentHeap pHeap = PersistentHeap.getHeap("/mnt/mem/persistent_pool_durable", 100_000_000L);
        PersistentMemoryBlock dmb = pHeap.allocateMemoryBlock(10, true);
        assert(dmb.isValid());
        dmb.free(true);
        assert(!dmb.isValid());
        caught = false;
        try {
            dmb.checkValid();
        } catch (IllegalStateException e) {
            caught = true;
        }
        assert(caught);

        TransactionalHeap tHeap = TransactionalHeap.getHeap("/mnt/mem/persistent_pool_transactional", 100_000_000L);
        TransactionalMemoryBlock tmb = tHeap.allocateMemoryBlock(10);
        assert(tmb.isValid());
        tmb.free();
        assert(!tmb.isValid());
        caught = false;
        try {
            tmb.checkValid();
        } catch (IllegalStateException e) {
            caught = true;
        }
        assert(caught);
        System.out.println("================================= All MemoryBlockFree tests passed =============================");
    }
}
