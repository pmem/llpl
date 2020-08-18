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
public class MemoryBlock1Tests {
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
	public void testCopyFromCompactMemBlkToCompactLargeLength() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1030);
	}

	@Test
	public void testCopyFromCompactMemBlkToCompactVeryLargeLength() {
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
			mbNew.copyFromMemoryBlock(mb, 0, 0, TestVars.TOTAL_SIZE);
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
		try {
			mb.flush(-1, 100);
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testFlushZeroLength() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
			mb.flush(0, 0);
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testFlushNegativeLength() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
			mb.flush(0, -1);
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testFlush() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte) -1, 0, mb.size());
		mb.flush();
		for (int i = 0; i < mb.size(); i++) {
			assert (mb.getByte(i) == (byte) -1);
		}
	}

	@Test
	public void testFlushWithRange() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		CompactMemoryBlock mbCompact = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte) -1, 10, 100);
		mbCompact.setMemory((byte) -2, 10, 100);
		mb.flush(10, 100);
		mbCompact.flush(10, 100);
		for (int i = 10; i < 110; i++) {
			assert (mb.getByte(i) == (byte) -1);
			assert (mbCompact.getByte(i) == (byte) -2);
		}
	}

	@Test
	public void testAddToTransactionRange() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		CompactMemoryBlock mbCompact = heap.allocateCompactMemoryBlock(1024, true);
		Transaction t = Transaction.create(heap);
		Assert.assertEquals(Transaction.State.New, t.state());
		t.run(() -> {
            mb.addToTransaction(10, 150);
            mbCompact.addToTransaction(190, 210);
			Assert.assertEquals(Transaction.State.Active, t.state());
			mb.setInt(100, 100);
			mbCompact.setLong(200, 200);
			assert (mb.getInt(100) == 100);
			assert (mbCompact.getLong(200) == 200);
			Assert.assertEquals(Transaction.State.Active, t.state());
		});
		Assert.assertEquals(Transaction.State.Committed, t.state());
		assert (mb.getInt(100) == 100);
		assert (mbCompact.getLong(200) == 200);
	}

	@Test
	public void testAddToTransactionNegativeOffset() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
			mbNew.copyFromMemoryBlock(null, 0, 0, 1024);
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
        Transaction t = Transaction.create(heap);
        Assert.assertEquals(Transaction.State.New, t.state());
        t.run(() -> {
            Assert.assertEquals(Transaction.State.Active, t.state());
            mb.addToTransaction();
            mb.setInt(100, 100);
            assert (mb.getInt(100) == 100);
            Assert.assertEquals(Transaction.State.Active, t.state());
        });
        Assert.assertEquals(Transaction.State.Committed, t.state());
        assert (mb.getInt(100) == 100);
    }

    public void testWithRangeFunctionValid() {
               heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        MemoryBlock mbInternal = mb.withRange(0, 1024, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            range.setLong(7, 3456);
            MemoryBlock mbNew = heap.allocateMemoryBlock(1024);
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
        heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
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
               heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        MemoryBlock mbInternal = mb.withRange(0, 1024, (Range range) -> {
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

    // AbstractMemoryBlock.freeMemory()
    @Test
    public void testMemoryBlockFreeMemory(){
        heap = TestVars.createHeap();
        // allocate memory
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        // assert valid
        Assert.assertTrue(mb.isValid());
        // free memory
        mb.freeMemory();
        // assert not valid
        Assert.assertFalse(mb.isValid());
    }

    // AbstractMemoryBlock.withRange(long, long, Consumer)
    @Test
    public void testMemoryBlockWithRangeConsumer(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        mb.withRange(offset, length, (Range range) -> {
            range.setByte(0, (byte)1);
			range.setInt(1, 1234);
			range.setShort(5, (short)2345);
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
    }

    // MemoryAccessor.withRange(long, long, Function)
    @Test
    public void testMemoryBlockWithRangeFunction(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        MemoryBlock mbInternal = mb.withRange(offset, length, (Range range) -> {
            range.setByte(0, (byte)1);
			range.setInt(1, 1234);
			range.setShort(5, (short)2345);
            MemoryBlock mbNew = heap.allocateMemoryBlock(1024);
            mbNew.setInt(0,54321);
            return mbNew;
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
        Assert.assertEquals(mbInternal.getInt(0), 54321);
    }

}
