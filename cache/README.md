# Cache performance test

Compile with

```
gcc -o cache-test cache-test.c
```

Use the `perf.sh` script to run the program and collect statistics using [perf] 
(http://www.brendangregg.com/perf.html):

```
$ ./perf.sh
Block size = 8 KB, 4.95913600 sec.

 Performance counter stats for './cache-test 8':

        7411795996      cycles:pu                                                  
       12616612587      instructions:pu           #    1.70  insn per cycle        
        3758204072      L1-dcache-loads:u                                          
            559173      L1-dcache-load-misses:u   #    0.01% of all L1-dcache hits 

       4.968913589 seconds time elapsed

       4.963807000 seconds user
       0.000000000 seconds sys


Block size = 256 KB, 5.98246000 sec.

 Performance counter stats for './cache-test 256':

        8943801815      cycles:pu                                                  
       12616612656      instructions:pu           #    1.41  insn per cycle        
        3758214610      L1-dcache-loads:u                                          
            908691      L1-dcache-load-misses:u   #    0.02% of all L1-dcache hits 

       5.992313679 seconds time elapsed

       5.977824000 seconds user
       0.007991000 seconds sys
```
