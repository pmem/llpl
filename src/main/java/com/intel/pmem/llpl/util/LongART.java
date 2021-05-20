/* 
 * Copyright (C) 2019-2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.util;

import com.intel.pmem.llpl.*;
import java.nio.ByteBuffer;
import java.util.function.*;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.NoSuchElementException;

public class LongART implements DynamicShardable<byte[]> {
    final AnyHeap heap;
    private Root root;
    private int maxKeyLen;
    //private int maxDepth;
    private long count = 0;
    static final long LONG_MASK = 1L << 63;
    static final int INT_MASK = 1 << 31;
    private byte[] lastKey;

    ReentrantLock lock = new ReentrantLock(false);
    
    public void lock() { this.lock.lock(); }

    public void unlock() { this.lock.unlock(); }

    public boolean isLocked() { return lock.isLocked(); }

    public LongART(AnyHeap heap) {
        this.heap = heap;   // maybe from TreeManager?
        this.root = new Root(heap);
    }
    
    public static void registerAllocationClasses(AnyHeap heap) {
        heap.registerAllocationSize(SimpleLeaf.SIZE, true);
        heap.registerAllocationSize(Node4.SIZE, true);
        heap.registerAllocationSize(Node16.SIZE, true);
        heap.registerAllocationSize(Node48.SIZE, true);
        heap.registerAllocationSize(Node256.SIZE, true);
    }

    @SuppressWarnings("unchecked")
    public LongART(AnyHeap heap, long handle) {
        if (handle < 0) throw new IllegalArgumentException("Invalid artree handle: "+handle);
        this.heap = heap;
        this.root = (Root)Node.rebuild(heap, handle);
        count = this.root.getCount();
        maxKeyLen = this.root.getMaxKeyLength(); 
    }
     
    public long handle() {
        return root.handle();
    }

    public void free() {
        root.free();
    }

    protected static int compareUnsigned(byte b1, byte b2) {
        return Integer.compareUnsigned(Byte.toUnsignedInt(b1), Byte.toUnsignedInt(b2));
    }

    enum Operation {
        DELETE_NODE,
        END,
        NO_OP;
    }

    public long size() {
        return root.getCount();
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

    synchronized void debug() {
        int numEntries = 0;
        Entry en = null;
        Iterator<Entry> e = getEntryIterator();
        System.out.println("================================================================");
        while (e.hasNext()) {
            en = e.next();
            System.out.println(format(en.getKey())+" => "+en.getValue());
            numEntries+=1;
        }
        if (numEntries != count) throw new RuntimeException("count is "+count+", numEntries is "+numEntries+" lastKey found is "+format(en.getKey()));
        // if (numEntries != count) System.err.println("count is "+count+", numEntries is "+numEntries+" lastKey found is "+format(en.getKey()));
    }

    public byte[] firstKey() {
        Node n = root.getChild();
        if (n == null) throw new NoSuchElementException();
        ByteBuffer lastKey = ByteBuffer.allocate(maxKeyLen);
        int depth = 0;
        while (true) {
            byte[] tmp = n.getPrefix();
            if (tmp.length > 0) lastKey.put(tmp); depth += tmp.length;
            if (n.isLeaf()) break;
            if (((InternalNode)n).hasBlankRadixChild()) break;
            byte b = ((InternalNode)n).findLowestRadix();
            lastKey.put(b); depth++;
            n = ((InternalNode)n).findChild(b);
        }
        return Arrays.copyOf(lastKey.array(), lastKey.position());
    }

    public byte[] lastKey() {
        Node n = root.getChild();
        //if (n == null) return new byte[]{};
        if (n == null) throw new NoSuchElementException();
        ByteBuffer lastKey = ByteBuffer.allocate(maxKeyLen);
        int depth = 0;
        while (true) {
            byte[] tmp = n.getPrefix();
            if (tmp.length > 0) lastKey.put(tmp); depth += tmp.length;
            if (n.isLeaf()) break;
            byte b = ((InternalNode)n).findHighestRadix();
            lastKey.put(b); depth++;
            InternalNode n1 = (InternalNode)n;
            n = ((InternalNode)n).findChild(b);
            if (n == null){
                System.out.println("n is a node "+n1.capacity());
            } 
        }
        return Arrays.copyOf(lastKey.array(), lastKey.position());
    }

    /**
     * inserts a byte[] key into this tree 
     * @param radixKey the key 
     * @param value the value 
     */    
    public void put(byte[] radixKey, Long value) {
        /*if (radixKey == null) throw new IllegalArgumentException("radixKey cannot be null");
        if (value == null) throw new IllegalArgumentException("value cannot be null");
        if (radixKey.length > maxKeyLen) maxKeyLen = radixKey.length;
        heap.execute(() -> {
            insert(root, root.getChild(), radixKey, value, 0, 0, (a, b) -> {return value;});
        });*/
        put(radixKey, value, (a, b) -> { return value; });
    }

    /**
     * inserts a byte[] key into this tree 
     * @param radixKey the key 
     * @param value the value 
     * @param merge Bifunction that returns a long value to be stored in this tree 
     */    
    public void put(byte[] radixKey, Object value, BiFunction<Object, Long, Long> merge) {
        if (radixKey == null || radixKey.length == 0) throw new IllegalArgumentException("Invalid radixKey");
        if (value == null) throw new IllegalArgumentException("value cannot be null");
        if (radixKey.length > maxKeyLen) setMaxKeyLength(radixKey.length);
        heap.execute(() -> {
            insert(root, root.getChild(), radixKey, value, 0, 0, merge);
        });
    }

    @SuppressWarnings("unchecked")
    private void insert(Node parent, Node node, byte[] key, Object value, int depth, int replaceIndex, BiFunction<Object, Long, Long> merge) {
        if (node == null) {    // empty tree
            long leafValue = merge.apply(value, 0L);

            Root rt = (Root)parent;    // if tree is empty, parent is guaranteed to be root
            Node leaf = SimpleLeaf.create(this.heap, key, 0, key.length, leafValue);
            rt.addChild(leaf);
            incrementCount();
            return;
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
                return;
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
            /*int newDepth = depth - 1 + (prefixLength / 9);
            if (newDepth > maxDepth) { 
                System.out.println("keylength is "+key.length+" current depth is "+depth+" prefixLen is "+prefixLength +" current maxDepth is "+maxDepth);
                setMaxDepth(newDepth);
            }*/
            return;
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
            /*int newDepth = depth - 1 + (prefixLength / 9);
            if (newDepth > maxDepth) { 
                System.out.println("keylength is "+key.length+" current depth is "+depth+" prefixLen is "+prefixLength +" current maxDepth is "+maxDepth);
                setMaxDepth(newDepth);
            }*/
            return;
        }

        // prefix is a subset of the key
        depth += intNode.getPrefixLength(); // or just += matchedLength;
        if (depth == key.length) {
            //this insertion will be a blankradix child to this internal node
            if (intNode.hasBlankRadixChild()) {
                Leaf child = intNode.findBlankRadixChild();
                long old = child.getValue();
                long newVal = merge.apply(value, old);
                if (old != newVal) child.setValue(newVal);
            }
            else{
                long newVal = merge.apply(value, 0L);
                SimpleLeaf leaf = new SimpleLeaf(this.heap, newVal);
                if (!intNode.addBlankRadixChild(leaf)) {
                    InternalNode newNode = intNode.grow(leaf, Optional.empty());
                    ((InternalNode)parent).putChildAtIndex(replaceIndex, newNode);
                    intNode.free();
                }
                incrementCount();
            }
            // no need to update prefix for a blank radix child - it has no prefix
            return;
        }
        //descending find next node with matching radix
        int childIndex = intNode.findChildIndex(key[depth]);
        //if (childIndex != -1 && childIndex == intNode.getBlankRadixIndex()) throw new RuntimeException("Child index is "+childIndex +" nodetype is "+intNode.getType()+" key[depth] = "+key[depth]);
        Node next = intNode.getChildAtIndex(childIndex);
        if (next != null) {
            insert(node, next, key, value, depth + 1, childIndex, merge);
        } else {
            // found insertion point. insert leaf
            int prefixLength = key.length - depth - 1;
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
        /*int newDepth = depth - 1 + (prefixLength / 9);
            if (newDepth > maxDepth) { 
                System.out.println("keylength is "+key.length+" current depth is "+depth+" prefixLen is "+prefixLength +" current maxDepth is "+maxDepth);
                setMaxDepth(newDepth);
            }*/
        }
    }

    public long get(byte[] radixKey) {
        if (radixKey == null || radixKey.length == 0) throw new IllegalArgumentException("Invalid radixKey");
        Node node;
        if ((node = root.getChild()) != null) {
                return search(root.getChild(), radixKey, 0, null, null);
        }
        return 0;
    }

    public byte[] splitKey() {
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

    public LongART split() {
        EntryIterator it = new EntryIterator();
        // long pre = count;
        long midPos = count / 2;
        int i = 0;
        byte[] splitKey = null;
        // byte[] newFirstKey = null;
        while(it.hasNext()) {
            if(i++ < midPos)
                it.next();
            else {
                splitKey = it.next().getKey();
                //if (it.hasNext()) newFirstKey = it.next().getKey();
                break;
            }
        } 
        // LongART ret = split(splitKey);
        // System.out.println("precount is "+pre+" midPos is "+midPos+" oldCount is "+count+" newCount is "+ret.count);
        // debug();
        // ret.debug();
        // if (keyCompare(lastKey(), splitKey) != 0) throw new RuntimeException("lastkey: "+format(lastKey())+" splitkey is "+format(splitKey));
        // if (keyCompare(ret.firstKey(), newFirstKey) != 0) throw new RuntimeException("new first key expected "+format(newFirstKey)+ " but found "+format(ret.firstKey()));
        // if (keyCompare(lastKey(), ret.firstKey()) > 0) throw new RuntimeException();
        //System.out.println("Done splitting.");
        // return ret;
        return split(splitKey);
    }

    private String format(byte[] ba) {
        StringBuffer sb = new StringBuffer("[ ");
        for (int i = 0; i< ba.length; i++) {
            sb.append(Byte.toUnsignedInt(ba[i])+ " ");
        }
        sb.append("]");
        return sb.toString();
    }

    int keyCompare(byte[] firstKey, byte[] nextKey){
            int ret = 0;
            int i = 0;
            while (i < firstKey.length && i < nextKey.length && (ret = compareUnsigned(firstKey[i], nextKey[i])) == 0) i++;
            return (ret == 0) ? Integer.compare(firstKey.length, nextKey.length) : ret;
    }

    public LongART split(byte[] splitKey) {
        // navigate to splitkey
        // build a cache of copied nodes as you iterate
        // navigate backup and remove radices less than key[depth]
        // return new tree. yikes.
        LongART newTree = new LongART(heap);
        Root newRoot = newTree.root;
        SearchHelper splitFunc = (Node parent, Node child, Byte radix, Consumer<Long> c) -> {
            if (radix == null) System.out.println("Uh oh! radix is null");
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
        /*Node n = root.getChild();
        if (!n.isLeaf() && ((InternalNode)n).getChildrenCount() == 0) {
            root.deleteChild();    
        }*/
        long newcount = 1 + (root.getCount() / 2);
        count = newcount;
        root.setCount(newcount);
        newRoot.setCount(newcount - 2);
        newTree.count = newcount - 2;
        return newTree;
    }

    void mergeTree(LongART tree) {
    // add each child entry in tree top node to this trees top node
        if (!tree.root.getChild().isLeaf()) {
            InternalNode from = (InternalNode)tree.root.getChild();
            InternalNode to = (InternalNode)this.root.getChild();
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

    public void statsPrint() {
        System.out.println("Printing Stats .Printing Stats ...");
        if (root.getChild() != null)
            root.getChild().statsPrint(0);
        System.out.println("");
    }

    public void print() {
        if (root.getChild() != null)
            root.getChild().print(0);
        System.out.println("");
    }

    public void clear(Consumer<Long> cleaner) {
        if (cleaner == null) throw new IllegalArgumentException("cleaner function cannot be null");
        heap.execute(() -> {
            root.destroy(cleaner);
            count = 0;
        });
    }

    @FunctionalInterface
    public interface SearchHelper {
        void apply(Node parent, Node child, Byte radix, Consumer<Long> cleaner);
    }

    void deleteNodes(Node parent, Node child, Byte radix, Consumer<Long> cleaner) {
        heap.execute(()->{
            if (child.isLeaf()) {
            //    System.out.println("Ascending: deleting node at radix "+new String(new byte[]{radix}));
                if (cleaner != null) cleaner.accept(((SimpleLeaf)child).getValue());
                child.free();
                ((InternalNode)parent).deleteChild(radix);
                decrementCount();
            }
            else if (((InternalNode)child).getChildrenCount() == 0) {
            //    System.out.println("Ascending: deleting node at radix "+new String(new byte[]{radix}));
                child.free();
                ((InternalNode)parent).deleteChild(radix);
            }
        });
    }

    public void delete(byte[] key, Consumer<Long> cleaner) {
        search(root.getChild(), key, 0, this::deleteNodes , cleaner);
    }

    void forEach(BiFunction<byte[], Long, Operation> fcn) {
        new InternalIterator(fcn, new byte[0]);
    }

    void forEach(BiFunction<byte[], Long, Operation> fcn, byte[] firstKey) {
        new InternalIterator(fcn, firstKey);
    }

    //public ValueIterator getValueIterator() {
    public Iterator<Long> getValueIterator() {
        return new ValueIterator();
    }

    public Iterator<LongART.Entry> getEntryIterator() {
        return new EntryIterator();
    }

    public Iterator<Entry> getHeadEntryIterator(byte[] lastKey, boolean lastInclusive) {
        if (lastKey == null || lastKey.length == 0) throw new IllegalArgumentException();
        return new EntryIterator(lastKey, lastInclusive);
    }

    public Iterator<Entry> getTailEntryIterator(byte[] firstKey, boolean firstInclusive) {
        if (firstKey == null || firstKey.length == 0) throw new IllegalArgumentException();
        return new EntryIterator(firstKey, firstInclusive, null, false);
    }

    public Iterator<Entry> getEntryIterator(byte[] firstKey, boolean firstInclusive, byte[] lastKey, boolean lastInclusive) {
        if (firstKey == null || firstKey.length == 0 || lastKey == null || lastKey.length == 0) throw new IllegalArgumentException();
        return new EntryIterator(firstKey, firstInclusive, lastKey, lastInclusive);
    }

    class StackItem {
        NodeEntry[] entries;
        int index = 0;
        int prefixLen = 0;
        private final boolean hasBlank;

        public StackItem(NodeEntry[] entries, int prefixLen, boolean hasBlank) {
            this.entries = entries;
            this.prefixLen = prefixLen;
            this.hasBlank = hasBlank;
        }

        public StackItem(NodeEntry[] entries, int prefixLen, Byte radix) {
            this.entries=entries;
            this.prefixLen=prefixLen;
            this.hasBlank=false;
            if (radix != entries[0].radix) this.index = calcIndex(radix);
        }

        int calcIndex(byte radix) {
            int i;
            for (i = 0; i < entries.length; i++) {
                if (compareUnsigned(radix, entries[i].radix) <= 0) break;
            }
            return i;
        }

        public boolean isDone() {
            return index >= entries.length;
        }

        public boolean currentIsBlank() {
            return hasBlank && (index == 0);
        }

        public int prefixLen() {
            return prefixLen;
        }

        public NodeEntry[] entries() {
            return entries;
        }

        public void saveIndex(int idx) {
            index = idx;
        }

        public int getIndex() {
            return index;
        }

        public void incrementIndex() { index+=1; }

        public boolean hasBlank() {
            return hasBlank;
        }

        public int length() {
            return entries.length;
        }

        public NodeEntry entryAt(int index) {
            return entries[index];
        }

        public NodeEntry entryAtIndex() {
            return entries[index];
        }
    }

    public static class Entry {
        byte[] key;
        long value;

        public Entry(byte[] key, long value) {
            this.key = key;
            this.value = value;
        }

        public byte[] getKey() {
            return key;
        }

        public long getValue() {
            return value;
        }
    }

    private class InternalIterator {
        byte[] key;
        byte[] firstKey;
        boolean visited = false;
        boolean found = true;

        BiFunction<byte[], Long, Operation> fcn;

        public InternalIterator(BiFunction<byte[], Long, Operation> fcn, byte[] firstKey) {
            this.fcn = fcn;
            this.key = new byte[50];
            this.firstKey = firstKey;
            if (firstKey.length > 0) {
                 System.arraycopy(firstKey, 0, key, 0, firstKey.length);
                 found = false;
            }
            Node node = root.getChild();
            findLowestKey(node, 0);
            if (!node.isLeaf() && ((InternalNode)node).getChildrenCount() == 0) {
                root.deleteChild();
            }
        }

        void findLowestKey(Node node, int depth) {
            // System.out.println("FLK: depth is "+depth);
            boolean blank = false;
            visited = false;
            if (node == null) return;
            if (node.isLeaf()) {
                 // System.out.println("node is leaf! value->"+((SimpleLeaf)node).getValue());
                 // System.out.println("next Key is "+ (new String(key, 0, depth))+" depth is "+depth);
                //if (!found) found = true;
                return;
            }
            while(true) {
                Node next = null;
                // copy node prefix into key
                byte[] prefix = node.getPrefix();
                if (!found) {
                    int matchedLength = node.checkPrefix(key, depth);
                    if (matchedLength != node.getPrefixLength()) {
                        found = true;
                        // System.out.println("not a match!");
                        break;
                    }
                    else {
                        depth += matchedLength;
                        next = (depth == firstKey.length) ? ((InternalNode)node).findBlankRadixChild() : ((InternalNode)node).findChild(key[depth++]);
                    }
                } else {
                if (prefix.length != 0) {
                    System.arraycopy(prefix, 0, key, depth, prefix.length);
                    depth+=prefix.length;
                }
                Byte b;
                // blankRadix check
                if (!blank && !visited && ((InternalNode)node).hasBlankRadixChild()) {
                    blank = true;
                } else {
                    blank = false;
                    b = ((InternalNode)node).findLowestRadix(key[depth], visited);
                    if (b == null) break;
                    key[depth] = b;
                }
                // System.out.println("blank is "+blank);
                next = blank ? ((InternalNode)node).findBlankRadixChild() : ((InternalNode)node).findChild(key[depth++]);
                }
                if (depth == 0 || next == null) break;
                findLowestKey(next, depth);
                // Ascending
                if (!blank) visited = true;
                if (next.isLeaf()) {
                    byte[] leafPrefix = next.getPrefix();
                    if (!found) {
                        found = true;
                        for (int i=0; i<leafPrefix.length; i++) {
                            System.out.println(Long.toHexString(leafPrefix[i]));
                        }
                        int matchedLength = next.checkPrefix(key, depth);
                        if (matchedLength != leafPrefix.length) {
                            depth-=prefix.length; if (!blank) depth--;
                            continue;
                        }
                    }
                    if (leafPrefix.length != 0) {
                        System.arraycopy(leafPrefix, 0, key, depth, leafPrefix.length);
                    }
                    final int d = depth + leafPrefix.length;
                    Operation op = fcn.apply(Arrays.copyOf(key, d), ((SimpleLeaf)next).getValue());
                    if (op == Operation.DELETE_NODE) {
                        final Node fnext = next;
                        final boolean fblank = blank;
                        Transaction.create(heap, ()-> {
                             fnext.free();
                            if (fblank) ((InternalNode)node).deleteChild(null);
                            else {((InternalNode)node).deleteChild(key[d - leafPrefix.length - 1]);}
                        });
                    }
                    else if (op == Operation.END) break;
                } else if (((InternalNode)next).getChildrenCount() == 0) {
                    final int d = depth;
                    final Node fnext = next;
                    Transaction.create(heap, ()-> {
                        fnext.free();
                        ((InternalNode)node).deleteChild(key[d - 1]);
                    });
                }
                // remove prefix from node
                depth-=prefix.length;
                if (!blank)  depth--;
            }
        }
    }

    class EntryIterator implements Iterator<LongART.Entry> {
        StackItem cursor;
        byte[] lastKey = null;
        boolean lastInclusive = false;
        ByteBuffer keyBuf;
        Deque<StackItem> cache;
        Entry prev;
        Entry next;

        public EntryIterator() {
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
                    iterate(first);
                    cursor = cache.getFirst();
                    advance(); //next();
                }
            }
        }

        void buildCache(Node parent, Node child, Byte radix, Consumer<Long> cleaner) {
            StackItem item;
            if (radix == null) item = new StackItem(((InternalNode)parent).getEntries(), parent.getPrefixLength(), true);
            else item = new StackItem(((InternalNode)parent).getEntries(), parent.getPrefixLength(), radix);
            cache.addLast(item);
            byte[] ba = parent.getPrefix();
            if (ba.length > 0) keyBuf.put(ba);
            if (radix != null) keyBuf.put(radix);
            else keyBuf.position(keyBuf.position() + 1);
        }

        public EntryIterator(byte[] lastKey, boolean lastInclusive) {
            this();
            this.lastKey = lastKey;
            this.lastInclusive = lastInclusive;
        }

        public EntryIterator(byte[] firstKey, boolean firstInclusive, byte[] lastKey, boolean lastInclusive) {
            cache = new ArrayDeque<>();
            Node first = root.getChild();
            // lastKey can be null, firstkey is never null as it is checked before calling this
            this.lastKey = lastKey;
            this.lastInclusive = lastInclusive;
            if (/*firstKey != null && */lastKey != null) {
                int comp = keyCompare(firstKey, lastKey);
                if (comp == 0) {
                    long val = get(firstKey);
                    next = (val == 0) ? null : new Entry(firstKey, val);
                    return;
                }
                if (comp > 0) throw new IllegalArgumentException();
            }
            if (first != null) {
                if (first.isLeaf()) {
                    SimpleLeaf leaf = (SimpleLeaf)first;
                    int x = keyCompare(firstKey, first.getPrefix());
                    next = ((firstInclusive && x == 0) || x < 0) ? new Entry(first.getPrefix(), leaf.getValue()) : null;
                }
                else {
                    keyBuf = ByteBuffer.allocate(maxKeyLen);
                    search(first, firstKey, 0, this::buildCache, null);
                    cursor = cache.peekFirst();
                    if (cursor != null) {
                        int pos = keyBuf.position(); keyBuf.position(0); keyBuf.mark();
                        if (pos > 0) keyBuf.put(firstKey, 0, pos - 1);
                        advance(); //next();
                        if (next != null) {
                            int x = keyCompare(firstKey, next.getKey());
                            if ((!firstInclusive || x != 0) && x >= 0) {
                                next();
                                while (next != null && keyCompare(firstKey, next.getKey()) > 0) {pop(); next();}
                            }
                        }
                    } else {
                        iterate(first);
                        cursor = cache.getFirst();
                        advance(); //next();
                    }
                }
                prev = next;
            }
        }

        int keyCompare(byte[] firstKey, byte[] nextKey){
            int ret = 0;
            int i = 0;
            while (i < firstKey.length && i < nextKey.length && (ret = compareUnsigned(firstKey[i], nextKey[i])) == 0) i++;
            return (ret == 0) ? Integer.compare(firstKey.length, nextKey.length) : ret;
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
            // if (index != cursor.getIndex()) throw new RuntimeException("index is "+index+", cursor index is "+cursor.getIndex());
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
                iterate(ne.child);
                cursor = cache.getFirst();
                ne = cursor.entryAtIndex();
            }
            SimpleLeaf leaf = (SimpleLeaf)ne.child;
            keyBuf.mark();
            if (!cursor.currentIsBlank()) keyBuf.put(ne.radix);
            cursor.incrementIndex();
            byte[] leafPrefix = leaf.getPrefix();
            if (leafPrefix.length > 0) keyBuf.put(leafPrefix);
            next = new Entry(Arrays.copyOf(keyBuf.array(),keyBuf.position()),leaf.getValue());
            keyBuf.reset();
            //return prev;
        }

        void pop() {
            keyBuf.reset().position(Math.max(0, keyBuf.position() - (1 + cursor.prefixLen()))).mark();
            cache.pop();
            cursor = cache.peekFirst();
            if (cursor != null) cursor.incrementIndex(); 
            keyBuf.reset();
        }

        void iterateChildren(InternalNode current) {
            NodeEntry[] entries = current.getEntries();
            byte[] nodePrefix = current.getPrefix();
            boolean blank;
            cache.push(new StackItem(entries, nodePrefix.length, blank = current.hasBlankRadixChild()));
            if (nodePrefix.length > 0) keyBuf.put(nodePrefix);
            if (!blank && !entries[0].child.isLeaf()) keyBuf.put(entries[0].radix);
            //System.out.println("partial key: "+new String(Arrays.copyOf(keyBuf.array(),keyBuf.position()))+" "+keyBuf);
            iterate(entries[0].child);
        }

        void iterate(Node current) {
            if (!current.isLeaf()) {
                iterateChildren((InternalNode)current);
            } else {
                return;
            }
        }
    }

    /*public */class ValueIterator implements Iterator<Long> {
        StackItem cursor;
        int index;
        Deque<StackItem> cache;
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
                    iterate(first);
                    cursor = cache.getFirst();
                    next();
                    //System.out.println("Cache Size is "+cache.size()+"; cursor size is "+cursor.length+" index is "+index);
                }
            }
        }

        public boolean hasNext() {
            return (next != 0);
        }

        public Long next() {
            prev = next;
            if (cursor == null) {
                next = 0;
                return prev;
            }
            while (index >= cursor.length()) {
                if (cache.size() == 0) {
                    next = 0;
                    return prev;
                }
                //System.out.println("PreCurrentFull: Cache Size is "+cache.size()+"; cursor size is "+cursor.length+" index is "+index);
                cache.pop();
                cursor = cache.peekFirst();
                if (cursor == null) {
                    next = 0;
                    return prev;
                }
                index = cursor.getIndex()+1;
            }
            if (!cursor.entryAt(index).child.isLeaf()) {
                cursor.saveIndex(index);
                iterate(cursor.entryAt(index).child);
                cursor = cache.getFirst();
                index=0;
            }
            next = ((SimpleLeaf)cursor.entryAt(index++).child).getValue();
            return prev;
        }

        void iterateChildren(InternalNode current) {
            NodeEntry[] entries = current.getEntries();
            cache.push(new StackItem(entries, current.getPrefixLength(),current.hasBlankRadixChild()));
            if (entries != null) {
                iterate(entries[0].child);
                }
        }

        void iterate(Node current) {
            if (!current.isLeaf()) {
                iterateChildren((InternalNode)current);
            } else {
                return;
            }
        }
    }

    public static abstract class Node {
        static final byte NODE4_TYPE = 7;
        static final byte NODE16_TYPE = 1;
        static final byte NODE48_TYPE = 2;
        static final byte NODE256_TYPE = 3;
        static final byte SIMPLE_LEAF_TYPE = 4;
        static final byte COMPLEX_LEAF_TYPE = 5;
        static final byte ROOT_TYPE = 6;
        static final byte FREED = (byte)0xff;

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
            Node ret = null;
            byte rawType = mb.getByte(NODE_TYPE_OFFSET);
            switch (rawType) {
                case NODE4_TYPE: ret = new Node4(heap, mb); break;
                case NODE16_TYPE: ret = new Node16(heap, mb); break;
                case NODE48_TYPE: ret = new Node48(heap, mb); break;
                case NODE256_TYPE: ret = new Node256(heap, mb); break;
                case SIMPLE_LEAF_TYPE: ret = new SimpleLeaf(heap, mb); break;
                //case COMPLEX_LEAF_TYPE: ret = new ComplexLeaf(heap, mb); break;
                case ROOT_TYPE: ret = new Root(heap, mb); break;
                case FREED: throw new RuntimeException("Node was freed");
                default: throw new RuntimeException("Could not detect Node type" + ". found "+rawType);
            }
            if (ret == null) throw new RuntimeException();
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

        /*private void setType(byte type) {
            mb.setByte(NODE_TYPE_OFFSET, type);
        }*/

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

        abstract void destroy(Consumer<Long> cleaner); 

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

    public static class NodeEntry{
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
            return mb.getLong(Node.COMPRESSED_PATH_OFFSET);
        }

        void setCount(long count) {
            mb.setLong(Node.COMPRESSED_PATH_OFFSET, count);
        }

        void incrementCount() {
            mb.setLong(Node.COMPRESSED_PATH_OFFSET, mb.getLong(Node.COMPRESSED_PATH_OFFSET) + 1);
        }

        void decrementCount() {
            mb.setLong(Node.COMPRESSED_PATH_OFFSET, mb.getLong(Node.COMPRESSED_PATH_OFFSET) - 1);
        }

        int getMaxKeyLength() {
            return mb.getInt(Node.PREFIX_LENGTH_OFFSET);
        }

        void setMaxKeyLength(int length) {
            mb.setInt(Node.PREFIX_LENGTH_OFFSET, length);
        }

        boolean isLeaf() { return false; }

        @Override
        void destroy(Consumer<Long> cleaner) {
            Node child = getChild();
            if (child != null) {
                child.destroy(cleaner);
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
            mb.setMemory((byte)0xff, 0, SIZE);
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

        // Node256 needs to override to avoid overflowing
        boolean addBlankRadixChild(Leaf child) {
            short childrenCount = getChildrenCount();
            if (childrenCount == capacity()) {
                return false;
            }
            else {
                incChildrenCount();
                if (hasBlankRadixChild()) throw new RuntimeException();
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
        void destroy(Consumer<Long> cleaner) {
            Node child;
            for (int i=0; i<capacity(); i++) {
                if ((child = getChildAtIndex(i)) != null) {
                    child.destroy(cleaner); 
                    child.free();
                }
            }
        }

        int clearBlankRadixFlag() {
            int index = (int)getBlankRadixIndex();
            if (index != -1) mb.setByte(BLANK_RADIX_INDEX_OFFSET, (byte)0xff);
            return index;
        }

        Byte findLowestRadix(byte radix, boolean visited) {
            //integer version
            int cmp;
            int lowest;
            cmp = visited ? radix : Byte.MIN_VALUE - 1;
            lowest = Byte.MAX_VALUE + 1;
            byte[] radices = getRadices();

            for (int i = 0; i < radices.length; i++) {
                if (radices[i] > cmp && radices[i] != (byte)0 && radices[i] < lowest) lowest = radices[i];
            }
            if (lowest == Byte.MAX_VALUE + 1) {/*System.out.println("Returning null");*/ return null;} 
            return (byte)lowest;
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
                int index = findChildIndex(b);    
                putChildAtIndex(index, newChild);
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

    public static abstract class Leaf extends Node {
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

    public static class SimpleLeaf extends Leaf {
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
        void destroy(Consumer<Long> cleaner) {
            cleaner.accept(getValue());
        }
    }

    public static class Node4 extends InternalNode {
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
            /* for (int i = 0; i < entries.length; i++) {
                if (entries[i] == null) throw new RuntimeException("entry[ "+i+" ] = null! getChildrenCount returned "+entries.length+" .hasBlank is "+ (blankIndex != -1)+" count is "+(index-1));
            }*/
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
            //if (index == -1) return;
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
                //if (radices[i] >= radix) {
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
            /*if (first) {
                byte high = findHighestRadix();
                if(LongART.compareUnsigned(high, radix) > 0){
                    throw new RuntimeException("first is "+first+" radix: "+Byte.toUnsignedInt(radix)+" highest: "+Byte.toUnsignedInt(high));
                }
                if (newNode.getChildrenCount() > 0) {
                byte low = newNode.findLowestRadix();
                    if (first) {
                        if(LongART.compareUnsigned(low, radix) <= 0){
                            throw new RuntimeException("radix: "+radix+" highest: "+high);
                        }
                    }
                    else {
                        if(LongART.compareUnsigned(low, radix) != 0){
                            throw new RuntimeException("radix: "+radix+" highest: "+high);
                        }
                    }
                }
                if ((getChildrenCount() + newNode.getChildrenCount()) != pre) throw new RuntimeException("preCount: "+pre+" oldCount: "+getChildrenCount()+" newcount: "+newNode.getChildrenCount());
                getEntries();
                newNode.getEntries(); 
            }*/
            return newNode;
        }
    }

    public static class Node16 extends InternalNode {
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
            /* for (int i = 0; i < entries.length; i++) {
                if (entries[i] == null) throw new RuntimeException("entry[ "+i+" ] = null! getChildrenCount returned "+entries.length+" .hasBlank is "+ (blankIndex != -1)+" count is "+(index-1));
            }*/
            return entries;
        }

        void addRadix(byte radix, int index) {
            mb.setByte(RADIX_OFFSET + index, radix);
        }

        @Override
        boolean addChild(byte radix, Node node) {
            int index = findChildIndex(radix);
            //boolean found = true;
            if (index == -1) {
                //found = false;
                if (getChildrenCount() >= MAX_CAPACITY) {
                    return false;   // need to grow, out of capacity
                } else {
                    index = getChildrenCount();
                    incChildrenCount();
                    addRadix(radix, index);
                }
            }
            putChildAtIndex(index, node);
            /*byte[] radices = getRadices();
            Arrays.sort(radices);
            int start = hasBlankRadixChild() ? 2 : 1;
            for (int i = start; i < radices.length; i++) {
                if (Byte.compare(radices[i-1], radices[i]) == 0) throw new RuntimeException("found is "+found+" radix is "+radix+", duplicate radix is "+radices[i]);
            }*/
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
            // int pre = getChildrenCount();
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
            /* if (first) {
                byte high = findHighestRadix();
                if(LongART.compareUnsigned(high, radix) > 0){
                    throw new RuntimeException("first is "+first+" radix: "+Byte.toUnsignedInt(radix)+" highest: "+Byte.toUnsignedInt(high));
                }
                if (newNode.getChildrenCount() > 0) {
                byte low = newNode.findLowestRadix();
                    if (first) {
                        if(LongART.compareUnsigned(low, radix) < 0){
                            throw new RuntimeException("radix: "+Byte.toUnsignedInt(radix)+" highest: "+Byte.toUnsignedInt(high));
                        }
                    }
                    else {
                        if(LongART.compareUnsigned(low, radix) != 0){
                            throw new RuntimeException("radix: "+radix+" highest: "+high);
                        }
                    }
                }
                if (!first) pre++;
                if ((getChildrenCount() + newNode.getChildrenCount()) != pre) throw new RuntimeException("preCount: "+pre+" oldCount: "+getChildrenCount()+" newcount: "+newNode.getChildrenCount());
                getEntries();
                newNode.getEntries(); 
            }*/
            return newNode;
        }
    }

    public static class Node48 extends InternalNode {
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
                // int count =0;
                StringBuffer radices = new StringBuffer("radics: ");
                for (int index = 0; index < oldRadices.length; index++) {
                    // Skip the blank child radix, otherwise it will map the 0th
                    // index to the blank child when it's not supposed to
                    if (index != blankRadixIndex) {
                        range.setByte(RADIX_OFFSET + Byte.toUnsignedInt(oldRadices[index]), (byte)(index + 1));
                        radices.append(" "+oldRadices[index]);
                        // count++;
                    }
                        //range.setByte(RADIX_OFFSET + (int)oldRadices[index] + 128, (byte)(index + 1));
                }
                    // System.out.println("found "+count+ " radices");
                    // System.out.println(radices);
                range.copyFromMemoryBlock(oldNode.mb, Node16.CHILDREN_OFFSET, Node48.CHILDREN_OFFSET, oldNode.capacity() * Long.BYTES);
                /*for (int i =0; i<16; i++) {
                    if (oldNode.mb.getLong(Node16.CHILDREN_OFFSET + (i * Long.BYTES)) == 0L) throw new RuntimeException("found null at index" +i);
                }*/
                //set radix
                //if (radix.isPresent()) range.setByte(RADIX_OFFSET + (int)radix.get() + 128, (byte)(16 + 1));
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
            //mb.setByte(RADIX_OFFSET + index + 128, radix);
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
            // StringBuffer sb = new StringBuffer("entries: ");
            // StringBuffer reject = new StringBuffer("rejected entries: ");
            for (int i=0; i < MAX_RADICES; i++) {
                if (radices[(int)i] != (1 + blankIndex) && radices[(int)i] !=0) {
                    entries[index++] = new NodeEntry((byte)(i), getChildAtIndex(radices[i]-1));
                    // sb.append("("+i+", "+radices[(int)i]+"); ");
                }
                //else reject.append("("+i+", "+radices[(int)i]+"); ");
            }
            /*for (int i = 0; i < entries.length; i++) {
                if (entries[i] == null) {
                    //System.out.println(sb); 
                    //System.out.println(reject); 
                    throw new RuntimeException("entry[ "+i+" ] = null! getChildrenCount returned "+entries.length+" .hasBlank is "+ hasBlank +" blankIndex is "+blankIndex+" count is "+(index-1));}
            }*/
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
                    //addRadix((byte)(index + 1), (int)radix);   // +1 for 1-based indices
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
                        //range.setByte(RADIX_OFFSET + (int)radix + 128, (byte)0);
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
        
        @Override
        Byte findLowestRadix(byte radix, boolean visited) {
            int cmp = visited ? radix : Byte.MIN_VALUE;
            int lowest = Byte.MAX_VALUE;
            for (int i=cmp + 1; i<=lowest; i++) { // TODO check for blank
                if (findChildIndex((byte)i) != -1) {
                    return (byte)i;
                }
            }
            return null;   
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
            // int pre = getChildrenCount();
            /*StringBuffer buf = new StringBuffer("Pre : children count is "+getChildrenCount()+"\n");
            for (int i=0; i<radices.length; i++) {
                if (radices[i] != 0) buf.append("radices["+i+"] = "+((int)radices[i] - 1)+ "\n");
            }
            StringBuffer buf2 = new StringBuffer("Pre Children, count is "+getChildrenCount()+": \n");
            for (int i = 0; i<MAX_CAPACITY; i++) {
                long l = findValueAtIndex(i);
                if (l != 0) buf2.append("index "+i+" = "+findValueAtIndex(i)+"\n");
            }*/
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
            /*this.radices = null;
            radices = getRadices();
            buf.append("Post : \n");
            for (int i=0; i<radices.length; i++) {
                if (radices[i] != 0) buf.append("radices["+i+"] = "+((int)radices[i] - 1)+ "\n");
            }
            newNode.radices = null;
            radices = newNode.getRadices();
            buf.append("New : \n");
            for (int i=0; i<radices.length; i++) {
                if (radices[i] != 0) buf.append("radices["+i+"] = "+((int)radices[i] - 1)+ "\n");
            }*/
            newNode.radices = null;
            this.radices = null;
            /* if (first) {
                byte high = findHighestRadix();
                if(LongART.compareUnsigned(high, radix) > 0){
                    throw new RuntimeException("first is "+first+" radix: "+Byte.toUnsignedInt(radix)+" highest: "+Byte.toUnsignedInt(high));
                }
                if (newNode.getChildrenCount() > 0) {
                byte low = newNode.findLowestRadix();
                    if (first) {
                        if(LongART.compareUnsigned(low, radix) < 0){
                            throw new RuntimeException("radix: "+radix+"old.highest: "+high+" new.lowest: "+low);
                        }
                        if(LongART.compareUnsigned(high, radix) > 0){
                            throw new RuntimeException("radix: "+radix+"old.highest: "+high+" new.lowest: "+low);
                        }
                        if ((getChildrenCount() + newNode.getChildrenCount()) != pre) throw new RuntimeException("preCount: "+pre+" oldCount: "+getChildrenCount()+" newcount: "+newNode.getChildrenCount() + " highest: "+high+" lowest: "+low);
                    }
                    else {
                        if(LongART.compareUnsigned(low, radix) != 0){
                            throw new RuntimeException("radix: "+radix+"old.highest: "+high+" new.lowest: "+low);
                        }
                        if(LongART.compareUnsigned(high, radix) != 0){
                            throw new RuntimeException("radix: "+radix+" old.highest: "+high+" new.lowest: "+low);
                        }
                        if ((getChildrenCount() + newNode.getChildrenCount()) != (1 + pre)) throw new RuntimeException("preCount: "+pre+" oldCount: "+getChildrenCount()+" newcount: "+newNode.getChildrenCount()+" highest: "+high+" lowest: "+low);
                    }
                }
                getEntries();
                newNode.getEntries();
             }*/
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

    public static class Node256 extends InternalNode {
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
                //if (radix.isPresent()) range.setLong(CHILDREN_OFFSET + ((int)radix.get() + 128) * Long.BYTES, newNode.handle());
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
    
        /*@Override
        int clearBlankRadixFlag() {
            throw new RuntimeException();
        }*/

        @Override
        Leaf findBlankRadixChild() {
            long val = findValueAtIndex(BLANK_RADIX_CHILD_INDEX);
            return (val == 0) ? null : (Leaf)Node.rebuild(heap, val);
        }

        @Override
        boolean addBlankRadixChild(Leaf child) {
            if (hasBlankRadixChild()) {
                findBlankRadixChild().setValue(child.getValue());
                // TODO: free incoming child
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
            /*for (int i = 128; i < 256; i++) {
                if ((val = findValueAtIndex(i)) != 0) entries[index++] = new NodeEntry((byte)(i - 128), Node.rebuild(heap, val)); 
            }
            for (int i = 0; i < 128; i++) {
                if ((val = findValueAtIndex(i)) != 0) entries[index++] = new NodeEntry((byte)(i - 128), Node.rebuild(heap, val)); 
            }*/
            for (int i = 0; i < 256; i++) {
                if ((val = findValueAtIndex(i)) != 0) entries[index++] = new NodeEntry((byte)i, Node.rebuild(heap, val)); 
            }
            /*for (int i = 0; i < entries.length; i++) {
                if (entries[i] == null) throw new RuntimeException("entry[ "+i+" ] = null! getChildrenCount returned "+entries.length+" .hasBlank is "+ !(blankChild == null)+" count is "+(index-1));
            }*/
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

        @Override
        Byte findLowestRadix(byte radix, boolean visited) {
            int cmp = visited ? radix : Byte.MIN_VALUE;
            int lowest = Byte.MAX_VALUE;
            for (int i=cmp + 1; i<=lowest; i++) {
                if (findValueAtIndex(i + 128) != 0L) {
                    return (byte)i;
                }
            }
            return null;   
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
            //if (child.handle() == 0) throw new RuntimeException();;
            mb.setLong(CHILDREN_OFFSET + index * Long.BYTES, child.handle());
        }

        @Override
        protected short capacity() { return (short)MAX_CAPACITY; }

        @Override
        InternalNode grow(Node child, Optional<Byte> radix) { return null; }

        InternalNode split(Byte radix, boolean first) {
            Node256 newNode = new Node256(heap);
            //int pre = getChildrenCount();
            // boolean oldblank = hasBlankRadixChild();
            if (radix == null) {
                /*newNode.mb.withRange(0, this.SIZE, (Range rng) -> {
                    rng.copyFromMemoryBlock(mb, CHILDREN_OFFSET, CHILDREN_OFFSET, (MAX_CAPACITY - 1) * Long.BYTES);
                    rng.setShort(CHILDREN_COUNT_OFFSET, (short)(getChildrenCount() - 1)); 
                });                
                mb.withRange(0, this.SIZE, (Range rng) -> {
                    rng.setMemory((byte)0, CHILDREN_OFFSET, (MAX_CAPACITY - 1) * Long.SIZE);
                    rng.setShort(CHILDREN_COUNT_OFFSET, (short)1);
                });*/
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
            /* if (first) {
                byte high = findHighestRadix();
                if(LongART.compareUnsigned(high, radix) > 0){
                    throw new RuntimeException("first is "+first+" radix: "+Byte.toUnsignedInt(radix)+" highest: "+Byte.toUnsignedInt(high));
                }
                if (newNode.getChildrenCount() > 0) {
                byte low = newNode.findLowestRadix();
                    if (first) {
                        if(LongART.compareUnsigned(low, radix) < 0){
                            throw new RuntimeException("first is true, radix: "+Byte.toUnsignedInt(radix)+" lowest: "+Byte.toUnsignedInt(high)+" highest is "+Byte.toUnsignedInt(high));
                        }
                    }
                    else {
                        if(LongART.compareUnsigned(low, radix) != 0){
                            throw new RuntimeException("first if false, radix: "+Byte.toUnsignedInt(radix)+" lowest: "+Byte.toUnsignedInt(high));
                        }
                    }
                }
                if (!first) pre++;
                if ((getChildrenCount() + newNode.getChildrenCount()) != pre) throw new RuntimeException("preCount: "+pre+" oldCount: "+getChildrenCount()+" newcount: "+newNode.getChildrenCount());
                getEntries();
                newNode.getEntries();
             }*/
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

