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
public class Transactions2Tests {
	TransactionalHeap heap = null;

	@BeforeMethod
	public void intialize() {
		heap = null;
	}

	@SuppressWarnings("deprecation")
	@AfterMethod
	public void testCleanup() {
		if (heap != null)
			heap.close();

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
	public void testNestedStatic() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction.create(heap, () -> {
            mb.setLong(0, 12345L);
            Transaction.create(heap, () -> {
                mb.setLong(8, 555);
            });
        });
        assert(mb.getLong(0) == 12345);
        assert(mb.getLong(8) == 555);
	}
    
    @Test
	public void testNestedStaticCommits() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                Transaction.create(heap, () -> {
                    try { 
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                    }
                });
            });
            assert(mb.getLong(0) == 12345);
            assert(mb.getLong(8) == 555);
        } 
        catch (Exception e) {
            Assert.fail("Transaction did not commit");
        }
	}
 
    @Test
	public void testNestedStaticCommitAbort() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                Transaction.create(heap, () -> {
                    mb.setLong(28, 555);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (Exception e) {
            assert(mb.getLong(0) == 0);
        }
	}
 
    @Test
	public void testNestedStaticAborts() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        try {
            Transaction.create(heap, () -> {
                mb.setLong(20, 12345L);
                Transaction.create(heap, () -> {
                    mb.setLong(8, 555);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getInt(20) == 0);
            assert(mb.getLong(8) == 0);
        }
	}

	@Test
	public void testNestedMixed1() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(Transaction.State.New == t.state());
        Transaction.create(heap, () -> {
            mb.setLong(0, 12345L);
            t.run(() -> {
                mb.setLong(8, 555);
                assert(Transaction.State.Active == t.state());
            });
        });
        assert(Transaction.State.Committed == t.state());
        assert(mb.getLong(0) == 12345);
        assert(mb.getLong(8) == 555);
	}
    
    @Test
	public void testNestedMixed1Commits() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(Transaction.State.New == t.state());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                t.run(() -> {
                    try { 
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                        assert(Transaction.State.Active == t.state());
                    }
                });
            });
            assert(Transaction.State.Committed == t.state());
            assert(mb.getLong(0) == 12345);
            assert(mb.getLong(8) == 555);
        } 
        catch (Exception e) {
            Assert.fail("Transaction did not commit");
        }
	}
 
    @Test
	public void testNestedMixed1CommitAbort() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(Transaction.State.New == t.state());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                t.run(() -> {
                    mb.setLong(28, 555);
                    assert(Transaction.State.Active == t.state());
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (Exception e) {
            assert(Transaction.State.Aborted == t.state());
            assert(mb.getLong(0) == 0);
        }
	}
 
    @Test
	public void testNestedMixed1Aborts() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(Transaction.State.New == t.state());
        try {
            Transaction.create(heap, () -> {
                mb.setLong(20, 12345L);
                t.run(() -> {
                    mb.setLong(8, 555);
                    assert(Transaction.State.Active == t.state());
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (Exception e) {
            assert(Transaction.State.New == t.state()); 
            assert(mb.getInt(20) == 0);
            assert(mb.getLong(8) == 0);
        }
	}

	@Test
	public void testNestedMixed2() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(Transaction.State.New == t.state());
        Transaction.create(heap, () -> {
            mb.setLong(0, 12345L);
            t.run(() -> {
                mb.setLong(8, 555);
                Assert.assertTrue(Transaction.State.Active == t.state());
            });
        });
        Assert.assertTrue(Transaction.State.Committed == t.state());
        assert(mb.getLong(0) == 12345);
        assert(mb.getLong(8) == 555);
	}
    
    @Test
	public void testNestedMixed2Commits() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(Transaction.State.New == t.state());
        try {
            t.run(() -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                    assert(Transaction.State.Active == t.state());
                }
                Transaction.create(heap, () -> {
                    try { 
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                    }
                });
            });
            assert(Transaction.State.Committed == t.state());
            assert(mb.getLong(0) == 12345);
            assert(mb.getLong(8) == 555);
        } 
        catch (Exception e) {
            Assert.fail("Transaction did not commit");
        }
	}
 
    @Test
	public void testNestedMixed2CommitAbort() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(Transaction.State.New == t.state());
        try {
            t.run(() -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                    assert(Transaction.State.Active == t.state());
                }
                Transaction.create(heap, () -> {
                    mb.setLong(28, 555);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (Exception e) {
            assert(Transaction.State.Aborted == t.state());
            assert(mb.getLong(0) == 0);
        }
	}
 
    @Test
	public void testNestedMixed2Aborts() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(Transaction.State.New == t.state());
        try {
            t.run(() -> {
                assert(Transaction.State.Active == t.state());
                mb.setLong(20, 12345L);
                Transaction.create(heap, () -> {
                    mb.setLong(8, 555);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (Exception e) {
            assert(Transaction.State.Aborted == t.state());
            assert(mb.getInt(20) == 0);
            assert(mb.getLong(8) == 0);
        }
	}

	@Test
	public void testNestedInstance() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t1 = Transaction.create(heap);
        Transaction t2 = Transaction.create(heap);
        assert(Transaction.State.New == t1.state());
        assert(Transaction.State.New == t2.state());
        t1.run(() -> {
            mb.setLong(0, 12345L);
            assert(Transaction.State.Active == t1.state());
            t2.run(() -> {
                mb.setLong(8, 555);
                assert(Transaction.State.Active == t2.state());
            });
        });
        assert(Transaction.State.Committed == t1.state());
        assert(Transaction.State.Committed == t2.state());
        assert(mb.getLong(0) == 12345);
        assert(mb.getLong(8) == 555);
	}
    
    @Test
	public void testNestedInstanceCommits() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t1 = Transaction.create(heap);
        Transaction t2 = Transaction.create(heap);
        assert(Transaction.State.New == t1.state());
        assert(Transaction.State.New == t2.state());
        try {
            t1.run(() -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                    assert(Transaction.State.Active == t1.state());
                }
                t2.run(() -> {
                    try { 
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                        assert(Transaction.State.Active == t2.state());
                    }
                });
            });
            assert(Transaction.State.Committed == t1.state());
            assert(Transaction.State.Committed == t2.state());
            assert(mb.getLong(0) == 12345);
            assert(mb.getLong(8) == 555);
        } 
        catch (Exception e) {
            Assert.fail("Transaction did not commit");
        }
	}
 
    @Test
	public void testNestedInstanceCommitAbort() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t1 = Transaction.create(heap);
        Transaction t2 = Transaction.create(heap);
        assert(Transaction.State.New == t1.state());
        assert(Transaction.State.New == t2.state());
        try {
            t1.run(() -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                    assert(Transaction.State.Active == t1.state());
                }
                t2.run(() -> {
                    assert(Transaction.State.Active == t2.state());
                    mb.setLong(28, 555);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (Exception e) {
            assert(Transaction.State.Aborted == t1.state());
            assert(Transaction.State.Aborted == t2.state());
            assert(mb.getLong(0) == 0);
        }
	}
 
    @Test
	public void testNestedInstanceAborts() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t1 = Transaction.create(heap);
        Transaction t2 = Transaction.create(heap);
        assert(Transaction.State.New == t1.state());
        assert(Transaction.State.New == t2.state());
        try {
            t1.run(() -> {
                assert(Transaction.State.Active == t1.state());
                mb.setLong(20, 12345L);
                t2.run(() -> {
                    mb.setLong(8, 555);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (Exception e) {
            assert(Transaction.State.Aborted == t1.state());
            assert(Transaction.State.New == t2.state());
            assert(mb.getInt(20) == 0);
            assert(mb.getLong(8) == 0);
        }
	}

	@Test
	public void testNestedStaticSeq1() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                Transaction.create(heap, () -> {
                    try {
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                    }
                });
                Transaction.create(heap, () -> {
                    try {
                        mb.setLong(26, 678910L);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(16, 678910L);
                    }
                });
            });
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 12345L);
            assert(mb.getLong(8) == 555L);
            assert(mb.getLong(16) == 678910L);
        }
	}

	@Test
	public void testNestedMixed1Seq1() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(t.isValid());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                Transaction.create(heap, () -> {
                    try {
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                    }
                });
                t.run(() -> {
                    try {
                        mb.setLong(26, 678910L);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(16, 678910L);
                        assert(Transaction.State.Active == t.state());
                    }
                });
            });
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 12345L);
            assert(mb.getLong(8) == 555L);
            assert(mb.getLong(16) == 678910L);
            assert(Transaction.State.Committed == t.state());
            assert(!t.isValid());
        }
	}

    @Test
	public void testNestedMixed2Seq1() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(t.isValid());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                t.run(() -> {
                    try {
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                        assert(Transaction.State.Active == t.state());
                    }
                });
                Transaction.create(heap, () -> {
                    try {
                        mb.setLong(26, 678910L);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(16, 678910L);
                    }
                });
            });
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 12345L);
            assert(mb.getLong(8) == 555L);
            assert(mb.getLong(16) == 678910L);
            assert(!t.isValid());
        }
	}

    @Test
	public void testNestedInstanceSeq1() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t1 = Transaction.create(heap);
        Transaction t2 = Transaction.create(heap);
        assert(t1.isValid());
        assert(t2.isValid());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                t1.run(() -> {
                    try {
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                        assert(Transaction.State.Active == t1.state());
                    }
                });
                t2.run(() -> {
                    try {
                        mb.setLong(26, 678910L);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(16, 678910L);
                        assert(Transaction.State.Active == t2.state());
                    }
                });
            });
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 12345L);
            assert(mb.getLong(8) == 555L);
            assert(mb.getLong(16) == 678910L);
            assert(!t1.isValid());
            assert(!t2.isValid());
        }
	}

	@Test
	public void testNestedStaticSeq2() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                Transaction.create(heap, () -> {
                    try {
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                    }
                });
                Transaction.create(heap, () -> {
                    mb.setLong(26, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        }   
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
        }
	}

	@Test
	public void testNestedMixed1Seq2() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(t.isValid());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                Transaction.create(heap, () -> {
                    try {
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                    }
                });
                t.run(() -> {
                    assert(Transaction.State.Active == t.state());
                    mb.setLong(26, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
            assert(!t.isValid());
        }
	}

    @Test
	public void testNestedMixed2Seq2() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(t.isValid());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                t.run(() -> {
                    try {
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                        assert(Transaction.State.Active == t.state());
                    }
                });
                Transaction.create(heap, () -> {
                    mb.setLong(26, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
            assert(!t.isValid());
        }
	}

    @Test
	public void testNestedInstanceSeq2() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t1 = Transaction.create(heap);
        Transaction t2 = Transaction.create(heap);
        assert(t1.isValid());
        assert(t2.isValid());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                t1.run(() -> {
                    try {
                        mb.setLong(28, 555);
                    } 
                    catch (IndexOutOfBoundsException e) {
                        mb.setLong(8, 555);
                        Assert.assertTrue(Transaction.State.Active == t1.state());
                    }
                });
                t2.run(() -> {
                    Assert.assertTrue(Transaction.State.Active == t2.state());
                    mb.setLong(26, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
            assert(!t1.isValid());
            assert(!t2.isValid());
        }
	}

	@Test
	public void testNestedStaticSeq3() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                Transaction.create(heap, () -> {
                    mb.setLong(28, 555);
                });
                Transaction.create(heap, () -> {
                    mb.setLong(16, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
        }
	}

	@Test
	public void testNestedMixed1Seq3() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(t.isValid());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                Transaction.create(heap, () -> {
                    mb.setLong(28, 555);
                });
                t.run(() -> {
                    assert(Transaction.State.Active == t.state());
                    mb.setLong(16, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
            assert(t.isValid()); 
        }
	}

    @Test
	public void testNestedMixed2Seq3() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(t.isValid());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                t.run(() -> {
                    assert(Transaction.State.Active == t.state());
                    mb.setLong(28, 555);
                });
                Transaction.create(heap, () -> {
                    mb.setLong(16, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
            assert(!t.isValid());
        }
	}

    @Test
	public void testNestedInstanceSeq3() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t1 = Transaction.create(heap);
        Transaction t2 = Transaction.create(heap);
        assert(t1.isValid());
        assert(t2.isValid());
        try {
            Transaction.create(heap, () -> {
                try {
                    mb.setLong(20, 12345L);
                } 
                catch (IndexOutOfBoundsException e) {
                    mb.setLong(0, 12345L);
                }
                t1.run(() -> {
                    Assert.assertTrue(Transaction.State.Active == t1.state());
                    mb.setLong(28, 555);
                });
                t2.run(() -> {
                    Assert.assertTrue(Transaction.State.Active == t2.state());
                    mb.setLong(16, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
            assert(!t1.isValid());
            assert(t2.isValid()); 
        }
	}

	@Test
	public void testNestedStaticSeq4() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        try {
            Transaction.create(heap, () -> {
                mb.setLong(20, 12345L);
                Transaction.create(heap, () -> {
                    mb.setLong(8, 555);
                });
                Transaction.create(heap, () -> {
                    mb.setLong(16, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
        }
	}

	@Test
	public void testNestedMixed1Seq4() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(t.isValid());
        try {
            Transaction.create(heap, () -> {
                mb.setLong(20, 12345L);
                Transaction.create(heap, () -> {
                    mb.setLong(8, 555);
                });
                t.run(() -> {
                    assert(Transaction.State.Active == t.state());
                    mb.setLong(16, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
            assert(t.isValid()); 
        }
	}

    @Test
	public void testNestedMixed2Seq4() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t = Transaction.create(heap);
        assert(t.isValid());
        try {
            Transaction.create(heap, () -> {
                mb.setLong(20, 12345L);
                t.run(() -> {
                    assert(Transaction.State.Active == t.state());
                    mb.setLong(8, 555);
                });
                Transaction.create(heap, () -> {
                    mb.setLong(16, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
            assert(t.isValid()); // weird
        }
	}

    @Test
	public void testNestedInstanceSeq4() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t1 = Transaction.create(heap);
        Transaction t2 = Transaction.create(heap);
        assert(t1.isValid());
        assert(t2.isValid());
        try {
            Transaction.create(heap, () -> {
                mb.setLong(20, 12345L);
                t1.run(() -> {
                    Assert.assertTrue(Transaction.State.Active == t1.state());
                    mb.setLong(8, 555);
                });
                t2.run(() -> {
                    Assert.assertTrue(Transaction.State.Active == t2.state());
                    mb.setLong(16, 678910L);
                });
            });
            Assert.fail("Exception not thrown");
        } 
        catch (IndexOutOfBoundsException e) {
            assert(mb.getLong(0) == 0);
            assert(mb.getLong(8) == 0);
            assert(mb.getLong(16) == 0);
            assert(t1.isValid()); //weird
            assert(t2.isValid()); //weird
        }
	}

    @Test
	public void testNestedSelf() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
        Transaction t1 = Transaction.create(heap); 
        assert(t1.isValid());
        t1.run(() -> {
            mb.setLong(0, 12345L);
            t1.run(() -> {
                mb.setLong(8, 555);
            });
            mb.setLong(16, 678910L);
        });
        assert(mb.getLong(0) == 12345);
        assert(mb.getLong(8) == 555);
        assert(mb.getLong(16) == 678910);
        assert(!t1.isValid());
	}

	public void testNestedInvalid() {
		heap = TestVars.createTransactionalHeap();
		TransactionalMemoryBlock mb = heap.allocateMemoryBlock(24);
		Transaction t1 = Transaction.create(heap); 
		assert(t1.isValid());
		try {
		    t1.run(() -> {
                mb.setLong(0, 12345L);
                mb.setLong(16, 678910L);
		    });
		    assert(!t1.isValid());
		    t1.run(() -> {
			    mb.setLong(8, 555);
		    });
		} 
        catch(TransactionException e) {
		    assert(mb.getLong(0) == 12345L);
		    assert(mb.getLong(8) == 0);
		    assert(mb.getLong(16) == 678910L);
		    assert(!t1.isValid());
		}
	}
}
