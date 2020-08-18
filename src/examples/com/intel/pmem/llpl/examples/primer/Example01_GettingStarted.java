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

public class Example01_GettingStarted {

	public static void main(String[] args) {
		String path = "/pmem/llpl/getting_started";
		boolean initialized = Heap.exists(path);
		Heap heap = initialized ? Heap.openHeap(path) : Heap.createHeap(path, 100_000_000);

		if (!initialized) {
			// first run -- write values
			MemoryBlock block = heap.allocateMemoryBlock(256, false);
			heap.setRoot(block.handle());

			// durable write at block offset 0
			block.setLong(0, 12345);
			block.flush(0, Long.BYTES);
			System.out.println("wrote and flushed value 12345");

			// transactional write at offset 0
			Transaction.create(heap, () -> {
				block.addToTransaction(8, Long.BYTES);
				block.setLong(8, 23456);
				System.out.println("wrote and flushed value 23456");
			});

			// allocate another block and link it to the first block
			MemoryBlock otherBlock = heap.allocateMemoryBlock(256, false);
			otherBlock.setInt(0, 111);
			otherBlock.flush(0, Integer.BYTES);
			block.setLong(16, otherBlock.handle());
			block.flush(16, Long.BYTES);
		}
		else {
			// subsequent runs -- read values back
			long blockHandle = heap.getRoot();
			MemoryBlock block = heap.memoryBlockFromHandle(blockHandle);
			long value1 = block.getLong(0);
			long value2 = block.getLong(8);
			MemoryBlock otherBlock = heap.memoryBlockFromHandle(block.getLong(16));
			int otherValue = otherBlock.getInt(0);
			System.out.println("read value = " + value1);
			System.out.println("read value = " + value2);
			System.out.println("read otherValue = " + otherValue);
			assert value1 == 12345;
			assert value2 == 23456;
			assert otherValue == 111;
			// deallocate persistent memory when appropriate
			otherBlock.free(false);
			block.free(false);
			heap.setRoot(0);
		}
	}
}
