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
public class AccessorTests {
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

    @Test
    public void testSetMemoryBlockHandle() {
        heap = TestVars.createHeap();
        MemoryBlock[] blocks = new MemoryBlock[100];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = heap.allocateMemoryBlock(16, false);
            blocks[i].setLong(7, blocks[i].handle());
        }
        Accessor acc = heap.createAccessor();
        for (int i = 0; i < blocks.length; i++) {
            acc.handle(blocks[i].handle());
            Assert.assertEquals(acc.getLong(7), blocks[i].handle());
        }
    }

    @Test
    public void testSizeCompactAccessor() {
        heap = TestVars.createHeap();
        CompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createHeap();
        CompactMemoryBlock[] blocks = new CompactMemoryBlock[100];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = heap.allocateCompactMemoryBlock(16, false);
            blocks[i].setLong(7, blocks[i].handle());
        }
        CompactAccessor acc = heap.createCompactAccessor();
        for (int i = 0; i < blocks.length; i++) {
            acc.handle(blocks[i].handle());
            Assert.assertEquals(acc.getLong(7), blocks[i].handle());
        }
    }

    @Test
    public void testSetAllocateMemory() {
        heap = TestVars.createHeap();
        long[] handles = new long[100];
        Accessor acc = heap.createAccessor();
        for (int i = 0; i < handles.length; i++) {
            handles[i] = heap.allocateMemory(16, false);
            acc.handle(handles[i]);
            acc.setLong(7, handles[i]);
        }
        MemoryBlock mb;
        for (int i = 0; i < handles.length; i++) {
            acc.handle(handles[i]);
            mb = heap.memoryBlockFromHandle(handles[i]);
            Assert.assertEquals(acc.getLong(7), mb.getLong(7));
        }
    }

    @Test
    public void testSetAllocateCompactMemory() {
        heap = TestVars.createHeap();
        long[] handles = new long[100];
        CompactAccessor acc = heap.createCompactAccessor();
        for (int i = 0; i < handles.length; i++) {
            handles[i] = heap.allocateCompactMemory(16, false);
            acc.handle(handles[i]);
            acc.setLong(7, handles[i]);
        }
        CompactMemoryBlock mb;
        for (int i = 0; i < handles.length; i++) {
            acc.handle(handles[i]);
            mb = heap.compactMemoryBlockFromHandle(handles[i]);
            Assert.assertEquals(acc.getLong(7), mb.getLong(7));
        }
    }

    @Test
    public void testResetAccessor() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        Accessor acc = heap.createAccessor();
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
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        CompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createHeap();
        boolean transactional = true;
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        Accessor acc = heap.createAccessor();
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
        heap = TestVars.createHeap();
        boolean transactional = true;
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        CompactAccessor acc = heap.createCompactAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after freeMemory
        acc.freeMemory(transactional);
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testCompactCheckBoundsNegativeOffsetWIP() {
        long offset = -1; // use an invalid offset
        long length = TestVars.TOTAL_SIZE;
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
        CompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mbNew.handle());
        try {
            mb.copyFrom(acc, offset, offset, length);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCopyFromCompactAccessorToCompactLargeLength() {
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        long handle = heap.allocateCompactMemory(1024, true);
        CompactAccessor acc = heap.createCompactAccessor();
        acc.handle(handle);
        acc.copyFrom(mb, 0, 0, 1030);
    }

    @Test
    public void testCopyFromCompactAccessorToCompactVeryLargeLength() {
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
        CompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
        CompactAccessor acc = heap.createCompactAccessor();
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
    public void testFlushNegativeOffset() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        try {
            acc.flush(-1, 100);
        }
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testFlushZeroLength() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        try {
            acc.flush(0, 0);
        }
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testFlushNegativeLength() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        try {
            acc.flush(0, -1);
        }
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testFlush() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        acc.setMemory((byte) -1, 0, acc.size());
        acc.flush();
        for (int i = 0; i < acc.size(); i++) {
            assert (acc.getByte(i) == (byte) -1);
        }
    }

    @Test
    public void testFlushWithRange() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        CompactMemoryBlock mbCompact = heap.allocateCompactMemoryBlock(1024, true);
        CompactAccessor acc2 = heap.createCompactAccessor();
        acc2.handle(mbCompact.handle());
        acc.setMemory((byte) -1, 10, 100);
        acc2.setMemory((byte) -2, 10, 100);
        acc.flush(10, 100);
        acc2.flush(10, 100);
        for (int i = 10; i < 110; i++) {
            assert (acc.getByte(i) == (byte) -1);
            assert (acc2.getByte(i) == (byte) -2);
        }
    }

    @Test
    public void testAddToTransactionRange() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        CompactMemoryBlock mbCompact = heap.allocateCompactMemoryBlock(1024, true);
        CompactAccessor acc2 = heap.createCompactAccessor();
        acc2.handle(mbCompact.handle());
        Transaction t = Transaction.create(heap);
        Assert.assertEquals(t.state(), Transaction.State.New);
        t.run(() -> {
            acc.addToTransaction(10, 150);
            acc2.addToTransaction(190, 210);
            Assert.assertEquals(t.state(), Transaction.State.Active);
            acc.setInt(100, 100);
            acc2.setLong(200, 200);
            assert (acc.getInt(100) == 100);
            assert (acc2.getLong(200) == 200);
            Assert.assertEquals(t.state(), Transaction.State.Active);
        });
        Assert.assertEquals(t.state(), Transaction.State.Committed);
        assert (acc.getInt(100) == 100);
        assert (acc2.getLong(200) == 200);
    }

    @Test
    public void testAddToTransactionNegativeOffset() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        try {
            acc.addToTransaction(-1, 100);
            Assert.fail("IndexOutOfBoundsException not thrown");
        }
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testAddToTransactionZeroLength() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        Transaction t = Transaction.create(heap);
        Assert.assertEquals(t.state(), Transaction.State.New);
        try {
            t.run(() -> {
                assert (Transaction.State.Active == t.state());
                acc.addToTransaction(0, 0);
            });
            Assert.fail("IndexOutOfBoundsException not thrown");
        }
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testAddToTransactionNegativeLength() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        Transaction t = Transaction.create(heap);
        Assert.assertEquals(t.state(), Transaction.State.New);
        try {
            t.run(() -> {
                Assert.assertEquals(t.state(), Transaction.State.Active);
                acc.addToTransaction(0, -1);
            });
            Assert.fail("IndexOutOfBoundsException not thrown");
        }
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testAddToTransactionLargeLength() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        try {
            acc.addToTransaction(0, 10L * 1024 * 1024);
            Assert.fail("IndexOutOfBoundsException not thrown");
        }
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testCopyFromMemoryBlockNull() {
        heap = TestVars.createHeap();
        MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
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
        heap = TestVars.createHeap();
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
        CompactAccessor acc = heap.createCompactAccessor();
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
    public void testAddToTransactionFull() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        Transaction t = Transaction.create(heap);
        Assert.assertEquals(t.state(), Transaction.State.New);
        t.run(() -> {
            Assert.assertEquals(t.state(), Transaction.State.Active);
            acc.addToTransaction();
            acc.setInt(100, 100);
            assert (acc.getInt(100) == 100);
            Assert.assertEquals(t.state(), Transaction.State.Active);
        });
        Assert.assertEquals(t.state(), Transaction.State.Committed);
        assert (acc.getInt(100) == 100);
    }

    @Test
    public void testAccessorWriteByte() {
        heap = TestVars.createHeap();
        Accessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        byte data = 42;
        acc.handle(handle);
        acc.setByte(offset,data);
        Assert.assertEquals(acc.getByte(offset), data);
    }

    @Test
    public void testAccessorWriteByteCompact() {
        heap = TestVars.createHeap();
        CompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        byte data = 42;
        acc.handle(handle);
        acc.setByte(offset,data);
        Assert.assertEquals(acc.getByte(offset), data);
    }

    @Test
    public void testAccessorWriteShort() {
        heap = TestVars.createHeap();
        Accessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        short data = 42;
        acc.handle(handle);
        acc.setShort(offset,data);
        Assert.assertEquals(acc.getShort(offset), data);
    }

    @Test
    public void testAccessorWriteShortCompact() {
        heap = TestVars.createHeap();
        CompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        short data = 42;
        acc.handle(handle);
        acc.setShort(offset,data);
        Assert.assertEquals(acc.getShort(offset), data);
    }

    @Test
    public void testAccessorWriteInt() {
        heap = TestVars.createHeap();
        Accessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        int data = 42;
        acc.handle(handle);
        acc.setInt(offset,data);
        Assert.assertEquals(acc.getInt(offset), data);
    }

    @Test
    public void testAccessorWriteIntCompact() {
        heap = TestVars.createHeap();
        CompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        int data = 42;
        acc.handle(handle);
        acc.setInt(offset,data);
        Assert.assertEquals(acc.getInt(offset), data);
    }

    @Test
    public void testAccessorWriteLong() {
        heap = TestVars.createHeap();
        Accessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 1;
        long data = 42;
        acc.handle(handle);
        acc.setLong(offset,data);
        Assert.assertEquals(acc.getLong(offset), data);
    }

    @Test
    public void testAccessorWriteLongCompact() {
        heap = TestVars.createHeap();
        CompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16,false);
        long offset = 7;
        long data = 42;
        acc.handle(handle);
        acc.setLong(offset,data);
        Assert.assertEquals(acc.getLong(offset), data);
    }

    @Test
    public void testCopyFromArray() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Accessor acc = heap.createAccessor();
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
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        CompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createHeap();
        int numBytes = 256;
        MemoryBlock mbSrc = heap.allocateMemoryBlock(numBytes, true);
        MemoryBlock mbDst = heap.allocateMemoryBlock(numBytes, true);
        Accessor acc = heap.createAccessor();
        acc.handle(mbDst.handle());
        for(int i = 0; i < numBytes; i++)
            mbSrc.setByte(i,(byte)i);
        acc.copyFrom(mbSrc, 0, 0, numBytes);
        for(int i = 0; i < numBytes; i++)
            Assert.assertEquals(mbSrc.getByte(i), mbDst.getByte(i));
    }

    @Test
    public void testAccessorFailArrayBoundsCheckingNegativeLength() {
        heap = TestVars.createHeap();
        int size = 1024;
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(size, true);
        CompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createHeap();
        int size = 1024;
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(size, true);
        CompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        CompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        CompactAccessor acc = heap.createCompactAccessor();
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

}
