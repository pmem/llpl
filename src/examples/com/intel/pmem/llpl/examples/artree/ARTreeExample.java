/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

import com.intel.pmem.llpl.*;

public class ARTreeExample {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/artree_example";
        TransactionalHeap heap = TransactionalHeap.exists(heapName)
                               ? TransactionalHeap.openHeap(heapName)
                               : TransactionalHeap.createHeap(heapName, 500_000_000L);

        long rootHandle = heap.getRoot();
        if (rootHandle == 0) {
            ARTree tree = new ARTree(heap);
            Transaction.create(heap, ()-> {
                heap.setRoot(tree.handle());
                Planet mercury =  new Planet(heap, "Mercury", Planet.Type.TERRESTRIAL, 1516, 42799783, 88, 0);
                Planet venus =  new Planet(heap, "Venus", Planet.Type.TERRESTRIAL, 3760, 67321289, 225, 0);
                Planet earth =  new Planet(heap, "Earth", Planet.Type.TERRESTRIAL, 3959, 92846983, 365, 1);
                Planet mars =  new Planet(heap, "Mars", Planet.Type.TERRESTRIAL, 2106, 153942122, 687, 2);
                Planet jupiter =  new Planet(heap, "Jupiter", Planet.Type.GAS_GIANT, 43440.7, 488713204, 4333, 79);
                Planet saturn =  new Planet(heap, "Saturn", Planet.Type.GAS_GIANT, 36183.7, 933472595, 10759, 53);
                Planet uranus =  new Planet(heap, "Uranus", Planet.Type.ICE_GIANT, 15759.2, 1755143104, 30687, 27);
                Planet neptune =  new Planet(heap, "Neptune", Planet.Type.ICE_GIANT, 15299.4, 2782556019L, 60190, 13);
                tree.apply(mercury.getName().getBytes(), mercury.handle());
                tree.apply(venus.getName().getBytes(), venus.handle());
                tree.apply(earth.getName().getBytes(), earth.handle());
                tree.apply(mars.getName().getBytes(), mars.handle());
                tree.apply(jupiter.getName().getBytes(), jupiter.handle());
                tree.apply(saturn.getName().getBytes(), saturn.handle());
                tree.apply(uranus.getName().getBytes(), uranus.handle());
                tree.apply(neptune.getName().getBytes(), neptune.handle());
            });
        }
        else {
            ARTree tree = new ARTree (heap, rootHandle);
            ARTree.EntryIterator it = tree.getEntryIterator();
            while (it.hasNext()) {
                ARTree.Entry e = it.next();
                Planet.fromHandle(heap, e.getValue()).print2();
            }
        }
    }
}
