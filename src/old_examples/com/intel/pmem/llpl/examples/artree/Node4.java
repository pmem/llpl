/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;
import java.util.Optional;
import java.util.Arrays;

public class Node4 extends InternalNode {
    protected static final long SIZE = Node.HEADER_SIZE + 4L * (1L + 8L);
    static final long RADIX_OFFSET = Node.HEADER_SIZE + (4L * 8L);
    static final long CHILDREN_OFFSET = Node.HEADER_SIZE;
    private static final int  MAX_CAPACITY = 4;

    Node4(TransactionalHeap heap) {
        super(heap, SIZE, (Range range) -> {
            range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE4_TYPE);
        });
    }

    Node4(TransactionalHeap heap, byte[] prefix, int start, int prefixLen, Node child, byte radix) {
        super(heap, SIZE, (Range range) -> {
            range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE4_TYPE);
            if (prefixLen > 0) {
                if (prefixLen > 8) throw new IllegalArgumentException("Prefix more than 8 bytes");
                range.setInt(Node.PREFIX_LENGTH_OFFSET, prefixLen);
                range.copyFromArray(prefix, start, Node.COMPRESSED_PATH_OFFSET, prefixLen);
            }
            range.setByte(RADIX_OFFSET, radix);
            range.setLong(CHILDREN_OFFSET, child.handle());
            range.setShort(Node.CHILDREN_COUNT_OFFSET, (short)1);
        });
    }

    Node4(TransactionalHeap heap, byte[] prefix, int prefixLen, boolean blank, Node child1, byte radix1, Node child2, byte radix2) {
        super(heap, SIZE, (Range range) -> {
            range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE4_TYPE);
            if (prefixLen > 0) {
                if (prefixLen > 8) throw new IllegalArgumentException("Prefix more than 8 bytes");
                range.setInt(Node.PREFIX_LENGTH_OFFSET, prefixLen);
                range.copyFromArray(prefix, 0, Node.COMPRESSED_PATH_OFFSET, prefixLen);
            }
            if (blank) range.setByte(Node.BLANK_RADIX_INDEX_OFFSET, (byte)0);
            else range.setByte(RADIX_OFFSET, radix1);
            range.setByte(RADIX_OFFSET + 1, radix2);
            range.setLong(CHILDREN_OFFSET, child1.handle());
            range.setLong(CHILDREN_OFFSET + 1 * Long.BYTES, child2.handle());
            range.setShort(Node.CHILDREN_COUNT_OFFSET, (short)2);
        });
    }

    Node4(TransactionalHeap heap, TransactionalCompactMemoryBlock mb) {
        super(heap, mb);
    }

    @Override
    byte[] getRadices() {
        byte[] ret = new byte[getChildrenCount()];
        mb.copyToArray(RADIX_OFFSET, ret, 0, ret.length);
        return ret;
    }

    NodeEntry[] getEntries() {
        byte[] radices = getRadices();
        NodeEntry[] entries = new NodeEntry[getChildrenCount()];
        int blankIndex=-1;
        int index=0;
        boolean hasBlank = hasBlankRadixChild();
        if (hasBlank) {
            entries[index++] = new NodeEntry((byte)0, (Leaf)getChildAtIndex(blankIndex=getBlankRadixIndex()));
        }
        for (int i=0; i < entries.length; i++) {
            if (i != blankIndex) entries[index++] = new NodeEntry(radices[i], getChildAtIndex(i));
        }
        Arrays.sort(entries, (hasBlank) ? 1 : 0, entries.length, (x, y)-> Integer.compareUnsigned(Byte.toUnsignedInt(x.radix), Byte.toUnsignedInt(y.radix)));
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
            } 
            else {
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
 
    @Override
    int findChildIndex(byte radix) {
        byte[] radices = getRadices();
        for (int i = 0; i < getChildrenCount(); i++) {
            if (radices[i] == radix)
                return i;
        }
        return -1;
    }

    @Override
    long findValueAtIndex(int index) {
        if (index == -1) return 0;  // 0 == NULL
        return mb.getLong(CHILDREN_OFFSET + index * 8);
    }

    @Override
    void putChildAtIndex(int index, Node child) {
        if (index == -1) return;
        mb.setLong(CHILDREN_OFFSET + index * Long.BYTES, child.handle());
    }

    @Override
    short capacity() {return (short)MAX_CAPACITY;}

    @Override
    InternalNode grow(Node child, Optional<Byte> radix) {
        return new Node16(heap, this, child, radix);
    }

    /*static String format(byte[] arr) {
        StringBuffer sb = new StringBuffer("[ ");
        for (int i=0; i<arr.length; i++) {
            sb.append(arr[i] + " " );
        }
        sb.append("]");
        return sb.toString();
    }*/
}
