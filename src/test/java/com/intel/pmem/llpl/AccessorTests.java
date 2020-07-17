/*
 * Copyright (C) 2020 Intel Corporation
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
public class AccessorTests {
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
    public void testSetMemoryBlockHandle() {
        heap = TestVars.createHeap();
        MemoryBlock[] blocks = new MemoryBlock[100];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = heap.allocateMemoryBlock(16, false);
            blocks[i].setLong(7, blocks[i].handle());
        }
        Accessor acc = heap.createAccessor();
        for (int i = 0; i < blocks.length; i++) {
            acc.handle(blocks[i].handle());
            Assert.assertEquals(acc.getLong(7), blocks[i].handle());
        }
    }

    @Test
    public void testSetCompactMemoryBlockHandle() {
        heap = TestVars.createHeap();
        CompactMemoryBlock[] blocks = new CompactMemoryBlock[100];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = heap.allocateCompactMemoryBlock(16, false);
            blocks[i].setLong(7, blocks[i].handle());
        }
        CompactAccessor acc = heap.createCompactAccessor();
        for (int i = 0; i < blocks.length; i++) {
            acc.handle(blocks[i].handle());
            Assert.assertEquals(acc.getLong(7), blocks[i].handle());
        }
    }

    @Test
    public void testSetAllocateMemory() {
        heap = TestVars.createHeap();
        long[] handles = new long[100];
        Accessor acc = heap.createAccessor();
        for (int i = 0; i < handles.length; i++) {
            handles[i] = heap.allocateMemory(16, false);
            acc.handle(handles[i]);
            acc.setLong(7, handles[i]);
        }
        MemoryBlock mb;
        for (int i = 0; i < handles.length; i++) {
            acc.handle(handles[i]);
            mb = heap.memoryBlockFromHandle(handles[i]);
            Assert.assertEquals(acc.getLong(7), mb.getLong(7));
        }
    }

    @Test
    public void testSetAllocateCompactMemory() {
        heap = TestVars.createHeap();
        long[] handles = new long[100];
        CompactAccessor acc = heap.createCompactAccessor();
        for (int i = 0; i < handles.length; i++) {
            handles[i] = heap.allocateCompactMemory(16, false);
            acc.handle(handles[i]);
            acc.setLong(7, handles[i]);
        }
        CompactMemoryBlock mb;
        for (int i = 0; i < handles.length; i++) {
            acc.handle(handles[i]);
            mb = heap.compactMemoryBlockFromHandle(handles[i]);
            Assert.assertEquals(acc.getLong(7), mb.getLong(7));
        }
    }

    @Test
    public void testResetAccessor() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        Accessor acc = heap.createAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after reset
        acc.resetHandle();
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testResetCompactAccessor() {
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        CompactAccessor acc = heap.createCompactAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after reset
        acc.resetHandle();
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testAccessorFreeMemory() {
        heap = TestVars.createHeap();
        boolean transactional = true;
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        Accessor acc = heap.createAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after freeMemory
        acc.freeMemory(transactional);
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testAccessorFreeMemoryCompact() {
        heap = TestVars.createHeap();
        boolean transactional = true;
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        // Accessor is not valid immediately after instantiation
        CompactAccessor acc = heap.createCompactAccessor();
        Assert.assertFalse(acc.isValid());
        // Accessor becomes valid after set handle
        acc.handle(mb.handle());
        Assert.assertTrue(acc.isValid());
        // Accssor becomes invalid after freeMemory
        acc.freeMemory(transactional);
        Assert.assertFalse(acc.isValid());
    }

    @Test
    public void testCompactCheckBoundsNegativeOffsetWIP() {
        long offset = -1; // use an invalid offset
        long length = TestVars.TOTAL_SIZE;
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
        CompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mbNew.handle());
        try {
            mb.copyFrom(acc, offset, offset, length);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

}

