/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.handlearray;

import com.intel.pmem.llpl.*;

public class Continent {
    private static final int NAME_OFFSET = 0; 
    private static final int AREA_OFFSET = 16;
    private static final int LANDMASS_OFFSET = 20;
    private static final int POPULATION_OFFSET = 28;
    private static final int CITY_OFFSET = 36;
    private static final int SIZE = 56;
    private TransactionalMemoryBlock block;
    
    public Continent(TransactionalHeap heap, String name, int area, double landmass, long population, String largest_city) {
        block = heap.allocateMemoryBlock(SIZE); 
        block.copyFromArray(name.getBytes(), 0, NAME_OFFSET, name.length());
        block.setInt(AREA_OFFSET, area);
        block.setLong(LANDMASS_OFFSET, Double.doubleToLongBits(landmass));
        block.setLong(POPULATION_OFFSET, population);
        block.copyFromArray(largest_city.getBytes(), 0, CITY_OFFSET, largest_city.length());
    }
   
    public static Continent fromHandle(TransactionalHeap heap, long handle) {
        return new Continent(heap.memoryBlockFromHandle(handle));
    }
    
    private Continent (TransactionalMemoryBlock block) {
        this.block =  block;
    }

    public String getName() {
        byte[] bytes = new byte[16];
        block.copyToArray(NAME_OFFSET, bytes, 0, bytes.length);
        return new String(bytes).trim();
    }
    
    public int getArea() {
        return block.getInt(AREA_OFFSET);
    }
    
    public double getLandMass() {
        return Double.longBitsToDouble(block.getLong(LANDMASS_OFFSET));
    }

    public long getPopulation() {
        return block.getLong(POPULATION_OFFSET);    
    }

    public String getLargestCity() {
        byte[] bytes = new byte[20];
        block.copyToArray(CITY_OFFSET, bytes, 0, bytes.length);
        return new String(bytes).trim();
    }

    public long handle() {
        return block.handle();
    }

    public void free() {
        block.free();
    }
}
