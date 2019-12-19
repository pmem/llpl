/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;
import java.nio.ByteBuffer;
import java.util.Arrays;
import com.intel.pmem.llpl.*;

public abstract class Node {
    static final byte NODE4_TYPE = 0;
    static final byte NODE16_TYPE = 1;
    static final byte NODE48_TYPE = 2;
    static final byte NODE256_TYPE = 3;
    static final byte SIMPLE_LEAF_TYPE = 4;
    static final byte COMPLEX_LEAF_TYPE = 5;
    static final byte ROOT_TYPE = 6;

    protected static final long HEADER_SIZE = 16L;
    protected static final long NODE_TYPE_OFFSET = 0L;
    protected static final long BLANK_RADIX_INDEX_OFFSET = 1L;
    protected static final long CHILDREN_COUNT_OFFSET = 2L;
    protected static final long PREFIX_LENGTH_OFFSET = 4L;
    protected static final long COMPRESSED_PATH_OFFSET = 8L;
    static final int MAX_PREFIX_LENGTH = 8;

    TransactionalHeap heap;
    TransactionalCompactMemoryBlock mb;

    Node(TransactionalHeap heap, long size) {
        this.heap = heap;
        this.mb = this.heap.allocateCompactMemoryBlock(size);
    }

    Node(TransactionalHeap heap, TransactionalCompactMemoryBlock mb) {
        this.heap = heap;
        this.mb = mb;
    }

    static Node rebuild(TransactionalHeap heap, long handle) {
        if (handle == 0) return null;
        TransactionalCompactMemoryBlock mb = heap.compactMemoryBlockFromHandle(handle);
        Node ret = null;
        byte rawType = mb.getByte(NODE_TYPE_OFFSET);
        switch (rawType) {
            case NODE4_TYPE: ret = new Node4(heap, mb); break;
            case NODE16_TYPE: ret = new Node16(heap, mb); break;
            case NODE48_TYPE: ret = new Node48(heap, mb); break;
            case NODE256_TYPE: ret = new Node256(heap, mb); break;
            case SIMPLE_LEAF_TYPE: ret = new SimpleLeaf(heap, mb); break;
            case ROOT_TYPE: ret = new Root(heap, mb); break;
            default: break;
        }
        return ret;
    }

    void free() {
        mb.free();
    }

    long handle() {
        return mb.handle();
    }

    byte getType() {
        return mb.getByte(NODE_TYPE_OFFSET);
    }

    void initType(byte type) {
        mb.setByte(NODE_TYPE_OFFSET, type);
    }

    void setType(byte type) {
        mb.setByte(NODE_TYPE_OFFSET, type);
    }

    int getPrefixLength() {
        return mb.getInt(PREFIX_LENGTH_OFFSET);
    }

    void setPrefixLength(int length) {
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
        } 
        else {
            throw new IllegalArgumentException("Prefix more than 8 bytes");
        }
    }

    void setPrefix(byte[] prefix) {
        if (prefix.length <= 8) {
            mb.copyFromArray(prefix, 0, COMPRESSED_PATH_OFFSET, prefix.length);
        } 
        else {
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
        } 
        else {
            SimpleLeaf leaf = (SimpleLeaf)this;
            System.out.println(start + "Value: " + Long.toHexString(leaf.getValue()));
        }
    }
}
