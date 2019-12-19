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
public class PersistentMemoryBlock2Tests {

	PersistentHeap heap = null;	
	PersistentHeap heapNew = null;

	@BeforeMethod
	public void initialze() {
		heap = null;
		heapNew = null;
	} 

	@SuppressWarnings("deprecation")
	@AfterMethod
	public void testCleanup() {
		if(heap != null)
			heap.close();
		if(heapNew != null)
			heapNew.close();
		if (TestVars.ISDAX) {
            TestVars.daxCleanUp();
        }
        else TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
	}

	@Test
	public void testMemBlockWriteByte() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setByte(12, (byte)128);
		Assert.assertEquals(mb.getByte(12), (byte)128);
	}

	@Test
	public void testMemBlockWriteByteZeroOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setByte(0, (byte)128);
		Assert.assertEquals(mb.getByte(0), (byte)128);
	}

	@Test
	public void testMemBlockWriteByteMaxOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setByte(1023, (byte)128);
		Assert.assertEquals(mb.getByte(1023), (byte)128);
	}

	@Test
	public void testMemBlockWriteByteMaxOffsetNegative() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.setByte(1024, (byte)128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockWriteInt() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setInt(12, 128);
		Assert.assertEquals(mb.getInt(12), 128);
	}

	@Test
	public void testMemBlockWriteIntZeroOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setInt(0, 128);
		Assert.assertEquals(mb.getInt(0), 128);
	}

	@Test
	public void testMemBlockWriteIntMaxOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.setInt(1023, 128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockWriteIntMaxOffsetNegative() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.setInt(1024, 128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockWriteShort() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setShort(12, (short)128);
		Assert.assertEquals(mb.getShort(12), (short)128);
	}

	@Test
	public void testMemBlockWriteShortZeroOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setShort(0, (short)128);
		Assert.assertEquals(mb.getShort(0), (short)128);
	}

	@Test
	public void testMemBlockWriteShortMaxOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.setShort(1023, (short)128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockWriteShortMaxOffsetNegative() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.setShort(1024, (short)128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockWriteLong() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setLong(12, 128);
		Assert.assertEquals(mb.getLong(12), 128);
	}

	@Test
	public void testMemBlockWriteLongZeroOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setLong(0, 128);
		Assert.assertEquals(mb.getLong(0), 128);
	}

	@Test
	public void testMemBlockWriteLongMaxOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.setLong(1023, 128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockWriteLongMaxOffsetNegative() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.setLong(1024, 128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockTxWriteByte() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.transactionalSetByte(12, (byte)128);
		Assert.assertEquals(mb.getByte(12), (byte)128);
	}

	@Test
	public void testMemBlockTxWriteByteZeroOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.transactionalSetByte(0, (byte)128);
		Assert.assertEquals(mb.getByte(0), (byte)128);
	}

	@Test
	public void testMemBlockTxWriteByteMaxOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.transactionalSetByte(1023, (byte)128);
		Assert.assertEquals(mb.getByte(1023), (byte)128);
	}

	@Test
	public void testMemBlockTxWriteByteMaxOffsetNegative() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.transactionalSetByte(1024, (byte)128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockTxWriteInt() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.transactionalSetInt(12, 128);
		Assert.assertEquals(mb.getInt(12), 128);
	}

	@Test
	public void testMemBlockTxWriteIntZeroOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.transactionalSetInt(0, 128);
		Assert.assertEquals(mb.getInt(0), 128);
	}

	@Test
	public void testMemBlockTxWriteIntMaxOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.transactionalSetInt(1023, 128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockTxWriteIntMaxOffsetNegative() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.transactionalSetInt(1024, 128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockTxWriteShort() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.transactionalSetShort(12, (short)128);
		Assert.assertEquals(mb.getShort(12), (short)128);
	}

	@Test
	public void testMemBlockTxWriteShortZeroOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.transactionalSetShort(0, (short)128);
		Assert.assertEquals(mb.getShort(0), (short)128);
	}

	@Test
	public void testMemBlockTxWriteShortMaxOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.transactionalSetShort(1023, (short)128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockTxWriteShortMaxOffsetNegative() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.transactionalSetShort(1024, (short)128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockTxWriteLong() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.transactionalSetLong(12, 128);
		Assert.assertEquals(mb.getLong(12), 128);
	}

	@Test
	public void testMemBlockTxWriteLongZeroOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.transactionalSetLong(0, 128);
		Assert.assertEquals(mb.getLong(0), 128);
	}

	@Test
	public void testMemBlockTxWriteLongMaxOffset() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.transactionalSetLong(1023, 128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

	@Test
	public void testMemBlockTxWriteLongMaxOffsetNegative() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		try {
			mb.transactionalSetLong(1024, 128);
			Assert.fail("IndexOutOfBoundsException not thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
	}

    @Test
    public void testSetMemoryValidZero() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
		mb.setMemory((byte)0, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)0);
    }

    @Test
    public void testSetMemoryValid() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryValidMax() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 0, 1024);
		long end = 0 + 1024;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryInvalidMax() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
        try {
            mb.setMemory((byte)-1, 0, 1025);
            Assert.fail("IndexOutOfBoundsException not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testSetMemoryInvalidMax2() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
        try {
            mb.setMemory((byte)-1, 0, 2000);
            Assert.fail("IndexOutOfBoundsException not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testSetMemoryInvalidMin() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
        try {
            mb.setMemory((byte)-1, -1, 30);
            Assert.fail("IndexOutOfBoundsException not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testSetMemoryCompactValidZero() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)0, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)0);
    }

    @Test
    public void testSetMemoryCompactValid() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryCompactValidMax() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 0, 1023);
		long end = 0 + 1023;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryCompactInvalidMax() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 0, 1024);
		long end = 0 + 1024;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryCompactInvalidMax2() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 0, 2000);
		long end = 0 + 2000;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryCompactInvalidMin() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
        try {
            mb.setMemory((byte)-1, -1, 30);
            Assert.fail("IndexOutOfBoundsException not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testSetMemoryTxValidZero() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
		mb.transactionalSetMemory((byte)0, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)0);
    }

    @Test
    public void testSetMemoryTxValid() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
		mb.transactionalSetMemory((byte)-1, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryTxValidMax() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
		mb.transactionalSetMemory((byte)-1, 0, 1024);
		long end = 0 + 1024;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryTxInvalidMax() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
        try {
            mb.transactionalSetMemory((byte)-1, 0, 1025);
            Assert.fail("IndexOutOfBoundsException not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testSetMemoryTxInvalidMax2() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
        try {
            mb.transactionalSetMemory((byte)-1, 0, 2000);
            Assert.fail("IndexOutOfBoundsException not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testSetMemoryTxInvalidMin() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
        try {
            mb.transactionalSetMemory((byte)-1, -1, 30);
            Assert.fail("IndexOutOfBoundsException not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testSetMemoryTxCompactValidZero() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.transactionalSetMemory((byte)0, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)0);
    }

    @Test
    public void testSetMemoryTxCompactValid() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.transactionalSetMemory((byte)-1, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryTxCompactValidMax() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.transactionalSetMemory((byte)-1, 0, 1023);
		long end = 0 + 1023;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryTxCompactInvalidMax() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.transactionalSetMemory((byte)-1, 0, 1024);
		long end = 0 + 1024;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryTxCompactInvalidMax2() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.transactionalSetMemory((byte)-1, 0, 2000);
		long end = 0 + 2000;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryTxCompactInvalidMin() {
		heap = TestVars.createPersistentHeap();
        Assert.assertTrue(PersistentHeap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
        try {
            mb.transactionalSetMemory((byte)-1, -1, 30);
            Assert.fail("IndexOutOfBoundsException not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

	@Test
	public void testCopyFromMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromCompactMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromCompactMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromPersistentMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromPersistentMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromPersistentCompactMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromPersistentCompactMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromTransactionalMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromTransactionalMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromTransactionalCompactMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromTransactionalCompactMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromCompactMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromCompactMemBlkToCompactkPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromPersistentMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromPersistentMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromPersistentCompactMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromPersistentCompactMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromTransactionalMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromTransactionalMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromTransactionalCompactMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testCopyFromTransactionalCompactMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.copyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromCompactMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromCompactMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromPersistentMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromPersistentMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromPersistentCompactMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromPersistentCompactMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromTransactionalMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromTransactionalMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromTransactionalCompactMemoryBlockAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromTransactionalCompactMemoryBlockPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentMemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromCompactMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromCompactMemBlkToCompactkPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Heap heap = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromPersistentMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromPersistentMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromPersistentCompactMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromPersistentCompactMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		PersistentHeap heap = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromTransactionalMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromTransactionalMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(1024);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromTransactionalCompactMemBlkToCompactAll() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		mb.setMemory((byte)-1, 0, 1024);
		long endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1024);
		endOffset = 0 + 1024;
		for(int i = 0; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

	@Test
	public void testTxCopyFromTransactionalCompactMemBlkToCompactPart() {
		if (TestVars.ISDAX) throw new SkipException("Test not valid in DAX mode");
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		TransactionalHeap heap = TransactionalHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TransactionalCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
		mb.setMemory((byte)-1, 56, 100);
		long endOffset = 56 + 100;
		for(int i = 56; i < endOffset; i++)
	        Assert.assertEquals(mb.getByte(i), (byte) -1);
        Assert.assertTrue (TestVars.createFolder(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1"));
		heapNew = PersistentHeap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		PersistentCompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
		mbNew.transactionalCopyFromMemoryBlock(mb, 56, 12, 100);
		endOffset = 12 + 100;
		for(int i = 12; i < endOffset; i++)
			Assert.assertEquals(mbNew.getByte(i), (byte) -1);
		try{} 
        finally {
		    heap.close();
		}
    }

    @Test
    public void testCopyFromMemoryBlockNegativeOffset1() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, -1, 0, 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemoryBlockNegativeOffset2() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, -1, 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemoryBlockZeroLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, -1, 0);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemoryBlockNegativeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, 0, -1);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemoryBlockLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, 0, 1025);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemoryBlockVeryLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, 0, 10L * 1024 * 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemBlkToCompactNegativeOffset1() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, -1, 0, 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemBlkToCompactNegativeOffset2() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, -1, 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemBlkToCompactZeroLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, -1, 0);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemBlkToCompactNegativeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, 0, -1);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemBlkToCompactLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, 0, 1025);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testCopyFromMemBlkToCompactVeryLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, 0, 10L * 1024 * 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemoryBlockNegativeOffset1() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, -1, 0, 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemoryBlockNegativeOffset2() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, -1, 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemoryBlockZeroLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, -1, 0);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemoryBlockNegativeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, -1);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemoryBlockLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1025);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemoryBlockVeryLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentMemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 10L * 1024 * 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemBlkToCompactNegativeOffset1() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, -1, 0, 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemBlkToCompactNegativeOffset2() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, -1, 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemBlkToCompactZeroLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, -1, 0);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemBlkToCompactNegativeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, -1);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemBlkToCompactLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 1025);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testTxCopyFromMemBlkToCompactVeryLargeLength() {
		heap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        PersistentCompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.transactionalCopyFromMemoryBlock(mb, 0, 0, 10L * 1024 * 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testMemBlkCopyFromArrayFull() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
		long len = 0 + 1024;
		for(int i = 0; i < len; i++)
		    arr[i] = (byte)-1;
		mb.copyFromArray(arr, 0, 0, 1024);
		len = 0 + 1024;
		for(int i = 0; i < len; i++)
			Assert.assertEquals(mb.getByte(i), (byte)-1);
    }

    @Test
    public void testMemBlkCopyFromArrayPart() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
		long len = 56 + 100;
		for(int i = 56; i < len; i++)
		    arr[i] = (byte)-1;
		mb.copyFromArray(arr, 56, 12, 100);
		len = 12 + 100;
		for(int i = 12; i < len; i++)
			Assert.assertEquals(mb.getByte(i), (byte)-1);
    }

    @Test
    public void testCompactMemBlkCopyFromArrayFull() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
		long len = 0 + 1024;
		for(int i = 0; i < len; i++)
		    arr[i] = (byte)-1;
		mb.copyFromArray(arr, 0, 0, 1024);
		len = 0 + 1024;
		for(int i = 0; i < len; i++)
			Assert.assertEquals(mb.getByte(i), (byte)-1);
    }

    @Test
    public void testCompactMemBlkCopyFromArrayPart() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
		long len = 56 + 100;
		for(int i = 56; i < len; i++)
		    arr[i] = (byte)-1;
		mb.copyFromArray(arr, 56, 12, 100);
		len = 12 + 100;
		for(int i = 12; i < len; i++)
			Assert.assertEquals(mb.getByte(i), (byte)-1);
    }

    @Test
    public void testMemBlkCopyFromArrayNull() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        try {
            mb.copyFromArray(null, 0, 0, 100);
            Assert.fail("NullPointerException wasn't thrown");
        } 
        catch (NullPointerException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyFromArrayNegativeSrcOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
        try {
            mb.copyFromArray(arr, -1, 0, 1024);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyFromArrayNegativeDestOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
        try {
            mb.copyFromArray(arr, 0, -1, 1024);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyFromArrayNegativeLength() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
        try {
            mb.copyFromArray(arr, 0, 0, -1);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyFromArrayTxFull() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
		long len = 0 + 1024;
		for(int i = 0; i < len; i++)
		    arr[i] = (byte)-1;
		mb.transactionalCopyFromArray(arr, 0, 0, 1024);
		len = 0 + 1024;
		for(int i = 0; i < len; i++)
			Assert.assertEquals(mb.getByte(i), (byte)-1);
    }

    @Test
    public void testMemBlkCopyFromArrayTxPart() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
		long len = 56 + 100;
		for(int i = 56; i < len; i++)
		    arr[i] = (byte)-1;
		mb.transactionalCopyFromArray(arr, 56, 12, 100);
		len = 12 + 100;
		for(int i = 12; i < len; i++)
			Assert.assertEquals(mb.getByte(i), (byte)-1);
    }

    @Test
    public void testCompactMemBlkCopyFromArrayTxFull() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
		long len = 0 + 1024;
		for(int i = 0; i < len; i++)
		    arr[i] = (byte)-1;
		mb.transactionalCopyFromArray(arr, 0, 0, 1024);
		len = 0 + 1024;
		for(int i = 0; i < len; i++)
			Assert.assertEquals(mb.getByte(i), (byte)-1);
    }

    @Test
    public void testCompactMemBlkCopyFromArrayTxPart() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
		long len = 56 + 100;
		for(int i = 56; i < len; i++)
		    arr[i] = (byte)-1;
		mb.transactionalCopyFromArray(arr, 56, 12, 100);
		len = 12 + 100;
		for(int i = 12; i < len; i++)
			Assert.assertEquals(mb.getByte(i), (byte)-1);
    }

    @Test
    public void testMemBlkCopyFromArrayTxNull() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        try {
            mb.transactionalCopyFromArray(null, 0, 0, 100);
            Assert.fail("NullPointerException wasn't thrown");
        } 
        catch (NullPointerException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyFromArrayTxNegativeSrcOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
        try {
            mb.transactionalCopyFromArray(arr, -1, 0, 1024);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyFromArrayTxNegativeDestOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
        try {
            mb.transactionalCopyFromArray(arr, 0, -1, 1024);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyFromArrayTxNegativeLength() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		byte[] arr = new byte[1024];
        try {
            mb.transactionalCopyFromArray(arr, 0, 0, -1);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyToArrayFull() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
		mb.setMemory((byte)-1, 0, 1024);
        mb.copyToArray(0, arr, 0, 1024);
        int len = 0 + 1024;
        for(int i = 0; i < len; i++)
            Assert.assertEquals(arr[i], (byte)-1);
    }

    @Test
    public void testMemBlkCopyToArrayPart() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
		mb.setMemory((byte)-1, 56, 100);
        mb.copyToArray(56, arr, 12, 100);
        int len = 12 + 100;
        for(int i = 12; i < len; i++)
            Assert.assertEquals(arr[i], (byte)-1);
    }

    @Test
    public void testCompactMemBlkCopyToArrayFull() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
		mb.setMemory((byte)-1, 0, 1024);
        mb.copyToArray(0, arr, 0, 1024);
        int len = 0 + 1024;
        for(int i = 0; i < len; i++)
            Assert.assertEquals(arr[i], (byte)-1);
    }

    @Test
    public void testCompactMemBlkCopyToArrayPart() {
        heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
		mb.setMemory((byte)-1, 56, 100);
        mb.copyToArray(56, arr, 12, 100);
        int len = 12 + 100;
        for(int i = 12; i < len; i++)
            Assert.assertEquals(arr[i], (byte)-1);
    }

    @Test
    public void testMemBlkCopyToArrayNull() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        try {
            mb.copyToArray(0, null, 0, 100);
            Assert.fail("NullPointerException wasn't thrown");
        } 
        catch (NullPointerException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyToArrayNegativeSrcOffset() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
        try {
            mb.copyToArray(-1, arr, 0, 1024);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyTooArrayNegativeDestOffset() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
        try {
            mb.copyToArray(0, arr, -1, 1024);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testMemBlkCopyToArrayNegativeLength() {
        heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
        try {
            mb.copyToArray(0, arr, 0, -1);
            Assert.fail("IndexOutOfBoundsException wasn't thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert true;
        }
    }

    @Test
    public void testWithRangeFullValid() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		    mb.withRange(0, 1024, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.assertEquals(mb.getByte(0), (byte)1);
		    Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short)2345);
            Assert.assertEquals(mb.getLong(7), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(0), (byte)1);
            Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short) 2345);
            Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testWithRangeValid() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		    mb.withRange(100, 500, (Range range) -> {
			    range.setByte(101, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.assertEquals(mb.getByte(101), (byte)1);
		    Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short)2345);
            Assert.assertEquals(mb.getLong(108), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(101), (byte)1);
            Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short) 2345);
            Assert.assertEquals(mb.getLong(108), 3456);
    }

    @Test
    public void testWithRangeInvalidDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.withRange(100, 500, (Range range) -> {
			    range.setByte(99, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeInvalidDataSizeForOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.withRange(100, 400, (Range range) -> {
			    range.setByte(100, (byte)1);
                range.setInt(499, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeNegativeDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.withRange(0, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeNegativeRangeOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.withRange(-1, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeInvalidRangeLength() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.withRange(0, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeNegativeRangeLength() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.withRange(0, -1, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeCompactFullValid() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		    mb.withRange(0, 1024, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.assertEquals(mb.getByte(0), (byte)1);
		    Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short)2345);
            Assert.assertEquals(mb.getLong(7), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.compactMemoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(0), (byte)1);
            Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short) 2345);
            Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testWithRangeCompactValid() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		    mb.withRange(100, 500, (Range range) -> {
			    range.setByte(101, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.assertEquals(mb.getByte(101), (byte)1);
		    Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short)2345);
            Assert.assertEquals(mb.getLong(108), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.compactMemoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(101), (byte)1);
            Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short) 2345);
            Assert.assertEquals(mb.getLong(108), 3456);
    }

    @Test
    public void testWithRangeCompactInvalidDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		try {
		    mb.withRange(100, 500, (Range range) -> {
			    range.setByte(99, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeCompactNegativeDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		try {
		    mb.withRange(0, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(-1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeCompactNegativeRangeOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		try {
		    mb.withRange(-1, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeCompactNegativeRangeLength() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		try {
		    mb.withRange(0, -1, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeValidFull() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		    mb.withRange((Range range) -> {
                range.setByte(101, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.assertEquals(mb.getByte(101), (byte)1);
		    Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short)2345);
            Assert.assertEquals(mb.getLong(108), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(101), (byte)1);
            Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short) 2345);
            Assert.assertEquals(mb.getLong(108), 3456);
    }

    @Test
    public void testWithRangeFullInvalidDataSizeForOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.withRange((Range range) -> {
                range.setByte(100, (byte)1);
                range.setInt(1023, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeFullLastDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		    mb.withRange((Range range) -> {
                range.setByte(1023, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.assertEquals(mb.getByte(1023), (byte)1);
		    Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short)2345);
            Assert.assertEquals(mb.getLong(7), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(1023), (byte)1);
            Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short) 2345);
            Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testWithRangeFullNegativeDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.withRange((Range range) -> {
                range.setByte(0, (byte)1);
                range.setInt(-1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxFullValid() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		    mb.transactionalWithRange(0, 1024, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.assertEquals(mb.getByte(0), (byte)1);
		    Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short)2345);
            Assert.assertEquals(mb.getLong(7), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(0), (byte)1);
            Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short) 2345);
            Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testWithRangeTxValid() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		    mb.transactionalWithRange(100, 500, (Range range) -> {
			    range.setByte(101, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.assertEquals(mb.getByte(101), (byte)1);
		    Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short)2345);
            Assert.assertEquals(mb.getLong(108), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(101), (byte)1);
            Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short) 2345);
            Assert.assertEquals(mb.getLong(108), 3456);
    }

    @Test
    public void testWithRangeTxInvalidDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(100, 500, (Range range) -> {
			    range.setByte(99, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxInvalidDataSizeForOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(100, 400, (Range range) -> {
			    range.setByte(100, (byte)1);
                range.setInt(499, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxNegativeDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(0, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxNegativeRangeOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(-1, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxInvalidRangeLength() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(0, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxNegativeRangeLength() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(0, -1, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxCompactFullValid() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		    mb.transactionalWithRange(0, 1024, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.assertEquals(mb.getByte(0), (byte)1);
		    Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short)2345);
            Assert.assertEquals(mb.getLong(7), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.compactMemoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(0), (byte)1);
            Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short) 2345);
            Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testWithRangeTxCompactValid() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		    mb.transactionalWithRange(100, 500, (Range range) -> {
			    range.setByte(101, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.assertEquals(mb.getByte(101), (byte)1);
		    Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short)2345);
            Assert.assertEquals(mb.getLong(108), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.compactMemoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(101), (byte)1);
            Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short) 2345);
            Assert.assertEquals(mb.getLong(108), 3456);
    }

    @Test
    public void testWithRangeTxCompactInvalidDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(100, 500, (Range range) -> {
			    range.setByte(99, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxCompactNegativeDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(0, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(-1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxCompactNegativeRangeOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(-1, 1025, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxCompactNegativeRangeLength() {
		heap = TestVars.createPersistentHeap();
        PersistentCompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange(0, -1, (Range range) -> {
			    range.setByte(0, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxValidFull() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		    mb.transactionalWithRange((Range range) -> {
                range.setByte(101, (byte)1);
                range.setInt(102, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.assertEquals(mb.getByte(101), (byte)1);
		    Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short)2345);
            Assert.assertEquals(mb.getLong(108), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(101), (byte)1);
            Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short) 2345);
            Assert.assertEquals(mb.getLong(108), 3456);
    }

    @Test
    public void testWithRangeTxFullInvalidDataSizeForOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange((Range range) -> {
                range.setByte(100, (byte)1);
                range.setInt(1023, 1234);
                range.setShort(106, (short)2345);
                range.setLong(108, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testWithRangeTxFullLastDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		    mb.transactionalWithRange((Range range) -> {
                range.setByte(1023, (byte)1);
                range.setInt(1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.assertEquals(mb.getByte(1023), (byte)1);
		    Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short)2345);
            Assert.assertEquals(mb.getLong(7), 3456);
            long handle = mb.handle();
            heap.close();
            heap = PersistentHeap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(1023), (byte)1);
            Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short) 2345);
            Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testWithRangeTxFullNegativeDataOffset() {
		heap = TestVars.createPersistentHeap();
        PersistentMemoryBlock mb = heap.allocateMemoryBlock(1024, true);
		try {
		    mb.transactionalWithRange((Range range) -> {
                range.setByte(0, (byte)1);
                range.setInt(-1, 1234);
                range.setShort(5, (short)2345);
                range.setLong(7, 3456);
            });
		    Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

}
