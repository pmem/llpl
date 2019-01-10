/**
 * Provides the classes necessary to create and access heaps of persistent memory 
 * for both volatile and persistent use. 
 * <p>
 * The package offers three kinds of heaps and associated memory blocks that together provide allocation, deallocation,
 * and reading and writing of heap memory:<br>
 * 1. {@link lib.llpl.Heap} / {@link lib.llpl.MemoryBlock} -- Suitable for volatile use of persistent memory or persistent use 
 * with custom data integity policies.<br>
 * 2. {@link lib.llpl.PersistentHeap} / {@link lib.llpl.PersistentMemoryBlock} -- Using this heap / memory block gives compile-time knowledge that all changes to persistent 
 * memory are done durably.<br>  Allocations and other modifications to persistent memory may optionally be done transactionally.<br>  
 * 3. {@link lib.llpl.TransactionalHeap} / {@link lib.llpl.TransactionalMemoryBlock} -- Using this 
 * heap / memory block gives compile-time knowledge that all changes to persistent memory are done transactionally.<br>
 * <br>
 *
 * @since 1.0
 * @see lib.llpl
 */

package lib.llpl;