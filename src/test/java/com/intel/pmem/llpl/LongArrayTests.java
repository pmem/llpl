/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import com.intel.pmem.llpl.util.LongArray;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

@Test(singleThreaded = true)
public class LongArrayTests {
    Heap heap = null;

    @BeforeMethod
    public void initialize() {
        heap = TestVars.createHeap();
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
    public void testCreateLongArray() {
        long arrayHandle = heap.getRoot();
        if (arrayHandle == 0) {
            LongArray array = new LongArray(heap, 7);
            Assert.assertTrue(array != null);
        }
        else {
            Assert.fail("Handle should be zero for new heaps");
        }
    }

    @Test
    public void testCreateAndReopenArray() {
        long arrayHandle = heap.getRoot();
        LongArray array = new LongArray(heap, 7);
        heap.setRoot(array.handle());
        arrayHandle = heap.getRoot();
        array = LongArray.fromHandle(heap, arrayHandle);
        Assert.assertTrue(array != null);
    }

    @Test
    public void testArrayAccessorMethods() {
        LongArray array = new LongArray(heap, 7);
        long numValues = 7;
        for (long i = 0; i < numValues; i++) {
            long data = 1L << i;
            array.set(i, data);
            Assert.assertEquals(data, array.get(i));
        }
    }

    @Test
    public void testFailToOpenAfterFree() {
        LongArray array = new LongArray(heap, 7);
        array.set(0,543210L);
        heap.setRoot(array.handle());
        // free the array to force an IllegalStateException on subsequent attempted write
        array.free();
        try {
            array.set(1,654321L);
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testWriteToNegativeIndex() {
        LongArray array = new LongArray(heap, 7);
        try {
            array.set(-1,12345L);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testReadFromNegativeIndex() {
        LongArray array = new LongArray(heap, 7);
        array.set(0,12345L);
        try {
            array.get(-1);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testWriteToOutOfBoundsIndex() {
        LongArray array = new LongArray(heap, 7);
        try {
            array.set(10,12345L);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testReadFromOutOfBoundsIndex() {
        LongArray array = new LongArray(heap, 7);
        array.set(0,12345L);
        try {
            array.get(10);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            assert true;
        }
    }

    // hashCode
    @Test
    public void testHashCode() {
        LongArray array1 = new LongArray(heap, 10);
        int hash1 = array1.hashCode();
        LongArray array2 = LongArray.fromHandle(heap, array1.handle());
        int hash2 = array2.hashCode();
        Assert.assertEquals(hash1, hash2);
    }

    // equals
    @Test
    public void testEquality() {
        LongArray array1 = new LongArray(heap, 10);
        LongArray array2 = LongArray.fromHandle(heap, array1.handle());
        Assert.assertEquals(array1, array2);
    }
}
