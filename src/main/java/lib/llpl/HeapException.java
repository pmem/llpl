/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

/**
 * Thrown to indicate that a heap operation failed to execute successfully.  
 */
public class HeapException extends RuntimeException {
	public HeapException(String message) {
		super(message);
	}

	public HeapException(String message, Throwable cause) {
		super(message, cause);
	}
}
