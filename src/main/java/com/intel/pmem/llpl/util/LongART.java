/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.AnyHeap;
import com.intel.pmem.llpl.AnyMemoryBlock;
import com.intel.pmem.llpl.HeapException;
import com.intel.pmem.llpl.Range;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.function.*;
import java.util.Optional;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An implementation of an Adaptive Radix Tree that uses {@code byte[]} for keys and {@code long} for values. 
 * The radix tree can be created using different heap types. Given a persistent heap, the radix tree will store values durably,
 * and given a transactional heap, it will store values transactionally.<br><br>
 * <b>This implementation is not thread-safe.</b> If multiple threads access a tree, and one or more of them modifies
 * the tree, then it must be synchronized externally. 
 * @since 1.2
 */

public class LongART implements DynamicShardable<byte[]> {
    final AnyHeap heap;
    private Root root;
    private int maxKeyLen;
    private long count = 0;
    private byte[] lastKey;
    private static final short VERSION = 100;

    /**
     * Creates a new radix tree.
     * The semantics of this method depend on the heap supplied.
     * Given a persistent heap, the radix tree will store values durably, and given
     * a transactional heap will store values transactionally. To reaccess this radix tree, for
     * example after a restart, call {@link LongART#fromHandle(AnyHeap, long)}
     * @param heap the heap on which to allocate the radix tree
     * @throws HeapException if the radix tree could not be created
     */
    public LongART(AnyHeap heap) {
        registerAllocationClasses(heap);
        this.heap = heap;   
        this.root = new Root(heap);
        root.setVersion(VERSION);
    }
    
    static void registerAllocationClasses(AnyHeap heap) {
        heap.registerAllocationSize(SimpleLeaf.SIZE, true);
        heap.registerAllocationSize(Node4.SIZE, true);
        heap.registerAllocationSize(Node16.SIZE, true);
        heap.registerAllocationSize(Node48.SIZE, true);
        heap.registerAllocationSize(Node256.SIZE, true);
    }
    
    @SuppressWarnings("unchecked")
    private LongART(AnyHeap heap, long handle) {
        if (handle <= 0) throw new IllegalArgumentException("Invalid artree handle: "+handle);
        registerAllocationClasses(heap);
        this.heap = heap;
        root = (Root)Node.rebuild(heap, handle);
        count = root.getCount();
        maxKeyLen = this.root.getMaxKeyLength(); 
    }
     
    /**
     * Returns a previously created radix tree that is associated with the supplied handle.
     * The {@code handle} must be that of a radix tree created on the supplied heap.
     * @param handle the handle of a previously-created radix tree
     * @param heap the heap from which to retrieve the radix tree 
     * @return the radix tree
     * @throws HeapException if the radix tree could not be reaccessed
     * @throws IllegalArgumentException if ${@code handle} is not valid
     */
    public static LongART fromHandle(AnyHeap heap, long handle) {
        return new LongART(heap, handle);
    }

    /**
     * Returns a handle to this radix tree. This stable value can be stored and used later to regain
     * access to the radix tree.
     * @return a handle to this radix tree
     * @throws IllegalStateException if {@link LongART#free} has been called on this object
     */
    public long handle() {
        return root.handle();
    }

    /**
     * Deallocates the memory used by this radix tree.
     * The semantics of this method depend on the heap supplied when the radix tree was constructed.
     * @throws HeapException if the radix tree could not be freed
     * @throws IllegalStateException if {@link LongART#free} has been called on this object
     */
    public void free() {
        root.free();
    }

    static int compareUnsigned(byte b1, byte b2) {
        return Integer.compareUnsigned(Byte.toUnsignedInt(b1), Byte.toUnsignedInt(b2));
    }

    /**
     * Returns the number of entries in this radix tree.
     * @return the number of entries
     * @throws IllegalStateException if {@link LongART#free} has been called on this object
     */
    public long size() {
        return root.getCount();
    }

    /**
     * Returns a hash code for this radix tree.  Note that this hash code is not computed based on the 
     * entries in this radix tree and is only stable for the lifetime of the Java process.   
     * @return a hash code for this radix tree 
     */
    @Override
    public int hashCode() {
        return root.mb.hashCode();
    }

    /**
     * Compares this radix tree to the specified object.  The result is true if and only if the argument is not 
     * null and is a radix tree whose handle is equal to the handle of this radix tree. 
     * @return true if the given object is equal 
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LongART && ((LongART)obj).root.mb.equals(this.root.mb);
    }

    private void incrementCount() {
        root.incrementCount();
        count++;
    }

    private void decrementCount() {
        root.decrementCount();
        count--;
    }

    private void setMaxKeyLength(int length) {
        root.setMaxKeyLength(maxKeyLen = length); 
    }

    /**
     * Retrieves the lowest key in this radix tree.
     * @return the lowest key 
     * @throws NoSuchElementException if the radix tree is empty 
     */
    public byte[] firstKey() {
        Node n = root.getChild();
        if (n == null) throw new NoSuchElementException();
        ByteBuffer firstKey = ByteBuffer.allocate(maxKeyLen);
        SearchHelper h = (Node parent, Node child, Byte radix, Consumer<Long> cleanerFunction) -> {
            byte[] ba = parent.getPrefix();
            if (ba.length > 0) firstKey.put(ba);
            if (radix != null ) firstKey.put(radix);
            if (child != null && child.isLeaf()) firstKey.put(child.getPrefix());
        };
        search2(n, new byte[]{}, 0, h, null, false);
        return Arrays.copyOf(firstKey.array(), firstKey.position());
    }

    /**
     * Retrieves the highest key in this radix tree.
     * @return the highest key 
     * @throws NoSuchElementException if the radix tree is empty 
     */
    public byte[] lastKey() {
        Node n = root.getChild();
        if (n == null) throw new NoSuchElementException();
        ByteBuffer lastKey = ByteBuffer.allocate(maxKeyLen);
        SearchHelper h = (Node parent, Node child, Byte radix, Consumer<Long> cleanerFunction) -> {
            byte[] ba = parent.getPrefix();
            if (ba.length > 0) lastKey.put(ba);
            if (radix != null ) lastKey.put(radix);
            if (child != null && child.isLeaf()) lastKey.put(child.getPrefix());
        };
        search2(n, new byte[]{}, 0, h, null, true);
        return Arrays.copyOf(lastKey.array(), lastKey.position());
    }

    /**
     * Maps the specified key to the specified value.
     * If a mapping already exists for the specified key, the value is replaced.
     * @param key the key to which the specified value is to be mapped
     * @param value the value to be mapped to the specified key
     * @return the previous {@code long} value mapped to the specified key, or zero
     * if there is no previous mapping
     * @throws IllegalArgumentException if the supplied key has zero length
     */    
    public long put(byte[] key, long value) {
        return put(key, value, (a, b) -> { return value; });
    }

    /**
     * Maps the specified key to the specified value.
     * If a mapping already exists for the specified key, the value is replaced.
     * The supplied merge function will be called with the {@code newValue} and current 
     * value. The value returned by the merge function will be stored.
     * @param key the key to which the specified value is to be mapped
     * @param newValue the new value to be passed to the merge function
     * @param mergeFunction the merge function
     * @return the previous {@code long} value mapped to the specified key, or zero 
     * if there is no previous mapping
     * @throws IllegalArgumentException if the supplied value is null or the supplied key 
     * has zero length 
     */    
    public long put(byte[] key, Object newValue, BiFunction<Object, Long, Long> mergeFunction) {
        if (key == null || key.length == 0) throw new IllegalArgumentException("Invalid key");
        if (newValue == null) throw new IllegalArgumentException("value cannot be null");
        if (key.length > maxKeyLen) setMaxKeyLength(key.length);
        return heap.execute(() -> {
            return insert(root, root.getChild(), key, newValue, 0, 0, mergeFunction);
        });
    }

    @SuppressWarnings("unchecked")
    private long insert(Node parent, Node node, byte[] key, Object value, int depth, int replaceIndex, BiFunction<Object, Long, Long> merge) {
        if (node == null) {    // empty tree
            long leafValue = merge.apply(value, 0L);

            Root rt = (Root)parent;    // if tree is empty, parent is guaranteed to be root
            Node leaf = SimpleLeaf.create(this.heap, key, 0, key.length, leafValue);
            rt.addChild(leaf);
            incrementCount();
            return 0L;
        }

        byte[] newPrefix = new byte[8];
        byte[] prefix = node.getPrefix();

        if (node.isLeaf()) {
            int matchedLength = node.checkPrefix(key, depth);
            if (matchedLength == node.getPrefixLength() && matchedLength + depth == key.length) {
                //replacement
                long old = ((Leaf)node).getValue();
                long newVal = merge.apply(value,old);
                if (newVal != old) ((Leaf)node).setValue(newVal);
                return old;
            }
            long newVal = merge.apply(value, 0L);
            InternalNode newNode;
            int i = 0;
            for (; i < (key.length-depth) && i < prefix.length && key[i+depth] == prefix[i]; i++) {
                newPrefix[i] = key[i+depth];
            }

            //depth += i;

            node.updatePrefix(prefix, i + 1, node.getPrefixLength() - i - 1);

            int prefixLength = key.length - depth - i - 1;
            Node newChild = SimpleLeaf.create(this.heap, key, depth + i + 1, prefixLength, newVal);

            if (depth + i == key.length) newNode = new Node4(this.heap, newPrefix, i, true, newChild, (byte)0, node, prefix[i]);
            else if (i == prefix.length) {
                newNode = new Node4(this.heap, newPrefix, i, true, (Leaf)node, (byte)0, newChild, key[depth + i]);
                //((InternalNode)parent).clearBlankRadixFlag();
            }
            else newNode = new Node4(this.heap, newPrefix, i, false, newChild, key[depth + i], node, prefix[i]);

            if (parent == root) { ((Root)parent).addChild(newNode); }
            else ((InternalNode)parent).putChildAtIndex(replaceIndex, newNode);
            incrementCount();
            return 0L;
        }

        // current node is an internal node
        InternalNode intNode = (InternalNode)node;
        int matchedLength = intNode.checkPrefix(key, depth);
        if (matchedLength != intNode.getPrefixLength()) {
            // mismatch. found insertion point of a new internal node
            InternalNode newNode;
            long leafVal = merge.apply(value, 0L);
            int i = 0;
            for (; i < matchedLength; i++) {
                newPrefix[i] = prefix[i];
            }

            intNode.updatePrefix(prefix, i + 1, intNode.getPrefixLength() - i - 1);

            int prefixLength = key.length - depth - i - 1;
            Node newChild = SimpleLeaf.create(this.heap, key, depth + i + 1, prefixLength, leafVal);

            if (depth + i == key.length) newNode = new Node4(this.heap, newPrefix, matchedLength, true, newChild, (byte)0, node, prefix[i]);
            else newNode = new Node4(this.heap, newPrefix, matchedLength, false, newChild, key[depth + i], node, prefix[i]);

            if (parent == root) { ((Root)parent).addChild(newNode); }
            else ((InternalNode)parent).putChildAtIndex(replaceIndex, newNode);
            incrementCount();
            return 0L;
        }

        // prefix is a subset of the key
        depth += intNode.getPrefixLength(); // or just += matchedLength;
        if (depth == key.length) {
            //this insertion will be a blankradix child to this internal node
            Leaf child;
            long old = 0L;
            if ((child = intNode.findBlankRadixChild()) != null) {
                old = child.getValue();
                long newVal = merge.apply(value, old);
                if (old != newVal) child.setValue(newVal);
            }
            else{
                long newVal = merge.apply(value, 0L);
                child = new SimpleLeaf(this.heap, newVal);
                if (!intNode.addBlankRadixChild(child)) {
                    InternalNode newNode = intNode.grow(child, Optional.empty());
                    ((InternalNode)parent).putChildAtIndex(replaceIndex, newNode);
                    intNode.free();
                }
                incrementCount();
            }
            // no need to update prefix for a blank radix child - it has no prefix
            return old;
        }
        //descending find next node with matching radix
        int childIndex = intNode.findChildIndex(key[depth]);
        Node next = intNode.getChildAtIndex(childIndex);
        long oldValue;
        if (next != null) {
            oldValue = insert(node, next, key, value, depth + 1, childIndex, merge);
        } else {
            // found insertion point. insert leaf
            int prefixLength = key.length - depth - 1;
            oldValue = 0L;
            long leafVal = merge.apply(value, 0L);
            Node newChild = SimpleLeaf.create(this.heap, key, depth + 1, prefixLength, leafVal);
            if (!intNode.addChild(key[depth], newChild)) {
                InternalNode newNode = intNode.grow(newChild, Optional.of(key[depth]));
                if (parent == root) { ((Root)parent).addChild(newNode); }
                else {
                    ((InternalNode)parent).putChildAtIndex(replaceIndex, newNode);
                }
                intNode.free();
            }
        incrementCount();
        }
        return oldValue;
    }

    /**
     * Retrieves the {@code long} value mapped to the supplied key.
     * @param key the key whose mapped value is to be returned 
     * @return the {@code long} value mapped to the supplied key
     * @throws IllegalStateException if the specified key is null or the radix tree has been freed
     */
    public long get(byte[] key) {
        if (key == null || key.length == 0) throw new IllegalArgumentException("Invalid key");
        Node node;
        if ((node = root.getChild()) != null) {
                return search(root.getChild(), key, 0, null, null);
        }
        return 0;
    }

    byte[] splitKey() {
        EntryIterator it = new EntryIterator();
        long midPos = count / 2;
        int i = 0;
        byte[] splitKey = null;
        while(it.hasNext()) {
                if(i++ < midPos)
                    it.next();
                else {
                    splitKey = it.next().getKey();
                    break;
                }
            } 
            return splitKey;
    }

    /**
     * [EXPERIMENTAL: This method may change or be removed in future versions] Splits this radix tree into two radix trees of approximately equal size.  
     * After the split, this radix tree will contain entries with the lower range of keys and 
     * the returned radix tree will contain entries with the higher range of keys.
     * @return the radix tree with the higher range of keys
     * @throws IllegalStateException if {@link LongART#free} has been called on this object
     */
    @Override
    public LongART split() {
        EntryIterator it = new EntryIterator();
        long midPos = count / 2;
        int i = 0;
        byte[] splitKey = null;
        while(it.hasNext()) {
            if(i++ < midPos)
                it.next();
            else {
                splitKey = it.next().getKey();
                break;
            }
        } 
        if (splitKey == null) return null;
        return split(splitKey);
    }

    LongART split(byte[] splitKey) {
        // navigate to splitkey
        // build a cache of copied nodes as you iterate
        // navigate backup and remove radices less than key[depth]
        // return new tree. yikes.
        if (splitKey == null || splitKey.length == 0) throw new IllegalArgumentException("Invalid splitKey");
        LongART newTree = new LongART(heap);
        Root newRoot = newTree.root;
        SearchHelper splitFunc = (Node parent, Node child, Byte radix, Consumer<Long> c) -> {
            //if (radix == null) System.out.println("Uh oh! radix is null");
            if (child.isLeaf()) {
                if(radix == (byte)-1) return; 
                InternalNode s = ((InternalNode)parent).split(++radix, true);
                if (s.getChildrenCount() != 0) newRoot.addChild(s);
                else s.free();
            }
            else {
                InternalNode s;
                Node rc = newRoot.getChild();
                if (rc == null){
                    if(radix == (byte)-1) return; 
                    s = ((InternalNode)parent).split(++radix, true);
                }
                else {
                    s = ((InternalNode)parent).split(radix, false);
                    s.updateChild(rc, radix);
                }
                if (s.getChildrenCount() != 0) newRoot.addChild(s);
                else s.free();
            }
        };
        search(root.getChild(), splitKey, 0, splitFunc, null);  
        long newcount = 1 + (root.getCount() / 2);
        count = newcount;
        root.setCount(newcount);
        newRoot.setCount(newcount - 2);
        newTree.count = newcount - 2;
        newTree.setMaxKeyLength(maxKeyLen);
        return newTree;
    }

    void mergeTree(LongART tree) {
    // add each child entry in tree top node to this trees top node
    // data from tree is moved to this tree
        Node node;
        if ((node = tree.root.getChild()) != null && !node.isLeaf()) {
            InternalNode from = (InternalNode)node;
            InternalNode to = (InternalNode)this.root.getChild();
            if (to == null) {
                this.root.addChild(from);
                return;
            }
            NodeEntry[] children = from.getEntries();
            for (int i=0; i<children.length; i++) {
                if(!to.addChild(children[i].radix, children[i].child)) {
                    InternalNode newNode = to.grow(children[i].child, Optional.of(children[i].radix));
                    root.addChild(newNode);
                    to.free();
                    to = newNode;
                }
            }
        }
        
    }

    @SuppressWarnings("unchecked")
    private long search2(Node node, byte[] key, int depth, SearchHelper helper, Consumer<Long> c, boolean reversed) {
        if (node == null) {
            return 0;
        }
        Node next;
        int matchedLength = (key.length == 0) ? node.getPrefixLength() : node.checkPrefix(key, depth);
        if (matchedLength != node.getPrefixLength()) {
            return -1;
        }
        if (node.isLeaf())
            return ((depth + matchedLength) == key.length) ? ((SimpleLeaf)node).getValue() : 0;
        else {
            depth += matchedLength;
            boolean blank;
            long l;
            if (key.length == 0) {
                InternalNode n = (InternalNode)node;
                byte b = 0;  
                if (reversed) {
                    blank = n.hasBlankRadixChild() && n.getChildrenCount() == 1; 
                    next = blank ? n.findBlankRadixChild() : n.findChild(b = n.findHighestRadix());
                }
                else {
                blank = n.hasBlankRadixChild();
                next = blank ? n.findBlankRadixChild() : n.findChild(b = n.findLowestRadix());
                } 

                if (helper != null) {
                    helper.apply(node, next, (blank ? null : b), c);
                }
                l = search2(next, key, blank ? depth : depth + 1, helper, c, reversed);
            }
            else {
                blank = (depth == key.length);
                next = blank ? ((InternalNode)node).findBlankRadixChild() : ((InternalNode)node).findChild(key[depth]);

                if (helper != null) {
                    helper.apply(node, next, (blank ? null : key[Math.min(depth,key.length-1)]), c);
                }
                l = search2(next, key, blank ? depth : depth + 1, helper, c, reversed);
            }
            return l;
        }
    }

    @SuppressWarnings("unchecked")
    private long search(Node node, byte[] key, int depth, SearchHelper helper, Consumer<Long> c) {
        if (node == null) {
            return 0;
        }
        Node next;
        int matchedLength = node.checkPrefix(key, depth);
        if (matchedLength != node.getPrefixLength()) {
            return 0;
        }
        if (node.isLeaf())
            return ((depth + matchedLength) == key.length) ? ((SimpleLeaf)node).getValue() : 0;
        else {
            depth += matchedLength;
            boolean blank = (depth == key.length);
            next = blank ? ((InternalNode)node).findBlankRadixChild() : ((InternalNode)node).findChild(key[depth]);
            long l = search(next, key, blank ? depth : depth + 1, helper, c);
            //if (l != 0 && helper != null) {
            if (helper != null) {
                helper.apply(node, next, (blank ? null : key[Math.min(depth,key.length-1)]), c);
            }
            return l;
        }
    }

    void statsPrint() {
        System.out.println("Printing Stats .Printing Stats ...");
        Node node;
        if ((node = root.getChild()) != null)
            node.statsPrint(0);
        System.out.println("");
    }

    void print() {
        Node node;
        if ((node = root.getChild()) != null)
            node.print(0);
        System.out.println("");
    }

    
    /**
     * Removes all of the entries in this radix tree.
     * The semantics of this method depend on the heap supplied when constructed.
     * @param cleanerFunction this function will be called once for each entry, passing the value of the 
     * entry being removed. This may be particularly useful for performing additional cleanup, in 
     * the case where the values stored in this radix tree are handles
     * @throws IllegalStateException if {@link LongART#free} has been called on this object
     */
    public void clear(Consumer<Long> cleanerFunction) {
        if (cleanerFunction == null) throw new IllegalArgumentException("cleaner function cannot be null");
        heap.execute(() -> {
            root.destroy(cleanerFunction);
            count = 0;
        });
    }

    @FunctionalInterface
    interface SearchHelper {
        void apply(Node parent, Node child, Byte radix, Consumer<Long> cleanerFunction);
    }

    void deleteNodes(Node parent, Node child, Byte radix, Consumer<Long> cleanerFunction) {
        heap.execute(()->{
            if (child == null) return;
            if (child.isLeaf()) {
                if (cleanerFunction != null) cleanerFunction.accept(((SimpleLeaf)child).getValue());
                child.free();
                ((InternalNode)parent).deleteChild(radix);
                decrementCount();
            }
            else if (((InternalNode)child).getChildrenCount() == 0) {
                child.free();
                ((InternalNode)parent).deleteChild(radix);
            }
        });
    }

    /**
     * Removes the mapping for the specified key from this radix tree if present.
     * The semantics of this method depend on the heap supplied when constructed.
     * @param key the key whose mapping is to be removed.
     * @param cleanerFunction this function will be called once for each entry, passing the value of the 
     * entry being removed. This may be particularly useful for performing additional cleanup, in 
     * the case where the values stored in this radix tree are handles
     * @return the removed value or zero if not found
     * @throws IllegalArgumentException if the supplied key has zero length
     */
    public long remove(byte[] key, Consumer<Long> cleanerFunction) {
        if (cleanerFunction == null) throw new NullPointerException("cleaner function cannot be null");
        if (key.length == 0) throw new IllegalArgumentException("Invalid key");
        return search(root.getChild(), key, 0, this::deleteNodes , cleanerFunction);
    }

    /**
     * Returns an ascending-order iterator over the values in this radix tree.
     * @return the iterator 
     */
    public Iterator<Long> getValueIterator() {
        return new ValueIterator();
    }

    /**
     * Returns an ascending-order iterator over the entries in this radix tree.
     * @return the iterator 
     */
    public Iterator<LongART.Entry> getEntryIterator() {
        return new EntryIterator();
    }

    /**
     * Returns a descending-order iterator over the entries in this radix tree.
     * @return the iterator 
     */
    public Iterator<LongART.Entry> getReverseEntryIterator() {
        return new EntryIterator(true);
    }

    /**
     * Returns a descending-order iterator over entries in this radix tree;
     * the iterator will include entries whose keys range from {@code firstKey} to {@code lastKey}.
     * @param firstKey low endpoint of the keys in the returned iterator
     * @param firstInclusive true if the lowest key is to be included in the returned iterator
     * @param lastKey high endpoint of the keys in the returned iterator
     * @param lastInclusive true if the highest key is to be included in the returned iterator
     * @return the iterator 
     * @throws IllegalArgumentException if firstKey or lastKey has zero length
     */
    public Iterator<LongART.Entry> getReverseEntryIterator(byte[] firstKey, boolean firstInclusive, byte[] lastKey, boolean lastInclusive) {
        if (firstKey == null || firstKey.length == 0 || lastKey == null || lastKey.length == 0) throw new IllegalArgumentException();
        return new EntryIterator(firstKey, firstInclusive, lastKey, lastInclusive, true);
    }

    /**
     * Returns an ascending-order iterator over entries in this radix tree;
     * the iterator will include entries whose keys are lower than (or equal to, 
     * if {@code lastInclusive} is true) {@code lastKey}.
     * @param lastKey high endpoint of the keys in the returned iterator
     * @param lastInclusive true if the highest key is to be included in the returned iterator
     * @return the iterator 
     * @throws IllegalArgumentException if lastKey has zero length
     */
    public Iterator<Entry> getHeadEntryIterator(byte[] lastKey, boolean lastInclusive) {
        if (lastKey == null || lastKey.length == 0) throw new IllegalArgumentException();
        return new EntryIterator(lastKey, lastInclusive);
    }

    /**
     * Returns an ascending-order iterator over entries in this radix tree;
     * the iterator will include entries whose keys are higher than (or equal to, 
     * if {@code firstInclusive} is true) {@code firstKey}.
     * @param firstKey low endpoint of the keys in the returned iterator
     * @param firstInclusive true if the lowest key is to be included in the returned iterator
     * @return the iterator
     * @throws IllegalArgumentException if lastKey has zero length
     */
    public Iterator<Entry> getTailEntryIterator(byte[] firstKey, boolean firstInclusive) {
        if (firstKey == null || firstKey.length == 0) throw new IllegalArgumentException();
        return new EntryIterator(firstKey, firstInclusive, null, false, false);
    }

    /**
     * Returns an ascending-order iterator over entries in this radix tree;
     * the iterator will include entries whose keys range from {@code firstKey} to {@code lastKey}.
     * @param firstKey low endpoint of the keys in the returned iterator
     * @param firstInclusive true if the lowest key is to be included in the returned iterator
     * @param lastKey high endpoint of the keys in the returned iterator
     * @param lastInclusive true if the highest key is to be included in the returned iterator
     * @return the iterator 
     * @throws IllegalArgumentException if firstKey or lastKey has zero length
     */
    public Iterator<Entry> getEntryIterator(byte[] firstKey, boolean firstInclusive, byte[] lastKey, boolean lastInclusive) {
        if (firstKey == null || firstKey.length == 0 || lastKey == null || lastKey.length == 0) throw new IllegalArgumentException();
        return new EntryIterator(firstKey, firstInclusive, lastKey, lastInclusive, false);
    }

    class StackItem {
        NodeEntry[] entries;
        int index = 0;
        int prefixLen = 0;
        private final boolean hasBlank;
        final boolean reversed;

        public StackItem(NodeEntry[] entries, int prefixLen, boolean reversed) {
            this.entries = entries;
            this.prefixLen = prefixLen;
            this.hasBlank = true;
            this.reversed = reversed;
            if (reversed) index = entries.length - 1;
        }

        public StackItem(NodeEntry[] entries, int prefixLen, Byte radix, boolean hasBlank, boolean reversed) {
            this.entries=entries;
            this.prefixLen=prefixLen;
            this.hasBlank=hasBlank;
            this.reversed = reversed;
            if (radix != entries[0].radix) this.index = calcIndex(radix);
            if (reversed && index == entries.length) index = entries.length - 1;
        }

        int calcIndex(byte radix) {
            int i;
            for (i = 0; i < entries.length; i++) {
                if (compareUnsigned(radix, entries[i].radix) <= 0) break;
            }
            return i;
        }

        public boolean isDone() {
            if (!reversed) return index >= entries.length;
            else return index < 0; 
        }

        public boolean currentIsBlank() {
            return hasBlank && (index == 0);
        }

        public int prefixLen() {
            return prefixLen;
        }

        public void next() {
            if (!reversed) index++;
            else index--;
        }

        public NodeEntry entryAtIndex() {
            return entries[index];
        }
    }

    /**
     * A radix tree entry ({@code byte[]} - {@code long} pair).
     * @since 1.2
     */
    public static class Entry {
        byte[] key;
        long value;

        Entry(byte[] key, long value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the {@code byte[]} key that corresponds to this entry.
         * @return the key 
         */
        public byte[] getKey() {
            return key;
        }

        /**
         * Returns the {@code long} value that corresponds to this entry.
         * @return the value
         */
        public long getValue() {
            return value;
        }
    }

    class EntryIterator implements Iterator<LongART.Entry> {
        StackItem cursor;
        byte[] lastKey = null;
        boolean lastInclusive = false;
        boolean reversed;
        ByteBuffer keyBuf;
        ArrayDeque<StackItem> cache;
        Entry prev;
        Entry next;

        public EntryIterator() {
            this(false);
        }

        public EntryIterator(boolean reversed) {
            this.reversed = reversed;
            cache = new ArrayDeque<>();
            Node first = root.getChild();
            if (first != null)
            {
                if (first.isLeaf()) {
                    SimpleLeaf leaf = (SimpleLeaf)first;
                    next = new Entry(first.getPrefix(), leaf.getValue());
                }
                else {
                    keyBuf = ByteBuffer.allocate(maxKeyLen);
                    search2(first, new byte[]{}, 0, this::buildCache, null, reversed);
                    cursor = cache.getFirst();
                    advance(); 
                }
            }
        }

        void buildCache(Node parent, Node child, Byte radix, Consumer<Long> cleanerFunction) {
            StackItem item;
            if (reversed && child == null) return;
            NodeEntry[] entries = ((InternalNode)parent).getEntries();
            if (radix == null && child != null) item = new StackItem(entries, parent.getPrefixLength(), reversed);
            else if (child == null) {
                item = new StackItem(entries, parent.getPrefixLength(), (radix == null) ? entries[0].radix : radix, ((InternalNode)parent).hasBlankRadixChild(), reversed);
            }
            else {
                item = new StackItem(((InternalNode)parent).getEntries(), parent.getPrefixLength(), radix, ((InternalNode)parent).hasBlankRadixChild(), reversed);
            }
            cache.push(item);
            byte[] ba = parent.getPrefix();
            if (ba.length > 0) keyBuf.put(ba);
            if (radix != null && child != null && !child.isLeaf()) keyBuf.put(radix);
        }

        public EntryIterator(byte[] lastKey, boolean lastInclusive) {
            this();
            this.lastKey = lastKey;
            this.lastInclusive = lastInclusive;
        }

        public EntryIterator(byte[] firstKey, boolean firstInclusive, byte[] lastKey, boolean lastInclusive, boolean reversed) {
            cache = new ArrayDeque<>();
            this.reversed = reversed;
            Node first = root.getChild();
            // lastKey can be null, firstkey is never null as it is checked before calling this
            if (lastKey != null) {
                int comp = keyCompare(firstKey, lastKey);
                if (comp == 0) {
                    long val = get(firstKey);
                    next = (val == 0) ? null : new Entry(firstKey, val);
                    return;
                }
                if (!reversed && comp > 0) throw new IllegalArgumentException();
                if (reversed && comp < 0) throw new IllegalArgumentException();
            }
            if (reversed) {
                this.lastKey = firstKey;
                firstKey = lastKey;
                this.lastInclusive = firstInclusive;
                firstInclusive = lastInclusive;
            }
            else {
                this.lastKey = lastKey;
                this.lastInclusive = lastInclusive;
            }
            if (first != null) {
                if (first.isLeaf()) {
                    SimpleLeaf leaf = (SimpleLeaf)first;
                    int x = keyCompare(firstKey, first.getPrefix());
                    next = ((firstInclusive && x == 0) || x < 0) ? new Entry(first.getPrefix(), leaf.getValue()) : null;
                }
                else {
                    keyBuf = ByteBuffer.allocate(maxKeyLen);
                    long l = search2(first, firstKey, 0, this::buildCache, null, reversed);
                    cursor = cache.peekFirst();
                    if (cursor != null) {
                        if (l == -1 && keyBuf.position() > 0) {
                            keyBuf.position(keyBuf.position() - 1);
                        }
                        advanceTo(firstKey, firstInclusive);
                    } else {
                        search2(first, new byte[]{}, 0, this::buildCache, null, reversed);
                        cursor = cache.getFirst();
                        advanceTo(firstKey, firstInclusive); 
                    }
                }
                prev = next;
            }
        }

        int keyCompare(byte[] firstKey, byte[] nextKey){
            byte[] first;
            byte[] next;
            if (reversed) { first = nextKey; next = firstKey; }
            else { first = firstKey; next = nextKey; }
            int ret = 0;
            int i = 0;
            while (i < first.length && i < next.length && (ret = compareUnsigned(first[i], next[i])) == 0) i++;
            return (ret == 0) ? Integer.compare(first.length, next.length) : ret;
        }

        public boolean hasNext() {
            if (next == null || lastKey == null) return (next != null);
            int y = keyCompare(lastKey, next.getKey());
            return lastInclusive ? y >= 0 : y > 0;
        }

        public LongART.Entry next() {
            prev = next;
            if (prev == null) throw new NoSuchElementException("Null");
            if (cursor == null) {
                next = null;
                return prev;
            }
            while (cursor.isDone()) {
                if (cache.size() == 0) {
                    next = null;
                    return prev;
                }
                pop();
                if (cursor == null) {
                    next = null;
                    return prev;
                }
            }
            advance();
            return prev;
        }

        void advance() {
            NodeEntry ne = cursor.entryAtIndex();
            if (!ne.child.isLeaf()) {
                if (!cursor.currentIsBlank()) keyBuf.put(ne.radix);
                search2(ne.child, new byte[]{}, 0, this::buildCache, null, reversed);
                cursor = cache.getFirst();
                ne = cursor.entryAtIndex();
            }
            SimpleLeaf leaf = (SimpleLeaf)ne.child;
            keyBuf.mark();
            if (!cursor.currentIsBlank()) keyBuf.put(ne.radix); 
            cursor.next();
            byte[] leafPrefix = leaf.getPrefix();
            if (leafPrefix.length > 0) keyBuf.put(leafPrefix);
            next = new Entry(Arrays.copyOf(keyBuf.array(),keyBuf.position()),leaf.getValue());
            keyBuf.reset();
        }

        void advanceTo(byte[] firstKey, boolean inclusive) {
            while (cursor != null && !cursor.isDone()) {
                advance();
                int x = keyCompare(firstKey, next.getKey());
                if ((inclusive && x == 0) || x < 0) break;
                while (cursor.isDone() && cache.size() > 0) {
                    pop(); 
                    if (cursor == null) break;
                }
            }
        }

        void pop() {
            keyBuf.reset().position(Math.max(0, keyBuf.position() - (1 + cursor.prefixLen()))).mark();
            cache.pop();
            cursor = cache.peekFirst();
            if (cursor != null) cursor.next(); 
            keyBuf.reset();
        }
    }

    class ValueIterator implements Iterator<Long> {
        StackItem cursor;
        ArrayDeque<StackItem> cache;
        long next;
        long prev;

        public ValueIterator() {
            cache = new ArrayDeque<>();
            Node first = root.getChild();
            if (first != null)
            {
                if (first.isLeaf()) {
                    SimpleLeaf leaf = (SimpleLeaf)first;
                    next = leaf.getValue();
                }
                else {
                    search2(first, new byte[]{}, 0, this::buildCache, null, false);
                    cursor = cache.getFirst();
                    advance();
                }
            }
        }

        public boolean hasNext() {
            return (next != 0);
        }

        public Long next() {
            prev = next;
            if (prev == 0) throw new NoSuchElementException();
            if (cursor == null) {
                next = 0;
                return prev;
            }
            while (cursor.isDone()) {
                if (cache.size() == 0) {
                    next = 0;
                    return prev;
                }
                cache.pop();
                cursor = cache.peekFirst();
                if (cursor != null) cursor.next();
                else {
                    next = 0;
                    return prev;
                }
            }
            advance(); 
            return prev;
        }

        void advance() {
            if (!cursor.entryAtIndex().child.isLeaf()) {
                search2(cursor.entryAtIndex().child, new byte[]{}, 0, this::buildCache, null, false);
                cursor = cache.getFirst();
            }
            next = ((SimpleLeaf)cursor.entryAtIndex().child).getValue();
            cursor.next();
        }

        void buildCache(Node parent, Node child, Byte radix, Consumer<Long> cleanerFunction) {
            StackItem item;
            if (radix == null) item = new StackItem(((InternalNode)parent).getEntries(), parent.getPrefixLength(), false);
            else item = new StackItem(((InternalNode)parent).getEntries(), parent.getPrefixLength(), radix, ((InternalNode)parent).hasBlankRadixChild(), false);
            cache.push(item);
        }
    }

    static abstract class Node {
        static final byte NODE4_TYPE = 7;
        static final byte NODE16_TYPE = 1;
        static final byte NODE48_TYPE = 2;
        static final byte NODE256_TYPE = 3;
        static final byte SIMPLE_LEAF_TYPE = 4;
        static final byte COMPLEX_LEAF_TYPE = 5;
        static final byte ROOT_TYPE = 6;
        //static final byte FREED = (byte)0xff;

        protected static final long HEADER_SIZE = 16L;
        protected static final long NODE_TYPE_OFFSET = 0L;
        protected static final long BLANK_RADIX_INDEX_OFFSET = 1L;
        protected static final long CHILDREN_COUNT_OFFSET = 2L;
        protected static final long PREFIX_LENGTH_OFFSET = 4L;
        protected static final long COMPRESSED_PATH_OFFSET = 8L;
        static final int MAX_PREFIX_LENGTH = 8;

        AnyHeap heap;
        AnyMemoryBlock mb;

        Node(AnyHeap heap, long size) {
            this.heap = heap;
            this.mb = this.heap.allocateCompactMemoryBlock(size);
        }

        Node(AnyHeap heap, AnyMemoryBlock mb) {
            this.heap = heap;
            this.mb = mb;
        }

        static Node rebuild(AnyHeap heap, long handle) {
            if (handle == 0) return null;
            AnyMemoryBlock mb = heap.compactMemoryBlockFromHandle(handle);
            Node ret;
            byte rawType = mb.getByte(NODE_TYPE_OFFSET);
            switch (rawType) {
                case NODE4_TYPE: ret = new Node4(heap, mb); break;
                case NODE16_TYPE: ret = new Node16(heap, mb); break;
                case NODE48_TYPE: ret = new Node48(heap, mb); break;
                case NODE256_TYPE: ret = new Node256(heap, mb); break;
                case SIMPLE_LEAF_TYPE: ret = new SimpleLeaf(heap, mb); break;
                case ROOT_TYPE: ret = new Root(heap, mb); break;
                default: throw new HeapException("Failed to reaccess tree with supplied handle");
            }
            return ret;
        }

        void free() {
            mb.freeMemory();
        }

        long handle() {
            return mb.handle();
        }

        private byte getType() {
            return mb.getByte(NODE_TYPE_OFFSET);
        }

        void initType(byte type) {
            mb.setByte(NODE_TYPE_OFFSET, type);
        }

        int getPrefixLength() {
            return mb.getInt(PREFIX_LENGTH_OFFSET);
        }

        protected void setPrefixLength(int length) {
            mb.setInt(PREFIX_LENGTH_OFFSET, length);
        }

        byte[] getPrefix() {
            byte[] prefix = new byte[getPrefixLength()];
            if (prefix.length == 0) return prefix;
            mb.copyToArray(COMPRESSED_PATH_OFFSET, prefix, 0, prefix.length);
            return prefix;
        }

        void initPrefix(byte[] prefix) {
            if (prefix.length <= 8) {
                mb.copyFromArray(prefix, 0, COMPRESSED_PATH_OFFSET, prefix.length);
            } else {
                throw new IllegalArgumentException("Prefix more than 8 bytes");
            }
        }

        protected void setPrefix(byte[] prefix) {
            if (prefix.length <= 8) {
                mb.copyFromArray(prefix, 0, COMPRESSED_PATH_OFFSET, prefix.length);
            } else {
                throw new IllegalArgumentException("Prefix more than 8 bytes");
            }
        }

        void updatePrefix(byte[] prefix, int start, int updatedLength) {
            if (updatedLength <= 0) {
                setPrefixLength(0);
                return;
            }
            byte[] updatedPrefix = new byte[8];
            for (int i = 0; i < updatedLength; i++) {
                updatedPrefix[i] = prefix[start + i];
            }
            setPrefixLength(updatedLength);
            setPrefix(updatedPrefix);
        }

        // compares the given key to the prefix of this node starting from depth
        // returns 0 if there is no match at all key[depth] != prefix[0]
        // returns the number of matching bytes in the prefix
        int checkPrefix(byte[] key, int depth) {
            byte[] prefix = getPrefix();
            int i = 0;
            while (i < (key.length-depth) && i < prefix.length && key[depth + i] == prefix[i]) i++;
            return i;
        }

        boolean isLeaf() {
            return false;
        }

        String getPrefixString() {
            StringBuilder sb = new StringBuilder();
            byte[] prefix = getPrefix();
            for (int i = 0; i < prefix.length; i++) {
                sb.append(String.format("%02X ", prefix[i]));
            }
            return sb.toString();
        }

        abstract void destroy(Consumer<Long> cleanerFunction); 

        public void statsPrint(int depth) {
            StringBuilder start = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                start.append("   ");
            }
            if (!isLeaf()) {
                InternalNode intNode = (InternalNode)this;
                System.out.println(depth+","+intNode.capacity()+","+intNode.getChildrenCount()+","+getPrefixLength());
                intNode.printStatsChildren(start, depth); 
            } else {
                System.out.println(depth+",0,0,"+getPrefixLength());
            }
        }

        void print(int depth) {
            StringBuilder start = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                start.append("   ");
            }
            System.out.println(start + "=========================");
            System.out.println(start + "Type: " + getType());
            System.out.println(start + "Prefix length: " + getPrefixLength());
            System.out.println(start + "Prefix: " + getPrefixString());
            if (!isLeaf()) {
                InternalNode intNode = (InternalNode)this;
                System.out.println(start + "Child count: " + intNode.getChildrenCount());
                if (intNode.hasBlankRadixChild()) {
                    System.out.println(start + "Has Blank Radix Child at " + intNode.getBlankRadixIndex());
                }
                intNode.printChildren(start, depth);
            } else {
                SimpleLeaf leaf = (SimpleLeaf)this;
                System.out.println(start + "Value: " + Long.toHexString(leaf.getValue()));
            }
        }
    }

    static class NodeEntry{
        byte radix;
        Node child;

        public NodeEntry(byte radix, Node child) {
            this.radix = radix;
            this.child = child;
        }
    }

    static final class Root extends Node {
        private static final long SIZE = Node.HEADER_SIZE + 8;  // 8 byte pointer to first node in tree
        private static final long CHILD_OFFSET = Node.HEADER_SIZE;

        Root(AnyHeap heap) {
            super(heap, SIZE);
            initType(Node.ROOT_TYPE);
        }

        Root(AnyHeap heap, AnyMemoryBlock mb) {
            super(heap, mb);
        }

        boolean addChild(Node node) {
            mb.setLong(CHILD_OFFSET, node.handle());
            return true;
        }

        Node getChild() {
            return Node.rebuild(heap, mb.getLong(CHILD_OFFSET));
        }

        long getCount() {
            return mb.getLong(COMPRESSED_PATH_OFFSET);
        }

        void setCount(long count) {
            mb.setLong(COMPRESSED_PATH_OFFSET, count);
        }

        void incrementCount() {
            mb.setLong(COMPRESSED_PATH_OFFSET, mb.getLong(COMPRESSED_PATH_OFFSET) + 1);
        }

        void decrementCount() {
            mb.setLong(COMPRESSED_PATH_OFFSET, mb.getLong(COMPRESSED_PATH_OFFSET) - 1);
        }

        int getMaxKeyLength() {
            return mb.getInt(PREFIX_LENGTH_OFFSET);
        }

        void setMaxKeyLength(int length) {
            mb.setInt(PREFIX_LENGTH_OFFSET, length);
        }

        void setVersion(short version) {
            mb.setShort(CHILDREN_COUNT_OFFSET, version);
        }

        short getVersion() {
            return mb.getShort(CHILDREN_COUNT_OFFSET);
        }

        boolean isLeaf() { return false; }

        @Override
        void destroy(Consumer<Long> cleanerFunction) {
            Node child = getChild();
            if (child != null) {
                child.destroy(cleanerFunction);
                child.free();
                mb.setLong(CHILD_OFFSET, 0L);
            }
            setCount(0);
        }
        
        void deleteChild() {
            destroy(null);    
        }

        @Override
        void free() {
            destroy(c -> {});  
            //mb.setMemory((byte)0xff, 0, SIZE);
            mb.freeMemory();
        }
    }

    static abstract class InternalNode extends Node {
        InternalNode(AnyHeap heap, AnyMemoryBlock mb) {
            super(heap, mb);
        }

        InternalNode(AnyHeap heap, long size, Consumer<Range> initializer) {
            super(heap, heap.allocateCompactMemoryBlock(size, (Range range) -> {
                range.setByte(BLANK_RADIX_INDEX_OFFSET, (byte)0xff);
                initializer.accept(range);
            })
            );
        }

        // Node256 needs to override this to just check if the address for blank radix child is 0
        boolean hasBlankRadixChild() {
            return getBlankRadixIndex() != (byte)0xff;
        }

        byte getBlankRadixIndex() {
            return mb.getByte(BLANK_RADIX_INDEX_OFFSET);
        }

        void setBlankRadixIndex(byte index) {
            mb.setByte(BLANK_RADIX_INDEX_OFFSET, index);
        }

        short getChildrenCount() {
            return mb.getShort(CHILDREN_COUNT_OFFSET);
        }

        private void setChildrenCount(short count) {
            mb.setShort(CHILDREN_COUNT_OFFSET, count);
        }

        void incChildrenCount() {
            setChildrenCount((short)(getChildrenCount() + (short)1));
        }

        void decChildrenCount() {
            setChildrenCount((short)(getChildrenCount() - (short)1));
        }

        @SuppressWarnings("unchecked")
        Leaf findBlankRadixChild() {
            return (Leaf)getChildAtIndex(getBlankRadixIndex());
        }
        
        /*
         * Node256 needs to override to avoid overflowing
         * returns false if node is at capacity
         * adds or replaces? blank radix child
         */
        boolean addBlankRadixChild(Leaf child) {
            short childrenCount = getChildrenCount();
            if (childrenCount == capacity()) {
                return false;
            }
            else {
                incChildrenCount();
                setBlankRadixIndex((byte)childrenCount);
                putChildAtIndex(childrenCount, child);
            }
            return true;
        }

        // returns null if no valid child exists with the given radix
        Node findChild(byte radix) {
            return getChildAtIndex(findChildIndex(radix));
        }

        Node getChildAtIndex(int index) {
            return Node.rebuild(heap, findValueAtIndex(index));
        }
        
        @Override
        void destroy(Consumer<Long> cleanerFunction) {
            Node child;
            for (int i=0; i<capacity(); i++) {
                if ((child = getChildAtIndex(i)) != null) {
                    child.destroy(cleanerFunction); 
                    child.free();
                }
            }
        }

        int clearBlankRadixFlag() {
            int index = (int)getBlankRadixIndex();
            if (index != -1) mb.setByte(BLANK_RADIX_INDEX_OFFSET, (byte)0xff);
            return index;
        }

        byte findLowestRadix() {
            byte[] radices = getRadices();
            byte lowest = radices[0];
            for (int i = 1; i < radices.length; i++) {
                if (LongART.compareUnsigned(lowest, radices[i]) > 0) lowest = radices[i]; 
            }
            return lowest;
        }

        byte findHighestRadix() {
            byte[] radices = getRadices();
            int blankIndex = getBlankRadixIndex();
            byte highest = radices[0];
            for (int i = 0; i < radices.length; i++) {
                if (i == blankIndex) continue;
                if (LongART.compareUnsigned(highest, radices[i]) < 0) highest = radices[i]; 
            }
            return highest;
        }

        protected abstract short capacity();
        protected abstract byte[] getRadices();
        /* returns false if node is at capacity 
         * adds or replaces child with given radix
         */
        abstract boolean addChild(byte radix, Node node);
        abstract int findChildIndex(byte radix);
        protected abstract long findValueAtIndex(int index);
        abstract void putChildAtIndex(int index, Node child);
        abstract InternalNode grow(Node child, Optional<Byte> radix); //revisit visibility
        abstract NodeEntry[] getEntries();
        abstract void deleteChild(Byte radix);
        // Split node only if nonblank radix byte
        abstract InternalNode split(Byte radix, boolean first);
        //abstract InternalNode duplicate();

        boolean isLeaf() {
            return false;
        }

        void updateChild(Node newChild, Byte b) {
            if (b == null) {
                System.out.println("updateChild: radix is null newChild addr is "+newChild.handle());
                if (!newChild.isLeaf()) throw new RuntimeException();
                addBlankRadixChild((Leaf)newChild);
            }
            else {
                addChild(b, newChild);
            }
        }

        void printStatsChildren(StringBuilder start, int depth) {
            Node node;
            for (int i = 0; i < getChildrenCount(); i++) {
                node = getChildAtIndex(i);
                if(node != null)
                {
                    node.statsPrint(depth + 1);
                }
                else throw new RuntimeException("Empty child at index: "+i+". this capacity is "+capacity());
            }
        }

        void printChildren(StringBuilder start, int depth) {
            byte[] radices = getRadices();
            Node node;
            for (int i = 0; i < getChildrenCount(); i++) {
                if (radices != null) {
                    System.out.println(start + "For radix " + new String(new byte[]{radices[i]}) + "(" + radices[i] + "):");
                    node = getChildAtIndex(i);
                    if(node != null)
                    {
                        node.print(depth + 1);
                    }
                } else {
                }
            }
        }
    }

    static abstract class Leaf extends Node {
        Leaf(AnyHeap heap, AnyMemoryBlock mb) {
            super(heap, mb);
        }

        Leaf(AnyHeap heap, long size) {
            super(heap, size);
        }

        abstract void setValue(long value);
        abstract long getValue();

        @Override
        boolean isLeaf() {
            return true;
        }
        
        static Node prependNodes(AnyHeap heap, byte[] key, int start, int length, long value) {
            int curStart = key.length - 8;
            int curLength = length;

            //first create leaf
            Node child = new SimpleLeaf(heap, key, curStart, Node.MAX_PREFIX_LENGTH, value); 
            curLength -= (Node.MAX_PREFIX_LENGTH + 1);
            curStart -= (Node.MAX_PREFIX_LENGTH + 1);

            while (curLength > Node.MAX_PREFIX_LENGTH) {
                InternalNode parent = new Node4(heap, key, curStart, Node.MAX_PREFIX_LENGTH, child, key[curStart + Node.MAX_PREFIX_LENGTH]);
                child = parent;
                curStart -= (Node.MAX_PREFIX_LENGTH + 1);
                curLength -= (Node.MAX_PREFIX_LENGTH + 1);
            }

            if (curLength >= 0) {
                InternalNode parent = new Node4(heap, key, start, curLength, child, key[curStart + Node.MAX_PREFIX_LENGTH]);
                child = parent;
            }
            return child;
        }
    }

    static class SimpleLeaf extends Leaf {
        protected static final long SIZE = Node.HEADER_SIZE + 8L;
        private static final long VALUE_OFFSET = Node.HEADER_SIZE;

        SimpleLeaf(AnyHeap heap) {
            super(heap, SIZE);
            initType(Node.SIMPLE_LEAF_TYPE);
        }

        SimpleLeaf(AnyHeap heap, long value) {
            this(heap, new byte[]{0}, 0, 0, value);
        }

        SimpleLeaf(AnyHeap heap, byte[] prefix, int start, int length, long value) {
            super(heap, heap.allocateCompactMemoryBlock(SIZE, (Range range) -> {
                //set type
                range.setByte(Node.NODE_TYPE_OFFSET, Node.SIMPLE_LEAF_TYPE);
                //set prefix
                if (length > 0) {
                    range.setInt(Node.PREFIX_LENGTH_OFFSET, length);
                    range.copyFromArray(prefix, start, Node.COMPRESSED_PATH_OFFSET, length);
                }
                range.setLong(VALUE_OFFSET, value);
            }));
        }

        //factory method that creates a leaf and prepends internal nodes as needed
        //returns the topmost parent node or leaf
        static Node create(AnyHeap heap, byte[] prefix, int start, int length, long value) {
            if (length > Node.MAX_PREFIX_LENGTH) {
                return Leaf.prependNodes(heap, prefix, start, length, value);
            }
            else return new SimpleLeaf(heap, prefix, start, length, value);
        }

        SimpleLeaf(AnyHeap heap, AnyMemoryBlock mb) {
            super(heap, mb);
        }

        long getValue() {
            return mb.getLong(VALUE_OFFSET);
        }

        @Override
        void setValue(long value) {
            mb.setLong(VALUE_OFFSET, value);
        }

        @Override
        void destroy(Consumer<Long> cleanerFunction) {
            cleanerFunction.accept(getValue());
        }
    }

    static class Node4 extends InternalNode {
        protected static final long SIZE = Node.HEADER_SIZE + 4L * (1L + 8L);
        static final long RADIX_OFFSET = Node.HEADER_SIZE + (4L * 8L);
        static final long CHILDREN_OFFSET = Node.HEADER_SIZE;
        private static final int  MAX_CAPACITY = 4;

        Node4(AnyHeap heap) {
            super(heap, SIZE, (Range range) -> {
                range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE4_TYPE);
            });
        }

        Node4(AnyHeap heap, byte[] prefix, int start, int prefixLen, Node child, byte radix) {
            super(heap, SIZE, (Range range) -> {
                //set type
                range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE4_TYPE);
                //set prefix
                if (prefixLen > 0) {
                    if (prefixLen > 8) throw new IllegalArgumentException("Prefix more than 8 bytes");
                    range.setInt(Node.PREFIX_LENGTH_OFFSET, prefixLen);
                    range.copyFromArray(prefix, start, Node.COMPRESSED_PATH_OFFSET, prefixLen);
                }
                //set radix
                range.setByte(RADIX_OFFSET, radix);
                //set value
                range.setLong(CHILDREN_OFFSET, child.handle());
                //set childrencount
                range.setShort(Node.CHILDREN_COUNT_OFFSET, (short)1);
            });
        }

        Node4(AnyHeap heap, byte[] prefix, int prefixLen, boolean blank, Node child1, byte radix1, Node child2, byte radix2) {
            super(heap, SIZE, (Range range) -> {
                //set type
                range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE4_TYPE);
                //set prefix
                if (prefixLen > 0) {
                    if (prefixLen > 8) throw new IllegalArgumentException("Prefix more than 8 bytes");
                    range.setInt(Node.PREFIX_LENGTH_OFFSET, prefixLen);
                    range.copyFromArray(prefix, 0, Node.COMPRESSED_PATH_OFFSET, prefixLen);
                }
                //set radix
                if (blank) range.setByte(Node.BLANK_RADIX_INDEX_OFFSET, (byte)0);
                else range.setByte(RADIX_OFFSET, radix1);
                range.setByte(RADIX_OFFSET + 1, radix2);
                //set value
                range.setLong(CHILDREN_OFFSET, child1.handle());
                range.setLong(CHILDREN_OFFSET + 1 * Long.BYTES, child2.handle());
                //set childrencount
                range.setShort(Node.CHILDREN_COUNT_OFFSET, (short)2);
            });
        }

        Node4 (AnyHeap heap, AnyMemoryBlock mb) {
            super(heap, mb);
        }

        Node4 duplicate() {
            AnyMemoryBlock dmb = heap.allocateCompactMemoryBlock(SIZE, (Range rng) -> {
                rng.copyFromMemoryBlock(this.mb, 0, 0, SIZE);
            });
            return new Node4(heap, dmb);
        }

        @Override
        protected byte[] getRadices() {
            byte[] ret = new byte[getChildrenCount()];
            if (ret.length == 0) return ret;
            mb.copyToArray(RADIX_OFFSET, ret, 0, ret.length);
            return ret;
        }

        NodeEntry[] getEntries() {
            byte[] radices = getRadices();
            NodeEntry[] entries = new NodeEntry[radices.length];
            int blankIndex=getBlankRadixIndex();
            int index=0;
            if (blankIndex != -1) {
                entries[index++] = new NodeEntry((byte)0, (Leaf)getChildAtIndex(blankIndex));
            }
            for (int i=0; i < entries.length; i++) {
                if (i != blankIndex) entries[index++] = new NodeEntry(radices[i], getChildAtIndex(i));
            }
            Arrays.sort(entries, (blankIndex != -1) ? 1 : 0, entries.length, (x, y)-> LongART.compareUnsigned(x.radix, y.radix));
            return entries;
        }

        void addRadix(byte radix, int index) {
            mb.setByte(RADIX_OFFSET + index, radix);
        }

        @Override
        boolean addChild(byte radix, Node node) {
            int index = findChildIndex(radix);
            if (index == -1) {
                if (getChildrenCount() >= MAX_CAPACITY) {
                    return false;   // need to grow, out of capacity returns fals
                } else {
                    index = getChildrenCount();
                    incChildrenCount();
                    addRadix(radix, index);
                }
            }
            putChildAtIndex(index, node);
            return true;
        }

        void deleteChild(Byte radix) {
            int index;
            if (radix == null) {
                //blankRadix
                index = clearBlankRadixFlag();
            }
            else index = findChildIndex(radix);
            if (index != -1) {
                mb.withRange(Node.HEADER_SIZE, this.SIZE - Node.HEADER_SIZE, (Range range) -> {
                    decChildrenCount();
                    int childrenCount = getChildrenCount();
                    if (childrenCount == index) {
                        range.setLong(CHILDREN_OFFSET + childrenCount * Long.BYTES, 0L);
                    }
                    else {
                        range.setLong(CHILDREN_OFFSET + index * Long.BYTES, mb.getLong(CHILDREN_OFFSET + childrenCount * Long.BYTES));
                        range.setLong(CHILDREN_OFFSET + childrenCount * Long.BYTES, 0L);
                        range.setByte(RADIX_OFFSET + index, mb.getByte(RADIX_OFFSET + childrenCount));
                    }
                    range.setByte(RADIX_OFFSET + childrenCount, (byte)0);
                });
            }
        }
     
        // returns -1 if no valid child found at given index
        @Override
        int findChildIndex(byte radix) { // ignores blankRadixIndex
            byte[] radices = getRadices();
            int blankIndex = (int)getBlankRadixIndex();
            for (int i = 0; i < getChildrenCount(); i++) {
                if (i == blankIndex) continue;
                if (radices[i] == radix) return i;
            }
            return -1;
        }

        // returns 0 if index is -1 or no valid value exists at given index
        @Override
        protected long findValueAtIndex(int index) {
            if (index == -1) return 0;  // 0 == NULL
            return mb.getLong(CHILDREN_OFFSET + index * 8);
        }

        @Override
        void putChildAtIndex(int index, Node child) {
            mb.setLong(CHILDREN_OFFSET + index * Long.BYTES, child.handle());
        }

        @Override
        protected short capacity() { return (short)MAX_CAPACITY; }

        @Override
        InternalNode grow(Node child, Optional<Byte> radix) {
            return new Node16(heap, this, child, radix);
        }
         
        InternalNode split(Byte radix, boolean first) {
            byte[] radices = getRadices();
            Node4 newNode = new Node4(heap);
            // int pre = getChildrenCount();
            //int index;
            //TODO : handle blank radix specially
            int blankIndex = getBlankRadixIndex();
            for (int i = 0; i < radices.length; i++) {
                if (radix != null && i == blankIndex) continue;
                if (radix == null || LongART.compareUnsigned(radices[i], radix) >= 0) {
                    newNode.addChild(radices[i], getChildAtIndex(i));
                }
            }
            radices = newNode.getRadices();
            for (int i = 0; i < radices.length; i++) {
                if (!first && radices[i] == radix) continue;
                deleteChild(radices[i]);
            }
            if (getPrefixLength() > 0) {
                newNode.setPrefixLength(getPrefixLength());
                newNode.setPrefix(getPrefix());
            }
            return newNode;
        }
    }

    static class Node16 extends InternalNode {
        protected static final long SIZE = Node.HEADER_SIZE + 16L * (1L + 8L);
        private static final long RADIX_OFFSET = Node.HEADER_SIZE;
        static final long CHILDREN_OFFSET = Node.HEADER_SIZE + 16L;
        private static final int  MAX_CAPACITY = 16;

        Node16(AnyHeap heap, AnyMemoryBlock mb) {
            super(heap, mb);
        }

        Node16(AnyHeap heap) {
            super(heap, SIZE, (Range range) -> {
                range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE16_TYPE);
            });
        }

        Node16(AnyHeap heap, Node4 oldNode, Node newNode, Optional<Byte> radix) {
            super(heap, SIZE, (Range range) -> {
            // offset is 1 to skip the TYPE field that's already set
                range.setByte(NODE_TYPE_OFFSET, Node.NODE16_TYPE);
                range.copyFromMemoryBlock(oldNode.mb, 1, 1, Node.HEADER_SIZE - 1);
                range.copyFromMemoryBlock(oldNode.mb, Node4.RADIX_OFFSET, RADIX_OFFSET, oldNode.capacity());
                range.copyFromMemoryBlock(oldNode.mb, Node4.CHILDREN_OFFSET, Node16.CHILDREN_OFFSET, oldNode.capacity() * Long.BYTES);

                //set radix
                if (radix.isPresent()) range.setByte(RADIX_OFFSET + 4, radix.get());
                else range.setByte(Node.BLANK_RADIX_INDEX_OFFSET, (byte)4);
                //set value
                range.setLong(CHILDREN_OFFSET + 4 * Long.BYTES, newNode.handle()); 
                //set childrencount
                range.setShort(InternalNode.CHILDREN_COUNT_OFFSET, (short)(4 + 1));
            });
        }

        Node16 duplicate() {
            AnyMemoryBlock dmb = heap.allocateCompactMemoryBlock(SIZE, (Range rng) -> {
                rng.copyFromMemoryBlock(this.mb, 0, 0, SIZE);
            });
            return new Node16(heap, dmb);
        }

        @Override
        protected byte[] getRadices() {
            byte[] ret = new byte[getChildrenCount()];
            if (ret.length == 0) return ret;
            mb.copyToArray(RADIX_OFFSET, ret, 0, ret.length);
            return ret;
        }

        NodeEntry[] getEntries() {
            byte[] radices = getRadices();
            NodeEntry[] entries = new NodeEntry[radices.length];
            int blankIndex = getBlankRadixIndex();
            int index=0;
            if (blankIndex != -1) {
                entries[index++] = new NodeEntry((byte)0, getChildAtIndex(blankIndex));
            }
            for (int i=0; i < entries.length; i++) {
                if (i != blankIndex) entries[index++] = new NodeEntry(radices[i], getChildAtIndex(i));
            }
            Arrays.sort(entries, (blankIndex != -1) ? 1 : 0, entries.length, (x, y)-> LongART.compareUnsigned(x.radix, y.radix));
            return entries;
        }

        void addRadix(byte radix, int index) {
            mb.setByte(RADIX_OFFSET + index, radix);
        }

        @Override
        boolean addChild(byte radix, Node node) {
            int index = findChildIndex(radix);
            if (index == -1) {
                if (getChildrenCount() >= MAX_CAPACITY) {
                    return false;   // need to grow, out of capacity
                } else {
                    index = getChildrenCount();
                    incChildrenCount();
                    addRadix(radix, index);
                }
            }
            putChildAtIndex(index, node);
            return true;
        }

        void deleteChild(Byte radix) {
            int index;
            if (radix == null) {
                //blankRadix
                index = clearBlankRadixFlag();
            }
            else index = findChildIndex(radix);

            if (index != -1) {
                mb.withRange(Node.HEADER_SIZE, this.SIZE - Node.HEADER_SIZE, (Range range) -> {
                    decChildrenCount();
                    int childrenCount = getChildrenCount();
                    if (childrenCount == index) {
                        range.setLong(CHILDREN_OFFSET + childrenCount * Long.BYTES, 0L);
                    }
                    else {
                        range.setLong(CHILDREN_OFFSET + index * Long.BYTES, mb.getLong(CHILDREN_OFFSET + childrenCount * Long.BYTES));
                        range.setLong(CHILDREN_OFFSET + childrenCount * Long.BYTES, 0L);
                        range.setByte(RADIX_OFFSET + index, mb.getByte(RADIX_OFFSET + childrenCount));
                    }
                    range.setByte(RADIX_OFFSET + childrenCount, (byte)0);
                });
            }
        }
     
        // returns -1 if no valid child found at given index
        @Override
        int findChildIndex(byte radix) { // ignores blankRadixIndex
            byte[] radices = getRadices();
            int blankIndex = (int)getBlankRadixIndex();
            for (int i = 0; i < radices.length; i++) {
                if (i == blankIndex) continue;
                if (radices[i] == radix) return i;
            }
            return -1;
        }

        // returns 0 if index is -1 or no valid value exists at given index
        @Override
        protected long findValueAtIndex(int index) {
            if (index == -1) return 0;  // 0 == NULL
            return mb.getLong(CHILDREN_OFFSET + index * 8);
        }

        @Override
        void putChildAtIndex(int index, Node child) {
            if (index == -1) return;
            mb.setLong(CHILDREN_OFFSET + index * Long.BYTES, child.handle());
        }

        @Override
        protected short capacity() { return (short)MAX_CAPACITY; }

        @Override
        InternalNode grow(Node child, Optional<Byte> radix) {
            return new Node48(heap, this, child, radix);
        }

        InternalNode split(Byte radix, boolean first) {
            byte[] radices = getRadices();
            Node16 newNode = new Node16(heap);
            int blankIndex = getBlankRadixIndex();
            for (int i = 0; i < radices.length; i++) {
                if (radix != null && i == blankIndex) continue;
                if (radix == null || LongART.compareUnsigned(radices[i], radix) >= 0) {
                    newNode.addChild(radices[i], getChildAtIndex(i));
                }
            }
            radices = newNode.getRadices();
            for (int i = 0; i < radices.length; i++) {
                if (!first && radices[i] == radix) continue;
                deleteChild(radices[i]);
            }
            if (getPrefixLength() > 0) {
                newNode.setPrefixLength(getPrefixLength());
                newNode.setPrefix(getPrefix());
            }
            return newNode;
        }
    }

    static class Node48 extends InternalNode {
        private static final int  MAX_CAPACITY = 48;
        private static final int  MAX_RADICES = 256;

        protected static final long SIZE = Node.HEADER_SIZE + 1L * (long)MAX_RADICES + (long)(Long.BYTES * MAX_CAPACITY);
        private static final long RADIX_OFFSET = Node.HEADER_SIZE;
        static final long CHILDREN_OFFSET = Node.HEADER_SIZE + 256L;

        // Special design to handle zero being both init value and a valid index:
        // the indices to the children will be 1-based, so an index of 0 is invalid
        private byte[] radices;

        Node48(AnyHeap heap, AnyMemoryBlock mb) {
            super(heap, mb);
        }

        Node48(AnyHeap heap) {
            super(heap, SIZE, (Range range) -> {
                range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE48_TYPE);
            });
        }

        Node48(AnyHeap heap, Node16 oldNode, Node newNode, Optional<Byte> radix) {
            super(heap, SIZE, (Range range) -> {
            // offset is 1 to skip the TYPE field that's already set
                range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE48_TYPE);
                range.copyFromMemoryBlock(oldNode.mb, 1, 1, Node.HEADER_SIZE - 1);
                int blankRadixIndex = oldNode.getBlankRadixIndex();
                byte[] oldRadices = oldNode.getRadices();
                StringBuffer radices = new StringBuffer("radics: ");
                for (int index = 0; index < oldRadices.length; index++) {
                    // Skip the blank child radix, otherwise it will map the 0th
                    // index to the blank child when it's not supposed to
                    if (index != blankRadixIndex) {
                        range.setByte(RADIX_OFFSET + Byte.toUnsignedInt(oldRadices[index]), (byte)(index + 1));
                        radices.append(" "+oldRadices[index]);
                    }
                }
                range.copyFromMemoryBlock(oldNode.mb, Node16.CHILDREN_OFFSET, Node48.CHILDREN_OFFSET, oldNode.capacity() * Long.BYTES);
                //set radix
                if (radix.isPresent()) range.setByte(RADIX_OFFSET + Byte.toUnsignedInt(radix.get()), (byte)(16 + 1));
                else range.setByte(Node.BLANK_RADIX_INDEX_OFFSET, (byte)16);
                //set value
                range.setLong(CHILDREN_OFFSET + 16 * Long.BYTES, newNode.handle());
                //set childrencount
                range.setShort(InternalNode.CHILDREN_COUNT_OFFSET, (short)(16 + 1));
            });
        }
        
        Node48 duplicate() {
            AnyMemoryBlock dmb = heap.allocateCompactMemoryBlock(SIZE, (Range rng) -> {
                rng.copyFromMemoryBlock(this.mb, 0, 0, SIZE);
            });
            return new Node48(heap, dmb);
        }

        @Override
        protected byte[] getRadices() {
            if (radices == null) {
                byte[] radices = new byte[MAX_RADICES];
                mb.copyToArray(RADIX_OFFSET, radices, 0, MAX_RADICES);
                this.radices = radices;
            }
            return this.radices;
        }

        void addRadix(byte radix, int index) {
            mb.setByte(RADIX_OFFSET + index, radix);
        }

        @Override
        NodeEntry[] getEntries() {
            byte[] radices = getRadices();
            NodeEntry[] entries = new NodeEntry[getChildrenCount()];
            int blankIndex = getBlankRadixIndex();
            int index=0;
            if (blankIndex != -1) {
                entries[index++] = new NodeEntry((byte)0, getChildAtIndex(blankIndex));
            }
            for (int i=0; i < MAX_RADICES; i++) {
                if (radices[(int)i] != (1 + blankIndex) && radices[(int)i] !=0) {
                    entries[index++] = new NodeEntry((byte)(i), getChildAtIndex(radices[i]-1));
                }
            }
            return entries;
        }

        @Override
        boolean addChild(byte radix, Node node) {
            int index = findChildIndex(radix);
            if (index == -1) {
                if (getChildrenCount() >= MAX_CAPACITY) {
                    return false;   // need to grow, out of capacity
                } else {
                    index = getChildrenCount();
                    incChildrenCount();
                    // For Node48, the radix field is an index to the child,
                    // and the radix itself is used a the index into the radix field
                    addRadix((byte)(index + 1), Byte.toUnsignedInt(radix));   // +1 for 1-based indices
                }
            }
            putChildAtIndex(index, node);
            this.radices = null;
            return true;
        }
        
        void deleteChild(Byte radix) {
            int index;
            if (radix == null) {
                index = clearBlankRadixFlag();
            }
            else index = findChildIndex(radix);
            if (index != -1) {
                mb.withRange(0, this.SIZE, (Range range) ->{
                    if (radix != null) {
                        //delete radix
                        range.setByte(RADIX_OFFSET + Byte.toUnsignedInt(radix), (byte)0);
                    }
                    //overwrite child value with the most recent child
                    decChildrenCount();
                    int childrenCount = getChildrenCount();
                    if (childrenCount == index) {
                        range.setLong(CHILDREN_OFFSET + childrenCount * Long.BYTES, 0L);
                    } 
                    else {
                        range.setLong(CHILDREN_OFFSET + index * Long.BYTES, mb.getLong(CHILDREN_OFFSET + childrenCount * Long.BYTES));
                        range.setLong(CHILDREN_OFFSET + childrenCount * Long.BYTES, 0L);
                        //update radix of most recent child
                        byte[] radices = getRadices();
                        for (int i = 0; i < radices.length; i++) {
                            if ((int)(radices[i]) == childrenCount + 1) {
                                range.setByte(RADIX_OFFSET + i, (byte)(index+1));
                                break;
                            }
                        }
                    }
                    this.radices = null;
                });
            }
        }
        
        @Override
        byte findLowestRadix() {
            byte lowest = Byte.MAX_VALUE;
            byte[] radices = getRadices();
            for (int i = 0; i < MAX_RADICES; i++) {
                if (radices[i] != 0) {
                    lowest = (byte)i;
                    break;
                }
            }
            return lowest;
        }
        
        @Override
        byte findHighestRadix() {
            byte highest = Byte.MIN_VALUE;
            byte[] radices = getRadices();
            for (int i = MAX_RADICES - 1; i >= 0; i--) {
                if (radices[i] != 0) {
                    highest = (byte)(i);
                    break;
                }
            }
            return highest;
        }
        
        // returns -1 if no valid child found at given index
        @Override
        int findChildIndex(byte radix) { // ignores blankRadixIndex
            byte[] radices = getRadices();
            int index = Byte.toUnsignedInt(radix);
            return radices[index] == 0 ? -1 : (int)radices[index] - 1;
        }

        // returns 0 if index is -1 or no valid value exists at given index
        @Override
        protected long findValueAtIndex(int index) {
            if (index == -1) return 0;
            return mb.getLong(CHILDREN_OFFSET + Long.BYTES * index);
        }

        @Override
        void putChildAtIndex(int index, Node child) {
            if (index == -1) return;
            mb.setLong(CHILDREN_OFFSET + index * Long.BYTES, child.handle());
        }

        @Override
        protected short capacity() { return (short)MAX_CAPACITY; }

        @Override
        InternalNode grow(Node child, Optional<Byte> radix) {
            return new Node256(heap, this, child, radix);
        }

        InternalNode split(Byte radix, boolean first) {
            byte[] radices = getRadices();
            Node48 newNode = new Node48(heap);
            // copy prefix
            if (getPrefixLength() > 0) {
                newNode.setPrefixLength(getPrefixLength());
                newNode.setPrefix(getPrefix());
            }
            int startIndex = (radix == null) ? 0 : Byte.toUnsignedInt(radix);
            for (int i = startIndex; i < MAX_RADICES; i++) {
                if((int)radices[i] != 0) {
                     newNode.addChild((byte)i, getChildAtIndex((int)radices[i] - 1));
                }
            }
            radices = newNode.getRadices();
            for (int i = first ? startIndex : startIndex + 1; i < radices.length; i++) {
                if (radices[i] != 0) deleteChild((byte)i);
            }
            newNode.radices = null;
            this.radices = null;
            return newNode;
        }

        @Override
        void printChildren(StringBuilder start, int depth) {
            byte[] radices = getRadices();
            Node node;
            for (int i = 0; i < radices.length; i++) {
                if (radices[i] != 0) {
                    System.out.println(start + "For radix " + new String(new byte[]{(byte)(i-128)}) + "(" + (i-128) + "):");
                    node = getChildAtIndex(radices[i]-1);
                    if(node != null)
                    {
                        node.print(depth + 1);
                    }
                }
            }
        }
    }

    static class Node256 extends InternalNode {
        // 257 for Node256: 1 more slot for the blank radix child
        protected static final long SIZE = Node.HEADER_SIZE + 257L * 8L;
        private static final long CHILDREN_OFFSET = Node.HEADER_SIZE;
        private static final int  BLANK_RADIX_CHILD_INDEX = 256;
        private static final int  MAX_CAPACITY = 257;

        Node256(AnyHeap heap, AnyMemoryBlock mb) {
            super(heap, mb);
        }

        Node256(AnyHeap heap) {
            super(heap, SIZE, (Range range) -> {
                range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE256_TYPE);
            });
        }

        Node256(AnyHeap heap, Node48 oldNode, Node newNode, Optional<Byte> radix) {
            super(heap, SIZE, (Range range) -> {
            // offset is 1 to skip the TYPE field that's already set
                range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE256_TYPE);
                range.copyFromMemoryBlock(oldNode.mb, 1, 1, Node.HEADER_SIZE - 1);
                byte[] oldRadices = oldNode.getRadices();
                int blankIndex = (int)oldNode.getBlankRadixIndex();
                for (int index = 0; index < oldRadices.length; index++) {
                    if (oldRadices[index] != 0) {
                        range.setLong(CHILDREN_OFFSET + index * Long.BYTES, oldNode.findValueAtIndex((int)(oldRadices[index] - 1)));
                    }
                    if (index == blankIndex) {
                        range.setLong(CHILDREN_OFFSET + BLANK_RADIX_CHILD_INDEX * Long.BYTES, oldNode.findValueAtIndex(blankIndex));
                        // Node256 stores its blank child in a different location. need to reset header
                        range.setByte(Node.BLANK_RADIX_INDEX_OFFSET, (byte)0xff);
                    }
                }
                //set value
                if (radix.isPresent()) range.setLong(CHILDREN_OFFSET + Byte.toUnsignedInt(radix.get()) * Long.BYTES, newNode.handle());
                else range.setLong(CHILDREN_OFFSET + BLANK_RADIX_CHILD_INDEX * Long.BYTES, newNode.handle());
                //set childrencount
                range.setShort(InternalNode.CHILDREN_COUNT_OFFSET, (short)(48 + 1));
            });
        }

        Node256 duplicate() {
            AnyMemoryBlock dmb = heap.allocateCompactMemoryBlock(SIZE, (Range rng) -> {
                rng.copyFromMemoryBlock(this.mb, 0, 0, SIZE);
            });
            return new Node256(heap, dmb);
        }

        @Override
        boolean hasBlankRadixChild() {
            return (findValueAtIndex(BLANK_RADIX_CHILD_INDEX) != 0);
        }
    
        @Override
        Leaf findBlankRadixChild() {
            long val = findValueAtIndex(BLANK_RADIX_CHILD_INDEX);
            return (val == 0) ? null : (Leaf)Node.rebuild(heap, val);
        }

        @Override
        boolean addBlankRadixChild(Leaf child) {
            if (hasBlankRadixChild()) {
                //findBlankRadixChild().setValue(child.getValue());
            } else {
                putChildAtIndex(BLANK_RADIX_CHILD_INDEX, child);
                incChildrenCount();
            }
            return true;
        }

        @Override
        protected byte[] getRadices() {
            return null;    // does not store radices
        }

        @Override
        NodeEntry[] getEntries() {
            NodeEntry[] entries = new NodeEntry[getChildrenCount()];
            int index=0;
            Node blankChild = findBlankRadixChild();
            if (blankChild != null) {
                entries[index++] = new NodeEntry((byte)0, blankChild);
            }
            long val;
            for (int i = 0; i < 256; i++) {
                if ((val = findValueAtIndex(i)) != 0) entries[index++] = new NodeEntry((byte)i, Node.rebuild(heap, val)); 
            }
            return entries;
        }

        @Override
        boolean addChild(byte radix, Node node) {
            int index = findChildIndex(radix);
            if (index == -1) {
                incChildrenCount();
                index = Byte.toUnsignedInt(radix);
            }
            putChildAtIndex(index, node);
            return true;
        }

        void deleteChild(Byte radix) {
            int index;
            if (radix == null) index = BLANK_RADIX_CHILD_INDEX;
            else index = findChildIndex(radix);
            if (index != -1) {
                //delete child at 'index'
                mb.setLong(CHILDREN_OFFSET + index * Long.BYTES, 0L);
                decChildrenCount();
            }
        }
        
        @Override
        byte findLowestRadix() {
            byte lowest = Byte.MAX_VALUE;
            for (int i=0; i < 256; i++) {
                if (findValueAtIndex(i) != 0) {
                    lowest = (byte)i;
                    break;
                }
            }
            return lowest;
        }

        @Override
        byte findHighestRadix() {
            byte highest = Byte.MIN_VALUE;
            for (int i=255; i >= 0; i--) {
                if (findValueAtIndex(i) != 0) {
                    highest = (byte)i;
                    break;
                }
            }
            return highest;
        }

        // returns null if no valid child found at given index
        @Override
        Node findChild(byte radix) {
            return getChildAtIndex(Byte.toUnsignedInt(radix));
        }

        // returns -1 if no valid child found at given index
        @Override
        int findChildIndex(byte radix) {
            int index = Byte.toUnsignedInt(radix);
            return findValueAtIndex(index) == 0 ? -1 : index;
        }

        // returns 0 if index is -1 or no valid value exists at given index
        @Override
        protected long findValueAtIndex(int index) {
            if (index == -1) return 0;
            return mb.getLong(CHILDREN_OFFSET + index * Long.BYTES);
        }

        @Override
        void putChildAtIndex(int index, Node child) {
            mb.setLong(CHILDREN_OFFSET + index * Long.BYTES, child.handle());
        }

        @Override
        protected short capacity() { return (short)MAX_CAPACITY; }

        @Override
        InternalNode grow(Node child, Optional<Byte> radix) { return null; }

        InternalNode split(Byte radix, boolean first) {
            Node256 newNode = new Node256(heap);
            if (radix == null) {
                newNode.mb.withRange(0, this.SIZE, (Range rng) -> {
                    rng.copyFromMemoryBlock(mb, CHILDREN_OFFSET, CHILDREN_OFFSET, (MAX_CAPACITY - 1) * Long.BYTES);
                    rng.setShort(CHILDREN_COUNT_OFFSET, (short)(getChildrenCount() - 1)); 
                });                
                mb.withRange(0, this.SIZE, (Range rng) -> {
                    rng.setMemory((byte)0, CHILDREN_OFFSET, (MAX_CAPACITY - 1) * Long.SIZE);
                    rng.setShort(CHILDREN_COUNT_OFFSET, (short)1);
                });
            }
            else {
                for (int i = Byte.toUnsignedInt(radix); i < 256; i++) {
                    if (findValueAtIndex(i) != 0) {
                        newNode.putChildAtIndex(i, getChildAtIndex(i));
                        newNode.incChildrenCount();
                        if (!first && i == Byte.toUnsignedInt(radix)) continue;
                        deleteChild((byte)i);
                    }
                }
                if (newNode.findValueAtIndex(Byte.toUnsignedInt(radix)-1) != 0) throw new RuntimeException();
            }
            if (getPrefixLength() > 0) {
                newNode.setPrefixLength(getPrefixLength());
                newNode.setPrefix(getPrefix());
            }
            return newNode;
        }

        @Override
        void printStatsChildren(StringBuilder start, int depth) {
            Node node;
            int i;
            for (i = 0; i < capacity(); i++) {
                if (findValueAtIndex(i) != 0 && (node = getChildAtIndex(i)) != null) {
                    node.statsPrint(depth + 1); 
                }
            } 
            if (i != capacity()) throw new RuntimeException("Node 256, Empty child or childrencount is wrong");
        }

        @Override
        void printChildren(StringBuilder start, int depth) {
            for (int i = 0; i < capacity(); i++) {
                if (findValueAtIndex(i) != 0) { System.out.println(start + "For radix " + new String(new byte[]{(byte)(i-128)}) + "(" + (i-128) + "):"); Node node = getChildAtIndex(i);
                    if( node != null) {
                        node.print(depth + 1);
                    }
                }
            }
        }
    }
}
