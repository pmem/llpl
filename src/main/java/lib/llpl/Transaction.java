/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

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
 * transaction to abort and to roll-back transactional memory modifications already made during execution of
 * the body.
 */
public final class Transaction {
    static {
        System.loadLibrary("llpl");
    }

    private State state; 
    private int depth;
    private final boolean doStart;
    private final AnyHeap heap;

    /**
     * The states through which a transaction can move.  Successful transactions traverse New - Active - Committed.
     */
    public enum State {New, Active, Committed, Aborted}

    // TODO: Consider using public factory method and making constructors private
    public Transaction(AnyHeap heap) {
        this(heap, true);
    }

    Transaction(AnyHeap heap, boolean doStart) {
        this.heap = heap;
        this.state = State.New;    
        this.doStart = doStart;    
    }

    /**
    * Returns the current state of this transaction.
    * @return the current state of this transaction
    */
    public State state() {
        return state;
    }

    /**
    * Transactionally executes the supplied body. Transactional modifications are limited to specified heap.
    * @param heap the heap associated with the transactional execution
    * @param body the function representing the transaction body function
    */
    public static void run(AnyHeap heap, Runnable body) {
        run(heap, () -> {body.run(); return (Void)null;});
    }

    /**
    * Transactionally executes the supplied body. Transactional modifications are limited to specified heap.
    * @param heap the heap associated with the transactional execution
    * @param body the function representing the transaction body function
    * @param <T> the return type of the supplied fuction
    * @return the value returned by the body function
    */
    public static <T> T run(AnyHeap heap, Supplier<T> body) {
        return internalRun(new Transaction(heap), null, (Range r) -> {return body.get();});
    }

    /**
    * Transactionally executes the supplied body as part of this {@code Transaction}. 
    * Transactional modifications are limited to heap associated with this transaction.
    * @param body the function representing the transaction body function
    */
    public void run(Runnable body) {
        run(null, (Range r) -> {body.run(); return (Void)null;});
    }

    /**
    * Transactionally executes the supplied body as part of this {@code Transaction}. 
    * Transactional modifications are limited to heap associated with this transaction.
    * @param body the function representing the transaction body function
    * @param <T> the return type of the supplied fuction
    * @return the value returned by the body function
    */
    public <T> T run(Supplier<T> body) {
        return run(null, (Range r) -> {return body.get();});
    }

    void run(Range range, Consumer<Range> op) {
        run(range, (Range r) -> {op.accept(r); return (Void)null;});
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
        if (range != null) range.addToTransactionNoCheck();
        try {
            result = body.apply(range);
        }
        catch (Throwable t) {
            // System.out.format("Transaction %s, caught %s, state = %s, depth = %d\n", transaction, t, transaction.state, transaction.depth);
            if (transaction.state == State.Active) nativeAbortTransaction();
            transaction.state = State.Aborted;
            throw t;
        }
        finally {
            if (transaction.state == Transaction.State.Active && transaction.depth == 1) {
                nativeCommitTransaction();
                transaction.state = State.Committed;
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
    private static native void nativeEndTransaction();
    private static native void nativeAbortTransaction();
    private static native int nativeTransactionState();
}
