/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.io.File;
import java.util.Arrays;
import java.util.TreeSet;

class MultipleTransactionalHeapTest {
    public static void main(String[] args) {
        int N_HEAPS = 10;
        long HEAP_SIZE = 10 * 1024 * 1024;
        String HEAP_PREFIX = "/mnt/mem/mtheap_";
        TransactionalHeap[] heaps = new TransactionalHeap[N_HEAPS];

        // set root
        TransactionalMemoryBlock[] blocks = new TransactionalMemoryBlock[N_HEAPS];
        for (int i = 0; i < N_HEAPS; i++) {
            heaps[i] = TransactionalHeap.createHeap(HEAP_PREFIX + i, HEAP_SIZE);
            heaps[i].setRoot((i + 1) * 1234);
            assert(heaps[i].getRoot() == (i + 1) * 1234);
        }

        // close heaps
        for (int i = 0; i < N_HEAPS; i++) {
            heaps[i].close();
        }

        // open heaps
        for (int i = 0; i < N_HEAPS; i++) {
            heaps[i] = TransactionalHeap.openHeap(HEAP_PREFIX + i);
        }

        // get root
        for (int i = 0; i < N_HEAPS; i++) {
            assert(heaps[i].getRoot() == (i + 1) * 1234);
        }

        // transactions
        for (int i = 0; i < N_HEAPS; i++) {
            int ii = i;
            blocks[i] = heaps[i].allocateMemoryBlock(1024);
            Transaction.create(heaps[ii], () -> {
                blocks[ii].setLong(100, ii * 12345);
            });
            Transaction.create(heaps[i], () -> {
                blocks[ii].setInt(200, ii * 23456);
            });
        }

        // memory block from address
        for (int i = 0; i < N_HEAPS; i++) {
            TransactionalMemoryBlock block = heaps[i].memoryBlockFromHandle(blocks[i].handle());
            assert(block.getLong(100) == i * 12345);
            assert(block.getInt(200) == i * 23456);
        }
        
        // close heaps
        for (int i = 0; i < N_HEAPS; i++) {
            heaps[i].close();
        }

        // heap delete and existence    
        for (int i = 0; i < N_HEAPS; i++) {
            String name = HEAP_PREFIX + i;
            assert(TransactionalHeap.exists(name));
            assert(new File(name).delete());
            assert(!TransactionalHeap.exists(name));
        }

        // allocation size registration
        // TODO: validate results
        int[] userSizes = new int[] {12, 105, 32, 50};
        TransactionalHeap custom = TransactionalHeap.createHeap("/mnt/mem/custom", 100_000_000);
        for (int size : userSizes) custom.registerAllocationSize(size, false);
        custom.allocateMemoryBlock(12);
        custom.allocateMemoryBlock(13);
        custom.allocateMemoryBlock(32);

        new File("/mnt/mem/custom").delete();
        System.out.println("================================= All MultipleTransactionalHeap tests passed ===================");
    }
}
