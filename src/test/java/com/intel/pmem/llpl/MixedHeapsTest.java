/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.SkipException;

@Test(singleThreaded = true)
public class MixedHeapsTest {

	@AfterMethod
	public void testCleanup() {
		if (TestVars.ISDAX) {
			TestVars.daxCleanUp();
		}
		else TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);

		TestVars.cleanUp(TestVars.INVALID_HEAP_PATH);
		TestVars.cleanUp(TestVars.BLOCK_HANDLE_FILE);
		TestVars.cleanUp(TestVars.POOL_SET_FILE);
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i);
			TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "P" + i);
			TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "T" + i);
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testReopenWrongHeapType() {
		Heap heap = TestVars.createHeap();
		Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		heap.setRoot(1234);
		Assert.assertTrue(heap.getRoot() == 1234);
		assert (heap.size() > 0);
		heap.close();

		try {
			@SuppressWarnings("unused")
			PersistentHeap pRef = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
			Assert.fail("HeapException was not thrown");
		} 
        catch (HeapException e) {
			assert true;
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMultipleMixedHeaps() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		Heap[] heaps = new Heap[TestVars.NUM_MIXED_HEAPS];
		PersistentHeap[] pHeaps = new PersistentHeap[TestVars.NUM_MIXED_HEAPS];
		TransactionalHeap[] tHeaps = new TransactionalHeap[TestVars.NUM_MIXED_HEAPS];
		// set root
		for (int i = 0; i < TestVars.NUM_MIXED_HEAPS; i++) {
			Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i));
			heaps[i] = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i);
			heaps[i].setRoot((i + 1) * 1234);
			Assert.assertTrue(heaps[i].getRoot() == (i + 1) * 1234);

			Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "P" + i));
			pHeaps[i] = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "P" + i);
			pHeaps[i].setRoot((i + 1) * 1234);
			Assert.assertTrue(pHeaps[i].getRoot() == (i + 1) * 1234);

			Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "T" + i));
			tHeaps[i] = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "T" + i);
			tHeaps[i].setRoot((i + 1) * 1234);
			Assert.assertTrue(tHeaps[i].getRoot() == (i + 1) * 1234);
		}

		// close heaps
		for (int i = 0; i < TestVars.NUM_MIXED_HEAPS; i++) {
			heaps[i].close();
			pHeaps[i].close();
			tHeaps[i].close();
		}

		// open heaps
		for (int i = 0; i < TestVars.NUM_MIXED_HEAPS; i++) {
			heaps[i] = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i);
			pHeaps[i] = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "P" + i);
			tHeaps[i] = TransactionalHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "T" + i);
		}

		// get root
		for (int i = 0; i < TestVars.NUM_MIXED_HEAPS; i++) {
			Assert.assertTrue(heaps[i].getRoot() == (i + 1) * 1234);
			Assert.assertTrue(pHeaps[i].getRoot() == (i + 1) * 1234);
			Assert.assertTrue(tHeaps[i].getRoot() == (i + 1) * 1234);
		}

		// writes
		MemoryBlock[] blocks = new MemoryBlock[TestVars.NUM_MIXED_HEAPS];
		PersistentMemoryBlock[] pBlocks = new PersistentMemoryBlock[TestVars.NUM_MIXED_HEAPS];
		TransactionalMemoryBlock[] tBlocks = new TransactionalMemoryBlock[TestVars.NUM_MIXED_HEAPS];
		for (int i = 0; i < TestVars.NUM_MIXED_HEAPS; i++) {
			int ii = i;
			blocks[i] = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
			blocks[ii].setInt(300, (ii + 1) * 34567);
			Transaction.create(heaps[ii], () -> {
				blocks[ii].setLong(100, (ii + 1) * 12345);
			});
			Transaction.create(heaps[i], () -> {
				blocks[ii].setInt(200, (ii + 1) * 23456);
			});
			pBlocks[i] = pHeaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, true);
			pBlocks[ii].setInt(300, (ii + 1) * 34567);
			Transaction.create(pHeaps[ii], () -> {
				pBlocks[ii].setLong(100, (ii + 1) * 12345);
			});
			Transaction.create(pHeaps[i], () -> {
				pBlocks[ii].setInt(200, (ii + 1) * 23456);
			});
			tBlocks[i] = tHeaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE);
			tBlocks[ii].setInt(300, (ii + 1) * 34567);
			Transaction.create(tHeaps[ii], () -> {
				tBlocks[ii].setLong(100, (ii + 1) * 12345);
			});
			Transaction.create(tHeaps[i], () -> {
				tBlocks[ii].setInt(200, (ii + 1) * 23456);
			});
		}

		// memory block from address
		for (int i = 0; i < TestVars.NUM_MIXED_HEAPS; i++) {
			MemoryBlock block = heaps[i].memoryBlockFromHandle(blocks[i].handle());
			Assert.assertTrue(block.getLong(100) == (i + 1) * 12345);
			Assert.assertTrue(block.getInt(200) == (i + 1) * 23456);
			Assert.assertTrue(block.getInt(300) == (i + 1) * 34567);
			PersistentMemoryBlock pBlock = pHeaps[i].memoryBlockFromHandle(pBlocks[i].handle());
			Assert.assertTrue(pBlock.getLong(100) == (i + 1) * 12345);
			Assert.assertTrue(pBlock.getInt(200) == (i + 1) * 23456);
			Assert.assertTrue(pBlock.getInt(300) == (i + 1) * 34567);
			TransactionalMemoryBlock tBlock = tHeaps[i].memoryBlockFromHandle(tBlocks[i].handle());
			Assert.assertTrue(tBlock.getLong(100) == (i + 1) * 12345);
			Assert.assertTrue(tBlock.getInt(200) == (i + 1) * 23456);
			Assert.assertTrue(tBlock.getInt(300) == (i + 1) * 34567);
		}

		// heap delete and existence
		for (int i = 0; i < TestVars.NUM_MIXED_HEAPS; i++) {
			heaps[i].close();
			pHeaps[i].close();
			tHeaps[i].close();
		}
	}
}
