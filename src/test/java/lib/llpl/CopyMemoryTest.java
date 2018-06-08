/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

public class CopyMemoryTest {
    static Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);

    public static void main(String[] args) {
        blockToBlockTest();
        arrayToBlockTest();
        blockToArrayTest();
        System.out.println("=================================All CopyMemory tests passed=================================");
    }

    static void blockToBlockTest() {
        MemoryBlock<Raw> rmb = h.allocateMemoryBlock(Raw.class, 120);
        MemoryBlock<Flushable> fmb = h.allocateMemoryBlock(Flushable.class, 120);
        MemoryBlock<Transactional> tmb = h.allocateMemoryBlock(Transactional.class, 120);

        rmb.setLong(0L, 0x44);
        fmb.setLong(40L, 0x88);
        tmb.setLong(80L, 0xcc);

        fmb.flush();

        // From Raw
        assert(fmb.isFlushed() == true);
        h.copyMemory(rmb, 0, rmb, 10, 10);
        h.copyMemory(rmb, 0, fmb, 50, 10);
        h.copyMemory(rmb, 0, tmb, 90, 10);
        assert(rmb.getLong(10L) == 0x44);
        assert(fmb.getLong(50L) == 0x44);
        assert(tmb.getLong(90L) == 0x44);
        assert(fmb.isFlushed() == false);
        fmb.flush();

        // From Flushable
        assert(fmb.isFlushed() == true);
        h.copyMemory(fmb, 40, rmb, 20, 10);
        h.copyMemory(fmb, 40, fmb, 60, 10);
        h.copyMemory(fmb, 40, tmb, 100, 10);
        assert(rmb.getLong(20L) == 0x88);
        assert(fmb.getLong(60L) == 0x88);
        assert(tmb.getLong(100L) == 0x88);
        assert(fmb.isFlushed() == false);
        fmb.flush();

        // From Transactional
        assert(fmb.isFlushed() == true);
        h.copyMemory(tmb, 80, rmb, 30, 10);
        h.copyMemory(tmb, 80, fmb, 70, 10);
        h.copyMemory(tmb, 80, tmb, 110, 10);
        assert(rmb.getLong(30L) == 0xcc);
        assert(fmb.getLong(70L) == 0xcc);
        assert(tmb.getLong(110L) == 0xcc);
        assert(fmb.isFlushed() == false);
        fmb.flush();

        h.freeMemoryBlock(rmb);
        h.freeMemoryBlock(fmb);
        h.freeMemoryBlock(tmb);
    }

    static void arrayToBlockTest() {
        MemoryBlock<Raw> rmb = h.allocateMemoryBlock(Raw.class, 120);
        MemoryBlock<Flushable> fmb = h.allocateMemoryBlock(Flushable.class, 120);
        MemoryBlock<Transactional> tmb = h.allocateMemoryBlock(Transactional.class, 120);
        byte[] srcArray = new byte[50];

        for (int i = 0; i < srcArray.length; i++) {
            srcArray[i] = (byte)i;
        }

        assert(fmb.isFlushed() == true);
        h.copyFromArray(srcArray, 0, rmb, 0, srcArray.length);
        h.copyFromArray(srcArray, 0, fmb, 0, srcArray.length);
        h.copyFromArray(srcArray, 0, tmb, 0, srcArray.length);

        assert(fmb.isFlushed() == false);
        for (int i = 0; i < srcArray.length; i++) {
            assert(rmb.getByte(i) == (byte)i);
            assert(fmb.getByte(i) == (byte)i);
            assert(tmb.getByte(i) == (byte)i);
        }
        fmb.flush();

        int srcOffset = 25;
        assert(fmb.isFlushed() == true);
        h.copyFromArray(srcArray, srcOffset, rmb, 50, srcArray.length - srcOffset);
        h.copyFromArray(srcArray, srcOffset, fmb, 50, srcArray.length - srcOffset);
        h.copyFromArray(srcArray, srcOffset, tmb, 50, srcArray.length - srcOffset);

        assert(fmb.isFlushed() == false);
        for (int i = srcOffset; i < srcArray.length; i++) {
            assert(rmb.getByte(50 + i - srcOffset) == (byte)i);
            assert(fmb.getByte(50 + i - srcOffset) == (byte)i);
            assert(tmb.getByte(50 + i - srcOffset) == (byte)i);
        }
        fmb.flush();

        h.freeMemoryBlock(rmb);
        h.freeMemoryBlock(fmb);
        h.freeMemoryBlock(tmb);
    }

    static void blockToArrayTest() {
        MemoryBlock<Raw> rmb = h.allocateMemoryBlock(Raw.class, 120);
        MemoryBlock<Flushable> fmb = h.allocateMemoryBlock(Flushable.class, 120);
        MemoryBlock<Transactional> tmb = h.allocateMemoryBlock(Transactional.class, 120);
        byte[] dstArray = new byte[50];

        rmb.setLong(10L, 0x44);
        fmb.setLong(50L, 0x88);
        tmb.setLong(90L, 0xcc);

        h.copyToArray(rmb, 10L, dstArray, 0, 10);
        h.copyToArray(fmb, 50L, dstArray, 10, 10);
        h.copyToArray(tmb, 90L, dstArray, 20, 10);

        assert(dstArray[0] == (byte)0x44);
        assert(dstArray[10] == (byte)0x88);
        assert(dstArray[20] == (byte)0xcc);

        h.freeMemoryBlock(rmb);
        h.freeMemoryBlock(fmb);
        h.freeMemoryBlock(tmb);
    }
}