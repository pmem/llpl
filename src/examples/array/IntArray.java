/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package examples.array;

import lib.llpl.*;

public class IntArray {
    private static final int HEADER_SIZE = 4;
    private static Heap heap = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
    MemoryBlock block;


    public static void main(String[] args) {
        int size = 10;
        IntArray ia = new IntArray(size);
        for (int i = 0; i < size; i++) {
            assert(ia.get(i) == 0);
        }
        ia.set(5, 10);
        ia.set(7, 20);
        assert(ia.size() == 10);
        for (int i = 0; i < size; i++) {
            // System.out.format("i = %d, ia[i] = %d\n", i, ia.get(i));
            if (i == 5) assert(ia.get(i) == 10);
            else if (i == 7) assert(ia.get(i) == 20);
            else assert(ia.get(i) == 0);
        }
        assert(ia.size() == 10);
        boolean caught = false;
        try {
            ia.set(10, 100);
        } catch (ArrayIndexOutOfBoundsException e) {
            caught = true;
        }
        assert(caught);
        System.out.println("done");
    }

    public static IntArray fromAddress(long addr) {
        MemoryBlock block = heap.memoryBlockFromHandle(addr);
        return new IntArray(block);
    }

    public IntArray(int size) {
        this.block = heap.allocateMemoryBlock(HEADER_SIZE + Integer.BYTES * size, false);
        this.block.setInt(0, size);
    }

    private IntArray(MemoryBlock block) {
        this.block = block;
    }

    public void set(int index, int value) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        block.setInt(HEADER_SIZE + Integer.BYTES * index, value);
    }

    public int get(int index) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return block.getInt(HEADER_SIZE + Integer.BYTES * index);
    }

    public int size() {
        return block.getInt(0);
    }

    public long address() {
        return block.handle();
    }
}
