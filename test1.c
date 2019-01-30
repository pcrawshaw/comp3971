// Compile with gcc -o test1 test1.c
// Generate ASM code only gcc -S test1.c
//   This will create test1.s

long int func(long int a, long int b, long int c) {
  int f = (a + b) - c;
  return f;
}
