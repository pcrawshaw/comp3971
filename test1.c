// Compile with gcc -o test1 test1.c
// Generate ASM code only gcc -S test1.c
//   This will create test1.s

long long int func(long long int a, long long int b, long long int c) {
  long long int f = (a + b) - c;
  return f;
}
