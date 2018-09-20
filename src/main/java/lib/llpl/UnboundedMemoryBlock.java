/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class UnboundedMemoryBlock extends MemoryBlock<Unbounded> {
    UnboundedMemoryBlock(Heap heap, long size) {
        super(heap, size, false);
    }

    UnboundedMemoryBlock(Heap heap, long poolHandle, long offset) {
        super(heap, poolHandle, offset, false);
    }

    @Override
    public void copyFromMemory(MemoryBlock<?> srcBlock, long srcOffset, long dstOffset, long length) {
        srcBlock.checkRange(srcOffset, length);
        checkRange(dstOffset, length);
        MemoryBlock.rawCopyBlockToBlock(srcBlock.directAddress() + srcBlock.baseOffset() + srcOffset, directAddress() + baseOffset() + dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        checkRange(dstOffset, length);
        MemoryBlock.rawCopyFromArray(srcArray, srcOffset, directAddress() + baseOffset() + dstOffset, length);
    }

    @Override
    public void setMemory(byte val, long offset, long length) {
        checkRange(offset, length);
        MemoryBlock.rawSetMemory(directAddress() + baseOffset() + offset, val, length);
    }

    @Override
    long baseOffset() { 
        return 0; 
    }

    @Override
    public long size() { 
        throw new UnsupportedOperationException(); 
    }

    @Override
    public void checkBounds(long offset) {
        heap().checkBounds(address() + offset);
    }

    @Override
    public void setByte(long offset, byte value) {
        checkBounds(offset);
        setAbsoluteByte(directAddress() + offset, value);
    }

    @Override
    public void setShort(long offset, short value) {
        checkBounds(offset);
        setAbsoluteShort(directAddress() + offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        checkBounds(offset);
        setAbsoluteInt(directAddress() + offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        checkBounds(offset);
        setAbsoluteLong(directAddress() + offset, value);
    }

    @Override
    void checkRange(long offset, long length) {
        if (offset < 0 || offset + length > heap().size()) throw new IndexOutOfBoundsException();
    }

}
