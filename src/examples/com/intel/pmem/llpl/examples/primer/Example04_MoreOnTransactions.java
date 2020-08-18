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

public class Example04_MoreOnTransactions {

	public static void main(String[] args) {
		TransactionalHeap heap = TransactionalHeap.createHeap("/pmem/llpl/more_on_transactions", 100_000_000);
		TransactionalMemoryBlock block = heap.allocateMemoryBlock(1024);

		// normal completion of transaction body commits transaction
		Transaction.create(heap, () -> {
			block.setLong(0, 123);
			block.setLong(8, 234);
		});
		assert block.getLong(0) == 123;
		assert block.getLong(8) == 234;

		// alternate create method give Transaction object to pass as arg
		Transaction tx = Transaction.create(heap);
		tx.run(() -> {
			block.setLong(16, 123);
			otherWrites(tx, block);  // see method at bottom
		});
		assert block.getLong(16) == 123;
		assert block.getLong(24) == 234;

		// uncaught exceptions in transaction body...
		// - will be caught internally by transaction
		// - the transaction will abort
		// - the caught exception will be rethrown
		block.setLong(100, 777);
		try {
			Transaction.create(heap, () -> {
				block.setLong(100, 888);
				block.setLong(10000, 234);  // throws IndexOutOfBoundsException
			});
		} catch (Exception e) {
			long readValue = block.getLong(100);
			assert readValue == 777;
			System.out.println("value at offset 100 = " + readValue);
		}
		assert block.getLong(100) == 777;


		// catch exceptions within body to recover and allow transaction to commit
		Transaction.create(heap, () -> {
			block.setLong(0, 999);
			int value = 0;
			try {
				value = Integer.parseInt("234.456");
			} catch (NumberFormatException e) {
				System.out.println("caught exception, setting value to -1");
				value = -1;
			}
			block.setInt(100, value);
		});
		System.out.println("value at offset 0 = " + block.getLong(0));
		System.out.println("value at offset 100 = " + block.getInt(100));
		assert block.getLong(0) == 999;
		assert block.getInt(100) == -1;

		// abort / commit handler can be implemented using try-catch outside of transaction
		boolean aborted = false;
		try {
			Transaction.create(heap, () -> {
				String s = null;
				int len = s.length();
			});
		} catch(Throwable t) {
			aborted = true;
			System.out.println("abort handler code");
			//throw(t);
		} finally {
			if (!aborted) {
				System.out.println("commit handler code");
			}
		}

		// nested transaction are flattened to one outer transaction
		block.setLong(0, 111);
		Transaction.create(heap, () -> {
			block.setLong(0, 123);
			Transaction.create(heap, () -> {
				block.setLong(8, 234);
			});
			// writes are visible but not committed at this point
		});
		// both writes commit here

		// without outer transaction sequences transactions commit / abort seprately
		Transaction.create(heap, () -> {
			block.setLong(0, 123);
		});
		// first transaction committed here
		Transaction.create(heap, () -> {
			block.setLong(8, 123);
		});
		// second transaction commited here
	}

	private static void otherWrites(Transaction tx, TransactionalMemoryBlock block) {
		tx.run(() -> block.setLong(24, 234));
	}
}
