/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class TransactionTest {
    public static void main(String[] args) {
        Heap heap = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
        MemoryBlock<Transactional> block1 = heap.allocateMemoryBlock(Transactional.class, 16);

        // thread-local syntax
        Transaction.run(heap, () -> {
            block1.setLong(4, 1000);
            block1.setInt(0, 777);
            assert(block1.getLong(4)) == 1000;
            assert(block1.getInt(0)) == 777;
            Transaction.run(heap, () -> {
                block1.setLong(8, 2000);
            });
        });
        assert(block1.getInt(0) == 777);

        // parameter passing syntax
        Transaction t1 = new Transaction(heap);
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


        MemoryBlock<Raw> block2 = heap.allocateMemoryBlock(Raw.class, 100);
        Transaction t2 = new Transaction(heap);
        t2.execute(() -> {
            block2.setTransactionalLong(4, 1000);
            block2.setTransactionalInt(0, 777);
            assert(block2.getLong(4)) == 1000;
            assert(block2.getInt(0)) == 777;
            foo(t2, block2, 888);
            assert(block2.getLong(10) == 888);
            // this produces a flattened inner transaction
            new Transaction(heap).execute(() -> {
                block1.setTransactionalLong(8, 2000);
            });
            assert(block1.getLong(8) == 2000);
        });
        // this will be a new top-level transaction
        foo(new Transaction(heap), block2, 999);
        assert(block2.getLong(10) == 999);
            
        // test abort / commit behavior
        Transaction t3 = new Transaction(heap);
        assert(t3.state() == Transaction.State.New);
        t3.execute(() -> {});
        assert(t3.state() == Transaction.State.Committed);

        Transaction t4 = new Transaction(heap);
        t4.execute(() -> {});
        assert(t4.state() == Transaction.State.Committed);

        Transaction t5 = new Transaction(heap);
        boolean caught = false;
        try {
            t5.execute(() -> {throw new RuntimeException();});
        }
        catch (RuntimeException re) {
            caught = true;
        }
        assert(t5.state() == Transaction.State.Aborted);
        assert(caught);

        String planets = "Mercury Venus Earth Mars Saturn Jupiter Neptune Uranus Pluto";
        long rmbAddress = Transaction.run(heap, () -> {
            MemoryBlock<Raw> rmb = heap.allocateMemoryBlock(Raw.class, 1024);
            rmb.addToTransaction(0, 1024);
            rmb.copyFromArray(planets.getBytes(), 0, 0, planets.length());
            return rmb.address();
        });
        MemoryBlock<Raw> read = heap.memoryBlockFromAddress(Raw.class, rmbAddress);
        byte[] planetBytes = new byte[1024];
        read.copyToArray(0, planetBytes, 0, planetBytes.length - 1);
        String planets1 = new String(planetBytes).trim();
        assert(planets1.equals(planets));
        System.out.println("=================================All Transaction tests passed================================");
    }

    private static void foo(Transaction tx, MemoryBlock<Raw> block, long x) {
        tx.execute(() -> {
            block.setTransactionalLong(10, x);
        });
    }

}
