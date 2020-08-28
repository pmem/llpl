/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

/**
 * Provides classes necessary to create and access heaps in persistent memory 
 * for both volatile and persistent use. <br><br>

 * Heaps in persistent memory can be created by calling a {@code createHeap} factory method, passing a path argument that uniquely identifies a 
 * heap.  This path argument is also used when calling an {@code openHeap} factory method to reopen a heap after a restart.<br><br>
 * 
 * Blocks of memory can be allocated by calling one of the memory allocation methods on a heap. There is a choice of 
 * regular allocations which store the allocation size, or compact allocations which do not store 
 * the allocation size, thus saving space.<br><br>
 *
 * Specific blocks of allocated memory may be accessed using a memory accessor object.  Two kinds of memory accessors are available: 
 * fixed-position {@code AnyMemoryBlock} instances and repositionable {@code AnyAccessor} instances.  These memory accessor objects are all instances of 
 * the {@code MemoryAccessor} base class.<br><br>
 
 * Allocated blocks of memory are uniqely identified within a heap using a Java {@code long} value called a "handle". After a restart, 
 * previously-allocated memory can be re-accessed by binding a handle to a memory accessor 
 * (see {@link AnyHeap#memoryBlockFromHandle} and {@link AnyAccessor#handle(long)}).  
 * Handles can be stored as {@code long} values within other memory blocks in order to link blocks. A handle can also be stored in a special 
 * "root" location on a heap to provide bootstrap access to an application's primary data (see {@link AnyHeap#setRoot(long)}).<br><br>
 *
 * Memory can be freed by calling the {@code freeMemory} method on a memory accessor (see {@link MemoryAccessor#freeMemory()}).<br><br>
 * <p>
 * The package offers three kinds of heaps and associated accessors. Together, these provide the ability to allocate, deallocate, read,
 * and write heap memory:<br><br>
 * 1. {@link com.intel.pmem.llpl.Heap} / {@link com.intel.pmem.llpl.MemoryBlock} / {@link com.intel.pmem.llpl.Accessor} 
 * -- Suitable for volatile use of persistent memory or persistent use with custom data integity policies.<br>
 * 2. {@link com.intel.pmem.llpl.PersistentHeap} / {@link com.intel.pmem.llpl.PersistentMemoryBlock} / {@link com.intel.pmem.llpl.PersistentAccessor} 
 * -- Using this heap and those memory accessors gives compile-time knowledge that all changes to persistent 
 * memory are done durably. Allocations and other modifications to persistent memory may, optionally, be done 
 * transactionally.<br>  
 * 3. {@link com.intel.pmem.llpl.TransactionalHeap} / {@link com.intel.pmem.llpl.TransactionalMemoryBlock} / {@link com.intel.pmem.llpl.TransactionalAccessor}
 * -- Using this heap and those memory accessors gives compile-time knowledge that all changes to persistent memory are 
 * done transactionally.<br><br>
 *
 * The abstract classes {@code AnyHeap}, {@code MemoryAccessor}, {@code AnyMemoryBlock}, and {@code AnyAccessor} can assist 
 * in writing code that will work with different kinds of heaps and accessors.
 * <p> 
 * See the LLPL examples directory for some ideas on how to write programs using LLPL. 
 *
 *
 * @since 1.0
 * @see com.intel.pmem.llpl
 */

package com.intel.pmem.llpl;
