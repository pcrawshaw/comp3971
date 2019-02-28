//
// bad.s
//
// Warning! This will crash with a segmentation fault
//
// Assemble with "gcc -g -o bad bad.s"
// Run under the GNU debugger with "gdb ./bad"
//

.text
.align 2
.global main

	// Save registers
main:	stp x29, x30, [sp, -16]!

	// Put a bogus memory address in register x9
	movz x9, 0xdead, lsl 16 // bits [31:16]
	movk x9, 0xbeef         // bits [15:0]

	// Try to store something at the (invalid!) memory address in x9
        // *** This should cause a segmentation fault ***
	str x0, [x9]

	// Restore FP (X29) and LR (X30)
        ldp     x29, x30, [sp], 16
        mov     w0, 0   // exit value
        ret             // return from main()
