/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

/**
 * Thrown to indicate that a transaction failed to execute successfully.  
 */
public class TransactionException extends RuntimeException {
    public TransactionException(String message) {
        super(message);
    }

	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}
}
