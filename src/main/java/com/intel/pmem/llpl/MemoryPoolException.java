/* 
 * Copyright (C) 2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

/**
 * Thrown to indicate that a {@code MemoryPool} operation failed to execute successfully.  
 * 
 * @since 1.2
 */
public class MemoryPoolException extends RuntimeException {
	public MemoryPoolException(String message) {
		super(message);
	}

	public MemoryPoolException(String message, Throwable cause) {
		super(message, cause);
	}
}
