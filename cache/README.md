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

        7152084308      cycles:pu                                                  
       12616612567      instructions:pu           #    1.76  insn per cycle        
        3758198889      L1-dcache-loads:u                                          
             26117      L1-dcache-load-misses:u   #    0.00% of all L1-dcache hits 

       4.799416983 seconds time elapsed

       4.790306000 seconds user
       0.003998000 seconds sys


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
