/* 
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.primer;

import com.intel.pmem.llpl.TransactionalHeap;
import com.intel.pmem.llpl.TransactionalMemoryBlock;
import com.intel.pmem.llpl.Transaction;
import com.intel.pmem.llpl.Range;

public class Example06_WrappingMemoryBlocks {
	public static void main(String[] args) {
		String path = "/pmem/llpl/wrapping_memory_blocks";
		boolean initialized = TransactionalHeap.exists(path);
		TransactionalHeap heap = initialized
			? TransactionalHeap.openHeap(path)
			: TransactionalHeap.createHeap(path, 100_000_000);

		if (!initialized) {
			Employee e = new Employee(heap, "Peter", 12345);
			heap.setRoot(e.handle());
			System.out.println("Created " + e);
		}
		else {
			long handle = heap.getRoot();
			Employee e = new Employee(heap, handle);
			System.out.println("Retrieved " + e);
		}
	}

	public static class Employee {
		private static final long ID_OFFSET = 0;
		private static final long NAME_LENGTH_OFFSET = 8;
		private static final long NAME_OFFSET = 12;
		private TransactionalMemoryBlock block;		// one instance field -- the memory block

		// constructor
		public Employee(TransactionalHeap heap, String name, long id) {
			byte[] nameBytes = name.getBytes();
			block = heap.allocateMemoryBlock(NAME_OFFSET + nameBytes.length, (Range range) -> {
				range.setLong(ID_OFFSET, id);
				range.setInt(NAME_LENGTH_OFFSET, nameBytes.length);
				range.copyFromArray(nameBytes, 0, NAME_OFFSET, nameBytes.length);
			});
		}

		// re-constructor -- rewrap a memory block -- no pmem allocation
		public Employee(TransactionalHeap heap, long handle) {
			this.block = heap.memoryBlockFromHandle(handle);
		}

		// accessor methods -- encasulates use of offsets
		public long id() {return block.getLong(ID_OFFSET);}

		public String name() {
			int length = block.getInt(NAME_LENGTH_OFFSET);
			byte[] bytes = new byte[length];
			block.copyToArray(NAME_OFFSET, bytes, 0, length);
			return new String(bytes);
		}

		// handle getter -- used when writing reference to this object in another block
		public long handle() {
			return block.handle();
		}

		public void free() {
			block.free();
		}

		public String toString() {
			return String.format("Employee(%s, %d)", name(), id());
		}
	}
}
