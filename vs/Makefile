CC=gcc
CFLCAGS="-g"

%.o: %.c
	$(CC) -c -o $@ $< $(CFLAGS)

vs: vs.o vs_simd.o
	$(CC) -o vs vs.o vs_simd.o
