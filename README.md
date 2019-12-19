
# Low Level Persistence Library #

## OVERVIEW ##
The Low Level Persistence Library offers access to persistent memory using `MemoryBlocks` allocated from
a persistent Heap.  

This Java library uses the Persistent Memory Development Kit (PMDK).
For more information on PMDK, please visit http://pmem.io and https://github.com/pmem/pmdk.

## HOW TO BUILD & RUN ##

### PREREQUISITES TO BUILD ###
The following are the prerequisites for building this Java library:

1. Linux operating system
2. Persistent Memory Development Kit (PMDK)
3. Java 8 or above
4. Build tools - `g++` compiler, `CMake` and `Maven`

### PREREQUISITES TO RUN ###
This library assumes the availability of hardware persistent memory or emulated persistent memory.  Instructions for creating emulated persistent memory are shown below.

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
   $ mvn compile
   $ mvn test -Dtest.heap.path=<path to persistent memory mount point>
   ```
Available Maven commands include:

   - `compile` - builds sources
   - `test` - builds and runs tests
   - `javadoc:javadoc` - builds javadocs into ```target/site/apidocs```
   - `package` - builds jar file into ```target``` directory

### USING THIS LIBRARY IN EXISTING JAVA APPLICATIONS ###
To import this library into an existing Java application, include the project's target/classes 
directory in your Java classpath and the project's ```target/cppbuild``` directory in your 
```java.library.path```.  For example: 
   ```
   $ javac -cp .:<path>/llpl/target/classes <source>
   $ java -cp .:<path>/llpl/target/classes -Djava.library.path=<path>/llpl/target/cppbuild <class>
   ```

## CONTRIBUTING ##
Thanks for your interest! Please see the CONTRIBUTING.md document for information on how to contribute.

We would love to hear your comments and suggestions via https://github.com/pmem/llpl/issues.

## Contacts ##
For more information on this library, contact Olasoji Denloye (olasoji.denloye@intel.com) or Steve Dohrmann
(steve.dohrmann@intel.com).
