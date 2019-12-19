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

/*
                                        Committed
 State transitions are New -> Active  /          
                                      \          
                                        Aborted
*/
/**
 * Provides a scope for grouping transactional allocations, deallocations, and writes into a single transactional
 * operation.  A single transaction is restricted to a single heap.  Semantically, nested transactions are flattened into
 * a single, thread-local transaction.  Any uncaught exceptions thrown from a transaction body will cause the 
 * transaction to abort and to roll-back any transactional memory modifications already made during execution of
 * the transaction body.
 */
public final class Transaction {
    static {
        System.loadLibrary("llpl");
    }

    private State state; 
    private int depth;
    private final boolean doStart;
    private final AnyHeap heap;
    private boolean isValid;

    /**
     * The states through which a transaction can move.  Successful transactions traverse New - Active - Committed.
     */
    enum State {New, Active, Committed, Aborted}

    Transaction(AnyHeap heap) {
        this(heap, true);
    }

    Transaction(AnyHeap heap, boolean doStart) {
        this.heap = heap;
        this.state = State.New;    
        this.isValid = true;
        this.doStart = doStart;    
    }

    /**
    * Creates a new transaction suitable for modification to the supplied heap.  Returns the new transaction.
    * @param heap the heap associated with the transaction
    * @return the current state of this transaction
    */
    public static Transaction create(AnyHeap heap) {
        return new Transaction(heap, true);
    }

    /**
     * Checks whether this transaction object is in a valid state for use. A transaction is marked invalid, for example, after it commits or aborts. 
     * @return true if this transaction is valid for use
     */
    public boolean isValid() {
        return isValid; 
    }

    State state() {
        return state;
    }

    /**
    * Creates a new transaction and executes the supplied body function within the transaction. Transactional modifications are limited to specified heap.
    * @param heap the heap associated with the transaction
    * @param body a function that represents the transaction body
    */
    public static void create(AnyHeap heap, Runnable body) {
        create(heap, () -> {body.run(); return (Void)null;});
    }

    /**
    * Creates a new transaction and executes the supplied body function within the transaction. Transactional modifications are limited to specified heap.
    * @param heap the heap associated with the transaction
    * @param body a function that represents the transaction body
    * @param <T> the return type of the supplied function
    * @throws IllegalStateException if there was an error starting the transaction
    * @return the value returned by the body function
    */
    public static <T> T create(AnyHeap heap, Supplier<T> body) {
        return internalRun(new Transaction(heap), null, (Range r) -> {return body.get();});
    }

    /**
    * Transactionally executes the supplied body function as part of this {@code Transaction}. 
    * Transactional modifications are limited to heap associated with this transaction.
    * @param body a function that represents the transaction body
    * @throws IllegalStateException if there was an error starting the transaction
    * @throws TransactionException if the tranaction is not active
    */
    public void run(Runnable body) {
        run(null, (Range r) -> {body.run(); return (Void)null;});
    }

    /**
    * Transactionally executes the supplied body function as part of this {@code Transaction}. 
    * @param body a function that represents the transaction body
    * @param <T> the return type of the supplied fuction
    * @throws IllegalStateException if there was an error starting the transaction
    * @throws TransactionException if the tranaction is not active
    * @return the value returned by the body function
    */
    public <T> T run(Supplier<T> body) {
        return run(null, (Range r) -> {return body.get();});
    }

    <T> T run(Range range, Function<Range, T> body) {
        return internalRun(this, range, body);
    }

    private static <T> T internalRun(Transaction transaction, Range range, Function<Range, T> body) {
        if (transaction.state == State.New) {
            if (transaction.doStart) {
                int result = nativeStartTransaction(transaction.heap.poolHandle());
                if (result == -1) throw new IllegalStateException("Error starting transaction.");
            }
            transaction.state = State.Active;
        }
        if (transaction.state != State.Active) throw new TransactionException("Transaction not active");
        transaction.depth++;
        T result = null;
        try {
            if (range != null) range.addToTransactionNoCheck();
            result = body.apply(range);
        }
        catch (Throwable t) {
            if (transaction.state == State.Active) nativeAbortTransaction();
            transaction.state = State.Aborted;
            transaction.isValid = false;
            throw t;
        }
        finally {
            if (transaction.state == Transaction.State.Active && transaction.depth == 1) {
                nativeCommitTransaction();
                transaction.state = State.Committed;
                transaction.isValid = false;
            }
            transaction.depth--;
        }
        return result;
    }

    static void checkTransactionActive(boolean expected) {
        boolean active = isTransactionActive();
        if (active != expected) throw new IllegalStateException(expected ? "Expected active transaction." : "Unexpected active transaction.");
    }

    static boolean isTransactionActive() {
        return nativeTransactionState() == 2;
    }

    private static native int nativeStartTransaction(long poolHandle);
    private static native void nativeCommitTransaction();
    private static native void nativeAbortTransaction();
    private static native int nativeTransactionState();
}
