/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

/**
 * Provides the classes necessary to create and access heaps of persistent memory 
 * for both volatile and persistent use. <br><br>Accessing persistent memory starts with creation of one 
 * of the three available kinds of heaps.  Heap factory methods accept a path arguemnt that uniquely identifies a 
 * heap and can be used to reopen a heap after a restart.  Once a heap is available, the next step 
 * is allocating a block of memory from a heap. The returned memory block object can then be used to read and 
 * write values within the block.<br><br>
 * MemoryBlocks are uniqely identified within a heap using an available memory block handle. After a restart, 
 * previously allocated memory can be re-accessed by passing a handle to a reopened heap.  Handles can be stored 
 * within other memory blocks in order to link blocks. A handle can also be stored in a special heap root location
 * to provide bootstrapped access to an application's primary data. Memory can be deallocated using a memory 
 * block's {@code free()} method.  
 * <p>
 * The package offers three kinds of heaps and associated memory blocks that together provide allocation, deallocation,
 * and reading and writing of heap memory:<br>
 * 1. {@link com.intel.pmem.llpl.Heap} / {@link com.intel.pmem.llpl.MemoryBlock} -- Suitable for volatile use of persistent memory or persistent use 
 * with custom data integity policies.<br>
 * 2. {@link com.intel.pmem.llpl.PersistentHeap} / {@link com.intel.pmem.llpl.PersistentMemoryBlock} -- Using this heap / memory block gives compile-time knowledge that all changes to persistent 
 * memory are done durably.<br>  Allocations and other modifications to persistent memory may optionally be done transactionally.<br>  
 * 3. {@link com.intel.pmem.llpl.TransactionalHeap} / {@link com.intel.pmem.llpl.TransactionalMemoryBlock} -- Using this 
 * heap / memory block gives compile-time knowledge that all changes to persistent memory are done transactionally.<br>
 * <br>
 *
 * @since 1.0
 * @see com.intel.pmem.llpl
 */

package com.intel.pmem.llpl;
