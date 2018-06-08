/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class TransactionTest {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
        MemoryBlock<Transactional> block1 = h.allocateMemoryBlock(Transactional.class, 16);

        // thread-local syntax
        Transaction.run(() -> {
            block1.setLong(4, 1000);
            block1.setInt(0, 777);
            assert(block1.getLong(4)) == 1000;
            assert(block1.getInt(0)) == 777;
            Transaction.run(() -> {
                block1.setLong(8, 2000);
            });
        });
        assert(block1.getInt(0) == 777);

        // parameter passing syntax
        Transaction t1 = new Transaction();
        t1.execute(() -> {
            block1.setLong(4, 1000);
            block1.setInt(0, 777);
            assert(block1.getLong(4)) == 1000;
            assert(block1.getInt(0)) == 777;
            t1.execute(() -> {
                block1.setLong(8, 2000);
            });
            assert(block1.getLong(8) == 2000);
        });
            
        System.out.println("=================================All Transaction tests passed====================");
    }
}
