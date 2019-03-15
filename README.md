# comp3971

## Simple C examples
* test1.c Simple example function
* test2.c Calling a function
* bad.c This program will crash!
* simd.c ARMv8 NEON SIMD examples
* vs/ Vector sum example
* mm/ Matrix multiply skeleton code

## Generating assembly code output from a C program

`gcc -S test.c` will compile the C code into assembly language. 
Add -O to generate optimized code. See `man gcc` fo additional compiler options.
