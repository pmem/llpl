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
public class HeapAllocationTests {

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
    public void testHeapMemoryAllocation(){
        heap = TestVars.createHeap();
        long handle = heap.allocateMemory(1024);
        MemoryBlock mb = heap.memoryBlockFromHandle(handle);
        mb.setLong(0,42);
        boolean success = true;
        Assert.assertTrue(success);
    }

    @Test
    public void testHeapCompactMemoryAllocation(){
        heap = TestVars.createHeap();
        long handle = heap.allocateCompactMemory(1024);
        CompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(handle);
        mb.setLong(0,42);
        boolean success = true;
        Assert.assertTrue(success);
    }

    @Test
    public void testHeapMemoryBlockAllocation(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        mb.setLong(0,42);
        boolean success = true;
        Assert.assertTrue(success);
    }

    @Test
    public void testHeapCompactMemoryBlockAllocation(){
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        mb.setLong(0,42);
        boolean success = true;
        Assert.assertTrue(success);
    }

    @Test
    public void testHeapMemoryBlockAllocationRange(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, (Range range) -> {
            long offset = 0;
            long data = 42;
            range.setLong(offset, data);
        });
        long data = mb.getLong(0);
        Assert.assertEquals(data,42L);
    }

    @Test
    public void testHeapCompactMemoryBlockAllocationRange(){
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, (Range range) -> {
            long offset = 0;
            long data = 42;
            range.setLong(offset, data);
        });
        long data = mb.getLong(0);
        Assert.assertEquals(data,42L);
    }

}

