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

	@Test
	public void testCompactNew() {
		heap = TestVars.createTransactionalHeap();
        assert(heap.registerAllocationSize(500, true));
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(500);
        assert(AnyHeap.getUsableSize(mb) == 500);
	}

	@Test
	public void testCompactExisting() {
		heap = TestVars.createTransactionalHeap();
        assert(heap.registerAllocationSize(500, true));
        assert(heap.registerAllocationSize(500, true));
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(500);
        assert(AnyHeap.getUsableSize(mb) == 500);
	}
}
