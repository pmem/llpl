# Low-Level Persistence Library #

## OVERVIEW ##
The Low-Level Persistence Library (LLPL) is a Java library that provides access to off-heap persistent memory.
LLPL includes several kinds of components that can be allocated and used alone or together in building applications:
* <b>heaps</b>: a pool of memory and an allocator for it
* <b>memory blocks</b>: unstructured bytes that can be laid out for any purpose and linked to build data structures
* <b>pre-built data structures</b>: arrays, linked list, and radix trees
* <b>memory pools</b>: a process-shareable pool of memory

Data stored in the components above can persist beyond the life of a JVM instance, i.e. across application or system restarts.
LLPL provides APIs that help developers ensure consistency of stored data.

Memory allocated using LLPL is not garbage-collected and must be explicitly deallocated using LLPL APIs.

LLPL uses the Persistent Memory Development Kit (PMDK).
For more information on PMDK, please visit http://pmem.io and https://github.com/pmem/pmdk.

## HOW TO BUILD & RUN ##

### PREREQUISITES TO BUILD ###
The following are the prerequisites for building this Java library:

1. Linux operating system
2. Persistent Memory Development Kit (PMDK) v1.5 or newer
3. Java 8 or newer
4. Build tools - `g++` compiler, `CMake` and `Maven`

### PREREQUISITES TO RUN ###
This library assumes the availability of hardware persistent memory or emulated persistent memory.
Instructions for creating emulated persistent memory are shown below.

### EMULATING PERSISTENT MEMORY ###
The preferred way is to create an in-memory DAX file system. This requires Linux kernel 4.2 or 
greater. Please follow the steps at:

   http://pmem.io/2016/02/22/pm-emulation.html

Alternatively, for use with older kernels, create a tmpfs partition as follows (as root):
   ```
   $ mount -t tmpfs -o size=4G tmpfs /mnt/mem  # creates a 4GB tmpfs partition
   $ chmod -R a+rw /mnt/mem                    # enables read/write permissions to all users
   ```
### STEPS TO BUILD AND RUN TESTS ###
Once all the prerequisites have been satisfied:
   ```
   $ git clone https://github.com/pmem/llpl.git
   $ cd llpl
   $ mvn test -Dtest.heap.path=<path to persistent memory mount point>
   ```
Available Maven commands include:

   - `compile` - builds sources
   - `test` - builds and runs tests
   - `javadoc:javadoc` - builds javadocs into ```target/site/apidocs```
   - `package` - builds jar file into ```target``` directory

### USING THIS LIBRARY IN EXISTING JAVA APPLICATIONS ###
#### WITH MAVEN ####
LLPL is available from the Maven central repository. Add the following dependency to your pom.xml:
```
<dependency>
    <groupId>com.intel.pmem</groupId>
    <artifactId>llpl</artifactId>
    <version>1.1.0-release</version>
    <type>jar</type>
</dependency>
```

#### WITH CLASSPATH ####  
To use this library in your Java application, build the LLPL jar and include 
its location in your Java classpath.  For example:
   ```
   $ mvn package
   $ javac -cp .:<path>/llpl/target/llpl-<version>.jar <source>
   $ java -cp .:<path>/llpl/target/llpl-<version>.jar <class>
   ```

Alternatively, include LLPL's `target/classes` directory in your Java classpath and the
`target/cppbuild` directory in your `java.library.path`.  For example:
   ```
   $ mvn compile
   $ javac -cp .:<path>/llpl/target/classes <source>
   $ java -cp .:<path>/llpl/target/classes -Djava.library.path=<path>/llpl/target/cppbuild <class>
   ```

## CONTRIBUTING ##
Thanks for your interest! Please see the CONTRIBUTING.md document for information on how to contribute.

We would love to hear your comments and suggestions via https://github.com/pmem/llpl/issues.

## Contacts ##
For more information on this library, contact Olasoji Denloye (olasoji.denloye@intel.com), Matt Welch (matt.welch@intel.com), or Steve Dohrmann (steve.dohrmann@intel.com).
