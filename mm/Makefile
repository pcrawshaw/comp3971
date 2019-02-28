#
# Makefile for matrix multiply example
#

CC=gcc
AS=as

# Rule for compiling C files to object file
%.o: %.c
	$(CC) -c -o $@ $<

# Rule for assembling ASM file into object file
%.o: %.s
	$(AS) -c -o $@ $<

# Rule for building dgemm executable
dgemm: dgemm.o mm_armv8.o mm_neon.o
	$(CC) -o dgemm dgemm.o mm_armv8.o mm_neon.o

# Rule to delete object and executable files
clean:
	rm -f dgemm *.o
