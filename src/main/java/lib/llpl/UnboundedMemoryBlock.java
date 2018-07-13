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

    UnboundedMemoryBlock(Heap heap, long poolAddress, long offset) {
        super(heap, poolAddress, offset, false);
    }

    @Override
    public void copyFromMemory(MemoryBlock<?> srcBlock, long srcOffset, long dstOffset, long length) {
        nativeCopyBlockToBlock(srcBlock.directAddress(), srcBlock.baseOffset() + srcOffset, directAddress(), baseOffset() + dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        nativeCopyFromByteArray(srcArray, srcOffset, directAddress(), baseOffset() + dstOffset, length);
    }

    @Override
    public void setMemory(byte val, long offset, long length) {
        nativeSetMemory(directAddress(), baseOffset() + offset, val, length);
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
        throw new UnsupportedOperationException(); 
    }

    @Override
    public void setByte(long offset, byte value) {
        setAbsoluteByte(directAddress() + offset, value);
    }

    @Override
    public void setShort(long offset, short value) {
        setAbsoluteShort(directAddress() + offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        setAbsoluteInt(directAddress() + offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        setAbsoluteLong(directAddress() + offset, value);
    }
}
