/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.print.DocFlavor.BYTE_ARRAY;

import java.util.Iterator;

class FlushableMemoryBlock extends MemoryBlock<Flushable> {
    private static final long FLUSH_FLAG_OFFSET = 8;    // store the flushed flag as an int at the 8th offset, real data starts at offset 12
    private static final long FLUSH_FLAG_LENGTH = 4;    // flushed flag is an int
    private static final long METADATA_SIZE = 12;
    private static final int FLUSHED = 0;               // need to maintain 0 for default (flushed)
    private static final int DIRTY = 1;                 // so "flushed" flag is really "dirty" flag
    private static final int FLUSH_GRANULARITY = 64;    // flushing is aligned to 64 byte cache lines

    private ConcurrentHashMap<Long, Long> addressRanges;

    FlushableMemoryBlock(Heap heap, long size) {
        super(heap, size);
        this.addressRanges = new ConcurrentHashMap<Long, Long>();
    }

    FlushableMemoryBlock(long poolAddress, long offset) {
        super(poolAddress, offset);
        this.addressRanges = new ConcurrentHashMap<Long, Long>();
    }

    private void addToMemoryRanges(long offset, long size) {
        if (addressRanges == null) addressRanges = new ConcurrentHashMap<Long, Long>();
        long start = directAddress() + offset;
        long end = directAddress() + offset + size - 1;
        long startFlushAddr = (start / FLUSH_GRANULARITY) * FLUSH_GRANULARITY;
        long endFlushAddr = (end / FLUSH_GRANULARITY) * FLUSH_GRANULARITY;
        addressRanges.put(startFlushAddr, 0L);
        if (startFlushAddr != endFlushAddr) addressRanges.put(endFlushAddr, 0L);
    }

    private void markDirty() {
        if (isFlushed()) {
            long address = directAddress() + FLUSH_FLAG_OFFSET;
            setAbsoluteInt(address, DIRTY);
            flushAbsolute(address, FLUSH_FLAG_LENGTH);
        }
    }

    private void markFlushed() {
        long address = directAddress() + FLUSH_FLAG_OFFSET;
        setAbsoluteInt(address, FLUSHED);
        flushAbsolute(address, FLUSH_FLAG_LENGTH);
    } 

    @Override
    public boolean isFlushed() {
        return getAbsoluteInt(directAddress() + FLUSH_FLAG_OFFSET) == FLUSHED ? true : false;
    }

    @Override
    public void flush() {
        if (addressRanges == null) {
            addressRanges = new ConcurrentHashMap<Long, Long>();
        }
        addressRanges.forEach((Long address, Long nulls) -> {
            nativeFlush(address, FLUSH_GRANULARITY);
        });
        addressRanges = new ConcurrentHashMap<Long, Long>();
        markFlushed();
    }

    @Override
    public void setByte(long offset, byte value) {
        markDirty();
        setRawLong(offset, value);
        addToMemoryRanges(offset, Byte.BYTES);
    }

    @Override
    public void setShort(long offset, short value) {
        markDirty();
        setRawLong(offset, value);
        addToMemoryRanges(offset, Short.BYTES);
    }

    @Override
    public void setInt(long offset, int value) {
        markDirty();
        setRawLong(offset, value);
        addToMemoryRanges(offset, Integer.BYTES);
    }

    @Override
    public void setLong(long offset, long value) {
        markDirty();
        setRawLong(offset, value);
        addToMemoryRanges(offset, Long.BYTES);
    }

    @Override
    public void copyFromMemory(MemoryBlock<?> srcBlock, long srcOffset, long dstOffset, long length) {
        markDirty();
        nativeMemoryBlockMemcpyRaw(srcBlock.address(), srcBlock.baseOffset() + srcOffset, address(), baseOffset() + dstOffset, length);
        addToMemoryRanges(dstOffset, length);
    }

    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        markDirty();
        nativeFromByteArrayMemcpyRaw(srcArray, srcOffset, address(), baseOffset() + dstOffset, length);
        addToMemoryRanges(dstOffset, length);
    }

    @Override
    public void setMemory(byte val, long offset, long length) {
        markDirty();
        nativeMemoryBlockMemsetRaw(address(), baseOffset() + offset, val, length);
        addToMemoryRanges(offset, length);
    }

    @Override
    long baseOffset() {
        return METADATA_SIZE; 
    }
}
