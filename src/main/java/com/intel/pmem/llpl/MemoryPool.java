/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Defines a read and write interface for accessing a pool of persistent memory.
 * A pool is created by calling the {@code createPool} method and specifying a path and a size.
 * A pool is opened by specifying the path of a previously-created pool to the {@code openPool} method.
 * Multiple calls to {@code openPool} using the same path, either within a process or
 * across multiple processes, will result in shared access to the same physical memory.<br><br>
 *
 * MemoryPool instances stand alone and are not intended to interact directly with any other
 * LLPL objects such as heaps or memory accessors.<br><br>
 *
 *
 * A simple example of use is shown below:
 <pre>

  import com.intel.pmem.llpl.MemoryPool;

  MemoryPool pool = MemoryPool.createPool("/mnt/mem/pool1", 100_000_000);

  pool.setLong(0, 12345); // write long at offset 0
  pool.setLong(8, 234);   // write long at offset 8
  pool.setInt(16, 234);   // write int at offset 16
  pool.flush(0, 20);      // flush above writes (20 bytes) for persistence;

 </pre>
 */

public interface MemoryPool {

    /**
     * Creates a new pool.
     * @param filePath a path to the new pool
     * @param byteCount the number of bytes to provision for the pool
     * @return a pool at the specified file path
     * @throws MemoryPoolException if the pool already exists or otherwise could not be created
     */
    public static MemoryPool createPool(String filePath, long byteCount) {
        if (filePath.startsWith("/dev/dax")) {
            throw new IllegalArgumentException("Device paths are not compatible with this method");
        }
        return new MemoryPoolImpl(filePath, byteCount);
    }

    /**
     * Opens an existing pool. Provides access to the pool associated with the specified {@code filePath}.
     * Multiple calls to {@code openPool} using the same path, either within a process or
     * across multiple processes, will result in shared access to the same physical memory.
     * @param filePath the path to the pool
     * @return the pool at the specified file path
     * @throws MemoryPoolException if the pool could not be opened
     */
    public static MemoryPool openPool(String filePath) {
        return new MemoryPoolImpl(filePath);
    }

    /**
     * Returns a MemoryPool that provides access to all of the memory of a specified Device DAX device.
     * Multiple calls to {@code mapDevice} using the same path, either within a process or
     * across multiple processes, will result in shared access to the same physical memory.
     * @param devicePath the path to the device
     * @return the pool at the specified device path
     * @throws MemoryPoolException if the pool could not be opened
     */
    public static MemoryPool mapDevice(String devicePath) {
        return new MemoryPoolImpl(devicePath);
    }

    /**
     * Tests for the existence of a pool associated with the specified {@code path}.
     * @param path the path to the pool
     * @return true if the pool exists
     */
    public static boolean exists(String path) {
        if (path.startsWith("/dev/dax")) {
            int flag = AnyHeap.nativeHeapExists(path);
            return (flag == -1) ? false : true;
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
     * Stores the specified {@code value} at {@code offset} within this pool's memory.
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool bounds
     */
    public void setByte(long offset, byte value);

    /**
     * Stores the specified {@code value} at {@code offset} within this pool's memory.
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool bounds
     */
    public void setShort(long offset, short value);

    /**
     * Stores the specified {@code value} at {@code offset} within this pool's memory.
     * @param offset the location at which to store the value
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool bounds
     */
    public void setInt(long offset, int value);

    /**
     * Stores the specified {@code value} at {@code offset} within this pool's memory.
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
     * {@code dstArray} byte array starting at array index {@code dstIndex}.
     * @param srcOffset the starting offset in this pool's memory
     * @param dstArray the destination byte array
     * @param dstIndex the starting offset in the destination array
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of array bounds or
     * outside of pool bounds
     */
    public void copyToByteArray(long srcOffset, byte[] dstArray, int dstIndex, int byteCount);

    /**
     * Sets {@code byteCount} bytes in this pool's memory, starting at {@code offset}, to the specified {@code value}.
     * @param value the value to set
     * @param offset the starting offset in this pool's memory
     * @param byteCount the number of bytes to set
     * @throws IndexOutOfBoundsException if setting would cause access of data outside of pool bounds
     */
    public void setMemory(byte value, long offset, long byteCount);

    /**
     * Copies {@code byteCount} bytes from this pool in a non-temporal way,
     * starting at {@code srcOffset}, to this pool's memory
     * starting at {@code dstOffset}.
     * @param srcOffset the starting offset in the pool's memory
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of pool bounds
     */
    public void copyFromPoolNT(long srcOffset, long dstOffset, long byteCount);

    /**
     * Copies {@code byteCount} bytes from the specified {@code srcPool} in a
     * non-temporal way, starting at {@code srcOffset}, to this pool's memory
     * starting at {@code dstOffset}.
     * @param srcPool the pool from which to copy bytes
     * @param srcOffset the starting offset in the source pool's memory
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of pool bounds
     */
    public void copyFromPoolNT(MemoryPool srcPool, long srcOffset, long dstOffset, long byteCount);

    /**
     * Copies {@code byteCount} bytes from {@code srcArray} in a non-temporal way,
     * starting at {@code srcIndex}, to this pool's memory starting at {@code dstOffset}.
     * @param srcArray the array from which to copy bytes
     * @param srcIndex the starting index in the source array
     * @param dstOffset the starting offset to which bytes are to be copied
     * @param byteCount the number of bytes to copy
     * @throws IndexOutOfBoundsException if copying would cause access of data outside of array bounds or
     * outside of pool bounds
     */
    public void copyFromByteArrayNT(byte[] srcArray, int srcIndex, long dstOffset, int byteCount);

    /**
     * Sets {@code byteCount} bytes in this pool's memory in a non-temporal way,
     * starting at {@code offset}, to the specified {@code value}.
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
    * Ensures that the specified range of bytes within this pool are written to persistent memory media.
    * @param offset the starting location from which to flush bytes
    * @param byteCount the number of bytes to flush
    * @throws IndexOutOfBoundsException if the operation would cause access of data outside of pool bounds
    */
    public void flush(long offset, long byteCount);

    /**
    * Returns a hash code for this memory pool.
    * Note that memory pool hash codes are computed based on the path specified when the pool was
    * created or opened.
    * @return a hash code for this memory pool
    */
    public int hashCode();

    /**
    * Compares this memory pool to the specified object.
    * The result is true if and only if the memory pool was created or opened using the same path
    * as the specified object.
    * @return true if the given object is a memory pool with the same path as this object.
    */
    public boolean equals(Object obj);
}
