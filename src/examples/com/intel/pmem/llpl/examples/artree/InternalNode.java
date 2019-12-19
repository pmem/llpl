/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;
import java.util.Optional;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public abstract class InternalNode extends Node {
    InternalNode(TransactionalHeap heap, TransactionalCompactMemoryBlock mb) {
        super(heap, mb);
    }

    InternalNode(TransactionalHeap heap, long size, Consumer<Range> initializer) {
        super(heap, heap.allocateCompactMemoryBlock(size, (Range range) -> {
            range.setByte(BLANK_RADIX_INDEX_OFFSET, (byte)0xff);
            if (initializer == null) throw new IllegalArgumentException("Initializer is null.");
            initializer.accept(range);
        }));
    }

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

    void setChildrenCount(short count) {
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

    Node findChild(byte radix) {
        return getChildAtIndex(findChildIndex(radix));
    }

    Node getChildAtIndex(int index) {
        return Node.rebuild(heap, findValueAtIndex(index));
    }
    
    void destroy() {
        Node child;
        for (int i=0; i<capacity(); i++) {
            if ((child = getChildAtIndex(i)) != null) {
                child.free();
            }
        }
    }

    int clearBlankRadixFlag() {
        int index = (int)getBlankRadixIndex();
        if (index != -1) mb.setByte(BLANK_RADIX_INDEX_OFFSET, (byte)0xff);
        return index;
    }

    abstract short capacity();
    abstract byte[] getRadices();
    abstract boolean addChild(byte radix, Node node);
    abstract int findChildIndex(byte radix);
    abstract long findValueAtIndex(int index);
    abstract void putChildAtIndex(int index, Node child);
    abstract InternalNode grow(Node child, Optional<Byte> radix);
    abstract NodeEntry[] getEntries();
    abstract void deleteChild(Byte radix);

    boolean isLeaf() {
        return false;
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
            } 
            else {}
        }
    }
}
