/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

public abstract class MemoryBlock<K extends MemoryBlock.Kind> implements Comparable<MemoryBlock<K>> {
    protected static final long SIZE_OFFSET = 0;

    private long size;
    private long address;
    private long directAddress;

    static {
        System.loadLibrary("llpl");
    }

    MemoryBlock(Heap heap, long size) {
        address = heap.nativeAllocate(size + baseOffset());
        if (address == 0) throw new PersistenceException("Failed to allocate MemoryBlock of size " + size);
        this.size = size;
        directAddress = heap.poolAddress() + address;
        long sizeAddress = directAddress + SIZE_OFFSET;
        setAbsoluteLong(sizeAddress, size);
        flushAbsolute(sizeAddress, 8);
    }

    MemoryBlock(long poolAddress, long offset) {
        this.address = offset;
        this.directAddress = poolAddress + offset;
        size = getLong(SIZE_OFFSET);
    }

    abstract long baseOffset();

    public interface Kind {}

    // these must interpret offsets as relative to the start of payload data
    public abstract void setByte(long offset, byte value);
    public abstract void setShort(long offset, short value);
    public abstract void setInt(long offset, int value);
    public abstract void setLong(long offset, long value);

    // TODO: consider these signatures
    // public abstract void setBytes(byte[] srcBytes, int srcOffset, long destOffset, int length);
    // public abstract void getBytes(srcOffset, byte[] destBytes, int destOffset, int length);
    // public abstract byte[] getBytes(srcOffset, int length);

    public abstract void copyFromMemory(MemoryBlock<?> srcBlock, long srcOffset, long dstOffset, long length);
    public abstract void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length);
    public abstract void setMemory(byte val, long offset, long length);

    public long address() {
        return this.address;
    }

    public long size() {
        return this.size;
    }

    public void flush() {throw new UnsupportedOperationException();}
    public boolean isFlushed() {throw new UnsupportedOperationException();}

    public void checkBounds(long offset) throws IndexOutOfBoundsException {
        if (offset < 0 || offset >= size) throw new IndexOutOfBoundsException();
    }

    long payloadAddress(long payloadOffset) {
        return directAddress + baseOffset() + payloadOffset;
    }

    // these getters accept payload-relative offsets
    public byte getByte(long offset) {
        checkValid();
        return Heap.UNSAFE.getByte(payloadAddress(offset));
    }

    public short getShort(long offset) {
        checkValid();
        return Heap.UNSAFE.getShort(payloadAddress(offset));
    }

    public int getInt(long offset) {
        checkValid();
        return Heap.UNSAFE.getInt(payloadAddress(offset));
    }

    public long getLong(long offset) {
        checkValid();
        return Heap.UNSAFE.getLong(payloadAddress(offset));
    }

    // these setters accept payload-relative offsets
    public void setDurableByte(long offset, byte value) {
        checkValid();
        long address = payloadAddress(offset);
        Heap.UNSAFE.putByte(address, value);
        nativeFlush(address, 1);
    }

    public void setDurableShort(long offset, short value) {
        checkValid();
        long address = payloadAddress(offset);
        Heap.UNSAFE.putShort(address, value);
        nativeFlush(address, 2);
    }

    public void setDurableInt(long offset, int value) {
        checkValid();
        long address = payloadAddress(offset);
        Heap.UNSAFE.putInt(address, value);
        nativeFlush(address, 4);
    }

    public void setDurableLong(long offset, long value) {
        checkValid();
        long address = payloadAddress(offset);
        Heap.UNSAFE.putLong(address, value);
        nativeFlush(address, 8);
    }

    public void setTransactionalByte(long offset, byte value) {
        checkValid();
        nativeSetTransactionalByte(payloadAddress(offset), value);
    }

    public void setTransactionalShort(long offset, short value) {
        checkValid();
        nativeSetTransactionalShort(payloadAddress(offset), value);
    }

    public void setTransactionalInt(long offset, int value) {
        checkValid();
        nativeSetTransactionalInt(payloadAddress(offset), value);
    }

    public void setTransactionalLong(long offset, long value) {
        checkValid();
        nativeSetTransactionalLong(payloadAddress(offset), value);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(address());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MemoryBlock)) return false;
        return this.address() == ((MemoryBlock)o).address();
    }

    @Override
    public int compareTo(MemoryBlock that) {
        long diff = this.address() - that.address();
        if (diff < 0) return -1;
        else if (diff > 0) return 1;
        else return 0;
    }

    public void flush(long offset, long size) {
        checkValid();
        nativeFlush(payloadAddress(offset), size);
    }

    void flushAbsolute(long address, long size) {
        checkValid();
        nativeFlush(address, size);
    }

    long directAddress() {
        return directAddress;
    }

    void checkValid() {
        if (address != 0) return;
        throw new IllegalStateException();
    }

    void markInvalid() {
        address = 0;
    }

    void setAbsoluteByte(long address, byte value) {
        checkValid();
        Heap.UNSAFE.putByte(address, value);
    }

    void setAbsoluteShort(long address, short value) {
        checkValid();
        Heap.UNSAFE.putShort(address, value);
    }

    void setAbsoluteInt(long address, int value) {
        checkValid();
        Heap.UNSAFE.putInt(address, value);
    }

    void setAbsoluteLong(long address, long value) {
        checkValid();
        Heap.UNSAFE.putLong(address, value);
    }

    byte getAbsoluteByte(long address) {
        return Heap.UNSAFE.getByte(address);
    }

    short getAbsoluteShort(long address) {
        return Heap.UNSAFE.getShort(address);
    }

    int getAbsoluteInt(long address) {
        return Heap.UNSAFE.getInt(address);
    }

    long getAbsoluteLong(long address) {
        return Heap.UNSAFE.getLong(address);
    }

    void setRawByte(long offset, byte value) {
        checkValid();
        Heap.UNSAFE.putByte(payloadAddress(offset), value);
    }

    void setRawShort(long offset, short value) {
        checkValid();
        Heap.UNSAFE.putShort(payloadAddress(offset), value);
    }

    void setRawInt(long offset, int value) {
        checkValid();
        Heap.UNSAFE.putInt(payloadAddress(offset), value);
    }

    void setRawLong(long offset, long value) {
        checkValid();
        Heap.UNSAFE.putLong(payloadAddress(offset), value);
    }

    native int nativeMemoryBlockMemcpyRaw(long srcBlock, long srcOffset, long dstBlock, long dstOffset, long length);
    native int nativeFromByteArrayMemcpyRaw(byte[] srcArray, int srcOffset, long dstBlock, long dstOffset, int length);
    native int nativeMemoryBlockMemsetRaw(long block, long offset, int val, long length);

    // transactional
    native void nativeSetTransactionalByte(long address, byte value);
    native void nativeSetTransactionalShort(long address, short value);
    native void nativeSetTransactionalInt(long address, int value);
    native void nativeSetTransactionalLong(long address, long value);

    native void nativeFlush(long address, long size);
    native void addToTransaction(long address, long size);
}
