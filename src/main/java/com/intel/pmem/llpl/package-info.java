/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

/**
 * Provides classes necessary to create and access heaps of persistent memory 
 * for both volatile and persistent use. <br><br>

 * Heaps of memory can be created by calling a heap create method, passing a path argument that uniquely identifies a 
 * heap.  This path argument is also used when calling a heap open method to reopen a heap after a restart.<br><br>
 * 
 * Blocks of memory can be allocated by calling one of the memory allocation methods on a heap. There is a choice of 
 * regular allocations which use a header to store the allocation size, or compact allocations which do not store 
 * the allocation size, thus saving space.<br><br>
 *
 * Access to a specific block of allocated memory is done using a memory accessor object.  Two kinds of memory accessors are available: 
 * fixed-position AnyMemoryBlock instances and repositionable AnyAccessor instances.  These memory accessor objects are all instances of 
 * the MemoryAccessor base class.<br><br>
 
 * Allocated blocks of memory are uniqely identified within a heap using a Java long value called a memory "handle". After a restart, 
 * previously allocated memory can be re-accessed by binding a handle to a memory accessor 
 * (see {@link AnyHeap#memoryBlockFromHandle} and {@link AnyAccessor#handle(long)}).  
 * Handles can be stored as long values within other memory blocks in order to link blocks. A handle can also be stored in a special 
 * heap "root" location to provide bootstrap access to an application's primary data. <br><br>
 *
 * Memory can be freed by calling the free method on a memory accessor.<br><br>
 * <p>
 * The package offers three kinds of heaps and associated accessors that together provide allocation, deallocation,
 * and reading and writing of heap memory:<br><br>
 * 1. {@link com.intel.pmem.llpl.Heap} / {@link com.intel.pmem.llpl.MemoryBlock} / {@link com.intel.pmem.llpl.Accessor} 
 * -- Suitable for volatile use of persistent memory or persistent use with custom data integity policies.<br>
 * 2. {@link com.intel.pmem.llpl.PersistentHeap} / {@link com.intel.pmem.llpl.PersistentMemoryBlock} / {@link com.intel.pmem.llpl.PersistentAccessor} 
 * -- Using this heap / memory accessors gives compile-time knowledge that all changes to persistent 
 * memory are done durably. Allocations and other modifications to persistent memory may optionally be done 
 * transactionally.<br>  
 * 3. {@link com.intel.pmem.llpl.TransactionalHeap} / {@link com.intel.pmem.llpl.TransactionalMemoryBlock} / {@link com.intel.pmem.llpl.TransactionalAccessor}
 * -- Using this heap / memory accessors gives compile-time knowledge that all changes to persistent memory are 
 * done transactionally.<br><br>
 *
 * The abstract classes {@code AnyHeap}, {@code MemoryAccessor}, {@code AnyMemoryBlock}, and {@code AnyAccessor} can assist 
 * in writing code that will work with different kinds of heaps and accessors.
 *
 * @since 1.0
 * @see com.intel.pmem.llpl
 */

package com.intel.pmem.llpl;
