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
        super(heap, size);
    }

    RawMemoryBlock(long poolAddress, long offset) {
        super(poolAddress, offset);
    }

    @Override
    public void copyFromMemory(MemoryBlock<?> srcBlock, long srcOffset, long dstOffset, long length) {
        nativeMemoryBlockMemcpyRaw(srcBlock.address(), srcBlock.baseOffset() + srcOffset, address(), baseOffset() + dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        nativeFromByteArrayMemcpyRaw(srcArray, srcOffset, address(), baseOffset() + dstOffset, length);
    }

    @Override
    public void setMemory(byte val, long offset, long length) {
        nativeMemoryBlockMemsetRaw(address(), baseOffset() + offset, val, length);
    }

    @Override
    long baseOffset() { 
        return METADATA_SIZE; 
    }

    @Override
    public void setByte(long offset, byte value) {
        setRawByte(offset, value);
    }

    @Override
    public void setShort(long offset, short value) {
        setRawShort(offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        setRawInt(offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        setRawLong(offset, value);
    }
}
