/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

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
