/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Random;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

@Test(singleThreaded = true)
public class RangeTests {
    Heap heap = null;

    @BeforeMethod
    public void initialize() {
        heap = null;
    }

    @SuppressWarnings("deprecation")
    @AfterMethod
    public void testCleanup() {
        if (heap != null)
            heap.close();

        if (TestVars.ISDAX) {
            TestVars.daxCleanUp();
        }
        else TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
    }

    // Range.isValid()
    // Range.startOffset()
    // Range.rangeLength()
    @Test
    public void testRangeisValid(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        acc.withRange(offset, length, (Range range) -> {
            Assert.assertTrue(range.isValid());
            Assert.assertEquals(offset, range.startOffset());
            Assert.assertEquals(length, range.rangeLength());
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
    }

    // Range.copyFromMemoryBlock()
    @Test
    public void testRangeCopy(){
        heap = TestVars.createHeap();
        MemoryBlock mb1 = heap.allocateMemoryBlock(1024);
        MemoryBlock mb2 = heap.allocateMemoryBlock(1024);
        Accessor acc = heap.createAccessor();
        acc.handle(mb1.handle());
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        mb2.setInt(0, 54321);
        acc.withRange(offset, length, (Range range) -> {
            range.setByte(0, (byte)1);
            range.copyFromMemoryBlock(mb2, 0, 1, 4);
            range.setShort(5, (short)2345);
        });
        Assert.assertEquals(mb1.getByte(0), (byte)1);
        Assert.assertEquals(mb1.getInt(1), 54321);
        Assert.assertEquals(mb1.getShort(5), (short)2345);
    }

    @Test
    public void testCopyFromBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).limit(200).reset();
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(buf, 100);
        });
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromROBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).limit(200).reset();
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(buf.asReadOnlyBuffer(), 100);
        });
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromDirectBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        dbuf.position(100).mark();
        dbuf.put(arr, 0, arr.length).limit(200).reset();
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(dbuf, 100);
        });
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromRODirectBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        dbuf.position(100).mark();
        dbuf.put(arr, 0, arr.length).limit(200).reset();
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(dbuf.asReadOnlyBuffer(), 100);
        });
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromBBCompact() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).limit(200).reset();
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(buf, 100);
        });
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromROBBCompact() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).limit(200).reset();
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(buf.asReadOnlyBuffer(), 100);
        });
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromDirectBBCompact() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        dbuf.position(100).mark();
        dbuf.put(arr, 0, arr.length).limit(200).reset();
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(dbuf, 100);
        });
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromRODirectBBCompact() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        dbuf.position(100).mark();
        dbuf.put(arr, 0, arr.length).limit(200).reset();
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(dbuf.asReadOnlyBuffer(), 100);
        });
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromEmptyRangeBB() {
    	heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100);
        buf.put(arr, 0, arr.length).position(1024);
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(buf, 100);
        });
    }

    @Test
    public void testCopyFromEmptyRangeDirectBB() {
    	heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100);
        buf.put(arr, 0, arr.length).position(1024);
        mb.withRange(100, 100, (Range range) -> {
            range.copyFromByteBuffer(buf, 100);
        });
    }

    @Test
    public void testCopyFromBBInvalidMB() {
    	heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).limit(200).reset();
        try {
            mb.withRange(1000, 100, (Range range) -> {
                range.copyFromByteBuffer(buf, 1000);
            });
            Assert.fail();
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCopyFromDirectBBInvalidMB() {
    	heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).limit(200).reset();
        try {
            mb.withRange(1000, 100, (Range range) -> {
                range.copyFromByteBuffer(buf, 1000);
            });
            Assert.fail();
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCopyFromBBInvalidCMB() {
    	heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).limit(200).reset();
        try {
            mb.withRange(100, 100, (Range range) -> {
                range.copyFromByteBuffer(buf, -100);
            });
            Assert.fail();
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCopyFromDirectBBInvalidCMB() {
    	heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).limit(200).reset();
        try {
            mb.withRange(100, 100, (Range range) -> {
                range.copyFromByteBuffer(buf, -100);
            });
            Assert.fail();
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }
}
