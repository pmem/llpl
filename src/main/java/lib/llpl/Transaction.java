/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

import java.util.function.Supplier;

public class Transaction {
    static {
        System.loadLibrary("llpl");
    }

    private static ThreadLocal<Transaction> tlTransaction = new ThreadLocal<>();
    private State state; 
    private int depth;

    public enum State {None, Active, Committed, Aborted}

    public Transaction(Heap heap) {
        nativeStartTransaction(heap.poolAddress());
        state = State.Active;
    }

    // static run methods track transaction with ThreadLocal
    public static void run(Heap heap, Runnable body) {
        run(heap, () -> {body.run(); return (Void)null;});
    }

    public static <T> T run(Heap heap, Supplier<T> body) {
        Transaction transaction = tlTransaction.get();
        if (transaction == null || transaction.state != State.Active) {
            tlTransaction.set(transaction = new Transaction(heap));
        }
        return transaction.execute(body);
    }

    public void execute(Runnable body) {
        execute(() -> {body.run(); return (Void)null;});
    }

    public <T> T execute(Supplier<T> body) {
        if (state != State.Active) throw new TransactionError("Transaction not active");
        depth++;
        T result = null;
        try {
            result = body.get();
        }
        catch (Throwable t) {
            state = State.Aborted;
            if (t instanceof PersistenceException) {
                nativeEndTransaction();
            }
            throw t;
        }
        finally {
            if (state == Transaction.State.Active && depth == 1) {
                state = State.Committed;
                nativeCommitTransaction();
                nativeEndTransaction();            
            }
            depth--;
        }
        return result;
    }

    private static native void nativeStartTransaction(long poolAddress);
    private static native void nativeCommitTransaction();
    private static native void nativeEndTransaction();
    private static native void nativeAbortTransaction();
}
