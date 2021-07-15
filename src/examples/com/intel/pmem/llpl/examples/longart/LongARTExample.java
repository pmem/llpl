/* 
 * Copyright (C) 2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.longart;

import com.intel.pmem.llpl.TransactionalHeap;
import com.intel.pmem.llpl.Transaction;
import com.intel.pmem.llpl.util.LongART;
import java.util.Iterator;

public class LongARTExample {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/longart_example";
        TransactionalHeap heap = TransactionalHeap.exists(heapName)
                               ? TransactionalHeap.openHeap(heapName)
                               : TransactionalHeap.createHeap(heapName, 500_000_000L);

        long rootHandle = heap.getRoot();
        if (rootHandle == 0) {
            LongART tree = new LongART(heap);
            Transaction.create(heap, ()-> {
                heap.setRoot(tree.handle());
                System.out.println("Creating new Radix Tree of Planets ...");     
                Planet mercury =  new Planet(heap, "Mercury", Planet.Type.TERRESTRIAL, 1516, 42799783, 88, 0);
                Planet venus =  new Planet(heap, "Venus", Planet.Type.TERRESTRIAL, 3760, 67321289, 225, 0);
                Planet earth =  new Planet(heap, "Earth", Planet.Type.TERRESTRIAL, 3959, 92846983, 365, 1);
                Planet mars =  new Planet(heap, "Mars", Planet.Type.TERRESTRIAL, 2106, 153942122, 687, 2);
                Planet jupiter =  new Planet(heap, "Jupiter", Planet.Type.GAS_GIANT, 43440.7, 488713204, 4333, 79);
                Planet saturn =  new Planet(heap, "Saturn", Planet.Type.GAS_GIANT, 36183.7, 933472595, 10759, 53);
                Planet uranus =  new Planet(heap, "Uranus", Planet.Type.ICE_GIANT, 15759.2, 1755143104, 30687, 27);
                Planet neptune =  new Planet(heap, "Neptune", Planet.Type.ICE_GIANT, 15299.4, 2782556019L, 60190, 13);
                tree.put(mercury.getName().getBytes(), mercury.handle());
                tree.put(venus.getName().getBytes(), venus.handle());
                tree.put(earth.getName().getBytes(), earth.handle());
                tree.put(mars.getName().getBytes(), mars.handle());
                tree.put(jupiter.getName().getBytes(), jupiter.handle());
                tree.put(saturn.getName().getBytes(), saturn.handle());
                tree.put(uranus.getName().getBytes(), uranus.handle());
                tree.put(neptune.getName().getBytes(), neptune.handle());
            });
        }
        else {
            LongART tree = LongART.fromHandle(heap, rootHandle);
            Iterator<LongART.Entry> it = tree.getEntryIterator();
            while (it.hasNext()) {
                LongART.Entry e = it.next();
                if (e != null) Planet.fromHandle(heap, e.getValue()).print2();
            }
        }
    }
}
