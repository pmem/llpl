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
        MemoryBlock<Raw> rmb = h.allocateMemoryBlock(Raw.class, 150);
        MemoryBlock<Flushable> fmb = h.allocateMemoryBlock(Flushable.class, 150);
        MemoryBlock<Transactional> tmb = h.allocateMemoryBlock(Transactional.class, 150);
        MemoryBlock<Unbounded> umb = h.allocateMemoryBlock(Unbounded.class, 150);

        rmb.setLong(0L, 0x44); rmb.flush();
        fmb.setLong(40L, 0x88); fmb.flush();
        tmb.setLong(80L, 0xcc);
        umb.setLong(100L, 0x22); umb.flush(100, 8);

        fmb.flush();

        // From Raw
        assert(fmb.isFlushed() == true);
        rmb.copyFromMemory(rmb, 0, 10, 10);
        fmb.copyFromMemory(rmb, 0, 50, 10);
        tmb.copyFromMemory(rmb, 0, 90, 10); 
        umb.copyFromMemory(rmb, 0, 110, 10); 
        assert(rmb.getLong(10L) == 0x44);
        assert(fmb.getLong(50L) == 0x44);
        assert(tmb.getLong(90L) == 0x44);
        assert(umb.getLong(110L) == 0x44);
        assert(fmb.isFlushed() == false);
        fmb.flush();

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
        rmb.copyFromMemory(fmb, 40, 20, 10);
        fmb.copyFromMemory(fmb, 40, 60, 10);
        tmb.copyFromMemory(fmb, 40, 100, 10);
        umb.copyFromMemory(fmb, 40, 120, 10); 
        assert(rmb.getLong(20L) == 0x88);
        assert(fmb.getLong(60L) == 0x88);
        assert(tmb.getLong(100L) == 0x88);
        assert(umb.getLong(120L) == 0x88);
        assert(fmb.isFlushed() == false);
        fmb.flush();

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
        rmb.copyFromMemory(tmb, 80, 30, 10);
        fmb.copyFromMemory(tmb, 80, 70, 10);
        tmb.copyFromMemory(tmb, 80, 110, 10);
        umb.copyFromMemory(tmb, 80, 130, 10);
        assert(rmb.getLong(30L) == 0xcc);
        assert(fmb.getLong(70L) == 0xcc);
        assert(tmb.getLong(110L) == 0xcc);
        assert(umb.getLong(130L) == 0xcc);
        assert(fmb.isFlushed() == false);
        fmb.flush();

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
        MemoryBlock<Raw> rmb = h.allocateMemoryBlock(Raw.class, 150);
        MemoryBlock<Flushable> fmb = h.allocateMemoryBlock(Flushable.class, 150);
        MemoryBlock<Transactional> tmb = h.allocateMemoryBlock(Transactional.class, 150);
        MemoryBlock<Unbounded> umb = h.allocateMemoryBlock(Unbounded.class, 150);
        byte[] srcArray = new byte[50];

        for (int i = 0; i < srcArray.length; i++) {
            srcArray[i] = (byte)i;
        }

        assert(fmb.isFlushed() == true);
        rmb.copyFromArray(srcArray, 0, 0, srcArray.length);
        fmb.copyFromArray(srcArray, 0, 0, srcArray.length);
        tmb.copyFromArray(srcArray, 0, 0, srcArray.length);
        umb.copyFromArray(srcArray, 0, 0, srcArray.length);
        fmb.flush();

        assert(fmb.isFlushed() == true);
        h.copyFromArray(srcArray, 0, rmb, 0, srcArray.length);
        h.copyFromArray(srcArray, 0, fmb, 0, srcArray.length);
        h.copyFromArray(srcArray, 0, tmb, 0, srcArray.length);

        assert(fmb.isFlushed() == false);
        for (int i = 0; i < srcArray.length; i++) {
            assert(rmb.getByte(i) == (byte)i);
            assert(fmb.getByte(i) == (byte)i);
            assert(tmb.getByte(i) == (byte)i);
            assert(umb.getByte(i) == (byte)i);
        }
        fmb.flush();

        int srcOffset = 25;
        assert(fmb.isFlushed() == true);
        rmb.copyFromArray(srcArray, srcOffset, 50, srcArray.length - srcOffset);
        fmb.copyFromArray(srcArray, srcOffset, 50, srcArray.length - srcOffset);
        tmb.copyFromArray(srcArray, srcOffset, 50, srcArray.length - srcOffset);
        umb.copyFromArray(srcArray, srcOffset, 50, srcArray.length - srcOffset);
        fmb.flush();

        assert(fmb.isFlushed() == true);
        h.copyFromArray(srcArray, srcOffset, rmb, 50, srcArray.length - srcOffset);
        h.copyFromArray(srcArray, srcOffset, fmb, 50, srcArray.length - srcOffset);
        h.copyFromArray(srcArray, srcOffset, tmb, 50, srcArray.length - srcOffset);

        assert(fmb.isFlushed() == false);
        for (int i = srcOffset; i < srcArray.length; i++) {
            assert(rmb.getByte(50 + i - srcOffset) == (byte)i);
            assert(fmb.getByte(50 + i - srcOffset) == (byte)i);
            assert(tmb.getByte(50 + i - srcOffset) == (byte)i);
            assert(umb.getByte(50 + i - srcOffset) == (byte)i);
        }
        fmb.flush();

        h.freeMemoryBlock(rmb);
        h.freeMemoryBlock(fmb);
        h.freeMemoryBlock(tmb);
        h.freeMemoryBlock(umb);
    }

    static void blockToArrayTest() {
        MemoryBlock<Raw> rmb = h.allocateMemoryBlock(Raw.class, 150);
        MemoryBlock<Flushable> fmb = h.allocateMemoryBlock(Flushable.class, 150);
        MemoryBlock<Transactional> tmb = h.allocateMemoryBlock(Transactional.class, 150);
        MemoryBlock<Unbounded> umb = h.allocateMemoryBlock(Unbounded.class, 150);
        byte[] dstArray = new byte[50];

        rmb.setLong(10L, 0x44); rmb.flush();
        fmb.setLong(50L, 0x88); fmb.flush();
        tmb.setLong(90L, 0xcc);
        umb.setLong(130L, 0x22); umb.flush(130, 8);

        rmb.copyToArray(10L, dstArray, 0, 10);
        fmb.copyToArray(50L, dstArray, 10, 10);
        tmb.copyToArray(90L, dstArray, 20, 10);
        umb.copyToArray(130L, dstArray, 30, 10);

        h.copyToArray(rmb, 10L, dstArray, 0, 10);
        h.copyToArray(fmb, 50L, dstArray, 10, 10);
        h.copyToArray(tmb, 90L, dstArray, 20, 10);

        assert(dstArray[0] == (byte)0x44);
        assert(dstArray[10] == (byte)0x88);
        assert(dstArray[20] == (byte)0xcc);
        assert(dstArray[30] == (byte)0x22);

        h.freeMemoryBlock(rmb);
        h.freeMemoryBlock(fmb);
        h.freeMemoryBlock(tmb);
        h.freeMemoryBlock(umb);
    }
}