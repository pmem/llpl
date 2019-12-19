/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;
import java.util.Optional;

public class Node256 extends InternalNode {
    // 257 for Node256: 1 more slot for the blank radix child
    protected static final long SIZE = Node.HEADER_SIZE + 257L * 8L;
    private static final long CHILDREN_OFFSET = Node.HEADER_SIZE;
    private static final int  BLANK_RADIX_CHILD_INDEX = 256;
    private static final int  MAX_CAPACITY = 257;

    Node256(TransactionalHeap heap, TransactionalCompactMemoryBlock mb) {
        super(heap, mb);
    }

    Node256(TransactionalHeap heap, Node48 oldNode, Node newNode, Optional<Byte> radix) {
        super(heap, SIZE, (Range range) -> {
            // offset is 1 to skip the TYPE field that's already set
            range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE256_TYPE);
            range.copyFromMemoryBlock(oldNode.mb, 1, 1, Node.HEADER_SIZE - 1);
            byte[] oldRadices = oldNode.getRadices();
            for (int index = 0; index < oldRadices.length; index++) {
                if (oldRadices[index] != 0) {
                    range.setLong(CHILDREN_OFFSET + index * Long.BYTES, oldNode.findValueAtIndex((int)(oldRadices[index] - 1)));
                }
            }
            if (oldNode.hasBlankRadixChild()) {
                range.setLong(CHILDREN_OFFSET + BLANK_RADIX_CHILD_INDEX * Long.BYTES, oldNode.findValueAtIndex(oldNode.getBlankRadixIndex()));
            }
            if (radix.isPresent()) range.setLong(CHILDREN_OFFSET + ((int)radix.get() + 128) * Long.BYTES, newNode.handle());
            else range.setLong(CHILDREN_OFFSET + BLANK_RADIX_CHILD_INDEX * Long.BYTES, newNode.handle());
            range.setShort(InternalNode.CHILDREN_COUNT_OFFSET, (short)(48 + 1));
        });
    }

    @Override
    boolean hasBlankRadixChild() {
        return (findValueAtIndex(BLANK_RADIX_CHILD_INDEX) != 0);
    }

    @Override
    boolean addBlankRadixChild(Leaf child) {
        if (hasBlankRadixChild()) {
            findBlankRadixChild().setValue(child.getValue());
        } 
        else {
            putChildAtIndex(BLANK_RADIX_CHILD_INDEX, child);
        }
        return true;
    }

    @Override
    byte[] getRadices() {
        return null;    // does not store radices
    }

    @Override
    NodeEntry[] getEntries() {
        NodeEntry[] entries = new NodeEntry[getChildrenCount()];
        int index=0;
        boolean hasBlank = hasBlankRadixChild();
        if (hasBlank) {
            entries[index++] = new NodeEntry((byte)0, getChildAtIndex(BLANK_RADIX_CHILD_INDEX));
        }
        Node n;
        for (int i=128; i < 256; i++) {
            if (findValueAtIndex(i) != 0) entries[index++] = new NodeEntry((byte)(i-128), getChildAtIndex(i));
        }
        for (int i=0; i < 128; i++) {
            if (findValueAtIndex(i) != 0) entries[index++] = new NodeEntry((byte)(i-128), getChildAtIndex(i));
        }
        return entries;
    }

    @Override
    boolean addChild(byte radix, Node node) {
        int index = findChildIndex(radix);
        if (findValueAtIndex(index) == 0) {
            incChildrenCount();
        }
        putChildAtIndex(index, node);
        return true;
    }

    void deleteChild(Byte radix) {
        int index;
        if (radix == null) index = BLANK_RADIX_CHILD_INDEX;
        else index = findChildIndex(radix);
        if (findValueAtIndex(index) != 0) {
            mb.setLong(CHILDREN_OFFSET + index * Long.BYTES, 0L);
            decChildrenCount();
        }
    }
    
    @Override
    int findChildIndex(byte radix) {
        return (int)radix + 128;
    }

    @Override
    long findValueAtIndex(int index) {
        if (index == -1) return 0;
        return mb.getLong(CHILDREN_OFFSET + index * Long.BYTES);
    }

    @Override
    void putChildAtIndex(int index, Node child) {
        if (index == -1) return;
        mb.setLong(CHILDREN_OFFSET + index * Long.BYTES, child.handle());
    }

    @Override
    short capacity() {return (short)MAX_CAPACITY;}

    @Override
    InternalNode grow(Node child, Optional<Byte> radix) {return null;}

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
