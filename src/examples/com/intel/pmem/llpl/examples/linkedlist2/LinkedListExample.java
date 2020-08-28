/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.linkedlist2;

import com.intel.pmem.llpl.*;

public class LinkedListExample {

    public static void main(String[] args) {
        String heapName = "/mnt/mem/linkedlist_example";
        Heap heap = Heap.exists(heapName)
                  ? Heap.openHeap(heapName)
                  : Heap.createHeap(heapName, 500_000_000L);

        long rootHandle = heap.getRoot();
        if (rootHandle == 0) {
            Transaction.create(heap, ()-> {
                LinkedList l = new LinkedList(heap);
                heap.setRoot(l.handle());   
                System.out.println("Creating new LinkedList of Employees ...");     
                Employee e1 = new Employee(heap, "John Doe", 78249);
                Employee e2 = new Employee(heap, "Jane Roe", 23113);
                Employee e3 = new Employee(heap, "Jan Rap", 60616);
                Employee e4 = new Employee(heap, "Fulan Fulana", 97205);
                Employee e5 = new Employee(heap, "Marko Markovic", 60443);
                Employee e6 = new Employee(heap, "Marie Novakova", 33491);
                l.insertFirst(e1.handle());
                l.insertFirst(e2.handle());
                l.insertFirst(e3.handle());
                l.insert(0, e4.handle());
                l.insert(1, e5.handle());
                l.insert(2, e6.handle());
            });
        }
        else {
            LinkedList l = LinkedList.fromHandle(heap, rootHandle);
            LinkedList.Iterator i = l.getIterator();
            Employee e;
            while (i.hasNext()) {
                e = Employee.fromHandle(heap, i.next());
                System.out.println(String.format("Employee: %-15s Id: %d",e.getName(),e.getId()));     
            }
        }
    }
}
