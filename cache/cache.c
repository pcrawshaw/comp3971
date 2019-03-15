//
// Copy memory blocks and display information about clock cycles used
//

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define DATA_SIZE 512

// Gets the number of clock ticks
static __inline__ unsigned long long rdtsc(void);

// 64 bytes of data
typedef struct {
  char _x[DATA_SIZE];
} data_t;


// Returns the current number of clock ticks
static __inline__ unsigned long long rdtsc(void)
{
    unsigned long long int x;
    __asm__ volatile (".byte 0x0f, 0x31" : "=A" (x));
    return x;
}

//
// Allocate memory in two arrays, and copy blocks of memory from
// one array to the other.
// Measure the number of instructions used for each operation.
//
int main(int argc, char *argv[]) {
    int i;
    long long int before, after;

    // Command line argument is number of iterations
    if (argc < 2) {
        printf("usage: cache <number of iterations>\n");
        exit(1);
    }
    int n = atoi(argv[1]);

    // Allocate memory in two arrays
    // Total memory allocated will be 2*n*64 bytes
    data_t* A = malloc(n * sizeof(data_t));
    data_t* B = malloc(n * sizeof(data_t));

    // Iterate n times, copying blocks of memory
    // from one array to the other
    for (i = 0; i < n; i++) {
        before = rdtsc();       // Get # of clock ticks before copy
        memcpy((void*)&A[i], (void*)&B[i], sizeof(data_t));
        after = rdtsc();        // Get # of clock ticks after copy

        // Compute and display number of clock cycles used for the copy
        printf("Copied %d bytes (Total %d bytes), cycles used: %ld\n", sizeof(data_t), (i+1)*sizeof(data_t), after - before);
    }

    // Free memory
    free(A);
    free(B);
}
