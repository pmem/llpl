/* 
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.primer;

import com.intel.pmem.llpl.TransactionalHeap;
import com.intel.pmem.llpl.PersistentHeap;
import com.intel.pmem.llpl.TransactionalMemoryBlock;
import com.intel.pmem.llpl.PersistentMemoryBlock;
import com.intel.pmem.llpl.Transaction;

public class Example03_UsingOtherHeaps {
	public static void main(String[] args) {
		{
			// TransactionalHeap and TransactionalMemoryBlock
			String path = "/pmem/llpl/using_other_heaps_transactional";
			boolean initialized = TransactionalHeap.exists(path);
			TransactionalHeap heap = initialized
					? TransactionalHeap.openHeap(path)
					: TransactionalHeap.createHeap(path, 100_000_000);

			if (!initialized) {
				Transaction.create(heap, () -> {
					TransactionalMemoryBlock block = heap.allocateMemoryBlock(256);
					heap.setRoot(block.handle());
					block.setLong(0, 12345);					// automatically added to transaction
					System.out.println("wrote value 12345");
				});
			}
			else {
				long blockHandle = heap.getRoot();
				TransactionalMemoryBlock block = heap.memoryBlockFromHandle(blockHandle);
				long value = block.getLong(0);
				System.out.println("read value " + value);
				assert value == 12345;
			}
		}

		{
			// PersistentHeap and PersistentMemoryBlock
			String path = "/pmem/llpl/using_other_heaps_persistent";
			boolean initialized = PersistentHeap.exists(path);
			PersistentHeap heap = initialized
					? PersistentHeap.openHeap(path)
					: PersistentHeap.createHeap(path, 100_000_000);

			if (!initialized) {
				PersistentMemoryBlock block = heap.allocateMemoryBlock(256, false
					);
				heap.setRoot(block.handle());
				block.setLong(0, 12345);					// automatically flushed
				System.out.println("wrote value 12345");
			}
			else {
				long blockHandle = heap.getRoot();
				PersistentMemoryBlock block = heap.memoryBlockFromHandle(blockHandle);
				long value = block.getLong(0);
				System.out.println("read value " + value);
				assert value == 12345;
			}
		}
	}
}
