/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.SkipException;

@Test(singleThreaded = true)
public class PersistentMemoryBlock1Tests {
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
	public void testCopyFromCompactMemBlkToCompactLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1030);
	}

    @Test
    public void testSizeCompactMemoryBlock() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        try {
            mb.size();
            Assert.fail("UnsupportedOperationException was not thrown");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true); 
        }
    }

	@Test
	public void testCopyFromCompactMemBlkToCompactVeryLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
			mbNew.copyFromMemoryBlock(mb, 0, 0, TestVars.TOTAL_SIZE);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testCopyFromCompactMemBlkVeryLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
			mbNew.copyFromMemoryBlock(mb, 0, 0, TestVars.TOTAL_SIZE);
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
		try {
			mbNew.copyFromMemoryBlock(null, 0, 0, 1024);
			Assert.fail("NullPointerException wasn't thrown");
		} 
        catch (NullPointerException e) {
			assert true;
		}
	}

	@Test
	public void testCopyFromMemBlkToCompactBlockNull() {
		heap = TestVars.createPersistentHeap();
		PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
			mbNew.copyFromMemoryBlock(null, 0, 0, 1024);
			Assert.fail("NullPointerException wasn't thrown");
		} 
        catch (NullPointerException e) {
			assert true;
		}
	}

	@Test
    public void testWithRangeFunctionValid() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbInternal = mb.withRange(0, 1024, (Range range) -> {
	        range.setByte(0, (byte)1);
			range.setInt(1, 1234);
			range.setShort(5, (short)2345);
			range.setLong(7, 3456);
			PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
			mbNew.setInt(12, 1234);
			return mbNew;
	    });
        assert (mb.getByte(0) == (byte)1);
        assert (mb.getInt(1) == 1234);
        assert (mb.getShort(5) == (short)2345);
        assert (mb.getLong(7) == 3456);
		assert (mbInternal.getInt(12) == 1234);
        long handle = mb.handle();
		long handleInternal = mbInternal.handle();
        heap.close();
        heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        mb = heap.memoryBlockFromHandle(handle);
		mbInternal = heap.memoryBlockFromHandle(handleInternal);
        assert (mb.getByte(0) == (byte)1);
        assert (mb.getInt(1) == 1234);
        assert (mb.getShort(5) == (short) 2345);
        assert (mb.getLong(7) == 3456);
		assert (mbInternal.getInt(12) == 1234);
    }

	@Test
    public void testWithRangeFunctionNull() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbInternal = mb.withRange(0, 1024, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
            return null;
        });
        assert (mb.getByte(0) == (byte)1);
        assert (mb.getInt(1) == 1234);
        assert (mb.getShort(5) == (short)2345);
        assert (mb.getLong(7) == 3456);
        assert (mbInternal == null);
    }

    @Test
    public void testTxWithRangeFunctionValid() {
	    heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbInternal = mb.transactionalWithRange(0, 1024, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
            PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
            mbNew.setInt(12, 1234);
            return mbNew;
        });
        assert (mb.getByte(0) == (byte)1);
        assert (mb.getInt(1) == 1234);
        assert (mb.getShort(5) == (short)2345);
        assert (mb.getLong(7) == 3456);
        assert (mbInternal.getInt(12) == 1234);
        long handle = mb.handle();
        long handleInternal = mbInternal.handle();
        heap.close();
        heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
        mb = heap.memoryBlockFromHandle(handle);
        mbInternal = heap.memoryBlockFromHandle(handleInternal);
        assert (mb.getByte(0) == (byte)1);
        assert (mb.getInt(1) == 1234);
        assert (mb.getShort(5) == (short) 2345);
        assert (mb.getLong(7) == 3456);
        assert (mbInternal.getInt(12) == 1234);
    }

    @Test
    public void testTxWithRangeFunctionNull() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbInternal = mb.transactionalWithRange(0, 1024, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
            return null;
        });
        assert (mb.getByte(0) == (byte)1);
        assert (mb.getInt(1) == 1234);
        assert (mb.getShort(5) == (short)2345);
        assert (mb.getLong(7) == 3456);
        assert (mbInternal == null);
    }

    // AbstractPersistentMemoryBlock.freeMemory()
    @Test
    public void testMemoryBlockFreeMemory(){
        heap = TestVars.createPersistentHeap();
        // allocate memory
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024);
        // assert valid
        Assert.assertTrue(mb.isValid());
        // free memory
        mb.freeMemory();
        // assert not valid
        Assert.assertFalse(mb.isValid());
    }

}
