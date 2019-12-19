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
	public void testAddToTransactionRange() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		PersistentCompactMemoryBlock mbCompact = heap.allocateCompactMemoryBlock(1024, true);
		Transaction t = Transaction.create(heap);
		t.run(() -> {
			mb.addToTransaction(10, 150);
                        mbCompact.addToTransaction(190, 210);
                        assert (Transaction.State.Active == t.state());
			mb.setInt(100, 100);
			mbCompact.setLong(200, 200);
			assert (mb.getInt(100) == 100);
			assert (mbCompact.getLong(200) == 200);
                        assert (Transaction.State.Active == t.state());
		});
                Assert.assertEquals(Transaction.State.Committed, t.state());
		assert (mb.getInt(100) == 100);
		assert (mbCompact.getLong(200) == 200);
	}

	@Test
	public void testAddToTransactionNegativeOffset() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
			mb.addToTransaction(-1, 100);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testAddToTransactionZeroLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		Transaction t = Transaction.create(heap);
		Assert.assertEquals(Transaction.State.New, t.state());
		try {
			t.run(() -> {
				assert (Transaction.State.Active == t.state());
				mb.addToTransaction(0, 0);
			});
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testAddToTransactionNegativeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		Transaction t = Transaction.create(heap);
		Assert.assertEquals(Transaction.State.New, t.state());
		try {
			t.run(() -> {
				Assert.assertEquals(Transaction.State.Active, t.state());
				mb.addToTransaction(0, -1);
			});
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testAddToTransactionLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
			mb.addToTransaction(0, 10L * 1024 * 1024);
			Assert.fail("IndexOutOfBoundsException not thrown");
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

	@Test
    public void testAddToTransactionFull() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		Transaction t = Transaction.create(heap);
        t.run(() -> {
            mb.addToTransaction();
            assert (Transaction.State.Active == t.state());
            mb.setInt(100, 100);
            assert (mb.getInt(100) == 100);
            assert (Transaction.State.Active == t.state());
        });
        assert (Transaction.State.Committed == t.state());
        assert (mb.getInt(100) == 100);
    }
}
