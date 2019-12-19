/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;

public class Planet {
    private static final int TYPE_OFFSET = 0;
    private static final int RADIUS_OFFSET = 1;
    private static final int DISTANCE_OFFSET = 9;
    private static final int YEAR_LENGTH_OFFSET = 18;
    private static final int NUM_MOONS_OFFSET = 22;
    private static final int NAME_SIZE_OFFSET = 26;
    private static final int NAME_OFFSET = 30;

    private TransactionalMemoryBlock block;
    
    public Planet(TransactionalHeap heap, String name, Type type, double radius, long distance, int year_len, int moons) {
        this.block = heap.allocateMemoryBlock(NAME_OFFSET + name.length()); 
        block.setInt(NAME_SIZE_OFFSET, name.length());
        block.copyFromArray(name.getBytes(), 0, NAME_OFFSET, name.length());
        setType(block, type);
        block.setLong(RADIUS_OFFSET, Double.doubleToLongBits(radius));
        block.setLong(DISTANCE_OFFSET, distance);
        block.setInt(YEAR_LENGTH_OFFSET, year_len);
        block.setInt(NUM_MOONS_OFFSET, moons);
    }
   
    public enum Type {
        TERRESTRIAL, GAS_GIANT, ICE_GIANT;
    }

    public static Planet fromHandle(TransactionalHeap heap, long handle) {
        return new Planet(heap.memoryBlockFromHandle(handle));
    }
    
    private Planet (TransactionalMemoryBlock block) {
        this.block =  block;
    }

    public String getName() {
        byte[] bytes = new byte[block.getInt(NAME_SIZE_OFFSET)];
        block.copyToArray(NAME_OFFSET, bytes, 0, bytes.length);
        return new String(bytes);
    }
    
    public String getType() {
        byte type = block.getByte(TYPE_OFFSET);
        switch (type) {
            case (byte)1:
                return "Terrestrial";
            case (byte)2:
                return "Gas Giant";
            case (byte)3:
                return "Ice Giant";
        } 
        return null;
    }
    
    private void setType(TransactionalMemoryBlock block, Type type) {
        switch (type) {
            case TERRESTRIAL:
                block.setByte(TYPE_OFFSET, (byte)1);
                break;
            case GAS_GIANT:
                block.setByte(TYPE_OFFSET, (byte)2);
                break;
            case ICE_GIANT:
                block.setByte(TYPE_OFFSET, (byte)3);
                break;
        } 
    }

    public double getRadius() {
        return Double.longBitsToDouble(block.getLong(RADIUS_OFFSET));
    }
    
    public long getDistance() {
        return block.getLong(DISTANCE_OFFSET);
    }

    public long getYearLength() {
        return block.getInt(YEAR_LENGTH_OFFSET);    
    }

    public long getMoons() {
        return block.getInt(NUM_MOONS_OFFSET);    
    }

    public void print() {
        System.out.println(String.format(" %-10s|%12s |%8.1f | %,14d |%8d |%4d | \n", getName(), getType(), getRadius(), getDistance(), getYearLength(), getMoons()));
    }

    public void print2() {
        StringBuffer sb = new StringBuffer(); 
        sb.append(String.format("%-17s%s\n", "Planet:", getName()));
        sb.append(String.format("%-17s%s\n", "Type:", getType()));
        sb.append(String.format("%-17s%,.1f miles\n", "Radius:", getRadius()));
        sb.append(String.format("%-17s%,d miles to the Sun\n", "Distance:", getDistance()));
        sb.append(String.format("%-17s%,d Earth Days\n", "Length of Year:", getYearLength()));
        if (getMoons() > 0) {
            sb.append(String.format("%-17s%d\n", "Number of Moons:", getMoons()));
        }
        System.out.println(sb);
    }

    public long handle() {
        return block.handle();
    }

    public void free() {
        block.free();
    }
}
