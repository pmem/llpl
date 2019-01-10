/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package examples.string_store;

import lib.llpl.*;
import java.io.Console;

public class Reader {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);

        Console c = System.console();
        if (c == null) {
            System.out.println("No console.");
            System.exit(1);
        }

        long rootAddr = h.getRoot();
        if (rootAddr == 0) {
            System.out.println("No string found!");
            System.exit(0);
        }
        MemoryBlock mr = h.memoryBlockFromHandle(rootAddr);
        byte[] bytes = new byte[mr.getInt(0)];
        for (int i = 0; i < mr.getInt(0); i++) {
            bytes[i] = mr.getByte(Integer.BYTES + i);
        }

        System.out.println("Found string \"" + new String(bytes) + "\".");
    }
}
