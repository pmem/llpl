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
import java.util.Arrays;

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
		if (TestVars.ISDAX) pool = MemoryPool.createPool(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 0L);
		else pool = MemoryPool.createPool(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
        Assert.assertTrue(pool != null);
		if (TestVars.ISDAX) Assert.assertTrue(pool.size() > 0);
		else Assert.assertEquals(TestVars.HEAP_SIZE, pool.size());
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
		pool.copyFromByteArray(bytes1, 0, 16, bytes1.length);
		pool.copyMemory(16, 32, bytes1.length); 
		pool.copyToByteArray(32, bytes2, 0, bytes1.length); 
		Assert.assertTrue(new String(bytes2).equals(string1));
		pool.setMemory(64, 100, (byte)123); for (long i = 64; i < 164; i++) 
		Assert.assertTrue(pool.getByte(i) == (byte)123);
		pool.close();
        //reopen
		pool = MemoryPool.openPool(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
       	Assert.assertTrue(pool != null);
		if (TestVars.ISDAX) Assert.assertTrue(pool.size() > 0);
		else Assert.assertEquals(TestVars.HEAP_SIZE, pool.size());
		Assert.assertTrue(pool.getLong(0) == 12345);
		pool.setInt(8, 23456); 
		Assert.assertTrue(pool.getInt(8) == 23456);
		pool.setShort(12, (short)345); 
		Assert.assertTrue(pool.getShort(12) == 345);
		pool.setByte(14, (byte)45); 
		Assert.assertTrue(pool.getByte(14) == 45);
		pool.copyFromByteArray(bytes1, 0, 16, bytes1.length);
		pool.copyMemory(16, 32, bytes1.length); 
		pool.copyToByteArray(32, bytes2, 0, bytes1.length); 
		Assert.assertTrue(new String(bytes2).equals(string1));
		pool.setMemory(64, 100, (byte)123); for (long i = 64; i < 164; i++) 
		Assert.assertTrue(pool.getByte(i) == (byte)123);
		pool.close();
	}

	@Test
	public void testNTMemoryPoolAPI() {
		if (TestVars.ISDAX) pool = MemoryPool.createPool(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, 0L);
		else pool = MemoryPool.createPool(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME, TestVars.HEAP_SIZE);
        Assert.assertTrue(pool != null);
		if (TestVars.ISDAX) Assert.assertTrue(pool.size() > 0);
		else Assert.assertEquals(TestVars.HEAP_SIZE, pool.size());
		String string1 = "Pluto and Saturn"; // len = 16
		byte[] bytes1 = string1.getBytes(); 
		byte[] bytes2 = new byte[bytes1.length];
		byte[] bytes3 = new byte[bytes1.length];
        //long[] lbytes = new long[] {123456L, 345678L, 5678910L};
		pool.setLong(0, 12345); 
		Assert.assertTrue(pool.getLong(0) == 12345);
		pool.setInt(8, 23456); 
		Assert.assertTrue(pool.getInt(8) == 23456);
		pool.setShort(12, (short)345); 
		Assert.assertTrue(pool.getShort(12) == 345);
		pool.setByte(14, (byte)45); 
		Assert.assertTrue(pool.getByte(14) == 45);
		pool.copyFromByteArrayNT(bytes1, 0, 16, bytes1.length);
		pool.copyMemoryNT(16, 32, bytes1.length); 
		pool.copyToByteArray(32, bytes2, 0, bytes1.length); 
		Assert.assertTrue(new String(bytes2).equals(string1));
		pool.setMemoryNT(64, 100, (byte)123); for (long i = 64; i < 164; i++) 
		Assert.assertTrue(pool.getByte(i) == (byte)123);
        //pool.copyFromLongArrayNT(lbytes, 128, lbytes.length);
        //Assert.assertEquals(lbytes[0], pool.getLong(128));
        //Assert.assertEquals(lbytes[1], pool.getLong(128 + 8));
        //Assert.assertEquals(lbytes[2], pool.getLong(128 + 16));
		pool.close();
        //reopen
		pool = MemoryPool.openPool(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
       	Assert.assertTrue(pool != null);
		if (TestVars.ISDAX) Assert.assertTrue(pool.size() > 0);
		else Assert.assertEquals(TestVars.HEAP_SIZE, pool.size());
		Assert.assertTrue(pool.getLong(0) == 12345);
		pool.setInt(8, 23456); 
		Assert.assertTrue(pool.getInt(8) == 23456);
		pool.setShort(12, (short)345); 
		Assert.assertTrue(pool.getShort(12) == 345);
		pool.setByte(14, (byte)45); 
		Assert.assertTrue(pool.getByte(14) == 45);
		pool.copyFromByteArrayNT(bytes1, 0, 16, bytes1.length);
		pool.copyMemoryNT(16, 32, bytes1.length); 
		pool.copyToByteArray(32, bytes2, 0, bytes1.length); 
		Assert.assertTrue(new String(bytes2).equals(string1));
		pool.setMemoryNT(64, 100, (byte)123); for (long i = 64; i < 164; i++) 
		Assert.assertTrue(pool.getByte(i) == (byte)123);
		pool.close();
	}
}
