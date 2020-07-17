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
public class TransactionalAccessorMemoryTests {
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
    public void testCopyFromCompactAccessorToCompactLargeLength() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        long handle = heap.allocateCompactMemory(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        acc.handle(handle);
        acc.copyFrom(mb, 0, 0, 1030);
    }

    @Test
    public void testCopyFromCompactAccessorToCompactVeryLargeLength() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        TransactionalCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        TransactionalMemoryBlock mbNew = heap.allocateMemoryBlock(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mbNew = heap.allocateMemoryBlock(1024);
        TransactionalAccessor acc = heap.createAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalAccessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16);
        long offset = 7;
        byte data = 42;
        acc.handle(handle);
        acc.setByte(offset,data);
        Assert.assertEquals(data,(byte)acc.getByte(offset));
    }

    @Test
    public void testAccessorWriteByteCompact() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16);
        long offset = 7;
        byte data = 42;
        acc.handle(handle);
        acc.setByte(offset,data);
        Assert.assertEquals(data,(byte)acc.getByte(offset));
    }

    @Test
    public void testAccessorWriteShort() {
        heap = TestVars.createTransactionalHeap();
        TransactionalAccessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16);
        long offset = 7;
        short data = 42;
        acc.handle(handle);
        acc.setShort(offset,data);
        Assert.assertEquals(data,(short)acc.getShort(offset));
    }

    @Test
    public void testAccessorWriteShortCompact() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16);
        long offset = 7;
        short data = 42;
        acc.handle(handle);
        acc.setShort(offset,data);
        Assert.assertEquals(data,(short)acc.getShort(offset));
    }

    @Test
    public void testAccessorWriteInt() {
        heap = TestVars.createTransactionalHeap();
        TransactionalAccessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16);
        long offset = 7;
        int data = 42;
        acc.handle(handle);
        acc.setInt(offset,data);
        Assert.assertEquals(data,(int)acc.getInt(offset));
    }

    @Test
    public void testAccessorWriteIntCompact() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16);
        long offset = 7;
        int data = 42;
        acc.handle(handle);
        acc.setInt(offset,data);
        Assert.assertEquals(data,(int)acc.getInt(offset));
    }

    @Test
    public void testAccessorWriteLong() {
        heap = TestVars.createTransactionalHeap();
        TransactionalAccessor acc = heap.createAccessor();
        long handle = heap.allocateMemory(16);
        long offset = 1;
        long data = 42;
        acc.handle(handle);
        acc.setLong(offset,data);
        Assert.assertEquals(data,(long)acc.getLong(offset));
    }

    @Test
    public void testAccessorWriteLongCompact() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
        long handle = heap.allocateMemory(16);
        long offset = 7;
        long data = 42;
        acc.handle(handle);
        acc.setLong(offset,data);
        Assert.assertEquals(data,(long)acc.getLong(offset));
    }

    @Test
    public void testCopyFromArray() {
        heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        TransactionalAccessor acc = heap.createAccessor();
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
    public void testCopyFromArrayCompact() {
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        int numBytes = 256;
        TransactionalMemoryBlock mbSrc = heap.allocateMemoryBlock(numBytes);
        TransactionalMemoryBlock mbDst = heap.allocateMemoryBlock(numBytes);
        TransactionalAccessor acc = heap.createAccessor();
        acc.handle(mbDst.handle());
        for(int i = 0; i < numBytes; i++)
            mbSrc.setByte(i,(byte)i);
        acc.copyFrom(mbSrc, 0, 0, numBytes);
        for(int i = 0; i < numBytes; i++)
            Assert.assertEquals(mbSrc.getByte(i), mbDst.getByte(i));
    }

    @Test
    public void testAccessorFailArrayBoundsCheckingNegativeLength() {
        heap = TestVars.createTransactionalHeap();
        int size = 1024;
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(size);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        int size = 1024;
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(size);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        TransactionalCompactAccessor acc = heap.createCompactAccessor();
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
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
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
        heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
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

