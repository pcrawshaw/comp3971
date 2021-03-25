# Cache performance test

Compile with

```
gcc -o cache-test cache-test.c
```

Use the `perf.sh` script to run the program and collect statistics using [perf] 
(http://www.brendangregg.com/perf.html):

```
$ ./perf.sh
Block size = 8192 KB, 5.71319400 sec.

 Performance counter stats for './cache-test 8':

        8535652282      cycles:pu                                                  
       12616612604      instructions:pu           #    1.48  insn per cycle        
        3758205303      L1-dcache-loads:u                                          
             32921      L1-dcache-load-misses:u   #    0.00% of all L1-dcache hits 

       5.724108672 seconds time elapsed

       5.718078000 seconds user
       0.000000000 seconds sys


Block size = 262144 KB, 4.61167200 sec.

 Performance counter stats for './cache-test 256':

        6889628423      cycles:pu                                                  
       12616612632      instructions:pu           #    1.83  insn per cycle        
        3758208166      L1-dcache-loads:u                                          
            926922      L1-dcache-load-misses:u   #    0.02% of all L1-dcache hits 

       4.621178867 seconds time elapsed

       4.607011000 seconds user
       0.007998000 seconds sys
```
