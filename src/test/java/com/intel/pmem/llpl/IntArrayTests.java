/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import com.intel.pmem.llpl.util.IntArray;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

@Test(singleThreaded = true)
public class IntArrayTests {
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
    public void testCreateIntArray() {
        long arrayHandle = heap.getRoot();
        if (arrayHandle == 0) {
            IntArray array = new IntArray(heap, 7);
            Assert.assertTrue(array != null);
        }
        else {
            Assert.fail("Handle should be zero for new heaps");
        }
    }

    @Test
    public void testCreateAndReopenArray() {
        long arrayHandle = heap.getRoot();
        IntArray array = new IntArray(heap, 7);
        heap.setRoot(array.handle());
        arrayHandle = heap.getRoot();
        array = IntArray.fromHandle(heap, arrayHandle);
        Assert.assertTrue(array != null);
    }

    @Test
    public void testArrayAccessorMethods() {
        IntArray array = new IntArray(heap, 7);
        long numValues = 7;
        for (long i = 0; i < numValues; i++) {
            int data = 1 << i;
            array.set(i, data);
            Assert.assertEquals(data, array.get(i));
        }
    }

    @Test
    public void testFailToOpenAfterFree() {
        IntArray array = new IntArray(heap, 7);
        array.set(0,543210);
        heap.setRoot(array.handle());
        // free the array to force an IllegalStateException on subsequent attempted write
        array.free();
        try {
            array.set(1,654321);
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testWriteToNegativeIndex() {
        IntArray array = new IntArray(heap, 7);
        try {
            array.set(-1,12345);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testReadFromNegativeIndex() {
        IntArray array = new IntArray(heap, 7);
        array.set(0,12345);
        try {
            array.get(-1);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testWriteToOutOfBoundsIndex() {
        IntArray array = new IntArray(heap, 7);
        try {
            array.set(10,12345);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testReadFromOutOfBoundsIndex() {
        IntArray array = new IntArray(heap, 7);
        array.set(0,12345);
        try {
            array.get(10);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testHashCode() {
        IntArray array1 = new IntArray(heap, 10);
        int hash1 = array1.hashCode();
        IntArray array2 = IntArray.fromHandle(heap, array1.handle());
        int hash2 = array2.hashCode();
        Assert.assertEquals(hash1, hash2);
    }

    @Test
    public void testEquality() {
        IntArray array1 = new IntArray(heap, 10);
        IntArray array2 = IntArray.fromHandle(heap, array1.handle());
        Assert.assertEquals(array1, array2);
    }
}
