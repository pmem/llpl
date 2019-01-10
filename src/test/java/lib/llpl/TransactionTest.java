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
        MemoryBlock block1 = heap.allocateMemoryBlock(16, true);

        Transaction.run(heap, () -> {
            Transaction.checkTransactionActive(true);
            MemoryBlock b = heap.allocateMemoryBlock(128, true);
            Transaction.checkTransactionActive(true);
        });
        // compatibility test
        Transaction.run(heap, () -> {
            Transaction t1 = new Transaction(heap);
            t1.run(() -> {
            Transaction.run(heap, () -> {
                Transaction.run(heap, () -> {
                     t1.run(() -> {
                         new Transaction(heap).run(() -> {
                             block1.transactionalWithRange(0, 10, (Range r) -> {
                                 r.setLong(2, 123456);
                             });
                         });
                     });
                 });
                });
            });
        });

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
        t1.run(() -> {
            block1.setLong(4, 1000);
            block1.setInt(0, 777);
            assert(block1.getLong(4)) == 1000;
            assert(block1.getInt(0)) == 777;
            t1.run(() -> {
                block1.setLong(8, 2000);
            });
            assert(block1.getLong(8) == 2000);
        });

        MemoryBlock block2 = heap.allocateMemoryBlock(100, true);
        Transaction t2 = new Transaction(heap);
        t2.run(() -> {
            block2.transactionalSetLong(4, 1000);
            block2.transactionalSetInt(0, 777);
            assert(block2.getLong(4)) == 1000;
            assert(block2.getInt(0)) == 777;
            foo(t2, block2, 888);
            assert(block2.getLong(10) == 888);
            // this produces a flattened inner transaction
            // new Transaction(heap).run(() -> {       // nested constructed transactions not supported
            t2.run(() -> {       // nested constructed transactions not supported
                block1.transactionalSetLong(8, 2000);
            });
            assert(block1.getLong(8) == 2000);
        });
        // this will be a new top-level transaction
        foo(new Transaction(heap), block2, 999);
        assert(block2.getLong(10) == 999);
            
        // test abort / commit behavior
        Transaction t3 = new Transaction(heap);
        assert(t3.state() == Transaction.State.New);
        t3.run(() -> {});
        assert(t3.state() == Transaction.State.Committed);

        Transaction t4 = new Transaction(heap);
        t4.run(() -> {});
        assert(t4.state() == Transaction.State.Committed);

        Transaction t5 = new Transaction(heap);
        boolean caught = false;
        try {
            t5.run(() -> {throw new RuntimeException();});
        }
        catch (RuntimeException re) {
            caught = true;
        }
        assert(t5.state() == Transaction.State.Aborted);
        assert(caught);

        String planets = "Mercury Venus Earth Mars Saturn Jupiter Neptune Uranus Pluto";
        long rmbAddress = new Transaction(heap).run(() -> {
        // long rmbAddress = Transaction.run(heap, () -> {
            Transaction.checkTransactionActive(true);
            MemoryBlock rmb = heap.allocateMemoryBlock(1024, true);
            Transaction.checkTransactionActive(true);
            new Transaction(heap).run(() -> { 
            Transaction.checkTransactionActive(true);
            rmb.addToTransaction(0, 1024);  
            rmb.copyFromArray(planets.getBytes(), 0, 0, planets.length());
            });
            return rmb.handle();
        });

        MemoryBlock read = heap.memoryBlockFromHandle(rmbAddress);
        byte[] planetBytes = new byte[1024];
        read.copyToArray(0, planetBytes, 0, planetBytes.length - 1);
        String planets1 = new String(planetBytes).trim();
        assert(planets1.equals(planets));

        // Test Transaction with Range and Durable with Range
        {
            PersistentHeap pHeap = PersistentHeap.getHeap("/mnt/mem/persistent_heap", 100_000_000);
            PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(1024, true);
            PersistentMemoryBlock mb1 = pHeap.allocateMemoryBlock(1024, true);
            mb.transactionalWithRange(20, 29, (Range range) -> {
                range.setLong(20, 123456);
                range.setInt(24, 890);
                range.setLong(40, 23456);
                boolean caught1 = false;
                try{range.setLong(42, 11111);} catch (IndexOutOfBoundsException oob) {caught1 = true;} 
                assert(caught1);
                // caught1 = false;
                // try{mb.setLong(40, 34567);} catch (IllegalStateException ise) {caught1 = true;} 
                // assert(caught1);
            });

            mb.withRange(20, 29, (Range range) -> {
                range.setLong(20, 123456);
                range.setInt(24, 890);
                range.setLong(40, 23456);
                boolean caught1 = false;
                try{range.setLong(42, 11111);} catch (IndexOutOfBoundsException oob) {caught1 = true;} 
                assert(caught1);
                // caught1 = false;
                // try{mb.setLong(40, 34567);} catch (IllegalStateException ise) {caught1 = true;} 
                // assert(caught1);
            });

            mb.transactionalWithRange(20, 29, (Range range) -> {
                range.setLong(20, 123456);
                range.setInt(24, 890);
                range.setLong(40, 23456);
                // range.setLong(42, 11111);   // throws OOB
            });

            mb.transactionalWithRange(20L, 49L, (r) -> {
                r.setLong(20, 123456);
                r.setInt(24, 890);
                r.setLong(40, 23456);
                // r.setLong(43, 11111);   // throws OOB
            });
        }

        System.out.println("================================= All Transaction tests passed =================================");
    }

    private static void foo(Transaction tx, MemoryBlock block, long x) {
        tx.run(() -> {
            block.transactionalSetLong(10, x);
        });
    }

}
