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
import org.testng.SkipException;

@Test(singleThreaded = true)
public class MemoryPoolTests {
	MemoryPool pool = null;

	@BeforeMethod
	public void initialize() {
		pool = null;
	}

	@SuppressWarnings("deprecation")
	@AfterMethod
	public void testCleanup() {
		if (TestVars.ISDAX) {
			TestVars.daxCleanUp();
		}
		else TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
	}

	@Test
	public void testBasicMemoryPoolAPI() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
		pool = MemoryPool.openPool(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(pool.size() > 0);
		String string1 = "Pluto and Saturn"; // len = 16
		byte[] bytes1 = string1.getBytes(); 
		byte[] bytes2 = new byte[bytes1.length];
		byte[] bytes3 = new byte[bytes1.length];
		pool.setLong(0, 12345); 
		Assert.assertTrue(pool.getLong(0) == 12345);
		pool.setInt(8, 23456); 
		Assert.assertTrue(pool.getInt(8) == 23456);
		pool.setShort(12, (short)345); 
		Assert.assertTrue(pool.getShort(12) == 345);
		pool.setByte(14, (byte)45); 
		Assert.assertTrue(pool.getByte(14) == 45);
		pool.copyFromArray(bytes1, 0, 16, bytes1.length);
		pool.copyMemory(16, 32, bytes1.length); 
		pool.copyToArray(32, bytes2, 0, bytes1.length); 
		Assert.assertTrue(new String(bytes2).equals(string1));
		pool.setMemory(64, 100, (byte)123); for (long i = 64; i < 164; i++) 
		Assert.assertTrue(pool.getByte(i) == (byte)123);
		pool.close();
	}

}
