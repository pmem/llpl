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
        Assert.assertEquals(value, mb.getByte(offset));
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
        Assert.assertEquals(value, mb.getByte(offset));
    }

}
