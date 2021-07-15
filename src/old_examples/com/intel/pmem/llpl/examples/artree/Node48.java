/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;
import java.util.Optional;

public class Node48 extends InternalNode {
    private static final int  MAX_CAPACITY = 48;
    private static final int  MAX_RADICES = 256;
    protected static final long SIZE = Node.HEADER_SIZE + 1L * (long)MAX_RADICES + (long)(Long.BYTES * MAX_CAPACITY);
    private static final long RADIX_OFFSET = Node.HEADER_SIZE;
    static final long CHILDREN_OFFSET = Node.HEADER_SIZE + 256L;

    // Special design to handle zero being both init value and a valid index:
    // the indices to the children will be 1-based, so an index of 0 is invalid
    private byte[] radices;

    Node48(TransactionalHeap heap, TransactionalCompactMemoryBlock mb) {
        super(heap, mb);
    }

    Node48(TransactionalHeap heap, Node16 oldNode, Node newNode, Optional<Byte> radix) {
        super(heap, SIZE, (Range range) -> {
            range.setByte(Node.NODE_TYPE_OFFSET, Node.NODE48_TYPE);
            range.copyFromMemoryBlock(oldNode.mb, 1, 1, Node.HEADER_SIZE - 1);
            int blankRadixIndex = oldNode.getBlankRadixIndex();
            byte[] oldRadices = oldNode.getRadices();
            for (int index = 0; index < oldRadices.length; index++) {
                if (index != blankRadixIndex)
                    range.setByte(RADIX_OFFSET + (int)oldRadices[index] + 128, (byte)(index + 1));
            }
            range.copyFromMemoryBlock(oldNode.mb, Node16.CHILDREN_OFFSET, Node48.CHILDREN_OFFSET, oldNode.capacity() * Long.BYTES);
            if (radix.isPresent()) range.setByte(RADIX_OFFSET + (int)radix.get() + 128, (byte)(16 + 1));
            else range.setByte(Node.BLANK_RADIX_INDEX_OFFSET, (byte)16);
            range.setLong(CHILDREN_OFFSET + 16 * Long.BYTES, newNode.handle());
            range.setShort(InternalNode.CHILDREN_COUNT_OFFSET, (short)(16 + 1));
        });
    }

    @Override
    byte[] getRadices() {
        if (radices == null) {
            byte[] radices = new byte[MAX_RADICES];
            mb.copyToArray(RADIX_OFFSET, radices, 0, MAX_RADICES);
            this.radices = radices;
        }
        return this.radices;
    }

    void addRadix(byte radix, int index) {
        mb.setByte(RADIX_OFFSET + index + 128, radix);
    }

    @Override
    NodeEntry[] getEntries() {
        byte[] radices = getRadices();
        NodeEntry[] entries = new NodeEntry[getChildrenCount()];
        int blankIndex=-1;
        int index=0;
        boolean hasBlank = hasBlankRadixChild();
        if (hasBlank) {
            entries[index++] = new NodeEntry((byte)0, getChildAtIndex(blankIndex=getBlankRadixIndex()));
        }
        for (int i=128; i < MAX_RADICES; i++) {
            if (radices[(int)i] != blankIndex && radices[(int)i] !=0) entries[index++] = new NodeEntry((byte)(i-128), getChildAtIndex(radices[i]-1));
        }
        for (int i=0; i < (MAX_RADICES / 2); i++) {
            if (radices[(int)i] != blankIndex && radices[(int)i] !=0) entries[index++] = new NodeEntry((byte)(i-128), getChildAtIndex(radices[i]-1));
        }
        return entries;
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
                // For Node48, the radix field is an index to the child,
                // and the radix itself is used a the index into the radix field
                addRadix((byte)(index + 1), (int)radix);   // +1 for 1-based indices
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
            mb.withRange(0, this.SIZE, (Range range) ->{
                if (radix != null) {
                    range.setByte(RADIX_OFFSET + (int)radix + 128, (byte)0);
                }
                decChildrenCount();
                int childrenCount = getChildrenCount();
                if (childrenCount == index) {
                    range.setLong(CHILDREN_OFFSET + childrenCount * Long.BYTES, 0L);
                } 
                else {
                    range.setLong(CHILDREN_OFFSET + index * Long.BYTES, mb.getLong(CHILDREN_OFFSET + childrenCount * Long.BYTES));
                    range.setLong(CHILDREN_OFFSET + childrenCount * Long.BYTES, 0L);
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
    int findChildIndex(byte radix) {
        byte[] radices = getRadices();
        if (radices[(int)radix + 128] != 0) return (int)(radices[(int)radix + 128]) - 1;   // 1-based
        else return -1;
    }

    @Override
    long findValueAtIndex(int index) {
        if (index == -1) return 0;
        return mb.getLong(CHILDREN_OFFSET + Long.BYTES * index);
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
        return new Node256(heap, this, child, radix);
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
