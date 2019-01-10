/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package examples.array;

import lib.llpl.*;

public class MemoryBlockArray {
    private static final int HEADER_SIZE = 4;
    private static TransactionalHeap heap = TransactionalHeap.getHeap("/mnt/mem/persistent_pool_tx", 2147483648L);
    TransactionalMemoryBlock block;


    public static void main(String[] args) {
        int size = 10;
        MemoryBlockArray mra = new MemoryBlockArray(size);
        for (int i = 0; i < size; i++) {
            assert(mra.get(i) == null);
        }
        TransactionalMemoryBlock mr1 = heap.allocateMemoryBlock(10);
        mr1.setLong(0, 0xcafe);
        TransactionalMemoryBlock mr2 = heap.allocateMemoryBlock(20);
        mr2.setLong(0, 0xbeef);

        mra.set(5, mr1);
        mra.set(7, mr2);
        assert(mra.size() == 10);

        for (int i = 0; i < size; i++) {
            if (i == 5) assert(mra.get(i) != null && mra.get(i).handle() == mr1.handle() && mra.get(i).getLong(0) == 0xcafe);
            else if (i == 7) assert(mra.get(i) != null && mra.get(i).handle() == mr2.handle() && mra.get(i).getLong(0) == 0xbeef);
            else assert(mra.get(i) == null);
        }
        assert(mra.size() == 10);
        boolean caught = false;
        try {
            mra.set(10, heap.allocateMemoryBlock(5));
        } catch (ArrayIndexOutOfBoundsException e) {
            caught = true;
        }
        assert(caught);
        System.out.println("done");
    }

    public static MemoryBlockArray fromAddress(long addr) {
        TransactionalMemoryBlock block = heap.memoryBlockFromHandle(addr);
        return new MemoryBlockArray(block);
    }

    public MemoryBlockArray(int size) {
        this.block = heap.allocateMemoryBlock(HEADER_SIZE + Long.BYTES * size);
        this.block.setInt(0, size);
    }

    private MemoryBlockArray(TransactionalMemoryBlock block) {
        this.block = block;
    }

    public void set(int index, TransactionalMemoryBlock value) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        block.setLong(HEADER_SIZE + Long.BYTES * index, value == null ? 0 : value.handle());
    }

    public TransactionalMemoryBlock get(int index) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        long addr = block.getInt(HEADER_SIZE + Long.BYTES * index);
        return addr == 0 ? null : heap.memoryBlockFromHandle(addr);
    }

    public int size() {
        return block.getInt(0);
    }

    public long address() {
        return block.handle();
    }
}
