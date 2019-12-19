/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.Consumer;

abstract class AbstractTransactionalMemoryBlock extends AnyMemoryBlock { 
    private static final long METADATA_SIZE = 8;

    AbstractTransactionalMemoryBlock(TransactionalHeap heap, long size, boolean bounded) {
        super(heap, size, bounded, true);
    }

    AbstractTransactionalMemoryBlock(TransactionalHeap heap, long poolHandle, long offset, boolean bounded) {
        super(heap, offset, bounded);
    }

    /**
    * Returns the heap from which this memory block was allocated.
    * @return the {@code TransactionalHeap} from which this memory block was allocated
    */
    public TransactionalHeap heap() {
        return (TransactionalHeap)super.heap();
    }

    @Override
    abstract long metadataSize();

    /**
    * Deallocates this memory block.
    * @throws HeapException if the memory block could not be deallocated
    */
    public void free() {
        heap().freeMemoryBlock(this);
    }

    /**
     * {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
    @Override
    public void setByte(long offset, byte value) {
        super.transactionalSetByte(offset, value);
    }

    /**
     * {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
    @Override
    public void setShort(long offset, short value) {
        super.transactionalSetShort(offset, value);
    }

    /**
     * {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
    @Override
    public void setInt(long offset, int value) {
        super.transactionalSetInt(offset, value);
    }

    /**
     * {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
    @Override
    public void setLong(long offset, long value) {
        super.transactionalSetLong(offset, value);
    }

    /**
     * {@inheritDoc}  
     * @param srcBlock {@inheritDoc}
     * @param srcOffset {@inheritDoc}
     * @param dstOffset {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc} 
     * @throws IllegalStateException {@inheritDoc}
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
    @Override
    public void copyFromMemoryBlock(AnyMemoryBlock srcBlock, long srcOffset, long dstOffset, long length) {
        super.transactionalCopyFromMemoryBlock(srcBlock, srcOffset, dstOffset, length);
    }

    /**
     * {@inheritDoc} 
     * @param srcArray {@inheritDoc}
     * @param srcOffset {@inheritDoc}
     * @param dstOffset {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */
    @Override
    public void copyFromArray(byte[] srcArray, int srcOffset, long dstOffset, int length) {
        super.transactionalCopyFromArray(srcArray, srcOffset, dstOffset, length);
    }

    /**
     *{@inheritDoc}
     * @param value {@inheritDoc}
     * @param offset {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc} 
     * @throws IllegalStateException {@inheritDoc}
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    @Override
    public void setMemory(byte value, long offset, long length) {
        super.transactionalSetMemory(value, offset, length);
    }

    /**
     * Transactionally executes the supplied {@code Function}, passing in a {@code Range} object suitable for modifying bytes in 
     * the specified range of offsets within this memory block.  
     * @param startOffset the starting offset of the range
     * @param rangeLength the number of bytes in the range
     * @param op the function to execute
     * @param <T> the return type of the supplied fuction
     * @return the object returned from the supplied function
     * @throws IndexOutOfBoundsException if the the specified range of bytes is not within this memory block's bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public <T> T withRange(long startOffset, long rangeLength, Function<Range, T> op) {
        return super.transactionalWithRange(startOffset, rangeLength, op);
    }

    /**
     * Transactionally executes the supplied {@code Consumer} function, passing in a {@code Range} object
     * suitable for modifying bytes in the specified range of offsets within this memory block.
     * Writing using the Range object in ranged operations such as this can be more efficient than
     * calling separate write methods on the memory block. 
     * @param startOffset the starting offset of the range
     * @param rangeLength the number of bytes in the range
     * @param op the function to execute
     * @throws IndexOutOfBoundsException if the the specified range of bytes is not within this memory block's bounds
     * @throws TransactionException if a transaction was not active and a new transaction could not be created
     */    
    public void withRange(long startOffset, long rangeLength, Consumer<Range> op) {
        super.transactionalWithRange(startOffset, rangeLength, op);
    }
}
