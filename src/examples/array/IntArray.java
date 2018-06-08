/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package examples.array;

import lib.llpl.*;

public class IntArray {
    static final int HEADER_SIZE = 4;

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
    }

    private static Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
    MemoryBlock<Flushable> block;

    public static IntArray fromAddress(long addr) {
        MemoryBlock<Flushable> block = h.memoryBlockFromAddress(Flushable.class, addr);
        if (!block.isFlushed()) {     // simple consistency scheme: if not consistent, throw away, return null
            h.freeMemoryBlock(block);
            return null;
        } else {
            return new IntArray(block);
        }
    }

    public IntArray(int size) {
        this.block = (MemoryBlock<Flushable>)h.allocateMemoryBlock(Flushable.class, HEADER_SIZE + Integer.BYTES * size);
        this.block.setInt(0, size);
    }

    private IntArray(MemoryBlock<Flushable> block) {
        this.block = block;
    }

    public void set(int index, int value) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        block.setInt(HEADER_SIZE + Integer.BYTES * index, value);
        block.flush();
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
        return block.address();
    }
}
