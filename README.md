
# Low Level Persistence Library #

## OVERVIEW ##
The Low Level Persistence Library offers access to persistent memory using `MemoryBlocks` allocated from
a persistent Heap.  

This Java library uses the **libpmemobj** library from the Persistent Memory Development Kit (PMDK).
For more information on PMDK, please visit http://pmem.io and https://github.com/pmem/pmdk.

## HOW TO BUILD & RUN ##

### PREREQUISITES TO BUILD ###
The following are the prerequisites for building this Java library:

1. Linux operating system (tested on CentOS 7.2)
2. Persistent Memory Development Kit (PMDK)
3. Java 8 or above
4. Build tools - `g++` compiler and `make`

### PREREQUISITES TO RUN ###
This library assumes the availability of hardware persistent memory or emulated persistent memory.  Instructions for creating emulated persistent memory are shown below.

### EMULATING PERSISTENT MEMORY ###
The preferred way is to create an in-memory DAX file system. This requires Linux kernel 4.2 or 
greater. Please follow the steps at:

   http://pmem.io/2016/02/22/pm-emulation.html

Alternatively, for use with older kernels, create a tmpfs partition as follows (as root):
   ```
   $ mount -t tmpfs -o size=8G tmpfs /mnt/mem  # creates a 8GB tmpfs partition
   $ chmod -R a+rw /mnt/mem                    # enables read/write permissions to all users
   ```
### STEPS TO BUILD AND RUN TESTS ###
Once all the prerequisites have been satisfied:
   ```
   $ git clone https://github.com/pmem/llpl.git
   $ cd llpl
   $ make && make tests
   ```
Available Makefile targets include:

   - `sources` - builds only sources
   - `examples` - builds the sources and examples
   - `tests` - builds and runs tests
   - `docs` - builds javadocs
   - `jar` - builds jar file into ```target``` directory

### USING THIS LIBRARY IN EXISTING JAVA APPLICATIONS ###
To import this library into an existing Java application, include the project's target/classes 
directory in your Java classpath and the project's ```target/cppbuild``` directory in your 
```java.library.path```.  For example: 
   ```
   $ javac -cp .:<path>/llpl/target/classes <source>
   $ java -cp .:<path>/llpl/target/classes -Djava.library.path=<path>/llpl/target/cppbuild <class>
   ```

## CONTRIBUTING ##
Thanks for your interest! Right now, architectural changes are still happening in the
project.  This makes it difficult to contribute code and difficult to effectively process pull
requests.  We expect these changes to settle out around March of this year and we look forward to
code contributions once this happens.  We will update this README then.

In the meantime, we would love to hear your comments and suggestions via https://github.com/pmem/llpl/issues.

## Contacts ##
For more information on this library, contact Olasoji Denloye (olasoji.denloye@intel.com) or Steve Dohrmann
(steve.dohrmann@intel.com).
