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
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Random;

@SuppressWarnings("deprecation")
@Test(singleThreaded=true)
public class MemoryBlock2Tests {

	Heap heap = null;	
	Heap heapNew = null;

	@BeforeMethod
	public void initialze() {
		heap = null;
		heapNew = null;
	} 

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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setByte(12, (byte)128);
		Assert.assertEquals(mb.getByte(12), (byte)128);
	}

	@Test
	public void testMemBlockWriteByteZeroOffset() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setByte(0, (byte)128);
		Assert.assertEquals(mb.getByte(0), (byte)128);
	}

	@Test
	public void testMemBlockWriteByteMaxOffset() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setByte(1023, (byte)128);
		Assert.assertEquals(mb.getByte(1023), (byte)128);
	}

	@Test
	public void testMemBlockWriteByteMaxOffsetNegative() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setInt(12, 128);
		Assert.assertEquals(mb.getInt(12), 128);
	}

	@Test
	public void testMemBlockWriteIntZeroOffset() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setInt(0, 128);
		Assert.assertEquals(mb.getInt(0), 128);
	}

	@Test
	public void testMemBlockWriteIntMaxOffset() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setShort(12, (short)128);
		Assert.assertEquals(mb.getShort(12), (short)128);
	}

	@Test
	public void testMemBlockWriteShortZeroOffset() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setShort(0, (short)128);
		Assert.assertEquals(mb.getShort(0), (short)128);
	}

	@Test
	public void testMemBlockWriteShortMaxOffset() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setLong(12, 128);
		Assert.assertEquals(mb.getLong(12), 128);
	}

	@Test
	public void testMemBlockWriteLongZeroOffset() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        Assert.assertEquals(mb.size(), 1024);
		mb.setLong(0, 128);
		Assert.assertEquals(mb.getLong(0), 128);
	}

	@Test
	public void testMemBlockWriteLongMaxOffset() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
		Assert.assertTrue(heap.size() > 0);
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
    public void testSetMemoryValidZero() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
		mb.setMemory((byte)0, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)0);
    }

    @Test
    public void testSetMemoryValid() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryValidMax() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 0, 1024);
		long end = 0 + 1024;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryInvalidMax() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);           
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
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)0, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)0);
    }

    @Test
    public void testSetMemoryCompactValid() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 12, 30);
		long end = 12 + 30;
		for(int i = 12; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryCompactValidMax() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 0, 1023);
		long end = 0 + 1023;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryCompactInvalidMax() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 0, 1024);
		long end = 0 + 1024;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemorynboundedInvalidMax2() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
		mb.setMemory((byte)-1, 0, 2000);
		long end = 0 + 2000;
		for(int i = 0; i < end; i++)		
         	assert(mb.getByte(i) == (byte)-1);
    }

    @Test
    public void testSetMemoryCompactInvalidMin() {
		heap = TestVars.createHeap();
        Assert.assertTrue(Heap.exists(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME));
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);           
        try {
            mb.setMemory((byte)-1, -1, 30);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		MemoryBlock mbNew = heapNew.allocateMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
		heapNew = Heap.createHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + "1");
		CompactMemoryBlock mbNew = heapNew.allocateCompactMemoryBlock(1024, true);
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
    public void testCopyFromMemoryBlockNegativeOffset1() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        MemoryBlock mbNew = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        CompactMemoryBlock mbNew = heap.allocateCompactMemoryBlock(1024, true);
		try {
           	mbNew.copyFromMemoryBlock(mb, 0, 0, 10L * 1024 * 1024);
			Assert.fail("IndexOutOfBoundsException wasn't thrown");
		} 
        catch (IndexOutOfBoundsException e) {
			assert true;
		}
    }

    @Test
    public void testMemBlkCopyFromArrayFull() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
    public void testMemBlkCopyToArrayFull() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
		mb.setMemory((byte)-1, 0, 1024);
        mb.copyToArray(0, arr, 0, 1024);
        int len = 0 + 1024;
        for(int i = 0; i < len; i++)
            Assert.assertEquals(arr[i], (byte)-1);
    }

    @Test
    public void testMemBlkCopyToArrayPart() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
		mb.setMemory((byte)-1, 56, 100);
        mb.copyToArray(56, arr, 12, 100);
        int len = 12 + 100;
        for(int i = 12; i < len; i++)
            Assert.assertEquals(arr[i], (byte)-1);
    }

    @Test
    public void testCompactMemBlkCopyToArrayFull() {
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
		mb.setMemory((byte)-1, 0, 1024);
        mb.copyToArray(0, arr, 0, 1024);
        int len = 0 + 1024;
        for(int i = 0; i < len; i++)
            Assert.assertEquals(arr[i], (byte)-1);
    }

    @Test
    public void testCompactMemBlkCopyToArrayPart() {
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024, true);
        byte[] arr = new byte[1024];
		mb.setMemory((byte)-1, 56, 100);
        mb.copyToArray(56, arr, 12, 100);
        int len = 12 + 100;
        for(int i = 12; i < len; i++)
            Assert.assertEquals(arr[i], (byte)-1);
    }

    @Test
    public void testMemBlkCopyToArrayNull() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024, true);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
            heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(0), (byte)1);
            Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short) 2345);
            Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testWithRangeValid() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
            heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(101), (byte)1);
            Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short) 2345);
            Assert.assertEquals(mb.getLong(108), 3456);
    }

    @Test
    public void testWithRangeInvalidDataOffset() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
    public void testWithRangeNegativeRangeOffset() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
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
            heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.compactMemoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(0), (byte)1);
            Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short) 2345);
            Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testWithRangeCompactValid() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
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
            heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.compactMemoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(101), (byte)1);
            Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short) 2345);
            Assert.assertEquals(mb.getLong(108), 3456);
    }

    @Test
    public void testWithRangeCompactInvalidDataOffset() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
            heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(101), (byte)1);
            Assert.assertEquals(mb.getInt(102), 1234);
            Assert.assertEquals(mb.getShort(106), (short) 2345);
            Assert.assertEquals(mb.getLong(108), 3456);
    }

    @Test
    public void testWithRangeFullInvalidDataSizeForOffset() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
            heap = Heap.openHeap(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
            mb = heap.memoryBlockFromHandle(handle);
		    Assert.assertEquals(mb.getByte(1023), (byte)1);
            Assert.assertEquals(mb.getInt(1), 1234);
            Assert.assertEquals(mb.getShort(5), (short) 2345);
            Assert.assertEquals(mb.getLong(7), 3456);
    }

    @Test
    public void testWithRangeFullNegativeDataOffset() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
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
    
    //CopyFromBB
    //direct v nondirect
    //compact vs noncompact
    @Test
    public void testCopyFromBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).reset();
        mb.copyFromByteBuffer(buf, 100);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromROBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).reset();
        mb.copyFromByteBuffer(buf.asReadOnlyBuffer(), 100);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromDirectBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        dbuf.position(100).mark();
        dbuf.put(arr, 0, arr.length).reset();
        mb.copyFromByteBuffer(dbuf, 100);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromRODirectBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        dbuf.position(100).mark();
        dbuf.put(arr, 0, arr.length).reset();
        mb.copyFromByteBuffer(dbuf.asReadOnlyBuffer(), 100);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromBBCompact() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).reset();
        mb.copyFromByteBuffer(buf, 100);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromROBBCompact() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).reset();
        mb.copyFromByteBuffer(buf.asReadOnlyBuffer(), 100);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromDirectBBCompact() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        dbuf.position(100).mark();
        dbuf.put(arr, 0, arr.length).reset();
        mb.copyFromByteBuffer(dbuf, 100);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromRODirectBBCompact() {
		heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        dbuf.position(100).mark();
        dbuf.put(arr, 0, arr.length).reset();
        mb.copyFromByteBuffer(dbuf.asReadOnlyBuffer(), 100);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(mb.getByte(100+i), arr[i]);
        }
    }

    @Test
    public void testCopyFromEmptyRangeBB() {
    	heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100);
        buf.put(arr, 0, arr.length).position(1024);
        mb.copyFromByteBuffer(buf, 100);
    }

    @Test
    public void testCopyFromEmptyRangeDirectBB() {
    	heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100);
        buf.put(arr, 0, arr.length).position(1024);
        mb.copyFromByteBuffer(buf, 100);
    }

    @Test
    public void testCopyFromBBInvalidMB() {
    	heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).reset();
        try {
            mb.copyFromByteBuffer(buf, 1000);
            Assert.fail();
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCopyFromDirectBBInvalidMB() {
    	heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).reset();
        try {
            mb.copyFromByteBuffer(buf, 1000);
            Assert.fail();
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCopyFromBBInvalidCMB() {
    	heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).reset();
        try {
            mb.copyFromByteBuffer(buf, -100);
            Assert.fail();
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCopyFromDirectBBInvalidCMB() {
    	heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        buf.position(100).mark();
        buf.put(arr, 0, arr.length).reset();
        try {
            mb.copyFromByteBuffer(buf, -100);
            Assert.fail();
        } catch(IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCopyToBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        mb.copyFromArray(arr, 0, 100, arr.length);
        buf.position(100).mark();
        mb.copyToByteBuffer(100, buf, arr.length);
        Assert.assertEquals(buf.position(), 100 + arr.length);
        buf.reset(); 
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(buf.get(), arr[i]);
        }
    }

    @Test
    public void testCopyToROBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer buf = ByteBuffer.allocate(1024).asReadOnlyBuffer();
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        mb.copyFromArray(arr, 0, 100, arr.length);
        buf.position(100).mark();
        try {
            mb.copyToByteBuffer(100, buf, arr.length);
            Assert.fail();
        } catch (ReadOnlyBufferException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCopyToDirectBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        mb.copyFromArray(arr, 0, 100, arr.length);
        dbuf.position(100).mark();
        mb.copyToByteBuffer(100, dbuf, arr.length);
        Assert.assertEquals(dbuf.position(), 100 + arr.length);
        dbuf.reset(); 
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(dbuf.get(), arr[i]);
        }
    }

    @Test
    public void testCopyToRODirectBB() {
		heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        ByteBuffer dbuf = ByteBuffer.allocateDirect(1024).asReadOnlyBuffer();
        byte[] arr = new byte[100];
        new Random().nextBytes(arr);
        mb.copyFromArray(arr, 0, 100, arr.length);
        dbuf.position(100).mark();
        try {
            mb.copyToByteBuffer(100, dbuf, arr.length);
            Assert.fail();
        } catch (ReadOnlyBufferException e) {
            Assert.assertTrue(true);
        }
    }
}
