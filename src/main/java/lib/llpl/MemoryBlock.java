/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

public abstract class MemoryBlock<K extends MemoryBlock.Kind> {
    protected static final long SIZE_OFFSET = 0;
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
        long allocSize = size + baseOffset();
        new Transaction(heap).execute(() -> {
            long[] addr_size_pair = heap.allocate(allocSize);
            this.address = addr_size_pair[0];
            if (address == 0) throw new PersistenceException("Failed to allocate MemoryBlock of size " + size);
            this.directAddress = heap.directAddress(address);
            if (bounded) setPersistentSize(addr_size_pair[1] - baseOffset());
            else this.size = -1;
        });
        // Stats.current.allocStats.update(getClass().getName(), allocSize, 0, 1);   // uncomment for allocation stats
    }

    MemoryBlock(Heap heap, long unused, long offset, boolean bounded) {
        this.heap = heap;
        this.address = offset;
        this.directAddress = heap.poolHandle() + address;
        this.size = bounded ? getPersistentSize() : -1;
    }

    Heap heap() {return heap;}

    abstract long baseOffset();

    // these must interpret offsets as relative to the start of payload data
    public abstract void setByte(long offset, byte value);
    public abstract void setShort(long offset, short value);
    public abstract void setInt(long offset, int value);
    public abstract void setLong(long offset, long value);

    public abstract void copyFromMemory(MemoryBlock<?> srcBlock, long srcOffset, long dstOffset, long length);
    public abstract void setMemory(byte val, long offset, long length);
    public abstract void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length);
    
    public void copyToArray(long srcOffset, byte[] dstArray, int dstOffset, int length) {
        checkValid();
        checkRange(srcOffset, length);
        if (dstOffset < 0 || dstOffset + length >= dstArray.length) throw new IndexOutOfBoundsException("array index out of bounds.");
        MemoryBlock.rawCopyToArray(directAddress() + baseOffset() + srcOffset, dstArray, dstOffset, length);
    }

    // TODO: consider these signatures
    // public abstract void setBytes(byte[] srcBytes, int srcOffset, long destOffset, int length);
    // public abstract void getBytes(srcOffset, byte[] destBytes, int destOffset, int length);
    // public abstract byte[] getBytes(srcOffset, int length);

    public long address() {return this.address;}
    public long size() {return this.size;}
    public boolean isFlushed() {throw new UnsupportedOperationException();}

    public void checkBounds(long offset) {
        if (offset < 0 || offset >= size) throw new IndexOutOfBoundsException();
    }

    // these getters accept payload-relative offsets
    public byte getByte(long offset) {
        checkValid();
        checkBounds(offset);
        return Heap.UNSAFE.getByte(payloadAddress(offset));
    }

    public short getShort(long offset) {
        checkValid();
        checkBounds(offset);
        return Heap.UNSAFE.getShort(payloadAddress(offset));
    }

    public int getInt(long offset) {
        checkValid();
        checkBounds(offset);
        return Heap.UNSAFE.getInt(payloadAddress(offset));
    }

    public long getLong(long offset) {
        checkValid();
        checkBounds(offset);
        return Heap.UNSAFE.getLong(payloadAddress(offset));
    }

    // these setters accept payload-relative offsets
    public void setDurableByte(long offset, byte value) {
        checkValid();
        checkBounds(offset);
        long address = payloadAddress(offset);
        Heap.UNSAFE.putByte(address, value);
        nativeFlush(address, 1);
    }

    public void setDurableShort(long offset, short value) {
        checkValid();
        checkBounds(offset);
        long address = payloadAddress(offset);
        Heap.UNSAFE.putShort(address, value);
        nativeFlush(address, 2);
    }

    public void setDurableInt(long offset, int value) {
        checkValid();
        checkBounds(offset);
        long address = payloadAddress(offset);
        Heap.UNSAFE.putInt(address, value);
        nativeFlush(address, 4);
    }

    public void setDurableLong(long offset, long value) {
        checkValid();
        checkBounds(offset);
        long address = payloadAddress(offset);
        Heap.UNSAFE.putLong(address, value);
        nativeFlush(address, 8);
    }

    public void setTransactionalByte(long offset, byte value) {
        checkValid();
        checkBounds(offset);
        nativeSetTransactionalByte(heap().poolHandle(), payloadAddress(offset), value);
    }

    public void setTransactionalShort(long offset, short value) {
        checkValid();
        checkBounds(offset);
        nativeSetTransactionalShort(heap().poolHandle(), payloadAddress(offset), value);
    }

    public void setTransactionalInt(long offset, int value) {
        checkValid();
        checkBounds(offset);
        nativeSetTransactionalInt(heap().poolHandle(), payloadAddress(offset), value);
    }

    public void setTransactionalLong(long offset, long value) {
        checkValid();
        checkBounds(offset);
        nativeSetTransactionalLong(heap().poolHandle(), payloadAddress(offset), value);
    }

    public void transactionalCopyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        checkRange(dstOffset, length);
        new Transaction(heap()).execute(() -> {
            MemoryBlock.txCopyFromArray(srcArray, srcOffset, directAddress() + baseOffset() + dstOffset, length);
        });
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

    public void flush() {
        flush(0, size());
    }

    public void flush(long offset, long size) {
        checkValid();
        checkRange(offset, size);
        nativeFlush(payloadAddress(offset), size);
    }

    public void addToTransaction(long offset, long size) {
        checkValid();
        checkRange(offset, size);
        nativeAddToTransaction(payloadAddress(offset), size);
    }


    public boolean isValid() {
        return directAddress != 0;
    }
    // end of public methods

    void setPersistentSize(long size) {
        nativeSetTransactionalLong(heap().poolHandle(), directAddress + SIZE_OFFSET, size);
        this.size = size;     
    }

    long getPersistentSize() {
        return getAbsoluteLong(directAddress + SIZE_OFFSET);
    }

    static void flushAbsolute(long address, long size) {
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

    void checkRange(long offset, long length) {
        if (offset < 0 || offset + length > size) throw new IndexOutOfBoundsException();
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
        Heap.UNSAFE.putByte(payloadAddress(offset), value);
    }

    void setRawShort(long offset, short value) {
        Heap.UNSAFE.putShort(payloadAddress(offset), value);
    }

    void setRawInt(long offset, int value) {
        Heap.UNSAFE.putInt(payloadAddress(offset), value);
    }

    void setRawLong(long offset, long value) {
        Heap.UNSAFE.putLong(payloadAddress(offset), value);
    }

    static void rawCopyToArray(long srcAddress, byte[] dstArray, int dstOffset, int length) {
        long dstAddress = Heap.UNSAFE.ARRAY_BYTE_BASE_OFFSET + Heap.UNSAFE.ARRAY_BYTE_INDEX_SCALE * dstOffset;
        Heap.UNSAFE.copyMemory(null, srcAddress, dstArray, dstAddress, length);
    }

    // Raw
    static void rawCopyBlockToBlock(long srcAddress, long dstAddress, long length) {
        Heap.UNSAFE.copyMemory(srcAddress, dstAddress, length);
    } 

    static void rawCopyFromArray(byte[] srcArray, int srcOffset, long dstAddress, int length) {
        long srcAddress = Heap.UNSAFE.ARRAY_BYTE_BASE_OFFSET + Heap.UNSAFE.ARRAY_BYTE_INDEX_SCALE * srcOffset;
        Heap.UNSAFE.copyMemory(srcArray, srcAddress, null, dstAddress, length);
    }

    static void rawSetMemory(long dstAddress, byte val, long length) {
        Heap.UNSAFE.setMemory(dstAddress, length, val); 
    }

    // Durable
    static void durableCopyBlockToBlock(long srcAddress, long dstAddress, long length) {
        rawCopyBlockToBlock(srcAddress, dstAddress, length);
        flushAbsolute(dstAddress, length);
    }

    static void durableCopyFromArray(byte[] srcArray, int srcOffset, long dstAddress, int length) {
        rawCopyFromArray(srcArray, srcOffset, dstAddress, length);
        flushAbsolute(dstAddress, length);
    }

    static void durableSetMemory(long dstAddress, byte val, long length) {
        rawSetMemory(dstAddress, val, length);
        flushAbsolute(dstAddress, length);
    }

    // Transactional -- must be called from within an active transaction
    static void txCopyBlockToBlock(long srcAddress, long dstAddress, long length) {
        nativeAddToTransaction(dstAddress, length); 
        rawCopyBlockToBlock(srcAddress, dstAddress, length);
    }

    static void txCopyFromArray(byte[] srcArray, int srcOffset, long dstAddress, int length) {
        nativeAddToTransaction(dstAddress, length);
        rawCopyFromArray(srcArray, srcOffset, dstAddress, length);
    }

    static void txSetMemory(long dstAddress, byte val, long length) {
        nativeAddToTransaction(dstAddress, length); 
        rawSetMemory(dstAddress, val, length);
    }

    // for debug
    public String contents() {
        StringBuilder buff = new StringBuilder();
        buff.append("[");
        for (int i = 0; i < size() - 1; i++) buff.append(getByte(i)).append(", ");
        buff.append(getByte(size() - 1)).append("]");
        return buff.toString();
    }

    // transactional
    native static void nativeSetTransactionalByte(long poolHandle, long address, byte value);
    native static void nativeSetTransactionalShort(long poolHandle, long address, short value);
    native static void nativeSetTransactionalInt(long poolHandle, long address, int value);
    native static void nativeSetTransactionalLong(long poolHandle, long address, long value);

    native static void nativeFlush(long address, long size);
    native static void nativeAddToTransaction(long address, long size);
}

