/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class MultipleHeapTest {
    public static void main(String[] args) {
        int N_HEAPS = 10; //Integer.parseInt(args[0]);
        long HEAP_SIZE = 10 * 1024 * 1024; //Long.parseLong(args[1]);
        String HEAP_PREFIX = "/mnt/mem/heap_";
        Heap[] heaps = new Heap[N_HEAPS];

        // set root
        MemoryBlock[] blocks = new MemoryBlock[N_HEAPS];
        for (int i = 0; i < N_HEAPS; i++) {
            heaps[i] = Heap.getHeap(HEAP_PREFIX + i, HEAP_SIZE);
            heaps[i].setRoot((i + 1) * 1234);
            assert(heaps[i].getRoot() == (i + 1) * 1234);
        }

        // get root
        for (int i = 0; i < N_HEAPS; i++) {
            heaps[i] = Heap.getHeap(HEAP_PREFIX + i);
            assert(heaps[i].getRoot() == (i + 1) * 1234);
        }

        // transactions
        for (int i = 0; i < N_HEAPS; i++) {
            int ii = i;
            blocks[i] = heaps[i].allocateMemoryBlock(Transactional.class, 1024);
            Transaction.run(heaps[ii], () -> {
                blocks[ii].setLong(100, ii * 12345);
            });
            new Transaction(heaps[i]).execute(() -> {
                blocks[ii].setInt(200, ii * 23456);
            });
        }

        // memory block from address
        for (int i = 0; i < N_HEAPS; i++) {
            MemoryBlock<Transactional> block = heaps[i].memoryBlockFromAddress(Transactional.class, blocks[i].address());
            assert(block.getLong(100) == i * 12345);
            assert(block.getInt(200) == i * 23456);
        }
        
        // heap free and existence    
        for (int i = 0; i < N_HEAPS; i++) {
            String name = HEAP_PREFIX + i;
            assert(Heap.exists(name));
            assert(Heap.freeHeap(name));
            assert(!Heap.exists(name));
        }

        System.out.println("=================================All MultipleHeap tests passed=======================");
    }
}
