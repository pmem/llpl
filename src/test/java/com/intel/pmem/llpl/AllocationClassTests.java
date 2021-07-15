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
import java.util.TreeSet;
import java.util.Random;
import com.intel.pmem.llpl.util.LongART;

@SuppressWarnings("deprecation")
@Test(singleThreaded = true)
public class AllocationClassTests {
	TransactionalHeap heap = null;

	@BeforeMethod
	public void initialize() {
		heap = null;
	}

	@AfterMethod
	public void testCleanup() {
		if (heap != null)
			heap.close();
		if (TestVars.ISDAX) {
			TestVars.daxCleanUp();
		}
		else TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
	}

    static TreeSet<Integer> getBuiltIns(TransactionalHeap heap) {
        TreeSet<Integer> set = new TreeSet();
        for (int i = 8; i < 128; i += 8) {
            if(AnyHeap.getUsableSize(heap.allocateCompactMemoryBlock(i)) == i) {
                set.add(i); // System.out.println("Built-in allocation class: "+ i +" available");
            }
        } 
        return set;
    }

    static TreeSet<Integer> registerClasses(TransactionalHeap heap, int count) {
        TreeSet<Integer> set = new TreeSet();
        Random rnd = new Random(918273645);
        int size;
        while(true) {
            if(heap.registerAllocationSize(size = rnd.nextInt(10240), true)) {
                set.add(size); // System.out.println("Custom allocation class: "+ size +" available");
                if (set.size() == count) break;
            }
        } 
        return set;
    }

    @Test
    public void testCustoms() {
		heap = TestVars.createTransactionalHeap();
        TreeSet<Integer> set = registerClasses(heap, 10);
        for (Integer size : set) {
            Assert.assertEquals(AnyHeap.getUsableSize(heap.allocateCompactMemoryBlock(size)), size.intValue());
        }
    }

    @Test
    public void testBuiltIns() {
		heap = TestVars.createTransactionalHeap();
        TreeSet<Integer> set = getBuiltIns(heap);
        int i = 1;
        for (Integer size : set) {
            for (; i <= size; i++) {
                Assert.assertEquals(AnyHeap.getUsableSize(heap.allocateCompactMemoryBlock(i)), size.intValue());
            }
        }
    }

    @Test
    public void testBuiltInsWithCustoms() {
		heap = TestVars.createTransactionalHeap();
        TreeSet<Integer> set = getBuiltIns(heap);
        registerClasses(heap, 10);
        int i = 1;
        for (Integer size : set) {
            for (; i <= size; i++) {
                Assert.assertEquals(AnyHeap.getUsableSize(heap.allocateCompactMemoryBlock(i)), size.intValue(), "current size is "+i);
            }
        }
    }
}
