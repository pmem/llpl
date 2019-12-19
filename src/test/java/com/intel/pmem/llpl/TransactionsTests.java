/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

@Test(singleThreaded = true)
public class TransactionsTests {
	Heap heap = null;
	PersistentHeap pHeap = null;
	TransactionalHeap tHeap = null;

	@BeforeMethod
	public void intialize() {
		heap = null;
        pHeap = null;
        tHeap = null;
	}

	@SuppressWarnings("deprecation")
	@AfterMethod
	public void testCleanup() {
		if (heap != null)
			heap.close();
		if (pHeap != null)
			pHeap.close();
		if (tHeap != null)
			tHeap.close();

		if (TestVars.ISDAX) {
			TestVars.daxCleanUp();
		}
		else TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME);
		TestVars.cleanUp(TestVars.INVALID_HEAP_PATH);
		TestVars.cleanUp(TestVars.BLOCK_HANDLE_FILE);
		TestVars.cleanUp(TestVars.POOL_SET_FILE);
		for (int i = 0; i < TestVars.NUM_HEAPS; i++) {
			TestVars.cleanUp(TestVars.HEAP_USER_PATH + TestVars.HEAP_NAME + i);
		}
	}

	@Test
	public void testStaticRunnable() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(16, true);
        Transaction.create(heap, () -> {
            mb.addToTransaction();
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
        });
        assert(mb.getLong(4) == 1000);
        assert(mb.getInt(0) == 777);
	}

	@Test
	public void testStaticRunnableAborts() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(16, true);
        try {
            Transaction.create(heap, () -> {
                mb.addToTransaction();
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(4) == 0);
            assert(mb.getShort(14) == 0);
        }
	}

	@Test
	public void testStaticRunnableCommits() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(16, true);
        try {
            Transaction.create(heap, () -> {
                mb.addToTransaction();
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
            });
            assert(mb.getLong(4) == 1000);
            assert(mb.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testStaticRunnablePersistent() {
		pHeap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
        Transaction.create(pHeap, () -> {
            mb.addToTransaction();
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
        });
        assert(mb.getLong(4) == 1000);
        assert(mb.getInt(0) == 777);
	}

	@Test
	public void testStaticUncaughtExceptionAbortsPersistent() {
		pHeap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
        try {
            Transaction.create(pHeap, () -> {
                mb.addToTransaction();
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(4) == 0);
            assert(mb.getShort(14) == 0);
        }
	}

	@Test
	public void testStaticCaughtExceptionCommitsPersistent() {
		pHeap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
        try {
            Transaction.create(pHeap, () -> {
                mb.addToTransaction();
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
            });
            assert(mb.getLong(4) == 1000);
            assert(mb.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testStaticRunnableTransactional() {
		tHeap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
        Transaction.create(tHeap, () -> {
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
        });
        assert(mb.getLong(4) == 1000);
        assert(mb.getInt(0) == 777);
	}

	@Test
	public void testStaticUncaughtExceptionAbortsTransactional() {
		tHeap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
        try {
            Transaction.create(tHeap, () -> {
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(4) == 0);
            assert(mb.getShort(14) == 0);
        }
	}

	@Test
	public void testStaticCaughtExceptionCommitsTransactional() {
		tHeap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
        try {
            Transaction.create(tHeap, () -> {
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
            });
            assert(mb.getLong(4) == 1000);
            assert(mb.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testStaticSupplier() {
		heap = TestVars.createHeap();
        MemoryBlock block = Transaction.create(heap, () -> {
		    MemoryBlock mb = heap.allocateMemoryBlock(16, true);
            mb.addToTransaction();
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
            return mb;
        });
        assert(block.getLong(4) == 1000);
        assert(block.getInt(0) == 777);
	}

	@Test
	public void testStaticSupplierAborts() {
		heap = TestVars.createHeap();
        MemoryBlock block = null;
        try {
            block = Transaction.create(heap, () -> {
		        MemoryBlock mb = heap.allocateMemoryBlock(16, true);
                mb.addToTransaction();
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
                return mb;
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(block == null);
        }
	}

	@Test
	public void testStaticSupplierCommits() {
		heap = TestVars.createHeap();
        try {
            MemoryBlock block = Transaction.create(heap, () -> {
		        MemoryBlock mb = heap.allocateMemoryBlock(16, true);
                mb.addToTransaction();
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
                return mb;
            });
            assert(block.getLong(4) == 1000);
            assert(block.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testStaticSupplierPersistent() {
		pHeap = TestVars.createPersistentHeap();
        PersistentMemoryBlock block = Transaction.create(pHeap, () -> {
		    PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
            mb.addToTransaction();
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
            return mb;
        });
        assert(block.getLong(4) == 1000);
        assert(block.getInt(0) == 777);
	}

	@Test
	public void testStaticSupplierAbortsPersistent() {
		pHeap = TestVars.createPersistentHeap();
        PersistentMemoryBlock block = null;
        try {
            block = Transaction.create(pHeap, () -> {
		        PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
                mb.addToTransaction();
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
                return mb;
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(block == null);
        }
	}

	@Test
	public void testStaticSupplierCommitsPersistent() {
		pHeap = TestVars.createPersistentHeap();
        try {
            PersistentMemoryBlock block = Transaction.create(pHeap, () -> {
		        PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
                mb.addToTransaction();
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
                return mb;
            });
            assert(block.getLong(4) == 1000);
            assert(block.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testStaticSupplierTransactional() {
		tHeap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock block = Transaction.create(tHeap, () -> {
		    TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
            return mb;
        });
        assert(block.getLong(4) == 1000);
        assert(block.getInt(0) == 777);
	}

	@Test
	public void testStaticSupplierAbortsTransactional() {
		tHeap = TestVars.createTransactionalHeap();
        TransactionalMemoryBlock block = null;
        try {
            block = Transaction.create(tHeap, () -> {
		        TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
                return mb;
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert (block == null);
        }
	}

	@Test
	public void testStaticSupplierCommitsTransactional() {
		tHeap = TestVars.createTransactionalHeap();
        try {
            TransactionalMemoryBlock block = Transaction.create(tHeap, () -> {
		        TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
                return mb;
            });
            assert(block.getLong(4) == 1000);
            assert(block.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testInstanceRunnable() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(16, true);

        Transaction tx = Transaction.create(heap);
        assert(Transaction.State.New == tx.state());
        tx.run(() -> {
            mb.addToTransaction();
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
            assert(Transaction.State.Active == tx.state());
        });
        assert(Transaction.State.Committed == tx.state());
        assert(mb.getLong(4) == 1000);
        assert(mb.getInt(0) == 777);
	}

	@Test
	public void testInstanceRunnableAborts() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(16, true);
        Transaction tx = Transaction.create(heap);
        assert(Transaction.State.New == tx.state());
        try {
            tx.run(() -> {
                mb.addToTransaction();
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
                assert(Transaction.State.Active == tx.state());
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(Transaction.State.Aborted == tx.state());
            assert(mb.getLong(4) == 0);
            assert(mb.getShort(14) == 0);
        }
	}

	@Test
	public void testInstanceRunnableCommits() {
		heap = TestVars.createHeap();
		MemoryBlock mb = heap.allocateMemoryBlock(16, true);
        try {
            Transaction tx = Transaction.create(heap);
            assert(Transaction.State.New == tx.state());
            tx.run(() -> {
                mb.addToTransaction();
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
                assert(Transaction.State.Active == tx.state());
            });
            assert(Transaction.State.Committed == tx.state());
            assert(mb.getLong(4) == 1000);
            assert(mb.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testInstanceRunnablePersistent() {
		pHeap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
        Transaction tx = Transaction.create(pHeap);
        assert(Transaction.State.New == tx.state());
        tx.run(() -> {
            mb.addToTransaction();
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
            assert(Transaction.State.Active == tx.state());
        });
        assert(Transaction.State.Committed == tx.state());
        assert(mb.getLong(4) == 1000);
        assert(mb.getInt(0) == 777);
	}

	@Test
	public void testInstanceRunnableAbortsPersistent() {
		pHeap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
        Transaction tx = Transaction.create(pHeap);
        assert(Transaction.State.New == tx.state());
        try {
            tx.run(() -> {
                mb.addToTransaction();
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
                assert(Transaction.State.Active == tx.state());
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(Transaction.State.Aborted == tx.state());
            assert(mb.getLong(4) == 0);
            assert(mb.getShort(14) == 0);
        }
	}

	@Test
	public void testInstanceRunnableCommitsPersistent() {
		pHeap = TestVars.createPersistentHeap();
		PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
        try {
            Transaction tx = Transaction.create(pHeap);
            assert(Transaction.State.New == tx.state());
            tx.run(() -> {
                mb.addToTransaction();
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
                assert(Transaction.State.Active == tx.state());
            });
            assert(Transaction.State.Committed == tx.state());
            assert(mb.getLong(4) == 1000);
            assert(mb.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testInstanceRunnableTransactional() {
		tHeap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
        Transaction tx = Transaction.create(tHeap);
        assert(Transaction.State.New == tx.state());
        tx.run(() -> {
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
            assert(Transaction.State.Active == tx.state());
        });
        assert(Transaction.State.Committed == tx.state());
        assert(mb.getLong(4) == 1000);
        assert(mb.getInt(0) == 777);
	}

	@Test
	public void testInstanceRunnableAbortsTransactional() {
		tHeap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
        Transaction tx = Transaction.create(tHeap);
        assert(Transaction.State.New == tx.state());
        try {
            tx.run(() -> {
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
                assert(Transaction.State.Active == tx.state());
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(Transaction.State.Aborted == tx.state());
            assert(mb.getLong(4) == 0);
            assert(mb.getShort(14) == 0);
        }
	}

	@Test
	public void testInstanceRunnableCommitsTransactional() {
		tHeap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
        try {
            Transaction tx = Transaction.create(tHeap);
            assert(Transaction.State.New == tx.state());
            tx.run(() -> {
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
                assert(Transaction.State.Active == tx.state());
            });
            assert(Transaction.State.Committed == tx.state());
            assert(mb.getLong(4) == 1000);
            assert(mb.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testInstanceSupplier() {
		heap = TestVars.createHeap();
        Transaction tx = Transaction.create(heap);
        assert(Transaction.State.New == tx.state());
        MemoryBlock block = tx.run(() -> {
		    MemoryBlock mb = heap.allocateMemoryBlock(16, true);
            mb.addToTransaction();
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
            assert(Transaction.State.Active == tx.state());
            return mb;
        });
        assert(Transaction.State.Committed == tx.state());
        assert(block.getLong(4) == 1000);
        assert(block.getInt(0) == 777);
	}

	@Test
	public void testInstanceSupplierAborts() {
		heap = TestVars.createHeap();
		MemoryBlock block = null;
        Transaction tx = Transaction.create(heap);
        assert(Transaction.State.New == tx.state());
        try {
            block = tx.run(() -> {
		        MemoryBlock mb = heap.allocateMemoryBlock(16, true);
                mb.addToTransaction();
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
                assert(Transaction.State.Active == tx.state());
                return mb;
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(Transaction.State.Aborted == tx.state());
            assert(block == null);
        }
	}

	@Test
	public void testInstanceSupplierCommits() {
		heap = TestVars.createHeap();
        try {
            Transaction tx = Transaction.create(heap);
            assert(Transaction.State.New == tx.state());
            MemoryBlock block = tx.run(() -> {
		        MemoryBlock mb = heap.allocateMemoryBlock(16, true);
                mb.addToTransaction();
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
                assert(Transaction.State.Active == tx.state());
                return mb;
            });
            assert(Transaction.State.Committed == tx.state());
            assert(block.getLong(4) == 1000);
            assert(block.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testInstanceSupplierPersistent() {
		pHeap = TestVars.createPersistentHeap();
        Transaction tx = Transaction.create(pHeap);
        assert(Transaction.State.New == tx.state());
        PersistentMemoryBlock block = tx.run(() -> {
		    PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
            mb.addToTransaction();
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
            assert(Transaction.State.Active == tx.state());
            return mb;
        });
        assert(Transaction.State.Committed == tx.state());
        assert(block.getLong(4) == 1000);
        assert(block.getInt(0) == 777);
	}

	@Test
	public void testInstanceSupplierAbortsPersistent() {
		pHeap = TestVars.createPersistentHeap();
		PersistentMemoryBlock block = null;
        Transaction tx = Transaction.create(pHeap);
        assert(Transaction.State.New == tx.state());
        try {
            block = tx.run(() -> {
		        PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
                mb.addToTransaction();
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
                assert(Transaction.State.Active == tx.state());
                return mb;
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(Transaction.State.Aborted == tx.state());
            assert(block == null);
        }
	}

	@Test
	public void testInstanceSupplierCommitsPersistent() {
		pHeap = TestVars.createPersistentHeap();
        try {
            Transaction tx = Transaction.create(pHeap);
            assert(Transaction.State.New == tx.state());
            PersistentMemoryBlock block = tx.run(() -> {
		        PersistentMemoryBlock mb = pHeap.allocateMemoryBlock(16, true);
                mb.addToTransaction();
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
                assert(Transaction.State.Active == tx.state());
                return mb;
            });
            assert(Transaction.State.Committed == tx.state());
            assert(block.getLong(4) == 1000);
            assert(block.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}

	@Test
	public void testInstanceSupplierTransactional() {
		tHeap = TestVars.createTransactionalHeap();
        Transaction tx = Transaction.create(tHeap);
        assert(Transaction.State.New == tx.state());
        TransactionalMemoryBlock block = tx.run(() -> {
		    TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
            mb.setLong(4, 1000);
            mb.setInt(0, 777);
            assert(Transaction.State.Active == tx.state());
            return mb;
        });
        assert(Transaction.State.Committed == tx.state());
        assert(block.getLong(4) == 1000);
        assert(block.getInt(0) == 777);
	}

	@Test
	public void testInstanceSupplierAbortsTransactional() {
		tHeap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock block = null;
        Transaction tx = Transaction.create(tHeap);
        assert(Transaction.State.New == tx.state());
        try {
            block = tx.run(() -> {
		        TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
                mb.setLong(4, 1000);
                mb.setInt(14, 777);
                assert(Transaction.State.Active == tx.state());
                return mb;
            });
		    Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(Transaction.State.Aborted == tx.state());
            assert(block == null);
        }
	}

	@Test
	public void testInstanceSupplierCommitsTransactional() {
		tHeap = TestVars.createTransactionalHeap();
        try {
            Transaction tx = Transaction.create(tHeap);
            assert(Transaction.State.New == tx.state());
            TransactionalMemoryBlock block = tx.run(() -> {
		        TransactionalMemoryBlock mb = tHeap.allocateMemoryBlock(16);
                try {
                    mb.setLong(4, 1000);
                    mb.setInt(14, 777);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setInt(0, 777);
                }
                assert(Transaction.State.Active == tx.state());
                return mb;
            });
            assert(Transaction.State.Committed == tx.state());
            assert(block.getLong(4) == 1000);
            assert(block.getInt(0) == 777);
        } 
        catch (Exception e) {
		    Assert.fail("Transaction did not commit");
        }
	}
}
