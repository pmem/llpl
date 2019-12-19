/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.io.File;

public class CopyMemoryTest {

    static String heapName = "/mnt/mem/persistent_pool";
    static String dHeapName = "/mnt/mem/persistent_pool_durable";
    static String tHeapName = "/mnt/mem/persistent_pool_transactional";
    static Heap heap = Heap.exists(heapName) ? Heap.openHeap(heapName) : Heap.createHeap(heapName, 2147483648L);
    static PersistentHeap dHeap = PersistentHeap.exists(dHeapName) ? PersistentHeap.openHeap(dHeapName) : PersistentHeap.createHeap(dHeapName, 2147483648L);
    static TransactionalHeap tHeap = TransactionalHeap.exists(tHeapName) ? TransactionalHeap.openHeap(tHeapName) : TransactionalHeap.createHeap(tHeapName, 100000000L);

    public static void main(String[] args) {
        blockToBlockTest();
        arrayToBlockTest();
        blockToArrayTest();
        new File(heapName).delete();
        new File(dHeapName).delete();
        new File(tHeapName).delete();
        System.out.println("================================= All CopyMemory tests passed ==================================");
    }

    static void blockToBlockTest() {
        MemoryBlock bmb = heap.allocateMemoryBlock(150, true);
        CompactMemoryBlock umb = heap.allocateCompactMemoryBlock(150, true);
        PersistentMemoryBlock pmb = dHeap.allocateMemoryBlock(150, true);
        TransactionalMemoryBlock tmb = tHeap.allocateMemoryBlock(150);

        bmb.durableSetLong(0L, 0x44);
        umb.durableSetLong(100L, 0x22);
        pmb.setLong(40L, 0x88);
        Transaction.create(tHeap, () -> {tmb.setLong(80L, 0xcc);});

        // From Raw
        bmb.copyFromMemoryBlock(bmb, 0, 10, 10);
        pmb.copyFromMemoryBlock(bmb, 0, 50, 10);
        tmb.copyFromMemoryBlock(bmb, 0, 90, 10); 
        umb.copyFromMemoryBlock(bmb, 0, 110, 10); 
        assert(bmb.getLong(10L) == 0x44);
        assert(tmb.getLong(90L) == 0x44);
        assert(umb.getLong(110L) == 0x44);

        // From Persistent
        bmb.copyFromMemoryBlock(pmb, 40, 20, 10);
        tmb.copyFromMemoryBlock(pmb, 40, 100, 10);
        umb.copyFromMemoryBlock(pmb, 40, 120, 10); 
        assert(bmb.getLong(20L) == 0x88);
        assert(tmb.getLong(100L) == 0x88);
        assert(umb.getLong(120L) == 0x88);

        // From Transactional
        bmb.copyFromMemoryBlock(tmb, 80, 30, 10);
        tmb.copyFromMemoryBlock(tmb, 80, 110, 10);
        umb.copyFromMemoryBlock(tmb, 80, 130, 10);
        assert(bmb.getLong(30L) == 0xcc);
        assert(tmb.getLong(110L) == 0xcc);
        assert(umb.getLong(130L) == 0xcc);

        bmb.free(true);
        umb.free(true);
        pmb.free(true);
        tmb.free();
   }

    static void arrayToBlockTest() {
        MemoryBlock bmb = heap.allocateMemoryBlock(150, true);
        CompactMemoryBlock umb = heap.allocateCompactMemoryBlock(150, true);
        PersistentMemoryBlock pmb = dHeap.allocateMemoryBlock(150, true);
        TransactionalMemoryBlock tmb = tHeap.allocateMemoryBlock(150);
        byte[] srcArray = new byte[50];

        for (int i = 0; i < srcArray.length; i++) {
            srcArray[i] = (byte)i;
        }

        bmb.copyFromArray(srcArray, 0, 0, srcArray.length);
        umb.copyFromArray(srcArray, 0, 0, srcArray.length);
        pmb.copyFromArray(srcArray, 0, 0, srcArray.length);
        tmb.copyFromArray(srcArray, 0, 0, srcArray.length);

        for (int i = 0; i < srcArray.length; i++) {
            assert(bmb.getByte(i) == (byte)i);
            assert(umb.getByte(i) == (byte)i);
            assert(pmb.getByte(i) == (byte)i);
            assert(tmb.getByte(i) == (byte)i);
        }

        int srcOffset = 25;
        bmb.copyFromArray(srcArray, srcOffset, 50, srcArray.length - srcOffset);
        umb.copyFromArray(srcArray, srcOffset, 50, srcArray.length - srcOffset);
        pmb.copyFromArray(srcArray, srcOffset, 50, srcArray.length - srcOffset);
        tmb.copyFromArray(srcArray, srcOffset, 50, srcArray.length - srcOffset);

        for (int i = srcOffset; i < srcArray.length; i++) {
            assert(bmb.getByte(50 + i - srcOffset) == (byte)i);
            assert(umb.getByte(50 + i - srcOffset) == (byte)i);
            assert(pmb.getByte(50 + i - srcOffset) == (byte)i);
            assert(tmb.getByte(50 + i - srcOffset) == (byte)i);
        }

        bmb.free(true);
        umb.free(true);
        pmb.free(true);
        tmb.free();
    }

    static void blockToArrayTest() {
        MemoryBlock bmb = heap.allocateMemoryBlock(150, true);
        CompactMemoryBlock umb = heap.allocateCompactMemoryBlock(150, true);
        PersistentMemoryBlock pmb = dHeap.allocateMemoryBlock(150, true);
        TransactionalMemoryBlock tmb = tHeap.allocateMemoryBlock(150);
        byte[] dstArray = new byte[50];

        bmb.durableSetLong(10L, 0x44);
        umb.durableSetLong(130L, 0x22);
        pmb.setLong(50L, 0x88);
        Transaction.create(tHeap, () -> {tmb.setLong(90L, 0xcc);});

        bmb.copyToArray(10L, dstArray, 0, 10);
        umb.copyToArray(130L, dstArray, 30, 10);
        pmb.copyToArray(50L, dstArray, 10, 10);
        tmb.copyToArray(90L, dstArray, 20, 10);

        assert(dstArray[0] == (byte)0x44);
        assert(dstArray[10] == (byte)0x88);
        assert(dstArray[20] == (byte)0xcc);
        assert(dstArray[30] == (byte)0x22);

        bmb.free(true);
        umb.free(true);
        pmb.free(true);
        tmb.free();
    }
}
