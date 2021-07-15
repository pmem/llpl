/* 
 * Copyright (C) 2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.longlinkedlist;

import com.intel.pmem.llpl.TransactionalHeap;
import com.intel.pmem.llpl.Transaction;
import com.intel.pmem.llpl.util.LongLinkedList;
import java.util.Iterator;

public class LongLinkedListExample {

    public static void main(String[] args) {
        String heapName = "/mnt/mem/linkedlist_example";
        TransactionalHeap heap = TransactionalHeap.exists(heapName)
                               ? TransactionalHeap.openHeap(heapName)
                               : TransactionalHeap.createHeap(heapName, 500_000_000L);

        long rootHandle = heap.getRoot();
        if (rootHandle == 0) {
            Transaction.create(heap, ()-> {
                LongLinkedList list = new LongLinkedList(heap);
                heap.setRoot(list.handle());   
                System.out.println("Creating new LinkedList of Employees ...");     
                Employee e1 = new Employee(heap, "John Doe", 78249);
                Employee e2 = new Employee(heap, "Jane Roe", 23113);
                Employee e3 = new Employee(heap, "Jan Rap", 60616);
                Employee e4 = new Employee(heap, "Fulan Fulana", 97205);
                Employee e5 = new Employee(heap, "Marko Markovic", 60443);
                Employee e6 = new Employee(heap, "Marie Novakova", 33491);
                list.addFirst(e1.handle());
                list.addFirst(e2.handle());
                list.addFirst(e3.handle());
                list.add(0, e4.handle());
                list.add(1, e5.handle());
                list.add(2, e6.handle());
            });
        }
        else {
            LongLinkedList list = LongLinkedList.fromHandle(heap, rootHandle);
            Iterator<Long> iter = list.iterator();
            Employee e;
            while (iter.hasNext()) {
                e = Employee.fromHandle(heap, iter.next());
                System.out.println(String.format("Employee: %-15s Id: %d",e.getName(),e.getId()));     
            }
        }
    }
}
