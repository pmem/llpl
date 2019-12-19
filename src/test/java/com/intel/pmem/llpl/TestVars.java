/* 
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package com.intel.pmem.llpl;

import com.intel.pmem.llpl.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.testng.Assert;

public class TestVars {
    public static String HEAP_USER_PATH;
	public static long TOTAL_SIZE;
    public static long MULTIPLE_HEAP_SIZE;
    public static long HEAP_SIZE_3G;
    public static long MEMORY_BLOCK_SIZE_2G;
    public static long INVALID_LARGE_MEM_BLOCK_SIZE;
	public static boolean ISDAX;

	public static final int NUM_HEAPS = 10;
	public static final int NUM_MIXED_HEAPS = 3;
	public static String HEAP_NAME = "custom";
	public static final long HEAP_SIZE = 10 * 1024 * 1024;
	public static final String INVALID_HEAP_PATH = "INVALID";
	public static final String POOL_SET_FILE = "poolset.txt";

	public static final long SMALL_HEAP_SIZE = 5 * 1024 * 1024;
	public static final long NEGATIVE_HEAP_SIZE = 10L * 1024 * 1024 * -1;
	public static final long MEMORY_BLOCK_SIZE = 1024;
	public static final long MEMORY_BLOCK_SIZE_100MB = 100L * 1024 * 1024;
	public static final long NEGATIVE_MEMORY_BLOCK_SIZE = 1024 * (-1);
	public static final boolean NON_TRANSACTIONAL = false;
	public static final boolean TRANSACTIONAL = true;
	public static final int MEM_BLOCK_OFFSET = 12;
	public static final int MEM_BLOCK_WRITE_DATA = 12345;
	public static final short SHORT_DATA = 2;
	public static final int INT_DATA = 4;
	public static final long LONG_DATA = 6L;
	public static final byte BYTE_DATA = -1;
	public static final String BLOCK_HANDLE_FILE = "block_handle.txt";
	public static final int HEAP_ROOT_DATA = 100;

	public static Heap createHeap() {
		if (ISDAX) return Heap.createHeap(HEAP_USER_PATH);		
		Assert.assertTrue(createFolder(HEAP_USER_PATH + HEAP_NAME));
		return Heap.createHeap(HEAP_USER_PATH + HEAP_NAME); 
	}

	public static PersistentHeap createPersistentHeap() {
		if (ISDAX) return PersistentHeap.createHeap(HEAP_USER_PATH);		
		Assert.assertTrue(createFolder(HEAP_USER_PATH + HEAP_NAME));
		return PersistentHeap.createHeap(HEAP_USER_PATH + HEAP_NAME); 
	}

	public static TransactionalHeap createTransactionalHeap() {
		if (ISDAX) return TransactionalHeap.createHeap(HEAP_USER_PATH);		
		Assert.assertTrue(createFolder(HEAP_USER_PATH + HEAP_NAME));
		return TransactionalHeap.createHeap(HEAP_USER_PATH + HEAP_NAME); 
	}

	public static boolean createFolder(String path) {
		boolean ret = false;
		File file = new File(path);
		if (file.exists()) {
			System.out.println("FOLDER EXISTS");
			return ret;
		}
		ret = file.mkdir();
		if (!ret)
			System.out.println("COULDNT NOT CREATE DIRECTORY");
		return ret;
	}

	public static boolean createFile(String path) {
		boolean ret = false;
		File file = new File(path);
		if (file.exists()) {
			System.out.println("FILE EXISTS");
			return ret;
		}
		try {
			ret = file.createNewFile();
		} 
        catch (IOException e) {
			e.printStackTrace();
		}
		if (!ret)
			System.out.println("COULDNT NOT CREATE FILE");
		return ret;
	}

	public static boolean daxCleanUp() {
		return (AnyHeap.removePool(HEAP_USER_PATH) == 0) ? true : false;
	}

	public static boolean cleanUp(String path) {
		boolean ret = false;
		Path pathToBeDeleted = new File(path).toPath();
		if (Files.exists(pathToBeDeleted)) {
			try {
				Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			} 
            catch (IOException e) {
			    throw new RuntimeException("An Error occured during files cleanup");	
			}
		}
		if (!Files.exists(pathToBeDeleted))
			ret = true;
		return ret;
	}
}
