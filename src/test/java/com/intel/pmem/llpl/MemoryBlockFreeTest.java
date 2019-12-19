/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.io.File;

class MemoryBlockFreeTest {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/persistent_pool_base";
        Heap heap = Heap.exists(heapName) ? Heap.openHeap(heapName) : Heap.createHeap(heapName, 2147483648L);
        MemoryBlock mb = heap.allocateMemoryBlock(10, true);
        assert(mb.isValid());
        heap.freeMemoryBlock(mb);
        assert(!mb.isValid());
        boolean caught = false;
        try {
            mb.checkValid();
        } 
        catch (Exception e) {
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
        } 
        catch (Exception e) {
            caught = true;
        }
        assert(caught);

        String pHeapName = "/mnt/mem/persistent_pool_durable";
        PersistentHeap pHeap = PersistentHeap.exists(pHeapName) ? PersistentHeap.openHeap(pHeapName) : PersistentHeap.createHeap(pHeapName, 100_000_000L);
        PersistentMemoryBlock dmb = pHeap.allocateMemoryBlock(10, true);
        assert(dmb.isValid());
        dmb.free(true);
        assert(!dmb.isValid());
        caught = false;
        try {
            dmb.checkValid();
        } 
        catch (IllegalStateException e) {
            caught = true;
        }
        assert(caught);

        String tHeapName = "/mnt/mem/persistent_pool_transactional";
        TransactionalHeap tHeap = TransactionalHeap.exists(tHeapName) ? TransactionalHeap.openHeap(tHeapName) : TransactionalHeap.createHeap(tHeapName, 100_000_000L);
        TransactionalMemoryBlock tmb = tHeap.allocateMemoryBlock(10);
        assert(tmb.isValid());
        tmb.free();
        assert(!tmb.isValid());
        caught = false;
        try {
            tmb.checkValid();
        } 
        catch (IllegalStateException e) {
            caught = true;
        }
        assert(caught);
        new File("/mnt/mem/persistent_pool_base").delete();
        new File("/mnt/mem/persistent_pool_durable").delete();
        new File("/mnt/mem/persistent_pool_transactional").delete();
        System.out.println("================================= All MemoryBlockFree tests passed =============================");
    }
}
