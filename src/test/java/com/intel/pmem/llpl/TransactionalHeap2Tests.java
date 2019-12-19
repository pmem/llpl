/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.SkipException;

@SuppressWarnings("deprecation")
@Test(singleThreaded=true)
public class TransactionalHeap2Tests {
	TransactionalHeap heap = null;

    @BeforeMethod
    public void initialize() {
        heap = null;
    }

    @AfterMethod
    public void testCleanup() {
        if(heap != null) heap.close();

		if (TestVars.ISDAX) {
            TestVars.daxCleanUp();
        }
        else TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);

		TestVars.cleanUp(TestVars.INVALID_HEAP_PATH);
		TestVars.cleanUp(TestVars.BLOCK_HANDLE_FILE);
		TestVars.cleanUp(TestVars.POOL_SET_FILE);
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i);
		}
    }

    @Test
    public void testCreateHeapNullPath() {
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(null);
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapEmptyPath() {
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap("");
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapNullPathZeroSize() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(null, 0);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapEmptyPathZeroSize() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap("", 0);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapEmptyPathNegativeSize() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap("", -1);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapValidPathNegativeSize() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, -1);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapWithExistingDirZeroSize() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 0);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapWithNonExistingFileZeroSize() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 0);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapFusedNoFile() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.POOL_SET_FILE, 0);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapFusedEmptyPoolFile() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue(TestVars.createFile(TestVars.POOL_SET_FILE));
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.POOL_SET_FILE, 0);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateHeapFusedExistingDir() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 0);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateGrowableWithLimitHeapSmall() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 5242880);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCreateFixedHeapExistingFile() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue(TestVars.createFile(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 10485760l);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testOpenHeapNullPath() {
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.openHeap(null);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testOpenHeapEmptyPath() {
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.openHeap("");
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testOpenHeapValidPathNoExist() {
        try {
            @SuppressWarnings("unused")
            TransactionalHeap heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateMemoryBlockTransactionalZeroSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        try {
            TransactionalMemoryBlock mb = heap.allocateMemoryBlock(0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateCompactMemoryBlockTransactionalZeroSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        try {
            TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateMemoryBlockTransactionalNegativeSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        try {
            TransactionalMemoryBlock mb = heap.allocateMemoryBlock(-1024);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateCompactMemoryBlockTransactionalNegativeSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        try {
            TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(-1024);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateMemoryBlockTransactionalZeroSizeOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
            TransactionalMemoryBlock mb = heap.allocateMemoryBlock(0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateCompactMemoryBlockTransactionalZeroSizeOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
            TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockLargeHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			TransactionalMemoryBlock mb = heap.memoryBlockFromHandle(heap.size()  + 1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockWrongLargeHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			TransactionalMemoryBlock mb = heap.memoryBlockFromHandle(heap.size()  - 1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockWrongLargeHandleOpenGrowableNoLimitHeap2() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			TransactionalMemoryBlock mb = heap.memoryBlockFromHandle(heap.size()  + 0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenCompactMemoryBlockWrongLargeHandleOpenGrowableNoLimitHeap1() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			TransactionalCompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(heap.size()  + 0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenCompactMemoryBlockWrongLargeHandleOpenGrowableNoLimitHeap3() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			TransactionalCompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(heap.size()  + 1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockNegativeHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			TransactionalMemoryBlock mb = heap.memoryBlockFromHandle(-1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenCompactMemoryBlockNegativeHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			TransactionalCompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(-1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockZeroHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			TransactionalMemoryBlock mb = heap.memoryBlockFromHandle(0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenCompactMemoryBlockZeroHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			TransactionalCompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFreeMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.free();
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        Assert.assertTrue (mb.isValid());
		mb.free();
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeTwiceMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.free();
		mb.free();
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeTwiceCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        Assert.assertTrue (mb.isValid());
		mb.free();
		mb.free();
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testCreateHeapValidPathGrowableWithNoLimit() {
		heap = TestVars.createTransactionalHeap();
        Assert.assertTrue(TransactionalHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        Assert.assertTrue(heap.size() > 0);
    }

    @Test
    public void testCreateTwiceHeapValidPathGrowableWithNoLimit() {
		heap = TestVars.createTransactionalHeap();
        Assert.assertTrue(TransactionalHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        Assert.assertTrue(heap.size() > 0);
		try {
            heap = (TestVars.ISDAX) ? TestVars.createTransactionalHeap()
			                        : TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAllocateMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
    }

    @Test
    public void testAllocateCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
    }

    @Test
    public void testWriteToMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testWriteToCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testTransactionalWriteToMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
	    Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testTransactionalWriteToCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
	    Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testAllocateMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
		heap.close();
		heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
    }

    @Test
    public void testAllocateCompactMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
		heap.close();
		heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
    }

    @Test
    public void testWriteToMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
		heap.close();
		heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testWriteToCompactMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
		heap.close();
		heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue (mb.isValid());
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testReadMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue(mb.isValid());
		long handle = mb.handle();
        Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock readMb = heap.memoryBlockFromHandle(handle);
        Assert.assertTrue(readMb.getInt(12) == 12345);
    }

    @Test
    public void testReadMemoryBlockTransactionalWrongHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue(mb.isValid());
		long handle = mb.handle();
		handle += - 1;
        Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock readMb = heap.memoryBlockFromHandle(handle);
        Assert.assertTrue(readMb.getInt(12) != 12345);
    }

    @Test
    public void testOpenCompactMemoryBlockFromMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue(mb.isValid());
		long handle = mb.handle();
        Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock readMb = heap.compactMemoryBlockFromHandle(handle);
        Assert.assertTrue(readMb.getInt(12) != 12345);
    }

    @Test
    public void testReadCompactMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
        Assert.assertTrue(mb.isValid());
		long handle = mb.handle();
        Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
        heap.close();
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock readMb = heap.compactMemoryBlockFromHandle(handle);
        Assert.assertTrue(readMb.getInt(12) == 12345);
    }

	@Test
    public void testAllocateMemoryBlockWithRange() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, (Range range) -> {
        long offset = 0;
        range.setShort(offset, TestVars.SHORT_DATA);
        offset += Short.SIZE / Byte.SIZE;
        range.setInt(offset, TestVars.INT_DATA);
        offset += Integer.SIZE / Byte.SIZE;
        range.setLong(offset, TestVars.LONG_DATA);
        offset += Long.SIZE / Byte.SIZE;
        long remainingLen = TestVars.MEMORY_BLOCK_SIZE - offset;
        range.setMemory(TestVars.BYTE_DATA, offset, remainingLen); });
		long offset = 0;
        Assert.assertTrue(mb.getShort(offset) == TestVars.SHORT_DATA);
        offset += Short.SIZE / Byte.SIZE;
        Assert.assertTrue(mb.getInt(offset) == TestVars.INT_DATA);
        offset += Integer.SIZE / Byte.SIZE;
        Assert.assertTrue(mb.getLong(offset) == TestVars.LONG_DATA);
        offset += Long.SIZE / Byte.SIZE;
        for (long i = offset; offset < TestVars.MEMORY_BLOCK_SIZE; offset++)
            Assert.assertTrue(mb.getByte(i) == TestVars.BYTE_DATA);
    }

	@Test
    public void testAllocateMemoryBlockWithRangeHeapReopen() {
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, (Range range) -> {
        long offset = 0;
        range.setShort(offset, TestVars.SHORT_DATA);
        offset += Short.SIZE / Byte.SIZE;
        range.setInt(offset, TestVars.INT_DATA);
        offset += Integer.SIZE / Byte.SIZE;
        range.setLong(offset, TestVars.LONG_DATA);
        offset += Long.SIZE / Byte.SIZE;
        long remainingLen = TestVars.MEMORY_BLOCK_SIZE - offset;
        range.setMemory(TestVars.BYTE_DATA, offset, remainingLen); });
		long handle = mb.handle();
		heap.close();
		heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		mb = heap.memoryBlockFromHandle(handle);
		long offset = 0;
        Assert.assertTrue(mb.getShort(offset) == TestVars.SHORT_DATA);
        offset += Short.SIZE / Byte.SIZE;
        Assert.assertTrue(mb.getInt(offset) == TestVars.INT_DATA);
        offset += Integer.SIZE / Byte.SIZE;
        Assert.assertTrue(mb.getLong(offset) == TestVars.LONG_DATA);
        offset += Long.SIZE / Byte.SIZE;
        for (long i = offset; offset < TestVars.MEMORY_BLOCK_SIZE; offset++)
            Assert.assertTrue(mb.getByte(i) == TestVars.BYTE_DATA);
    }

	@Test
    public void testAllocateCompactMemoryBlockWithRange() {
		heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, (Range range) -> {
        long offset = 0;
        range.setShort(offset, TestVars.SHORT_DATA);
        offset += Short.SIZE / Byte.SIZE;
        range.setInt(offset, TestVars.INT_DATA);
        offset += Integer.SIZE / Byte.SIZE;
        range.setLong(offset, TestVars.LONG_DATA);
        offset += Long.SIZE / Byte.SIZE;
        long remainingLen = TestVars.MEMORY_BLOCK_SIZE - offset;
        range.setMemory(TestVars.BYTE_DATA, offset, remainingLen); });
		long offset = 0;
        Assert.assertTrue(mb.getShort(offset) == TestVars.SHORT_DATA);
        offset += Short.SIZE / Byte.SIZE;
        Assert.assertTrue(mb.getInt(offset) == TestVars.INT_DATA);
        offset += Integer.SIZE / Byte.SIZE;
        Assert.assertTrue(mb.getLong(offset) == TestVars.LONG_DATA);
        offset += Long.SIZE / Byte.SIZE;
        for (long i = offset; offset < TestVars.MEMORY_BLOCK_SIZE; offset++)
            Assert.assertTrue(mb.getByte(i) == TestVars.BYTE_DATA);
    }

	@Test
    public void testAllocateCompactMemoryBlockWithRangeHeapReopen() {
		heap = TestVars.createTransactionalHeap();
        TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, (Range range) -> {
        long offset = 0;
        range.setShort(offset, TestVars.SHORT_DATA);
        offset += Short.SIZE / Byte.SIZE;
        range.setInt(offset, TestVars.INT_DATA);
        offset += Integer.SIZE / Byte.SIZE;
        range.setLong(offset, TestVars.LONG_DATA);
        offset += Long.SIZE / Byte.SIZE;
        long remainingLen = TestVars.MEMORY_BLOCK_SIZE - offset;
        range.setMemory(TestVars.BYTE_DATA, offset, remainingLen); });
		long handle = mb.handle();
		heap.close();
		heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		mb = heap.compactMemoryBlockFromHandle(handle);
		long offset = 0;
        Assert.assertTrue(mb.getShort(offset) == TestVars.SHORT_DATA);
        offset += Short.SIZE / Byte.SIZE;
        Assert.assertTrue(mb.getInt(offset) == TestVars.INT_DATA);
        offset += Integer.SIZE / Byte.SIZE;
        Assert.assertTrue(mb.getLong(offset) == TestVars.LONG_DATA);
        offset += Long.SIZE / Byte.SIZE;
        for (long i = offset; offset < TestVars.MEMORY_BLOCK_SIZE; offset++)
            Assert.assertTrue(mb.getByte(i) == TestVars.BYTE_DATA);
    }

}
