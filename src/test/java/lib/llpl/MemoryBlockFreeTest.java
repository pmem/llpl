/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class MemoryBlockFreeTest {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
        MemoryBlock<?> mb = h.allocateMemoryBlock(Raw.class, 10);
        assert(mb.isValid());
        h.freeMemoryBlock(mb);
        assert(!mb.isValid());
        boolean caught = false;
        try {
            mb.checkValid();
        } catch (Exception e) {
            caught = true;
        }
        assert(caught);

        mb = h.allocateMemoryBlock(Transactional.class, 10);
        assert(mb.isValid());
        h.freeMemoryBlock(mb);
        assert(!mb.isValid());
        caught = false;
        try {
            mb.checkValid();
        } catch (IllegalStateException e) {
            caught = true;
        }
        assert(caught);

        mb = h.allocateMemoryBlock(Flushable.class, 10);
        assert(mb.isValid());
        h.freeMemoryBlock(mb);
        assert(!mb.isValid());
        caught = false;
        try {
            mb.checkValid();
        } catch (IllegalStateException e) {
            caught = true;
        }
        assert(caught);
        System.out.println("=================================All MemoryBlockFree tests passed===========================");
    }
}
