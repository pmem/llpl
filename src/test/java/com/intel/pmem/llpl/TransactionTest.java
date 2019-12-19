/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.io.File;

class TransactionTest {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/persistent_pool";
        Heap heap = Heap.exists(heapName) ? Heap.openHeap(heapName) : Heap.createHeap(heapName, 2147483648L);
        MemoryBlock block1 = heap.allocateMemoryBlock(16, true);

        Transaction.create(heap, () -> {
            Transaction.checkTransactionActive(true);
            MemoryBlock b = heap.allocateMemoryBlock(128, true);
            Transaction.checkTransactionActive(true);
        });
        // compatibility test
        Transaction.create(heap, () -> {
            Transaction t1 = Transaction.create(heap);
            t1.run(() -> {
                Transaction.create(heap, () -> {
                    Transaction.create(heap, () -> {
                         t1.run(() -> {
                             Transaction.create(heap).run(() -> {
                                 block1.transactionalWithRange(0, 10, (Range r) -> {
                                     r.setLong(2, 123456);
                                 });
                             });
                         });
                    });
                });
            });
        });

        Transaction.create(heap, () -> {
            block1.setLong(4, 1000);
            block1.setInt(0, 777);
            assert(block1.getLong(4)) == 1000;
            assert(block1.getInt(0)) == 777;
            Transaction.create(heap, () -> {
                block1.setLong(8, 2000);
            });
        });
        assert(block1.getInt(0) == 777);

        // parameter passing syntax
        Transaction t1 = Transaction.create(heap);
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
        Transaction t2 = Transaction.create(heap);
        t2.run(() -> {
            block2.transactionalSetLong(4, 1000);
            block2.transactionalSetInt(0, 777);
            assert(block2.getLong(4)) == 1000;
            assert(block2.getInt(0)) == 777;
            method1(t2, block2, 888);
            assert(block2.getLong(10) == 888);
            // this produces a flattened inner transaction
            t2.run(() -> {       
                block1.transactionalSetLong(8, 2000);
            });
            assert(block1.getLong(8) == 2000);
        });
        // this will be a new top-level transaction
        method1(Transaction.create(heap), block2, 999);
        assert(block2.getLong(10) == 999);
            
        // test abort / commit behavior
        Transaction t3 = Transaction.create(heap);
        assert(t3.state() == Transaction.State.New);
        t3.run(() -> {});
        assert(t3.state() == Transaction.State.Committed);

        Transaction t4 = Transaction.create(heap);
        t4.run(() -> {});
        assert(t4.state() == Transaction.State.Committed);

        Transaction t5 = Transaction.create(heap);
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
        long rmbAddress = Transaction.create(heap).run(() -> {
            Transaction.checkTransactionActive(true);
            MemoryBlock rmb = heap.allocateMemoryBlock(1024, true);
            Transaction.checkTransactionActive(true);
            Transaction.create(heap).run(() -> { 
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
            String pHeapName = "/mnt/mem/persistent_heap";
            PersistentHeap pHeap = PersistentHeap.exists(pHeapName) ? PersistentHeap.openHeap(pHeapName) : PersistentHeap.createHeap(pHeapName, 100_000_000);
            PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(1024, true);
            PersistentMemoryBlock mb1 = pHeap.allocateMemoryBlock(1024, true);
            mb.transactionalWithRange(20, 29, (Range range) -> {
                range.setLong(20, 123456);
                range.setInt(24, 890);
                range.setLong(40, 23456);
                boolean caught1 = false;
                try{range.setLong(42, 11111);} catch (IndexOutOfBoundsException oob) {caught1 = true;} 
                assert(caught1);
            });

            mb.withRange(20, 29, (Range range) -> {
                range.setLong(20, 123456);
                range.setInt(24, 890);
                range.setLong(40, 23456);
                boolean caught1 = false;
                try{range.setLong(42, 11111);} catch (IndexOutOfBoundsException oob) {caught1 = true;} 
                assert(caught1);
            });

            mb.transactionalWithRange(20, 29, (Range range) -> {
                range.setLong(20, 123456);
                range.setInt(24, 890);
                range.setLong(40, 23456);
            });

            mb.transactionalWithRange(20L, 49L, (r) -> {
                r.setLong(20, 123456);
                r.setInt(24, 890);
                r.setLong(40, 23456);
            });
        }

        new File("/mnt/mem/persistent_pool").delete();
        new File("/mnt/mem/persistent_heap").delete();
        System.out.println("================================= All Transaction tests passed =================================");
    }

    private static void method1(Transaction tx, MemoryBlock block, long x) {
        tx.run(() -> {
            block.transactionalSetLong(10, x);
        });
    }
}
