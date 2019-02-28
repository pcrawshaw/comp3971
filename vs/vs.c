//
// Driver program for vector_sum.s
//

// Compile: gcc -o vs vs.c vs_simd.s

#include <stdio.h>
#include <stdint.h>

#define SIZE 256

int16_t A[SIZE], B[SIZE], C[SIZE];

void vector_sum(int16_t* c, int16_t* a, int16_t* b, size_t n);

int main() {
  size_t i;
  for (i=0; i < SIZE; i++) {
    A[i] = i;
    B[i] = i;
  }

  vector_sum(C, A, B, SIZE);

  for (i=0; i < SIZE; i++)
    printf("%d\n", C[i]);

}
