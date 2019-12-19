/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.SkipException;

@Test(singleThreaded = true)
public class PersistentHeap1Tests {
	PersistentHeap heap = null;

	@BeforeMethod
	public void initialze() {
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

		TestVars.cleanUp(TestVars.INVALID_HEAP_PATH);
		TestVars.cleanUp(TestVars.BLOCK_HANDLE_FILE);
		TestVars.cleanUp(TestVars.POOL_SET_FILE);
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i);
		}
	}

	@Test
	public void testCreateHeapFusedInvalidFormatFile() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		try (PrintWriter poolsetFile = new PrintWriter(TestVars.POOL_SET_FILE)) {
			poolsetFile.println("PMEMPOOLSET");
		} 
        catch (FileNotFoundException e) {
			assert false;
			return;
		}
		try {
			@SuppressWarnings("unused")
			PersistentHeap heap = PersistentHeap.createHeap(TestVars.POOL_SET_FILE, 0);
		} 
        catch (HeapException e) {
			assert true;
		}
	}

	@Test
	public void testCreateHeapFused() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		try (PrintWriter poolsetFile = new PrintWriter(TestVars.POOL_SET_FILE)) {
			poolsetFile.println("PMEMPOOLSET");
			poolsetFile.println("OPTION SINGLEHDR");
			poolsetFile.println(TestVars.HEAP_SIZE + " " + TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		} 
        catch (FileNotFoundException e) {
			assert false;
			return;
		}
		heap = PersistentHeap.createHeap(TestVars.POOL_SET_FILE, 0);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
	}

	@Test
	public void testCreateTwiceHeapFused() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		try (PrintWriter poolsetFile = new PrintWriter(TestVars.POOL_SET_FILE)) {
			poolsetFile.println("PMEMPOOLSET");
			poolsetFile.println("OPTION SINGLEHDR");
			poolsetFile.println(TestVars.HEAP_SIZE + " " + TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		} 
        catch (FileNotFoundException e) {
			assert false;
			return;
		}
		heap = PersistentHeap.createHeap(TestVars.POOL_SET_FILE, 0);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		try {
			heap = PersistentHeap.createHeap(TestVars.POOL_SET_FILE, 0);
		} 
        catch (HeapException e) {
			assert true;
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testOpenHeapFused() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		try (PrintWriter poolsetFile = new PrintWriter(TestVars.POOL_SET_FILE)) {
			poolsetFile.println("PMEMPOOLSET");
			poolsetFile.println("OPTION SINGLEHDR");
			poolsetFile.println(TestVars.HEAP_SIZE + " " + TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		} 
        catch (FileNotFoundException e) {
			assert false;
			return;
		}
		heap = PersistentHeap.createHeap(TestVars.POOL_SET_FILE, 0);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		long heapSize = heap.size();
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.POOL_SET_FILE);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		assert (heapSize == heap.size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testOpenTwiceHeapFused() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		try (PrintWriter poolsetFile = new PrintWriter(TestVars.POOL_SET_FILE)) {
			poolsetFile.println("PMEMPOOLSET");
			poolsetFile.println("OPTION SINGLEHDR");
			poolsetFile.println(TestVars.HEAP_SIZE + " " + TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		} 
        catch (FileNotFoundException e) {
			assert false;
			return;
		}
		heap = PersistentHeap.createHeap(TestVars.POOL_SET_FILE, 0);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		long heapSize = heap.size();
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.POOL_SET_FILE);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		assert (heapSize == heap.size());
		try {
			heap = PersistentHeap.openHeap(TestVars.POOL_SET_FILE);
		} 
        catch (HeapException e) {
			assert true;
		}
	}

	@Test
	public void testCreateFixedHeap() {
		heap = (TestVars.ISDAX) ? TestVars.createPersistentHeap()
					            : PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		if (!TestVars.ISDAX) {
			File file = new File(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
			if (!file.exists() || !file.isFile())
				assert false;
		}
	}

	@Test
	public void testCreateTwiceFixedHeap() {
		heap = (TestVars.ISDAX) ? TestVars.createPersistentHeap()
					            : PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		if (!TestVars.ISDAX) {
			File file = new File(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
			if (!file.exists() || !file.isFile())
				assert false;
		}
		try {
			heap = (TestVars.ISDAX) ? TestVars.createPersistentHeap()
					                : PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		} 
        catch (HeapException e) {
			assert true;
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testOpenFixedHeap() {
		heap = (TestVars.ISDAX) ? TestVars.createPersistentHeap()
					            : PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		if (!TestVars.ISDAX) {
			File file = new File(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
			if (!file.exists() || !file.isFile())
				assert false;
		}
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testOpenTwiceFixedHeap() {
		heap = (TestVars.ISDAX) ? TestVars.createPersistentHeap()
					            : PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		if (!TestVars.ISDAX) {
			File file = new File(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
			if (!file.exists() || !file.isFile())
				assert false;
		}
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		try {
			heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		} 
        catch (HeapException e) {
			assert true;
		}
	}

	@Test
	public void testCreateGrowableWithLimitHeap() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		File file = new File(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		if (!file.exists() || !file.isDirectory())
			assert false;
	}

	@Test
	public void testCreateTwiceGrowableWithLimitHeap() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		File file = new File(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		if (!file.exists() || !file.isDirectory())
			assert false;
		try {
			heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		} 
        catch (HeapException e) {
			assert true;
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testOpenGrowableWithLimitHeap() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		long heapSize = heap.size();
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		File file = new File(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		if (!file.exists() || !file.isDirectory())
			assert false;
		assert (heapSize == heap.size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testOpenTwiceGrowableWithLimitHeap() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		long heapSize = heap.size();
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		File file = new File(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		if (!file.exists() || !file.isDirectory())
			assert false;
		assert (heapSize == heap.size());

		try {
			heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		} 
        catch (HeapException e) {
			assert true;
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMultipleOnGrowableNoLimitHeaps() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		PersistentHeap[] heaps = new PersistentHeap[TestVars.NUM_HEAPS];
		// set root
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i));
			heaps[i] = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i);
			heaps[i].setRoot((i + 1) * 1234);
		}
		// get root
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			Assert.assertTrue(heaps[i].getRoot() == (i + 1) * 1234);
		}
		// transactions/non transactional writes to memory blocks
		PersistentMemoryBlock[] blocks = new PersistentMemoryBlock[TestVars.NUM_HEAPS];
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			int ii = i;
			if (i % 2 == 0)
				blocks[i] = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, TestVars.TRANSACTIONAL);
			else
				blocks[i] = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, TestVars.NON_TRANSACTIONAL);
			Transaction.create(heaps[ii], () -> {
				blocks[ii].setLong(100, ii * 12345);
			});
			Transaction.create(heaps[i], () -> {
				blocks[ii].setInt(200, ii * 23456);
			});
			blocks[ii].setInt(300, ii * 34567);
		}
		// memory block from address
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			PersistentMemoryBlock block = heaps[i].memoryBlockFromHandle(blocks[i].handle());
			Assert.assertTrue(block.getLong(100) == i * 12345);
			Assert.assertTrue(block.getInt(200) == i * 23456);
			Assert.assertTrue(block.getInt(300) == i * 34567);
		}
		try {} 
        finally {
			for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
				heaps[i].close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMultipleOnGrowableNoLimitHeapsExceedsMemory() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		PersistentHeap[] heaps = null;
		int MAX_HEAPS_ALLOCATED = 0;
		try {
			heaps = new PersistentHeap[TestVars.NUM_HEAPS];
			// set root
			for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
				Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i));
				heaps[i] = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i);
				MAX_HEAPS_ALLOCATED++;
				System.out.println("Heap size GNL: " + heaps[i].size());
				heaps[i].setRoot((i + 1) * 1234);
				Assert.assertTrue(heaps[i].getRoot() == (i + 1) * 1234);
				PersistentMemoryBlock mb = null;
				if (i % 2 == 0)
					mb = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE_2G, TestVars.NON_TRANSACTIONAL);
				else
					mb = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE_2G, TestVars.TRANSACTIONAL);
				Assert.assertTrue(mb.isValid());
				Assert.assertTrue(mb.size() == TestVars.MEMORY_BLOCK_SIZE_2G);
				System.out.println("Growable no limit Heap: " + MAX_HEAPS_ALLOCATED + " Size: " + heaps[i].size());
			}
		} 
        catch (HeapException e) {
			assert (MAX_HEAPS_ALLOCATED < TestVars.NUM_HEAPS);
		} 
        finally {
            assert (MAX_HEAPS_ALLOCATED < TestVars.NUM_HEAPS);
			for (int i = 0; i < MAX_HEAPS_ALLOCATED; i++) {
				heaps[i].close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMultipleOnGrowableWithLimitHeaps() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		PersistentHeap[] heaps = new PersistentHeap[TestVars.NUM_HEAPS];
		// set root
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i));
			heaps[i] = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i, TestVars.HEAP_SIZE);
			heaps[i].setRoot((i + 1) * 1234);
		}
		// get root
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			Assert.assertTrue(heaps[i].getRoot() == (i + 1) * 1234);
		}
		// transactions/non transactional writes to memory blocks
		PersistentMemoryBlock[] blocks = new PersistentMemoryBlock[TestVars.NUM_HEAPS];
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			int ii = i;
			if (i % 2 == 0)
				blocks[i] = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, TestVars.TRANSACTIONAL);
			else
				blocks[i] = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, TestVars.NON_TRANSACTIONAL);
			Transaction.create(heaps[ii], () -> {
				blocks[ii].setLong(100, ii * 12345);
			});
			Transaction.create(heaps[i], () -> {
				blocks[ii].setInt(200, ii * 23456);
			});
			blocks[ii].setInt(300, ii * 34567);
		}
		// memory block from address
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			PersistentMemoryBlock block = heaps[i].memoryBlockFromHandle(blocks[i].handle());
			Assert.assertTrue(block.getLong(100) == i * 12345);
			Assert.assertTrue(block.getInt(200) == i * 23456);
			Assert.assertTrue(block.getInt(300) == i * 34567);
		}
		try {} 
        finally {
			for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
				heaps[i].close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMultipleOnGrowableWithLimitHeapsExceedsMemory() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		PersistentHeap[] heaps = null;
		int MAX_HEAPS_ALLOCATED = 0;
		try {
			heaps = new PersistentHeap[TestVars.NUM_HEAPS];
			// set root
			for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
				Assert.assertTrue(TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i));
				heaps[i] = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i, TestVars.HEAP_SIZE_3G);
				MAX_HEAPS_ALLOCATED++;
				heaps[i].setRoot((i + 1) * 1234);
				Assert.assertTrue(heaps[i].getRoot() == (i + 1) * 1234);
				PersistentMemoryBlock mb = null;
				if (i % 2 == 0)
					mb = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE_2G, TestVars.NON_TRANSACTIONAL);
				else
					mb = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE_2G, TestVars.TRANSACTIONAL);
				Assert.assertTrue(mb.isValid());
				Assert.assertTrue(mb.size() == TestVars.MEMORY_BLOCK_SIZE_2G);
			}
		} 
        catch (HeapException e) {
			assert (MAX_HEAPS_ALLOCATED < TestVars.NUM_HEAPS);
		} 
        finally {
            assert (MAX_HEAPS_ALLOCATED < TestVars.NUM_HEAPS);
			for (int i = 0; i < MAX_HEAPS_ALLOCATED; i++) {
				heaps[i].close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMultipleFixedHeaps() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		PersistentHeap[] heaps = new PersistentHeap[TestVars.NUM_HEAPS];
		// set root
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			heaps[i] = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i, TestVars.HEAP_SIZE);
			heaps[i].setRoot((i + 1) * 1234);
		}
		// get root
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			Assert.assertTrue(heaps[i].getRoot() == (i + 1) * 1234);
		}
		// transactions/non transactional writes to memory blocks
		PersistentMemoryBlock[] blocks = new PersistentMemoryBlock[TestVars.NUM_HEAPS];
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			int ii = i;
			if (i % 2 == 0)
				blocks[i] = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, TestVars.TRANSACTIONAL);
			else
				blocks[i] = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, TestVars.NON_TRANSACTIONAL);
			Transaction.create(heaps[ii], () -> {
				blocks[ii].setLong(100, ii * 12345);
			});
			Transaction.create(heaps[i], () -> {
				blocks[ii].setInt(200, ii * 23456);
			});
			blocks[ii].setInt(300, ii * 34567);
		}
		// memory block from address
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			PersistentMemoryBlock block = heaps[i].memoryBlockFromHandle(blocks[i].handle());
			Assert.assertTrue(block.getLong(100) == i * 12345);
			Assert.assertTrue(block.getInt(200) == i * 23456);
			Assert.assertTrue(block.getInt(300) == i * 34567);
		}
		try {} 
        finally {
			for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
				heaps[i].close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMultipleFixedHeapsExceedsMemory() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		PersistentHeap[] heaps = null;
		int MAX_HEAPS_ALLOCATED = 0;
		try {
			heaps = new PersistentHeap[TestVars.NUM_HEAPS];
			// set root
			for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
				heaps[i] = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i,
						TestVars.MULTIPLE_HEAP_SIZE);
				MAX_HEAPS_ALLOCATED++;
				heaps[i].setRoot((i + 1) * 1234);
				Assert.assertTrue(heaps[i].getRoot() == (i + 1) * 1234);
				PersistentMemoryBlock mb = null;
				if (i % 2 == 0)
					mb = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE_100MB, TestVars.NON_TRANSACTIONAL);
				else
					mb = heaps[i].allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE_100MB, TestVars.TRANSACTIONAL);
				Assert.assertTrue(mb.isValid());
				Assert.assertTrue(mb.size() == TestVars.MEMORY_BLOCK_SIZE_100MB);
				System.out.println("Fixed Heap: " + MAX_HEAPS_ALLOCATED + " Size: " + heaps[i].size());
			}
		} 
        catch (HeapException e) {
			assert (MAX_HEAPS_ALLOCATED < TestVars.NUM_HEAPS);
		} 
        finally {
            assert (MAX_HEAPS_ALLOCATED < TestVars.NUM_HEAPS);
			for (int i = 0; i < MAX_HEAPS_ALLOCATED; i++) {
				heaps[i].close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSize() {
		heap = TestVars.createPersistentHeap();
		assert (heap.size() > 0);
		long heapSize = heap.size();
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		assert (heap.size() == heapSize);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSetGetRoots() {
		heap = TestVars.createPersistentHeap();
		Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.getRoot() == 0);
		heap.setRoot(100);
		Assert.assertTrue(heap.getRoot() == 100);
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		Assert.assertTrue(heap.getRoot() == 100);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testOpenMemoryBlockNonTransactionalWrongHandleWithLargeSizeOpenGrowableNoLimitHeap() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, TestVars.NON_TRANSACTIONAL);
		Assert.assertTrue(mb.isValid());
		mb.setLong(0, heap.size());
		long handle = mb.handle();
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		try {
			mb = heap.memoryBlockFromHandle(handle + 8);
		} 
        catch (HeapException e) {
			assert true;
		}
	}

	@Test
	public void testAllocateMemoryBlockWithRangeNull() {
		heap = TestVars.createPersistentHeap();
		try {
			heap.allocateMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, TestVars.TRANSACTIONAL, null);
		}   
        catch (IllegalArgumentException e) {
			assert true;
		}
	}

	@Test
	public void testAllocateCompactMemoryBlockWithRangeNull() {
		heap = TestVars.createPersistentHeap();
		try {
			heap.allocateCompactMemoryBlock(TestVars.MEMORY_BLOCK_SIZE, TestVars.NON_TRANSACTIONAL, null);
		} 
        catch (IllegalArgumentException e) {
			assert true;
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testOpenCompactMemoryBlockWrongLargeHandleOpenGrowableNoLimitHeap2() {
		heap = TestVars.createPersistentHeap();
		heap.close();
		heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentCompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(heap.size() - 1);
		Assert.assertTrue(mb.isValid());
	}
}
