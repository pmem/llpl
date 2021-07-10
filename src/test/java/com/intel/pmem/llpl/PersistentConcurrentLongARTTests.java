/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import com.intel.pmem.llpl.*;
import com.intel.pmem.llpl.util.LongART;
import com.intel.pmem.llpl.util.ConcurrentLongART;

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
public class PersistentConcurrentLongARTTests {
    PersistentHeap heap = null;
    static byte[] firstKey;
    static byte[] lastKey;
    static byte[] prefixKey;
    static long firstValue;
    static long lastValue;
    static long prefixValue;
    static final long TREESIZE = 5000;
    static final long SEED = 123456789;
    static Random rnd = new Random(SEED);

    @BeforeMethod
    public void initialize() {
        heap = TestVars.createPersistentHeap();
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

    static ConcurrentLongART getCart(AnyHeap heap) {
        return new ConcurrentLongART(heap, 4);
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

    static void fill (ConcurrentLongART tree, ConcurrentSkipListMap<KeyBytes, Long> control) {
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

    static void fill (ConcurrentLongART tree) {
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
    public void testHandleEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        Assert.assertTrue(cart.handle() > 0);
    }

    @Test
    public void testSizeEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        Assert.assertEquals(cart.size(), 0);
    }

    @Test
    public void testHandleRecreatedEmptycart() {
        long cartHandle = (getCart(heap)).handle(); 
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Assert.assertTrue(cart.handle() > 0);
    }

    @Test
    public void testSizeRecreatedEmptycart() {
        long cartHandle = (getCart(heap)).handle(); 
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Assert.assertEquals(cart.size(), 0);
    }

    @Test
    public void testSizeFilled() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        Assert.assertEquals(cart.size(), TREESIZE);
    }

    @Test
    public void testSizeRecreatedFilled() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        ConcurrentLongART newCart = ConcurrentLongART.fromHandle(heap, cart.handle());
        Assert.assertEquals(newCart.size(), TREESIZE);
    }
        
    @Test
    public void testSizeClearedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        Assert.assertEquals(cart.size(), 0);
    }

    @Test
    public void testSizeRecreatedClearedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {return;});
        ConcurrentLongART recreatedCart = ConcurrentLongART.fromHandle(heap, cart.handle());
        Assert.assertEquals(recreatedCart.size(), 0);
    }

    @Test
    public void testHandleFreedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        cart.free();
        try {
            cart.handle();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testSizeFreedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        cart.free();
        try {
            cart.size();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testHashCodeStableSession() {
        ConcurrentLongART cart1 = new ConcurrentLongART(heap, 4);
        int hash1 = cart1.hashCode();
        ConcurrentLongART cart2 = ConcurrentLongART.fromHandle(heap, cart1.handle());
        int hash2 = cart2.hashCode();
        Assert.assertEquals(hash1, hash2);
    }

    @Test
    public void testEquality() {
        ConcurrentLongART cart1 = new ConcurrentLongART(heap, 4);
        ConcurrentLongART cart2 = ConcurrentLongART.fromHandle(heap, cart1.handle());
        Assert.assertEquals(cart1, cart2);
    }

    /* reconstruct */
    @Test
    public void testReconstructNegativeHandle() {
        try {
            ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, -1234L);
            Assert.fail("IllegalArgumentException wasnt thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testReconstructZeroHandle() {
        try {
            ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, 0);
            Assert.fail("IllegalArgumentException wasnt thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testReconstructBogusHandle() {
        try {
            ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, heap.size() / 2);
            Assert.fail("HeapException wasnt thrown");
        } 
        catch (HeapException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testReconstructTooBigHandle() {
        try {
            ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, Long.MAX_VALUE);
            Assert.fail("HeapException wasnt thrown");
        } 
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    /* FirstKey() and LastKey() */
    @Test
    public void testFirstkeyEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        try {
            cart.firstKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLastkeyEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        try {
            cart.lastKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFirstkeyRecreatedEmptycart() {
        long cartHandle = (getCart(heap)).handle(); 
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        try {
            cart.firstKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLastkeyRecreatedEmptycart() {
        long cartHandle = (getCart(heap)).handle(); 
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        try {
            cart.lastKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFirstkeyFilledcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        Assert.assertEquals(cart.firstKey(), firstKey);
    }

    @Test
    public void testLastkeyFilledcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        Assert.assertEquals(cart.lastKey(), lastKey);
    }

    @Test
    public void testFirstkeyRecreatedFilledcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        ConcurrentLongART recreatedCart = ConcurrentLongART.fromHandle(heap, cart.handle());
        Assert.assertEquals(recreatedCart.firstKey(), firstKey);
    }

    @Test
    public void testLastkeyRecreatedFilledcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        ConcurrentLongART recreatedCart = ConcurrentLongART.fromHandle(heap, cart.handle());
        Assert.assertEquals(recreatedCart.lastKey(), lastKey);
    }

    @Test
    public void testFirstkeyClearedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        try {
            cart.firstKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLastkeyClearedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        try {
            cart.lastKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFirstkeyRecreatedClearedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        ConcurrentLongART recreatedArt = ConcurrentLongART.fromHandle(heap, cart.handle());
        try {
            cart.firstKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLastkeyRecreatedClearedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        ConcurrentLongART recreatedArt = ConcurrentLongART.fromHandle(heap, cart.handle());
        try {
            cart.lastKey();
            Assert.fail("NoSuchElementException wasnt thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testFirstKeyFreedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        cart.free();
        try {
            cart.firstKey();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testLastKeyFreedcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        cart.free();
        try {
            cart.lastKey();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }

    /* Clear tests */
    @Test
    public void testClearEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        cart.clear(c ->{});
        Assert.assertEquals(cart.size(), 0);
    }

    @Test
    public void testClearReconstructedEmptycart() {
        long cartHandle = (getCart(heap)).handle(); 
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        cart.clear(c ->{});
        Assert.assertEquals(cart.size(), 0);
    }

    @Test
    public void testClearNullConsumer() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        try {
            cart.clear(null);
            Assert.fail("IllegalArgumentException wasnt thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testDoubleClearEmpty() {
        ConcurrentLongART cart = getCart(heap);
        cart.clear(c ->{});
        cart.clear(c ->{});
        Assert.assertEquals(cart.size(), 0);
    }

    @Test
    public void testDoubleClearReconstructedEmpty() {
        long cartHandle = (getCart(heap)).handle(); 
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        cart.clear(c ->{});
        cart.clear(c ->{});
        Assert.assertEquals(cart.size(), 0);
    }

    @Test
    public void testDoubleClearFull() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c ->{});
        cart.clear(c ->{});
        Assert.assertEquals(cart.size(), 0);
    }

    @Test
    public void testDoubleClearReconstructedFull() {
        ConcurrentLongART cart = getCart(heap);
        long cartHandle = cart.handle(); 
        fill(cart);
        ConcurrentLongART newCart = ConcurrentLongART.fromHandle(heap, cartHandle);
        newCart.clear(c ->{});
        newCart.clear(c ->{});
        Assert.assertEquals(newCart.size(), 0);
    }

    @Test 
    void testClearAfterFree() {
        ConcurrentLongART cart = getCart(heap);
        cart.free();
        try {
            cart.clear(c -> {});
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    /* Free Tests */

    @Test 
    void testFreeReconstructedEmpty() {
        long cartHandle = (getCart(heap)).handle(); 
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        cart.free();
    }

    @Test 
    void testFreeReconstructedFull() {
        ConcurrentLongART cart = getCart(heap);
        long cartHandle = cart.handle(); 
        fill(cart);
        ConcurrentLongART newArt = ConcurrentLongART.fromHandle(heap, cartHandle);
        // no need for clear()
        cart.free();
    }

    @Test
    void testDoubleFree() {
        ConcurrentLongART cart = getCart(heap);
        cart.free();
        try {
            cart.free();
            Assert.fail("IllegalStateException wasnt thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true);
        }
    }

    /* Insertion Tests */

    @Test
    public void testInsertNullKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            cart.put(null, new Long(1234));
            Assert.fail("IllegalArgumentException wasnt thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testInsertZeroLengthKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            cart.put(new byte[]{}, new Long(1234));
            Assert.fail("IllegalArgumentException wasnt thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testInsertInvalidMerge() {
        ConcurrentLongART cart = getCart(heap);
        try {
            cart.put(getKey(8), new Long(1234), (a, b) -> { return null; });
            Assert.fail("NullPointerException wasnt thrown");
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testInsertSingle() {
        ConcurrentLongART cart = getCart(heap);
        cart.put(getKey(8), 1234L);
        Assert.assertEquals(cart.size(), 1L);
    }

    @Test
    public void testInsert() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        Assert.assertEquals(cart.size(), TREESIZE);
    }

    @Test
    public void testInsertSingleMerge() {
        ConcurrentLongART cart = getCart(heap);
        cart.put(getKey(8), 1234L, (a, b) -> { return 1234L; });
        Assert.assertEquals(cart.size(), 1L);
    }
    /* Get tests */
    
    @Test
    public void testGetNullKey() {
        ConcurrentLongART cart = getCart(heap); 
        fill(cart);
        try {
            cart.get(null);
            Assert.fail("IllegalArgumentException wasnt thrown"); 
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetZeroLengthKey() {
        ConcurrentLongART cart = getCart(heap); 
        fill(cart);
        try {
            cart.get(new byte[]{});
            Assert.fail("IllegalArgumentException was not thrown"); 
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testGetEmpty() {
        ConcurrentLongART cart = getCart(heap); 
        Assert.assertEquals(cart.get(firstKey), 0L);
    }

    @Test
    public void testGetReconstructedEmpty() {
        long cartHandle = (getCart(heap)).handle();
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle); 
        Assert.assertEquals(cart.get(firstKey), 0L);
    }

    @Test
    public void testGetFirstFilled() {
        ConcurrentLongART cart = getCart(heap); 
        fill(cart);
        Assert.assertEquals(cart.get(firstKey), firstValue);
    }

    @Test
    public void testGetLastFilled() {
        ConcurrentLongART cart = getCart(heap); 
        fill(cart);
        Assert.assertEquals(cart.get(lastKey), lastValue);
    }

    @Test
    public void testGetPrefixFilled() {
        ConcurrentLongART cart = getCart(heap); 
        fill(cart);
        Assert.assertEquals(cart.get(prefixKey), prefixValue);
    }

    @Test
    public void testGetReconstructedFirstFilled() {
        ConcurrentLongART cart = getCart(heap); 
        fill(cart);
        long cartHandle = cart.handle();
        ConcurrentLongART newArt = ConcurrentLongART.fromHandle(heap, cartHandle); 
        Assert.assertEquals(cart.get(firstKey), firstValue);
    }

    @Test
    public void testGetReconstructedLastFilled() {
        ConcurrentLongART cart = getCart(heap); 
        fill(cart);
        long cartHandle = cart.handle();
        ConcurrentLongART newArt = ConcurrentLongART.fromHandle(heap, cartHandle); 
        Assert.assertEquals(cart.get(lastKey), lastValue);
    }
    
    @Test
    public void testGetReconstructedPrefixFilled() {
        ConcurrentLongART cart = getCart(heap); 
        fill(cart);
        long cartHandle = cart.handle();
        ConcurrentLongART newArt = ConcurrentLongART.fromHandle(heap, cartHandle); 
        Assert.assertEquals(cart.get(prefixKey), prefixValue);
    }

    // EntryIterator Tests
    // since iterators are volatile is there any need to test on rebuild?
    // maybe only when full.
    @Test
    public void testHasNextEntryIteratorNewEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<LongART.Entry> it = cart.getEntryIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextRangeEntryIteratorNewEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<LongART.Entry> it = cart.getEntryIterator(firstKey, true, lastKey, true);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testRangeEntryIteratorNullFromKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getEntryIterator(null, true, lastKey, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeEntryIteratorNullToKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getEntryIterator(firstKey, true, null, false);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeEntryIteratorZeroLengthFromKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getEntryIterator(new byte[]{}, true, lastKey, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeEntryIteratorZeroLengthToKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getEntryIterator(firstKey, true, new byte[]{}, false);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeReverseEntryIteratorNullFromKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getReverseEntryIterator(null, true, lastKey, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeReverseEntryIteratorNullToKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getReverseEntryIterator(firstKey, true, null, false);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeReverseEntryIteratorZeroLengthFromKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getReverseEntryIterator(new byte[]{}, true, lastKey, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testRangeReverseEntryIteratorZeroLengthToKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getReverseEntryIterator(firstKey, true, new byte[]{}, false);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true); 
        }
    }

    @Test
    public void testHasNextHeadEntryIteratorNewEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<LongART.Entry> it = cart.getHeadEntryIterator(prefixKey, true);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHeadEntryIteratorNullKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getHeadEntryIterator(null, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testHeadEntryIteratorZeroLengthKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getHeadEntryIterator(new byte[]{}, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testHasNextTailEntryIteratorNewEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<LongART.Entry> it = cart.getTailEntryIterator(prefixKey, true);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testTailEntryIteratorNullKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getTailEntryIterator(null, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testTailEntryIteratorZeroLengthKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            Iterator<LongART.Entry> it = cart.getTailEntryIterator(new byte[]{}, true);
            Assert.fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

   @Test
    public void testHasNextEntryIteratorRecreatedEmptycart() {
        long cartHandle = getCart(heap).handle();
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Iterator<LongART.Entry> it = cart.getEntryIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextRangeEntryIteratorRecreatedEmptycart() {
        long cartHandle = getCart(heap).handle();
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Iterator<LongART.Entry> it = cart.getEntryIterator(firstKey, false, lastKey, false);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextHeadEntryIteratorRecreatedEmptycart() {
        long cartHandle = getCart(heap).handle();
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Iterator<LongART.Entry> it = cart.getHeadEntryIterator(prefixKey, false);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testHasNextTailEntryIteratorRecreatedEmptycart() {
        long cartHandle = getCart(heap).handle();
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Iterator<LongART.Entry> it = cart.getTailEntryIterator(prefixKey, false);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testNextNewEmptyEntryIterator() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<LongART.Entry> it = cart.getEntryIterator();
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextNewEmptyRangeEntryIterator() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<LongART.Entry> it = cart.getEntryIterator(firstKey, true, lastKey, false);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextNewEmptyHeadEntryIterator() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<LongART.Entry> it = cart.getHeadEntryIterator(prefixKey, true);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextNewEmptyTailEntryIterator() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<LongART.Entry> it = cart.getTailEntryIterator(prefixKey, true);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testHasNextReverseEntryIteratorNewEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<LongART.Entry> it = cart.getReverseEntryIterator();
        Assert.assertFalse(it.hasNext());
    }

    /*@Test
    public void testHasNextValueIteratorNewEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        Iterator<Long> it = cart.getValueIterator();
        Assert.assertFalse(it.hasNext());
    }*/

    @Test
    public void testNextEntryIteratorRecreatedEmptycart() {
        long cartHandle = getCart(heap).handle();
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Iterator<LongART.Entry> it = cart.getEntryIterator();
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextRangeEntryIteratorRecreatedEmptycart() {
        long cartHandle = getCart(heap).handle();
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Iterator<LongART.Entry> it = cart.getEntryIterator(firstKey, false, lastKey, true);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextHeadEntryIteratorRecreatedEmptycart() {
        long cartHandle = getCart(heap).handle();
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Iterator<LongART.Entry> it = cart.getHeadEntryIterator(prefixKey, false);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNextTailEntryIteratorRecreatedEmptycart() {
        long cartHandle = getCart(heap).handle();
        ConcurrentLongART cart = ConcurrentLongART.fromHandle(heap, cartHandle);
        Iterator<LongART.Entry> it = cart.getTailEntryIterator(prefixKey, false);
        try {
            it.next();
            Assert.fail("NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testHasNextEntryIteratorSingleEntrycart() {
        ConcurrentLongART cart = getCart(heap);
        cart.put(firstKey, firstValue);
        Iterator<LongART.Entry> it = cart.getEntryIterator();
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(it.hasNext());
        }
    }

    @Test
    public void testNextEntryIteratorSingleEntrycart() {
        ConcurrentLongART cart = getCart(heap);
        cart.put(firstKey, firstValue);
        Iterator<LongART.Entry> it = cart.getEntryIterator();
        LongART.Entry e = it.next();
        Assert.assertEquals(e.getKey(), firstKey);
        Assert.assertEquals(e.getValue(), firstValue);
        for (int i = 0; i < 10; i++) {
            Assert.assertFalse(it.hasNext());
        }
    }

    @Test
    public void testIterateEntryIterator() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);
        ConcurrentLongART.fromHandle(heap, cart.handle());

        Iterator<LongART.Entry> it = cart.getEntryIterator();
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateEntryIteratorRecreatedART() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);
        ConcurrentLongART newCart = ConcurrentLongART.fromHandle(heap, cart.handle());
        Iterator<LongART.Entry> it = newCart.getEntryIterator();
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIterator() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);

        Iterator<LongART.Entry> it = cart.getEntryIterator(firstKey, false, lastKey, false);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(firstKey), false, new KeyBytes(lastKey), false).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorEqualRangeKeysPresent() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);

        Iterator<LongART.Entry> it = cart.getEntryIterator(prefixKey, true, prefixKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(prefixKey), true, new KeyBytes(prefixKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorEqualRangeKeysAbsent() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);
        byte[] fromKey = getKey(20);
        Iterator<LongART.Entry> it = cart.getEntryIterator(fromKey, true, fromKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(fromKey), true, new KeyBytes(fromKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorFromKeyAbsentPreRange() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);
        Iterator<LongART.Entry> it = cart.getEntryIterator(new byte[]{(byte)0}, true, lastKey, true);

        LongART.Entry entry = null;
        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(new byte[]{(byte)0}), true, new KeyBytes(lastKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorFromKeyAbsentInRange() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);
        byte[] fromKey = getKey(20);
        Iterator<LongART.Entry> it = cart.getEntryIterator(fromKey, true, lastKey, true);

        LongART.Entry entry = null;
        int i = 0;
        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(fromKey), true, new KeyBytes(lastKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorFromKeyAbsentInRangePartialPrefixMatch() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);
        byte[] fromKey = Arrays.copyOfRange(lastKey, 0, 8);
        Iterator<LongART.Entry> it = cart.getEntryIterator(fromKey, true, lastKey, true);

        LongART.Entry entry = null;
        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(fromKey), true, new KeyBytes(lastKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeTailEntryIteratorFromKeyAbsentPostRange() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);
        byte[] fromKey = new byte[24];
        Arrays.fill(fromKey, (byte)0xff);
        Iterator<LongART.Entry> it = cart.getTailEntryIterator(fromKey, true);

        LongART.Entry entry = null;
        for (Map.Entry<KeyBytes, Long> e : control.tailMap(new KeyBytes(fromKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateReverseEntryIterator() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);

        Iterator<LongART.Entry> it = cart.getReverseEntryIterator();
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.descendingMap().entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeReverseEntryIterator() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);

        Iterator<LongART.Entry> it = cart.getReverseEntryIterator(firstKey, false, lastKey, false);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(firstKey), false, new KeyBytes(lastKey), false).descendingMap().entrySet()) {
            Assert.assertTrue(it.hasNext());
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeEntryIteratorPrefix() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);

        Iterator<LongART.Entry> it = cart.getEntryIterator(prefixKey, true, lastKey, false);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(prefixKey), true, new KeyBytes(lastKey), false).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateHeadEntryIteratorPrefix() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);
cart = ConcurrentLongART.fromHandle(heap, cart.handle()); 

        Iterator<LongART.Entry> it = cart.getHeadEntryIterator(prefixKey, true);
        LongART.Entry entry = null;
Integer cnt = 0;
       for (Map.Entry<KeyBytes, Long> e : control.headMap(new KeyBytes(prefixKey), true).entrySet()) {
            entry = it.next();
            cnt++;
            Assert.assertEquals(entry.getKey(), e.getKey().get(), cnt.toString());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateTailEntryIteratorPrefix() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);

        Iterator<LongART.Entry> it = cart.getTailEntryIterator(prefixKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.tailMap(new KeyBytes(prefixKey), true).entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }

    @Test
    public void testIterateRangeReverseEntryIteratorPrefix() {
        ConcurrentLongART cart = getCart(heap);
        ConcurrentSkipListMap<KeyBytes, Long> control = new ConcurrentSkipListMap();
        fill(cart, control);

        Iterator<LongART.Entry> it = cart.getReverseEntryIterator(prefixKey, true, lastKey, true);
        LongART.Entry entry = null;

        for (Map.Entry<KeyBytes, Long> e : control.subMap(new KeyBytes(prefixKey), true, new KeyBytes(lastKey), true).descendingMap().entrySet()) {
            entry = it.next();
            Assert.assertEquals(entry.getKey(), e.getKey().get());
            Assert.assertEquals(entry.getValue(), e.getValue().longValue());
        }
    }
   
    // Delete tests
    public void testDeleteNullKey() {
        ConcurrentLongART cart = getCart(heap);
        try {
            cart.remove(null, c -> {}); 
            Assert.fail("NullPointerException was not thrown");
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }
    }

    public void testDeleteNullCleaner() {
        ConcurrentLongART cart = getCart(heap);
        try {
            cart.remove(firstKey, null); 
            Assert.fail("NullPointerException was not thrown");
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }
    }

    public void testDeleteNewEmptycart() {
        ConcurrentLongART cart = getCart(heap);
        cart.remove(firstKey, (Long l) -> { Assert.assertTrue(l == 0L); });
    }

    public void testDeleteFilledcart() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.remove(lastKey, (Long l) -> { Assert.assertTrue(l == lastValue); });
    }
    
    public void testDoubleDelete() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.remove(prefixKey, c -> {});
        cart.remove(prefixKey, (Long l) -> { Assert.assertTrue(l == 0L); });
    }

    public void testGetAfterDelete() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.remove(firstKey, c -> {});
        Assert.assertEquals(cart.get(firstKey), 0L);
    }
    
    public void testDeleteAfterClear() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        cart.remove(lastKey, (Long l) -> { Assert.assertTrue(l == 0L); });
    }

    public void testDeleteAfterFree() {
        ConcurrentLongART cart = getCart(heap);
        fill(cart);
        cart.clear(c -> {});
        cart.free();
        try {
            cart.remove(lastKey, c -> {});
            Assert.fail("IllegalStateException was not thrown");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true); 
        }
    }
}
