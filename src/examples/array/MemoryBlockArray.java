/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package examples.array;

import lib.llpl.*;

public class MemoryBlockArray {
    static final int HEADER_SIZE = 4;

    public static void main(String[] args) {
        int size = 10;
        MemoryBlockArray mra = new MemoryBlockArray(size);
        for (int i = 0; i < size; i++) {
            assert(mra.get(i) == null);
        }
        MemoryBlock<Transactional> mr1 = h.allocateMemoryBlock(Transactional.class, 10);
        mr1.setLong(0, 0xcafe);
        MemoryBlock<Transactional> mr2 = h.allocateMemoryBlock(Transactional.class, 20);
        mr2.setLong(0, 0xbeef);

        mra.set(5, mr1);
        mra.set(7, mr2);
        assert(mra.size() == 10);

        for (int i = 0; i < size; i++) {
            if (i == 5) assert(mra.get(i) != null && mra.get(i).address() == mr1.address() && mra.get(i).getLong(0) == 0xcafe);
            else if (i == 7) assert(mra.get(i) != null && mra.get(i).address() == mr2.address() && mra.get(i).getLong(0) == 0xbeef);
            else assert(mra.get(i) == null);
        }
        assert(mra.size() == 10);
        boolean caught = false;
        try {
            mra.set(10, h.allocateMemoryBlock(Transactional.class, 5));
        } catch (ArrayIndexOutOfBoundsException e) {
            caught = true;
        }
        assert(caught);
    }

    private static Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
    MemoryBlock<Transactional> block;

    public static MemoryBlockArray fromAddress(long addr) {
        MemoryBlock<Transactional> block = h.memoryBlockFromAddress(Transactional.class, addr);
        return new MemoryBlockArray(block);
    }

    public MemoryBlockArray(int size) {
        this.block = h.allocateMemoryBlock(Transactional.class, HEADER_SIZE + Long.BYTES * size);
        this.block.setInt(0, size);
    }

    private MemoryBlockArray(MemoryBlock<Transactional> block) {
        this.block = block;
    }

    public void set(int index, MemoryBlock<Transactional> value) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        block.setLong(HEADER_SIZE + Long.BYTES * index, value == null ? 0 : value.address());
    }

    public MemoryBlock<Transactional> get(int index) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        long addr = block.getInt(HEADER_SIZE + Long.BYTES * index);
        return addr == 0 ? null : h.memoryBlockFromAddress(Transactional.class, addr);
    }

    public int size() {
        return block.getInt(0);
    }

    public long address() {
        return block.address();
    }
}
