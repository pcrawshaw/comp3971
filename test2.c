// Compile with gcc -o test2 test2.c
// Generate ASM code only gcc -S test2.c
//   This will create test2.s

#include <stdio.h>

#define A 1
#define B 2
#define C 3

long long int func(long long int a, int b, int c) {
  int f = (a + b) * c;
  return f;
}

int main() {
   long long int r = func(A, B, C);
   printf("%lld\n", r);
}
