/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.io.File;

class TransactionalMemoryBlockTest {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/persistent_pool_transactional";
        TransactionalHeap heap = TransactionalHeap.exists(heapName) ? TransactionalHeap.openHeap(heapName) : TransactionalHeap.createHeap(heapName, 2147483648L);
        TransactionalMemoryBlock block1 = heap.allocateMemoryBlock(16);
        TransactionalMemoryBlock block = heap.memoryBlockFromHandle(block1.handle());

        Transaction.create(heap, () -> {block.setByte(0, (byte)5);});
        assert(block.getByte(0) == (byte)5);

        Transaction.create(heap, () -> {block.setShort(1, (short)5);});
        assert(block.getShort(1) == (short)5);
        assert(block.getByte(0) == (byte)5);
        assert(block.getShort(0) == (short)1285);

        Transaction.create(heap, () -> {block.setInt(2, 327686);});
        assert(block.getInt(2) == 327686);
        assert(block.getShort(1) == (short)1541);
        assert(block.getInt(1) == 83887621);
        assert(block.getInt(0) == 394501);

        Transaction.create(heap, () -> {block.setLong(4, 123456789101112L);});
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

        TransactionalMemoryBlock block3 = heap.allocateMemoryBlock(64, (Range range) -> {
            range.setInt(0, 1234);
            range.setLong(4, 23456);
            range.setMemory((byte)-1, 12, 52);
        });
        
        new File(heapName).delete();
        System.out.println("================================= All TransactionalMemoryBlock tests passed ====================");
    }
}
