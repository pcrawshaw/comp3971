#!/bin/sh

# Run the test program and collect stats using 8KB and 256KB data blocks

perf stat \
  -e cycles:p \
  -e instructions:p \
  -e L1-dcache-loads:p \
  -e L1-dcache-load-misses:p \
  ./cache-test 8

perf stat \
  -e cycles:p \
  -e instructions:p \
  -e L1-dcache-loads:p \
  -e L1-dcache-load-misses:p \
  ./cache-test 256
