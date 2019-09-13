#!/bin/bash
# Simple regression test suite.
java -jar ./build/hojo.jar --minimal < ./test/example.hjo > ./test/actual.txt 2>&1 && diff -u ./test/expected.txt ./test/actual.txt || exit 1
