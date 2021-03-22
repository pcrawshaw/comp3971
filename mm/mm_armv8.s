// ARMv8 version of matrix multiply. See p. 223
// Dimensions changed to 4x4

	.text
	.global mm_armv8

mm_armv8:
	// Save registers on stack
        // Value of SP must be quadword aligned (multiple of 16 bytes)
        // So we subtract 48 bytes, even though we only need 40 bytes
	sub sp, sp, #48
	stur x30, [sp, #32]
	stur x29, [sp, #24]
	stur x21, [sp, #16]
	stur x20, [sp, #8]
	stur x19, [sp, #0]

	// Initialize index vars
	mov x10, #4  // row size, loop end
	mov x19, #0  // i = 0
L1:	mov X20, #0  // j = 0
L2:	mov x21, #0  // k = 0

	// Need to calculate c[i][j]
	// We want the ith row
	lsl x11, x19, #2   // x11 = i * 2^2 (size of 1 row)

        // Get jth element of row i
	add x11, x11, x20

	// Matrix elements are 4 bytes (singe-prec FP), so mult by 4
	lsl x11, x11, #2

	// Now we have the offset of c[i][j],
	// Add this to the base address of c, which is x2 (3rd arg)
	// And load into 32-bit FP register s4
	add x11, x2, x11
	ldur s4, [x11, #0]

	// Use a similar sequence of instructions to get b[k][j]
        // Load into 32-bit FP register s5
L3:	lsl x9, x21, #2
	add x9, x9, x20
	lsl x9, x9, #2
	add x9, x1, x9
	ldur s5, [x9, #0]

	// Use a similar sequence of instructions to get a[i][k]
        // Load into 32-bit FP register s6
	lsl x9, x19, #2
	add x9, x9, x21
	lsl x9, x9, #2
	add x9, x0, x9
	ldur s6, [x9, #0]

	// Now we have our values loaded in FP registers,
	// so we multiply and accumulate the sum in d4
//	fmul	s5, s6, s5  // s5 = a[i][k] * b[k][j]
//	fadd	d4, d4, s5    // d4 = c[i][j] + a[i][k] * b[k][j]
	fmadd	s4, s6, s5, s4 // Use fused multiply+add

	// Increment k, loop back if not yet 32, otherwise
	// innermost loop is done, so store accumulated value in c[i][j]
	add x21, x21, #1
	cmp x21, x10
	b.lt L3
	stur s4, [x11, #0]

	// Increment and test for middle loop
	add x20, x20, #1
	cmp x20, x10
	b.lt L2

	// Increment and test for outermost loop
	add x19, x19, #1
	cmp x19, x10
	b.lt L1

	// Restore registers from stack
        ldur x30, [sp, #32]
        ldur x29, [sp, #24]
        ldur x21, [sp, #16]
        ldur x20, [sp, #8]
        ldur x19, [sp, #0]
	add sp, sp, #48
	ret
