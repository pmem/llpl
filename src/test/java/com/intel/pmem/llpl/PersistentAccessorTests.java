/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

@Test(singleThreaded = true)
public class PersistentAccessorTests {
    PersistentHeap heap = null;

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

    @Test
    public void testSetMemoryBlockHandle() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock[] blocks = new PersistentMemoryBlock[100];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = heap.allocateMemoryBlock(16, false);
            blocks[i].setLong(7, blocks[i].handle());
        }
        PersistentAccessor acc = heap.createAccessor();
        for (int i = 0; i < blocks.length; i++) {
            acc.handle(blocks[i].handle());
            Assert.assertEquals(acc.getLong(7), blocks[i].handle());
        }
    }

    @Test
    public void testSizeCompactAccessor() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(heap.allocateMemory(100));
        try {
            acc.size();
            Assert.fail("UnsupportedOperationException was not thrown");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testSetCompactMemoryBlockHandle() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock[] blocks = new PersistentCompactMemoryBlock[100];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = heap.allocateCompactMemoryBlock(16, false);
            blocks[i].setLong(7, blocks[i].handle());
        }
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        for (int i = 0; i < blocks.length; i++) {
            acc.handle(blocks[i].handle());
            Assert.assertEquals(acc.getLong(7), blocks[i].handle());
        }
    }

    @Test
    public void testSetAllocateMemory() {
        heap = TestVars.createPersistentHeap();
        long[] handles = new long[100];
        PersistentAccessor acc = heap.createAccessor();
        for (int i = 0; i < handles.length; i++) {
            handles[i] = heap.allocateMemory(16, false);
            acc.handle(handles[i]);
            acc.setLong(7, handles[i]);
        }
        PersistentMemoryBlock mb;
        for (int i = 0; i < handles.length; i++) {
            acc.handle(handles[i]);
            mb = heap.memoryBlockFromHandle(handles[i]);
            Assert.assertEquals(acc.getLong(7), mb.getLong(7));
        }
    }

    @Test
    public void testSetAllocateCompactMemory() {
        heap = TestVars.createPersistentHeap();
        long[] handles = new long[100];
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        for (int i = 0; i < handles.length; i++) {
            handles[i] = heap.allocateCompactMemory(16, false);
            acc.handle(handles[i]);
            acc.setLong(7, handles[i]);
        }
        PersistentCompactMemoryBlock mb;
        for (int i = 0; i < handles.length; i++) {
            acc.handle(handles[i]);
            mb = heap.compactMemoryBlockFromHandle(handles[i]);
            Assert.assertEquals(acc.getLong(7), mb.getLong(7));
        }
    }

    @Test
    public void testResetAccessor() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        PersistentAccessor acc = heap.createAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after reset
        acc.resetHandle();
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testResetCompactAccessor() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after reset
        acc.resetHandle();
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testAccessorFreeMemory() {
        heap = TestVars.createPersistentHeap();
        boolean transactional = true;
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        PersistentAccessor acc = heap.createAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after freeMemory
        acc.freeMemory(transactional);
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testAccessorFreeMemoryCompact() {
        heap = TestVars.createPersistentHeap();
        boolean transactional = true;
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after freeMemory
        acc.freeMemory(transactional);
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testAccessorWithRangeConsumer() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        long longData = 12345;
        int intData = 100;
        byte byteData = 1;
        long totalLength = Long.BYTES + Integer.BYTES + 1;
        acc.withRange(0, totalLength, (Range range) -> {
            range.setLong(0, longData);
            range.setInt(Long.BYTES, intData);
            range.setByte(Long.BYTES + Integer.BYTES, byteData);
        });
        Assert.assertEquals(mb.getLong(0), longData);
        Assert.assertEquals(mb.getInt(Long.BYTES), intData);
        Assert.assertEquals(mb.getByte(Long.BYTES + Integer.BYTES), byteData);
    }

    @Test
    public void testAccessorWithRangeConsumerCompact() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        long longData = 12345;
        int intData = 100;
        byte byteData = 1;
        long totalLength = Long.BYTES + Integer.BYTES + 1;
        acc.withRange(0, totalLength, (Range range) -> {
            range.setLong(0, longData);
            range.setInt(Long.BYTES, intData);
            range.setByte(Long.BYTES + Integer.BYTES, byteData);
        });
        Assert.assertEquals(mb.getLong(0), longData);
        Assert.assertEquals(mb.getInt(Long.BYTES), intData);
        Assert.assertEquals(mb.getByte(Long.BYTES + Integer.BYTES), byteData);
    }

    @Test
    public void testAccessorWithRangeFunction() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        long longData = 12345;
        int intData = 100;
        byte byteData = 1;
        long totalLength = Long.BYTES + Integer.BYTES + 1;
        PersistentCompactAccessor accInternal = acc.withRange(0, totalLength, (Range range) -> {
            range.setLong(0, longData);
            range.setInt(Long.BYTES, intData);
            range.setByte(Long.BYTES + Integer.BYTES, byteData);
            PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
            PersistentCompactAccessor accNew = heap.createCompactAccessor();
            accNew.handle(mbNew.handle());
            mbNew.setInt(12, 1234);
            return accNew;
        });
        Assert.assertEquals(mb.getLong(0), longData);
        Assert.assertEquals(mb.getInt(Long.BYTES), intData);
        Assert.assertEquals(mb.getByte(Long.BYTES + Integer.BYTES), byteData);
        Assert.assertEquals(accInternal.getInt(12), 1234);
    }

    @Test
    public void testAccessorWithRangeFunctionCompact() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        long longData = 12345;
        int intData = 100;
        byte byteData = 1;
        long totalLength = Long.BYTES + Integer.BYTES + 1;
        PersistentCompactAccessor accInternal = acc.withRange(0, totalLength, (Range range) -> {
            range.setLong(0, longData);
            range.setInt(Long.BYTES, intData);
            range.setByte(Long.BYTES + Integer.BYTES, byteData);
            PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
            PersistentCompactAccessor accNew = heap.createCompactAccessor();
            accNew.handle(mbNew.handle());
            mbNew.setInt(12, 1234);
            return accNew;
        });
        Assert.assertEquals(mb.getLong(0), longData);
        Assert.assertEquals(mb.getInt(Long.BYTES), intData);
        Assert.assertEquals(mb.getByte(Long.BYTES + Integer.BYTES), byteData);
        Assert.assertEquals(accInternal.getInt(12), 1234);
    }

    @Test
    public void testAccessorWithRangeImplicit() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        long longData = 12345;
        int intData = 100;
        byte byteData = 1;
        acc.withRange((Range range) -> {
            range.setLong(0, longData);
            range.setInt(Long.BYTES, intData);
            range.setByte(Long.BYTES + Integer.BYTES, byteData);
        });
        Assert.assertEquals(mb.getLong(0), longData);
        Assert.assertEquals(mb.getInt(Long.BYTES), intData);
        Assert.assertEquals(mb.getByte(Long.BYTES + Integer.BYTES), byteData);
    }

    @Test
    public void testTxWithRangeFunctionValid() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        PersistentAccessor accInternal = acc.transactionalWithRange(0, 1024, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
            PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
            PersistentAccessor accNew = heap.createAccessor();
            accNew.handle(mbNew.handle());
            mbNew.setInt(12, 1234);
            return accNew;
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
        Assert.assertEquals(mb.getLong(7), 3456);
        Assert.assertEquals(accInternal.getInt(12), 1234);
    }

    @Test
    public void testTxWithRangeFunctionValidCompact() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        PersistentCompactAccessor accInternal = acc.transactionalWithRange(0, 1024, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
            PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
            PersistentCompactAccessor accNew = heap.createCompactAccessor();
            accNew.handle(mbNew.handle());
            mbNew.setInt(12, 1234);
            return accNew;
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
        Assert.assertEquals(mb.getLong(7), 3456);
        Assert.assertEquals(accInternal.getInt(12), 1234);
    }

    @Test
    public void testTxWithRangeConsumer() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        acc.transactionalWithRange(0, 1024, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
            PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
            mbNew.setInt(12, 1234);
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
        Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testTxWithRangeConsumerCompact() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        acc.transactionalWithRange(0, 1024, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
            PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
            mbNew.setInt(12, 1234);
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
        Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testTxWithRangeFunctionImplicitBounds() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        acc.transactionalWithRange((Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
        Assert.assertEquals(mb.getLong(7), 3456);
    }

    // PersistentAccessor.freeMemory()
    @Test
    public void testPersistentAccessorFreeMemory(){
        heap = TestVars.createPersistentHeap();
        // allocate memory
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        // assert valid
        Assert.assertTrue(acc.isValid());
        // acc.freeMemory()
        acc.freeMemory();
        Assert.assertFalse(acc.isValid());
    }

    // PersistentCompactAccessor.freeMemory()
    @Test
    public void testPersistentCompactAccessorFreeMemory(){
        heap = TestVars.createPersistentHeap();
        // allocate memory
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        // assert valid
        Assert.assertTrue(acc.isValid());
        // acc.freeMemory()
        acc.freeMemory();
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testSetMemory(){
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        byte value = 100;
        long offset = 0;
        long length = 1;
        acc.setMemory(value, offset, length);
        Assert.assertEquals(mb.getByte(offset), value);
    }

    @Test
    public void testSetMemoryCompact(){
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        byte value = 100;
        long offset = 0;
        long length = 1;
        acc.setMemory(value, offset, length);
        Assert.assertEquals(mb.getByte(offset), value);
    }

    @Test
    public void testCopyFromCompactAccessorToCompactLargeLength() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        long handle = heap.allocateCompactMemory(1024, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(handle);
        acc.copyFrom(mb, 0, 0, 1030);
    }

    @Test
    public void testCopyFromCompactAccessorToCompactVeryLargeLength() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mbNew.handle());
        try {
            acc.copyFrom(mb, 0, 0, TestVars.TOTAL_SIZE);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        }
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testCopyFromCompactAccessorVeryLargeLength() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mbNew.handle());
        try {
            acc.copyFrom(mb, 0, 0, TestVars.TOTAL_SIZE);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        }
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testCopyFromMemoryBlockNull() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mbNew.handle());
        try {
            acc.copyFrom(null, 0, 0, 1024);
            Assert.fail("NullPointerException wasn't thrown");
        }
        catch (NullPointerException e) {
            assert true;
        }
    }

    @Test
    public void testCopyFromMemBlkToCompactAccessorNull() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mbNew.handle());
        try {
            acc.copyFrom(null, 0, 0, 1024);
            Assert.fail("NullPointerException wasn't thrown");
        }
        catch (NullPointerException e) {
            assert true;
        }
    }

    @Test
    public void testAccessorWriteByte() {
        heap = TestVars.createPersistentHeap();
        PersistentAccessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        byte data = 42;
        acc.handle(handle);
        acc.setByte(offset,data);
        Assert.assertEquals(acc.getByte(offset), data);
    }

    @Test
    public void testAccessorWriteByteCompact() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        byte data = 42;
        acc.handle(handle);
        acc.setByte(offset,data);
        Assert.assertEquals(acc.getByte(offset), data);
    }

    @Test
    public void testAccessorWriteShort() {
        heap = TestVars.createPersistentHeap();
        PersistentAccessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        short data = 42;
        acc.handle(handle);
        acc.setShort(offset,data);
        Assert.assertEquals(acc.getShort(offset), data);
    }

    @Test
    public void testAccessorWriteShortCompact() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        short data = 42;
        acc.handle(handle);
        acc.setShort(offset,data);
        Assert.assertEquals(acc.getShort(offset), data);
    }

    @Test
    public void testAccessorWriteInt() {
        heap = TestVars.createPersistentHeap();
        PersistentAccessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        int data = 42;
        acc.handle(handle);
        acc.setInt(offset,data);
        Assert.assertEquals(acc.getInt(offset), data);
    }

    @Test
    public void testAccessorWriteIntCompact() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        int data = 42;
        acc.handle(handle);
        acc.setInt(offset,data);
        Assert.assertEquals(acc.getInt(offset), data);
    }

    @Test
    public void testAccessorWriteLong() {
        heap = TestVars.createPersistentHeap();
        PersistentAccessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 1;
        long data = 42;
        acc.handle(handle);
        acc.setLong(offset,data);
        Assert.assertEquals(acc.getLong(offset), data);
    }

    @Test
    public void testAccessorWriteLongCompact() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        long data = 42;
        acc.handle(handle);
        acc.setLong(offset,data);
        Assert.assertEquals(acc.getLong(offset), data);
    }

    @Test
    public void testCopyFromArray() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        byte[] arr = new byte[1024];
        long len = 0 + 1024;
        for(int i = 0; i < len; i++)
            arr[i] = (byte)-1;
        acc.copyFromArray(arr, 0, 0, 1024);
        len = 0 + 1024;
        for(int i = 0; i < len; i++)
            Assert.assertEquals(acc.getByte(i), (byte)-1);
    }

    @Test
    public void testCopyFromArraytoCompact() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        byte[] arr = new byte[1024];
        long len = 0 + 1024;
        for(int i = 0; i < len; i++)
            arr[i] = (byte)-1;
        acc.copyFromArray(arr, 0, 0, 1024);
        len = 0 + 1024;
        for(int i = 0; i < len; i++)
            Assert.assertEquals(acc.getByte(i), (byte)-1);
    }

    @Test
    public void testCopyFromMemoryBlock() {
        heap = TestVars.createPersistentHeap();
        int numBytes = 256;
        PersistentMemoryBlock mbSrc = heap.allocateMemoryBlock(numBytes, true);
        PersistentMemoryBlock mbDst = heap.allocateMemoryBlock(numBytes, true);
        PersistentAccessor acc = heap.createAccessor();
        acc.handle(mbDst.handle());
        for(int i = 0; i < numBytes; i++)
            mbSrc.setByte(i,(byte)i);
        acc.copyFrom(mbSrc, 0, 0, numBytes);
        for(int i = 0; i < numBytes; i++)
            Assert.assertEquals(mbSrc.getByte(i), mbDst.getByte(i));
    }

    @Test
    public void testAccessorFailArrayBoundsCheckingNegativeLength() {
        heap = TestVars.createPersistentHeap();
        int size = 1024;
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(size, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        byte[] arr = new byte[size];
        int offset = size;
        int len = -1;
        for(int i = 0; i < len; i++)
            arr[i] = (byte)-1;
        try {
            acc.copyFromArray(arr, offset, 0, len);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAccessorFailArrayBoundsCheckingNegativeOffset() {
        heap = TestVars.createPersistentHeap();
        int size = 1024;
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(size, true);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        byte[] arr = new byte[size];
        int offset = -1;
        int len = size;
        for(int i = 0; i < len; i++)
            arr[i] = (byte)-1;
        try {
            acc.copyFromArray(arr, 0, (long)offset, len);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAccessorFailBoundsCheckingNegativeOffset(){
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        long value = 100;
        long offset = -1;
        try {
            acc.setLong(offset, value);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAccessorFailBoundsCheckingLargeOffset(){
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        PersistentCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        long value = 100;
        long offset = heap.size() + 1024;
        try {
            acc.setLong(offset, value);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testMemoryFailBoundsCheckingNegativeOffset(){
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        long value = 100;
        long offset = -1;
        try {
            mb.setLong(offset, value);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testMemoryFailBoundsCheckingLargeOffset(){
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        long value = 100;
        long offset = heap.size() + 1024;
        try {
            mb.setLong(offset, value);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

}
