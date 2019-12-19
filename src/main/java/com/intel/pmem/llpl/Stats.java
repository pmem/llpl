/*
 * Copyright (C) 2018-2019 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

package com.intel.pmem.llpl;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Map;
import java.util.Comparator;

class Stats {
    public static Stats current;

    static {
        current = new Stats();
    }

    public AllocationStats allocStats;

    private Stats() {
        allocStats = new AllocationStats(AllocationStats.Key.NPV);
     }

    public static class AllocationStats {
        private ConcurrentSkipListMap<Key, Record> data;

        private static class Key implements Comparable<Key> {
            private String className;
            private long pSize;
            private long vSize;

            public Key(String className, long pSize, long vSize) {
                this.className = className;
                this.pSize = pSize;
                this.vSize = vSize;
            }

            public int compareTo(Key that) {
                return NPV.compare(this, that);
            }

            public static Comparator<Key> NPV = (Key k1, Key k2) -> {
                int nCompare = k1.className.compareTo(k2.className);
                if (nCompare != 0) return nCompare;
                int vCompare = Long.compare(k1.vSize, k2.vSize);
                if (vCompare != 0) return vCompare;
                return Long.compare(k1.pSize, k2.pSize);
            };

            public static Comparator<Key> VPN = (Key k1, Key k2) -> {
                int vCompare = Long.compare(k1.vSize, k2.vSize);
                if (vCompare != 0) return vCompare;
                int pCompare = Long.compare(k1.pSize, k2.pSize);
                if (pCompare != 0) return pCompare;
                return k1.className.compareTo(k2.className);
            };
    

            public static Comparator<Key> PNV = (Key k1, Key k2) -> {
                int pCompare = Long.compare(k1.pSize, k2.pSize);
                if (pCompare != 0) return pCompare;
                int vCompare = Long.compare(k1.vSize, k2.vSize);
                if (vCompare != 0) return vCompare;
                return k1.className.compareTo(k2.className);
            };
        }

        private static class Record {
            private long instances;
        }

        public AllocationStats(Comparator<Key> comparator) {
            this.data = new ConcurrentSkipListMap<>(comparator);
        }

        public AllocationStats() {
            this(Key.NPV);
        }

        public void clear() {data.clear();}

        public void update(String className, long pSize, long vSize, long count) {
            Key key = new Key(className, pSize, vSize);
            Record current = data.get(key);
            if (current == null) {
                current = new Record();
                data.put(key, current);
            }
            current.instances += count;
        }

        public String toString() {
            StringBuilder buff = new StringBuilder();
            long totalInstances = 0;
            long totalPersistentBytes = 0;
            long totalVolatileBytes = 0;
            buff.append("                                                  Allocation Stats\n");
            buff.append("---------------------------------------------------------------------------------------------------------------------------\n");         
            buff.append("                                                                Persistent       Persistent    Volatile          Volatile  \n");
            buff.append("Class Name                                          Instances   bytes each       bytes total   bytes each       bytes total\n");
            buff.append("---------------------------------------------------------------------------------------------------------------------------\n");
            for (Map.Entry<Key, Record> entry : data.entrySet()) {
                Key key = entry.getKey();
                String cls = key.className;
                long persistentUnitSize = key.pSize;
                long volatileUnitSize = key.vSize;
                Record rec = entry.getValue();
                long clsPersistentTotal = rec.instances * persistentUnitSize;
                long clsVolatileTotal =  rec.instances * volatileUnitSize;
                totalInstances += rec.instances;
                totalPersistentBytes += clsPersistentTotal;
                totalVolatileBytes += clsVolatileTotal;
                buff.append(String.format("%-43s%,18d%,13d%,18d%,13d%,18d\n", cls, rec.instances, persistentUnitSize, clsPersistentTotal, volatileUnitSize, clsVolatileTotal));
            }
            buff.append("                                                -------------                ---------------                ---------------\n");
            buff.append(String.format("                                           %,18d             %,18d             %,18d\n", totalInstances, totalPersistentBytes, totalVolatileBytes)); 
            return buff.toString();
        }
    }

    public static void printAllocationStats() {printAllocationStats(current);}

    public static void printAllocationStats(Stats stats) {
        System.out.println(stats.allocStats.toString());
    }

    public static void printStats() {
        printStats(null, current);
    }

    public static void printStats(String header, Stats stats) {
        if (header != null) {
            System.out.format("\n======= %s ===========\n\n", header); 
        }
        printAllocationStats(stats);  // uncomment for allocation stats
        System.out.println();
    }  
}
