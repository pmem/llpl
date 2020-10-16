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
        System.loadLibrary("llpl");
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
		return UNSAFE.getByte(dataAddress(offset));
	}

	@Override
	public short getShort(long offset) {
		return UNSAFE.getShort(dataAddress(offset));
	}

	@Override
	public int getInt(long offset){
		return UNSAFE.getInt(dataAddress(offset));
	}
	
	@Override
	public long getLong(long offset){
		return UNSAFE.getLong(dataAddress(offset));
	}
	
	@Override
	public void setByte(long offset, byte value){
		UNSAFE.putByte(dataAddress(offset), value);
	}
	
	@Override
	public void setShort(long offset, short value){
		UNSAFE.putShort(dataAddress(offset), value);
	}
	
	@Override
	public void setInt(long offset, int value){
		UNSAFE.putInt(dataAddress(offset), value);
	}
	
	@Override
	public void setLong(long offset, long value){
		UNSAFE.putLong(dataAddress(offset), value);
	}
	
	@Override
	public void copyMemory(long srcOffset, long dstOffset, long byteCount) {
		UNSAFE.copyMemory(dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
	}

	@Override
	public void copyFromArray(byte[] srcArray, int srcIndex, long dstOffset, int byteCount) {
        long srcAddress = UNSAFE.ARRAY_BYTE_BASE_OFFSET + UNSAFE.ARRAY_BYTE_INDEX_SCALE * srcIndex;
        UNSAFE.copyMemory(srcArray, srcAddress, null, dataAddress(dstOffset), byteCount);
	}

	@Override
	public void copyToArray(long srcOffset, byte[] dstArray, int dstIndex, int byteCount) {
	    long dstAddress = UNSAFE.ARRAY_BYTE_BASE_OFFSET + UNSAFE.ARRAY_BYTE_INDEX_SCALE * dstIndex;
        UNSAFE.copyMemory(null, dataAddress(srcOffset), dstArray, dstAddress, byteCount);
	}

	@Override
	public void setMemory(long offset, long byteCount, byte value) {
        UNSAFE.setMemory(dataAddress(offset), byteCount, value); 
	}

	@Override
	public void copyMemoryNT(long srcOffset, long dstOffset, long byteCount) {
		nativeCopyMemoryNT(dataAddress(srcOffset), dataAddress(dstOffset), byteCount);
	}

	@Override
	public void copyFromArrayNT(byte[] srcArray, long dstOffset, int byteCount) {
		nativeCopyFromArrayNT(srcArray, dataAddress(dstOffset), byteCount);
	}

	@Override
	public void copyFromShortArrayNT(short[] srcArray, long dstOffset, int elementCount) {
		nativeCopyFromShortArrayNT(srcArray, dataAddress(dstOffset), Short.BYTES * elementCount);
	}

	@Override
	public void copyFromIntArrayNT(int[] srcArray, long dstOffset, int elementCount) {
		nativeCopyFromIntArrayNT(srcArray, dataAddress(dstOffset), Integer.BYTES * elementCount);
	}

	@Override
	public void copyFromLongArrayNT(long[] srcArray, long dstOffset, int elementCount) {
		nativeCopyFromLongArrayNT(srcArray, dataAddress(dstOffset), Long.BYTES * elementCount);
	}

	@Override
	public void setMemoryNT(long offset, long byteCount, int value) {
        nativeSetMemoryNT(dataAddress(offset), byteCount, value); 
	}

	@Override
	public void flush(long offset, long byteCount) {
		nativeFlush(dataAddress(offset), byteCount);
	}

	private static native long nativeOpenPool(String path, long byteCount);
	private static native int nativeClosePool(long poolAddress, long byteCount);
	private static native void nativeFlush(long offset, long byteCount);
	private static native long nativePoolSize(String path);
	private static native void nativeCopyMemoryNT(long srcOffset, long dstOffset, long byteCount);
	private static native void nativeCopyFromArrayNT(byte[] srcArray, long dst, int byteCount);
	private static native void nativeCopyFromShortArrayNT(short[] srcArray, long dst, int Count);
	private static native void nativeCopyFromIntArrayNT(int[] srcArray, long dst, int byteCount);
	private static native void nativeCopyFromLongArrayNT(long[] srcArray, long dst, int byteCount);
    private static native void nativeSetMemoryNT(long offset, long length, int value);
}
