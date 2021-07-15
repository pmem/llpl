/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl.examples.longarray;

import com.intel.pmem.llpl.TransactionalHeap;
import com.intel.pmem.llpl.Transaction;
import com.intel.pmem.llpl.util.LongArray;

public class LongArrayExample {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/LongArray_example";
        TransactionalHeap heap = TransactionalHeap.exists(heapName)
                               ? TransactionalHeap.openHeap(heapName)
                               : TransactionalHeap.createHeap(heapName, 500_000_000L);

        long arrayHandle = heap.getRoot();
        if (arrayHandle == 0) {
            System.out.println("Writing longs to heap at " + heapName);
            System.out.println("Creating LongArray of Longs ...");
            Transaction.create(heap, ()-> {
                LongArray array = new LongArray(heap, 7);
                array.set(0,543210);
                array.set(1,654321);
                array.set(2,765432);
                array.set(3,876543);
                array.set(4,987654);
                array.set(5,109876);
                array.set(6,111098);
                heap.setRoot(array.handle());
            });
        }
        else {
            System.out.println("Reading from heap at " + heapName);
            LongArray array = LongArray.fromHandle(heap, arrayHandle);
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < array.size(); i++) {
                long l = array.get(i);
                buffer.append("long[" + i + "] = " + l + "\n");
            }
            System.out.println(buffer);
        }
    }
}
