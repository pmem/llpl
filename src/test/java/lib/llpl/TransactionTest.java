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
        Transaction.run(h, () -> {
            // Maybe addToTransaction() is required?
            block1.setLong(4, 1000);
            block1.setInt(0, 777);
            assert(block1.getLong(4)) == 1000;
            assert(block1.getInt(0)) == 777;
            Transaction.run(h, () -> {
                block1.setLong(8, 2000);
            });
        });
        assert(block1.getInt(0) == 777);

        // parameter passing syntax
        Transaction t1 = new Transaction(h);
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


        MemoryBlock<Raw> block2 = h.allocateMemoryBlock(Raw.class, 100);
        Transaction t2 = new Transaction(h);
        t2.execute(() -> {
            block2.setTransactionalLong(4, 1000);
            block2.setTransactionalInt(0, 777);
            assert(block2.getLong(4)) == 1000;
            assert(block2.getInt(0)) == 777;
            foo(t2, block2, 888);
            assert(block2.getLong(10) == 888);
            // this produces a flattened inner transaction
            new Transaction(h).execute(() -> {
                block1.setTransactionalLong(8, 2000);
            });
            assert(block1.getLong(8) == 2000);
        });
        // this will be a new top-level transaction
        foo(new Transaction(h), block2, 999);
        assert(block2.getLong(10) == 999);
            
        System.out.println("=================================All Transaction tests passed================================");
    }

    private static void foo(Transaction tx, MemoryBlock<Raw> block, long x) {
        tx.execute(() -> {
            block.setTransactionalLong(10, x);
        });
    }

}
