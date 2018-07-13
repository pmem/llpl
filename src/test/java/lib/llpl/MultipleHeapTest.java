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
        MemoryBlock[] blocks = new MemoryBlock[N_HEAPS];
        for (int i = 0; i < N_HEAPS; i++) heaps[i] = Heap.getHeap(HEAP_PREFIX + i, HEAP_SIZE);

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

        for (int i = 0; i < N_HEAPS; i++) {
            MemoryBlock<Transactional> block = heaps[i].memoryBlockFromAddress(Transactional.class, blocks[i].address());
            assert(block.getLong(100) == i * 12345);
            assert(block.getInt(200) == i * 23456);
        }
            
        for (int i = 0; i < N_HEAPS; i++) assert(Heap.freeHeap(HEAP_PREFIX + i));
        System.out.println("=================================All MultipleHeap tests passed=======================");
    }
}
