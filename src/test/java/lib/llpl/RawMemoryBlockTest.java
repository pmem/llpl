/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class RawMemoryBlockTest {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
        MemoryBlock<Raw> block1 = h.allocateMemoryBlock(Raw.class, 16);
        block1.setLong(3, 88888); block1.flush();
        MemoryBlock<Raw> block = h.memoryBlockFromAddress(Raw.class, block1.address());
        assert(block.getLong(3) == 88888);
        block.setByte(0, (byte)5); block.flush();
        assert(block.getByte(0) == (byte)5);

        block.setShort(1, (short)5); block.flush();
        assert(block.getShort(1) == (short)5);
        assert(block.getByte(0) == (byte)5);
        assert(block.getShort(0) == (short)1285);

        block.setInt(2, 327686); block.flush();
        assert(block.getInt(2) == 327686);
        assert(block.getShort(1) == (short)1541);
        assert(block.getInt(1) == 83887621);
        assert(block.getInt(0) == 394501);

        block.setLong(4, 123456789101112L); block.flush();
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
        System.out.println("=================================All RawMemoryBlock tests passed============================");
    }
}
