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
public class Heap2Tests {

	Heap heap = null;

    @BeforeMethod
	public void intialize() {
        heap = null;
	}

    @AfterMethod
    public void testCleanup() {
        if (heap != null) heap.close();

        if (TestVars.ISDAX) {
               TestVars.daxCleanUp();
        }
        else TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);

        TestVars.cleanUp(TestVars.INVALID_HEAP_PATH);
        for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
            TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i);
        }

        TestVars.cleanUp(TestVars.BLOCK_HANDLE_FILE);
        TestVars.cleanUp(TestVars.POOL_SET_FILE);
    }

    @Test
    public void testCreateHeapNullPath() {
        try {
            @SuppressWarnings("unused")
            Heap heap = Heap.createHeap(null);
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
            Heap heap = Heap.createHeap("");
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
            Heap heap = Heap.createHeap(null, 0);
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
            Heap heap = Heap.createHeap("", 0);
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
            Heap heap = Heap.createHeap("", -1);
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
            Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, -1);
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
            Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 0);
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
            Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 0);
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
            Heap heap = Heap.createHeap(TestVars.POOL_SET_FILE, 0);
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
            Heap heap = Heap.createHeap(TestVars.POOL_SET_FILE, 0);
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
            Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 0);
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
            Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 5242880);
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
            Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 10485760l);
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
            Heap heap = Heap.openHeap(null);
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
            Heap heap = Heap.openHeap("");
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
            Heap heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            Assert.fail("HeapException not thrown");
        }
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateMemoryBlockNonTransactionalZeroSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        try {
            MemoryBlock mb = heap.allocateMemoryBlock(0, false);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateCompactMemoryBlockNonTransactionalZeroSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        try {
            CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(0, false);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateMemoryBlockTransactionalZeroSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        try {
            MemoryBlock mb = heap.allocateMemoryBlock(0, true);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateCompactMemoryBlockTransactionalZeroSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        try {
            CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(0, true);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateMemoryBlockNonTransactionalNegativeSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        try {
            MemoryBlock mb = heap.allocateMemoryBlock(-1024, false);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateCompactMemoryBlockNonTransactionalNegativeSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        try {
            CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(-1024, false);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateMemoryBlockTransactionalNegativeSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        try {
            MemoryBlock mb = heap.allocateMemoryBlock(-1024, true);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateCompactMemoryBlockTransactionalNegativeSizeOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        try {
            CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(-1024, true);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateMemoryBlockNonTransactionalZeroSizeOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
            MemoryBlock mb = heap.allocateMemoryBlock(0, false);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateMemoryBlockTransactionalZeroSizeOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
            MemoryBlock mb = heap.allocateMemoryBlock(0, true);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateCompactMemoryBlockNonTransactionalZeroSizeOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
            CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(0, false);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testAllocateCompactMemoryBlockTransactionalZeroSizeOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
            CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(0, true);
            Assert.assertTrue(mb.isValid());
            Assert.fail("HeapException not thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockLargeHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			MemoryBlock mb = heap.memoryBlockFromHandle(heap.size()  + 1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockWrongLargeHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			MemoryBlock mb = heap.memoryBlockFromHandle(heap.size()  - 1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockWrongLargeHandleOpenGrowableNoLimitHeap2() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			MemoryBlock mb = heap.memoryBlockFromHandle(heap.size()  + 0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenCompactMemoryBlockWrongLargeHandleOpenGrowableNoLimitHeap1() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			CompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(heap.size()  + 0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenCompactMemoryBlockWrongLargeHandleOpenGrowableNoLimitHeap3() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			CompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(heap.size()  + 1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockNegativeHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			MemoryBlock mb = heap.memoryBlockFromHandle(-1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenCompactMemoryBlockNegativeHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			CompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(-1);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenMemoryBlockZeroHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			MemoryBlock mb = heap.memoryBlockFromHandle(0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
	public void testOpenCompactMemoryBlockZeroHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        try {
			CompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(0);
            Assert.assertTrue(mb.isValid());
            Assert.fail("IllegalArgumentException not thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFreeMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, false);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.free(false);
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeCompactMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, false);
        Assert.assertTrue (mb.isValid());
		mb.free(false);
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.free(true);
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        Assert.assertTrue (mb.isValid());
		mb.free(true);
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeTwiceMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, false);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.free(false);
		mb.free(false);
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeTwiceCompactMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, false);
        Assert.assertTrue (mb.isValid());
		mb.free(false);
		mb.free(false);
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeTwiceMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.free(true);
		mb.free(true);
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testFreeTwiceCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        Assert.assertTrue (mb.isValid());
		mb.free(true);
		mb.free(true);
		Assert.assertTrue (!mb.isValid());
    }

    @Test
    public void testCreateHeapValidPathGrowableWithNoLimit() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        Assert.assertTrue(heap.size() > 0);
    }

    @Test
    public void testCreateTwiceHeapValidPathGrowableWithNoLimit() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        Assert.assertTrue(heap.size() > 0);
		try {
            heap = (TestVars.ISDAX) ? TestVars.createHeap()
			                        : Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAllocateMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
    }

    @Test
    public void testAllocateCompactMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
    }

    @Test
    public void testAllocateMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
    }

    @Test
    public void testAllocateCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
    }

    @Test
    public void testWriteToMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testWriteToCompactMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testWriteToMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testWriteToCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testTransactionalWriteToMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
	    Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testTransactionalWriteToCompactMemoryBlockNonTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
	    Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testTransactionalWriteToMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
	    Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testTransactionalWriteToCompactMemoryBlockTransactionalOnGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
	    Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testAllocateMemoryBlockNonTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		heap.close();
		heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
    }

    @Test
    public void testAllocateMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		heap.close();
		heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
    }

    @Test
    public void testAllocateCompactMemoryBlockNonTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		heap.close();
		heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
    }

    @Test
    public void testAllocateCompactMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		heap.close();
		heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
    }

    @Test
    public void testWriteToMemoryBlockNonTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		heap.close();
		heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testWriteToMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		heap.close();
		heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
        Assert.assertTrue (mb.size() == TestVars.MEMORY_BLOCK_SIZE);
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testWriteToCompactMemoryBlockNonTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		heap.close();
		heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue (mb.isValid());
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testWriteToCompactMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		heap.close();
		heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue (mb.isValid());
		mb.setInt(12, 12345);
       	Assert.assertTrue(mb.getInt(12) == 12345);
    }

    @Test
    public void testReadMemoryBlockNonTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue(mb.isValid());
		long handle = mb.handle();
        Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock readMb = heap.memoryBlockFromHandle(handle);
        Assert.assertTrue(readMb.getInt(12) == 12345);
    }

    @Test
    public void testReadMemoryBlockTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue(mb.isValid());
		long handle = mb.handle();
        Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock readMb = heap.memoryBlockFromHandle(handle);
        Assert.assertTrue(readMb.getInt(12) == 12345);
    }

    @Test
    public void testReadMemoryBlockNonTransactionalWrongHandleOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
        Assert.assertTrue(mb.isValid());
		long handle = mb.handle();
		handle += - 1;
        Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock readMb = heap.memoryBlockFromHandle(handle);
        Assert.assertTrue(readMb.getInt(12) != 12345);
    }

    @Test
    public void testOpenCompactMemoryBlockFromMemoryBlockNonTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue(mb.isValid());
		long handle = mb.handle();
        Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock readMb = heap.compactMemoryBlockFromHandle(handle);
        Assert.assertTrue(readMb.getInt(12) != 12345);
    }

    @Test
    public void testReadCompactMemoryBlockNonTransactionalOpenGrowableNoLimitHeap() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, false);
        Assert.assertTrue(mb.isValid());
		long handle = mb.handle();
        Transaction.create(heap, () -> mb.setInt(12, 12345));
        Assert.assertTrue(mb.getInt(12) == 12345);
        heap.close();
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock readMb = heap.compactMemoryBlockFromHandle(handle);
        Assert.assertTrue(readMb.getInt(12) == 12345);
    }
}
