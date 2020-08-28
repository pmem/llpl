/* 
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.primer;

import com.intel.pmem.llpl.Heap;
import com.intel.pmem.llpl.MemoryBlock;
import com.intel.pmem.llpl.Transaction;
import java.io.File;

public class Example02_SizingHeaps {
	private static long MB = 1024L * 1024L;
	private static long GB = 1024 * MB;

	public static void main(String[] args) {
		// 1. heap of fixed size
		Heap fixedHeap = Heap.createHeap("/pmem/llpl/fixed", 100 * MB);
		System.out.println("created fixed heap of size " + fixedHeap.size());

		// 2. growable heap -- starts at minimum size and grows to all available pmem of file system
		String dir_path1 = "/pmem/llpl/growable/";
		new File(dir_path1).mkdirs();
		Heap growableHeap = Heap.createHeap(dir_path1);
		System.out.println("created growable heap with initial size " + growableHeap.size());

		// 3. growable heap with limit -- starts at minimum size and grows up to specified limit
		String dir_path2 = "/pmem/llpl/growable_with_limit/";
		new File(dir_path2).mkdirs();
		Heap limitedHeap = Heap.createHeap(dir_path2, 1 * GB);
		System.out.println("created growable-with-limit heap with initial size " + limitedHeap.size());

		// 4. DAX device heap -- size is fixed to size of DAX device
		// Heap daxHeap = Heap.createHeap("/dev/dax1");

		// 5. fused heap -- size is set to combined size of file systems / DAX devices in
		// String configFile = "/pmem/llpl/fused/poolset";
		// Heap fixed = Heap.createHeap("/pmem/llpl/fused/pool.set");
	}
}
