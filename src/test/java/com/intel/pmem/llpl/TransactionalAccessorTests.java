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
public class TransactionalAccessorTests {
    TransactionalHeap heap = null;

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
        heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock[] blocks = new TransactionalMemoryBlock[100];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = heap.allocateMemoryBlock(16);
            blocks[i].setLong(7, blocks[i].handle());
        }
        TransactionalAccessor acc = heap.createAccessor();
        for (int i = 0; i < blocks.length; i++) {
            acc.handle(blocks[i].handle());
            Assert.assertEquals(acc.getLong(7), blocks[i].handle());
        }
    }

    @Test
    public void testSetCompactMemoryBlockHandle() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock[] blocks = new TransactionalCompactMemoryBlock[100];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = heap.allocateCompactMemoryBlock(16);
            blocks[i].setLong(7, blocks[i].handle());
        }
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        for (int i = 0; i < blocks.length; i++) {
            acc.handle(blocks[i].handle());
            Assert.assertEquals(acc.getLong(7), blocks[i].handle());
        }
    }

    @Test
    public void testSetAllocateMemory() {
        heap = TestVars.createTransactionalHeap();
        long[] handles = new long[100];
        TransactionalAccessor acc = heap.createAccessor();
        for (int i = 0; i < handles.length; i++) {
            handles[i] = heap.allocateMemory(16);
            acc.handle(handles[i]);
            acc.setLong(7, handles[i]);
        }
        TransactionalMemoryBlock mb;
        for (int i = 0; i < handles.length; i++) {
            acc.handle(handles[i]);
            mb = heap.memoryBlockFromHandle(handles[i]);
            Assert.assertEquals(acc.getLong(7), mb.getLong(7));
        }
    }

    @Test
    public void testSetAllocateCompactMemory() {
        heap = TestVars.createTransactionalHeap();
        long[] handles = new long[100];
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        for (int i = 0; i < handles.length; i++) {
            handles[i] = heap.allocateCompactMemory(16);
            acc.handle(handles[i]);
            acc.setLong(7, handles[i]);
        }
        TransactionalCompactMemoryBlock mb;
        for (int i = 0; i < handles.length; i++) {
            acc.handle(handles[i]);
            mb = heap.compactMemoryBlockFromHandle(handles[i]);
            Assert.assertEquals(acc.getLong(7), mb.getLong(7));
        }
    }

    @Test
    public void testResetAccessor() {
        heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        // Accessor is not valid immediately after instantiation
        TransactionalAccessor acc = heap.createAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        // Accessor is not valid immediately after instantiation
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        // Accessor is not valid immediately after instantiation
        TransactionalAccessor acc = heap.createAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after freeMemory
        acc.freeMemory();
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testAccessorFreeMemoryCompact() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        // Accessor is not valid immediately after instantiation
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after freeMemory
        acc.freeMemory();
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testTransactionalAccessorWithRangeConsumer() {
        heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        TransactionalAccessor acc = heap.createAccessor();
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
    public void testTransactionalAccessorWithRangeConsumerCompact() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        TransactionalAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        long longData = 12345;
        int intData = 100;
        byte byteData = 1;
        long totalLength = Long.BYTES + Integer.BYTES + 1;
        TransactionalAccessor accInternal = acc.withRange(0, totalLength, (Range range) -> {
            range.setLong(0, longData);
            range.setInt(Long.BYTES, intData);
            range.setByte(Long.BYTES + Integer.BYTES, byteData);
            TransactionalMemoryBlock mbNew = heap.allocateMemoryBlock(1024);
            TransactionalAccessor accNew = heap.createAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        long longData = 12345;
        int intData = 100;
        byte byteData = 1;
        long totalLength = Long.BYTES + Integer.BYTES + 1;
        TransactionalCompactAccessor accInternal = acc.withRange(0, totalLength, (Range range) -> {
            range.setLong(0, longData);
            range.setInt(Long.BYTES, intData);
            range.setByte(Long.BYTES + Integer.BYTES, byteData);
            TransactionalCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024);
            TransactionalCompactAccessor accNew = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        TransactionalAccessor acc = heap.createAccessor();
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
    public void testSetMemory(){
        heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        TransactionalAccessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        byte value = 100;
        long offset = 0;
        long length = 1;
        acc.setMemory(value, offset, length);
        Assert.assertEquals(value, mb.getByte(offset));
    }

    @Test
    public void testSetMemoryCompact(){
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        byte value = 100;
        long offset = 0;
        long length = 1;
        acc.setMemory(value, offset, length);
        Assert.assertEquals(value, mb.getByte(offset));
    }

}
