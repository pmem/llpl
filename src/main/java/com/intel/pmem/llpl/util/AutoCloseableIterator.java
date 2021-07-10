/*
 * Copyright (C) 2021 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl.util;

import java.util.Iterator;

/**
 * An {@code Iterator} that is {@code AutoCloseable}.
 * @since 1.2
 */
public interface AutoCloseableIterator<E> extends Iterator<E>, AutoCloseable {

}
