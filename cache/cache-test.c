// Read and wite memory blocks to demonstrate how program performnce
// is affected by caching.

// Compile with: gcc -o cache-test cache-test.c

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define KB 1024
#define MB 1024 * 1024

int main(int argc, char *argv[]) {
    static int arr[4 * 1024 * 1024];        // 16MB array (4M ints)
    unsigned int steps = 256 * 1024 * 1024; // 256 million steps

    // Command line argument is data size
    if (argc < 2) {
        printf("usage: cache-test <data size in KB>\n");
        exit(1);
    }
    int length = (atoi(argv[1]) * KB) / sizeof(int);

    // Do loads and stores 256 million times on a block
    // of memory of size `length` bytes.
    // We use up to length bytes of the array for each block size.
    clock_t start = clock();
    for (int i = 0; i < steps; i++) {
        arr[i % length] *= 10;
        arr[i % length] /= 10;
    }
    double elapsed = (double)(clock() - start) / CLOCKS_PER_SEC;

    printf("%.8f sec.\n",elapsed);

    return 0;
}
