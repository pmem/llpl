/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.io.File;

class CompactMemoryBlockTest {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/persistent_pool";
        Heap heap = Heap.exists(heapName) ? Heap.openHeap(heapName) : Heap.createHeap(heapName, 2147483648L);
        int nBlocks = 10;
        long[] offsets = new long[nBlocks];
        for (int i = 0; i < nBlocks; i++) offsets[i] = heap.allocateCompactMemoryBlock(1024, false).handle();
        CompactMemoryBlock block = null; 

        for (int i = 0; i < nBlocks; i++) {
            block = heap.compactMemoryBlockFromHandle(offsets[i]);
            block.durableSetByte(0, (byte)5);
            assert(block.getByte(0) == (byte)5);

            block.durableSetShort(1, (short)5);
            assert(block.getShort(1) == (short)5);
            assert(block.getByte(0) == (byte)5);
            assert(block.getShort(0) == (short)1285);

            block.durableSetInt(2, 327686);
            assert(block.getInt(2) == 327686);
            assert(block.getShort(1) == (short)1541);
            assert(block.getInt(1) == 83887621);
            assert(block.getInt(0) == 394501);

            block.durableSetLong(4, 123456789101112L);
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

        // size() is not supported, should not compile (outside of package)
        block.size();
        new File("/mnt/mem/persistent_pool").delete();

        System.out.println("================================= All CompactMemoryBlock tests passed ========================");
    }
}
