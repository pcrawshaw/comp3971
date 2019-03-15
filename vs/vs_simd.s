.text
.global vector_sum

// void vector_sum(int16_t* C, int16_t* A, int16_t* B, size_t n)
// Sum two arrays of 16-bit ints using SIMD registers
// Process the arrays 32 elements at a time
// Base addrs of C, A, and B arrays in x0, x1, x2
// Number of vector elements is in x3
vector_sum:
	stp x29, x30, [sp, #-16]! // Save registers

	// Make x3 the end of the dest array
	lsl x3, x3, #1    // mul by 2 for data size
	add x3, x3, x0    // add to base address

	// Load 32 values from each source array
	// into 4 SIMD registers (8 in each)
	// Then post-increment x1 and x2 by 64
	// to move to the next block of 32 values
.loop:	ld1 {v9.8h - v12.8h}, [x1], #64
	ld1 {v13.8h - v16.8h}, [x2], #64

	// We have 32 values from arrays A and B in two
	// sets of 4 SIMD registers (v9-v12 and v13-v16)
	add v0.8h, v9.8h, v13.8h
	add v1.8h, v10.8h, v14.8h
	add v2.8h, v11.8h, v15.8h
	add v3.8h, v12.8h, v16.8h

	// Results are in v0-v3, store in array C
	st1 {v0.8h - v3.8h}, [x0], #64

	// If we have not reached the end of array C,
	// loop and process another 32 values
	cmp x0, x3
	bne .loop

	// Restore registers and return
	ldp x29, x30, [sp], #16
	ret
