/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package lib.llpl;

public class TransactionError extends Error {
    public TransactionError(String message) {
        super(message);
    }
}
