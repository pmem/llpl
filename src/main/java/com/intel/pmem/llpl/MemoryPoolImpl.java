/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import java.io.File;
import java.nio.ByteBuffer;
import sun.misc.Unsafe;

class MemoryPoolImpl implements MemoryPool {

    static Unsafe UNSAFE;

    static {
        Util.loadLibrary();
        try {
            java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe)f.get(null);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to initialize UNSAFE.");
        }
    }

    private long poolAddress;
    private final long poolSize;
    private final String poolPath;

    MemoryPoolImpl(String path, long byteCount) {
        if (byteCount <= 0) throw new IllegalArgumentException("byteCount must be greater than zero");
        File file = new File(path);
        if (file.exists()) throw new MemoryPoolException("Unable to create pool, path already exists:" + path);
        try {
            this.poolAddress = nativeOpenPool(path, byteCount);
        } catch (MemoryPoolException e) {
            throw new MemoryPoolException("Unable to create pool: " + e.getMessage());
        }
        this.poolSize = byteCount;
        this.poolPath = path;
    }

    MemoryPoolImpl(String path) {
        File file = new File(path);
        if (path.startsWith("/dev/dax")) {
            try {
                this.poolAddress = nativeOpenPool(path, 0);
            } catch (MemoryPoolException e) {
                throw new MemoryPoolException("Unable to map device: " + e.getMessage());
            }
            this.poolSize = nativePoolSize(path);
            this.poolPath = path;
        }
        else {
            if (file.exists() && file.isFile()) this.poolSize = file.length();
            else throw new IllegalArgumentException("path supplied must be an existing file");
            try {
                this.poolAddress = nativeOpenPool(path, this.poolSize);
            } catch (MemoryPoolException e) {
                throw new MemoryPoolException("Unable to open pool: " + e.getMessage());
            }
            this.poolPath = path;
        }
    }

    private void checkBounds(long offset, long byteCount) {
        if (offset < 0 || byteCount < 0 || offset + byteCount > poolSize) {
            StringBuilder errorMessage = new StringBuilder("MemoryPool");
            if (offset < 0) errorMessage.append("negative offset: " + offset);
            else if (byteCount < 0) errorMessage.append("negative length: " + byteCount);
            else errorMessage.append(String.format("offset + length is out of bounds: %s + %s", offset, byteCount));
            throw new IndexOutOfBoundsException(errorMessage.toString());
        }
    }

    private long dataAddress(long offset) {
        return poolAddress + offset;
    }

    void close() {
        int result = nativeClosePool(poolAddress, poolSize);
        if (result == -1) throw new MemoryPoolException("Unable to close pool");
    }

    @Override
    public long size() {
        return poolSize;
    }

    @Override
    public byte getByte(long offset) {
        checkBounds(offset, Byte.BYTES);
        return UNSAFE.getByte(dataAddress(offset));
    }

    @Override
    public short getShort(long offset) {
        checkBounds(offset, Short.BYTES);
        return UNSAFE.getShort(dataAddress(offset));
    }

    @Override
    public int getInt(long offset){
        checkBounds(offset, Integer.BYTES);
        return UNSAFE.getInt(dataAddress(offset));
    }

    @Override
    public long getLong(long offset){
        checkBounds(offset, Long.BYTES);
        return UNSAFE.getLong(dataAddress(offset));
    }

    @Override
    public void setByte(long offset, byte value){
        checkBounds(offset, Byte.BYTES);
        UNSAFE.putByte(dataAddress(offset), value);
    }

    @Override
    public void setShort(long offset, short value){
        checkBounds(offset, Short.BYTES);
        UNSAFE.putShort(dataAddress(offset), value);
    }

    @Override
    public void setInt(long offset, int value){
        checkBounds(offset, Integer.BYTES);
        UNSAFE.putInt(dataAddress(offset), value);
    }

    @Override
    public void setLong(long offset, long value){
        checkBounds(offset, Long.BYTES);
        UNSAFE.putLong(dataAddress(offset), value);
    }

    @Override
    public void copyFromPool(long srcOffset, long dstOffset, long byteCount) {
        checkBounds(srcOffset, byteCount);
        checkBounds(dstOffset, byteCount);
        UNSAFE.copyMemory(dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
    }

    @Override
    public void copyFromPool(MemoryPool srcPool, long srcOffset, long dstOffset, long byteCount) {
        ((MemoryPoolImpl)srcPool).checkBounds(srcOffset, byteCount);
        checkBounds(dstOffset, byteCount);
        UNSAFE.copyMemory(((MemoryPoolImpl)srcPool).dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
    }

    @Override
    public void copyFromByteArray(byte[] srcArray, int srcIndex, long dstOffset, int byteCount) {
        if (srcIndex < 0 || srcIndex + byteCount > srcArray.length) {
            throw new IndexOutOfBoundsException(indexOutOfBoundsMessage(srcIndex, byteCount));
        }
        checkBounds(dstOffset, byteCount);
        long srcAddress = UNSAFE.ARRAY_BYTE_BASE_OFFSET + UNSAFE.ARRAY_BYTE_INDEX_SCALE * srcIndex;
        UNSAFE.copyMemory(srcArray, srcAddress, null, dataAddress(dstOffset), byteCount);
    }

    @Override
    public void copyToByteArray(long srcOffset, byte[] dstArray, int dstIndex, int byteCount) {
        if (dstIndex < 0 || dstIndex + byteCount > dstArray.length) {
            throw new IndexOutOfBoundsException(indexOutOfBoundsMessage(dstIndex, byteCount));
        }
        checkBounds(srcOffset, byteCount);
        long dstAddress = UNSAFE.ARRAY_BYTE_BASE_OFFSET + UNSAFE.ARRAY_BYTE_INDEX_SCALE * dstIndex;
        UNSAFE.copyMemory(null, dataAddress(srcOffset), dstArray, dstAddress, byteCount);
    }

    @Override
    public void setMemory(byte value, long offset, long byteCount) {
        checkBounds(offset, byteCount);
        UNSAFE.setMemory(dataAddress(offset), byteCount, value);
    }

    @Override
    public void copyFromPoolNT(long srcOffset, long dstOffset, long byteCount) {
        checkBounds(srcOffset, byteCount);
        checkBounds(dstOffset, byteCount);
        nativeCopyMemoryNT(dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
    }

    @Override
    public void copyFromPoolNT(MemoryPool srcPool, long srcOffset, long dstOffset, long byteCount) {
        ((MemoryPoolImpl)srcPool).checkBounds(srcOffset, byteCount);
        checkBounds(dstOffset, byteCount);
        nativeCopyMemoryNT(((MemoryPoolImpl)srcPool).dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
    }

    @Override
    public void copyFromByteArrayNT(byte[] srcArray, int srcIndex, long dstOffset, int byteCount) {
        if (srcIndex < 0 || srcIndex + byteCount > srcArray.length) {
            throw new IndexOutOfBoundsException(indexOutOfBoundsMessage(srcIndex, byteCount));
        }
        checkBounds(dstOffset, byteCount);
        nativeCopyFromByteArrayNT(srcArray, srcIndex, dataAddress(dstOffset), byteCount);
    }

    @Override
    public void setMemoryNT(byte value, long offset, long byteCount) {
        checkBounds(offset, byteCount);
        nativeSetMemoryNT(dataAddress(offset), byteCount, value);
    }

    @Override
    public void flush(long offset, long byteCount) {
        checkBounds(offset, byteCount);
        nativeFlush(dataAddress(offset), byteCount);
    }

    @Override
    public int hashCode() {
        return poolPath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MemoryPool)) return false;
        MemoryPoolImpl other = (MemoryPoolImpl)obj;
        return this.poolPath.equals(other.poolPath);
    }

    static String indexOutOfBoundsMessage(long index, long length)
    {
        if (index < 0) return "negative index: " + index;
        if (length < 0) return "negative length: " + length;
        return String.format("index + length is out of bounds: %s + %s", index, length);
    }

    private static native long nativeOpenPool(String path, long byteCount);
    private static native int nativeClosePool(long poolAddress, long byteCount);
    private static native void nativeFlush(long offset, long byteCount);
    private static native long nativePoolSize(String path);
    private static native void nativeCopyMemoryNT(long srcOffset, long dstOffset, long byteCount);
    private static native void nativeCopyFromByteArrayNT(byte[] srcArray, int srcIndex, long dst, int byteCount);
    private static native void nativeSetMemoryNT(long offset, long length, byte value);
}
