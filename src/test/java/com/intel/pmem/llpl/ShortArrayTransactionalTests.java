/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import com.intel.pmem.llpl.*;
import com.intel.pmem.llpl.util.ShortArray;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

@Test(singleThreaded = true)
public class ShortArrayTransactionalTests {
    TransactionalHeap heap = null;

    @BeforeMethod
    public void initialize() {
        heap = TestVars.createTransactionalHeap();
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
    public void testCreateShortArray() {
        long arrayHandle = heap.getRoot();
        if (arrayHandle == 0) {
            ShortArray array = new ShortArray(heap, 7);
            Assert.assertTrue(array != null);
        }
        else {
            //TODO: Is this correct? It's essentially an assert(arrayHandle == 0)
            Assert.fail("Handle should be zero for new heaps");
        }
    }

    @Test
    public void testCreateAndReopenArray() {
        long arrayHandle = heap.getRoot();
        // TODO: Can we assert arrayHandle == 0 here?
        Assert.assertEquals(arrayHandle, 0);
        ShortArray array = new ShortArray(heap, 7);
        Assert.assertTrue(array != null);
        heap.setRoot(array.handle());
        arrayHandle = heap.getRoot();
        array = ShortArray.fromHandle(heap, arrayHandle);
        Assert.assertTrue(array != null);
    }

    @Test
    public void testArrayAccessorMethods() {
        ShortArray array = new ShortArray(heap, 7);
        Assert.assertTrue(array != null);
        long numValues = 7;
        for (long i = 0; i < numValues; i++) {
            short data = (short)(1 << i);
            array.set(i, data);
            Assert.assertEquals(data, array.get(i));
        }
    }

    @Test
    public void testFailToOpenAfterFree() {
        ShortArray array = new ShortArray(heap, 7);
        array.set(0,(short)54321);
        heap.setRoot(array.handle());
        // free the array to force an IllegalStateException on subsequent attempted write
        array.free();
        try {
            array.set(1,(short)54321);
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testWriteToNegativeIndex() {
        ShortArray array = new ShortArray(heap, 7);
        try {
            array.set(-1,(short)12345);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testReadFromNegativeIndex() {
        ShortArray array = new ShortArray(heap, 7);
        array.set(0,(short)12345);
        try {
            array.get(-1);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testWriteToOutOfBoundsIndex() {
        ShortArray array = new ShortArray(heap, 7);
        try {
            array.set(10,(short)12345);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testReadFromOutOfBoundsIndex() {
        ShortArray array = new ShortArray(heap, 7);
        array.set(0,(short)12345);
        try {
            array.get(10);
            Assert.fail("ArrayIndexOutOfBoundsException wasn't thrown");
        } catch(ArrayIndexOutOfBoundsException e) {
            assert true;
        }
    }

}
