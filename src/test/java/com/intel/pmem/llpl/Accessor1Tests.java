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

@Test(singleThreaded = true)
public class Accessor1Tests {
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
	public void testCopyFromCompactAccessorToCompactLargeLength() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		long handle = heap.allocateCompactMemory(1024, true);
        CompactAccessor acc = heap.createCompactAccessor();
        acc.handle(handle);
		acc.copyFromMemoryBlock(mb, 0, 0, 1030);
	}

	@Test
	public void testCopyFromCompactAccessorToCompactVeryLargeLength() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
        CompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mbNew.handle());
		try {
			acc.copyFromMemoryBlock(mb, 0, 0, TestVars.TOTAL_SIZE);
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
			acc.copyFromMemoryBlock(mb, 0, 0, TestVars.TOTAL_SIZE);
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
		Assert.assertEquals(Transaction.State.New, t.state());
		t.run(() -> {
            acc.addToTransaction(10, 150);
            acc2.addToTransaction(190, 210);
			Assert.assertEquals(Transaction.State.Active, t.state());
			acc.setInt(100, 100);
			acc2.setLong(200, 200);
			assert (acc.getInt(100) == 100);
			assert (acc2.getLong(200) == 200);
			Assert.assertEquals(Transaction.State.Active, t.state());
		});
		Assert.assertEquals(Transaction.State.Committed, t.state());
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
		Assert.assertEquals(Transaction.State.New, t.state());
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
		Assert.assertEquals(Transaction.State.New, t.state());
		try {
			t.run(() -> {
				Assert.assertEquals(Transaction.State.Active, t.state());
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
			acc.copyFromMemoryBlock(null, 0, 0, 1024);
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
			acc.copyFromMemoryBlock(null, 0, 0, 1024);
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
        Assert.assertEquals(Transaction.State.New, t.state());
        t.run(() -> {
            Assert.assertEquals(Transaction.State.Active, t.state());
            acc.addToTransaction();
            acc.setInt(100, 100);
            assert (acc.getInt(100) == 100);
            Assert.assertEquals(Transaction.State.Active, t.state());
        });
        Assert.assertEquals(Transaction.State.Committed, t.state());
        assert (acc.getInt(100) == 100);
    }
}
