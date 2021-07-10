/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import com.intel.pmem.llpl.util.LongLinkedList;

import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class LongLinkedListTransactionalTests {
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

    // List creation
    @Test
    public void testCreateLongLinkedList() {
        LongLinkedList list = new LongLinkedList(heap);
        Assert.assertTrue(list != null);
    }

    @Test
    public void testListInitialSize() {
        LongLinkedList list = new LongLinkedList(heap);
        Assert.assertTrue(list != null);
        Assert.assertEquals(list.size(), 0L);
    }

    @Test
    public void testHandle() {
        LongLinkedList list = new LongLinkedList(heap);
        Assert.assertTrue(list.handle() != 0);
    }

    @Test
    public void testReopen() {
        LongLinkedList list = new LongLinkedList(heap);
        heap.setRoot(list.handle());
        // reopen list
        long listHandle = heap.getRoot();
        list = LongLinkedList.fromHandle(heap, listHandle);
        Assert.assertTrue(list != null);
    }

    // add
    @Test
    public void testAddToEmpty() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        Assert.assertEquals(list.size(), 1L);
        Assert.assertEquals(list.get(0), 42L);
    }

    @Test
    public void testAddToNonEmptyAtLast() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        Assert.assertEquals(list.size(), 2L);
        Assert.assertEquals(list.get(1), 43L);
    }

    @Test
    public void testAddNonEmptyAtFirst() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(0, 43L);
        Assert.assertEquals(list.size(), 2L);
        Assert.assertEquals(list.get(0), 43L);
        Assert.assertEquals(list.get(1), 42L);
    }

    @Test
    public void testAddToNegativeIndex() {
        LongLinkedList list = new LongLinkedList(heap);
        try {
            list.add(-1,12345L);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddToIndexOutOfBounds() {
        LongLinkedList list = new LongLinkedList(heap);
        try {
            list.add(1,12345L);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddToClearedList() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.clear();
        list.add(0, 43L);
        Assert.assertEquals(list.size(), 1L);
    }

    // addFirst
    @Test
    public void testAddFirstEmptyList() {
        LongLinkedList list = new LongLinkedList(heap);
        list.addFirst(42L);
        Assert.assertEquals(list.size(), 1L);
        Assert.assertEquals(list.get(0), 42L);
    }

    @Test
    public void testAddFirstToNonEmpty() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.addFirst(43L);
        Assert.assertEquals(list.size(), 2L);
        Assert.assertEquals(list.get(0), 43L);
    }

    @Test
    public void testAddFirstToClearedList() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.clear();
        list.addFirst(43L);
        Assert.assertEquals(list.size(), 1L);
    }

    // get
    @Test
    public void testGetEmptyList() {
        LongLinkedList list = new LongLinkedList(heap);
        Assert.assertEquals(list.size(), 0);
        try {
            long data = list.get(0);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetOnlyValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        Assert.assertEquals(list.get(0), 42L);
    }

    @Test
    public void testGetMiddleValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        list.add(2, 44L);
        Assert.assertEquals(list.get(1), 43L);
    }

    @Test
    public void testGetEndValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        list.add(2, 44L);
        Assert.assertEquals(list.get(2), 44L);
    }

    @Test
    public void testGetFromClearedList() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,543210L);
        list.clear();
        Assert.assertEquals(list.size(), 0);
        try {
            long data = list.get(0);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetNegativeIndex() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        try {
            list.get(-1);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetIndexOutOfBounds() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        try {
            list.get(10);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    // getFirst
    @Test
    public void testGetFirstEmptyList() {
        LongLinkedList list = new LongLinkedList(heap);
        try {
            list.getFirst();
            Assert.fail("NoSuchElementException wasn't thrown");
        } catch(java.util.NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetFirstOnlyValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        Assert.assertEquals(list.getFirst(), 42L);
    }

    @Test
    public void testGetFirstOfMultipleValues() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        list.add(2, 44L);
        Assert.assertEquals(list.getFirst(), 42L);
    }

    @Test
    public void testGetFirstFromClearedList() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 12345L);
        list.clear();
        try {
            list.getFirst();
            Assert.fail("NoSuchElementException wasn't thrown");
        } catch(java.util.NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    // set
    @Test
    public void testSetEmptyList() {
        LongLinkedList list = new LongLinkedList(heap);
        try {
            long oldValue = list.set(0, 54321L);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testSetOnlyValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        long oldValue = list.set(0, 54321L);
        Assert.assertEquals(oldValue, 42L);
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0), 54321L);
    }

    @Test
    public void testSetFirstValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        list.add(2, 44L);
        long oldValue = list.set(0, 54321L);
        Assert.assertEquals(oldValue, 42L);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.get(0), 54321L);
    }

    @Test
    public void testSetMiddleValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        list.add(2, 44L);
        long oldValue = list.set(1, 54321L);
        Assert.assertEquals(oldValue, 43L);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.get(1), 54321L);
    }

    @Test
    public void testSetLastValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        list.add(2, 44L);
        long oldValue = list.set(2, 54321L);
        Assert.assertEquals(oldValue, 44L);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.get(2), 54321L);
    }

    @Test
    public void testSetNegativeIndex() {
        LongLinkedList list = new LongLinkedList(heap);
        try {
            long oldValue = list.set(-1,12345L);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testSetIndexOutOfBounds() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        try {
            long oldValue = list.set(10,54321L);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testSetClearedList() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        list.clear();
        try {
            long oldValue = list.set(10,54321L);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    // remove
    @Test
    public void testRemoveOnlyValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        long data = list.remove(0);
        Assert.assertEquals(list.size(), 0L);
        Assert.assertEquals(data, 42L);
    }

    @Test
    public void testRemoveMiddleValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        list.add(2, 44L);
        long data = list.remove(1);
        Assert.assertEquals(list.size(), 2L);
        Assert.assertEquals(data, 43L);
    }

    @Test
    public void testRemoveLastValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        list.add(2, 44L);
        long data = list.remove(2);
        Assert.assertEquals(list.size(), 2L);
        Assert.assertEquals(data, 44L);
    }

    @Test
    public void testRemoveNegativeIndex() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        try {
            list.remove(-1);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testRemoveIndexOutOfBounds() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        try {
            list.remove(1);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testRemoveFromClearedList() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        list.clear();
        try {
            list.remove(0);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    // removeFirst
    @Test
    public void testRemoveFirstOnlyValue() {
        LongLinkedList list = new LongLinkedList(heap);
        list.addFirst(42L);
        long data = list.removeFirst();
        Assert.assertEquals(list.size(), 0L);
        Assert.assertEquals(data, 42L);
    }

    @Test
    public void testRemoveFirstMultipleValues() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        list.add(1, 43L);
        list.add(2, 44L);
        long data = list.removeFirst();
        Assert.assertEquals(list.size(), 2L);
        Assert.assertEquals(data, 42L);
    }

    @Test
    public void testRemoveFirstFromClearedList() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        list.clear();
        try {
            list.removeFirst();
            Assert.fail("NoSuchElementException wasn't thrown");
        } catch(java.util.NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    // iterator
    @Test
    public void testIteration() {
        LongLinkedList list = new LongLinkedList(heap);
        long numElements = 8;
        for (long i = 0; i < numElements; i++) {
            long data = i;
            list.add(i, data);
        }
        Iterator<Long> litr = list.iterator();
        long i = 0;
        while(litr.hasNext()) {
            Assert.assertEquals(litr.next().longValue(), i);
            i++;
        }
    }

    @Test
    public void testEmptyIterator() {
        LongLinkedList list = new LongLinkedList(heap);
        Iterator<Long> litr = list.iterator();
        Assert.assertFalse(litr.hasNext());
    }

    @Test
    public void testEmptyIteratorAfterRemove() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0, 42L);
        Iterator<Long> litr = list.iterator();
        list.remove(0);
        // re-create the iterator since we just modified the list
        litr = list.iterator();
        Assert.assertFalse(litr.hasNext());
        try {
           long data = litr.next();
           Assert.fail("NoSuchElementException wasn't thrown");
        } catch(java.util.NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    // clear
    @Test
    public void testClearOne() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,543210L);
        list.clear();
        Assert.assertEquals(list.size(), 0);
    }

    @Test
    public void testClearMultiple() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,543210L);
        list.add(0,654321L);
        list.clear();
        Assert.assertEquals(list.size(), 0);
    }

    // free
    @Test
    public void testFailToOpenAfterFree() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,543210L);
        // free the list to force an IllegalStateException on subsequent attempted write
        list.free();
        try {
            list.add(1,654321L);
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testClearAfterFree() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        list.free();
        try {
            list.clear();
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddAfterFree() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        list.free();
        try {
            list.add(0,54321L);
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddFirstAfterFree() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        list.free();
        try {
            list.addFirst(54321L);
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testSetAfterFree() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        list.free();
        try {
            list.set(0,54321L);
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetAfterFree() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        list.free();
        try {
            list.get(0);
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetFirstAfterFree() {
        LongLinkedList list = new LongLinkedList(heap);
        list.add(0,12345L);
        list.free();
        try {
            list.getFirst();
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testHandleAfterFree() {
        LongLinkedList list = new LongLinkedList(heap);
        list.free();
        try {
            long handle = list.handle();
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testDoubleFree() {
        LongLinkedList list = new LongLinkedList(heap);
        list.free();
        try {
            list.free();
            Assert.fail("IllegalStateException wasn't thrown");
        } catch(IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testClearAfterClear() {
        LongLinkedList list = new LongLinkedList(heap);
        list.clear();
        list.clear();
        Assert.assertTrue(true);
    }

    // hashCode
    @Test
    public void testHashCode() {
        LongLinkedList list1 = new LongLinkedList(heap);
        int hash1 = list1.hashCode();
        LongLinkedList list2 = LongLinkedList.fromHandle(heap, list1.handle());
        int hash2 = list2.hashCode();
        Assert.assertEquals(hash1, hash2);
    }

    // equals
    @Test
    public void testEquality() {
        LongLinkedList list1 = new LongLinkedList(heap);
        LongLinkedList list2 = LongLinkedList.fromHandle(heap, list1.handle());
        Assert.assertEquals(list1, list2);
    }
}
