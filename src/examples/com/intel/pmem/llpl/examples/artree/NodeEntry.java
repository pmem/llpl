/* 
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl.examples.artree;

public class NodeEntry {
    byte radix;
    Node child;

    public NodeEntry(byte radix, Node child) {
        this.radix = radix;
        this.child = child;
    }
}
