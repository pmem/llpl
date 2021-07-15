/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.linkedlist;

import com.intel.pmem.llpl.*;

public class Employee {
    private static final int ID_OFFSET = 0;
    private static final int NAME_LENGTH_OFFSET = 8; 
    private static final int NAME_OFFSET = 12;
    private TransactionalMemoryBlock block;
    
    public Employee(TransactionalHeap heap, String name, long id) {
        byte[] nameBytes = name.getBytes();
        block = heap.allocateMemoryBlock(NAME_OFFSET + nameBytes.length); 
        block.setLong(ID_OFFSET, id);
        block.setInt(NAME_LENGTH_OFFSET, nameBytes.length);
        block.copyFromArray(nameBytes, 0, NAME_OFFSET, nameBytes.length);
    }
   
    public static Employee fromHandle(TransactionalHeap heap, long handle) {
        return new Employee(heap.memoryBlockFromHandle(handle));
    }
    
    private Employee(TransactionalMemoryBlock block) {
        this.block =  block;
    }

    public String getName() {
        int nameLength = block.getInt(NAME_LENGTH_OFFSET);
        byte[] bytes = new byte[nameLength];
        block.copyToArray(NAME_OFFSET, bytes, 0, nameLength);
        return new String(bytes);
    }
    
    public long getId() {
        return block.getLong(ID_OFFSET);
    }
    
    public long handle() {
        return block.handle();
    }

    public void free() {
        block.free();    
    }
}
