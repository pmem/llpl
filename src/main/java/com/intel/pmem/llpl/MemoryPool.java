/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

// NOTE: this code is [EXPERIMENTAL] 

package com.intel.pmem.llpl;
import java.nio.ByteBuffer;

/**
 * [EXPERIMENTAL] Defines a read and write interface for accessing a pool of persistent memory.
 * A pool is opened by specifying a path and a size to the {@code openPool} method.  Multiple 
 * calls to {@code openPool} using the same path (whether the calls are within a process or 
 * across multiple processes) will result in shared access to the same physical memory.<br><br> 
 *
 * MemoryPool instances stand alone and are not intended to interact with any other 
 * LLPL objects such as heaps or memory accessors. Currently, no bounds checking of 
 * offsets is done and crashes of the JVM may result if invalid offsets (e.g. less than 0 
 * or greater than pool size) are used.<br><br>
 * 
 *   
 * A simple example of use is show below:
 <pre>

  import com.intel.pmem.llpl.MemoryPool;
	
  MemoryPool pool = MemoryPool.openPool("/mnt/mem/pool1", 100_000_000);

  pool.setLong(0, 12345); // write long at offset 0
  pool.setLong(8, 234);   // write long at offset 8
  pool.setInt(16, 234);   // write int at offset 16
  pool.flush(0, 20);      // flush above writes (20 bytes) for persistence;

  pool.close();
 </pre>
 */

public interface MemoryPool {
	public static MemoryPool createPool(String path, long byteCount) {
		return new MemoryPoolImpl(path, byteCount);
	}

	public static MemoryPool openPool(String path) {
		return new MemoryPoolImpl(path);
	}

	public void close();

	public byte getByte(long offset);
	public short getShort(long offset);
	public int getInt(long offset);
	public long getLong(long offset);
	
	public void setByte(long offset, byte value);
	public void setShort(long offset, short value);
	public void setInt(long offset, int value);
	public void setLong(long offset, long value);
	
	public void copyMemory(long srcOffset, long dstOffset, long byteCount);
	public void copyFromArray(byte[] srcArray, int srcIndex, long dstOffset, int byteCount);
	public void copyToArray(long srcOffset, byte[] dstArray, int dstIndex, int byteCount);
	public void setMemory(long offset, long byteCount, byte value);

	public void copyMemoryNT(long srcOffset, long dstOffset, long byteCount);
	public void copyFromArrayNT(byte[] srcArray, long dstOffset, int byteCount);
	public void copyFromShortArrayNT(short[] srcArray, long dstOffset, int elementCount);
	public void copyFromIntArrayNT(int[] srcArray, long dstOffset, int elementCount);
	public void copyFromLongArrayNT(long[] srcArray, long dstOffset, int elementCount);
	public void setMemoryNT(long offset, long byteCount, int value);

	public long size();

	public void flush(long offset, long byteCount);
}
