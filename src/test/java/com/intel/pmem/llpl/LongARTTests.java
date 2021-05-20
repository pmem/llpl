/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import com.intel.pmem.llpl.*;
import com.intel.pmem.llpl.util.LongART;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

@Test(singleThreaded = true)
public class LongARTTests {
    Heap heap = null;
    static byte[] firstKey;
    static byte[] lastKey;
    static byte[] prefixKey;
    static long firstValue;
    static long lastValue;
    static long prefixValue;
    static final int TREESIZE = 120;
    static final long SEED = 123456789;
    static Random rnd = new Random(SEED);

    @BeforeMethod
    public void initialize() {
        heap = TestVars.createHeap();
        firstKey = new byte[]{(byte)0};
        lastKey = new byte[20]; Arrays.fill(lastKey, (byte)0xff);
        prefixKey = Arrays.copyOfRange(lastKey, 0, 12);
        firstValue = 0x1234L << 8 | (byte)1;
        lastValue = 0x1234L << 8 | (byte)2;
        prefixValue = 0x1234L << 8 | (byte)3;
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

    static byte[] getKey(int size) {
        byte[] ret = new byte[size];
        rnd.nextBytes(ret);
        return ret;
    }
    
    static class KeyBytes implements Comparable<KeyBytes> {
        byte[] arr;

        public KeyBytes(byte[] arr) { this.arr = arr; }

        byte[] get() { return arr; }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeyBytes)) return false;
            KeyBytes other = (KeyBytes)obj;
            return Arrays.equals(arr, other.get());
        }

        @Override
        public int compareTo(KeyBytes o) {
            byte[] other = o.get();
            int ret = 0;
            int i = 0;
            while (i < arr.length && i < other.length && (ret = Integer.compareUnsigned(Byte.toUnsignedInt(arr[i]), Byte.toUnsignedInt(other[i]))) == 0) i++;
            return (ret == 0) ? Integer.compare(arr.length, other.length) : ret;
        }
    }

    static void fill (LongART tree, ConcurrentSkipListMap<KeyBytes, Long> control) {
        tree.put(firstKey, firstValue);
        tree.put(lastKey, lastValue);
        tree.put(prefixKey, prefixValue);
        control.put(new KeyBytes(firstKey), firstValue);
        control.put(new KeyBytes(lastKey), lastValue);
        control.put(new KeyBytes(prefixKey), prefixValue);
        byte[] key;
        long val = 0;
        rnd.setSeed(SEED);
        for (int i = 3; i < TREESIZE; i++) {
            key = new byte[8 + rnd.nextInt(12)]; 
            rnd.nextBytes(key);
            val = 0x1234L << 8 | (byte)i;
            tree.put(key, val);
            control.put(new KeyBytes(key), val);
        }
    }

    static void fill (LongART tree) {
        tree.put(firstKey, firstValue);
        tree.put(lastKey, lastValue);
        tree.put(prefixKey, prefixValue);
        byte[] key;
        rnd.setSeed(SEED);
        for (int i = 3; i < TREESIZE; i++) {
            key = new byte[8 + rnd.nextInt(12)]; 
            rnd.nextBytes(key);
            tree.put(key, 0x1234L << 8 | (byte)i);
        }
    }

    /* Basic functions, Handle and size */   
    @Test
    public void testHandleEmptyART() {
        LongART art = new LongART(heap);
        Assert.assertTrue(art.handle() > 0);
    }

    @Test
    public void testSizeEmptyART() {
        LongART art = new LongART(heap);
        Assert.assertEquals(art.size(), 0);
    }

    @Test
    public void testHandleRecreatedEmptyART() {
        long artHandle = (new LongART(heap)).handle(); 
        LongART art = new LongART(heap, artHandle);
        Assert.assertTrue(art.handle() > 0);
    }

    @Test
    public void testSizeRecreatedEmptyART() {
        long artHandle = (new LongART(heap)).handle(); 
        LongART art = new LongART(heap, artHandle);
        Assert.assertEquals(art.size(), 0);
    }

    @Test
    public void testSizeFilled() {
        LongART art = new LongART(heap);
        fill(art);
        Assert.assertEquals(art.size(), TREESIZE);
    }

    @Test
    public void testSizeRecreatedFilled() {
        LongART art = new LongART(heap);
        fill(art);
        LongART newArt = new LongART(heap, art.handle());
        Assert.assertEquals(newArt.size(), TREESIZE);
    }
        
    @Test
    public void testSizeClearedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        Assert.assertEquals(art.size(), 0);
    }

    @Test
    public void testSizeRecreatedClearedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {return;});
        LongART recreatedArt = new LongART(heap, art.handle());
        Assert.assertEquals(recreatedArt.size(), 0);
    }

    @Test
    public void testHandleFreedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        art.free();
        try {
            art.handle();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testSizeFreedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        art.free();
        try {
            art.size();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }

    /* FirstKey() and LastKey() */

    @Test
    public void testFirstkeyEmptyART() {
        LongART art = new LongART(heap);
        try {
            art.firstKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLastkeyEmptyART() {
        LongART art = new LongART(heap);
        try {
            art.lastKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFirstkeyRecreatedEmptyART() {
        long artHandle = (new LongART(heap)).handle(); 
        LongART art = new LongART(heap, artHandle);
        try {
            art.firstKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLastkeyRecreatedEmptyART() {
        long artHandle = (new LongART(heap)).handle(); 
        LongART art = new LongART(heap, artHandle);
        try {
            art.lastKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFirstkeyFilledART() {
        LongART art = new LongART(heap);
        fill(art);
        Assert.assertEquals(art.firstKey(), firstKey);
    }

    @Test
    public void testLastkeyFilledART() {
        LongART art = new LongART(heap);
        fill(art);
        Assert.assertEquals(art.lastKey(), lastKey);
    }

    @Test
    public void testFirstkeyRecreatedFilledART() {
        LongART art = new LongART(heap);
        fill(art);
        LongART recreatedArt = new LongART(heap, art.handle());
        Assert.assertEquals(recreatedArt.firstKey(), firstKey);
    }

    @Test
    public void testLastkeyRecreatedFilledART() {
        LongART art = new LongART(heap);
        fill(art);
        LongART recreatedArt = new LongART(heap, art.handle());
        Assert.assertEquals(recreatedArt.lastKey(), lastKey);
    }

    @Test
    public void testFirstkeyClearedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        try {
            art.firstKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLastkeyClearedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        try {
            art.lastKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFirstkeyRecreatedClearedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        LongART recreatedArt = new LongART(heap, art.handle());
        try {
            art.firstKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLastkeyRecreatedClearedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        LongART recreatedArt = new LongART(heap, art.handle());
        try {
            art.lastKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFirstKeyFreedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        art.free();
        try {
            art.firstKey();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testLastKeyFreedART() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        art.free();
        try {
            art.lastKey();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }

    /* Clear tests */
    @Test
    public void testClearEmptyART() {
        LongART art = new LongART(heap);
        art.clear(c ->{});
        Assert.assertEquals(art.size(), 0);
    }

    @Test
    public void testClearReconstructedEmptyART() {
        long artHandle = (new LongART(heap)).handle(); 
        LongART art = new LongART(heap, artHandle);
        art.clear(c ->{});
        Assert.assertEquals(art.size(), 0);
    }

    @Test
    public void testClearNullConsumer() {
        LongART art = new LongART(heap);
        fill(art);
        try {
            art.clear(null);
            Assert.fail("IllegalArgumentException wasnt thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testDoubleClearEmpty() {
        LongART art = new LongART(heap);
        art.clear(c ->{});
        art.clear(c ->{});
        Assert.assertEquals(art.size(), 0);
    }

    @Test
    public void testDoubleClearReconstructedEmpty() {
        long artHandle = (new LongART(heap)).handle(); 
        LongART art = new LongART(heap, artHandle);
        art.clear(c ->{});
        art.clear(c ->{});
        Assert.assertEquals(art.size(), 0);
    }

    @Test
    public void testDoubleClearFull() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c ->{});
        art.clear(c ->{});
        Assert.assertEquals(art.size(), 0);
    }

    @Test
    public void testDoubleClearReconstructedFull() {
        LongART art = new LongART(heap);
        long artHandle = art.handle(); 
        fill(art);
        LongART newArt = new LongART(heap, artHandle);
        newArt.clear(c ->{});
        newArt.clear(c ->{});
        Assert.assertEquals(art.size(), 0);
    }

    @Test 
    void testClearAfterFree() {
        LongART art = new LongART(heap);
        art.free();
        try {
            art.clear(c -> {});
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    /* Free Tests */

    @Test 
    void testFreeReconstructedEmpty() {
        long artHandle = (new LongART(heap)).handle(); 
        LongART art = new LongART(heap, artHandle);
        art.free();
    }

    @Test 
    void testFreeReconstructedFull() {
        LongART art = new LongART(heap);
        long artHandle = art.handle(); 
        fill(art);
        LongART newArt = new LongART(heap, artHandle);
        // no need for clear()
        art.free();
    }

    @Test
    void testDoubleFree() {
        LongART art = new LongART(heap);
        art.free();
        try {
            art.free();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    /* Insertion Tests */

    @Test
    public void testInsertNullKey() {
        LongART art = new LongART(heap);
        try {
            art.put(null, new Long(1234));
            Assert.fail("IllegalArgumentException wasnt thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testInsertZeroLengthKey() {
        LongART art = new LongART(heap);
        try {
            art.put(new byte[]{}, new Long(1234));
            Assert.fail("IllegalArgumentException wasnt thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testInsertNullValue() {
        LongART art = new LongART(heap);
        try {
            art.put(getKey(8), null);
            Assert.fail("IllegalArgumentException wasnt thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testInsertInvalidMerge() {
        LongART art = new LongART(heap);
        try {
            art.put(getKey(8), new Long(1234), (a, b) -> { return null; });
            Assert.fail("NullPointerException wasnt thrown");
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }
    }

    /* Get tests */
    
    @Test
    public void testGetNullKey() {
        LongART art = new LongART(heap); 
        fill(art);
        try {
            art.get(null);
            Assert.fail("IllegalArgumentException wasnt thrown"); 
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetZeroLengthKey() {
        LongART art = new LongART(heap); 
        fill(art);
        try {
            art.get(new byte[]{});
            Assert.fail("IllegalArgumentException was not thrown"); 
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testGetEmpty() {
        LongART art = new LongART(heap); 
        Assert.assertEquals(art.get(firstKey), 0L);
    }

    @Test
    public void testGetReconstructedEmpty() {
        long artHandle = (new LongART(heap)).handle();
        LongART art = new LongART(heap, artHandle); 
        Assert.assertEquals(art.get(firstKey), 0L);
    }

    @Test
    public void testGetFirstFilled() {
        LongART art = new LongART(heap); 
        fill(art);
        Assert.assertEquals(art.get(firstKey), firstValue);
    }

    @Test
    public void testGetLastFilled() {
        LongART art = new LongART(heap); 
        fill(art);
        Assert.assertEquals(art.get(lastKey), lastValue);
    }

    @Test
    public void testGetPrefixFilled() {
        LongART art = new LongART(heap); 
        fill(art);
        Assert.assertEquals(art.get(prefixKey), prefixValue);
    }

    @Test
    public void testGetReconstructedFirstFilled() {
        LongART art = new LongART(heap); 
        fill(art);
        long artHandle = art.handle();
        LongART newArt = new LongART(heap, artHandle); 
        Assert.assertEquals(art.get(firstKey), firstValue);
    }

    @Test
    public void testGetReconstructedLastFilled() {
        LongART art = new LongART(heap); 
        fill(art);
        long artHandle = art.handle();
        LongART newArt = new LongART(heap, artHandle); 
        Assert.assertEquals(art.get(lastKey), lastValue);
    }
    
    @Test
    public void testGetReconstructedPrefixFilled() {
        LongART art = new LongART(heap); 
        fill(art);
        long artHandle = art.handle();
        LongART newArt = new LongART(heap, artHandle); 
        Assert.assertEquals(art.get(prefixKey), prefixValue);
    }

    // EntryIterator Tests
    // since iterators are volatile is there any need to test on rebuild?
    // maybe only when full.
    @Test
    public void testHasNextEntryIteratorNewEmptyART() {
        LongART art = new LongART(heap);
        Iterator<LongART.Entry> it = art.getEntryIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextRangeEntryIteratorNewEmptyART() {
        LongART art = new LongART(heap);
        Iterator<LongART.Entry> it = art.getEntryIterator(firstKey, true, lastKey, true);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testRangeEntryIteratorNullFromKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getEntryIterator(null, true, lastKey, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeEntryIteratorNullToKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getEntryIterator(firstKey, true, null, false);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeEntryIteratorZeroLengthFromKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getEntryIterator(new byte[]{}, true, lastKey, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeEntryIteratorZeroLengthToKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getEntryIterator(firstKey, true, new byte[]{}, false);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testHasNextHeadEntryIteratorNewEmptyART() {
        LongART art = new LongART(heap);
        Iterator<LongART.Entry> it = art.getHeadEntryIterator(prefixKey, true);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHeadEntryIteratorNullKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getHeadEntryIterator(null, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testHeadEntryIteratorZeroLengthKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getHeadEntryIterator(new byte[]{}, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testHasNextTailEntryIteratorNewEmptyART() {
        LongART art = new LongART(heap);
        Iterator<LongART.Entry> it = art.getTailEntryIterator(prefixKey, true);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testTailEntryIteratorNullKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getTailEntryIterator(null, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testTailEntryIteratorZeroLengthKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getTailEntryIterator(new byte[]{}, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

   @Test
    public void testHasNextEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = new LongART(heap, artHandle);
        Iterator<LongART.Entry> it = art.getEntryIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextRangeEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = new LongART(heap, artHandle);
        Iterator<LongART.Entry> it = art.getEntryIterator(firstKey, false, lastKey, false);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextHeadEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = new LongART(heap, artHandle);
        Iterator<LongART.Entry> it = art.getHeadEntryIterator(prefixKey, false);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextTailEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = new LongART(heap, artHandle);
        Iterator<LongART.Entry> it = art.getTailEntryIterator(prefixKey, false);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testNextNewEmptyEntryIterator() {
        LongART art = new LongART(heap);
        Iterator<LongART.Entry> it = art.getEntryIterator();
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextNewEmptyRangeEntryIterator() {
        LongART art = new LongART(heap);
        Iterator<LongART.Entry> it = art.getEntryIterator(firstKey, true, lastKey, false);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextNewEmptyHeadEntryIterator() {
        LongART art = new LongART(heap);
        Iterator<LongART.Entry> it = art.getHeadEntryIterator(prefixKey, true);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextNewEmptyTailEntryIterator() {
        LongART art = new LongART(heap);
        Iterator<LongART.Entry> it = art.getTailEntryIterator(prefixKey, true);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = new LongART(heap, artHandle);
        Iterator<LongART.Entry> it = art.getEntryIterator();
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextRangeEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = new LongART(heap, artHandle);
        Iterator<LongART.Entry> it = art.getEntryIterator(firstKey, false, lastKey, true);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextHeadEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = new LongART(heap, artHandle);
        Iterator<LongART.Entry> it = art.getHeadEntryIterator(prefixKey, false);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextTailEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = new LongART(heap, artHandle);
        Iterator<LongART.Entry> it = art.getTailEntryIterator(prefixKey, false);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testHasNextEntryIteratorSingleEntryART() {
        LongART art = new LongART(heap);
        art.put(firstKey, firstValue);
        Iterator<LongART.Entry> it = art.getEntryIterator();
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(it.hasNext());
        }
    }

    @Test
    public void testNextEntryIteratorSingleEntryART() {
        LongART art = new LongART(heap);
        art.put(firstKey, firstValue);
        Iterator<LongART.Entry> it = art.getEntryIterator();
        LongART.Entry e = it.next();
        Assert.assertEquals(e.getKey(), firstKey);
        Assert.assertEquals(e.getValue(), firstValue);
        for (int i = 0; i < 10; i++) {
            Assert.assertFalse(it.hasNext());
        }
    }

    @Test
    public void testIterateEntryIterator() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getEntryIterator();
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.entrySet()) {
            if (it.hasNext()) entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIterator() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getEntryIterator(firstKey, false, lastKey, false);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(firstKey), false, new KeyBytes(lastKey), false).entrySet()) {
            if (it.hasNext()) entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorPrefix() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getEntryIterator(prefixKey, true, lastKey, false);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(prefixKey), true, new KeyBytes(lastKey), false).entrySet()) {
            if (it.hasNext()) entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateHeadEntryIteratorPrefix() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getHeadEntryIterator(prefixKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.headMap(new KeyBytes(prefixKey), true).entrySet()) {
            if (it.hasNext()) entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateTailEntryIteratorPrefix() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getTailEntryIterator(prefixKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.tailMap(new KeyBytes(prefixKey), true).entrySet()) {
            if (it.hasNext()) entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }
}
