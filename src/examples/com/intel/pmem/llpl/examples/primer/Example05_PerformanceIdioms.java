/* 
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.primer;

import com.intel.pmem.llpl.Heap;
import com.intel.pmem.llpl.TransactionalHeap;
import com.intel.pmem.llpl.MemoryBlock;
import com.intel.pmem.llpl.TransactionalMemoryBlock;
import com.intel.pmem.llpl.Transaction;
import com.intel.pmem.llpl.Range;

public class Example05_PerformanceIdioms {
	public static void main(String[] args) {
		Heap heap = Heap.createHeap("/pmem/llpl/performance_idioms", 100_000_000);
		TransactionalHeap txHeap = TransactionalHeap.createHeap("/pmem/llpl/tx_performance_idioms", 100_000_000);
		byte[] bytes = "Hello world".getBytes();
		int stringLength = bytes.length;

		// sequence of write then flush is correct...
		MemoryBlock block1 = heap.allocateMemoryBlock(256, true);
		block1.setLong(0, 12345);
		block1.flush(0, Long.BYTES);
		block1.setInt(8, stringLength);
		block1.flush(8, Integer.BYTES);
		block1.copyFromArray(bytes, 0, 12, stringLength);
		block1.flush(12, stringLength);

		// ...but flush of whole range is faster here
		block1.setLong(0, 12345);
		block1.setInt(8, stringLength);
		block1.copyFromArray(bytes, 0, 12, stringLength);
		block1.flush(0, Long.BYTES + Integer.BYTES + stringLength);

		// for transactional writes, sequence of adds and writes is correct...
		MemoryBlock block2 = heap.allocateMemoryBlock(256, true);
		Transaction.create(heap, () -> {
			block2.addToTransaction(0, Long.BYTES);
			block2.setLong(0, 12345);
			block2.addToTransaction(8, Integer.BYTES);
			block2.setInt(8, stringLength);
			block2.addToTransaction(12, stringLength);
			block2.copyFromArray(bytes, 0, 12, stringLength);
		});

		// ...but add of whole range is faster here
		Transaction.create(heap, () -> {
			block2.addToTransaction(0, Long.BYTES + Integer.BYTES + stringLength);
			block2.setLong(0, 12345);
			block2.setInt(8, stringLength);
			block2.copyFromArray(bytes, 0, 12, stringLength);
		});

		// when using a TransactionalMemoryBlock addToTransaction is automatic... 
		TransactionalMemoryBlock txBlock = txHeap.allocateMemoryBlock(256);
		Transaction.create(txHeap, () -> {								// outer transaction
			txBlock.setLong(0, 12345);									// add and write
			txBlock.setInt(8, stringLength);							// add and write
			txBlock.copyFromArray(bytes, 0, 12, stringLength); 		// add and copy
		});

		// ...but withRange method adds whole range -- is faster here
		long totalLength = Long.BYTES + Integer.BYTES + stringLength;
		txBlock.withRange(0, totalLength, (Range range) -> {
			range.setLong(0, 12345);
			range.setInt(8, stringLength);
			range.copyFromArray(bytes, 0, 12, stringLength);
		});

		// special allocate-and-initialize method -- very efficient
		TransactionalMemoryBlock txBlock2 = txHeap.allocateMemoryBlock(totalLength, (Range range) -> {
			range.setLong(0, 12345);
			range.setInt(8, stringLength);
			range.copyFromArray(bytes, 0, 12, stringLength);
		});
        assert(txBlock2.isValid());
	}
}
