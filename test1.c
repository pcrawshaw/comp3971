// Compile with gcc -o test1 test1.c
// Generate ASM code only gcc -S test1.c
//   This will create test1.s

int func(int a, int b, int c) {
  int f = (a + b) - c;
  return f;
}
