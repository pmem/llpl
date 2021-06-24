/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

public class Util {

    private static boolean loaded = false;
    private static String extension = "";

    public static boolean isLoaded() {
        if (loaded) return true;
        try {
            System.loadLibrary("llpl");
            loaded = true;
        } catch (UnsatisfiedLinkError e) { 
            // Could not load native library from "java.library.path"
            // Next, try loading from the classpath
            loaded = false;
        }
        return loaded;
    }

    static String getLibName() {
        return "/com/intel/pmem/llpl/" + getOSName() + "/" + getOSArch() + "/" + "libllpl" + extension;
    }

    static String getOSName() {
        String os = System.getProperty("os.name");
        String ret;
        if (os.contains("Linux")) {
            ret = "linux";
            extension = ".so";
        }
        else throw new UnsupportedOperationException("Operating System is not supported");
        return ret;
    }

    static String getOSArch() {
        return System.getProperty("os.arch");
    }

    public static void loadLibrary() {
        if (isLoaded()) return;
        String libName = getLibName();
        File nativeLib = null;
        try (InputStream in = Util.class.getResourceAsStream(libName)) {
            nativeLib = File.createTempFile("libllpl", extension);
            try (FileOutputStream out = new FileOutputStream(nativeLib)) {
                byte[] buf = new byte[4096];
                int bytesRead;
                while(true) {
                    bytesRead = in.read(buf);
                    if (bytesRead == -1) break;
                    out.write(buf, 0, bytesRead);
                }
            }
            System.load(nativeLib.getAbsolutePath());
            loaded = true;
        } 
        catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load native llpl library");
        }
        finally {
            if (nativeLib != null) nativeLib.deleteOnExit();
        }
    }
}
