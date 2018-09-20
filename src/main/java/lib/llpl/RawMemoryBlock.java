/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class RawMemoryBlock extends MemoryBlock<Raw> {
    private static final long METADATA_SIZE = 8;


    RawMemoryBlock(Heap heap, long size) {
        super(heap, size, true);
    }

    RawMemoryBlock(Heap heap, long poolHandle, long offset) {
        super(heap, poolHandle, offset, true);
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
        return METADATA_SIZE; 
    }

    @Override
    public void setByte(long offset, byte value) {
        checkValid();
        checkBounds(offset);
        setRawByte(offset, value);
    }

    @Override
    public void setShort(long offset, short value) {
        checkValid();
        checkBounds(offset);
        setRawShort(offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        checkValid();
        checkBounds(offset);
        setRawInt(offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        checkValid();
        checkBounds(offset);
        setRawLong(offset, value);
    }
}
