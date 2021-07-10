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

@Test(singleThreaded = true)
public class MemoryAccessorTests {
    Heap heap = null;

    @BeforeMethod
    public void initialize() {
        heap = null;
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

    // MemoryAccessor.address()
    @Test
    public void testMemoryAccessorAddress(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        long address = mb.handle();
        Assert.assertTrue(address != 0L);
    }

    // MemoryAccessor.rawWithRange(long, long, Consumer)
    /*@Test
    public void testMemoryAccessorRawWithRangeConsumer(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        mb.rawWithRange(offset, length, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
    }

    // MemoryAccessor.rawWithRange(long, long, Function)
    @Test
    public void testMemoryAccessorRawWithRangeFunction(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        MemoryBlock mbInternal = mb.rawWithRange(offset, length, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            MemoryBlock mbNew = heap.allocateMemoryBlock(1024);
            mbNew.setInt(0,54321);
            return mbNew;
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
        Assert.assertEquals(mbInternal.getInt(0), 54321);
    }*/

    // Accessor.withRange(Consumer)
    @Test
    public void testAccessorWithRangeConsumerImplicit(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        acc.withRange( (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
    }

    // Accessor.withRange(long, long, Consumer)
    @Test
    public void testAccessorWithRangeConsumer(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        acc.withRange(offset, length, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
    }

    // Accessor.withRange(long, long, Function)
    @Test
    public void testAccessorWithRangeFunction(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        MemoryBlock mbInternal = acc.withRange(offset, length, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            MemoryBlock mbNew = heap.allocateMemoryBlock(1024);
            mbNew.setInt(0,54321);
            return mbNew;
        });
        Assert.assertEquals(acc.getByte(0), (byte)1);
        Assert.assertEquals(acc.getInt(1), 1234);
        Assert.assertEquals(acc.getShort(5), (short)2345);
        Assert.assertEquals(mbInternal.getInt(0), 54321);
    }

    // CompactAccessor.withRange(long, long, Consumer)
    @Test
    public void testCompactAccessorWithRangeConsumer(){
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        CompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        acc.withRange(offset, length, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
        });
        Assert.assertEquals(mb.getByte(0), (byte)1);
        Assert.assertEquals(mb.getInt(1), 1234);
        Assert.assertEquals(mb.getShort(5), (short)2345);
    }

    // CompactAccessor.withRange(long, long, Function)
    @Test
    public void testCompactAccessorWithRangeFunction(){
        heap = TestVars.createHeap();
        CompactMemoryBlock mb = heap.allocateCompactMemoryBlock(1024);
        CompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        long offset = 0;
        long length = 1 + Integer.BYTES + Short.BYTES;
        MemoryBlock mbInternal = acc.withRange(offset, length, (Range range) -> {
            range.setByte(0, (byte)1);
            range.setInt(1, 1234);
            range.setShort(5, (short)2345);
            MemoryBlock mbNew = heap.allocateMemoryBlock(1024);
            mbNew.setInt(0,54321);
            return mbNew;
        });
        Assert.assertEquals(acc.getByte(0), (byte)1);
        Assert.assertEquals(acc.getInt(1), 1234);
        Assert.assertEquals(acc.getShort(5), (short)2345);
        Assert.assertEquals(mbInternal.getInt(0), 54321);
    }

    // Accessor.freeMemory()
    @Test
    public void testAccessorFreeMemory(){
        heap = TestVars.createHeap();
        // allocate memory
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        Accessor acc = heap.createAccessor();
        acc.handle(mb.handle());
        // assert valid
        Assert.assertTrue(acc.isValid());
        // acc.freeMemory()
        acc.freeMemory();
        Assert.assertFalse(acc.isValid());
    }

    // CompactAccessor.freeMemory()
    @Test
    public void testCompactAccessorFreeMemory(){
        heap = TestVars.createHeap();
        // allocate memory
        MemoryBlock mb = heap.allocateMemoryBlock(1024);
        CompactAccessor acc = heap.createCompactAccessor();
        acc.handle(mb.handle());
        // assert valid
        Assert.assertTrue(acc.isValid());
        // acc.freeMemory()
        acc.freeMemory();
        Assert.assertFalse(acc.isValid());
    }

    // AnyMemoryBlock.hashCode()
    @Test
    public void testAnyMemoryBlockHashCode(){
        heap = TestVars.createHeap();
        MemoryBlock mb1 = heap.allocateMemoryBlock(1024);
        MemoryBlock mb2 = heap.allocateMemoryBlock(1024);
        int mb1Hash = mb1.hashCode();
        int mb2Hash = mb2.hashCode();
        Assert.assertTrue(true);
        Assert.assertFalse(mb1Hash == mb2Hash);
    }

    // AnyMemoryBlock.equals();
    @Test
    public void testAnyMemoryBlockEquals(){
        heap = TestVars.createHeap();
        MemoryBlock mb1 = heap.allocateMemoryBlock(1024);
        long handle = mb1.handle();
        MemoryBlock mb2 = heap.memoryBlockFromHandle(handle);
        Assert.assertTrue(mb1.equals(mb2));
    }

    @Test
    public void testAnyMemoryBlockEqualsNegative(){
        heap = TestVars.createHeap();
        MemoryBlock mb1 = heap.allocateMemoryBlock(1024);
        MemoryBlock mb2 = heap.allocateMemoryBlock(1024);
        Assert.assertFalse(mb1.equals(mb2));
    }

    @Test
    public void testAnyMemoryBlockEqualsNegativeInvalidComparison(){
        heap = TestVars.createHeap();
        MemoryBlock mb1 = heap.allocateMemoryBlock(1024);
        int someInt = 42;
        Assert.assertFalse(mb1.equals(someInt));
    }

    // size() tests
    static long memoryAccessorSize(MemoryAccessor memAx) {
        return memAx.size();
    }

    static long anyAccessorSize(AnyAccessor ax) {
        return ax.size();
    }

    static long anyMemoryBlockSize(AnyMemoryBlock mb) {
        return mb.size();
    }

    @Test
    public void testMemoryAccessorWithMemoryBlock(){
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024L);
        Assert.assertEquals(memoryAccessorSize(mb), 1024L);
    }

    @Test
    public void testMemoryAccessorWithCompactMemoryBlock(){
        heap = TestVars.createHeap();
        CompactMemoryBlock cmb = heap.allocateCompactMemoryBlock(1024L);
        try {
            memoryAccessorSize(cmb);
            Assert.fail("UnsupportedOperationException was not thrown");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testMemoryAccessorWithAccessor(){
        heap = TestVars.createHeap();
        Accessor ax = heap.createAccessor();
        ax.handle(heap.allocateMemory(1024L));
        Assert.assertEquals(memoryAccessorSize(ax), 1024L);
    }

    @Test
    public void testMemoryAccessorWithCompactAccessor(){
        heap = TestVars.createHeap();
        CompactAccessor cax = heap.createCompactAccessor();
        cax.handle(heap.allocateMemory(1024L));
        try {
            memoryAccessorSize(cax);
            Assert.fail("UnsupportedOperationException was not thrown");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testMemoryAccessorWithAccessorReset(){
        heap = TestVars.createHeap();
        Accessor ax = heap.createAccessor();
        ax.handle(heap.allocateMemory(1024L));
        ax.resetHandle();
        Assert.assertEquals(memoryAccessorSize(ax), 0L);
    }

    @Test
    public void testMemoryAccessorWithCompactAccessorReset(){
        heap = TestVars.createHeap();
        CompactAccessor cax = heap.createCompactAccessor();
        cax.handle(heap.allocateMemory(1024L));
        cax.resetHandle();
        try {
            memoryAccessorSize(cax);
            Assert.fail("UnsupportedOperationException was not thrown");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAnyMemoryBlockWithMemoryBlock() {
        heap = TestVars.createHeap();
        MemoryBlock mb = heap.allocateMemoryBlock(1024L);
        Assert.assertEquals(anyMemoryBlockSize(mb), 1024L);
    }

    @Test
    public void testAnyMemoryBlockWithCompactMemoryBlock(){
        heap = TestVars.createHeap();
        CompactMemoryBlock cmb = heap.allocateCompactMemoryBlock(1024L);
        try {
            anyMemoryBlockSize(cmb);
            Assert.fail("UnsupportedOperationException was not thrown");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testAnyAccessorWithAccessor() {
        heap = TestVars.createHeap();
        Accessor ax = heap.createAccessor();
        ax.handle(heap.allocateMemory(1024L));
        Assert.assertEquals(anyAccessorSize(ax), 1024L);
    }

    @Test
    public void testAnyAccessorWithCompactAccessorReset(){
        heap = TestVars.createHeap();
        CompactAccessor cax = heap.createCompactAccessor();
        cax.handle(heap.allocateMemory(1024L));
        cax.resetHandle();
        try {
            anyAccessorSize(cax);
            Assert.fail("UnsupportedOperationException was not thrown");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true);
        }
    }
}
