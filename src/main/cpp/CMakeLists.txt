# 
# Copyright (C) 2018-2019 Intel Corporation
#
# SPDX-License-Identifier: BSD-3-Clause
# 
#

cmake_minimum_required(VERSION 2.8.9)
project(llpl)
set(CMAKE_BUILD_TYPE Release)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -shared -D_FORTIFY_SOURCE=2 -z noexecstack -z,relro -z,now -Wformat -Wformat-security -Werror=format-security")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -pie")

#include Java packages
find_package(Java REQUIRED)
FIND_PACKAGE(JNI REQUIRED)

link_directories(/usr/local/lib64)

INCLUDE_DIRECTORIES(${JAVA_INCLUDE_PATH})
INCLUDE_DIRECTORIES(${JAVA_INCLUDE_PATH2})
LINK_DIRECTORIES(/usr/lib)

include_directories(include)

#Add the source files
file(GLOB SOURCES "*.cpp")

#Generate the shared library from the sources
add_library(llpl SHARED ${SOURCES})
#Add dependency for pmemobj library
target_link_libraries(llpl pmemobj pmempool)
