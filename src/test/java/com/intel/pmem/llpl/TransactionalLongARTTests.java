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
public class TransactionalLongARTTests {
    TransactionalHeap heap = null;
    static byte[] firstKey;
    static byte[] lastKey;
    static byte[] prefixKey;
    static long firstValue;
    static long lastValue;
    static long prefixValue;
    static final long TREESIZE = 120;
    static final long SEED = 123456789;
    static Random rnd = new Random(SEED);

    @BeforeMethod
    public void initialize() {
        heap = TestVars.createTransactionalHeap();
        firstKey = new byte[8]; Arrays.fill(firstKey, (byte)0);
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
        LongART art = LongART.fromHandle(heap, artHandle);
        Assert.assertTrue(art.handle() > 0);
    }

    @Test
    public void testSizeRecreatedEmptyART() {
        long artHandle = (new LongART(heap)).handle(); 
        LongART art = LongART.fromHandle(heap, artHandle);
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
        LongART newArt = LongART.fromHandle(heap, art.handle());
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
        LongART recreatedArt = LongART.fromHandle(heap, art.handle());
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

    @Test
    public void testHashCodeStableSession() {
        LongART art1 = new LongART(heap);
        int hash1 = art1.hashCode();
        LongART art2 = LongART.fromHandle(heap, art1.handle());
        int hash2 = art2.hashCode();
        Assert.assertEquals(hash1, hash2);
    }

    @Test
    public void testEquality() {
        LongART art1 = new LongART(heap);
        LongART art2 = LongART.fromHandle(heap, art1.handle());
        Assert.assertEquals(art1, art2);
    }

    /* reconstruct */
    @Test
    public void testReconstructNegativeHandle() {
        try {
            LongART art = LongART.fromHandle(heap, -1234L);
            Assert.fail("IllegalArgumentException wasnt thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testReconstructZeroHandle() {
        try {
            LongART art = LongART.fromHandle(heap, 0);
            Assert.fail("IllegalArgumentException wasnt thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testReconstructBogusHandle() {
        try {
            LongART art = LongART.fromHandle(heap, heap.size() / 2);
            Assert.fail("HeapException wasnt thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testReconstructTooBigHandle() {
        try {
            LongART art = LongART.fromHandle(heap, Long.MAX_VALUE);
            Assert.fail("IllegalArgumentException wasnt thrown");
        } 
        catch (IllegalArgumentException e) {
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
        LongART art = LongART.fromHandle(heap, artHandle);
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
        LongART art = LongART.fromHandle(heap, artHandle);
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
        LongART recreatedArt = LongART.fromHandle(heap, art.handle());
        Assert.assertEquals(recreatedArt.firstKey(), firstKey);
    }

    @Test
    public void testLastkeyRecreatedFilledART() {
        LongART art = new LongART(heap);
        fill(art);
        LongART recreatedArt = LongART.fromHandle(heap, art.handle());
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
        LongART recreatedArt = LongART.fromHandle(heap, art.handle());
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
        LongART recreatedArt = LongART.fromHandle(heap, art.handle());
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
        LongART art = LongART.fromHandle(heap, artHandle);
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
        LongART art = LongART.fromHandle(heap, artHandle);
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
        LongART newArt = LongART.fromHandle(heap, artHandle);
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
        LongART art = LongART.fromHandle(heap, artHandle);
        art.free();
    }

    @Test 
    void testFreeReconstructedFull() {
        LongART art = new LongART(heap);
        long artHandle = art.handle(); 
        fill(art);
        LongART newArt = LongART.fromHandle(heap, artHandle);
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
    public void testInsertInvalidMerge() {
        LongART art = new LongART(heap);
        try {
            art.put(getKey(8), new Long(1234), (a, b) -> { return null; });
            Assert.fail("NullPointerException wasnt thrown");
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testInsertSingle() {
        LongART art = new LongART(heap);
        art.put(getKey(8), 1234L);
        Assert.assertEquals(art.size(), 1L);
    }

    @Test
    public void testReplacement() {
        LongART art = new LongART(heap);
        byte[] key = getKey(8);
        art.put(key, 1234L);
        fill(art);
        Assert.assertEquals(art.put(key, 5678), 1234L);
    }

    @Test
    public void testInsertSimilarShorterKey() {
        LongART art = new LongART(heap);
        byte[] key = getKey(8);
        art.put(key, 1234L);
        fill(art);
        Assert.assertEquals(art.put(Arrays.copyOf(key,7), 1234L), 0);
    }

    @Test
    public void testInsertBlankRadixKey() {
        LongART art = new LongART(heap);
        fill(art);
        Assert.assertEquals(art.put(Arrays.copyOf(prefixKey,11), 1234L), 0);
    }

    @Test
    public void testInsertBlankRadixKeyAndGrow() {
        LongART art = new LongART(heap);
        String s1 = "This is the first key";
        String s2 = "This is the next one!";
        String s3 = "This is the third key";
        String s4 = "This is the 4th key!!";
        String s5 = "This is the ";
        art.put(s1.getBytes(), 1234L);
        art.put(s2.getBytes(), 1234L);
        art.put(s3.getBytes(), 1234L);
        art.put(s4.getBytes(), 1234L);
        art.put(s5.getBytes(), 1234L);
        Assert.assertEquals(art.size(), 5L);
    }

    @Test
    public void testReplaceBlankRadixKey() {
        LongART art = new LongART(heap);
        fill(art);
        art.put(Arrays.copyOf(prefixKey, 11), 1234L);
        Assert.assertEquals(art.put(Arrays.copyOf(prefixKey, 11), 5678L), 1234L);
    }

    @Test
    public void testInsertSimilarLongerKey() {
        LongART art = new LongART(heap);
        byte[] key = getKey(8);
        byte[] key2 = Arrays.copyOfRange(key, 0, 10);
        art.put(key, 1234L);
        fill(art);
        Assert.assertEquals(art.put(key2, 5678L), 0);
    }

    @Test
    public void testInsert() {
        LongART art = new LongART(heap);
        fill(art);
        Assert.assertEquals(art.size(), TREESIZE);
    }

    @Test
    public void testInsertSimilarKey() {
        LongART art = new LongART(heap);
        String s1 = "This is a long key";
        String s2 = "This isnt the same key";
        art.put(s1.getBytes(), 1234L);
        art.put(s2.getBytes(), 5678L);
        Assert.assertEquals(art.size(), 2L);
    }

    @Test
    public void testInsertSingleMerge() {
        LongART art = new LongART(heap);
        art.put(getKey(8), 1234L, (a, b) -> { return 1234L; });
        Assert.assertEquals(art.size(), 1L);
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
        LongART art = LongART.fromHandle(heap, artHandle); 
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
        LongART newArt = LongART.fromHandle(heap, artHandle); 
        Assert.assertEquals(art.get(firstKey), firstValue);
    }

    @Test
    public void testGetReconstructedLastFilled() {
        LongART art = new LongART(heap); 
        fill(art);
        long artHandle = art.handle();
        LongART newArt = LongART.fromHandle(heap, artHandle); 
        Assert.assertEquals(art.get(lastKey), lastValue);
    }
    
    @Test
    public void testGetReconstructedPrefixFilled() {
        LongART art = new LongART(heap); 
        fill(art);
        long artHandle = art.handle();
        LongART newArt = LongART.fromHandle(heap, artHandle); 
        Assert.assertEquals(art.get(prefixKey), prefixValue);
    }

    // EntryIterator Tests
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
    public void testRangeReverseEntryIteratorNullFromKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getReverseEntryIterator(null, true, lastKey, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeReverseEntryIteratorNullToKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getReverseEntryIterator(firstKey, true, null, false);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeReverseEntryIteratorZeroLengthFromKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getReverseEntryIterator(new byte[]{}, true, lastKey, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeReverseEntryIteratorZeroLengthToKey() {
        LongART art = new LongART(heap);
        try {
            Iterator<LongART.Entry> it = art.getReverseEntryIterator(firstKey, true, new byte[]{}, false);
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
        LongART art = LongART.fromHandle(heap, artHandle);
        Iterator<LongART.Entry> it = art.getEntryIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextRangeEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = LongART.fromHandle(heap, artHandle);
        Iterator<LongART.Entry> it = art.getEntryIterator(firstKey, false, lastKey, false);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextHeadEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = LongART.fromHandle(heap, artHandle);
        Iterator<LongART.Entry> it = art.getHeadEntryIterator(prefixKey, false);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextTailEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = LongART.fromHandle(heap, artHandle);
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
    public void testHasNextReverseEntryIteratorNewEmptyART() {
        LongART art = new LongART(heap);
        Iterator<LongART.Entry> it = art.getReverseEntryIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextValueIteratorNewEmptyART() {
        LongART art = new LongART(heap);
        Iterator<Long> it = art.getValueIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testNextEntryIteratorRecreatedEmptyART() {
        long artHandle = new LongART(heap).handle();
        LongART art = LongART.fromHandle(heap, artHandle);
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
        LongART art = LongART.fromHandle(heap, artHandle);
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
        LongART art = LongART.fromHandle(heap, artHandle);
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
        LongART art = LongART.fromHandle(heap, artHandle);
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
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateEntryIteratorRecreatedART() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);
        LongART newArt = LongART.fromHandle(heap, art.handle());
        Iterator<LongART.Entry> it = newArt.getEntryIterator();

        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeSingleEntryIteratorEq() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        art.put(firstKey, firstValue);
        control.put(new KeyBytes(firstKey), firstValue);

        Iterator<LongART.Entry> it = art.getEntryIterator(firstKey, true, lastKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(firstKey), true, new KeyBytes(lastKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeSingleEntryIteratorLT() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        art.put(firstKey, firstValue);
        control.put(new KeyBytes(firstKey), firstValue);
        byte[] fromKey = getKey(4);

        Iterator<LongART.Entry> it = art.getEntryIterator(fromKey, true, lastKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(fromKey), true, new KeyBytes(lastKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeSingleEntryIteratorPrefix() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        art.put(firstKey, firstValue);
        control.put(new KeyBytes(firstKey), firstValue);

        Iterator<LongART.Entry> it = art.getEntryIterator(prefixKey, true, lastKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(prefixKey), true, new KeyBytes(lastKey), true).entrySet()) {
            entry = it.next();
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
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorInvalideRange() {
        LongART art = new LongART(heap);
        fill(art);
        try {
            Iterator<LongART.Entry> it = art.getEntryIterator(lastKey, false, firstKey, false);
            Assert.fail("IllegalArgumenException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testIterateRangeReverseEntryIteratorInvalideRange() {
        LongART art = new LongART(heap);
        fill(art);
        try {
            Iterator<LongART.Entry> it = art.getReverseEntryIterator(lastKey, false, firstKey, false);
            Assert.fail("IllegalArgumenException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }


    @Test
    public void testIterateRangeEntryIteratorEqualRangeKeysPresent() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getEntryIterator(prefixKey, true, prefixKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(prefixKey), true, new KeyBytes(prefixKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorEqualRangeKeysAbsent() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);
        byte[] fromKey = getKey(20);
        Iterator<LongART.Entry> it = art.getEntryIterator(fromKey, true, fromKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(fromKey), true, new KeyBytes(fromKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorFromKeyAbsentPreRange() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);
        Iterator<LongART.Entry> it = art.getEntryIterator(new byte[]{(byte)0}, true, lastKey, true);

        LongART.Entry entry = null;
        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(new byte[]{(byte)0}), true, new KeyBytes(lastKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorFromKeyAbsentInRange() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);
        byte[] fromKey = getKey(20);
        Iterator<LongART.Entry> it = art.getEntryIterator(fromKey, true, lastKey, true);

        LongART.Entry entry = null;
        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(fromKey), true, new KeyBytes(lastKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorFromKeyAbsentInRangePartialPrefixMatch() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);
        byte[] fromKey = Arrays.copyOfRange(lastKey, 0, 8);
        Iterator<LongART.Entry> it = art.getEntryIterator(fromKey, true, lastKey, true);

        LongART.Entry entry = null;
        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(fromKey), true, new KeyBytes(lastKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeTailEntryIteratorFromKeyAbsentPostRange() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);
        byte[] fromKey = new byte[24];
        Arrays.fill(fromKey, (byte)0xff);
        Iterator<LongART.Entry> it = art.getTailEntryIterator(fromKey, true);

        LongART.Entry entry = null;
        for (Map.Entry<KeyBytes, Long> e : control.tailMap(new KeyBytes(fromKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorHead() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getEntryIterator(new byte[]{(byte)8}, true, lastKey, false);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(new byte[]{(byte)8}), true, new KeyBytes(lastKey), false).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateReverseEntryIterator() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getReverseEntryIterator();
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.descendingMap().entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeReverseEntryIterator() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getReverseEntryIterator(firstKey, false, lastKey, false);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(firstKey), false, new KeyBytes(lastKey), false).descendingMap().entrySet()) {
            Assert.assertTrue(it.hasNext());
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateValueIterator() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<Long> it = art.getValueIterator();
        Long value = null;

        for (Long e : control.values()) {
            value = it.next();
            Assert.assertEquals(value, e);
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
            entry = it.next();
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
            entry = it.next();
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
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeReverseEntryIteratorPrefix() {
        LongART art = new LongART(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(art, control);

        Iterator<LongART.Entry> it = art.getReverseEntryIterator(prefixKey, true, lastKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(prefixKey), true, new KeyBytes(lastKey), true).descendingMap().entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }
   
    // Delete tests
    public void testDeleteNullKey() {
        LongART art = new LongART(heap);
        try {
            art.remove(null, c -> {}); 
            Assert.fail("NullPointerException was not thrown");
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }
    }

    public void testDeleteNullCleaner() {
        LongART art = new LongART(heap);
        try {
            art.remove(firstKey, null); 
            Assert.fail("NullPointerException was not thrown");
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }
    }

    public void testDeleteNewEmptyART() {
        LongART art = new LongART(heap);
        art.remove(firstKey, (Long l) -> { Assert.assertTrue(l == 0L); });
    }

    public void testDeleteFilledART() {
        LongART art = new LongART(heap);
        fill(art);
        art.remove(lastKey, (Long l) -> { Assert.assertTrue(l == lastValue); });
    }
    
    public void testDoubleDelete() {
        LongART art = new LongART(heap);
        fill(art);
        art.remove(prefixKey, c -> {});
        art.remove(prefixKey, (Long l) -> { Assert.assertTrue(l == 0L); });
    }

    public void testGetAfterDelete() {
        LongART art = new LongART(heap);
        fill(art);
        art.remove(firstKey, c -> {});
        Assert.assertEquals(art.get(firstKey), 0L);
    }
    
    public void testDeleteAfterClear() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        art.remove(lastKey, (Long l) -> { Assert.assertTrue(l == 0L); });
    }

    public void testDeleteAfterFree() {
        LongART art = new LongART(heap);
        fill(art);
        art.clear(c -> {});
        art.free();
        try {
            art.remove(lastKey, c -> {});
            Assert.fail("IllegalStateException was not thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }

    // Split tests
    public void testSplitEmptyART() {
        LongART art = new LongART(heap);
        Assert.assertNull(art.split());
    }
}
