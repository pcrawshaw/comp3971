//
// Demonstrate some ARMv8 SIMD instructions and how to use printf
//

/*
  Compile: gcc -o simd simd.s
  Compile with debug info: gcc -o simd -g simd.s

  Run: ./simd
  Run with GDB debugger: gdb ./simd
  [See GDB tutorial]
*/


//
// Define some data
//
	.section	.rodata
	.align	3

// A format string for printf()
fmtstr:	.string	"%f %f %f %f\n"

// Some double-precision values (8 bytes each)
a:	.double	1.0
	.double	2.0
	.double 3.0
	.double	4.0
	.double	5.0
	.double	6.0
	.double 7.0
	.double	8.0

// Some single-precision values (4 bytes each)
b:	.float 1.0
	.float 2.0
	.float 3.0
	.float 4.0
	.float 5.0
	.float 6.0
	.float 7.0
	.float 8.0
//
// Code section starts here
//
	.text
	.align	2

	.global	main
main:
	// Save X29 (FP) and X30 (LR), then decrement SP (X31)
	// Note: SP must be quadword-aligned (16 bytes)
	stp	x29, x30, [sp, -16]! // ! Means pre-indexed addressing mode

	adr x9, a       // Get addr of a

	// Load 8 64-bit FP values into v16, v17, v18, and v19 (2 in each)
	ld1 {v16.2d - v19.2d}, [x9]

	// Some by-element vector adds
	fadd v0.2d, v16.2d, v17.2d
	fadd v1.2d, v18.2d, v19.2d

        // More example operations
	movi v2.2d, #0               // Puts 0 in v2.2d[0] and v2.2d[1]
	fmla v2.2d, v16.2d, v17.2d   // Fused multiply+add
	fmla v2.2d, v18.2d, v19.2d

        // Load 8 32-bit floats into v20 and v21 (4 in each)
	adr x9, b                    // Address of b in x9
	ld1 {v11.4s - v12.4s}, [x9]  // Load from addr in x9

        // Do some operations
	fadd v3.4s, v11.4s, v12.4s
	fmla v4.4s, v11.4s, v12.4s[0]  // See docs for how this works
	movi v5.4s, #0
	faddp v5.4s, v11.4s, v12.4s

	mul V11.4S, V12.4S, V13.4S[0]  // See docs

	// Load address of format string in X0
	adrp	x0, fmtstr           // Get base address of 4KB page
	add	x0, x0, :lo12:fmtstr // Add lower 12 bits of address
	//adr x0, fmtstr             // Could use instead of above

	// Call printf
	// See section 5.4.2 of "Procedure Call Standard for the
	// ARM 64-bit Architecture" for detailed parameter passing rules
	bl printf

        // Restore FP (X29) and LR (X30)
	ldp	x29, x30, [sp], 16    // Post-indexed addressing mode
	mov	w0, 0                 // exit value
	ret                           // return from main()
