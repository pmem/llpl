/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

// NOTE: this code is [EXPERIMENTAL] 

package com.intel.pmem.llpl;
import java.nio.ByteBuffer;
import java.io.File;

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

 </pre>
 */

public interface MemoryPool {

    /**
     * Creates a new pool.
     * @param path a path to the new pool
     * @param byteCount the number of bytes to provision for the pool
     * @return the pool at the specified path
     * @throws MemoryPoolException if the pool could not be created
     */
	public static MemoryPool createPool(String path, long byteCount) {
		return new MemoryPoolImpl(path, byteCount);
	}

    /**
     * Opens an existing pool. Provides access to the pool associated with the specified {@code path}.
     * @param path the path to the pool
     * @return the pool at the specified path
     * @throws MemoryPoolException if the pool could not be opened
     */
	public static MemoryPool openPool(String path) {
		return new MemoryPoolImpl(path);
	}

    /**
    * Tests for the existence of a pool associated with the given path.
    * @param path the path to the pool
    * @return true if the pool exists
    * @throws MemoryPoolException if the pool is on a DAX device and its status could not be determined 
    */
    public static boolean exists(String path) {
        if (path.startsWith("/dev/dax")) {
            try {
                int flag = AnyHeap.nativeHeapExists(path);
                return (flag == 1) ? true : false;
            } catch(Exception e) {
                throw new MemoryPoolException("Unable to determine status of pool at \"" + path + "\"");
            }
        }
        File file = new File(path);
        return (file.exists() && file.isFile());
    }

    /**
     * Retrieves the {@code byte} value at {@code offset} within this pool's memory.
     * @param offset the location from which to retrieve data
     * @return the {@code byte} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool 
     * bounds
     */
	public byte getByte(long offset);

    /**
     * Retrieves the {@code short} value at {@code offset} within this pool's memory.
     * @param offset the location from which to retrieve data
     * @return the {@code short} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool 
     * bounds
     */
	public short getShort(long offset);

    /**
     * Retrieves the {@code int} value at {@code offset} within this pool's memory.
     * @param offset the location from which to retrieve data
     * @return the {@code int} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool 
     * bounds
     */
	public int getInt(long offset);

    /**
     * Retrieves the {@code long} value at {@code offset} within this pool's memory.
     * @param offset the location from which to retrieve data
     * @return the {@code long} value stored at {@code offset}
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool 
     * bounds
     */
	public long getLong(long offset);

	/**
     * Stores the supplied {@code byte} value at {@code offset} within this pool's memory.
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool bounds
     */
	public void setByte(long offset, byte value);

	/**
     * Stores the supplied {@code short} value at {@code offset} within this pool's memory.
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool bounds
     */
	public void setShort(long offset, short value);

	/**
     * Stores the supplied {@code int} value at {@code offset} within this pool's memory.
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool bounds
     */
	public void setInt(long offset, int value);

	/**
     * Stores the supplied {@code long} value at {@code offset} within this pool's memory.
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool bounds
     */
	public void setLong(long offset, long value);
	
    /**
     * Copies {@code byteCount} bytes from this pool, starting at {@code srcOffset}, to
     * this pool's memory starting at {@code dstOffset}.
     * @param srcOffset the starting offset in the pool's memory
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of pool bounds
     */
	public void copyFromPool(long srcOffset, long dstOffset, long byteCount);

    /**
     * Copies {@code byteCount} bytes from the specified {@code srcPool}, starting at {@code srcOffset}, to
     * this pool's memory starting at {@code dstOffset}.
     * @param srcPool the pool from which to copy bytes
     * @param srcOffset the starting offset in the source pool's memory
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of pool bounds
     */
	public void copyFromPool(MemoryPool srcPool, long srcOffset, long dstOffset, long byteCount);

    /**
     * Copies {@code byteCount} bytes from {@code srcArray}, starting at {@code srcIndex}, to
     * this pool's memory starting at {@code dstOffset}.
     * @param srcArray the array from which to copy bytes
     * @param srcIndex the starting index in the source array
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of array bounds or
     * outside of pool bounds
     */
	public void copyFromByteArray(byte[] srcArray, int srcIndex, long dstOffset, int byteCount);

    /**
     * Copies {@code byteCount} bytes from this pool's memory, starting at {@code srcOffset}, to the
     * {@code dstArray} byte array starting at array index {@code dstOffset}.
     * @param srcOffset the starting offset in this pool's memory
     * @param dstArray the destination byte array
     * @param dstIndex the starting offset in the destination array
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of array bounds or
     * outside of pool bounds
     */
	public void copyToByteArray(long srcOffset, byte[] dstArray, int dstIndex, int byteCount);

    /**
     * Sets {@code byteCount} bytes in this pool's memory, starting at {@code offset}, to the supplied {@code byte}
     * value.
     * @param value the value to set
     * @param offset the starting offset in this pool's memory
     * @param byteCount the number of bytes to set
     * @throws IndexOutOfBoundsException if setting would cause access of data outside of pool bounds
     */
	public void setMemory(byte value, long offset, long byteCount);

    /**
     * Copies {@code byteCount} bytes from this pool in a non-temporal way, starting at {@code srcOffset}, to
     * this pool's memory starting at {@code dstOffset}.
     * @param srcOffset the starting offset in the pool's memory
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of pool bounds
     */
	public void copyFromPoolNT(long srcOffset, long dstOffset, long byteCount);

    /**
     * Copies {@code byteCount} bytes from the specified {@code srcPool} in a non-temporal way, starting at {@code srcOffset}, to
     * this pool's memory starting at {@code dstOffset}.
     * @param srcPool the pool from which to copy bytes
     * @param srcOffset the starting offset in the source pool's memory
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of pool bounds
     */
	public void copyFromPoolNT(MemoryPool srcPool, long srcOffset, long dstOffset, long byteCount);

    /**
     * Copies {@code byteCount} bytes from {@code srcArray} in a non-temporal way, starting at {@code srcIndex}, to
     * this pool's memory starting at {@code dstOffset}.
     * @param srcArray the array from which to copy bytes
     * @param srcIndex the starting index in the source array
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of array bounds or
     * outside of pool bounds
     */
	public void copyFromByteArrayNT(byte[] srcArray, int srcIndex, long dstOffset, int byteCount);

    /**
     * Sets {@code byteCount} bytes in this pool's memory in a non-temporal way, starting at {@code offset}, to the supplied {@code byte}
     * value.
     * @param value the value to set
     * @param offset the starting offset in this pool's memory
     * @param byteCount the number of bytes to set
     * @throws IndexOutOfBoundsException if setting would cause access of data outside of pool bounds
     */
	public void setMemoryNT(byte value, long offset, long byteCount);

    /**
     * Returns the provisioned size, in bytes, of this memory pool.
     * @return the provisioned size, in bytes, of this memory pool 
     */
	public long size();

    /**
    * Ensures that the supplied range of bytes within this pool are written to persistent memory media.
    * @param offset the starting location from which to flush bytes
    * @param byteCount the number of bytes to flush
    * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool bounds
    */
	public void flush(long offset, long byteCount);
}
