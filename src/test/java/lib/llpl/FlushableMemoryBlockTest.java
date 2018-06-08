/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class FlushableMemoryBlockTest {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
        FlushableMemoryBlock block = (FlushableMemoryBlock)h.allocateMemoryBlock(Flushable.class, 16);

        block.setByte(0, (byte)5);
        assert(block.isFlushed() == false);
        FlushableMemoryBlock block2 = (FlushableMemoryBlock)h.memoryBlockFromAddress(Flushable.class, block.address());
        assert(block2.isFlushed() == false);
        block.flush();
        assert(block.isFlushed() == true);
        assert(block2.isFlushed() == true);
        assert(block.getByte(0) == (byte)5);

        block.setShort(1, (short)5);
        assert(block.isFlushed() == false);
        assert(block2.isFlushed() == false);
        block.flush();
        assert(block.isFlushed() == true);
        assert(block2.isFlushed() == true);
        assert(block.getShort(1) == (short)5);
        assert(block.getByte(0) == (byte)5);
        assert(block.getShort(0) == (short)1285);

        block.setInt(2, 327686);
        assert(block.isFlushed() == false);
        block.flush();
        assert(block.isFlushed() == true);
        assert(block.getInt(2) == 327686);
        assert(block.getShort(1) == (short)1541);
        assert(block.getInt(1) == 83887621);
        assert(block.getInt(0) == 394501);

        block.setLong(4, 123456789101112L);
        assert(block.isFlushed() == false);
        block.flush();
        assert(block.isFlushed() == true);
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

        h.freeMemoryBlock(block);
        System.out.println("=================================All FlushableMemoryBlock tests passed======================");
    }
}
