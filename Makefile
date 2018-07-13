# 
# Copyright (C) 2018 Intel Corporation
#
# SPDX-License-Identifier: BSD-3-Clause
# 
#

CC = g++
JAVAC = $(JAVA_HOME)/bin/javac
JAVA = $(JAVA_HOME)/bin/java

JNI_INCLUDES = $(JAVA_HOME)/include $(JAVA_HOME)/include/linux

CFLAGS = -O3 -DNDEBUG -fPIC
JAVAFLAGS = -Xlint:unchecked -proc:none -XDenableSunApiLintControl
LINK_FLAGS = -fPIC -O3 -DNDEBUG -shared -lpmem -lpmemobj -Wl,-rpath,/usr/local/lib:/usr/local/lib64

CPP_SOURCE_DIR = src/main/cpp
JAVA_SOURCE_DIR = src/main/java
PACKAGE_NAME = lib/llpl

TEST_DIR = src/test/java/$(PACKAGE_NAME)

TARGET_DIR = target
CPP_BUILD_DIR = $(TARGET_DIR)/cppbuild
CLASSES_DIR = $(TARGET_DIR)/classes
TEST_CLASSES_DIR = $(TARGET_DIR)/test_classes

BASE_CLASSPATH = $(CLASSES_DIR):lib

ALL_CPP_SOURCES = $(wildcard $(CPP_SOURCE_DIR)/*.cpp)
ALL_JAVA_SOURCES = $(wildcard $(JAVA_SOURCE_DIR)/$(PACKAGE_NAME)/*.java)
ALL_OBJ = $(addprefix $(CPP_BUILD_DIR)/, $(notdir $(ALL_CPP_SOURCES:.cpp=.o)))

ALL_TEST_SOURCES = $(addprefix $(TEST_DIR)/, FlushableMemoryBlockTest.java TransactionalMemoryBlockTest.java RawMemoryBlockTest.java MemoryBlockCollectionTest.java MemoryBlockFreeTest.java MemoryBlockEqualityTest.java CopyMemoryTest.java SetMemoryTest.java ReallocateTest.java TransactionTest.java UnboundedMemoryBlockTest.java MultipleHeapTest.java)
ALL_TEST_CLASSES = $(addprefix $(TEST_CLASSES_DIR)/, $(notdir $(ALL_TEST_SOURCES:.java=.class)))

LIBRARIES = $(addprefix $(CPP_BUILD_DIR)/, libllpl.so)

EXAMPLES_DIR = src/examples
ALL_EXAMPLE_DIRS = $(wildcard $(EXAMPLES_DIR)/*)
#$(addprefix $(EXAMPLES_DIR)/, reservations employees)

all: sources examples testsources
sources: cpp java
cpp: $(LIBRARIES)
java: classes

examples: sources
	$(foreach example_dir,$(ALL_EXAMPLE_DIRS), $(JAVAC) $(JAVAFLAGS) -cp $(BASE_CLASSPATH):$(example_dir) $(example_dir)/*.java;)

testsources: sources
	$(JAVAC) $(JAVAFLAGS) -cp $(BASE_CLASSPATH):src $(TEST_DIR)/*.java;

clean: cleanex
	rm -rf target

cleanex:
	$(foreach example_dir,$(ALL_EXAMPLE_DIRS), rm -rf $(example_dir)/*.class;)

tests: $(ALL_TEST_CLASSES)
	$(foreach test,$^, $(JAVA) -ea -cp $(BASE_CLASSPATH):$(TEST_CLASSES_DIR) -Djava.library.path=$(CPP_BUILD_DIR) $(PACKAGE_NAME)/$(notdir $(test:.class=));)

$(LIBRARIES): | $(CPP_BUILD_DIR)
$(ALL_OBJ): | $(CPP_BUILD_DIR)
$(ALL_TEST_CLASSES): | $(TEST_CLASSES_DIR)

classes: | $(CLASSES_DIR)
	$(JAVAC) $(JAVAFLAGS) -d $(CLASSES_DIR) -cp $(BASE_CLASSPATH) $(ALL_JAVA_SOURCES)

$(TEST_CLASSES_DIR)/%.class: $(TEST_DIR)/%.java
	$(JAVAC) -cp $(BASE_CLASSPATH):$(TEST_CLASSES_DIR) -d $(TEST_CLASSES_DIR) $<

$(CPP_BUILD_DIR)/%.so: $(ALL_OBJ)
	$(CC) -Wl,-soname,$@ -o $@ $(ALL_OBJ) $(LINK_FLAGS)

$(CPP_BUILD_DIR)/%.o: $(CPP_SOURCE_DIR)/%.cpp
ifndef JAVA_HOME
	$(error JAVA_HOME not set)
endif
	$(CC) $(CFLAGS) $(addprefix -I, $(JNI_INCLUDES)) -o $@ -c $<

$(CPP_BUILD_DIR):
	mkdir -p $(CPP_BUILD_DIR)

$(CLASSES_DIR):
	mkdir -p $(CLASSES_DIR)

$(TEST_CLASSES_DIR):
	mkdir -p $(TEST_CLASSES_DIR)
