/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class UnboundedMemoryBlockTest {
    public static void main(String[] args) {
        Heap heap = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
        int nBlocks = 10;
        long[] addresses = new long[nBlocks];
        for (int i = 0; i < nBlocks; i++) addresses[i] = heap.allocateMemoryBlock(Unbounded.class, 1024).address();
        MemoryBlock<Unbounded> block = null; 

        for (int i = 0; i < nBlocks; i++) {
            block = heap.memoryBlockFromAddress(Unbounded.class, addresses[i]);
            block.setByte(0, (byte)5); block.flush(0, 1);
            assert(block.getByte(0) == (byte)5);

            block.setShort(1, (short)5); block.flush(1, 2);
            assert(block.getShort(1) == (short)5);
            assert(block.getByte(0) == (byte)5);
            assert(block.getShort(0) == (short)1285);

            block.setInt(2, 327686); block.flush(2, 4);
            assert(block.getInt(2) == 327686);
            assert(block.getShort(1) == (short)1541);
            assert(block.getInt(1) == 83887621);
            assert(block.getInt(0) == 394501);

            block.setLong(4, 123456789101112L); block.flush(4, 8);
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

        // size() is not supported
        boolean caught = false;
        try {
            block.size();
        }
        catch (UnsupportedOperationException uoe) {
            caught = true;
        }
        assert(caught);

        // checkBounds() is not supported
        caught = false;
        try {
            block.checkBounds(1);
        }
        catch (UnsupportedOperationException uoe) {
            caught = true;
        }
        assert(caught);


        System.out.println("=================================All UnboundedMemoryBlock tests passed=======================");
    }
}
