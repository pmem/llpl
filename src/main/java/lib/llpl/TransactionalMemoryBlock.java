/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

class TransactionalMemoryBlock extends MemoryBlock<Transactional> {
    private static final long METADATA_SIZE = 8;

    TransactionalMemoryBlock(Heap heap, long size) {
        super(heap, size, true);
    }

    TransactionalMemoryBlock(Heap heap, long poolAddress, long offset) {
        super(heap, poolAddress, offset, true);
    }

    @Override
    public void setByte(long offset, byte value) {
        setTransactionalByte(offset, value);
    }

    @Override
    public void setShort(long offset, short value) {
        setTransactionalShort(offset, value);
    }

    @Override
    public void setInt(long offset, int value) {
        setTransactionalInt(offset, value);
    }

    @Override
    public void setLong(long offset, long value) {
        setTransactionalLong(offset, value);
    }

    @Override
    public void setDurableByte(long offset, byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDurableShort(long offset, short value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDurableInt(long offset, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDurableLong(long offset, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyFromMemory(MemoryBlock<?> srcBlock, long srcOffset, long dstOffset, long length) {
        if (nativeCopyBlockToBlock(heap().poolAddress(), srcBlock.directAddress(), srcBlock.baseOffset() + srcOffset, directAddress(), baseOffset() + dstOffset, length) != 0)
            throw new PersistenceException("Failed to copy from MemoryBlock");
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        nativeCopyFromByteArray(heap().poolAddress(), srcArray, srcOffset, directAddress(), baseOffset() + dstOffset, length);
    }

    @Override
    public void setMemory(byte val, long offset, long length) {
        nativeSetMemory(heap().poolAddress(), directAddress(), baseOffset() + offset, val, length);
    }

    @Override
    long baseOffset() {
        return METADATA_SIZE; 
    }

    private static native int nativeCopyBlockToBlock(long poolAddress, long srcBlockDirectAddress, long srcOffset, long dstBlock, long dstOffset, long length);
    private static native int nativeCopyFromByteArray(long poolAddress, byte[] srcArray, int srcOffset, long dstBlockDirectAddress, long dstOffset, int length);
    private static native int nativeSetMemory(long poolAddress, long blockDirectAddress, long offset, int val, long length);
}
