/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

import java.util.Arrays;
import java.util.TreeSet;

class MultipleHeapTest {
    public static void main(String[] args) {
        int N_HEAPS = 10;
        long HEAP_SIZE = 10 * 1024 * 1024;
        String HEAP_PREFIX = "/mnt/mem/heap_";
        Heap[] heaps = new Heap[N_HEAPS];

        // set root
        MemoryBlock[] blocks = new MemoryBlock[N_HEAPS];
        for (int i = 0; i < N_HEAPS; i++) {
            heaps[i] = Heap.getHeap(HEAP_PREFIX + i, HEAP_SIZE);
            heaps[i].setRoot((i + 1) * 1234);
            assert(heaps[i].getRoot() == (i + 1) * 1234);
        }

        // close heaps
        for (int i = 0; i < N_HEAPS; i++) {
            heaps[i].close();
        }

        // open heaps
        for (int i = 0; i < N_HEAPS; i++) {
            heaps[i] = Heap.getHeap(HEAP_PREFIX + i);
        }

        // get root
        for (int i = 0; i < N_HEAPS; i++) {
            assert(heaps[i].getRoot() == (i + 1) * 1234);
        }

        // transactions
        for (int i = 0; i < N_HEAPS; i++) {
            int ii = i;
            blocks[i] = heaps[i].allocateMemoryBlock(1024, true);
            Transaction.run(heaps[ii], () -> {
            // Transaction.run(heaps[ii], () -> {
                blocks[ii].setLong(100, ii * 12345);
            });
            Transaction.run(heaps[i], () -> {
            // Transaction.run(heaps[i], () -> {
                blocks[ii].setInt(200, ii * 23456);
            });
        }

        // memory block from address
        for (int i = 0; i < N_HEAPS; i++) {
            MemoryBlock block = heaps[i].memoryBlockFromHandle(blocks[i].handle());
            assert(block.getLong(100) == i * 12345);
            assert(block.getInt(200) == i * 23456);
        }
        
        // heap delete and existence    
        for (int i = 0; i < N_HEAPS; i++) {
            String name = HEAP_PREFIX + i;
            assert(Heap.exists(name));
            assert(Heap.deleteHeap(name));
            assert(!Heap.exists(name));
        }

        // allocation size registration
        // TODO: validate results
        int[] userSizes = new int[] {12, 105, 32, 50};
        Heap custom = Heap.getHeap("/mnt/mem/custom", 100_000_000L);
        for (int size : userSizes) custom.registerAllocationSize(size, false);
        custom.allocateMemoryBlock(12, true);
        custom.allocateMemoryBlock(13, true);
        custom.allocateMemoryBlock(32, true);

        System.out.println("================================= All MultipleHeap tests passed ================================");
    }
}
