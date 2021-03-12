/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

// NOTE: this code is [EXPERIMENTAL] 

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
	private long poolSize;

	MemoryPoolImpl(String path, long byteCount) {
		if (byteCount < 0) throw new IllegalArgumentException("byteCount must be greater than or equal to zero");
        if (path.startsWith("/dev/dax") && byteCount != 0) throw new IllegalArgumentException("byteCount must be zero when using Device DAX");
		this.poolAddress = nativeOpenPool(path, byteCount);
		if (poolAddress == 0) throw new RuntimeException("Unable to create pool");
		this.poolSize = byteCount == 0 ? nativePoolSize(path) : byteCount;
	}

	MemoryPoolImpl(String path) {
        File file = new File(path);
        if (path.startsWith("/dev/dax")) this.poolSize = nativePoolSize(path);
        else if (file.exists() && file.isFile()) this.poolSize = file.length();
        else throw new IllegalArgumentException("path supplied must be an existing file");
		this.poolAddress = nativeOpenPool(path, this.poolSize);
		if (poolAddress == 0) throw new RuntimeException("Unable to create pool");
	}

	private void checkBounds(long offset, long byteCount) {
		if (offset < 0 || byteCount < 0 || offset + byteCount > poolSize) {
			throw new IndexOutOfBoundsException();
		}
	}

	private long dataAddress(long offset) {
		return poolAddress + offset;
	} 

	@Override
	public void close() {
		int result = nativeClosePool(poolAddress, poolSize);
		if (result == -1) throw new RuntimeException("Unable to close pool");
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
	public void copyMemory(long srcOffset, long dstOffset, long byteCount) {
        checkBounds(srcOffset, byteCount);
        checkBounds(dstOffset, byteCount);
		UNSAFE.copyMemory(dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
	}

	@Override
	public void copyFrom(MemoryPool pool, long srcOffset, long dstOffset, long byteCount) {
        ((MemoryPoolImpl)pool).checkBounds(srcOffset, byteCount);
        checkBounds(dstOffset, byteCount);
		UNSAFE.copyMemory(((MemoryPoolImpl)pool).dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
	}

	@Override
	public void copyFromByteArray(byte[] srcArray, int srcIndex, long dstOffset, int byteCount) {
        if (srcIndex < 0 || srcIndex + byteCount > srcArray.length) throw new IndexOutOfBoundsException();
        checkBounds(dstOffset, byteCount);
        long srcAddress = UNSAFE.ARRAY_BYTE_BASE_OFFSET + UNSAFE.ARRAY_BYTE_INDEX_SCALE * srcIndex;
        UNSAFE.copyMemory(srcArray, srcAddress, null, dataAddress(dstOffset), byteCount);
	}

	@Override
	public void copyToByteArray(long srcOffset, byte[] dstArray, int dstIndex, int byteCount) {
        if (dstIndex < 0 || dstIndex + byteCount > dstArray.length) throw new IndexOutOfBoundsException();
        checkBounds(srcOffset, byteCount);
	    long dstAddress = UNSAFE.ARRAY_BYTE_BASE_OFFSET + UNSAFE.ARRAY_BYTE_INDEX_SCALE * dstIndex;
        UNSAFE.copyMemory(null, dataAddress(srcOffset), dstArray, dstAddress, byteCount);
	}

	@Override
	public void setMemory(long offset, long byteCount, byte value) {
        checkBounds(offset, byteCount);
        UNSAFE.setMemory(dataAddress(offset), byteCount, value); 
	}

	@Override
	public void copyMemoryNT(long srcOffset, long dstOffset, long byteCount) {
        checkBounds(srcOffset, byteCount);
        checkBounds(dstOffset, byteCount);
		nativeCopyMemoryNT(dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
	}

	@Override
	public void copyFromNT(MemoryPool pool, long srcOffset, long dstOffset, long byteCount) {
        ((MemoryPoolImpl)pool).checkBounds(srcOffset, byteCount);
        checkBounds(dstOffset, byteCount);
		nativeCopyMemoryNT(((MemoryPoolImpl)pool).dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
	}

	@Override
	public void copyFromByteArrayNT(byte[] srcArray, int srcIndex, long dstOffset, int byteCount) {
        if (srcIndex < 0 || srcIndex + byteCount > srcArray.length) throw new IndexOutOfBoundsException();
        checkBounds(dstOffset, byteCount);
		nativeCopyFromByteArrayNT(srcArray, srcIndex, dataAddress(dstOffset), byteCount);
	}

	@Override
	public void setMemoryNT(long offset, long byteCount, byte value) {
        checkBounds(offset, byteCount);
        nativeSetMemoryNT(dataAddress(offset), byteCount, value); 
	}

	@Override
	public void flush(long offset, long byteCount) {
        checkBounds(offset, byteCount);
		nativeFlush(dataAddress(offset), byteCount);
	}

	private static native long nativeOpenPool(String path, long byteCount);
	private static native int nativeClosePool(long poolAddress, long byteCount);
	private static native void nativeFlush(long offset, long byteCount);
	private static native long nativePoolSize(String path);
	private static native void nativeCopyMemoryNT(long srcOffset, long dstOffset, long byteCount);
	private static native void nativeCopyFromByteArrayNT(byte[] srcArray, int srcIndex, long dst, int byteCount);
	private static native void nativeSetMemoryNT(long offset, long length, byte value);
}
