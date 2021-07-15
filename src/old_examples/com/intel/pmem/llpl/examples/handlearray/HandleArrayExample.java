/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.handlearray;

import com.intel.pmem.llpl.*;

public class HandleArrayExample {
    public static void main(String[] args) {
        String heapName = "/mnt/mem/HandleArray_example";
        TransactionalHeap heap = TransactionalHeap.exists(heapName)
                               ? TransactionalHeap.openHeap(heapName)
                               : TransactionalHeap.createHeap(heapName, 500_000_000L);

        long arrayHandle = heap.getRoot();
        if (arrayHandle == 0) {
            System.out.println("Creating HandleArray of Continents ...");
            Transaction.create(heap, ()-> {
                HandleArray ha = new HandleArray(heap, 7);
                Continent Africa = new Continent(heap, "Africa", 30370000, 20.4, 1287920000L, "Lagos, Nigeria");
                Continent Antarctica = new Continent(heap, "Antarctica", 14000000, 9.2, 4490L, "McMurdo Station");
                Continent Asia = new Continent(heap, "Asia", 44579000, 29.5, 4545133000L, "Shanghai, China");
                Continent Europe = new Continent(heap, "Europe", 10180000, 6.8, 742648000L, "Moscow, Russia");
                Continent NorthAmerica = new Continent(heap, "North America", 24709000, 16.5, 587615000L, "Mexico City, Mexico");
                Continent Australia = new Continent(heap, "Australia", 8600000, 5.9, 41261000L, "Sydney, Australia");
                Continent SouthAmerica = new Continent(heap, "South America", 17840000, 12.0, 428240000L, "Sao Paulo, Brazil");
                ha.set(0, Africa.handle());
                ha.set(1, Antarctica.handle());
                ha.set(2, Asia.handle());
                ha.set(3, Europe.handle());
                ha.set(4, NorthAmerica.handle());
                ha.set(5, Australia.handle());
                ha.set(6, SouthAmerica.handle());
                heap.setRoot(ha.handle());
            });
        }
        else {
            HandleArray ha = HandleArray.fromHandle(heap, arrayHandle); 
            StringBuffer buffer = new StringBuffer();
            buffer.append("+-----------------------------------------------------------------------------------------+\n");
            buffer.append("| Continent      |   Area (SqKm)  |   Landmass(%)  |   Population   | Largest City        |\n");
            buffer.append("+----------------+----------------+----------------+----------------+---------------------+\n");
            for (int i = 0; i < ha.size(); i++) {
                Continent c = Continent.fromHandle(heap, ha.get(i));
                buffer.append(String.format("| %-15s|%,15d |%15.2f |%,15d | %-20s|\n",c.getName(), c.getArea(), c.getLandMass(), c.getPopulation(), c.getLargestCity()));
            } 
            buffer.append("+-----------------------------------------------------------------------------------------+\n");
            System.out.println(buffer);
        }
    }
}
