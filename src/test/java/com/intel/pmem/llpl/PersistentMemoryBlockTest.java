/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.io.File;

class PersistentMemoryBlockTest {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/persistent_pool";
        PersistentHeap heap = PersistentHeap.exists(heapName) ? PersistentHeap.openHeap(heapName) : PersistentHeap.createHeap(heapName, 2147483648L);
        PersistentMemoryBlock block1 = heap.allocateMemoryBlock(16, true);
        block1.setLong(3, 88888);

        PersistentMemoryBlock block = heap.memoryBlockFromHandle(block1.handle());
        work(block);

        PersistentMemoryBlock block2 = heap.allocateMemoryBlock(64, true);
        block2.setLong(3, 88888);
        block2.withRange(0, 16, (Range range) -> { 
            work(block2, range);
        });

        final Range[] savedRanges = new Range[1];

        PersistentMemoryBlock block3 = heap.allocateMemoryBlock(64, false, (Range range) -> {
            range.setInt(0, 1234);
            range.setLong(4, 23456);
            range.setMemory((byte)-1, 12, 52);
            savedRanges[0] = range;
        });


        boolean caught = false;
        try{
            savedRanges[0].setInt(0, 100);
        }
        catch(IllegalStateException ise) {
            caught = true;
        }
        assert(caught);

        new File("/mnt/mem/persistent_pool").delete();
        System.out.println("================================= All PersistentMemoryBlock tests passed =======================");
    }

    static void work(PersistentMemoryBlock block) {
        assert(block.getLong(3) == 88888);
        block.setByte(0, (byte)5);
        assert(block.getByte(0) == (byte)5);

        block.setShort(1, (short)5);
        assert(block.getShort(1) == (short)5);
        assert(block.getByte(0) == (byte)5);
        assert(block.getShort(0) == (short)1285);

        block.setInt(2, 327686);
        assert(block.getInt(2) == 327686);
        assert(block.getShort(1) == (short)1541);
        assert(block.getInt(1) == 83887621);
        assert(block.getInt(0) == 394501);

        block.setLong(4, 123456789101112L);
        assert(block.getLong(4) == 123456789101112L);
        assert(block.getLong(3) == 31604938009884672L);
        assert(block.getInt(3) == 255473664);
        assert(block.getShort(3) == (short)14336);
        assert(block.getByte(3) == (byte)0);

        assert(block.getLong(4) == 123456789101112L);
        assert(block.getLong(3) == 31604938009884672L);
        assert(block.getInt(3) == 255473664);
        assert(block.getShort(3) == (short)14336);
        assert(block.getByte(3) == (byte)0);
    }        

    static void work(PersistentMemoryBlock block, Range range) {
        assert(block.getLong(3) == 88888);
        range.setByte(0, (byte)5);
        assert(block.getByte(0) == (byte)5);

        range.setShort(1, (short)5);
        assert(block.getShort(1) == (short)5);
        assert(block.getByte(0) == (byte)5);
        assert(block.getShort(0) == (short)1285);

        range.setInt(2, 327686);
        assert(block.getInt(2) == 327686);
        assert(block.getShort(1) == (short)1541);
        assert(block.getInt(1) == 83887621);
        assert(block.getInt(0) == 394501);

        range.setLong(4, 123456789101112L);
        assert(block.getLong(4) == 123456789101112L);
        assert(block.getLong(3) == 31604938009884672L);
        assert(block.getInt(3) == 255473664);
        assert(block.getShort(3) == (short)14336);
        assert(block.getByte(3) == (byte)0);

        assert(block.getLong(4) == 123456789101112L);
        assert(block.getLong(3) == 31604938009884672L);
        assert(block.getInt(3) == 255473664);
        assert(block.getShort(3) == (short)14336);
        assert(block.getByte(3) == (byte)0);
    }        
}
