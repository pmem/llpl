/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

public abstract class MemoryBlock<K extends MemoryBlock.Kind> {
    protected static final long SIZE_OFFSET = -8;
    private final Heap heap;
    private long size;
    private long address;       // TODO: consider rename to offset or blockOffset
    private long directAddress; // TODO: consider rename to address or blockAddress

    static {
        System.loadLibrary("llpl");
    }

    public interface Kind {}

    MemoryBlock(Heap heap, long size, boolean bounded) {
        this.heap = heap;
        new Transaction(heap).execute(() -> {
            this.address = heap.allocate(size + baseOffset());
            if (address == 0) throw new PersistenceException("Failed to allocate MemoryBlock of size " + size);
            directAddress = heap.poolAddress() + address;
            if (bounded) {
                this.size = size;
                setTransactionalLong(SIZE_OFFSET, size);
            }
        });
    }

    MemoryBlock(Heap heap, long poolAddress, long offset, boolean bounded) {
        this.heap = heap;
        this.address = offset;
        this.directAddress = poolAddress + offset;
        if (bounded) size = getLong(SIZE_OFFSET);
    }

    Heap heap() {return heap;}

    abstract long baseOffset();

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

    public long address() {return this.address;}
    public long size() {return this.size;}
    public void flush() {throw new UnsupportedOperationException();}
    public boolean isFlushed() {throw new UnsupportedOperationException();}

    public void checkBounds(long offset) {
        if (offset < 0 || offset >= size) throw new IndexOutOfBoundsException();
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
        nativeSetTransactionalByte(heap().poolAddress(), payloadAddress(offset), value);
    }

    public void setTransactionalShort(long offset, short value) {
        checkValid();
        nativeSetTransactionalShort(heap().poolAddress(), payloadAddress(offset), value);
    }

    public void setTransactionalInt(long offset, int value) {
        checkValid();
        nativeSetTransactionalInt(heap().poolAddress(), payloadAddress(offset), value);
    }

    public void setTransactionalLong(long offset, long value) {
        checkValid();
        nativeSetTransactionalLong(heap().poolAddress(), payloadAddress(offset), value);
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

    public void flush(long offset, long size) {
        checkValid();
        nativeFlush(payloadAddress(offset), size);
    }

    void flushAbsolute(long address, long size) {
        nativeFlush(address, size);
    }

    long payloadAddress(long payloadOffset) {
        return directAddress + baseOffset() + payloadOffset;
    }

    long directAddress() {
        return directAddress;
    }

    void checkValid() {
        if (directAddress != 0) return;
        throw new IllegalStateException();
    }

    void markInvalid() {
        directAddress = 0;
    }

    public boolean isValid() {
        return directAddress != 0;
    }

    void setAbsoluteByte(long address, byte value) {
        Heap.UNSAFE.putByte(address, value);
    }

    void setAbsoluteShort(long address, short value) {
        Heap.UNSAFE.putShort(address, value);
    }

    void setAbsoluteInt(long address, int value) {
        Heap.UNSAFE.putInt(address, value);
    }

    void setAbsoluteLong(long address, long value) {
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

    static native int nativeCopyBlockToBlock(long srcBlockDirectAddress, long srcOffset, long dstBlock, long dstOffset, long length);
    static native int nativeCopyFromByteArray(byte[] srcArray, int srcOffset, long dstBlockDirectAddress, long dstOffset, int length);
    static native int nativeSetMemory(long blockDirectAddress, long offset, int val, long length);

    // transactional
    native static void nativeSetTransactionalByte(long poolAddress, long address, byte value);
    native static void nativeSetTransactionalShort(long poolAddress, long address, short value);
    native static void nativeSetTransactionalInt(long poolAddress, long address, int value);
    native static void nativeSetTransactionalLong(long poolAddress, long address, long value);

    native static void nativeFlush(long address, long size);
    native static void addToTransaction(long address, long size);
}
