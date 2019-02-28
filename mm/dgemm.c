//
// C driver program for matrix multiplication
//

// Compile: gcc -o dgemm dgemm.c mm_armv8.s mm_neon.s
//          You can also just type 'make' (see the Makefile)
//
// Run: ./dgemm


#include <stdio.h>
#include <stdlib.h>

#define SIZE 4    // 4x4 matrix

// Pointers to three 4x4 matrices. Memory will be allocated in main()
float *A, *B, *C;

// Prototype for assembly version of matrix multiply in file mm_armv8.s
void mm_armv8(float *c, float *a, float *b, size_t n);

// Prototype for NEON SIMD assembly version of matrix multiply which
// will be in the file mm_neon.s
void mm_neon(float *c, float *a, float *b, size_t n);

// Unoptimized matrix multiply using single-dimenional arrays to
// represent matrices. n is matrix dimension
void mm(float* C, float* A, float* B, size_t n)
{
  for (size_t i=0; i < n; i++)           // rows
    for (size_t j=0; j < n; j++)         // columns
    {
      // For each C[i,j], compute dot product
      //   of ith row of A and jth col of B
      float cij = C[i*n + j];           // cij = C[i][j]
      for (size_t k=0; k < n; k++)
        cij += A[i*n + k] * B[k*n + j];  // cij = A[i][k] * B[k][j]

      C[i*n + j] = cij;                  // C[i][j] = cij
    }
}

// Utility functions
void fill_matrix(float *m, size_t n)
{
  size_t i, j;
  for (i=0; i < SIZE; i++)
    for (j=0; j < SIZE; j++)
       m[i*n + j] = drand48();
}

void zero_matrix(float *m, size_t n) {
  size_t i, j;
  for (i=0; i < SIZE; i++)
    for (j=0; j < SIZE; j++)
       m[i*n + j] = 0.0;
}


void print_matrix(float *m, size_t n) {
  size_t i, j;
  for (i=0; i < SIZE; i++) {
    for (j=0; j < SIZE; j++) {
      printf("%6.2f ", m[i*n + j]);
    }
    printf("\n");
  }
  printf("\n");
}


// Main program

int main() {
  // Allocate memory for each of three 4x4 matrices
  // Each element is 4 bytes (a single-prec FP value).
  // so we have 16 * 4 = 64 bytes for each matrix
  A = malloc(SIZE * SIZE * sizeof(float));
  B = malloc(SIZE * SIZE * sizeof(float));
  C = malloc(SIZE * SIZE * sizeof(float));

  // Fill A and B with random values
  fill_matrix(A, SIZE);
  fill_matrix(B, SIZE);
  puts("A = ");
  print_matrix(A, SIZE);
  puts("B = ");
  print_matrix(B, SIZE);

  // Test C version of matrix multiply
  zero_matrix(C, SIZE);
  mm(C, A, B, SIZE);
  puts("Using C code, C = ");
  print_matrix(C, SIZE);

  // Test the ARMv8 assembly version of matrix multiply
  zero_matrix(C, SIZE);
  mm_armv8(C, A, B, SIZE);
  puts("Using ARMv8 ASM, C =");
  print_matrix(C, SIZE);

  // Test the Neon version of matrix multiply
  zero_matrix(C, SIZE);
  mm_neon(C, B, A, SIZE);
  puts("Using ARMv8 NEON ASM, C = ");
  print_matrix(C, SIZE);
}
