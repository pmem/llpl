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

@SuppressWarnings("deprecation")
@Test(singleThreaded = true)
public class TransactionalMemoryBlock1Tests {
	TransactionalHeap heap = null;

	@BeforeMethod
	public void initialize() {
		heap = null;
	}

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
		heap = TestVars.createTransactionalHeap();
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		TransactionalCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1030);
	}

	@Test
	public void testCopyFromCompactMemBlkToCompactVeryLargeLength() {
		heap = TestVars.createTransactionalHeap();
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		TransactionalCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024);
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
		heap = TestVars.createTransactionalHeap();
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		TransactionalMemoryBlock mbNew = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
		TransactionalCompactMemoryBlock mbCompact = heap.allocateCompactMemoryBlock(1024);
		Transaction.create(heap, () -> {
			mb.addToTransaction(190, 210);
            mb.addToTransaction(10, 150);
			Transaction.checkTransactionActive(true);
			mb.setInt(100, 100);
			mbCompact.setLong(200, 200);
			assert (mb.getInt(100) == 100);
			assert (mbCompact.getLong(200) == 200);
			Transaction.checkTransactionActive(true);
		});
		Transaction.checkTransactionActive(false);
		assert (mb.getInt(100) == 100);
		assert (mbCompact.getLong(200) == 200);
	}

	@Test
	public void testAddToTransactionNegativeOffset() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mbNew = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createTransactionalHeap();
		TransactionalCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024);
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
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        TransactionalMemoryBlock mbInternal = mb.withRange(0, 1024, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
            TransactionalMemoryBlock mbNew = heap.allocateMemoryBlock(1024);
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
        heap = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
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
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        TransactionalMemoryBlock mbInternal = mb.withRange(0, 1024, (Range range) -> {
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
		heap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
        Transaction.create(heap, () -> {
            mb.addToTransaction();
            Transaction.checkTransactionActive(true);
            mb.setInt(100, 100);
            assert (mb.getInt(100) == 100);
            Transaction.checkTransactionActive(true);
        });
        Transaction.checkTransactionActive(false);
        assert (mb.getInt(100) == 100);
    }
}
