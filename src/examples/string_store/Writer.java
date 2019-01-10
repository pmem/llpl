/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package examples.string_store;

import lib.llpl.*;
import java.io.Console;

public class Writer {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);

        Console c = System.console();
        if (c == null) {
            System.out.println("No console.");
            System.exit(1);
        }

        String str = c.readLine("Inset your test string: ");

        MemoryBlock mr = h.allocateMemoryBlock(Integer.BYTES + str.length(), false);
        byte[] bytes = str.getBytes();
        mr.setInt(0, str.length());
        for (int i = 0; i < str.length(); i++) {
            mr.setByte(Integer.BYTES + i, bytes[i]);
        }

        h.setRoot(mr.handle());

        System.out.println("String \"" + str + "\" successfully written.");
    }
}
